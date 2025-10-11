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
import com.ancevt.replines.core.repl.annotation.ReplCommand;
import com.ancevt.replines.core.repl.annotation.ReplExecute;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * Central execution engine of the REPLines framework — the core Read-Eval-Print Loop runner.
 * <p>
 * The {@code ReplRunner} is responsible for:
 * <ul>
 *   <li>reading command lines from an {@link InputStream};</li>
 *   <li>parsing and executing commands via a {@link CommandRegistry};</li>
 *   <li>handling output and applying filters (e.g. colorization);</li>
 *   <li>managing asynchronous command execution through an {@link Executor};</li>
 *   <li>providing lifecycle control (start, stop, isRunning, etc.).</li>
 * </ul>
 *
 * <h2>Overview</h2>
 * A {@code ReplRunner} implements a standard REPL loop — reading text lines, parsing the first token as a command word,
 * and executing the corresponding {@link Command}. It integrates closely with {@link CommandRegistry} and supports
 * both synchronous and asynchronous command invocation.
 *
 * <h2>Typical Usage</h2>
 * <pre>{@code
 * ReplRunner repl = ReplRunner.builder()
 *     .withDefaultCommands()
 *     .withColorizer()
 *     .configure(reg -> {
 *         reg.command("echo")
 *            .description("Prints all arguments back")
 *            .action((r, a) -> r.println(String.join(" ", a.getElements())))
 *            .build();
 *     })
 *     .build();
 *
 * repl.start(System.in, System.out);
 * }</pre>
 *
 * <h2>Command Execution Model</h2>
 * When the user types a line:
 * <pre>
 *     compute 1 2 3
 * </pre>
 * <ul>
 *   <li>The first token (<b>compute</b>) is treated as the command word.</li>
 *   <li>All subsequent tokens are passed to the command as {@link com.ancevt.replines.core.argument.Arguments}.</li>
 *   <li>If the command is marked as asynchronous via {@link Command#setAsync(boolean)}, it will be executed using
 *       {@link #getExecutor()}.</li>
 * </ul>
 *
 * <h2>Input and Output Streams</h2>
 * The REPL reads user input from an {@link InputStream} (line by line) and writes all output to an {@link OutputStream}.
 * The developer can replace {@link System#in} and {@link System#out} with custom streams, e.g. for Swing or network integration.
 *
 * <h2>Output Filters</h2>
 * Output filters allow dynamic transformation of printed text — for example, applying ANSI color codes or formatting.
 * Filters are applied in the order they were added via {@link #addOutputFilter(Function)}.
 *
 * <pre>{@code
 * ReplRunner repl = new ReplRunner();
 * repl.addOutputFilter(text -> text.replace("ERROR", "<red>ERROR<>"));
 * }</pre>
 *
 * <h2>Asynchronous Execution</h2>
 * The REPL supports async command processing using an {@link Executor}.
 * If none is provided explicitly, a default {@link java.util.concurrent.Executors#newCachedThreadPool()} is used.
 * The executor can be automatically shut down on {@link #stop()} if created internally.
 *
 * <pre>{@code
 * repl.getRegistry().command("compute")
 *     .description("Simulates a background computation")
 *     .action((r, a) -> {
 *         Thread.sleep(1000);
 *         return 42;
 *     })
 *     .result((r, result) -> r.println("Answer: " + result))
 *     .async()
 *     .build();
 * }</pre>
 *
 * <h2>Lifecycle</h2>
 * <ul>
 *   <li>{@link #start()} — starts the REPL loop using {@link System#in}/{@link System#out}.</li>
 *   <li>{@link #start(InputStream, OutputStream)} — starts with custom I/O.</li>
 *   <li>{@link #stop()} — gracefully stops the REPL and optionally shuts down its executor.</li>
 *   <li>{@link #isRunning()} — returns whether the REPL loop is active.</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * Unknown or malformed commands result in an {@link UnknownCommandException}.
 * The error message is printed directly into the output stream.
 *
 * <pre>{@code
 * UnknownCommandException e -> "Unknown command: xyz"
 * }</pre>
 *
 * <h2>Extensibility</h2>
 * {@code ReplRunner} can be extended or customized:
 * <ul>
 *   <li>by subclassing and overriding {@link #execute(String)} to modify parsing logic;</li>
 *   <li>by registering additional output filters;</li>
 *   <li>by replacing {@link CommandRegistry} via {@link #setRegistry(CommandRegistry)};</li>
 *   <li>or by composing through {@link ReplRunnerBuilder}.</li>
 * </ul>
 *
 * <h2>Builder Integration</h2>
 * For fluent configuration, use {@link ReplRunnerBuilder}:
 * <pre>{@code
 * ReplRunner repl = ReplRunner.builder()
 *     .withRegistry(new CommandRegistry())
 *     .withColorizer()
 *     .withDefaultCommands()
 *     .autoShutdownExecutor(true)
 *     .build();
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * The REPL loop runs in a single thread, but asynchronous commands may run concurrently
 * using a shared or external {@link Executor}.
 * Adding or removing output filters should be done before calling {@link #start()}.
 *
 * <h2>See Also</h2>
 * <ul>
 *   <li>{@link Command}</li>
 *   <li>{@link CommandRegistry}</li>
 *   <li>{@link ReplRunnerBuilder}</li>
 *   <li>{@link com.ancevt.replines.filter.ColorizeFilter}</li>
 * </ul>
 *
 */

public class ReplRunner {

    private CommandRegistry registry;
    private boolean running;

    private InputStream inputStream;

    private OutputStream outputStream;

    private Executor executor;
    private boolean shutdownExecutorOnStop = false;

    private String commandFilterPrefix = "";

    private final List<Function<String, String>> outputFilters = new ArrayList<>();

    private ReplErrorHandler errorHandler = (r, e) -> {
        String msg = e.getMessage() != null ? e.getMessage() : e.toString();
        r.println("Error: " + msg);
    };

    public ReplRunner() {
        this.registry = new CommandRegistry();
    }

    public ReplRunner(CommandRegistry registry) {
        this.registry = registry;
    }

    /**
     * Sets the command filter prefix for the REPL.
     * <p>
     * When a prefix is defined, only commands that start with this prefix
     * will be recognized and executed by the {@link com.ancevt.replines.core.repl.ReplRunner}.
     * This can be useful when integrating the REPL into a larger system
     * where user input may include lines that are not intended as commands.
     * </p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * repl.setCommandFilterPrefix("/");
     * // Only lines starting with "/" will be treated as commands:
     * // "/help" -> executes "help"
     * // "hello world" -> ignored
     * }</pre>
     *
     * @param commandFilterPrefix the prefix string used to identify REPL commands;
     *                            an empty string means that all input lines are treated as commands
     */
    public void setCommandFilterPrefix(String commandFilterPrefix) {
        this.commandFilterPrefix = commandFilterPrefix;
    }

    /**
     * Returns the currently configured command filter prefix.
     *
     * @return the command filter prefix string, or an empty string
     *         if all input lines are treated as commands
     * @see #setCommandFilterPrefix(String)
     */
    public String getCommandFilterPrefix() {
        return commandFilterPrefix;
    }

    /**
     * Sets a custom error handler invoked when a command fails or an unknown command is entered.
     *
     * @param handler the error handler
     */
    public void setErrorHandler(ReplErrorHandler handler) {
        this.errorHandler = handler;
    }

    /**
     * @return currently configured error handler
     */
    public ReplErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * @return input stream used by this REPL
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Sets the input stream for reading user commands.
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * @return output stream used by this REPL
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Sets the output stream for printing results and errors.
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * @return current command registry
     */
    public CommandRegistry getRegistry() {
        return registry;
    }

    /**
     * Sets a new command registry.
     */
    public void setRegistry(CommandRegistry commandRegistry) {
        this.registry = commandRegistry;
    }

    /**
     * @return true if REPL loop is running
     */
    public boolean isRunning() {
        return running;
    }
    /**
     * Adds a filter that transforms REPL output before it is written.
     *
     * @param filter transformation function, e.g. for colorization
     */
    public void addOutputFilter(Function<String, String> filter) {
        if (filter != null) outputFilters.add(filter);
    }
    /**
     * Removes a previously added output filter.
     *
     * @return true if the filter was present and removed
     */
    public boolean removeOutputFilter(Function<String, String> filter) {
        return outputFilters.remove(filter);
    }
    /** @return an unmodifiable list of active output filters */
    public List<Function<String, String>> getOutputFilters() {
        return Collections.unmodifiableList(outputFilters);
    }
    /** Removes all output filters. */
    public void clearOutputFilters() {
        outputFilters.clear();
    }

    private String applyFilters(String text) {
        String result = text;
        for (Function<String, String> f : outputFilters) {
            result = f.apply(result);
        }
        return result;
    }
    /**
     * Prints text to the output stream without appending a newline.
     * Output filters are applied before writing.
     */
    public void print(Object s) {
        try {
            String text = String.valueOf(s);
            text = applyFilters(text);
            outputStream.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Prints text to the output stream followed by a newline.
     * Output filters are applied before writing.
     */
    public void println(Object s) {
        print(s + "\n");
    }
    /**
     * Executes a single command line string.
     *
     * @param commandLine full input line
     * @throws UnknownCommandException if no matching command is found
     */
    public void execute(String commandLine) throws UnknownCommandException {
        String[] tokens = commandLine.trim().split("\\s+");
        if (commandLine.isEmpty() || tokens.length == 0) return;

        String commandWord = tokens[0];

        for (Command<?> command : registry.getCommands()) {
            if (commandWord.equals(command.getCommandWord()) ||
                    command.getCommandWords().stream().anyMatch(s -> s.equals(commandWord))) {

                try {
                    if (command.isAsync()) {
                        command.executeAsync(this, commandLine);
                    } else {
                        command.execute(this, commandLine);
                    }
                } catch (Exception e) {
                    errorHandler.handle(this, e);
                }
                return;
            }
        }

        throw new UnknownCommandException(format("Unknown command: %s", commandWord), commandWord, commandLine, registry);
    }
    /**
     * Starts the REPL loop using the given input and output streams.
     * Blocks until stopped or input ends.
     */
    public void start(InputStream inputStream, OutputStream outputStream) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.running = true;

        ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();

        int b;
        while (running && (b = inputStream.read()) != -1) {
            if (b == '\n') {
                byte[] bytes = lineBuffer.toByteArray();
                String line = new String(bytes, StandardCharsets.UTF_8);
                lineBuffer.reset();

                try {
                    if (line.startsWith(commandFilterPrefix)) {
                        execute(line);
                    }
                } catch (Exception e) {
                    errorHandler.handle(this, e);
                }
            } else if (b != '\r') {
                lineBuffer.write(b);
            }
        }
    }
    /**
     * Starts the REPL loop with a given input stream
     * and {@link System#out} as output.
     */
    public void start(InputStream inputStream) throws IOException {
        start(inputStream, System.out);
    }
    /** Starts the REPL loop using {@link System#in} and {@link System#out}. */
    public void start() {
        try {
            start(System.in, System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /** Stops the REPL loop. */
    public void stop() {
        running = false;

        if (shutdownExecutorOnStop && executor instanceof ExecutorService) {
            ((ExecutorService) executor).shutdownNow();
        }
    }
    /** @return executor used for async command execution */
    public Executor getExecutor() {
        return executor;
    }
    /** Sets the executor for async command execution. */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
    /**
     * Creates a builder for configuring and constructing {@link ReplRunner}.
     */
    public static ReplRunnerBuilder builder() {
        return new ReplRunnerBuilder();
    }

    void setShutdownExecutorOnStop(boolean value) {
        this.shutdownExecutorOnStop = value;
    }

    // Some dev sandbox
    public static void main(String[] args) throws IOException {
        ReplRunner repl = ReplRunner.builder()
                .withColorizer()
                .withDefaultCommands()
                .withCommandFilterPrefix("")
                .withErrorHandler((r, e) -> {
                    r.println("<red>Error: " + e.getMessage());
                    e.printStackTrace(new PrintStream(r.getOutputStream()));
                })
                .configure(reg -> {
                    reg.command("/test")
                            .description("Prints each argument with index")
                            .action((r, a) -> {
                                r.println("tested");
                                for (int i = 0; i < a.size(); i++) {
                                    r.println(i + "\t" + a.getElements()[i]);
                                }
                            })
                            .register();

                    reg.command("/testerr")
                            .action((r, a) -> {
                                int i = 2 / 0;
                            })
                            .register();
                    reg.registerClass(MyCommand.class);
                })

                .build();


        repl.getRegistry().command("/compute")
                .action((r, a) -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return 42;
                })
                .result((r, result) -> r.println("Answer: " + result))
                .async()
                .register();


        repl.start(System.in, System.out);

        System.out.println("END");
    }

    @ReplCommand(name = "/sum", description = "Adds two integers")
    private static class MyCommand {

        @ReplExecute()
        public void execute(ReplRunner repl, Arguments args) {
            repl.println(args.next(int.class) + args.next(int.class));
        }

        @ReplCommand(name = "/command2")
        private static void myCommand2(ReplRunner repl, Arguments args) {
            repl.println("Executed " + args.getSource());
        }

    }

}
