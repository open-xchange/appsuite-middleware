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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.exceptions.ConfigException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchPluginException;


public class PropertyHandler {

    public enum PropertyFiles {
        ADMIN (PROPERTIES_ADMIN),
        GROUP (PROPERTIES_GROUP),
        PLUGIN (PROPERTIES_PLUGIN),
        RESOURCE (PROPERTIES_RESOURCE),
        RMI (PROPERTIES_RMI),
        SQL (PROPERTIES_SQL),
        USER (PROPERTIES_USER);
        
        private final String text;
        
        PropertyFiles(final String text) {
            this.text = text;
        }
        
        public String getText() {
            return this.text;
        }
    }

    private static final String PROPERTIES_ADMIN = "ADMIN_PROP_CONFIG";
    private static final String PROPERTIES_GROUP = "GROUP_PROP_CONFIG";
    private static final String PROPERTIES_PLUGIN = "PLUGIN_PROP_CONFIG";
    private static final String PROPERTIES_RESOURCE = "RESOURCE_PROP_CONFIG";
    private static final String PROPERTIES_RMI = "RMI_PROP_CONFIG";
    private static final String PROPERTIES_SQL = "SQL_PROP_CONFIG";
    private static final String PROPERTIES_USER = "USER_PROP_CONFIG";
    
    private static final Log log = LogFactory.getLog(PropertyHandler.class);

    final private CombinedConfiguration cc;
    final private Hashtable<String, Configuration> pluginconfigs;
    private String configdirname;

    
    public PropertyHandler(final Properties sysprops) {
        cc = new CombinedConfiguration();
        pluginconfigs = new Hashtable<String, Configuration>();
        try {
            loadMainProperty(sysprops);
            loadOtherRequiredProperties();
        } catch (final FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
        } catch (final ConfigException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    public boolean getBoolean(final PropertyFiles propfiles, final String key) throws InvalidDataException {
        try {
            return cc.getConfiguration(propfiles.getText()).getBoolean(key);
        } catch (final RuntimeException e) {
            throw new InvalidDataException(e.toString());
        }
    }

    public boolean getBoolean(final PropertyFiles propfiles, final String key, final boolean defaultValue) throws InvalidDataException {
        try {
            return cc.getConfiguration(propfiles.getText()).getBoolean(key, defaultValue);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw new InvalidDataException(e.toString());
        }
    }
    
    public boolean getBoolean(final String pluginname, final String key, final boolean defaultValue) throws NoSuchPluginException, InvalidDataException {
        final Configuration pluginconfig = pluginconfigs.get(pluginname);
        if (null != pluginconfig) {
            try {
                return pluginconfig.getBoolean(key, defaultValue);
            } catch (final RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new InvalidDataException(e.toString());
            }
        }
        throw new NoSuchPluginException(pluginname);
    }
    
    public int getInt(final PropertyFiles propfiles, final String key) throws InvalidDataException {
        try {
            return cc.getConfiguration(propfiles.getText()).getInt(key);
        } catch (final RuntimeException e) {
            throw new InvalidDataException(e.toString());
        }
    }
    
    public int getInt(final PropertyFiles propfiles, final String key, final int defaultValue) throws InvalidDataException {
        try {
            return cc.getConfiguration(propfiles.getText()).getInt(key, defaultValue);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw new InvalidDataException(e.toString());
        }
    }

    public int getInt(final String pluginname, final String key) throws NoSuchPluginException, InvalidDataException {
        final Configuration pluginconfig = pluginconfigs.get(pluginname);
        if (null != pluginconfig) {
            try {
                return pluginconfig.getInt(key);
            } catch (final RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new InvalidDataException(e.toString());
            }
        }
        throw new NoSuchPluginException(pluginname);
    }

    public int getInt(final String pluginname, final String key, final int defaultValue) throws NoSuchPluginException, InvalidDataException {
        final Configuration pluginconfig = pluginconfigs.get(pluginname);
        if (null != pluginconfig) {
            try {
                return pluginconfig.getInt(key, defaultValue);
            } catch (final RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new InvalidDataException(e.toString());
            }
        }
        throw new NoSuchPluginException(pluginname);
    }

    public long getLong(final String pluginname, final String key) throws NoSuchPluginException, InvalidDataException {
        final Configuration pluginconfig = pluginconfigs.get(pluginname);
        if (null != pluginconfig) {
            try {
                return pluginconfig.getLong(key);
            } catch (final RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new InvalidDataException(e.toString());
            }
        }
        throw new NoSuchPluginException(pluginname);
    }
    
    public long getLong(final String pluginname, final String key, final int defaultValue) throws NoSuchPluginException, InvalidDataException {
        final Configuration pluginconfig = pluginconfigs.get(pluginname);
        if (null != pluginconfig) {
            try {
                return pluginconfig.getLong(key, defaultValue);
            } catch (final RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new InvalidDataException(e.toString());
            }
        }
        throw new NoSuchPluginException(pluginname);
    }
    
    public String getString(final PropertyFiles propfiles, final String key) throws InvalidDataException {
        try {
            return cc.getConfiguration(propfiles.getText()).getString(key);
        } catch (final RuntimeException e) {
            throw new InvalidDataException(e.toString());
        }
    }

    public String getString(final PropertyFiles propfiles, final String key, final String defaultValue) throws InvalidDataException {
        try {
            return cc.getConfiguration(propfiles.getText()).getString(key, defaultValue);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw new InvalidDataException(e.toString());
        }
    }

    public String getString(final String pluginname, final String key) throws NoSuchPluginException, InvalidDataException {
        final Configuration pluginconfig = pluginconfigs.get(pluginname);
        if (null != pluginconfig) {
            try {
                return pluginconfig.getString(key);
            } catch (final RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new InvalidDataException(e.toString());
            }
        }
        throw new NoSuchPluginException(pluginname);
    }

    public String getString(final String pluginname, final String key, final String defaultValue) throws NoSuchPluginException, InvalidDataException {
        final Configuration pluginconfig = pluginconfigs.get(pluginname);
        if (null != pluginconfig) {
            try {
                return pluginconfig.getString(key, defaultValue);
            } catch (final RuntimeException e) {
                log.error(e.getMessage(), e);
                throw new InvalidDataException(e.toString());
            }
        }
        throw new NoSuchPluginException(pluginname);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getList(final PropertyFiles propfiles, final String key) throws InvalidDataException {
        try {
            return cc.getConfiguration(propfiles.getText()).getList(key);
        } catch (final RuntimeException e) {
            log.error(e.getMessage(), e);
            throw new InvalidDataException(e.toString());
        }
    }
    
    public void initializeDefaults() {
        // Booleans
        checkAndSetDefault(PropertyFiles.GROUP, AdminProperties.Group.AUTO_LOWERCASE, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.GROUP, AdminProperties.Group.CHECK_NOT_ALLOWED_CHARS, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.RESOURCE, AdminProperties.Resource.AUTO_LOWERCASE, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.USER, AdminProperties.User.PRIMARY_MAIL_UNCHANGEABLE, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.USER, AdminProperties.User.DISPLAYNAME_UNIQUE, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.USER, AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.USER, AdminProperties.User.AUTO_LOWERCASE, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.RESOURCE, AdminProperties.Resource.AUTO_LOWERCASE, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.RESOURCE, AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, Boolean.TRUE);
        checkAndSetDefault(PropertyFiles.USER, AdminProperties.User.CREATE_HOMEDIRECTORY, Boolean.FALSE);
        checkAndSetDefault(PropertyFiles.SQL, AdminProperties.SQL.LOG_PARSED_QUERIES, Boolean.FALSE);
        checkAndSetDefault(PropertyFiles.ADMIN, AdminProperties.Prop.MASTER_AUTHENTICATION_DISABLED, Boolean.FALSE);
        checkAndSetDefault(PropertyFiles.ADMIN, AdminProperties.Prop.CONTEXT_AUTHENTICATION_DISABLED, Boolean.FALSE);
        
        // Ints
        checkAndSetDefault(PropertyFiles.GROUP, AdminProperties.Group.GID_NUMBER_START, Integer.valueOf(-1));
        checkAndSetDefault(PropertyFiles.USER, AdminProperties.User.UID_NUMBER_START, Integer.valueOf(-1));
        checkAndSetDefault(PropertyFiles.RMI, AdminProperties.RMI.RMI_PORT, Integer.valueOf(1099));
        checkAndSetDefault(PropertyFiles.ADMIN, AdminProperties.Prop.CONCURRENT_JOBS, Integer.valueOf(2));
        
        // Strings
        checkAndSetDefault(PropertyFiles.USER, AdminProperties.User.HOME_DIR_ROOT, "/home");
        checkAndSetDefault(PropertyFiles.ADMIN, AdminProperties.Prop.BIND_ADDRESS, "localhost");
        checkAndSetDefault(PropertyFiles.ADMIN, AdminProperties.Prop.SERVER_NAME, "local");
        checkAndSetDefault(PropertyFiles.SQL, AdminProperties.SQL.INITIAL_OX_SQL_DIR, "/opt/openexchange-internal/system/setup/mysql/");
        checkAndSetDefault(PropertyFiles.GROUP, AdminProperties.Group.CHECK_GROUP_UID_REGEXP, "[ $@%\\.+a-zA-Z0-9_-]");
        checkAndSetDefault(PropertyFiles.RESOURCE, AdminProperties.Resource.CHECK_RES_UID_REGEXP, "[ $@%\\.+a-zA-Z0-9_-]");
        checkAndSetDefault(PropertyFiles.USER, AdminProperties.User.CHECK_USER_UID_REGEXP, "[$@%\\.+a-zA-Z0-9_-]");
        checkAndSetDefault(PropertyFiles.ADMIN, AdminProperties.Prop.MASTER_AUTH_FILE, "/opt/open-xchange/admindaemon/etc/mpasswd");
    }
    
    private void checkAndSetDefault(final PropertyFiles propfiles, final String key, final Object defaultvalue) {
        final Configuration configuration = cc.getConfiguration(propfiles.getText());
        if (!configuration.containsKey(key)) {
            log.debug("Setting key: " + key + " to our defaultvalue: " + defaultvalue);
            configuration.setProperty(key, defaultvalue);
        }
    }

    public void checkAndSetDefault(final String pluginname, final String key, final Object defaultvalue) {
        final Configuration pluginconfig = pluginconfigs.get(pluginname);
        if (!pluginconfig.containsKey(key)) {
            log.debug("Setting key: " + key + " to our defaultvalue: " + defaultvalue);
            pluginconfig.setProperty(key, defaultvalue);
        }
    }
    
    /**
     * This method registers a new property file for the given plugin name. Note that only one file per plugin name is possible
     * 
     * @param pluginname A {@link String} containing a name for the plugin
     * @param propertyfile A {@link String} containing a file name below the plugin dir
     * @throws ConfigException Is thrown if something isn't correct with the given property file
     */
    public void registerPluginProperty(final String pluginname, final String propertyfile) throws ConfigException {
        try {
            pluginconfigs.put(pluginname, new PropertiesConfiguration(this.configdirname + File.separatorChar + "plugin" + File.separatorChar + propertyfile));
        } catch (final ConfigurationException e) {
            throw new ConfigException(e.toString());
        }
    }
    
    /**
     * Removes the given plugin from the property handler
     * 
     * @param pluginname
     */
    public void unregisterPluginProperty(final String pluginname) {
        pluginconfigs.remove(pluginname);
    }
    
    private void loadMainProperty(final Properties sysprops) throws FileNotFoundException, IOException, ConfigException {
        if ( sysprops.getProperty( "configdir" ) != null ) {
            this.configdirname = sysprops.getProperty("configdir");
            try {
                cc.addConfiguration(new PropertiesConfiguration(this.configdirname + File.separatorChar + "AdminDaemon.properties"), PropertyFiles.ADMIN.getText());
            } catch (final ConfigurationException e) {
                throw new ConfigException(e.toString());
            }
        } else {
            log.error("Parameter '-Dconfigdir' not given in system properties!");
            log.error("Now, using default parameter!");
        }
    }

    private void loadOtherRequiredProperties() throws ConfigException {
        try {
            final String user_prop_file = cc.getString("USER_PROP");
            final String sql_prop_file = cc.getString("SQL_PROP");
            final String group_prop_file = cc.getString("GROUP_PROP");
            final String resource_prop_file = cc.getString("RESOURCE_PROP");
            final String rmi_prop_file = cc.getString("RMI_PROP");
            if (null != sql_prop_file) {
                cc.addConfiguration(new PropertiesConfiguration(sql_prop_file), PropertyFiles.SQL.getText());
            }
            if (null != user_prop_file) {
                cc.addConfiguration(new PropertiesConfiguration(user_prop_file), PropertyFiles.USER.getText());
            }
            if (null != group_prop_file) {
                cc.addConfiguration(new PropertiesConfiguration(group_prop_file), PropertyFiles.GROUP.getText());
            }
            if (null != resource_prop_file) {
                cc.addConfiguration(new PropertiesConfiguration(resource_prop_file), PropertyFiles.RESOURCE.getText());
            }
            if (null != rmi_prop_file) {
                cc.addConfiguration(new PropertiesConfiguration(rmi_prop_file), PropertyFiles.RMI.getText());
            }
        } catch (final ConfigurationException e) {
            throw new ConfigException(e.toString());
        }
    }
}
