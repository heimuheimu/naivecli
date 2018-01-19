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

package com.heimuheimu.naivecli.socket;

import java.io.*;
import java.net.Socket;

/**
 * 将字节通信的 Socket 封装为文本形式通信的 Socket。
 *
 * @author heimuheimu
 */
public class TextualSocket {

    /**
     * 被封装的  Socket 实例
     */
    private final Socket socket;

    /**
     * 文本内容读取器
     */
    private final BufferedReader reader;

    /**
     * 文本内容输出器
     */
    private final BufferedWriter writer;

    /**
     * 构造一个文本形式通信的 Socket 实例。
     *
     * @param socket 被封装的  Socket 实例
     * @throws IOException 如果 TextualSocket 创建过程中发生 IO 错误，将抛出此异常
     */
    public TextualSocket(Socket socket) throws IOException {
        this.socket = socket;

        InputStream is = socket.getInputStream();
        this.reader = new BufferedReader(new InputStreamReader(is));

        OutputStream os = socket.getOutputStream();
        this.writer = new BufferedWriter(new OutputStreamWriter(os));
    }

    /**
     * 输出一行文本内容。
     *
     * @param text 输出的文本内容
     * @throws IOException 如果输出过程中发生错误，则抛出此异常
     */
    public void writeLine(String text) throws IOException {
        writer.write(text);
        writer.newLine();
        writer.flush();
    }

    /**
     * 读取一行文本内容，如果输入流已结束，则返回 {@code null}。
     *
     * @return 读取的一行文本内容，如果输入流已结束，则为 {@code null}
     * @throws IOException 如果读取过程中发生错误，则抛出此异常
     * @throws InterruptedException 在读取等待过程中，线程被中断，则抛出此异常
     */
    public String readLine() throws IOException, InterruptedException {
        return reader.readLine();
    }

    @Override
    public String toString() {
        return "TextualSocket{" +
                "socket=" + socket +
                '}';
    }
}
