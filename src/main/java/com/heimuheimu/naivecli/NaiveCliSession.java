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

import com.heimuheimu.naivecli.command.NaiveCommandExecutor;
import com.heimuheimu.naivecli.constant.BeanStatusEnum;
import com.heimuheimu.naivecli.socket.TextualSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 与命令行工具调用方建立的 Session，通过 Socket 进行通信
 *
 * @author heimuheimu
 */
public class NaiveCliSession implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NaiveCliSession.class);

    private static final AtomicLong THREAD_NUMBER = new AtomicLong();

    /**
     * 当前实例所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.UNINITIALIZED;

    /**
     * Session 上一次活跃时间
     */
    private volatile long lastActiveTime = System.currentTimeMillis();

    /**
     * 与调用方建立的 Socket 连接
     */
    private final Socket socket;

    /**
     * 命令执行器
     */
    private final NaiveCommandExecutor executor;

    /**
     * 将字节通信的 Socket 封装为文本形式通信的 Socket
     */
    private volatile TextualSocket textualSocket;

    /**
     * IO 线程
     */
    private IoThread ioThread;

    /**
     * 构造一个与命令行工具调用方建立的 Session
     *
     * @param socket 与调用方建立的 Socket 连接
     */
    public NaiveCliSession(Socket socket, NaiveCommandExecutor naiveCommandExecutor) {
        this.socket = socket;
        this.executor = naiveCommandExecutor;
    }

    public synchronized void init() {
        if (state == BeanStatusEnum.UNINITIALIZED) {
            try {
                if (socket.isConnected() && !socket.isClosed()) {
                    textualSocket = new TextualSocket(socket);
                    ioThread = new IoThread();
                    ioThread.setDaemon(true);
                    long threadNumber = THREAD_NUMBER.incrementAndGet();
                    ioThread.setName("NaiveCliSession-" + threadNumber  + "-" + socket.getInetAddress().getCanonicalHostName());
                    ioThread.start();
                    state = BeanStatusEnum.NORMAL;
                    LOGGER.info("NaiveCliSession has benn initialized. Thread number: `{}`. Socket: `{}`.", threadNumber, socket);
                } else {
                    LOGGER.error("NaiveCliSession init failed: `Socket is not connected or has been closed`. Socket: `{}`.", socket);
                    close();
                }
            } catch (Exception e) {
                LOGGER.error("NaiveCliSession init failed: `" + e.getMessage() + "`. Socket: `" + socket + "`.", e);
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
                //关闭Socket连接
                socket.close();
                //停止IO线程
                ioThread.stopSignal = true;
                ioThread.interrupt();
                LOGGER.info("NaiveCliSession has been closed. Cost: {}ms. Socket: `{}`.",
                        (System.currentTimeMillis() - startTime), socket);
            } catch (Exception e) {
                LOGGER.error("Close NaiveCliSession failed. Unexpected error. Socket: `" + socket + "`.", e);
            }
        }
    }

    /**
     * 判断当前 Session 是否活跃
     *
     * @return 是否活跃
     */
    public boolean isActive() {
        return state == BeanStatusEnum.NORMAL;
    }

    /**
     * 获得当前 Session 已闲置的秒数
     *
     * @return 当前 Session 已闲置的秒数
     */
    public int getIdleSeconds() {
        return (int) TimeUnit.SECONDS.convert(System.currentTimeMillis() - lastActiveTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "NaiveCliSession{" +
                "state=" + state +
                ", lastActiveTime=" + lastActiveTime +
                ", socket=" + socket +
                '}';
    }

    private class IoThread extends Thread {

        private volatile boolean stopSignal = false;

        @Override
        public void run() {
            try {
                while (!stopSignal) {
                    String input = textualSocket.readLine();
                    if (input != null) {
                        input = input.toLowerCase();
                        if (input.equals("quit")) {
                            textualSocket.writeLine("bye bye~");
                            close();
                            break;
                        } else if (input.equals("ping")) {
                            textualSocket.writeLine("pong");
                            lastActiveTime = System.currentTimeMillis();
                        } else {
                            lastActiveTime = System.currentTimeMillis();
                            List<String> outputList = executor.execute(input);
                            for (String output : outputList) {
                                textualSocket.writeLine(output);
                            }
                            lastActiveTime = System.currentTimeMillis();
                        }
                    } else {
                        LOGGER.info("End of the input stream has been reached. Socket: `{}`", socket);
                        close();
                        break;
                    }
                }
            } catch (InterruptedException ignored) {
                //ignored exception
                close();
            } catch (SocketException ignored) {
                //ignored exception
                close();
            } catch(Exception e) {
                LOGGER.error("[IoThread] NaiveCliSession need to be closed: `" + e.getMessage() + "`. Socket: `" + socket + "`.", e);
                close();
            }
        }
    }
}
