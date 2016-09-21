
package com.openexchange.report.appsuite.internal;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;

public class ReportProperties implements Reloadable {

    private static String STORAGE_PATH = "com.openexchange.report.serialization.fileStorage";
    private static String STORAGE_PATH_DEFAULT = "/tmp";

    private static String MAX_CHUNK_SIZE = "com.openexchange.report.serialization.maxChunkSize";
    private static int MAX_CHUNK_SIZE_DEFAULT = 200;

    private static String MAX_THREAD_POOL_SIZE = "com.openexchange.report.serialization.maxThreadPoolSize";
    private static int MAX_THREAD_POOL_SIZE_DEFAULT = 20;

    private static String THREAD_PRIORITY = "com.openexchange.report.serialization.threadPriority";
    private static int THREAD_PRIORITY_DEFAULT = 1;

    private static String storagePath;
    private static Integer maxChunkSize;
    private static Integer maxThreadPoolSize;
    private static Integer threadPriority;

    public ReportProperties() {
        super();
    }

    public static String getStoragePath() {
        if (storagePath == null) {
            storagePath = loadStringValue(STORAGE_PATH, STORAGE_PATH_DEFAULT);
        }
        return storagePath;
    }

    public static int getMaxChunkSize() {
        if (maxChunkSize == null) {
            maxChunkSize = loadIntegerValue(MAX_CHUNK_SIZE, MAX_CHUNK_SIZE_DEFAULT);
        }
        return maxChunkSize;
    }

    public static int getMaxThreadPoolSize() {
        if (maxThreadPoolSize == null) {
            maxThreadPoolSize = loadIntegerValue(MAX_THREAD_POOL_SIZE, MAX_THREAD_POOL_SIZE_DEFAULT);
        }
        return maxThreadPoolSize;
    }

    public static int getThreadPriority() {
        if (threadPriority == null) {
            threadPriority = loadIntegerValue(THREAD_PRIORITY, THREAD_PRIORITY_DEFAULT);
        }
        return threadPriority;
    }

    private static Integer loadIntegerValue(String key, int defaultValue) {
        Integer propertyValue = null;
        synchronized (ReportProperties.class) {
            ConfigurationService service = Services.getService(ConfigurationService.class);
            propertyValue = service.getIntProperty(key, defaultValue);
        }
        return propertyValue;
    }

    private static String loadStringValue(String key, String defaultValue) {
        String propertyValue = null;
        synchronized (ReportProperties.class) {
            ConfigurationService service = Services.getService(ConfigurationService.class);
            propertyValue = service.getProperty(key, defaultValue);
        }
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
