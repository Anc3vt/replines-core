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
import com.ancevt.replines.core.argument.reflection.CommandArgument;
import com.ancevt.replines.core.argument.reflection.OptionArgument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderBindActionTest {

    static class EchoArgs {
        @CommandArgument
        String text;

        @OptionArgument(names = {"-n", "--num"})
        int number;
    }

    @Test
    void testActionWithFunctionReturnsValue() {
        Command<String> cmd = Command.<String>builder("echo")
                .action(EchoArgs.class, (repl, parsed) -> {
                    assertEquals("hello", parsed.text);
                    assertEquals(42, parsed.number);
                    return "OK-" + parsed.text;
                })
                .register();

        ReplRunner dummy = new ReplRunner() {
            public void println(String s) { /* no-op */ }
        };

        Arguments args = Arguments.parse("hello -n 42");
        args.skip();
        String result = cmd.getAction().apply(dummy, args);

        assertEquals("OK-hello", result);
    }

    @Test
    void testActionWithConsumerExecutesSideEffect() {
        StringBuilder output = new StringBuilder();

        Command<Void> cmd = Command.<Void>builder("echo")
                .action(EchoArgs.class, (repl, parsed) -> {
                    output.append(parsed.text).append(":").append(parsed.number);
                })
                .register();

        ReplRunner dummy = new ReplRunner() {
            public void println(String s) { /* no-op */ }
        };

        Arguments args = Arguments.parse("world --num 7");
        args.skip();
        cmd.getAction().apply(dummy, args); // выполнится consumer

        assertEquals("world:7", output.toString());
    }



}
