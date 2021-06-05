/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Properties;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.config.ConfigurationService;

/**
 * {@link PropertyHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class PropertyHandler {

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PropertyHandler.class);

    private static final String RMI_PROPERTIES = "RMI.properties";
    private static final String ADMIN_USER_PROPERTIES = "AdminUser.properties";
    private static final String GROUP_PROPERTIES = "Group.properties";
    private static final String RESOURCE_PROPERTIES = "Resource.properties";

    protected Hashtable<String, Object> allPropValues = null;
    private final Hashtable<String, String> userPropValues = null;
    protected Hashtable<String, String> groupPropValues = null;
    private final Hashtable<String, String> resPropValues = null;
    private final Hashtable<String, String> rmiPropValues = null;
    protected Hashtable<String, String> sqlPropValues = null;

    private String configDirName;
    private final Properties sysprops;

    protected final static String PROPERTIES_SQL = "SQL_PROP_CONFIG";

    // The following lines define the property values for the database implementations
    public static final String GROUP_STORAGE = "GROUP_STORAGE";
    public static final String RESOURCE_STORAGE = "RESOURCE_STORAGE";
    public static final String USER_STORAGE = "USER_STORAGE";

    /**
     * Initializes a new {@link PropertyHandler}.
     *
     * @param sysprops the system properties
     */
    public PropertyHandler(Properties sysprops) {
        this.allPropValues = new Hashtable<>();
        this.sysprops = sysprops;
        try {
            loadProps(sysprops);
        } catch (FileNotFoundException e) {
            LOGGER.error("", e);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    /**
     * Get String value from Properties-File. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public String getProp(String key, String fallBack) {
        String retString = fallBack;
        if (allPropValues.containsKey(key)) {
            return allPropValues.get(key).toString();
        }
        LOGGER.error("Property '{}' not found in file {}! Using fallback :{}", key, this.configDirName, fallBack);
        return retString;
    }

    /**
     * Get String value from System properties. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public String getSysProp(String key, String fallBack) {
        String retString = fallBack;
        retString = this.sysprops.getProperty(key);
        if (retString == null) {
            LOGGER.debug("Property '{}' not found in the run script! Using fallback :{}", key, fallBack);
            return fallBack;
        }
        return retString;
    }

    /**
     * Get String value from the {@value #GROUP_PROPERTIES} file. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public String getGroupProp(String key, String fallBack) {
        return getProperty(groupPropValues, GROUP_PROPERTIES, key, fallBack);
    }

    /**
     * Get boolean value from the {@value #GROUP_PROPERTIES} file. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public boolean getGroupProp(String key, boolean fallBack) {
        return getBooleanProperty(groupPropValues, GROUP_PROPERTIES, key, fallBack);
    }

    /**
     * Get boolean value from the {@value #ADMIN_USER_PROPERTIES} file. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public boolean getUserProp(final String key, final boolean fallBack) {
        return getBooleanProperty(userPropValues, ADMIN_USER_PROPERTIES, key, fallBack);
    }

    /**
     * Get String value from the {@value #ADMIN_USER_PROPERTIES} file. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public String getUserProp(String key, String fallBack) {
        return getProperty(userPropValues, ADMIN_USER_PROPERTIES, key, fallBack);
    }

    /**
     * Get boolean value from the {@value #RESOURCE_PROPERTIES} file. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public boolean getResourceProp(String key, boolean fallBack) {
        return getBooleanProperty(resPropValues, RESOURCE_PROPERTIES, key, fallBack);
    }

    /**
     * Get String value from the {@value #ADMIN_USER_PROPERTIES} file. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public String getResourceProp(final String key, final String fallback) {
        return getProperty(resPropValues, RESOURCE_PROPERTIES, key, fallback);
    }

    /**
     * Get integer value from the {@value #RMI_PROPERTIES} file. If not set or not found, use given fallback.
     *
     * @param key The property key
     * @param fallBack The fall-back value
     * @return The property value
     */
    public int getRmiProp(final String key, final int fallBack) {
        return getIntProperty(rmiPropValues, RMI_PROPERTIES, key, fallBack);
    }

    /**
     * Adds all properties from the specified file to the local cache
     *
     * @param file The file name
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException if an I/O error is occurred
     */
    protected void addpropsfromfile(String file) throws FileNotFoundException, IOException {
        Properties configProps = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            configProps.load(in);
        }

        final Enumeration<?> enumeration = configProps.propertyNames();
        while (enumeration.hasMoreElements()) {
            final String param = String.class.cast(enumeration.nextElement());
            String value = configProps.getProperty(param);

            if (value.startsWith("$PWD")) {
                // FIXME: Set a parsed value here instead of working dir
                // A new File without any content point to the current working dir
                value = stringReplacer(value, "$PWD", new File("").getAbsolutePath());
            }

            allPropValues.put(param, value);

            if (param.toLowerCase().endsWith("_prop")) {
                Properties customprops = new Properties();
                try (FileInputStream in = new FileInputStream(value)) {
                    customprops.load(in);
                }
                Enumeration<?> enuma = customprops.propertyNames();
                Hashtable<String, String> custconfig = new Hashtable<>();
                if (allPropValues.containsKey(param + "_CONFIG")) {
                    @SuppressWarnings("unchecked") Hashtable<String, String> ht = (Hashtable<String, String>) allPropValues.get(param + "_CONFIG");
                    custconfig = ht;
                }
                while (enuma.hasMoreElements()) {
                    String param_ = (String) enuma.nextElement();
                    String value_ = customprops.getProperty(param_);
                    if (value_.startsWith("$PWD")) {
                        value_ = stringReplacer(value_, "$PWD", new File("").getAbsolutePath());
                    }
                    if (value_.startsWith("\"")) {
                        value_ = value_.substring(1);
                    }
                    if (value_.endsWith("\"")) {
                        value_ = value_.substring(0, value_.length() - 1);

                    }
                    custconfig.put(param_, value_);
                }
                allPropValues.put(param + "_CONFIG", custconfig);
            }
        }
    }

    ///////////////////////////////// HELPERS ////////////////////////////

    /**
     * Loads all properties from the configured properties directory
     *
     * @param sysprops The system properties
     * @throws FileNotFoundException if no properties files are found
     * @throws IOException if an I/O error is occurred
     */
    private void loadProps(final Properties sysprops) throws FileNotFoundException, IOException {
        allPropValues.put(AdminProperties.Prop.ADMINDAEMON_LOGLEVEL, "ALL");

        if (sysprops.getProperty("openexchange.propdir") != null) {
            configDirName = sysprops.getProperty("openexchange.propdir");
            addpropsfromfile(this.configDirName + File.separatorChar + "AdminDaemon.properties");
        } else {
            LOGGER.error("Parameter '-Dopenexchange.propdir' not given in system properties!");
            LOGGER.error("Now, using default parameter!");
        }
    }

    /**
     * Replaces the specified source string with the specified replacement if a match is found
     *
     * @param source The source string
     * @param find The search string
     * @param replacement The replacement
     * @return The new string with the replacement
     */
    private String stringReplacer(String source, String find, String replacement) {
        int i = 0;
        int j;
        final int k = find.length();
        final int m = replacement.length();

        String src = source;
        while (i < src.length()) {
            j = src.indexOf(find, i);

            if (j == -1) {
                break;
            }

            if (j == 0) {
                src = replacement + src.substring(j + k);
            } else if (j + k == src.length()) {
                src = src.substring(0, j) + replacement;
            } else {
                src = src.substring(0, j) + replacement + src.substring(j + k);
            }
            i = j + m;
        }

        return src;
    }

    /**
     * Retrieves the specified boolean property from the specified properties' file
     * If the property is absent or cannot be retrieved,
     * the supplied fallback is returned
     *
     * @param propertiesFile The properties file
     * @param key The property key
     * @param fallback The fallback value
     * @return The property's value or the fallback
     */
    private boolean getBooleanProperty(Hashtable<String, String> cachedValues, String propertiesFile, String key, boolean fallback) {
        return Boolean.parseBoolean(getProperty(cachedValues, propertiesFile, key, Boolean.toString(fallback)));
    }

    /**
     * Retrieves the specified integer property from the specified properties' file
     * If the property is absent or cannot be retrieved,
     * the supplied fallback is returned
     *
     * @param cachedValues The cached values
     * @param propertiesFile The properties file
     * @param key The property key
     * @param fallback The fallback value
     *
     * @return The property's value or the fallback
     */
    private int getIntProperty(Hashtable<String, String> cachedValues, String propertiesFile, String key, int fallback) {
        return Integer.parseInt(getProperty(cachedValues, propertiesFile, key, Integer.toString(fallback)));
    }

    /**
     * Retrieves the specified property from the specified properties' file
     * If the property is absent or cannot be retrieved,
     * the supplied fallback is returned
     *
     * @param cachedValues The cached values, if <code>null</code> will be initialised and all properties will be cached in that {@link Hashtable}
     * @param propertiesFile The properties file
     * @param key The property key
     * @param fallback The fallback value
     *
     * @return The property's value or the fallback
     */
    private String getProperty(Hashtable<String, String> cachedValues, String propertiesFile, String key, String fallback) {
        String retval = fallback;
        synchronized (this) {
            if (cachedValues == null) {
                Properties properties = getPropertiesFile(propertiesFile);
                if (null != properties) {
                    Hashtable<String, String> ht = cachedValues = new Hashtable<>(properties.size());
                    for (Entry<Object, Object> entry : properties.entrySet()) {
                        ht.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        if (cachedValues != null && cachedValues.containsKey(key)) {
            return cachedValues.get(key).toString();
        }
        LOGGER.debug("Property '{}' not found in file '{}'! Using fallback :{}", key, propertiesFile, fallback);
        return retval;
    }

    /**
     * Retrieves the properties from the specified file
     *
     * @param propertiesFile The properties file
     * @return The properties or <code>null</code> if the config service is absent or no file is found.
     */
    private Properties getPropertiesFile(String propertiesFile) {
        ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == service) {
            service = AdminCache.getConfigurationService();
        }
        if (null == service) {
            LOGGER.debug("Service '{}' is missing.", ConfigurationService.class.getName());
            return null;
        }
        try {
            return service.getFile(propertiesFile);
        } catch (Exception e) {
            LOGGER.error("Properties file '{}' file cannot be opened for reading!", propertiesFile, e);
            return null;
        }
    }
}
