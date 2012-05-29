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
package org.helios.redis.ts.core;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.helios.redis.ts.BaseTestCase;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <p>Title: TSUnitTestCase</p>
 * <p>Description: Test Cases for TSUnit</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.core.TSUnitTestCase</code></p>
 */

public class TSUnitTestCase extends BaseTestCase {
	/**
	 * Tests secs  to mins.
	 */
	@Test
	public void testSecondsToMinutes() {
		simpleConversion("SECONDS", "MINUTES", 60*randInt(100));
	}

	
	/**
	 * Tests min to secs.
	 */
	@Test
	public void testMinutesToSeconds() {
		simpleConversion("MINUTES", "SECONDS");
	}

	/**
	 * Tests secs  to hours.
	 */
	@Test
	public void testSecondsToHours() {
		simpleConversion("SECONDS", "HOURS", 60*60*randInt(100));
	}

	
	/**
	 * Tests hours to secs.
	 */
	@Test
	public void testHoursToSeconds() {
		simpleConversion("HOURS", "SECONDS");
	}
	
	/**
	 * Tests secs  to days.
	 */
	@Test
	public void testSecondsToDays() {
		simpleConversion("SECONDS", "DAYS", 60*60*24*randInt(100));
	}

	
	/**
	 * Tests days to secs.
	 */
	@Test
	public void testDaysToSeconds() {
		simpleConversion("DAYS", "SECONDS");
	}
	
	/**
	 * Tests secs  to weeks.
	 */
	@Test
	@Ignore
	public void testSecondsToWeeks() {
		simpleConversion("SECONDS", "WEEKS", 60*60*24*7*randInt(100));
	}

	
	/**
	 * Tests weeks to secs.
	 */
	@Test
	public void testWeeksToSeconds() {
		simpleConversion("WEEKS", "SECONDS");
	}
	
	
	/**
	 * Parametersized time conversion test
	 * @param nameFrom The name of the unit fromwhich we are converting
	 * @param nameTo The name of the unit to which we are converting
	 * @param value An optional value to test on. If null, will use a random
	 */
	protected void simpleConversion(String nameFrom, String nameTo, Number value) {
		long base = value!=null ? value.longValue() : randInt(1000);
		long tuVal = -1;
		long tsVal = -1;
		
		if(nameFrom.equals("WEEKS") || nameTo.equals("WEEKS")) {
			if(nameFrom.equals("WEEKS") && nameTo.equals("WEEKS")) {
				tsVal = TSUnit.valueOf(nameTo).convert(base, TSUnit.valueOf(nameFrom));			
				assertEquals(nameFrom + " to " + nameTo + " fail", base, tsVal);
				return;
			}
			if(nameFrom.equals("WEEKS")) {
				tuVal = TimeUnit.valueOf(nameTo).convert(base, TimeUnit.DAYS)*7;
			} else {
				tuVal = TimeUnit.DAYS.convert(base, TimeUnit.valueOf(nameFrom))/7;				
			}
			tsVal = TSUnit.valueOf(nameTo).convert(base, TSUnit.valueOf(nameFrom));
			log("Base:" + base);
			log("TU:" + tuVal);
			log("TS:" + tsVal);
			assertEquals(nameFrom + " to " + nameTo + " fail", tuVal, tsVal);						

		} else {
			tuVal = TimeUnit.valueOf(nameTo).convert(base, TimeUnit.valueOf(nameFrom));
			tsVal = TSUnit.valueOf(nameTo).convert(base, TSUnit.valueOf(nameFrom));
			assertEquals(nameFrom + " to " + nameTo + " fail", tuVal, tsVal);		
		}
		
	}
	
	/**
	 * Parametersized time conversion test
	 * @param nameFrom The name of the unit fromwhich we are converting
	 * @param nameTo The name of the unit to which we are converting
	 */
	protected void simpleConversion(String nameFrom, String nameTo) {
		simpleConversion(nameFrom, nameTo, null);
	}
	

}
