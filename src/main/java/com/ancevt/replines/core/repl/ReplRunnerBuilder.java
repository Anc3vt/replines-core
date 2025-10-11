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

import com.ancevt.replines.filter.ColorizeFilter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Fluent builder for configuring and constructing instances of {@link ReplRunner}.
 * <p>
 * The {@code ReplRunnerBuilder} provides a modular and declarative way to assemble
 * a fully functional REPL environment. It allows specifying input/output streams,
 * command registry, asynchronous executor, colorization filters, default commands,
 * and custom error handling.
 * <p>
 * The builder follows an immutable, chainable design pattern — all configuration methods
 * return the builder itself, enabling fluent API syntax.
 *
 * <h2>Overview</h2>
 * A {@link ReplRunner} represents the main execution engine of the REPLines framework.
 * {@code ReplRunnerBuilder} acts as its configuration layer, ensuring consistent setup
 * for all components, including:
 * <ul>
 *   <li>Command registration via {@link CommandRegistry}</li>
 *   <li>Stream management (input/output)</li>
 *   <li>Asynchronous command execution via {@link java.util.concurrent.Executor}</li>
 *   <li>Output text filters (colorizers, formatters, sanitizers)</li>
 *   <li>Error interception through {@link ReplErrorHandler}</li>
 * </ul>
 *
 * <h2>Typical Usage</h2>
 * <pre>{@code
 * ReplRunner repl = ReplRunner.builder()
 *     .withInput(System.in)
 *     .withOutput(System.out)
 *     .withDefaultCommands()
 *     .withColorizer()
 *     .configure(reg -> {
 *         reg.command("hello")
 *            .description("Greets the user")
 *            .action((r, a) -> r.println("Hello, world!"))
 *            .build();
 *     })
 *     .withErrorHandler((repl, e) -> {
 *         repl.println("<red>Error:</red> " + e.getMessage());
 *     })
 *     .autoShutdownExecutor(true)
 *     .build();
 *
 * repl.start();
 * }</pre>
 *
 * <h2>Configuration Options</h2>
 * <dl>
 *   <dt>{@link #withInput(InputStream)}</dt>
 *   <dd>Specifies the input stream from which the REPL reads commands (default: {@link System#in}).</dd>
 *
 *   <dt>{@link #withOutput(OutputStream)}</dt>
 *   <dd>Specifies the output stream to which REPL prints command results (default: {@link System#out}).</dd>
 *
 *   <dt>{@link #withRegistry(CommandRegistry)}</dt>
 *   <dd>Allows providing a custom {@link CommandRegistry}. If omitted, a new empty registry is created.</dd>
 *
 *   <dt>{@link #withExecutor(Executor)}</dt>
 *   <dd>Defines the executor used for asynchronous commands .
 *   If not set, a cached daemon thread pool is created internally.</dd>
 *
 *   <dt>{@link #autoShutdownExecutor(boolean)}</dt>
 *   <dd>Determines whether the executor should automatically shut down when {@link ReplRunner#stop()} is called.</dd>
 *
 *   <dt>{@link #addFilter(Function)}</dt>
 *   <dd>Adds an output transformation filter applied to all printed text (e.g., color highlighting, markup parsing).</dd>
 *
 *   <dt>{@link #withColorizer()}</dt>
 *   <dd>Enables the built-in {@link com.ancevt.replines.filter.ColorizeFilter} for ANSI terminal colorization.</dd>
 *
 *   <dt>{@link #withDefaultCommands()}</dt>
 *   <dd>Registers built-in REPL commands:
 *       <ul>
 *         <li><b>help</b> — displays a list of registered commands</li>
 *         <li><b>exit</b> — terminates the REPL loop</li>
 *       </ul>
 *   </dd>
 *
 *   <dt>{@link #configure(Consumer)}</dt>
 *   <dd>Registers a callback that receives the {@link CommandRegistry} before the REPL is built.
 *       Useful for registering additional commands or modifying existing ones.</dd>
 *
 *   <dt>{@link #withErrorHandler(ReplErrorHandler)}</dt>
 *   <dd>Installs a global error handler that will be invoked on any exception thrown
 *       during command execution or REPL operation. See {@link ReplErrorHandler}.</dd>
 * </dl>
 *
 * <h2>Output Filters</h2>
 * Filters are applied in the same order as they were added.
 * <pre>{@code
 * ReplRunner repl = ReplRunner.builder()
 *     .addFilter(text -> "[OUT] " + text)
 *     .addFilter(text -> text.replace("error", "<red>error</red>"))
 *     .build();
 * }</pre>
 *
 * <h2>Asynchronous Execution</h2>
 * Asynchronous commands (marked with {@link Command.Builder#async()}) are executed using the
 * configured {@link Executor}. If no executor is provided, a daemon-thread pool is created internally.
 *
 * <pre>{@code
 * registry.command("compute")
 *     .description("Performs heavy computation asynchronously")
 *     .action((r, a) -> {
 *         Thread.sleep(2000);
 *         return "done";
 *     })
 *     .async()
 *     .build();
 * }</pre>
 *
 * <h2>Error Handling</h2>
 * Custom error handlers can fully control how REPL errors are displayed, logged, or suppressed:
 * <pre>{@code
 * .withErrorHandler((repl, e) -> {
 *     if (e instanceof UnknownCommandException uce) {
 *         repl.println("<yellow>Unknown command:</yellow> " + uce.getCommandWord());
 *     } else {
 *         repl.println("<red>Unhandled:</red> " + e.getMessage());
 *     }
 * })
 * }</pre>
 *
 * <h2>Lifecycle and Ownership</h2>
 * The builder manages resource ownership internally:
 * <ul>
 *   <li>If an executor is created by the builder, it will be marked as <i>owned</i> and can be auto-shutdown on {@link ReplRunner#stop()}.</li>
 *   <li>Provided streams are not closed automatically — ownership remains with the caller.</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * {@code ReplRunnerBuilder} is not thread-safe. It is intended to be configured in a single thread
 * before the REPL is started. The resulting {@link ReplRunner} is thread-safe regarding async execution.
 *
 * <h2>See Also</h2>
 * <ul>
 *   <li>{@link ReplRunner}</li>
 *   <li>{@link Command}</li>
 *   <li>{@link CommandRegistry}</li>
 *   <li>{@link ReplErrorHandler}</li>
 *   <li>{@link com.ancevt.replines.filter.ColorizeFilter}</li>
 * </ul>
 *
 */
public class ReplRunnerBuilder {

    private InputStream input;
    private OutputStream output;
    private CommandRegistry registry;
    private Executor executor;
    private boolean autoShutdownExecutor = false;
    private boolean executorOwnedInternally = false;
    private final List<Function<String, String>> filters = new ArrayList<>();
    private final List<Consumer<CommandRegistry>> registryActions = new ArrayList<>();
    private boolean useColorizer;
    private ReplErrorHandler errorHandler;
    private String commandFilterPrefix = "";


    /**
     * Sets a prefix that will be used to filter input lines recognized as REPL commands.
     * <p>
     * When this prefix is defined, the {@link com.ancevt.replines.core.repl.ReplRunner}
     * built by this builder will only execute lines that start with the given prefix.
     * All other lines will be ignored.
     * </p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * ReplRunner repl = ReplRunner.builder()
     *     .withCommandFilterPrefix("/")
     *     .withDefaultCommands()
     *     .build();
     *
     * // The following line will execute the "/help" command:
     * repl.execute("/help");
     *
     * // This line will be ignored because it does not start with "/":
     * repl.execute("hello");
     * }</pre>
     *
     * @param prefix the command prefix to match at the beginning of each input line;
     *               an empty string means all input lines are treated as commands
     * @return this builder instance for method chaining
     * @see com.ancevt.replines.core.repl.ReplRunner#setCommandFilterPrefix(String)
     */
    public ReplRunnerBuilder withCommandFilterPrefix(String prefix) {
        this.commandFilterPrefix = prefix;
        return this;
    }


    /**
     * Sets the input stream for the REPL.
     *
     * @param in input stream, e.g. {@link System#in}
     * @return this builder
     */
    public ReplRunnerBuilder withInput(InputStream in) {
        this.input = in;
        return this;
    }
    /**
     * Sets the output stream for the REPL.
     *
     * @param out output stream, e.g. {@link System#out}
     * @return this builder
     */
    public ReplRunnerBuilder withOutput(OutputStream out) {
        this.output = out;
        return this;
    }
    /**
     * Sets the command registry to use.
     * If not provided, a new empty {@link CommandRegistry} will be created.
     *
     * @param registry command registry instance
     * @return this builder
     */
    public ReplRunnerBuilder withRegistry(CommandRegistry registry) {
        this.registry = registry;
        return this;
    }
    /**
     * Sets the executor to be used for command execution.
     * <p>
     * If not set, a default cached thread pool will be used.
     * Note: If you provide your own executor, you are responsible
     * for shutting it down, unless {@link #autoShutdownExecutor(boolean)} is set to true.
     *
     * @param executor the executor to use
     * @return this builder
     */
    public ReplRunnerBuilder withExecutor(Executor executor) {
        this.executor = executor;
        this.executorOwnedInternally = false;
        return this;
    }

    /**
     * Indicates whether the executor should be automatically shut down when
     * {@link ReplRunner#stop()} is called.
     * <p>
     * This is useful when the executor is dedicated to this REPL instance.
     * Use with caution if you're sharing the executor across components.
     *
     * @param value true to auto-shutdown the executor; false otherwise
     * @return this builder
     */
    public ReplRunnerBuilder autoShutdownExecutor(boolean value) {
        this.autoShutdownExecutor = value;
        return this;
    }
    /**
     * Adds an output filter that will be applied to all printed text.
     *
     * @param filter transformation function (e.g. colorizer)
     * @return this builder
     */
    public ReplRunnerBuilder addFilter(Function<String, String> filter) {
        filters.add(filter);
        return this;
    }
    /**
     * Enables the built-in colorizer filter.
     * This replaces tags like {@code <red>} with ANSI escape codes.
     *
     * @return this builder
     */
    public ReplRunnerBuilder withColorizer() {
        this.useColorizer = true;
        return this;
    }
    /**
     * Registers default commands such as {@code help} and {@code exit}.
     *
     * @return this builder
     */
    public ReplRunnerBuilder withDefaultCommands() {
        registryActions.add(ReplRunnerBuilder::registerDefaultCommands);
        return this;
    }
    /**
     * Adds a configuration action that receives the {@link CommandRegistry}.
     * Useful for registering custom commands.
     *
     * @param consumer registry configuration callback
     * @return this builder
     */
    public ReplRunnerBuilder configure(Consumer<CommandRegistry> consumer) {
        registryActions.add(consumer);
        return this;
    }

    /**
     * Builds and returns a new {@link ReplRunner} with the configured options.
     *
     * @return configured ReplRunner
     */
    public ReplRunner build() {
        if (executor == null) {
            executor = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });
            executorOwnedInternally = true;
        }

        ReplRunner repl = new ReplRunner();
        repl.setShutdownExecutorOnStop(autoShutdownExecutor || executorOwnedInternally);
        repl.setExecutor(executor);

        // registry
        CommandRegistry finalRegistry = (registry != null ? registry : new CommandRegistry());
        registryActions.forEach(action -> action.accept(finalRegistry));
        repl.setRegistry(finalRegistry);

        // streams
        if (input != null) repl.setInputStream(input);
        if (output != null) repl.setOutputStream(output);

        // executor
        if (executor != null) repl.setExecutor(executor);

        // filters
        filters.forEach(repl::addOutputFilter);
        if (useColorizer) repl.addOutputFilter(new ColorizeFilter()::colorize);

        if (errorHandler != null) {
            repl.setErrorHandler(errorHandler);
        }

        repl.setCommandFilterPrefix(commandFilterPrefix);

        return repl;
    }

    /**
     * Sets a custom global error handler.
     *
     * @param handler handler for all REPL errors
     * @return this builder
     */
    public ReplRunnerBuilder withErrorHandler(ReplErrorHandler handler) {
        this.errorHandler = handler;
        return this;
    }

    private static void registerDefaultCommands(CommandRegistry registry) {
        registry.command("/help")
                .description("Shows help info")
                .action((r, a) -> {
                    r.println("<g>" + r.getRegistry().formattedCommandList());
                })
                .register();

        registry.command("/exit")
                .description("Exit the REPL")
                .action((r, a) -> {
                    r.stop();
                })
                .register();
    }
}
