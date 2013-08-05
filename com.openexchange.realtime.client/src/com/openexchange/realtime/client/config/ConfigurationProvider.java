/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.realtime.client.config;

import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import com.openexchange.realtime.client.RTConnectionProperties.RTConnectionType;

/**
 * Provides configurations required to be able to send requests with help of the realtime client framework. Use
 * com.openexchange.realtime.client.config.ConfigurationProvider.getInstance().toString() to get the currently used configuration.<br>
 * <br>
 * If a file with the name 'config.properties' is provided in the 'conf' folder, the configuration made there will be used for further
 * processing. A template of the configuration file can be found within the 'tmp' folder. <br>
 * <br>
 * Second possibility to configure the parameters (besides providing a config-file) is to use {@link ConfigurationProvider.Builder}. Use
 * this class to override the default configuration by setting each parameter by your preferred value. If you do not set a parameter, the
 * default will be used.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class ConfigurationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProvider.class);

    /**
     * Singleton that provides the configuration data
     */
    private static ConfigurationProvider SINGLETON = null;

    /**
     * Indicates if the configuration is based on the configuration file or not.
     */
    private boolean initializedWithPropertiesFile = false;

    /**
     * Id that will be used to identify the client
     */
    protected String clientId = "open-xchange-realtime";

    /**
     * Selector (or roomname) that will be used for requests
     */
    private String defaultSelector = "default";

    // url paths
    /**
     * Path for API calls
     */
    private String apiPath = "/appsuite/api";

    /**
     * Path for creation calls
     */
    private String createPath = apiPath + "/oxodocumentfilter";

    /**
     * Path for query calls
     */
    private String queryPath = apiPath + "/rt";

    /**
     * Path for send calls
     */
    private String sendPath = apiPath + "/rt";

    /**
     * Path for atmosphere calls
     */
    private String atmospherePath = "/realtime/atmosphere/rt";

    /**
     * Parameter used for query actions
     */
    private String queryAction = queryPath + "?action=query";

    /**
     * Parameter used for send actions
     */
    private String sendAction = queryPath + "?action=send";

    // connection
    /**
     * Connection type that is used for hte connection
     */
    private RTConnectionType connectionType = RTConnectionType.LONG_POLLING;

    /**
     * Host to address the requests
     */
    private String host = "localhost";

    /**
     * Port to address the requests
     */
    private AtomicInteger port = new AtomicInteger(80);

    /**
     * Flag to identify if secured connections (https) should be used
     */
    private AtomicBoolean secure = new AtomicBoolean(true);

    /**
     * Flag to identify if secured connections (https) should be used
     */
    private AtomicBoolean isTraceEnabled = new AtomicBoolean(false);
    
    /**
     * Path for login calls
     */
    private String loginPath = apiPath + "/login";
    
    /**
     * Parameter used for login actions
     */
    private String loginAction = "login";
    
    /**
     * Initializes a new {@link ConfigurationProvider} - only internal
     */
    private ConfigurationProvider() {
        // prevent instantiation
    }

    /**
     * Get an instance of the {@link ConfigurationProvider}. Use this if you would like to use the default configuration.
     * 
     * @return {@link ConfigurationProvider} instance, based on properties file if provided
     */
    public static final synchronized ConfigurationProvider getInstance() {
        if (SINGLETON == null) {
            SINGLETON = new ConfigurationProvider();
            SINGLETON.loadPropertiesFile();
        }
        return SINGLETON;
    }

    /**
     * Try to load the properties from the file 'config.properties' from the 'conf' folder. A template of the used properties-file can be
     * found in 'tmp' folder. Use this as default and copy it to the 'conf' folder. If a value is set within the file, the default will be
     * overridden. If there is no value set, the default will be used.
     * 
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    protected void loadPropertiesFile() {
        Properties prop = new Properties();

        try {
            // Exit after the following call if property file not available
            prop.load(new FileInputStream("conf/config.properties"));

            Set<Object> keySet = prop.keySet();
            for (Object o : keySet) {
                if (o instanceof String) {
                    String key = (String) o;
                    int colon = key.lastIndexOf(".");
                    String fieldName = key.substring(colon + 1);
                    Field field = ConfigurationProvider.getInstance().getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);

                    Object value = prop.get(key);

                    if (!value.equals("")) {

                        if (value.equals("true") || value.equals("false")) {
                            boolean b = Boolean.getBoolean((String) value);
                            field.set(ConfigurationProvider.getInstance(), b);
                        } else if (value.equals("RTConnectionType.LONG_POLLING")) {
                            field.set(ConfigurationProvider.getInstance(), RTConnectionType.LONG_POLLING);
                        } else if (value.equals("RTConnectionType.WEBSOCKET")) {
                            field.set(ConfigurationProvider.getInstance(), RTConnectionType.WEBSOCKET);
                        } else if (fieldName.equals("port")) {
                            field.set(ConfigurationProvider.getInstance(), new Integer(value.toString()));
                        } else {
                            field.set(ConfigurationProvider.getInstance(), value);
                        }
                    }
                }
            }
            SINGLETON.initializedWithPropertiesFile = true;
        } catch (Exception e) {
            LOG.error("Couldn't parse property file", e);
        }
    }

    /**
     * Returns the current status of the object in string representation
     * 
     * @return String with the status of the object
     */
    @Override
    public synchronized String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(" InitializedWithPropertiesFile: " + this.isInitializedWithPropertiesFile() + newLine);
        result.append(" apiPath: " + this.getApiPath() + newLine);
        result.append(" atmospherePath: " + this.getAtmospherePath() + newLine);
        result.append(" clientId: " + this.getClientId() + newLine);
        result.append(" connectionType: " + this.getConnectionType() + newLine);
        result.append(" createPath: " + this.getCreatePath() + newLine);
        result.append(" defaultSelector: " + this.getDefaultSelector() + newLine);
        result.append(" host: " + this.getHost() + newLine);
        result.append(" isTraceEnabled: " + this.isTraceEnabled() + newLine);
        result.append(" loginPath: " + this.getLoginPath() + newLine);
        result.append(" loginAction: " + this.getLoginAction() + newLine);
        result.append(" port: " + this.getPort() + newLine);
        result.append(" queryAction: " + this.getQueryAction() + newLine);
        result.append(" queryPath: " + this.getQueryPath() + newLine);
        result.append(" secure: " + this.isSecure() + newLine);
        result.append(" sendPath: " + this.getSendPath() + newLine);
        result.append(" sendAction: " + this.getSendAction() + newLine);
        result.append("}");

        return result.toString();
    }

    /**
     * Gets the clientId
     * 
     * @return The clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the defaultSelector
     * 
     * @return The defaultSelector
     */
    public String getDefaultSelector() {
        return defaultSelector;
    }

    /**
     * Gets the apiPath
     * 
     * @return The apiPath
     */
    public String getApiPath() {
        return apiPath;
    }

    /**
     * Gets the createPath
     * 
     * @return The createPath
     */
    public String getCreatePath() {
        return createPath;
    }

    /**
     * Gets the queryPath
     * 
     * @return The queryPath
     */
    public String getQueryPath() {
        return queryPath;
    }

    /**
     * Gets the sendPath
     * 
     * @return The sendPath
     */
    public String getSendPath() {
        return sendPath;
    }

    /**
     * Gets the queryAction
     * 
     * @return The queryAction
     */
    public String getQueryAction() {
        return queryAction;
    }

    /**
     * Gets the sendAction
     * 
     * @return The sendAction
     */
    public String getSendAction() {
        return sendAction;
    }

    /**
     * Gets the connectionType
     * 
     * @return The connectionType
     */
    public RTConnectionType getConnectionType() {
        return connectionType;
    }

    /**
     * Gets the host
     * 
     * @return The host
     */
    public String getHost() {
        return host;
    }

    
    /**
     * Gets the loginPath
     *
     * @return The loginPath
     */
    public String getLoginPath() {
        return loginPath;
    }

    
    /**
     * Gets the loginAction
     *
     * @return The loginAction
     */
    public String getLoginAction() {
        return loginAction;
    }

    /**
     * Gets the port
     * 
     * @return The port
     */
    public int getPort() {
        return port.get();
    }

    /**
     * Gets the secure
     * 
     * @return The secure
     */
    public boolean isSecure() {
        return secure.get();
    }

    /**
     * Gets the atmospherePath
     * 
     * @return The atmospherePath
     */
    public String getAtmospherePath() {
        return atmospherePath;
    }

    
    /**
     * Gets if tracing is enabled
     *
     * @return true if tracing is enabled
     */
    public boolean isTraceEnabled() {
        return isTraceEnabled.get();
    }

    /**
     * Gets the initializedWithPropertiesFile
     * 
     * @return The initializedWithPropertiesFile
     */
    public boolean isInitializedWithPropertiesFile() {
        return this.initializedWithPropertiesFile;
    }

    /**
     * Factory method for creating a new {@link ConfigurationProvider.Builder} to create a configured instance of
     * {@link ConfigurationProvider}
     * 
     * @return a {@link ConfigurationProvider.Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Static class that should be used to configure a new {@link ConfigurationProvider}. By calling the setter-methods the default values
     * will be overridden which means, calling {@link Builder#fromFile()} will create a {@link ConfigurationProvider} with the default settings (also created
     * by {@link ConfigurationProvider#getInstance()}.
     * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
     * @since 7.4
     */
    public static class Builder {

        private final ConfigurationProvider configurationProvider;

        private Builder() {
            super();
            configurationProvider = new ConfigurationProvider();
        }

        public Builder fromFile() {
            configurationProvider.loadPropertiesFile();
            return this;
        }
        
        /**
         * Sets the apiPath.
         * 
         * @param apiPath - Path for api calls.
         */
        public Builder setApiPath(String apiPath) {
            if(!Strings.isNullOrEmpty(apiPath)) {
                configurationProvider.apiPath = apiPath;
            }
            return this;
        }

        /**
         * Sets the clientId.
         * 
         * @param clientId - Id of the client.
         */
        public Builder setClientId(String clientId) {
            if(!Strings.isNullOrEmpty(clientId)) {
                configurationProvider.clientId = clientId;
            }
            return this;
        }

        /**
         * Sets the connectionType.
         * 
         * @param connectionType - {@link RTConnectionType} the client would like to use.
         */
        public Builder setConnectionType(String connectionType) {
            if(!Strings.isNullOrEmpty(connectionType)) {
                configurationProvider.connectionType = getConnectionType(connectionType);
            }
            return this;
        }
        
        /**
         * Sets the connectionType.
         * 
         * @param connectionType - {@link RTConnectionType} the client would like to use.
         */
        public Builder setConnectionType(RTConnectionType connectionType) {
            if(connectionType != null) {
                configurationProvider.connectionType = connectionType;
            }
            return this;
        }

        /**
         * Sets the createPath.
         * 
         * @param createPath - the createPath.
         */
        public Builder setCreatePath(String createPath) {
            if(!Strings.isNullOrEmpty(createPath)) {
                configurationProvider.createPath = createPath;
            }
            return this;
        }

        /**
         * Sets the atmospherePath.
         * 
         * @param atmospherePath - the atmospherePath.
         */
        public Builder setAtmospherePath(String atmospherePath) {
            if(!Strings.isNullOrEmpty(atmospherePath)) {
                configurationProvider.atmospherePath = atmospherePath;
            }
            return this;
        }

        /**
         * Sets the defaultSelector.
         * 
         * @param defaultSelector - the defaultSelector.
         */
        public Builder setDefaultSelector(String defaultSelector) {
            if(!Strings.isNullOrEmpty(defaultSelector)) {
                configurationProvider.defaultSelector = defaultSelector;
            }
            return this;
        }

        /**
         * Sets the host.
         * 
         * @param host - the host.
         */
        public Builder setHost(String host) {
            if(!Strings.isNullOrEmpty(host)) {
                configurationProvider.host = host;
            }
            return this;
        }

        /**
         * Sets the login path
         * 
         * @param loginPath the login path
         */
        public Builder setLoginPath(String loginPath) {
            if(!Strings.isNullOrEmpty(loginPath)) {
                configurationProvider.loginPath = loginPath;
            }
            return this;
        }

        /**
         * Sets the login path
         * 
         * @param loginPath the login path
         */
        public Builder setLoginAction(String loginAction) {
            if(!Strings.isNullOrEmpty(loginAction)) {
                configurationProvider.loginPath = loginAction;
            }
            return this;
        }

        /**
         * Sets the port.
         * 
         * @param port - the port.
         */
        public Builder setPort(String port) {
            if(!Strings.isNullOrEmpty(port)) {
                configurationProvider.port = new AtomicInteger(Integer.parseInt(port));
            }
            return this;
        }
        
        /**
         * Sets the port.
         * 
         * @param port - the port.
         */
        public Builder setPort(int port) {
            configurationProvider.port = new AtomicInteger(port);
            return this;
        }

        /**
         * Sets the queryAction.
         * 
         * @param queryAction - the queryAction.
         */
        public Builder setQueryAction(String queryAction) {
            if(!Strings.isNullOrEmpty(queryAction)) {
                configurationProvider.queryAction = queryAction;
            }
            return this;
        }

        /**
         * Sets the queryPath.
         * 
         * @param queryPath - the queryPath.
         */
        public Builder setQueryPath(String queryPath) {
            if (!Strings.isNullOrEmpty(queryPath)) {
                configurationProvider.queryPath = queryPath;
            }
            return this;
        }

        /**
         * Toggle https usage
         * @param isSecure if true then use https, else http
         */
        public Builder setSecure(String isSecure) {
            if(!Strings.isNullOrEmpty(isSecure)) {
                configurationProvider.secure.set(Boolean.parseBoolean(isSecure));
            }
            return this;
        }

        /**
         * Toggle https usage
         * @param isSecure if true then use https, else http
         */
        public Builder setSecure(boolean isSecure) {
            configurationProvider.secure.set(isSecure);
            return this;
        }

        /**
         * Sets the sendAction.
         * 
         * @param sendAction - the sendAction.
         */
        public Builder setSendAction(String sendAction) {
            if(!Strings.isNullOrEmpty(sendAction)) {
                configurationProvider.sendAction = sendAction;
            }
            return this;
        }

        /**
         * Sets the sendPath.
         * 
         * @param sendPath - the sendPath.
         */
        public Builder setSendPath(String sendPath) {
            if(!Strings.isNullOrEmpty(sendPath)) {
                configurationProvider.sendPath = sendPath;
            }
            return this;
        }
        
        /**
         * Enable or disable tracing of messages
         *
         * @param isTraceEnabled true or false
         */
        public Builder setTraceEnabled(String isTraceEnabled) {
            if(!Strings.isNullOrEmpty(isTraceEnabled)) {
                configurationProvider.isTraceEnabled.set(Boolean.parseBoolean(isTraceEnabled));
            }
            return this;
        }

        /**
         * Enable or disable tracing of messages
         *
         * @param isTraceEnabled true or false
         */
        public Builder setTraceEnabled(boolean isTraceEnabled) {
            configurationProvider.isTraceEnabled.set(isTraceEnabled);
            return this;
        }

        private RTConnectionType getConnectionType(String connectionType) {
            for (RTConnectionType type : RTConnectionType.values()) {
                if (type.name().equalsIgnoreCase(connectionType)) {
                    return type;
                }
            }
            LOG.info("Couldn't find matching RTConnectionType for: " + connectionType + ". Using RTConnectionType.LONG_POLLING as default");
            return RTConnectionType.LONG_POLLING;
        }
        
        /**
         * @return A valid {@link ConfigurationProvider} instance.
         */
        public ConfigurationProvider build() {
            if (configurationProvider.port.get() != -1 && !(configurationProvider.port.get() > 0 && configurationProvider.port.get() <= 65535)) {
                throw new IllegalStateException("Port must be between 1 and 65535!");
            }
            SINGLETON = configurationProvider;
            return configurationProvider;
        }
    }
}
