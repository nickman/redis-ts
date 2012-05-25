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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: Tier</p>
 * <p>Description: Model and parser for one timeseries tier that is a rollup of the timeseries tier below it. The bottom timeseries tier is <b><code>live</code></b>.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.cor e.Tier</code></p>
 */

public class Tier {
	/** The number of periods in this timeseries tier */
	protected int periodCount;
	/** The elapsed time of one period in the timeseries tier */
	protected int period;
	/** The time duration of one full tier rotation */
	protected int tierDuration;
	/** The time unit of the specified period */
	protected TimeUnit periodTimeUnit;
	/** The time unit of the specified tier duration */
	protected TimeUnit tierTimeUnit;
	/** The name of this tier */
	protected String name;
	
	public static final Set<String> FIELD_CODES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"p"
	)));	
	Tier(String tierDef, int level) {
		if(level==0) {
			name = TimeSeriesModel.LIVE_TIER;
		}
	}
	
	
	
	
}


/**
import java.util.regex.*

Pattern p = Pattern.compile("([p|d|c|n])=(\\d+)([s|m|h|d|w])", Pattern.CASE_INSENSITIVE);
String value = "p=2h";
m = p.matcher(value);
if(m.matches()) {
    println "MATCH";
} else {
    println "NO MATCH";
}



return null;


*/