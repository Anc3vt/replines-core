/*
 * Copyright (C) 2025 Ancevt.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ancevt.replines.core.repl;

import com.ancevt.replines.core.repl.annotation.ReplCommand;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Registry of available commands in the REPL.
 * <p>
 * A registry stores {@link Command} objects and provides methods
 * for registering commands manually, or by scanning annotated classes.
 * It can also generate formatted help text.
 */
public class CommandRegistry {

    private final Set<Command<?>> commands = new LinkedHashSet<>();

    /**
     * Starts definition of a new command using a builder.
     * The command will be automatically registered after {@link Command.Builder#register()} is called.
     *
     * @param words one or more command words (aliases)
     * @param <T>   type of the command result
     * @return builder for creating the command
     */
    public <T> Command.Builder<T> command(String... words) {
        return new Command.Builder<T>(words) {
            @Override
            public Command<T> register() {
                Command<T> cmd = super.register();
                CommandRegistry.this.register(cmd);
                return cmd;
            }
        };
    }

    /**
     * Starts definition of a new command using a builder.
     * The command will be automatically registered after {@link Command.Builder#register()} is called.
     *
     * @param words list of command words (aliases)
     * @param <T>   type of the command result
     * @return builder for creating the command
     */
    public <T> Command.Builder<T> command(List<String> words) {
        return new Command.Builder<T>(words) {
            @Override
            public Command<T> register() {
                Command<T> cmd = super.register();
                CommandRegistry.this.register(cmd);
                return cmd;
            }
        };
    }

    /**
     * Registers commands by scanning a given instance for
     * {@link ReplCommand} annotations.
     *
     * @param instance object with annotated methods or class
     * @return this registry for chaining
     */
    public CommandRegistry register(Object instance) {
        AnnotationCommandLoader.load(this, instance);
        return this;
    }

    /**
     * Registers commands by scanning a list of instances.
     *
     * @param instances objects with annotated methods or classes
     * @return this registry for chaining
     */
    public CommandRegistry register(List<Object> instances) {
        AnnotationCommandLoader.load(this, instances);
        return this;
    }

    /**
     * Registers commands by scanning multiple instances.
     *
     * @param instances objects with annotated methods or classes
     * @return this registry for chaining
     */
    public CommandRegistry register(Object... instances) {
        AnnotationCommandLoader.load(this, instances);
        return this;
    }

    /**
     * Registers commands by scanning a class for
     * {@link ReplCommand} annotations.
     * The class must have a no-arg constructor.
     *
     * @param clazz class with annotated methods or type
     * @return this registry for chaining
     */
    public CommandRegistry registerClass(Class<?> clazz) {
        AnnotationCommandLoader.load(this, clazz);
        return this;
    }

    /**
     * Registers commands by scanning a list of classes.
     *
     * @param classes list of classes with annotations
     * @return this registry for chaining
     */
    public CommandRegistry registerClass(List<Class<?>> classes) {
        AnnotationCommandLoader.loadClass(this, classes);
        return this;
    }

    /**
     * Registers commands by scanning multiple classes.
     *
     * @param classes classes with annotations
     * @return this registry for chaining
     */
    public CommandRegistry registerClass(Class<?>... classes) {
        AnnotationCommandLoader.loadClass(this, classes);
        return this;
    }

    /**
     * Registers a pre-constructed command.
     *
     * @param command command to register
     * @return this registry for chaining
     */
    public CommandRegistry register(Command<?> command) {
        commands.add(command);
        return this;
    }

    /**
     * Returns a formatted list of all registered commands with their descriptions.
     *
     * @return help text
     */
    public String formattedCommandList() {
        StringBuilder sb = new StringBuilder("Available commands:\n");
        for (Command<?> command : commands) {
            String word = command.getCommandWord();
            String desc = command.getDescription();
            sb.append(String.format("  %-20s %s\n", word, desc.isEmpty() ? "" : desc));
        }
        return sb.toString();
    }

    /**
     * Returns a formatted list of commands that start with the given prefix.
     *
     * @param prefix optional command prefix filter
     * @return help text
     */
    public String formattedCommandList(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append("Available commands");
        if (prefix != null && !prefix.isEmpty()) {
            sb.append(" starting with '").append(prefix).append("'");
        }
        sb.append(":\n");

        boolean anyFound = false;

        for (Command<?> command : commands) {
            String word = command.getCommandWord();
            if (prefix == null || prefix.isEmpty() || word.startsWith(prefix)) {
                String desc = command.getDescription();
                sb.append(String.format("  %-20s %s\n", word, desc.isEmpty() ? "" : desc));
                anyFound = true;
            }
        }

        if (!anyFound) {
            sb.append("  (no matching commands)\n");
        }

        return sb.toString();
    }

    /**
     * @return set of registered commands
     */
    public Set<Command<?>> getCommands() {
        return commands;
    }


}
