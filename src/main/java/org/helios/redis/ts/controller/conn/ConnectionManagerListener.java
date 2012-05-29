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
package org.helios.redis.ts.controller.conn;

/**
 * <p>Title: ConnectionManagerListener</p>
 * <p>Description: Defines a listener that receives events from the {@link RedisConnectionManager}</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.controller.conn.ConnectionManagerListener</code></p>
 */
public interface ConnectionManagerListener {
	/**
	 * Fired when the connection manager connects 
	 * or reconnects to an instance with different run_id from the prior connection. 
	 */
	public void onConnectNewInstance();
	
	/**
	 * Fired when the connection manager connects to the 
	 * same run_id as was connected to in the prior connection 
	 */
	public void onConnect();
	
	/**
	 * Fired when connectivity is lost
	 */
	public void onDisconnect();
	
	/**
	 * Fired when a heatbeat is not received in a timely manner. 
	 * Does not, by itself, indicate a disconnect.
	 */
	public void onHeartbeatFailed();
}
