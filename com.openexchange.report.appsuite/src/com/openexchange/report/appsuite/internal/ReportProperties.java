
package com.openexchange.report.appsuite.internal;

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

    public ReportProperties() {
        super();
    }

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
        Integer propertyValue = null;
        ConfigurationService service = Services.getService(ConfigurationService.class);
        propertyValue = service.getIntProperty(key, defaultValue);
        return propertyValue;
    }

    private static String loadStringValue(String key, String defaultValue) {
        String propertyValue = null;
        ConfigurationService service = Services.getService(ConfigurationService.class);
        propertyValue = service.getProperty(key, defaultValue);
        return propertyValue;
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
        return Reloadables.interestsForProperties(STORAGE_PATH, MAX_CHUNK_SIZE,MAX_THREAD_POOL_SIZE,THREAD_PRIORITY);
    }

}
