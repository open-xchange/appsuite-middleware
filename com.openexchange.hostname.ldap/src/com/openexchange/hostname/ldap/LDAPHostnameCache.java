package com.openexchange.hostname.ldap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.cache.OXCachingException;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.CacheStatistics;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.StatisticElement;
import com.openexchange.caching.Statistics;
import com.openexchange.hostname.ldap.services.HostnameLDAPServiceRegistry;
import com.openexchange.java.Autoboxing;

public class LDAPHostnameCache {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(LDAPHostnameCache.class);
    
    public final static String REGION_NAME = "LDAPHostname";
    
    private static final Object[] EMPTY_ARGS = new Object[0];
    
    private static final ConcurrentMap<Object, ReadWriteLock> contextLocks = new ConcurrentHashMap<Object, ReadWriteLock>();
    
    private static volatile LDAPHostnameCache singleton;
    
    private Cache cache;
    
    /**
     * Gets the singleton instance.
     * 
     * @return The singleton instance
     * @throws OXCachingException If instance initialization failed
     */
    public static LDAPHostnameCache getInstance() throws OXCachingException {
        LDAPHostnameCache tmp = singleton;
        if (null == tmp) {
            synchronized (LDAPHostnameCache.class) {
                tmp = singleton;
                if (null == tmp) {
                    tmp = singleton = new LDAPHostnameCache();
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the singleton instance.
     */
    public static void releaseInstance() {
        if (null != singleton) {
            synchronized (LDAPHostnameCache.class) {
                if (null != singleton) {
                    singleton = null;
                }
            }
        }
    }

    /**
     * Fetches the appropriate lock.
     * 
     * @param key The lock's key
     * @return The appropriate lock
     */
    private static ReadWriteLock getLock(final Object key) {
        ReadWriteLock l = contextLocks.get(key);
        if (l == null) {
            final ReentrantReadWriteLock tmp = new ReentrantReadWriteLock();
            l = contextLocks.putIfAbsent(key, tmp);
            if (null == l) {
                l = tmp;
            }
        }
        return l;
    }

    
    /**
     * Singleton instantiation.
     * 
     * @throws OXCachingException If cache instantiation fails
     */
    private LDAPHostnameCache() throws OXCachingException {
        super();
        initCache();
    }

    
    
    /**
     * Initializes cache reference.
     * 
     * @throws OXCachingException If initializing the cache reference fails
     */
    public void initCache() throws OXCachingException {
        /*
         * Check for proper started mail cache configuration
         */
        if (cache != null) {
            return;
        }
        try {
            cache = HostnameLDAPServiceRegistry.getServiceRegistry().getService(CacheService.class).getCache(REGION_NAME);
        } catch (final CacheException e) {
            LOG.error(e.getMessage(), e);
            throw new OXCachingException(OXCachingException.Code.FAILED_INIT, e, REGION_NAME, e.getMessage());
        }
    }
    
    public void addHostnameToCache(final int cid, final String hostname) throws OXCachingException {
        if (null == cache) {
            return;
        } else if ((hostname == null) || (hostname.length() == 0)) {
            return;
        }
        try {
            final Integer cid_int = Autoboxing.I(cid);
            final Lock writeLock = getLock(cid_int).writeLock();
            writeLock.lock();
            try {
                cache.put(cid_int, hostname);
            } finally {
                writeLock.unlock();
            }
        } catch (final CacheException e) {
            throw new OXCachingException(OXCachingException.Code.FAILED_PUT, e, EMPTY_ARGS);
        }
        
    }

    public String getHostnameFromCache(final int cid) throws OXCachingException {
        if (null == cache) {
            return null;
        }
        final Integer cid_int = Autoboxing.I(cid);
        final Lock readLock = getLock(cid_int).readLock();
        readLock.lock();
        try {
            return (String) cache.get(cid_int);
        } finally {
            readLock.unlock();
        }
        
    }
    
    public void outputSettings() {
        try {
            final ElementAttributes defaultElementAttributes = cache.getDefaultElementAttributes();
            System.out.println("Create time " + defaultElementAttributes.getCreateTime());
            System.out.println("Idle time " + defaultElementAttributes.getIdleTime());
            System.out.println("Last access time " + defaultElementAttributes.getLastAccessTime());
            System.out.println("Max life time " + defaultElementAttributes.getMaxLifeSeconds());
        } catch (CacheException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        final CacheStatistics statistics = cache.getStatistics();
        final StatisticElement[] statElements = statistics.getStatElements();
        for (final StatisticElement elem : statElements) {
            System.out.println(elem.getName() + " : " + elem.getData());
        }
        System.out.println("----------------------");
        final Statistics[] auxiliaryCacheStats = statistics.getAuxiliaryCacheStats();
        for (final Statistics stat : auxiliaryCacheStats) {
            System.out.println(stat.getTypeName());
            final StatisticElement[] statElements2 = stat.getStatElements();
            for (final StatisticElement elem : statElements2) {
                System.out.println(elem.getName() + " : " + elem.getData());
            }
        }
    }
    


}
