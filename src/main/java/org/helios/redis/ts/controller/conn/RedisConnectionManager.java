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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.apache.log4j.Logger;
import org.helios.redis.ts.controller.TSConfiguration;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

/**
 * <p>Title: RedisConnectionManager</p>
 * <p>Description: Manages connections and connection retry against the redis instance 
 * and publishing events to interested listeners.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.redis.ts.controller.conn.RedisConnectionManager</code></p>
 */
public class RedisConnectionManager {
	/** Indicates if we have a good connection to redis */
	protected final AtomicBoolean connected = new AtomicBoolean(false);
	/** The jedis pool config */
	protected final Config poolConfig = new Config();
	/** The jedis pool instance */
	protected final JedisPool jedisPool;
	/** Instance logger */
	protected final Logger log;
	/** The RunID of the connected instance */
	protected String runId = null;
	/** The reconnect period in seconds */
	protected long reconnectPeriod = -1;
	/** The heartbeat period in seconds */
	protected long heartbeatPeriod = -1;
	
	/** The redis host name or IP address */
	protected final String host;
	/** The redis auth password */
	protected final String auth;
	/** The redis listening port */
	protected final int port;
	/** The redis connect timeout */
	protected final int timeout;
	
	
	/** Scheduler thread pool Thread Factory Thread Serial Number */
	protected static final AtomicInteger serial = new AtomicInteger(0);
	/** Scheduler thread pool Thread Factory Thread Group */
	protected static final ThreadGroup schedulerThreadGroup = new ThreadGroup(RedisConnectionManager.class.getSimpleName() + "-ThreadGroup");
	/** Uncaught exception handler for scheduler */
	protected static final UncaughtExceptionHandler schedulerExceptionHandler = new UncaughtExceptionHandler(){
		public void uncaughtException(Thread t, Throwable e) {
			System.err.println("Scheduler Exception on thread [" + t + "]. Stack trace follows:");
			e.printStackTrace(System.err);
		};
	};
	
	/** Scheduler thread pool Thread Factory*/
	protected static final ThreadFactory threadFactory = new ThreadFactory(){
		public Thread newThread(Runnable r) {
			Thread t = new Thread(schedulerThreadGroup, r, RedisConnectionManager.class.getSimpleName() + "Thread#" + serial.incrementAndGet());
			t.setDaemon(false);
			t.setUncaughtExceptionHandler(schedulerExceptionHandler);
			return t;
		}
	};
	/** Scheduler thread pool */
	protected final ScheduledThreadPoolExecutor scheduler; 
	/** Dedicated heartbeat processor connection */
	protected Jedis heartbeatJedis = null;
	/** Dedicated ts event connection */
	protected Jedis tsEventJedis = null;
	/** Scheduler handle for reconnect loop */
	protected ScheduledFuture<?> reconnectScheduleHandle = null;
	/** Scheduler handle for sending heartbeat events */
	protected ScheduledFuture<?> heartbeatSendHandle = null;
	/** Scheduler handle for expecting heartbeat events */
	protected ScheduledFuture<?> heartbeatExpectHandle = null;
	/** Heartbeat timeout counter */
	protected final AtomicInteger totalHeartbeatTimeouts = new AtomicInteger(0);
	/** Heartbeat consecutive timeout counter */
	protected final AtomicInteger consecutiveHeartbeatTimeouts = new AtomicInteger(0);
	/** The timestamp of the last heartbeat received */
	protected final AtomicLong lastHeartbeatTime = new AtomicLong(-1);		
	/** Registered connection event listeners */
	protected final Set<ConnectionManagerListener> listeners = new CopyOnWriteArraySet<ConnectionManagerListener>();	
	/** A map of the pool config field types keyed by the field */
	protected static final Map<String, Field> poolConfigFieldNames = new HashMap<String, Field>(Config.class.getDeclaredFields().length);
	/** heartbeat JedisPubsub */
	protected JedisPubSub heartbeatPubSub = new JedisPubSub(){
		public void onMessage(String channel, String message) {
			if(log.isDebugEnabled()) log.debug("Processing Message From Channel [" + channel + "]\n\t" + message);
			lastHeartbeatTime.set(Long.parseLong(message));
		}

		public void onPMessage(String pattern, String channel, String message) {
			if(log.isDebugEnabled()) log.debug("Processing Wildcard [" + pattern + "] Message From Channel [" + channel + "]\n\t" + message);
		}

		public void onSubscribe(String channel, int subscribedChannels) {
			if(log.isDebugEnabled()) log.debug("Subscribing to [" + channel + "] Subscriptions:" + subscribedChannels);
		}

		public void onUnsubscribe(String channel, int subscribedChannels) {
			if(log.isDebugEnabled()) log.debug("Unsubscribing from [" + channel + "] Subscriptions:" + subscribedChannels);
		}

		public void onPUnsubscribe(String pattern, int subscribedChannels) {
			if(log.isDebugEnabled()) log.debug("P-Unsubscribing from [" + pattern + "] Subscriptions:" + subscribedChannels);
		}

		public void onPSubscribe(String pattern, int subscribedChannels) {
			if(log.isDebugEnabled()) log.debug("P-Subscribing to [" + pattern + "] Subscriptions:" + subscribedChannels);
		}
		
	}; 
	
	
	static {
		try {
			for(Field f: Config.class.getDeclaredFields()) {
				f.setAccessible(true);
				poolConfigFieldNames.put(f.getName(), f);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize Pool Config Fields", e);
		}
	}
	
	/**
	 * Creates a new RedisConnectionManager
	 * @param configProps The redis-ts.config specified properties
	 */
	public RedisConnectionManager(Properties configProps) {
		reconnectPeriod = Integer.parseInt(configProps.getProperty("redis.reconnect.period", "5"));
		heartbeatPeriod = Integer.parseInt(configProps.getProperty("redis.heartbeat.period", "3"));
		int poolSize = Integer.parseInt(configProps.getProperty("redis.scheduler.pool.size", "5"));
		scheduler = new ScheduledThreadPoolExecutor(poolSize, threadFactory);
		scheduler.prestartCoreThread();
		initPoolConfig(configProps);
		host = configProps.getProperty("redis.connect.host");
		auth = configProps.getProperty("redis.connect.auth");
		port = Integer.parseInt(configProps.getProperty("redis.connect.port", "6379"));
		timeout = Integer.parseInt(configProps.getProperty("redis.connect.timeout", "2000"));
		log = Logger.getLogger(getClass().getName() + "-" + host + ":" + port);
		if(auth==null || auth.trim().isEmpty()) {
			jedisPool = new JedisPool(poolConfig, host, port, timeout);
		} else {
			jedisPool = new JedisPool(poolConfig, host, port, timeout, auth);
		}
	}
	
	/**
	 * Starts the connection manager
	 */
	public void start() {
		try {
			runId = getInfo("run_id");
			onSuccessfulConnect();
			connected.set(true);
			log.info("Connection Manager Initialized");
			log.info("Redis Run ID:" + runId);				
			fireConnectNewInstance();
		} catch (Exception e) {
			log.warn("Failed initial connection. Initiating reconnect loop");
		}		
	}
	
	protected void onSuccessfulConnect() {
		heartbeatJedis = jedisPool.getResource();	
		log.info("HBJedis Connected:" + heartbeatJedis.isConnected());
		heartbeatSendHandle = scheduler.scheduleAtFixedRate(new Runnable(){			
			public void run() {
				Jedis jedis = null;
				try {
					jedis = jedisPool.getResource();
					jedis.publish(TSConfiguration.PS_HEARTBEAT, "" + System.currentTimeMillis());
				} catch (Exception e) {
					log.error("Failed to publish heartbeat", e);
				} finally {
					jedisPool.returnResource(jedis);
				}
			}
		}, 0, TimeUnit.MILLISECONDS.convert(heartbeatPeriod, TimeUnit.SECONDS)/2, TimeUnit.MILLISECONDS);
		
			
		new Thread() {
			public void run() {
				heartbeatJedis.subscribe(heartbeatPubSub, TSConfiguration.PS_HEARTBEAT);
			}
		}.start();
		
		scheduler.scheduleAtFixedRate(new Runnable(){
			protected final long MAX_HB_ELAPSED = TimeUnit.MILLISECONDS.convert(heartbeatPeriod, TimeUnit.SECONDS);
			public void run() {
				long now = System.currentTimeMillis();
				long lastHb = lastHeartbeatTime.get();
				long diff = now-lastHb;
				if(log.isDebugEnabled()) log.debug("\n\t****\n\tHB Info:" +
						"\n\tNow:" + now +
						"\n\tLast HB:" + lastHb +
						"\n\tDiff:" + diff +
						"\n\tMax Diff:" + MAX_HB_ELAPSED +
						"\n\t****\n");
						
				if(System.currentTimeMillis()-lastHeartbeatTime.get()  > MAX_HB_ELAPSED) {
					processHeartbeatTimeout();
				} else {
					consecutiveHeartbeatTimeouts.set(0);
				}
			}
		}, heartbeatPeriod, heartbeatPeriod, TimeUnit.SECONDS);		
	}
	/**
	 * Closes the connection manager and deallocates all associated resources
	 */
	public void stop() {
		
	}
	
	/**
	 * Returns true if the connection manager is connected, false otherwise
	 * @return true if the connection manager is connected, false otherwise
	 */
	public boolean isConnected() {
		return connected.get();
	}
	
	/**
	 * Called when a heartbeat timeout occurs
	 */
	protected void processHeartbeatTimeout() {
		consecutiveHeartbeatTimeouts.incrementAndGet();
		totalHeartbeatTimeouts.incrementAndGet();
		log.warn("\n\t=======================\n\tHeartbeat Failure\n\tConsecutive:" + 
				consecutiveHeartbeatTimeouts.get() + 
				"\n\tTotal:" + totalHeartbeatTimeouts.get() + 
				"\n\t=======================\n");		
		fireHeartbeatFailed();
	}
	
	/**
	 * Returns a jedis instance from the pool
	 * @return a jedis instance 
	 */
	public Jedis getJedis() {
		return jedisPool.getResource();
	}
	
	/**
	 * Returns a jedis instance to the pool
	 * @param jedis the instance to return to the pool
	 */
	public void returnJedis(Jedis jedis) {
		jedisPool.returnResource(jedis);
	}
	
	/**
	 * Registers a new connection manager event listener
	 * @param listener the new connection manager event listener to register
	 */
	public void addListener(ConnectionManagerListener listener) {
		if(listener!=null) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Unregisters a connection manager event listener
	 * @param listener the new connection manager event listener to unregister
	 */
	public void removeListener(ConnectionManagerListener listener) {
		if(listener!=null) {
			listeners.remove(listener);
		}
	}
	
	
	/**
	 * Returns a map of entries from redis INFO
	 * @return a map of entries from redis INFO
	 */
	public Map<String, String> getInfo() {
		Map<String, String> infoMap = new HashMap<String, String>(52);
		Jedis jedis = null;
		String infoString = null; 
		try {
			jedis = jedisPool.getResource();
			infoString = jedis.info();
		} finally {
			jedisPool.returnResource(jedis);
		}
		if(infoString==null) {
			throw new RuntimeException("Failed to get INFO from redis", new Throwable());
		}
		for(String info: infoString.split("\r\n")) {
			info = info.replace(" ", "");
			if(!info.isEmpty() && !info.startsWith("#")) {
				String[] frags = info.split(":");
				infoMap.put(frags[0], frags[1]);
			}
		}
		return infoMap;
	}
	
	/**
	 * Returns the value for the specified key from the redis INFO
	 * @param key The key of the INFO field requested
	 * @return The value of the key or nulll if it was not found 
	 */
	public String getInfo(String key) {
		return getInfo().get(key);
	}
	
	
	/**
	 * Initializes the pool config from the pool properties
	 * @param configProps the pool properties
	 */
	protected void initPoolConfig(Properties configProps) {
		String fName = null;
		try {
			for(String s: configProps.stringPropertyNames()) {
				if(s.startsWith("redis.pool.")) {
					fName = s.replace("redis.pool.", "");
					Field f = poolConfigFieldNames.get(fName);
					if(f!=null) {
						String value = configProps.getProperty(s);
						if(value==null || value.trim().isEmpty()) continue;
						value = value.trim();
						PropertyEditor pe = PropertyEditorManager.findEditor(f.getType());
						pe.setAsText(value);
						f.set(poolConfig, pe.getValue());
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to configure Jedis Pool Item [" + fName + "]", e);
		}		
	}
	
	
	/**
	 * Fired when the connection manager connects 
	 * or reconnects to an instance with different run_id from the prior connection. 
	 */
	protected void fireConnectNewInstance() {
		for(ConnectionManagerListener listener: listeners) {
			listener.onConnectNewInstance();
		}
	}
	
	/**
	 * Fired when the connection manager connects to the 
	 * same run_id as was connected to in the prior connection 
	 */	
	protected void fireConnect() {
		for(ConnectionManagerListener listener: listeners) {
			listener.onConnect();
		}
	}
	
	/**
	 * Fired when connectivity is lost
	 */
	protected void fireDisconnect() {
		for(ConnectionManagerListener listener: listeners) {
			listener.onDisconnect();
		}
	}
	
	/**
	 * Fired when a heatbeat is not received in a timely manner. 
	 * Does not, by itself, indicate a disconnect.
	 */	
	protected void fireHeartbeatFailed() {
		for(ConnectionManagerListener listener: listeners) {
			listener.onHeartbeatFailed();
		}
	}
	
	
	
}
