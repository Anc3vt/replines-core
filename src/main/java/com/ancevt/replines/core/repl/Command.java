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


import com.ancevt.replines.core.argument.Arguments;
import com.ancevt.replines.core.argument.reflection.ArgumentBinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
/**
 * Represents a single REPL command.
 *
 * A command has:
 * <ul>
 *     <li>one or more keywords (command words)</li>
 *     <li>a description</li>
 *     <li>an action to execute</li>
 * </ul>
 *
 * Example (using the builder API):
 * <pre>
 *     Command&lt;Void&gt; ping = Command.&lt;Void&gt;builder("ping")
 *         .description("Simple ping command")
 *         .action((repl, args) -&gt; repl.println("pong"))
 *         .build();
 *
 *     registry.register(ping);
 * </pre>
 *
 * @param <R> type of the result returned by the command
 */
public class Command<R> {

    private final List<String> commandWords;
    private final String description;

    private final BiFunction<ReplRunner, Arguments, R> action;

    private BiConsumer<ReplRunner, R> resultAction;

    private boolean isAsync;
    /**
     * Creates a new command with the given words, description and action.
     *
     * @param commandWords list of words (aliases) that trigger the command
     * @param description  human-readable description
     * @param action       function to execute when the command is invoked
     */
    public Command(List<String> commandWords, String description, BiFunction<ReplRunner, Arguments, R> action) {
        if (commandWords.isEmpty()) {
            throw new IllegalArgumentException("commandWords must not be empty");
        } else {
            this.commandWords = commandWords;
            this.description = description;
            this.action = action;
        }
    }
    /**
     * Creates a new command with the given words and action.
     *
     * @param commandWords list of words (aliases) that trigger the command
     * @param action       function to execute when the command is invoked
     */
    public Command(List<String> commandWords, BiFunction<ReplRunner, Arguments, R> action) {
        if (commandWords.isEmpty()) {
            throw new IllegalArgumentException("commandWords must not be empty");
        } else {
            this.commandWords = commandWords;
            this.description = "";
            this.action = action;
        }
    }
    /**
     * Creates a new command with a single word and action.
     *
     * @param commandWord single keyword
     * @param action      function to execute when the command is invoked
     */
    public Command(String commandWord, BiFunction<ReplRunner, Arguments, R> action) {
        this.commandWords = new ArrayList<>();
        this.description = "";
        this.action = action;

        commandWords.add(commandWord);
    }
    /**
     * Creates a new command with a single word, description and action.
     *
     * @param commandWord single keyword
     * @param description human-readable description
     * @param action      function to execute when the command is invoked
     */
    public Command(String commandWord, String description, BiFunction<ReplRunner, Arguments, R> action) {
        this.commandWords = new ArrayList<>();
        this.description = description;
        this.action = action;

        commandWords.add(commandWord);
    }
    /**
     * Creates a new command with words, description, action and result handler.
     *
     * @param commandWords list of keywords
     * @param description  description text
     * @param action       main execution function
     * @param resultAction handler for processing the result
     */
    public Command(List<String> commandWords, String description, BiFunction<ReplRunner, Arguments, R> action, BiConsumer<ReplRunner, R> resultAction) {
        this.resultAction = resultAction;
        if (commandWords.isEmpty()) {
            throw new IllegalArgumentException("commandWords must not be empty");
        } else {
            this.commandWords = commandWords;
            this.description = description;
            this.action = action;
        }
    }
    /**
     * Creates a new command with words, action and result handler.
     *
     * @param commandWords list of keywords
     * @param action       main execution function
     * @param resultAction handler for processing the result
     */
    public Command(List<String> commandWords, BiFunction<ReplRunner, Arguments, R> action, BiConsumer<ReplRunner, R> resultAction) {
        this.resultAction = resultAction;
        if (commandWords.isEmpty()) {
            throw new IllegalArgumentException("commandWords must not be empty");
        } else {
            this.commandWords = commandWords;
            this.description = "";
            this.action = action;
        }
    }
    /**
     * Creates a new command with a single word, action and result handler.
     *
     * @param commandWord  single keyword
     * @param action       main execution function
     * @param resultAction handler for processing the result
     */
    public Command(String commandWord, BiFunction<ReplRunner, Arguments, R> action, BiConsumer<ReplRunner, R> resultAction) {
        this.resultAction = resultAction;
        this.commandWords = new ArrayList<>();
        this.description = "";
        this.action = action;

        commandWords.add(commandWord);
    }
    /**
     * Creates a new command with word, description, action and result handler.
     *
     * @param commandWord  single keyword
     * @param description  description text
     * @param action       main execution function
     * @param resultAction handler for processing the result
     */
    public Command(String commandWord, String description, BiFunction<ReplRunner, Arguments, R> action, BiConsumer<ReplRunner, R> resultAction) {
        this.resultAction = resultAction;
        this.commandWords = new ArrayList<>();
        this.description = description;
        this.action = action;

        commandWords.add(commandWord);
    }
    /**
     * @return main function that executes the command
     */
    public BiFunction<ReplRunner, Arguments, R> getAction() {
        return action;
    }
    /**
     * Sets a handler for processing the result after the command is executed.
     *
     * @param resultAction consumer for result values
     */
    public void setResultAction(BiConsumer<ReplRunner, R> resultAction) {
        this.resultAction = resultAction;
    }
    /**
     * @return handler for processing results, or null if not set
     */
    public BiConsumer<ReplRunner, R> getResultAction() {
        return resultAction;
    }
    /**
     * Executes the command synchronously.
     *
     * @param replRunner  runner context
     * @param commandLine full input line
     */
    public void execute(ReplRunner replRunner, String commandLine) {
        Arguments arguments = Arguments.parse(commandLine);
        arguments.skip();
        R result = action.apply(replRunner, arguments);
        if (resultAction != null) resultAction.accept(replRunner, result);
    }
    /**
     * Executes the command asynchronously using {@link ReplRunner#getExecutor()}.
     * Falls back to the common ForkJoinPool if no executor is configured.
     *
     * @param replRunner  runner context
     * @param commandLine full input line
     */
    public void executeAsync(ReplRunner replRunner, String commandLine) {
        Runnable task = () -> {
            try {
                Arguments arguments = Arguments.parse(commandLine);
                arguments.skip();
                R result = action.apply(replRunner, arguments);

                if (result != null) {
                    if (resultAction != null) {
                        resultAction.accept(replRunner, result);
                    } else {
                        replRunner.println(String.valueOf(result));
                    }
                }
            } catch (Throwable e) {
                replRunner.getErrorHandler().handle(replRunner, e);
            }
        };

        Executor executor = replRunner.getExecutor();
        if (executor != null) {
            CompletableFuture.runAsync(task, executor);
        } else {
            CompletableFuture.runAsync(task);
        }
    }


    /**
     * @return description text
     */
    public String getDescription() {
        return description;
    }
    /**
     * @return the primary command word
     */
    public String getCommandWord() {
        return commandWords.get(0);
    }
    /**
     * @return all command words (aliases)
     */
    public List<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public String toString() {
        return "Command{" +
                "commandWords='" + commandWords + '\'' +
                ", description='" + description + '\'' +
                ", action=" + action +
                '}';
    }
    /**
     * @return true if the command should be executed asynchronously
     */
    public boolean isAsync() {
        return isAsync;
    }
    /**
     * Sets whether the command should be executed asynchronously.
     *
     * @param async true for async
     */
    public void setAsync(boolean async) {
        isAsync = async;
    }
    /**
     * Creates a new command builder for a single word.
     */
    public static <R> Builder<R> builder(String commandWord) {
        return new Builder<>(commandWord);
    }
    /**
     * Creates a new command builder for a list of words.
     */
    public static <R> Builder<R> builder(List<String> commandWords) {
        return new Builder<>(commandWords);
    }
    /**
     * Creates a new command builder for one or more words.
     */
    public static <R> Builder<R> builder(String... commandWords) {
        if (commandWords.length == 1) {
            return new Builder<>(commandWords[0]);
        } else {
            return new Builder<>(Arrays.asList(commandWords));
        }
    }

    /**
     * Fluent builder for constructing {@link Command} instances.
     *
     * <p>Example usage:</p>
     * <pre>
     * Command&lt;Void&gt; hello = Command.&lt;Void&gt;builder("hello")
     *     .description("Prints greeting")
     *     .action((repl, args) -&gt; repl.println("Hello world!"))
     *     .async()
     *     .build();
     *
     * registry.register(hello);
     * </pre>
     *
     * @param <R> type of result returned by the command
     */
    public static class Builder<R> {
        private final List<String> commandWords = new ArrayList<>();
        private String description = "";
        private BiFunction<ReplRunner, Arguments, R> action;
        private BiConsumer<ReplRunner, R> resultAction;
        private boolean async = false;

        /**
         * Creates a builder for a command with one or more keywords.
         *
         * @param words command keywords (aliases)
         */
        public Builder(String... words) {
            this.commandWords.addAll(Arrays.asList(words));
        }

        /**
         * Creates a builder for a command with a list of keywords.
         *
         * @param words list of command keywords (aliases)
         */
        public Builder(List<String> words) {
            this.commandWords.addAll(words);
        }

        /**
         * Sets a human-readable description for the command.
         *
         * @param description description text
         * @return this builder
         */
        public Builder<R> description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Defines the main action to execute when the command is invoked,
         * automatically binding command-line arguments into a typed object
         * using {@link ArgumentBinder}.
         *
         * <p>Use this variant when the action produces a result.</p>
         *
         * <pre>{@code
         * class MyArgs {
         *     @CommandArgument
         *     String name;
         *
         *     @OptionArgument(names = {"-n", "--number"})
         *     int number;
         * }
         *
         * registry.command("greet")
         *     .action(MyArgs.class, (repl, parsed) -> {
         *         repl.println("Hello " + parsed.name + ", number: " + parsed.number);
         *         return parsed.number;
         *     })
         *     .result((repl, result) -> repl.println("Returned: " + result))
         *     .build();
         * }</pre>
         *
         * @param argClass type annotated with {@link com.ancevt.replines.core.argument.reflection.CommandArgument}
         *                 and/or {@link com.ancevt.replines.core.argument.reflection.OptionArgument}
         * @param function function taking {@link ReplRunner} and a parsed {@code T}, returning {@code R}
         * @param <T>      argument model type
         * @return this builder
         */
        public <T> Builder<R> action(Class<T> argClass, BiFunction<ReplRunner, T, R> function) {
            this.action = (repl, args) -> {
                try {
                    T parsed = ArgumentBinder.convert(args, argClass);
                    return function.apply(repl, parsed);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to bind args", e);
                }
            };
            return this;
        }

        /**
         * Defines the main action to execute when the command is invoked,
         * automatically binding command-line arguments into a typed object
         * using {@link ArgumentBinder}.
         *
         * <p>Use this variant when the action has no return value
         * (equivalent to returning {@code null}).</p>
         *
         * <pre>{@code
         * class EchoArgs {
         *     @CommandArgument
         *     String text;
         * }
         *
         * registry.command("echo")
         *     .action(EchoArgs.class, (repl, parsed) -> {
         *         repl.println("Echo: " + parsed.text);
         *     })
         *     .build();
         * }</pre>
         *
         * @param argClass type annotated with {@link com.ancevt.replines.core.argument.reflection.CommandArgument}
         *                 and/or {@link com.ancevt.replines.core.argument.reflection.OptionArgument}
         * @param consumer consumer taking {@link ReplRunner} and a parsed {@code T}
         * @param <T>      argument model type
         * @return this builder (with result type {@code Void})
         */
        @SuppressWarnings("unchecked")
        public <T> Builder<Void> action(Class<T> argClass, BiConsumer<ReplRunner, T> consumer) {
            this.action = (repl, args) -> {
                try {
                    T parsed = ArgumentBinder.convert(args, argClass);
                    consumer.accept(repl, parsed);
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to bind args", e);
                }
            };
            return (Builder<Void>) this;
        }


        /**
         * Defines the main action to execute when the command is invoked.
         *
         * @param action function taking {@link ReplRunner} and {@link Arguments},
         *               returning a result of type {@code T}
         * @return this builder
         */
        public Builder<R> action(BiFunction<ReplRunner, Arguments, R> action) {
            this.action = action;
            return this;
        }

        /**
         * Defines the main action as a consumer (no return value).
         * Equivalent to using {@code action(...)} with {@code null} return.
         *
         * @param consumer consumer taking {@link ReplRunner} and {@link Arguments}
         * @return this builder
         */
        public Builder<R> action(BiConsumer<ReplRunner, Arguments> consumer) {
            this.action = (r, a) -> {
                consumer.accept(r, a);
                return null;
            };
            return this;
        }

        /**
         * Sets a handler for processing the result after command execution.
         *
         * @param resultAction consumer taking {@link ReplRunner} and the result value
         * @return this builder
         */
        public Builder<R> result(BiConsumer<ReplRunner, R> resultAction) {
            this.resultAction = resultAction;
            return this;
        }

        /**
         * Marks the command as asynchronous.
         * It will be executed via {@link java.util.concurrent.CompletableFuture}
         * using the {@link ReplRunner#getExecutor()} if provided.
         *
         * @return this builder
         */
        public Builder<R> async() {
            this.async = true;
            return this;
        }

        /**
         * Builds the command instance with the current configuration.
         *
         * @return new {@link Command} instance
         */
        public Command<R> register() {
            Command<R> command = new Command<>(commandWords, description, action, resultAction);
            command.setAsync(async);
            return command;
        }

        /**
         * Builds the command and immediately registers it
         * in the given {@link CommandRegistry}.
         *
         * @param registry registry to register the command in
         */
        public void register(CommandRegistry registry) {
            registry.register(register());
        }
    }



}
