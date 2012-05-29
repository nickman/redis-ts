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

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * <p>Title: TSController</p>
 * <p>Description: The main redis-ts controller</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.controller.TSController</code></p>
 */

public class TSController {
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	/** The configured raw properties */
	protected final Properties tsProperties;
	
	/**
	 * Creates a new TSController
	 * @param props The configured properties
	 */
	TSController(Properties props) {
		this.tsProperties = props;
	}
}


/**
 * Connect to redis, on fail, start reconnect loop
 * OnConnect:
 * 	Verify if config already exists. If it does, verify it matches. If not, exit with error message. 
 * (Need way to clear existing config)
 * Decompose config and write individual members
 * Load script templates and populate tokens
 * Load scripts into redis and publish aliases
 * 
 * 
 */
