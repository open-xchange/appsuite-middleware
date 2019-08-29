
package com.openexchange.report.appsuite.internal;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;

public class ReportProperties implements Reloadable {

    private static String STORAGE_PATH = "com.openexchange.report.appsuite.fileStorage";
    private static String STORAGE_PATH_DEFAULT = "/tmp";

    private static String MAX_CHUNK_SIZE = "com.openexchange.report.appsuite.maxChunkSize";
    private static int MAX_CHUNK_SIZE_DEFAULT = 200;

    private static String MAX_THREAD_POOL_SIZE = "com.openexchange.report.appsuite.maxThreadPoolSize";
    private static int MAX_THREAD_POOL_SIZE_DEFAULT = 20;

    private static String THREAD_PRIORITY = "com.openexchange.report.appsuite.threadPriority";
    private static int THREAD_PRIORITY_DEFAULT = 1;

    private static volatile String storagePath;
    private static volatile Integer maxChunkSize;
    private static volatile Integer maxThreadPoolSize;
    private static volatile Integer threadPriority;

    /**
     * Initializes a new {@link ReportProperties}.
     */
    public ReportProperties() {
        super();
    }

    /**
     * Gets the value of the <code>"com.openexchange.report.appsuite.fileStorage"</code> property.
     *
     * @return The storage path
     */
    public static String getStoragePath() {
        String sp = storagePath;
        if (sp == null) {
            synchronized (ReportProperties.class) {
                sp = storagePath;
                if (sp == null) {
                    sp = loadStringValue(STORAGE_PATH, STORAGE_PATH_DEFAULT);
                    storagePath = sp;
                }
            }
        }
        return sp;
    }

    /**
     * Gets the value of the <code>"com.openexchange.report.appsuite.maxChunkSize"</code> property.
     *
     * @return The max. chunk size
     */
    public static int getMaxChunkSize() {
        Integer mcs = maxChunkSize;
        if (mcs == null) {
            synchronized (ReportProperties.class) {
                mcs = maxChunkSize;
                if (mcs == null) {
                    mcs = loadIntegerValue(MAX_CHUNK_SIZE, MAX_CHUNK_SIZE_DEFAULT);
                    maxChunkSize = mcs;
                }
            }
        }
        return mcs.intValue();
    }

    /**
     * Gets the value of the <code>"com.openexchange.report.appsuite.maxThreadPoolSize"</code> property.
     *
     * @return The max. thread pool size
     */
    public static int getMaxThreadPoolSize() {
        Integer mtpz = maxThreadPoolSize;
        if (mtpz == null) {
            synchronized (ReportProperties.class) {
                mtpz = maxThreadPoolSize;
                if (mtpz == null) {
                    mtpz = loadIntegerValue(MAX_THREAD_POOL_SIZE, MAX_THREAD_POOL_SIZE_DEFAULT);
                    maxThreadPoolSize = mtpz;
                }
            }
        }
        return mtpz.intValue();
    }

    /**
     * Gets the value of the <code>"com.openexchange.report.appsuite.threadPriority"</code> property.
     *
     * @return The thread priority
     */
    public static int getThreadPriority() {
        Integer tp = threadPriority;
        if (tp == null) {
            synchronized (ReportProperties.class) {
                tp = threadPriority;
                if (tp == null) {
                    tp = loadIntegerValue(THREAD_PRIORITY, THREAD_PRIORITY_DEFAULT);
                    threadPriority = tp;
                }
            }
        }
        return tp.intValue();
    }

    private static Integer loadIntegerValue(String key, int defaultValue) {
        ConfigurationService service = Services.getService(ConfigurationService.class);
        return I(service.getIntProperty(key, defaultValue));
    }

    private static String loadStringValue(String key, String defaultValue) {
        ConfigurationService service = Services.getService(ConfigurationService.class);
        return service.getProperty(key, defaultValue);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        storagePath = null;
        maxChunkSize = null;
        maxThreadPoolSize = null;
        threadPriority = null;
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(STORAGE_PATH, MAX_CHUNK_SIZE, MAX_THREAD_POOL_SIZE, THREAD_PRIORITY);
    }

}
