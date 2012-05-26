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
import static org.junit.Assert.fail;

import org.helios.redis.ts.BaseTestCase;
import org.junit.Test;

/**
 * <p>Title: TierTestCase</p>
 * <p>Description: Test cases for the Tier parser</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.core.TierTestCase</code></p>
 */

public class TierTestCase extends BaseTestCase {

	/**
	 * Tests a series of two triplet tier expressions calculating the tier duration
	 */
	@Test
	public void testTwoTripletsCalcTier() {
		tripletTest("c=60, p=15s", 0, Tier.LIVE_TIER, 15, 15*60, 60);
		tripletTest("c=60, p=15s", 1, "t1", 15, 15*60, 60);
	}
	
	/**
	 * Tests a series of two triplet tier expressions calculating the period count
	 */
	@Test
	public void testTwoTripletsCalcCount() {
		tripletTest("t=15m, p=15s", 0, Tier.LIVE_TIER, 15, 15*60, 60);
		tripletTest("t=15m, p=15s", 1, "t1", 15, 15*60, 60);
	}

	/**
	 * Tests a series of two triplet tier expressions calculating the period
	 */
	@Test
	public void testTwoTripletsCalcPeriod() {
		tripletTest("t=15m, c=60", 0, Tier.LIVE_TIER, 15, 15*60, 60);
		tripletTest("t=15m, c=60", 1, "t1", 15, 15*60, 60);
	}

	/**
	 * Tests a series of two triplet tier expressions calculating the tier duration
	 */
	@Test
	public void testThreeTriplets() {
		tripletTest("c=60, p=15s, t=15m", 0, Tier.LIVE_TIER, 15, 15*60, 60);
		tripletTest("c=60, p=15s, t=15m", 1, "t1", 15, 15*60, 60);
	}
	
	
	/**
	 * Tests a tier creation
	 * @param tierDef The tier definition expressions
	 * @param level The level of the tier within the time series model
	 * @param name The expected name of the tier
	 * @param periodDuration The expected period duration in seconds
	 * @param tierDuration The expected tier duration in seconds
	 * @param periodCount The expected number of periods in the tier
	 */
	protected void tripletTest(String tierDef, int level, String name, long periodDuration, long tierDuration, long periodCount) {
		Tier tier = Tier.newTier(tierDef, level);
		assertEquals("Unexpected name for expression [" + tierDef + "]", name, tier.getName());
		assertEquals("Unexpected period duration for expression [" + tierDef + "]", periodDuration, tier.getPeriodDuration().seconds);
		assertEquals("Unexpected tier duration for expression [" + tierDef + "]", tierDuration, tier.getTierDuration().seconds);
		assertEquals("Unexpected period count for expression [" + tierDef + "]", periodCount, tier.getPeriodCount());
		assertEquals("Unexpected tier level for provided level [" + level + "] and expression [" + tierDef + "]", level, tier.getLevel());		
	}

}
