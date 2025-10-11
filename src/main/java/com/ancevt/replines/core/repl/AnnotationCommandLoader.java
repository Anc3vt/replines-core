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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Loads commands from classes and methods annotated with
 * {@link ReplCommand}
 * and {@link ReplExecute}.
 * <p>
 * This utility scans objects or classes using reflection
 * and registers discovered commands into a {@link CommandRegistry}.
 * </p>
 *
 * Supported cases:
 * <ul>
 *     <li><b>Class-level</b>: a class annotated with {@link ReplCommand} containing
 *     a method annotated with {@link ReplExecute}.</li>
 *     <li><b>Method-level</b>: any method annotated with {@link ReplCommand}.</li>
 * </ul>
 *
 * Example:
 * <pre>
 * &#64;ReplCommand(name = "hello", description = "Says hello")
 * public class HelloCommand {
 *
 *     &#64;ReplExecute
 *     public void run(ReplRunner repl, Arguments args) {
 *         repl.println("Hello, world!");
 *     }
 * }
 *
 * CommandRegistry registry = new CommandRegistry();
 * AnnotationCommandLoader.load(registry, HelloCommand.class);
 * </pre>
 */
public class AnnotationCommandLoader {

    /**
     * Loads commands from a set of instances.
     *
     * @param registry  target registry to register commands in
     * @param instances one or more instances containing annotated commands
     */
    public static void load(CommandRegistry registry, Object... instances) {
        for (Object instance : instances) {
            load(registry, instance);
        }
    }

    /**
     * Loads commands from a list of instances.
     *
     * @param registry  target registry
     * @param instances list of instances containing annotated commands
     */
    public static void load(CommandRegistry registry, List<Object> instances) {
        for (Object instance : instances) {
            load(registry, instance);
        }
    }

    /**
     * Loads commands from a class annotated with {@link ReplCommand}.
     * The class must have a no-arg constructor.
     *
     * @param registry target registry
     * @param clazz    class to scan
     * @throws RuntimeException if instantiation fails
     */
    public static void load(CommandRegistry registry, Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            load(registry, instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate class: " + clazz.getName(), e);
        }
    }

    /**
     * Loads commands from a list of classes.
     *
     * @param registry target registry
     * @param classes  list of classes to scan
     */
    public static void loadClass(CommandRegistry registry, List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            load(registry, clazz);
        }
    }

    /**
     * Loads commands from multiple classes.
     *
     * @param registry target registry
     * @param classes  classes to scan
     */
    public static void loadClass(CommandRegistry registry, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            load(registry, clazz);
        }
    }

    /**
     * Loads commands from a single instance.
     * <p>
     * Supports:
     * <ul>
     *     <li>Class annotated with {@link ReplCommand} + a method annotated with {@link ReplExecute}</li>
     *     <li>Methods annotated with {@link ReplCommand}</li>
     * </ul>
     *
     * @param registry target registry
     * @param instance object to scan
     */
    public static void load(CommandRegistry registry, Object instance) {
        if (instance == null) return;
        Class<?> clazz = instance.getClass();

        // Case 1: class-level annotation + @ReplExecute method
        if (clazz.isAnnotationPresent(ReplCommand.class)) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(ReplExecute.class)) {
                    registerCommand(clazz.getAnnotation(ReplCommand.class), method, instance, registry);
                }
            }
        }

        // Case 2: method-level annotation
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ReplCommand.class)) {
                registerCommand(method.getAnnotation(ReplCommand.class), method, instance, registry);
            }
        }
    }

    /**
     * Registers a discovered command into the registry.
     *
     * @param cmdAnn   annotation containing metadata
     * @param method   method to invoke
     * @param instance object containing the method
     * @param registry registry to register in
     */
    private static void registerCommand(ReplCommand cmdAnn, Method method,
                                        Object instance, CommandRegistry registry) {
        method.setAccessible(true);

        BiFunction<ReplRunner, Arguments, Object> action = (repl, args) -> {
            try {
                return method.invoke(instance, repl, args);
            } catch (Exception e) {
                throw new RuntimeException("Failed to execute command: " + cmdAnn.name(), e);
            }
        };

        Command<Object> command = new Command<>(
                cmdAnn.name(),
                cmdAnn.description(),
                action
        );

        command.setAsync(cmdAnn.async());
        registry.register(command);
    }
}
