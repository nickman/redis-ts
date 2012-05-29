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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * <p>Title: Boot</p>
 * <p>Description: Main bootstrap to start redis-ts.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.controller.Boot</code></p>
 */

public class Boot {
	/** Static class logger */
	protected static final Logger LOG = Logger.getLogger(Boot.class);
	/**
	 * Expects one argument which is the name of the redis-ts configuration file.
	 * @param args the name of the redis-ts configuration file
	 */
	public static void main(String[] args) {
		if(args.length<1) {
			LOG.warn("Invalid usage");
			LOG.warn("Usage:  java org.helios.redis.ts.controller.Boot <config file>" );
			System.exit(-1);
		}
		File f = new File(args[0]);
		if(!f.canRead()) {
			LOG.warn("Unable to read configuration file [" + f + "]");
			System.exit(-2);
		}
		FileInputStream fos = null;
		Properties p = new Properties();
		try {
			fos = new FileInputStream(f);
			p.load(fos);
			new TSController(p);
		} catch (IOException ioe) {
			LOG.error("Failed to read properties from [" + f + "]", ioe);
			System.exit(-3);
		} finally {
			try { fos.close(); } catch (Exception e) {}
		}
	}

}
