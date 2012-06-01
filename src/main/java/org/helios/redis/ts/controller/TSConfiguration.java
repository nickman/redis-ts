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
package org.helios.redis.ts.controller;

/**
 * <p>Title: TSConfiguration</p>
 * <p>Description: Defines the redis-ts configuration and the keys where it will be written into in Redis</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.controller.TSConfiguration</code></p>
 */

public interface TSConfiguration {
	/** The key delimeter used by redis-ts */
	public static final String TS_DELIM = ".";
	/** The root key redis-ts namespace. */
	public static final String TS_ROOT = "redis-ts";
	/** The root redis-ts configuration namespace */
	public static final String TS_CONFIG_ROOT =  TS_ROOT + TS_DELIM + "config";
	/** The redis-ts configured model */
	public static final String TS_MODEL = TS_CONFIG_ROOT + TS_DELIM + "model";
	/** The redis-ts tier names */
	public static final String TS_TIER_NAMES = TS_CONFIG_ROOT + TS_DELIM + "tier-names";
	
	
	
	/** The pubsub delimeter used by redis-ts */
	public static final String PS_DELIM = ".";
	/** The root pubsub redis-ts channel namespace. */
	public static final String PS_ROOT = "redis-ts";

	
	// model 
	// tiers
	// scripts
	
	
}
