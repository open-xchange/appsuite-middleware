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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import com.openexchange.java.Streams;

public class PropertyHandler {

    protected Hashtable<String, Object>       allPropValues       = null;
    private Hashtable<String, String>       userPropValues      = null;
    protected Hashtable<String, String>       groupPropValues     = null;
    private Hashtable<String, String>       resPropValues       = null;
    private Hashtable<String, String>       rmiPropValues       = null;
    protected Hashtable<String, String> sqlPropValues = null;
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyHandler.class);

    private String configdirname;
    private final Properties sysprops;

    //private static final String PROPERTIES_USER             = "USER_PROP_CONFIG";
    //private static final String PROPERTIES_GROUP            = "GROUP_PROP_CONFIG";
    //private static final String PROPERTIES_RESOURCE         = "RESOURCE_PROP_CONFIG";
    //private static final String PROPERTIES_RMI              = "RMI_PROP_CONFIG";
    protected final static String PROPERTIES_SQL = "SQL_PROP_CONFIG";

    // The following lines define the property values for the database implementations
    public static final String GROUP_STORAGE = "GROUP_STORAGE";
    public static final String RESOURCE_STORAGE = "RESOURCE_STORAGE";
    public static final String USER_STORAGE = "USER_STORAGE";

    public PropertyHandler(final Properties sysprops) {
        this.allPropValues = new Hashtable<String, Object>();
        this.sysprops = sysprops;
        try {
            loadProps(sysprops);
        } catch (final FileNotFoundException e) {
            log.error("", e);
        } catch (final IOException e) {
            log.error("", e);
        }
    }

    /**
     * Get String value from Properties-File. If not set or not found, use given fallback!
     *
     * @param key
     * @param fallBack
     * @return
     */
    public String getProp( final String key, final String fallBack ) {
        String retString = fallBack;

        if ( this.allPropValues.containsKey( key ) ) {
            retString = this.allPropValues.get( key ).toString();
        } else {
            log.error("Property '{}' not found in file {}! Using fallback :{}", key, this.configdirname, fallBack );
        }

        return retString;
    }



    /**
     *
     * @param key
     * @param fallBack
     * @return
     */
    public String getSysProp( final String key, final String fallBack ) {
        String retString = fallBack;
        final Properties syprops = new Properties( this.sysprops );
        retString = syprops.getProperty( key );

        if ( retString == null ) {
            log.debug("Property ''{}'' not found in the run script! Using fallback :{}", key, fallBack);
            retString = fallBack;
        }

        return retString;
    }

    public String getGroupProp( final String key, final String fallBack ) {
        String retBool = fallBack;

        synchronized (this) {
            if ( this.groupPropValues == null ) {
                ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null == service) {
                    service = AdminCache.getConfigurationService();
                }
                if (null == service) {
                    log.debug("Service '{}' is missing.", ConfigurationService.class.getName());
                } else {
                    final Properties properties = service.getFile("Group.properties");
                    final Hashtable<String, String> ht = this.groupPropValues = new Hashtable<String, String>(properties.size());
                    for (final Entry<Object, Object> entry : properties.entrySet()) {
                        ht.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        if ( this.groupPropValues != null && this.groupPropValues.containsKey( key ) ) {
            retBool =  this.groupPropValues.get( key ).toString();
        } else {
            log.debug("Property ''{}'' not found in file ''Group.properties''! Using fallback :{}", key, fallBack);
        }

        return retBool;
    }

    /**
     *
     * @param key
     * @param fallBack
     * @return
     */
    public boolean getGroupProp( final String key, final boolean fallBack ) {
        boolean retBool = fallBack;

        synchronized (this) {
            if ( this.groupPropValues == null ) {
                ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null == service) {
                    service = AdminCache.getConfigurationService();
                }
                if (null == service) {
                    log.debug("Service '{}' is missing.", ConfigurationService.class.getName());
                } else {
                    final Properties properties = service.getFile("Group.properties");
                    final Hashtable<String, String> ht = this.groupPropValues = new Hashtable<String, String>(properties.size());
                    for (final Entry<Object, Object> entry : properties.entrySet()) {
                        ht.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        if ( this.groupPropValues != null && this.groupPropValues.containsKey( key ) ) {
            retBool = Boolean.parseBoolean( this.groupPropValues.get( key ).toString() );
        } else {
            log.debug("Property '{}' not found in file 'Group.properties'! Using fallback :{}", key, fallBack );
        }

        return retBool;
    }

    /**
     *
     * @param key
     * @param fallBack
     * @return
     */
    public boolean getUserProp( final String key, final boolean fallBack ) {
        boolean retBool = fallBack;

        synchronized (this) {
            if ( this.userPropValues == null ) {
                ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null == service) {
                    service = AdminCache.getConfigurationService();
                }
                if (null == service) {
                    log.debug("Service '{}' is missing.", ConfigurationService.class.getName());
                } else {
                    final Properties properties = service.getFile("AdminUser.properties");
                    final Hashtable<String, String> ht = this.userPropValues = new Hashtable<String, String>(properties.size());
                    for (final Entry<Object, Object> entry : properties.entrySet()) {
                        ht.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        if ( this.userPropValues != null && this.userPropValues.containsKey( key ) ) {
            final String val = this.userPropValues.get( key ).toString();
            retBool = Boolean.parseBoolean( val );
        } else {
            log.debug("Property ''{}'' not found in file ''AdminUser.properties''! Using fallback :{}", key, fallBack);
        }

        return retBool;
    }

    /**
     *
     * @param key
     * @param fallBack
     * @return
     */
    public String getUserProp( final String key, final String fallBack ) {
        String retBool = fallBack;

        synchronized (this) {
            if ( this.userPropValues == null ) {
                ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null == service) {
                    service = AdminCache.getConfigurationService();
                }
                if (null == service) {
                    log.debug("Service '{}' is missing.", ConfigurationService.class.getName());
                } else {
                    final Properties properties = service.getFile("AdminUser.properties");
                    final Hashtable<String, String> ht = this.userPropValues = new Hashtable<String, String>(properties.size());
                    for (final Entry<Object, Object> entry : properties.entrySet()) {
                        ht.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        if ( this.userPropValues != null && this.userPropValues.containsKey( key ) ) {
            retBool =  this.userPropValues.get( key ).toString();
        } else {
            log.debug("Property ''{}'' not found in file ''AdminUser.properties''! Using fallback :{}", key, fallBack);
        }
        return retBool;
    }

    /**
     *
     * @param key
     * @param fallBack
     * @return
     */
    public boolean getResourceProp( final String key, final boolean fallBack ) {
        boolean retBool = fallBack;

        synchronized (this) {
            if ( this.resPropValues == null ) {
                ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null == service) {
                    service = AdminCache.getConfigurationService();
                }
                if (null == service) {
                    log.debug("Service '{}' is missing.", ConfigurationService.class.getName());
                } else {
                    final Properties properties = service.getFile("Resource.properties");
                    final Hashtable<String, String> ht = this.resPropValues = new Hashtable<String, String>(properties.size());
                    for (final Entry<Object, Object> entry : properties.entrySet()) {
                        ht.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        if ( this.resPropValues != null && this.resPropValues.containsKey( key ) ) {
            retBool = Boolean.parseBoolean( this.resPropValues.get( key ).toString() );
        } else {
            log.debug("Property ''{}'' not found in file ''Resource.properties''! Using fallback :{}", key, fallBack);
        }

        return retBool;
    }


    /**
     *
     * @param key
     * @param fallBack
     * @return
     */
    public int getRmiProp( final String key, final int fallBack ) {
        int retInt = fallBack;

        synchronized (this) {
            if ( this.rmiPropValues == null ) {
                ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null == service) {
                    service = AdminCache.getConfigurationService();
                }
                if (null == service) {
                    log.debug("Service '{}' is missing.", ConfigurationService.class.getName());
                } else {
                    final Properties properties = service.getFile("RMI.properties");
                    final Hashtable<String, String> ht = this.rmiPropValues = new Hashtable<String, String>(properties.size());
                    for (final Entry<Object, Object> entry : properties.entrySet()) {
                        ht.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        if ( this.rmiPropValues != null && this.rmiPropValues.containsKey( key ) ) {
            retInt = Integer.parseInt( this.rmiPropValues.get( key ).toString() );
        } else {
            log.debug("Property ''{}'' not found in file ''RMI.properties''! Using fallback :{1", key, fallBack);
        }

        return retInt;
    }

    private void loadProps(final Properties sysprops) throws FileNotFoundException, IOException {
        this.allPropValues.put( AdminProperties.Prop.ADMINDAEMON_LOGLEVEL, "ALL" );

        if ( sysprops.getProperty( "openexchange.propdir" ) != null ) {
            this.configdirname = sysprops.getProperty("openexchange.propdir");
            addpropsfromfile(this.configdirname + File.separatorChar + "AdminDaemon.properties");
        } else {
            log.error("Parameter '-Dopenexchange.propdir' not given in system properties!");
            log.error("Now, using default parameter!");
        }
    }

    protected void addpropsfromfile(final String file) throws FileNotFoundException, IOException {
        final Properties configprops  = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            configprops.load(in);
        } finally {
            Streams.close(in);
        }

        final Enumeration<?> enumi = configprops.propertyNames();
        while ( enumi.hasMoreElements() ) {
            final String param = (String)enumi.nextElement();
            String value = configprops.getProperty( param );

            if ( value.startsWith( "$PWD" ) ) {
                // FIXME: Set a parsed value here instead of working dir
                // A new File without any content point to the current working dir
                value = stringReplacer( value, "$PWD", new File( "" ).getAbsolutePath() );
            }

            this.allPropValues.put( param, value );

            if ( param.toLowerCase().endsWith( "_prop" ) ) {
                final Properties customprops = new Properties();
                try {
                    in = new FileInputStream(value);
                    customprops.load(in);
                } finally {
                    Streams.close(in);
                }
                final Enumeration<?> enuma = customprops.propertyNames();
                Hashtable<String, String> custconfig = new Hashtable<String, String>();
                if ( this.allPropValues.containsKey( param + "_CONFIG" ) ) {
                    custconfig = (Hashtable<String, String>)this.allPropValues.get( param + "_CONFIG" );
                }
                while ( enuma.hasMoreElements() ){
                    final String param_ = (String)enuma.nextElement();
                    String value_ = customprops.getProperty( param_ );
                    if ( value_.startsWith( "$PWD" ) ) {
                        value_ = stringReplacer( value_, "$PWD", new File( "" ).getAbsolutePath() );
                    }
                    if ( value_.startsWith( "\"" ) ) {
                        value_ = value_.substring( 1 );
                    }
                    if ( value_.endsWith( "\"" ) ) {
                        value_ = value_.substring( 0 , value_.length() - 1 );

                    }
                    custconfig.put( param_, value_ );
                }
                this.allPropValues.put( param + "_CONFIG", custconfig );
            }
        }
    }

    private String stringReplacer(final String source, final String find, final String replacement ) {
        int i = 0;
        int j;
        final int k = find.length();
        final int m = replacement.length();

        String src = source;
        while ( i < src.length() ) {
            j = src.indexOf( find, i );

            if ( j == -1 ) {
                break;
            }

            if ( j == 0 ) {
                src = replacement + src.substring( j + k );
            } else if ( j + k == src.length() ) {
                src = src.substring( 0, j ) + replacement;
            } else {
                src = src.substring( 0, j ) + replacement + src.substring( j + k );
            }
            i = j + m;
        }

        return src;
    }

    public String getResourceProp(final String key, final String fallback) {
        String retval = fallback;
        synchronized (this) {
            if (this.resPropValues == null) {
                ConfigurationService service = AdminServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null == service) {
                    service = AdminCache.getConfigurationService();
                }
                if (null == service) {
                    log.debug("Service '{}' is missing.", ConfigurationService.class.getName());
                } else {
                    final Properties properties = service.getFile("Resource.properties");
                    final Hashtable<String, String> ht = this.resPropValues = new Hashtable<String, String>(properties.size());
                    for (final Entry<Object, Object> entry : properties.entrySet()) {
                        ht.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
        }

        if (this.resPropValues != null && this.resPropValues.containsKey(key)) {
            retval = this.resPropValues.get(key).toString();
        } else {
            log.debug("Property '{}' not found in file 'Resource.properties'! Using fallback :{}", key, fallback);
        }
        return retval;
    }


}
