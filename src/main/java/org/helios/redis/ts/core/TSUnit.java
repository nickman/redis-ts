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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: TSUnit</p>
 * <p>Description: An extended analog of {@link java.util.concurrent.TimeUnit} that starts at SECONDS adds an extra member called WEEK and provides decodes for short names. </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.core.TSUnit</code></p>
 */

public enum TSUnit  {
	/** The seconds TSUnit */
	SECONDS(1L, "S"),
	/** The minutes TSUnit */
	MINUTES(60L, "M"),
	/** The hours TSUnit */
	HOURS(3600L, "H"),
	/** The days TSUnit */
	DAYS(86400L, "D"),
	/** The weeks TSUnit */
	WEEKS(86400L*7, "W");
	
	private static final Map<String, TSUnit> CODE2TSUNIT = new HashMap<String, TSUnit>(TSUnit.values().length*2);
	
	static {
		for(TSUnit ts: TSUnit.values()) {
			CODE2TSUNIT.put(ts.shortCode, ts);
			CODE2TSUNIT.put(ts.shortCode.toLowerCase(), ts);
		}
	}
	
	/**
	 * Determines if the passed code is a valid short name regardless of case
	 * @param code The code to test
	 * @return true if the code is a valid code, false if it is not.
	 */
	public static boolean isValidCode(String code) {
		if(code==null) return false;
		return CODE2TSUNIT.containsKey(code);
	}
	
	/**
	 * Returns the matching TSUnit for the passed short code. The passed code is trimmed.
	 * @param code The short code to get the TSUnit for
	 * @return the matching TSUnit
	 */
	public static TSUnit forCode(String code) {
		if(code==null) throw new IllegalArgumentException("The passed code was null", new Throwable());
		code = code.trim();
		TSUnit ts = CODE2TSUNIT.get(code);
		if(ts==null) throw new IllegalArgumentException("The passed code [" + code + "] was not a valid TSUnit short code", new Throwable());
		return ts;
	}
	
	/**
	 * Creates a new TSUnit
	 * @param secs The number of seconds in one unit of this TSUnit
	 * @param shortCode The short code for this unit
	 */
	private TSUnit(long secs, String shortCode) {
		this.secs= secs;
		this.shortCode = shortCode; 
	}
	
	/** The number of seconds in this TSUnit */
	public final long secs;
	/** The short name of this TSUnit */
	public final String shortCode;
	
	public long convert(long sourceValue, TSUnit sourceUnit) {
		if(sourceUnit==null) throw new IllegalArgumentException("The passed source unit was null", new Throwable());
		if(sourceUnit==this) return sourceValue;
		long result = sourceUnit.secs * sourceValue/this.secs;
		return result;		
	}
	
	public static void main(String[] args) {
		log("TSUnit Test");
		log("6 Hours in Minutes:" + TSUnit.MINUTES.convert(6, TSUnit.HOURS));
		log("60 Minutes in Hours:" + TSUnit.HOURS.convert(90, TSUnit.MINUTES));
	}
	
	public static void log(Object msg) {
		System.out.println(msg);
	}
	
}
