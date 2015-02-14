package de.hskl.ps.bluetoothinvokeexample.util;

import android.util.Log;
import de.hskl.ps.bluetoothinvokeexample.BuildConfig;

/**
 * Better Log Class.
 * 
 * Wrapper around android.util.Log. Checks for {@link BuildConfig#DEBUG}. Also tries to gain some
 * performance by using {@link String#format(String, Object...)}.
 * 
 * @author Patrick Schwartz
 * @date 2015
 */
public final class BetterLog {

    /**
     * Send a {@link Log#DEBUG} Log message.
     * 
     * Will only be logged if {@link BuildConfig#DEBUG} is true.
     * 
     * @param tag
     *            Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg
     *            Format message that should be logged.
     * @param args
     *            Arguments for formatting.
     */
    public static void d(String tag, String msg, Object... args) {
        if(BuildConfig.DEBUG) {
            Log.d(tag, String.format(msg, args));
        }
    }

    /**
     * Send a {@link Log#ERROR} Log message.
     * 
     * Will always be logged.
     * 
     * @param tag
     *            Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg
     *            Format message that should be logged.
     * @param args
     *            Arguments for formatting.
     */
    public static void e(String tag, String msg, Object... args) {
        Log.e(tag, String.format(msg, args));
    }

    /**
     * Send a {@link Log#ERROR} Log message.
     * 
     * Will always be logged.
     * 
     * @param tag
     *            Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param t
     *            An Exception to log.
     * @param msg
     *            Format message that should be logged.
     * @param args
     *            Arguments for formatting.
     */
    public static void e(String tag, Throwable t, String msg, Object... args) {
        Log.e(tag, String.format(msg, args), t);
    }

    /**
     * Send a {@link Log#INFO} Log message.
     * 
     * Will always be logged.
     * 
     * @param tag
     *            Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg
     *            Format message that should be logged.
     * @param args
     *            Arguments for formatting.
     */
    public static void i(String tag, String msg, Object... args) {
        Log.i(tag, String.format(msg, args));
    }

    /**
     * Send a {@link Log#VERBOSE} Log message.
     * 
     * Will only be logged if {@link BuildConfig#DEBUG} is true.
     * 
     * @param tag
     *            Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @param msg
     *            Format message that should be logged.
     * @param args
     *            Arguments for formatting.
     */
    public static void v(String tag, String msg, Object... args) {
        if(BuildConfig.DEBUG) {
            Log.v(tag, String.format(msg, args));
        }
    }
}
