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

package com.heimuheimu.naivecli.command;

import java.util.List;

/**
 * 命令行工具支持执行的命令。
 *
 * @author heimuheimu
 */
public interface NaiveCommand {

    /**
     * 获得命令名称，不区分大小写，不能含有空格，不能使用"quit"、"ping"等系统命令名称
     *
     * @return 命令名称
     */
    String getName();

    /**
     * 获得命令的参数说明。
     *
     * @return 命令的参数说明
     */
    default String getArgumentDescription() {
        return "";
    }

    /**
     * 执行该命令，并返回执行信息。
     *
     * @param args 命令执行参数
     * @return 执行返回信息
     */
    List<String> execute(String[] args);
}
