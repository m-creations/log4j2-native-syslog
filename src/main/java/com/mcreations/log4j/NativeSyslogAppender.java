package com.mcreations.log4j;

import static com.mcreations.log4j.Clib.LOG_ERR;
import static com.mcreations.log4j.Clib.LOG_INFO;
import static com.mcreations.log4j.Clib.LOG_NDELAY;
import static com.mcreations.log4j.Clib.LOG_PERROR;
import static com.mcreations.log4j.Clib.LOG_PID;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.net.Facility;
import org.apache.logging.log4j.core.net.Priority;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;

/**
 * 
 * <ul>
 * <li>in rsyslog.conf: $MaxMessageSize 1M
 * <li>in /etc/rsyslog.d/99-myapplication.conf: local0.*
 * /var/log/myapplication.log
 * <li>touch /var/log/myapplication.log &amp;&amp; chmod syslog:adm
 * /var/log/myapplication.log
 * </ul>
 */
@Plugin(name = "NativeSyslog", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class NativeSyslogAppender extends AbstractAppender {

	private ByteBufferOutputStream out;
	private Facility facility;
	/**
	 * Flag which is set to <code>true</code> after openlog is called.
	 */
	private boolean open = false;

	protected NativeSyslogAppender(String name, Facility facility, Layout<? extends Serializable> layout, Filter filter,
			boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
		this.facility = facility;
		out = new ByteBufferOutputStream(Constants.ENCODER_BYTE_BUFFER_SIZE);
	}

	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		// Clib.inst.printf("%s\n", "Hello World!");

		openlog("mcreations-syslog", (16 << 3), LOG_PERROR, LOG_PID, LOG_NDELAY);
		Clib.inst.syslog(LOG_ERR, "Hello world!");

		ByteBufferOutputStream out = new ByteBufferOutputStream();
		out.write("Jöi!".getBytes("UTF-8"));
		ByteBuffer buf = out.getByteBuffer();
		buf.rewind();
		Clib.inst.syslog(LOG_INFO, buf);

		for (int i = 0; i < 16 * 1024; i++) {
			out.write("-".getBytes());
		}
		out.write("schulz".getBytes());
		buf.rewind();
		Clib.inst.syslog(LOG_INFO, buf);
		out.close();
		Clib.inst.closelog();
	}

	private static void openlog(String ident, int facility, int... options) {
		int option = 0;
		for (int o : options) {
			option |= o;
		}
		Clib.inst.openlog(ident, option, facility);
	}

	public void append(final LogEvent event) {
		LoggerContext ctx = LogManager.getContext(false);
		ExtendedLogger lg = ctx.getLogger(event.getLoggerName());
		if (!lg.isEnabled(event.getLevel()))
			return;
		if (!open) {
			openlog(getName(), facility.getCode(), LOG_PID, LOG_NDELAY);
			open = true;
		}
		try {
			getLayout().encode(event, out);
			out.flush();
			ByteBuffer buf = out.getByteBuffer();
			buf.rewind();
			Clib.inst.syslog(Priority.getPriority(facility, event.getLevel()), out.getByteBuffer());
		} catch (final AppenderLoggingException ex) {
			error("Unable to call native syslog function for appender " + getName() + ": " + ex);
			throw ex;
		} catch (IOException ioe) {
			error("Unable to flush the output stream of appender " + getName() + ": " + ioe);
		}
	}

	/**
	 * Create a NativeSyslogAppender.
	 * 
	 * @param name
	 *            The name of the Appender.
	 * @param ignoreExceptions
	 *            If {@code "true"} (default) exceptions encountered when
	 *            appending events are logged; otherwise they are propagated to
	 *            the caller.
	 * @param facility
	 *            The Facility is used to try to classify the message.
	 * @param id the ID of the Appender
	 * @param newLine
	 *            If true, a newline will be appended to the end of the syslog
	 *            record. The default is false.
	 * @param escapeNL
	 *            String that should be used to replace newlines within the
	 *            message text.
	 * @param layout the {@link Layout} of this Appender
	 * @param filter
	 *            A Filter to determine if the event should be handled by this
	 *            Appender.
	 * @param charsetName
	 *            The character set to use when converting the syslog String to
	 *            a byte array.
	 * @return A SyslogAppender.
	 */
	@PluginFactory
	public static NativeSyslogAppender createAppender(
			// @formatter:off
			@PluginAttribute("name") final String name,
			@PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions,
			@PluginAttribute(value = "facility", defaultString = "LOCAL0") final Facility facility,
			@PluginAttribute("id") final String id,
			@PluginAttribute(value = "newLine", defaultBoolean = false) final boolean newLine,
			@PluginAttribute("newLineEscape") final String escapeNL,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filter") final Filter filter,
			@PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charsetName) {
		// @formatter:on
		if (name == null) {
			LOGGER.error("No name provided for NativeSyslogAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new NativeSyslogAppender(name, facility, layout, filter, ignoreExceptions);
	}
}
