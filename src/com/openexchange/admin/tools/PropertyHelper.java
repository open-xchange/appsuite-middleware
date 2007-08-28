package com.openexchange.admin.tools;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchPluginException;

public class PropertyHelper {
    private static PropertyHandler prop = ClientAdminThread.cache.getProperties();
    
    public static final String PLUGIN_NAME = "hosting";

    // The following lines define the property values for the database implementations
    public static final String CONTEXT_STORAGE = "CONTEXT_STORAGE";
    public static final String UTIL_STORAGE = "UTIL_STORAGE";
    public static final String JMX_PORT = "JMX_PORT";
    public static final String JMX_BIND_ADDRESS = "JMX_BIND_ADDRESS";
    public static final String CHECK_CONTEXT_LOGIN_MAPPING_REGEXP = "CHECK_CONTEXT_LOGIN_MAPPING_REGEXP";
    public static final String AVERAGE_CONTEXT_SIZE = "AVERAGE_CONTEXT_SIZE";
    public static final String CONTEXTS_PER_SCHEMA = "CONTEXTS_PER_SCHEMA";
    public static final String CREATE_CONTEXT_USE_UNIT = "CREATE_CONTEXT_USE_UNIT";
    
    public static String getString(final String key) throws NoSuchPluginException, InvalidDataException {
        return prop.getString(PLUGIN_NAME, key);
    }

    public static String getString(final String key, final String defaultValue) throws NoSuchPluginException, InvalidDataException {
        return prop.getString(PLUGIN_NAME, key, defaultValue);
    }
    
    public static int getInt(final String key) throws NoSuchPluginException, InvalidDataException {
        return prop.getInt(PLUGIN_NAME, key);
    }

    public static int getInt(final String key, final int defaultValue) throws NoSuchPluginException, InvalidDataException {
        return prop.getInt(PLUGIN_NAME, key, defaultValue);
    }

    public static long getLong(final String key) throws NoSuchPluginException, InvalidDataException {
        return prop.getLong(PLUGIN_NAME, key);
    }
    
    public static void initializeDefaults() {
        // Longs
        prop.checkAndSetDefault(PLUGIN_NAME, PropertyHelper.AVERAGE_CONTEXT_SIZE, Long.valueOf(100));
        
        // Strings
        prop.checkAndSetDefault(PLUGIN_NAME, PropertyHelper.CREATE_CONTEXT_USE_UNIT, "context");
        prop.checkAndSetDefault(PLUGIN_NAME, PropertyHelper.JMX_BIND_ADDRESS, "localhost");
        prop.checkAndSetDefault(PLUGIN_NAME, PropertyHelper.CHECK_CONTEXT_LOGIN_MAPPING_REGEXP, "[$%\\.+a-zA-Z0-9_-]");
        
        // Ints
        prop.checkAndSetDefault(PLUGIN_NAME, PropertyHelper.CONTEXTS_PER_SCHEMA, Integer.valueOf(1));
        prop.checkAndSetDefault(PLUGIN_NAME, PropertyHelper.JMX_PORT, Integer.valueOf(9998));
    }
}
