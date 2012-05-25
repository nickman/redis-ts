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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: Tier</p>
 * <p>Description: Model and parser for one timeseries tier that is a rollup of the timeseries tier below it. The bottom timeseries tier is <b><code>live</code></b>.</p>
 * <p>Shot codes for tier attributes<ul>
 * <li><b>p</b>:&nbsp;Period</li>
 * <li><b>d</b>:&nbsp;Duration</li>
 * <li><b>c</b>:&nbsp;Period Count</li>
 * <li><b>n</b>:&nbsp;Name</li>
 * </ul></p> 
 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.cor e.Tier</code></p>
 */

public class Tier {
	/** The number of periods in this timeseries tier */
	protected long periodCount;
	/** The duration od one period in the timeseries tier */
	protected Duration periodDuration;
	/** The duration of one full tier rotation */
	protected Duration tierDuration;
	/** The name of this tier */
	protected String name;
	/** The field codes for a tier definition */
	public static enum FieldCode {
		p,
		d, 
		c,
		n;
	}
	/** The regex to parse a tier expression */
	public static final Pattern TIER_EXPR_REGEX = Pattern.compile("([p|d|c|n])=(?:(\\d+)([s|m|h|d|w])|(.*))", Pattern.CASE_INSENSITIVE | Pattern.COMMENTS);
	/** The regex to split a group of tier expressions */
	public static final Pattern TIER_GRP_REGEX = Pattern.compile(",");
	
	Tier(String tierDef, int level) {
		if(tierDef==null) throw new InvalidTierDefinitionException("The passed tier definition was null");
		tierDef = tierDef.trim().replace(" ", "").toLowerCase();
		if(tierDef.isEmpty()) throw new InvalidTierDefinitionException("The passed tier definition was empty");
		if(level==0) {
			name = TimeSeriesModel.LIVE_TIER;
		}
		String[] expressions = TIER_GRP_REGEX.split(tierDef);		
		Map<String, Triplet> triplets = new HashMap<String, Triplet>(3); // we need 2 out of a possible 3 attributes to complete the tier definition
  
		for(String expression: expressions) {
			Matcher matcher = TIER_EXPR_REGEX.matcher(expression);
			if(!matcher.matches()) {
				throw new InvalidTierDefinitionException("The passed tier definition contained an invalid expression [" + expression + "]");				
			}
			long size = -1L;
			String unit = null;
			String name = null;
			String attr = matcher.group(1);
			if("n".equals(attr)) {
				name = matcher.group(4);
				if(name.isEmpty()) name = "tier" + level;
			} else {
				size = Long.parseLong(matcher.group(2));
				if("c".equals(attr)) {
					unit = null;
				} else {
					unit = matcher.group(3);
				}
				if(triplets.put(attr, new Triplet(attr, unit, size))!=null) {
					throw new InvalidTierDefinitionException("The passed tier definition contained duplicate attributes [" + tierDef + "]");
				}
			}
		}
		if(triplets.size()<2) {
			throw new InvalidTierDefinitionException("The passed tier definition contained an insufficient number of attributes [" + tierDef + "]");
		}
		Set<FieldCode> pending = EnumSet.of(FieldCode.p, FieldCode.d, FieldCode.c);
		for(Triplet triplet: triplets.values()) {
			switch (triplet.fc) {
			case p:
				periodDuration = new Duration(triplet.size, triplet.unit).refine();
				pending.remove(FieldCode.p);
				break;
			case d:
				tierDuration = new Duration(triplet.size, triplet.unit).refine();
				pending.remove(FieldCode.d);
				break;				
			case c:
				periodCount = triplet.size;
				pending.remove(FieldCode.c);
			}
		}
		if(!pending.isEmpty()) {
			switch(pending.iterator().next()) {
			case p:
				periodDuration = new Duration(tierDuration.renderIn(TSUnit.SECONDS).size/periodCount, TSUnit.SECONDS).refine();
				break;
			case d:
				tierDuration = new Duration(periodDuration.renderIn(TSUnit.SECONDS).size*periodCount, TSUnit.SECONDS).refine();
				break;				
			case c:
				periodCount = tierDuration.renderIn(TSUnit.SECONDS).size / periodDuration.renderIn(TSUnit.SECONDS).size;		
			}
		}
	}
	
	
	/**
	 * Constructs a <code>String</code> with all attributes in <code>name:value</code> format.
	 * @return a <code>String</code> representation of this object.
	 */
	public String toString() {
	    final String TAB = "\n\t";
	    StringBuilder retValue = new StringBuilder();    
	    retValue.append("Tier [")
		    .append(TAB).append("periodCount:").append(this.periodCount)
		    .append(TAB).append("periodDuration:").append(this.periodDuration)
		    .append(TAB).append("tierDuration:").append(this.tierDuration)
		    .append(TAB).append("name:").append(this.name)
	    	.append("\n]");    
	    return retValue.toString();
	}
	
	public static void main(String[] args) {
		log("Tier Test");
		log(new Tier("p=15s, d=15m", 0));
	}
	
	public static void log(Object msg) {
		System.out.println(msg);
	}
	
	
	private static class Triplet {
		protected final FieldCode fc;
		protected final TSUnit unit;
		protected final long size;
		
		/**
		 * Creates a new Triplet
		 * @param fc
		 * @param unit
		 * @param size
		 */
		public Triplet(String attr, String unit, long size) {
			this.fc = FieldCode.valueOf(attr);
			this.size = size;
			this.unit = unit==null ? null : TSUnit.forCode(unit);			
		}
	}




	
}


