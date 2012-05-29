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
import org.helios.redis.ts.controller.conn.ConnectionManagerListener;
import org.helios.redis.ts.controller.conn.RedisConnectionManager;
import org.helios.redis.ts.tsmodel.TimeSeriesModel;

import redis.clients.jedis.Jedis;

/**
 * <p>Title: TSController</p>
 * <p>Description: The main redis-ts controller</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.controller.TSController</code></p>
 */

public class TSController implements ConnectionManagerListener {
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	/** The configured raw properties */
	protected final Properties tsProperties;
	/** The connection manager */
	protected final RedisConnectionManager connectionManager;
	/** The time-series model expression */
	protected final String tsModelExpr;
	
	/** The time-series model */
	protected TimeSeriesModel tsModel = null;
	/**
	 * Creates a new TSController
	 * @param props The configured properties
	 */
	TSController(Properties props) {
		tsProperties = props;
		String tmp = tsProperties.getProperty("redis.ts.model");
		if(tmp==null) {			
			throw new RuntimeException("No Time Series Model Provided.\nPlease Configure [redis.ts.model] in the redis-ts.conf", new Throwable());
		}
		tsModelExpr = tmp.replace(" ", ""); 
		tsModel = TimeSeriesModel.create(tsModelExpr);
		log.info("Time Series Model: [" + tsModelExpr + "]");
		connectionManager = new RedisConnectionManager(props);
		connectionManager.addListener(this);
		connectionManager.start();
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.redis.ts.controller.conn.ConnectionManagerListener#onConnectNewInstance()
	 */
	@Override
	public void onConnectNewInstance() {
		log.info("Processing New Instance Connect");
		Jedis jedis = null;
		try {
			jedis = connectionManager.getJedis();
			String storedModel = jedis.get(TSConfiguration.TS_MODEL);
			if(storedModel==null) {
				fullInit(jedis);
			} else if(tsModelExpr.equals(storedModel)) {
				refresh(jedis);
			} else {
				blowUp();
			}
		} finally {
			connectionManager.returnJedis(jedis);
		}
	}
	
	/**
	 * Initializes a fresh redis instance for redis-ts
	 * @param jedis A jedis connection
	 */
	protected void fullInit(Jedis jedis) {
		log.info("redis-ts full init");
		jedis.set(TSConfiguration.TS_MODEL, tsModelExpr);		
	}
	
	/**
	 * Called when redis-ts reconnects to a properly configured redis instance
	 * but where the run_id instance is new, so some light config is done to bring
	 * it up to date.
	 * @param jedis A jedis connection
	 */
	protected void refresh(Jedis jedis) {
		log.info("redis-ts refresh");
	}
	
	/**
	 * Called when time series model stored in redis does not match the model in redis-ts
	 */
	protected void blowUp() {
		log.error("redis-ts blow up !!");
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.redis.ts.controller.conn.ConnectionManagerListener#onConnect()
	 */
	@Override
	public void onConnect() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.redis.ts.controller.conn.ConnectionManagerListener#onDisconnect()
	 */
	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.redis.ts.controller.conn.ConnectionManagerListener#onHeartbeatFailed()
	 */
	@Override
	public void onHeartbeatFailed() {
		// TODO Auto-generated method stub
		
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
