package com.mcreations.log4j;

import java.nio.ByteBuffer;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public interface Clib extends Library {
	Clib inst = (Clib) Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"), Clib.class);

	/** #define LOG_PID 0x01 // log the pid with each message */
	public static final int LOG_PID = 0x01;
	/** #define LOG_CONS 0x02 // log on the console if errors in sending */
	public static final int LOG_CONS = 0x02;
	/** #define LOG_ODELAY 0x04 // delay open until first syslog() (default) */
	public static final int LOG_ODELAY = 0x04;
	/** #define LOG_NDELAY 0x08 // don't delay open */
	public static final int LOG_NDELAY = 0x08;
	/** #define LOG_NOWAIT 0x10 /* don't wait for console forks: DEPRECATED */
	public static final int LOG_NOWAIT = 0x10;
	/** #define LOG_PERROR 0x20 // log to stderr as well */
	public static final int LOG_PERROR = 0x20;

	// Priorities -------------------------------------------
	/** #define LOG_EMERG 0 // system is unusable */
	public static final int LOG_EMERG = 0;
	/** #define LOG_ALERT 1 // action must be taken immediately */
	public static final int LOG_ALERT = 1;
	/** #define LOG_CRIT 2 // critical conditions */
	public static final int LOG_CRIT = 2;
	/** #define LOG_ERR 3 // error conditions */
	public static final int LOG_ERR = 3;
	/** #define LOG_WARNING 4 // warning conditions */
	public static final int LOG_WARNING = 4;
	/** #define LOG_NOTICE 5 // normal but significant condition */
	public static final int LOG_NOTICE = 5;
	/** #define LOG_INFO 6 // informational */
	public static final int LOG_INFO = 6;
	/** #define LOG_DEBUG 7 // debug-level messages */
	public static final int LOG_DEBUG = 7;

	void printf(String format, Object... args);

	void openlog(final String ident, int option, int facility);

	void syslog(int priority, String format, Object... args);

	/**
	 * Overloaded version of {@link #syslog(int, String, Object...)} where
	 * <tt>buf</tt> is a {@link ByteBuffer}.
	 * 
	 * @param priority
	 *            one of the priorities {@link #LOG_EMERG}, {@link #LOG_ALERT},
	 *            {@link #LOG_CRIT}, {@link #LOG_ERR}, {@link #LOG_WARNING},
	 *            {@link #LOG_NOTICE}, {@link #LOG_INFO}, {@link #LOG_DEBUG}
	 * @param buf
	 *            the {@link ByteBuffer} which contains the format string of the
	 *            message
	 * @param args
	 *            possible arguments to the format string, possibly empty
	 */
	void syslog(int priority, ByteBuffer buf, Object... args);

	void closelog();
}