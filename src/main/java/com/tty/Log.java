package com.tty;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Log {

    private static volatile Logger LOGGER;
    private static volatile boolean DEBUG;

    private static final String PREFIX_DEBUG = "[DEBUG] ";

    private static final String[] ANSI_COLORS = {
            "\u001B[31m",
            "\u001B[32m",
            "\u001B[33m",
            "\u001B[34m",
            "\u001B[35m",
            "\u001B[36m",
            "\u001B[91m",
            "\u001B[92m",
            "\u001B[93m",
            "\u001B[94m"
    };

    private static final String ANSI_RESET = "\u001B[0m";

    private static volatile boolean ENABLE_COLOR = true;
    private static volatile Level MIN_LEVEL = Level.INFO;

    public static void init(Logger logger, boolean debug) {
        if (logger == null) {
            throw new IllegalArgumentException("Log init failed: logger is null.");
        }
        LOGGER = logger;
        DEBUG = debug;
    }

    public static void ensureInitialized() {
        if (LOGGER == null) {
            synchronized (Log.class) {
                if (LOGGER == null) {
                    LOGGER = Logger.getLogger("ComTTYLib");
                }
            }
        }
    }

    public static void setEnableColor(boolean enable) {
        ENABLE_COLOR = enable;
    }

    public static void setMinLevel(Level level) {
        if (level != null) {
            MIN_LEVEL = level;
        }
    }

    public static void info(String msg, Object... args) {
        log(Level.INFO, msg, args);
    }

    public static void warn(String msg, Object... args) {
        log(Level.WARNING, msg, args);
    }

    public static void warn(Throwable throwable) {
        log(Level.WARNING, throwable, null);
    }

    public static void warn(Throwable throwable, String msg, Object... args) {
        log(Level.WARNING, throwable, msg, args);
    }

    public static void error(String msg, Object... args) {
        log(Level.SEVERE, msg, args);
    }

    public static void error(Throwable throwable) {
        log(Level.SEVERE, throwable, null);
    }

    public static void error(Throwable throwable, String msg, Object... args) {
        log(Level.SEVERE, throwable, msg, args);
    }

    public static void debug(String msg, Object... args) {
        if (!DEBUG) return;
        log(Level.INFO, PREFIX_DEBUG + wrapCaller(msg), args);
    }

    public static void debug(Throwable throwable, String msg, Object... args) {
        if (!DEBUG) return;
        log(Level.INFO, throwable, PREFIX_DEBUG + wrapCaller(msg), args);
    }

    private static void log(Level level, String msg, Object... args) {
        ensureInitialized();
        if (shouldNotLog(level)) return;

        LOGGER.log(level, formatMessage(msg, args));
    }

    private static void log(Level level, Throwable throwable, String msg, Object... args) {
        ensureInitialized();
        if (shouldNotLog(level)) return;

        String message = (msg == null)
                ? wrapCaller(throwable.getMessage())
                : formatMessage(msg, args);

        LOGGER.log(level, message, throwable);
    }

    private static boolean shouldNotLog(Level level) {
        return level.intValue() < MIN_LEVEL.intValue();
    }

    private static String formatMessage(String msg, Object... args) {
        if (msg == null) {
            return "null";
        }
        if (args == null || args.length == 0) {
            return msg;
        }
        return formatBraceStyle(msg, args);
    }

    private static String formatBraceStyle(String msg, Object... args) {
        StringBuilder sb = new StringBuilder();
        int length = msg.length();
        int seqIndex = 0;

        for (int i = 0; i < length; i++) {
            char c = msg.charAt(i);

            if (c == '{' && i + 1 < length) {

                if (i + 3 < length
                        && msg.charAt(i + 1) == '{'
                        && msg.charAt(i + 2) == '}'
                        && msg.charAt(i + 3) == '}') {

                    sb.append("{}");
                    i += 3;
                    continue;
                }

                if (msg.charAt(i + 1) == '}') {
                    if (seqIndex < args.length) {
                        appendArg(sb, args[seqIndex++]);
                    } else {
                        sb.append("{}");
                    }
                    i++;
                    continue;
                }

                int end = msg.indexOf('}', i + 1);
                if (end > i + 1) {
                    String indexStr = msg.substring(i + 1, end);
                    try {
                        int index = Integer.parseInt(indexStr);
                        if (index >= 0 && index < args.length) {
                            appendArg(sb, args[index]);
                        } else {
                            sb.append('{').append(indexStr).append('}');
                        }
                        i = end;
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            sb.append(c);
        }

        while (seqIndex < args.length) {
            sb.append(" ");
            appendArg(sb, args[seqIndex++]);
        }

        return sb.toString();
    }

    private static void appendArg(StringBuilder sb, Object arg) {
        String value = String.valueOf(arg);
        if (ENABLE_COLOR) {
            sb.append(randomColor())
                    .append(value)
                    .append(ANSI_RESET);
        } else {
            sb.append(value);
        }
    }

    private static String wrapCaller(String msg) {
        return "[" + getCallerClassName() + "] " + msg;
    }

    private static String getCallerClassName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (!className.equals(Log.class.getName())
                    && !className.startsWith("java.lang.Thread")) {
                return colorize(className);
            }
        }
        return colorize("Unknown");
    }

    private static String randomColor() {
        return ANSI_COLORS[ThreadLocalRandom.current().nextInt(ANSI_COLORS.length)];
    }

    private static String colorize(String text) {
        if (!ENABLE_COLOR || text == null || text.isEmpty()) {
            return text;
        }
        return randomColor() + text + ANSI_RESET;
    }
}
