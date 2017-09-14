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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 命令执行器
 *
 * @author heimuheimu
 */
public class NaiveCommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NaiveCommandExecutor.class);

    /**
     * 命令执行器支持的命令 Map
     */
    private final Map<String, NaiveCommand> commandMap;

    public NaiveCommandExecutor(Collection<NaiveCommand> commandList) {
        commandMap = new HashMap<>();
        for (NaiveCommand command : commandList) {
            String commandName = command.getName().toLowerCase();
            NaiveCommand existedCommand = commandMap.get(commandName);
            if (existedCommand != null && existedCommand != command) {
                LOGGER.error("Duplicate command: `" + commandName + "`. Existed command: `" + existedCommand
                        + "`. Replace command: `" + command + "`.");
            }
            commandMap.put(commandName, command);
        }
    }

    /**
     * 执行指定命令
     *
     * @param command 命令内容
     * @return 命令执行后的输出
     */
    public List<String> execute(String command) {
        List<String> output = new ArrayList<>();
        try {
            String[] commandParts = command.split(" ");
            String commandName = commandParts[0].toLowerCase();
            String[] args = new String[commandParts.length - 1];
            if (args.length > 0) {
                System.arraycopy(commandParts, 1, args, 0, args.length);
            }
            NaiveCommand naiveCommand = commandMap.get(commandName);
            if (naiveCommand != null) {
                return naiveCommand.execute(args);
            } else {
                output.add("`" + commandName + "` is not supported.");
                output.add("Supported command:");
                int index = 1;
                for (String supportedCommandName : commandMap.keySet()) {
                    output.add("    " + (index++) + ". " + supportedCommandName);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Execute command failed. command: `" + command + "`.", e);
            output.add("Execute command failed: `" + e.getMessage() + "`.");
        }
        return output;
    }

}
