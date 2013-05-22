package org.quickcached.mem;

import javax.management.*;
import java.lang.management.*;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * Based on http://www.javaspecialists.eu/archive/Issue092.html
 */
public class MemoryWarningSystem {
	private static final Logger logger = Logger.getLogger(MemoryWarningSystem.class.getName());
	
	private final Collection<Listener> listeners =	new ArrayList<Listener>();

	public interface Listener {
		public void memoryUsageHigh(long usedMemory, long maxMemory);
	}
	
	public static int getMemUsedPercentage() {
		long usedMemory = Runtime.getRuntime().totalMemory() - 
				Runtime.getRuntime().freeMemory();			        

		long heapMaxSize = Runtime.getRuntime().maxMemory();
		
		int memPercentUsed = (int) (100.0*usedMemory/heapMaxSize);
		return memPercentUsed;
	}

	public MemoryWarningSystem() {
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		NotificationEmitter emitter = (NotificationEmitter) mbean;
		emitter.addNotificationListener(new NotificationListener() {
			public void handleNotification(Notification n, Object hb) {
				if(n.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
					long maxMemory = tenuredGenPool.getUsage().getMax();
					long usedMemory = tenuredGenPool.getUsage().getUsed();
					for(Listener listener : listeners) {
						listener.memoryUsageHigh(usedMemory, maxMemory);
					}
				}
			}
		}, null, null);
	}

	public boolean addListener(Listener listener) {
		return listeners.add(listener);
	}

	public void removeAllListener() {
		listeners.clear();
	}

	public boolean removeListener(Listener listener) {
		return listeners.remove(listener);
	}

	private static final MemoryPoolMXBean tenuredGenPool = findTenuredGenPool();

	public static void setPercentageUsageThreshold(double percentage) {
		if (percentage <= 0.0 || percentage > 1.0) {
			throw new IllegalArgumentException("Percentage not in range "+percentage);
		}
		long maxMemory = tenuredGenPool.getUsage().getMax();
		long warningThreshold = (long) (maxMemory * percentage);
		tenuredGenPool.setUsageThreshold(warningThreshold);
	}

	/**
	 * Tenured Space Pool can be determined by it being of type
	 * HEAP and by it being possible to set the usage threshold.
	 */
	private static MemoryPoolMXBean findTenuredGenPool() {
		for (MemoryPoolMXBean pool :
				ManagementFactory.getMemoryPoolMXBeans()) {
			// I don't know whether this approach is better, or whether
			// we should rather check for the pool name "Tenured Gen"?
			if(pool.getType() == MemoryType.HEAP
					&& pool.isUsageThresholdSupported()) {
				return pool;
			}
		}
		throw new AssertionError("Could not find tenured space");
	}
}
