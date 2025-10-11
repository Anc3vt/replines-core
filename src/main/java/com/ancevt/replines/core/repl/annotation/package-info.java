/**
 * Provides annotations for defining REPL (Read–Eval–Print Loop) commands
 * and their execution methods in the Replines Core framework.
 *
 * <p>The annotations in this package are designed to simplify the process
 * of declaring commands that can be automatically registered and executed
 * within a {@link com.ancevt.replines.core.repl.ReplRunner} or
 * {@link com.ancevt.replines.core.repl.CommandRegistry}.
 * Instead of manually creating {@link com.ancevt.replines.core.repl.Command}
 * objects, you can annotate classes and methods to describe commands declaratively.</p>
 *
 * <p>There are two main annotations:</p>
 * <ul>
 *   <li>{@link com.ancevt.replines.core.repl.annotation.ReplCommand}
 *       — Marks a class or a method as a REPL command.</li>
 *   <li>{@link com.ancevt.replines.core.repl.annotation.ReplExecute}
 *       — Marks a method within a class annotated with {@code @ReplCommand}
 *         as the entry point for command execution.</li>
 * </ul>
 *
 * <p>The {@link com.ancevt.replines.core.repl.AnnotationCommandLoader}
 * is responsible for scanning these annotations and registering corresponding
 * commands in a {@link com.ancevt.replines.core.repl.CommandRegistry}.</p>
 *
 * <p>### Class-Level Usage Example</p>
 *
 * <pre>{@code
 * @ReplCommand(name = "greet", description = "Greets the specified user")
 * public class GreetCommand {
 *
 *     @ReplExecute
 *     public void execute(ReplRunner repl, Arguments args) {
 *         String name = args.hasNext() ? args.next() : "Guest";
 *         repl.println("Hello, " + name + "!");
 *     }
 * }
 *
 * // Registration:
 * CommandRegistry registry = new CommandRegistry();
 * registry.registerClass(GreetCommand.class);
 *
 * ReplRunner repl = new ReplRunner(registry);
 * repl.execute("greet Alice");
 * // Output: Hello, Alice!
 * }</pre>
 *
 * <p>In this example, the {@code GreetCommand} class defines a REPL command
 * named {@code greet}. The {@link ReplExecute} method serves as the command's
 * entry point, accepting a {@link com.ancevt.replines.core.repl.ReplRunner}
 * and parsed {@link com.ancevt.replines.core.argument.Arguments}.</p>
 *
 * <p>### Method-Level Usage Example</p>
 *
 * <pre>{@code
 * public class UtilityCommands {
 *
 *     @ReplCommand(name = "echo", description = "Prints input text back to the console")
 *     public Object echo(ReplRunner repl, Arguments args) {
 *         String message = args.hasNext() ? args.next() : "(empty)";
 *         repl.println("echo: " + message);
 *         return null;
 *     }
 *
 *     @ReplCommand(name = "sum", description = "Adds two integers")
 *     public Object sum(ReplRunner repl, Arguments args) {
 *         int a = args.hasNext() ? Integer.parseInt(args.next()) : 0;
 *         int b = args.hasNext() ? Integer.parseInt(args.next()) : 0;
 *         repl.println("Result: " + (a + b));
 *         return a + b;
 *     }
 * }
 *
 * // Registration:
 * CommandRegistry registry = new CommandRegistry();
 * registry.register(new UtilityCommands());
 *
 * ReplRunner repl = new ReplRunner(registry);
 * repl.execute("sum 10 20");
 * // Output: Result: 30
 * }</pre>
 *
 * <p>This variant uses {@link ReplCommand} directly on methods,
 * allowing you to bundle multiple commands into a single class.</p>
 *
 * <p>### Asynchronous Execution</p>
 *
 * <p>The {@link com.ancevt.replines.core.repl.annotation.ReplCommand#async()}
 * flag allows a command to run asynchronously, enabling non-blocking execution
 * in the REPL environment.</p>
 *
 * <pre>{@code
 * @ReplCommand(name = "compute", description = "Performs a long computation", async = true)
 * public class ComputeCommand {
 *
 *     @ReplExecute
 *     public void execute(ReplRunner repl, Arguments args) {
 *         try {
 *             Thread.sleep(2000);
 *             repl.println("Computation complete!");
 *         } catch (InterruptedException e) {
 *             repl.println("Interrupted!");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>When {@code async = true}, the command executes in a background thread,
 * allowing the REPL to remain responsive while the computation runs.</p>
 *
 * <p>### Summary</p>
 * <ul>
 *   <li>Use {@link ReplCommand} at the class level with {@link ReplExecute} for simple commands.</li>
 *   <li>Use {@link ReplCommand} on methods for multiple commands in one class.</li>
 *   <li>Set {@code async = true} to make commands execute concurrently.</li>
 * </ul>
 *
 * <p>These annotations provide a clean, declarative way to integrate REPL commands
 * without writing manual registration code.</p>
 *
 * @see com.ancevt.replines.core.repl.ReplRunner
 * @see com.ancevt.replines.core.repl.CommandRegistry
 * @see com.ancevt.replines.core.repl.AnnotationCommandLoader
 * @see com.ancevt.replines.core.repl.annotation.ReplCommand
 * @see com.ancevt.replines.core.repl.annotation.ReplExecute
 */
package com.ancevt.replines.core.repl.annotation;
