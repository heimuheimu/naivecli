/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.naivecli;

import com.heimuheimu.naivecli.command.NaiveCommand;
import com.heimuheimu.naivecli.command.NaiveCommandExecutor;
import com.heimuheimu.naivecli.constant.BeanStatusEnum;
import com.heimuheimu.naivecli.socket.TextualSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 为 Java 项目提供命令行工具，命令的输入与输出均通过文本形式进行交互
 *
 * @author heimuheimu
 */
public class NaiveCommandLineUtilities implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NaiveCommandLineUtilities.class);

    private BeanStatusEnum state = BeanStatusEnum.UNINITIALIZED;

    private final CopyOnWriteArrayList<NaiveCliSession> currentSessionList = new CopyOnWriteArrayList<>();

    /**
     * 命令行工具 Socket 监听端口
     */
    private final int port;

    /**
     * 命令行工具允许同时存在的最大 {@link NaiveCliSession} 数量
     */
    private final int maxSessions;

    /**
     * NaiveCliSession 允许的最大闲置秒数，超过该时间的 NaiveCliSession 将会被自动关闭
     */
    private final int maxIdleSeconds;

    /**
     * 命令行工具使用的命令执行器
     */
    private final NaiveCommandExecutor naiveCommandExecutor;

    private CliServerThread cliServerThread;

    /**
     * 构造一个命令行工具
     *
     * @param port 监听的端口
     * @param commandList 命令行工具支持的命令列表
     * @param maxSessions 命令行工具允许同时存在的最大 {@link NaiveCliSession} 数量
     * @param maxIdleSeconds  NaiveCliSession 允许的最大闲置秒数，超过该时间的 NaiveCliSession 将会被自动关闭，如果小于等于 0，则不进行自动关闭
     */
    public NaiveCommandLineUtilities(int port, Collection<NaiveCommand> commandList, int maxSessions, int maxIdleSeconds) {
        this.port = port;
        this.naiveCommandExecutor = new NaiveCommandExecutor(commandList);
        this.maxSessions = maxSessions;
        this.maxIdleSeconds = maxIdleSeconds;
    }

    public synchronized void init() {
        if (state == BeanStatusEnum.UNINITIALIZED) {
            try {
                cliServerThread = new CliServerThread();
                cliServerThread.setName("[NaiveCommandLineUtilities]:" + port);
                cliServerThread.setDaemon(true);
                cliServerThread.start();
                state = BeanStatusEnum.NORMAL;
                LOGGER.info("NaiveCommandLineUtilities has benn initialized. Port: `{}`.", port);
            } catch (Exception e) {
                LOGGER.error("NaiveCommandLineUtilities init failed: `" + e.getMessage() + "`. Port: `" + port + "`.", e);
                close();
            }
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            try {
                if (cliServerThread != null) {
                    cliServerThread.close();
                }
                for (NaiveCliSession session : currentSessionList) {
                    session.close();
                }
                LOGGER.info("NaiveCommandLineUtilities has benn closed. Cost: `{} ms`. Port: `{}`.",
                        (System.currentTimeMillis() - startTime), port);
            } catch (Exception e) {
                LOGGER.error("Close NaiveCommandLineUtilities failed: `" + e.getMessage() + "`. Port: `" + port + "`.", e);
            }
        }
    }

    /**
     * 移除不活跃的 NaiveCliSession
     */
    public void removeInactiveSessions() {
        for (NaiveCliSession session : currentSessionList) {
            if (!session.isActive() || (maxIdleSeconds > 0 && session.getIdleSeconds() > maxIdleSeconds)) {
                session.close();
                currentSessionList.remove(session);
                LOGGER.info("Remove inactive session: `{}`.", session);
            }
        }
    }

    private class CliServerThread extends Thread {

        private volatile boolean stopSignal = false;

        private final ServerSocket serverSocket;

        private CliServerThread() throws IOException {
            serverSocket = new ServerSocket(port);
        }

        @Override
        public void run() {
            while (!stopSignal) {
                try {
                    Socket socket = serverSocket.accept();
                    removeInactiveSessions();
                    if (currentSessionList.size() >= maxSessions) {
                        LOGGER.error("Create NaiveCliSession failed: `Too many sessions.` Max sessions: " + maxSessions + ".");
                        try {
                            TextualSocket textualSocket = new TextualSocket(socket);
                            textualSocket.writeLine("Too many sessions. Max sessions: " + maxSessions + ".");
                        } catch (Exception ignored) {}
                        try {
                            socket.close();
                        } catch (Exception ignored) {}
                    } else {
                        NaiveCliSession naiveCliSession = new NaiveCliSession(socket, naiveCommandExecutor);
                        naiveCliSession.init();
                        if (naiveCliSession.isActive()) {
                            currentSessionList.add(naiveCliSession);
                        }
                    }
                } catch (SocketException e) {
                    //do nothing
                } catch (Exception e) {
                    LOGGER.error("Accept NaiveCliSession failed. Port: `" + port + "`.", e);
                }
            }
        }

        private void close() throws IOException {
            this.stopSignal = true;
            serverSocket.close();
        }
    }
}
