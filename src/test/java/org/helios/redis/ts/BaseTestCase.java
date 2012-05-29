/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package org.helios.redis.ts;

import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * <p>Title: BaseTestCase</p>
 * <p>Description: Base test case</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.BaseTestCase</code></p>
 */

public abstract class BaseTestCase {
	protected final Logger log = Logger.getLogger(getClass());

	/** The name of the test method currently running */
	@Rule
	public TestName TESTNAME = new TestName();
	/** My trusty random generator */
	protected final Random R = new Random(System.nanoTime());
	
	/**
	 * No frills logger
	 * @param o An object to log
	 */
	protected void log(Object o) {
		log.info(o);
	}
	
	/**
	 * Returns a positive random long
	 * @return a positive random long
	 */
	protected long randLong() {
		return Math.abs(R.nextLong());
	}
	
	/**
	 * Returns a positive random int
	 * @return a positive random int
	 */
	protected int randInt() {
		return Math.abs(R.nextInt());
	}

	/**
	 * Returns a positive random int
	 * @param n he bound on the random number to be returned.
	 * @return a positive random int
	 */
	protected int randInt(int n) {
		return Math.abs(R.nextInt(n));
	}
	
	/**
	 * Static init beft ore test case class
	 * @throws java.lang.Exception thrown on any exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
	}

	/**
	 * Static teardown after  test case class
	 * @throws java.lang.Exception thrown on any exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Init for before test method
	 * @throws java.lang.Exception hrown on any exception
	 */
	@Before
	public void setUp() throws Exception {
		log.info("\t\tTest [" + TESTNAME.getMethodName() + "]");
	}

	/**
	 * Teardown for after test method
	 * @throws java.lang.Exception hrown on any exception
	 */
	@After
	public void tearDown() throws Exception {
	}


}
