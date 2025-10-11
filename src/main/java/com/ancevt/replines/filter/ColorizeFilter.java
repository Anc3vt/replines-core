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

package com.ancevt.replines.filter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorizeFilter {

    private static final String RESET = "\u001B[0m";

    private final Map<String, String> tags = new LinkedHashMap<>();
    private boolean enabled = true;

    public ColorizeFilter() {
        // === Foreground colors (basic) ===
        tags.put("<black>", "\u001B[30m");
        tags.put("<red>", "\u001B[31m");
        tags.put("<green>", "\u001B[32m");
        tags.put("<yellow>", "\u001B[33m");
        tags.put("<blue>", "\u001B[34m");
        tags.put("<magenta>", "\u001B[35m");
        tags.put("<cyan>", "\u001B[36m");
        tags.put("<white>", "\u001B[37m");

        // Aliases (shortcuts)
        tags.put("<k>", "\u001B[30m"); // black
        tags.put("<r>", "\u001B[31m"); // red
        tags.put("<g>", "\u001B[32m"); // green
        tags.put("<y>", "\u001B[33m"); // yellow
        tags.put("<b>", "\u001B[34m"); // blue
        tags.put("<m>", "\u001B[35m"); // magenta
        tags.put("<c>", "\u001B[36m"); // cyan
        tags.put("<w>", "\u001B[37m"); // white

        // === Bright foreground colors ===
        tags.put("<bright-black>", "\u001B[90m");
        tags.put("<bright-red>", "\u001B[91m");
        tags.put("<bright-green>", "\u001B[92m");
        tags.put("<bright-yellow>", "\u001B[93m");
        tags.put("<bright-blue>", "\u001B[94m");
        tags.put("<bright-magenta>", "\u001B[95m");
        tags.put("<bright-cyan>", "\u001B[96m");
        tags.put("<bright-white>", "\u001B[97m");

        // === Background colors (basic) ===
        tags.put("<bg-black>", "\u001B[40m");
        tags.put("<bg-red>", "\u001B[41m");
        tags.put("<bg-green>", "\u001B[42m");
        tags.put("<bg-yellow>", "\u001B[43m");
        tags.put("<bg-blue>", "\u001B[44m");
        tags.put("<bg-magenta>", "\u001B[45m");
        tags.put("<bg-cyan>", "\u001B[46m");
        tags.put("<bg-white>", "\u001B[47m");

        // === Bright background colors ===
        tags.put("<bg-bright-black>", "\u001B[100m");
        tags.put("<bg-bright-red>", "\u001B[101m");
        tags.put("<bg-bright-green>", "\u001B[102m");
        tags.put("<bg-bright-yellow>", "\u001B[103m");
        tags.put("<bg-bright-blue>", "\u001B[104m");
        tags.put("<bg-bright-magenta>", "\u001B[105m");
        tags.put("<bg-bright-cyan>", "\u001B[106m");
        tags.put("<bg-bright-white>", "\u001B[107m");

        // === Text styles ===
        tags.put("<bold>", "\u001B[1m");
        tags.put("<underline>", "\u001B[4m");
        tags.put("<blink>", "\u001B[5m");

        // === Reset ===
        tags.put("<reset>", RESET);
        tags.put("<>", RESET); // shortcut
    }

    public void setEnabled(boolean flag) {
        enabled = flag;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]*>");

    public String colorize(String input) {
        if (!enabled) {
            return TAG_PATTERN.matcher(input).replaceAll("");
        }
        Matcher m = TAG_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String tag = m.group();
            String ansi = tags.getOrDefault(tag, "");
            m.appendReplacement(sb, ansi);
        }
        m.appendTail(sb);
        if (!input.endsWith("<reset>") && !input.endsWith("<>")) {
            sb.append(RESET);
        }
        return sb.toString();
    }

}
