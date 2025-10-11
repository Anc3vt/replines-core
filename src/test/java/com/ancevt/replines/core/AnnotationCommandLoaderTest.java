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

package com.ancevt.replines.core;

import com.ancevt.replines.core.argument.Arguments;
import com.ancevt.replines.core.repl.CommandRegistry;
import com.ancevt.replines.core.repl.ReplRunner;
import com.ancevt.replines.core.repl.UnknownCommandException;
import com.ancevt.replines.core.repl.annotation.ReplCommand;
import com.ancevt.replines.core.repl.annotation.ReplExecute;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AnnotationCommandLoaderTest {

    @ReplCommand(name = "hello", description = "Prints hello world")
    public static class HelloCommand {
        @ReplExecute
        public Object run(ReplRunner repl, Arguments args) {
            repl.println("Hello, World!");
            return null;
        }
    }

    public static class MixedCommand {

        @ReplCommand(name = "echo", description = "Echoes input")
        public Object echo(ReplRunner repl, Arguments args) {
            String text = args.hasNext() ? args.next() : "(empty)";
            repl.println("echo: " + text);
            return null;
        }

        @ReplCommand(name = "sum", description = "Sums two ints")
        public Object sum(ReplRunner repl, Arguments args) {
            int a = args.hasNext() ? Integer.parseInt(args.next()) : 0;
            int b = args.hasNext() ? Integer.parseInt(args.next()) : 0;
            repl.println("sum: " + (a + b));
            return a + b;
        }
    }

    private String outputToString(ByteArrayOutputStream out) {
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    public void testReplCommandWithReplExecuteMethod() throws Exception {
        CommandRegistry registry = new CommandRegistry();
        registry.registerClass(HelloCommand.class);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReplRunner repl = new ReplRunner(registry);
        repl.setOutputStream(out);

        repl.execute("hello");

        String output = outputToString(out);
        assertTrue(output.contains("Hello, World!"));
    }

    @Test
    public void testReplCommandMethodsOnClass() throws Exception {
        CommandRegistry registry = new CommandRegistry();
        registry.register(new MixedCommand());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReplRunner repl = new ReplRunner(registry);
        repl.setOutputStream(out);

        repl.execute("echo test123");
        repl.execute("sum 10 15");

        String output = outputToString(out);
        assertTrue(output.contains("echo: test123"));
        assertTrue(output.contains("sum: 25"));
    }

    @Test
    public void testHelpIncludesAnnotatedCommands() {
        CommandRegistry registry = new CommandRegistry();
        registry.registerClass(HelloCommand.class);
        registry.registerClass(MixedCommand.class);

        String help = registry.formattedCommandList();

        assertTrue(help.contains("hello"));
        assertTrue(help.contains("echo"));
        assertTrue(help.contains("sum"));
    }

    @Test
    public void testUnknownAnnotatedCommandFailsGracefully() {
        CommandRegistry registry = new CommandRegistry();
        ReplRunner repl = new ReplRunner(registry);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        repl.setOutputStream(out);

        try {
            repl.execute("doesnotexist");
            fail("Expected UnknownCommandException to be thrown");
        } catch (Exception e) {
            assertTrue(e instanceof UnknownCommandException);
            assertTrue(e.getMessage().contains("Unknown command"));
        }
    }

}
