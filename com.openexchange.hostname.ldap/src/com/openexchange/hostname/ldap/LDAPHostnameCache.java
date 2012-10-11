package com.openexchange.hostname.ldap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.exception.OXException;
import com.openexchange.hostname.ldap.services.HostnameLDAPServiceRegistry;
import com.openexchange.java.Autoboxing;

public class LDAPHostnameCache {

    public final static String REGION_NAME = "LDAPHostname";

    private static final ConcurrentMap<Object, ReadWriteLock> contextLocks = new ConcurrentHashMap<Object, ReadWriteLock>();

    private static final Object[] EMPTY_ARGS = new Object[0];

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(LDAPHostnameCache.class));

    private static volatile LDAPHostnameCache singleton;

    private Cache cache;

    /**
     * Singleton instantiation.
     *
     * @throws OXException If cache instantiation fails
     */
    private LDAPHostnameCache() throws OXException {
        super();
        initCache();
    }

    /**
     * Gets the singleton instance.
     *
     * @return The singleton instance
     * @throws OXException If instance initialization failed
     */
    public static LDAPHostnameCache getInstance() throws OXException {
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



    public void addHostnameToCache(final int cid, final String hostname) throws OXException {
        if (null == cache) {
            return;
        } else if ((hostname == null) || (hostname.length() == 0)) {
            return;
        }
        {
            final Integer cid_int = Autoboxing.I(cid);
            final Lock writeLock = getLock(cid_int).writeLock();
            writeLock.lock();
            try {
                if (null != cache.get(cid_int)) {
                    cache.remove(cid_int);
                }
                cache.put(cid_int, hostname);
            } finally {
                writeLock.unlock();
            }
        }

    }

    public String getHostnameFromCache(final int cid) {
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

    public void outputSettings() throws OXException {
        final ElementAttributes defaultElementAttributes = cache.getDefaultElementAttributes();
        final StringBuilder sb = new StringBuilder(128).append('\n');
        sb.append("Cache setting for hostname ldap bundle:\n");
        sb.append("\tCreate time: ");
        sb.append(defaultElementAttributes.getCreateTime());
        sb.append('\n');
        sb.append("\tIdle time: ");
        sb.append(defaultElementAttributes.getIdleTime());
        sb.append('\n');
        sb.append("\tMax life: ");
        sb.append(defaultElementAttributes.getMaxLifeSeconds());
        sb.append('\n');
        sb.append("\tTime To Live time: ");
        sb.append(defaultElementAttributes.getTimeToLiveSeconds());
        sb.append('\n');
        sb.append("\tSize: ");
        sb.append(defaultElementAttributes.getSize());
        sb.append('\n');
        sb.append("\tIsEternal: ");
        sb.append(defaultElementAttributes.getIsEternal());
        sb.append('\n');
        sb.append("\tIsLateral: ");
        sb.append(defaultElementAttributes.getIsLateral());
        sb.append('\n');
        sb.append("\tIsRemote: ");
        sb.append(defaultElementAttributes.getIsRemote());
        sb.append("\tIsSpool: ");
        sb.append(defaultElementAttributes.getIsSpool());
        sb.append('\n');
        LOG.info(sb.toString());
    }

    /**
     * Initializes cache reference.
     *
     * @throws OXException If initializing the cache reference fails
     */
    private void initCache() throws OXException {
        /*
         * Check for proper started hostname cache configuration
         */
        if (cache != null) {
            return;
        }
        cache = HostnameLDAPServiceRegistry.getServiceRegistry().getService(CacheService.class).getCache(REGION_NAME);
    }

}
