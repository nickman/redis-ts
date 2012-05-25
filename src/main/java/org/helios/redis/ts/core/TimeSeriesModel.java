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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: TimeSeriesModel</p>
 * <p>Description: Container and parser for the timeseries core structure and tier model.</p>
 * <p>
 * Period: p
 * Duration: d
 * Count: c
 * Name: n
 * </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.core.TimeSeriesModel</code></p>
 */

public class TimeSeriesModel {
	/**  The live ts tier name */ 
	public static final String LIVE_TIER = "live";
	
	/** The timeseries tiers */
	protected List<Tier> tiers = new ArrayList<Tier>();
	
	private TimeSeriesModel() {
		
	}
	
	/**
	 * Creates a new TimeSeriesModel from the passed stringified model
	 * @param model The string representation of the model
	 * @return a new TimeSeriesModel 
	 */
	public static TimeSeriesModel create(String model) {
		if(model==null) throw new IllegalArgumentException("The passed model was null", new Throwable());
		String[] frags = model.split("\\|");
		int cnt = 0;
		for(String frag: frags) {
			frag = frag.replace(" ", "");
			Tier tier = new Tier(frag, cnt);
			
			cnt++;
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		log("Test TimeSeriesModel");
		String config = "p=15s,d=15m   |  p=2m,d=1h";
	}
	
	public static void log(Object msg) {
		System.out.println(msg);
	}
}
	
	
