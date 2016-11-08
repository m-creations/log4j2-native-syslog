package com.mcreations.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class NativeSyslogAppenderTest extends TestCase {
	Logger log = LogManager.getLogger();

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public NativeSyslogAppenderTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(NativeSyslogAppenderTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		log.trace("Logging on level {}", Level.TRACE);
		log.debug("Logging on level {}", Level.DEBUG);
		log.info("Logging on level {}", Level.INFO);
		log.warn("Logging on level {}", Level.WARN);
		log.error("Logging on level {}", Level.ERROR);
		log.fatal("Logging on level {}", Level.FATAL);
		assertTrue(true);
	}
}
