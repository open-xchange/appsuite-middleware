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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Set;
import com.openexchange.realtime.client.RTConnectionProperties.RTConnectionType;

/**
 * Provides configurations required to be able to send requests with the realtime client framework. Use
 * com.openexchange.realtime.client.config.ConfigurationProvider.getInstance().toString() to get the currently used configuration.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class ConfigurationProvider {

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
    private String clientId = "open-xchange-realtime";

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
     * Path for login calls
     */
    private String loginPath = "/ajax/login";

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

    // actions
    /**
     * Parameter used for login action
     */
    private String loginAction = "login";

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
    private int port = 80;

    /**
     * Flag to identify if secured connections (https) should be used
     */
    private boolean secure = true;

    /**
     * Initializes a new {@link ConfigurationProvider}.
     */
    private ConfigurationProvider() {

    }

    /**
     * Get an instance of the {@link ConfigurationProvider}. Use this if you would like to use the default configuration.
     * 
     * @return
     */
    public static final synchronized ConfigurationProvider getInstance() {
        if (SINGLETON == null) {
            SINGLETON = new ConfigurationProvider();

            try {
                loadPropertiesFile();
            } catch (IllegalArgumentException e) {
                // Error in reading property file content
            } catch (SecurityException e) {
                // Error in reading property file content
            } catch (IllegalAccessException e) {
                // Error in reading property file content
            } catch (IOException e) {
                // Error in reading property file content
            } catch (NoSuchFieldException e) {
                // Error in reading property file content
            }
        }
        return SINGLETON;
    }

    private static void loadPropertiesFile() throws IllegalArgumentException, IllegalAccessException, IOException, SecurityException, NoSuchFieldException {
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
        } catch (FileNotFoundException fileNotFoundException) {
            // TODO log: no properties file provided -use default
        }

    }

    /**
     * Returns the current status of the object in string representation
     * 
     * @return String with the status of the object
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(" InitializedWithPropertiesFile: " + this.isInitializedWithPropertiesFile() + newLine);
        result.append(" clientId: " + this.getClientId() + newLine);
        result.append(" defaultSelector: " + this.getDefaultSelector() + newLine);
        result.append(" apiPath: " + this.getApiPath() + newLine);
        result.append(" loginPath: " + this.getLoginPath() + newLine);
        result.append(" createPath: " + this.getCreatePath() + newLine);
        result.append(" queryPath: " + this.getQueryPath() + newLine);
        result.append(" sendPath: " + this.getSendPath() + newLine);
        result.append(" loginAction: " + this.getLoginAction() + newLine);
        result.append(" queryAction: " + this.getQueryAction() + newLine);
        result.append(" sendAction: " + this.getSendAction() + newLine);
        result.append(" loginAction: " + this.getLoginAction() + newLine);
        result.append(" connectionType: " + this.getConnectionType() + newLine);
        result.append(" host: " + this.getHost() + newLine);
        result.append(" port: " + this.getPort() + newLine);
        result.append(" secure: " + this.isSecure() + newLine);
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
     * Sets the clientId
     * 
     * @param clientId The clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
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
     * Sets the defaultSelector
     * 
     * @param defaultSelector The defaultSelector to set
     */
    public void setDefaultSelector(String defaultSelector) {
        this.defaultSelector = defaultSelector;
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
     * Sets the apiPath
     * 
     * @param apiPath The apiPath to set
     */
    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
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
     * Sets the loginPath
     * 
     * @param loginPath The loginPath to set
     */
    public void setLoginPath(String loginPath) {
        this.loginPath = loginPath;
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
     * Sets the createPath
     * 
     * @param createPath The createPath to set
     */
    public void setCreatePath(String createPath) {
        this.createPath = createPath;
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
     * Sets the queryPath
     * 
     * @param queryPath The queryPath to set
     */
    public void setQueryPath(String queryPath) {
        this.queryPath = queryPath;
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
     * Sets the sendPath
     * 
     * @param sendPath The sendPath to set
     */
    public void setSendPath(String sendPath) {
        this.sendPath = sendPath;
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
     * Sets the loginAction
     * 
     * @param loginAction The loginAction to set
     */
    public void setLoginAction(String loginAction) {
        this.loginAction = loginAction;
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
     * Sets the queryAction
     * 
     * @param queryAction The queryAction to set
     */
    public void setQueryAction(String queryAction) {
        this.queryAction = queryAction;
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
     * Sets the sendAction
     * 
     * @param sendAction The sendAction to set
     */
    public void setSendAction(String sendAction) {
        this.sendAction = sendAction;
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
     * Sets the connectionType
     * 
     * @param connectionType The connectionType to set
     */
    public void setConnectionType(RTConnectionType connectionType) {
        this.connectionType = connectionType;
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
     * Sets the host
     * 
     * @param host The host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port
     * 
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port
     * 
     * @param port The port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the secure
     * 
     * @return The secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets the secure
     * 
     * @param secure The secure to set
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Gets the initializedWithPropertiesFile
     * 
     * @return The initializedWithPropertiesFile
     */
    public boolean isInitializedWithPropertiesFile() {
        return this.initializedWithPropertiesFile;
    }
}
