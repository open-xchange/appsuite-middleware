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

import com.openexchange.admin.exceptions.OXGenericException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import com.openexchange.admin.properties.AdminProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyHandler {
    
    protected Hashtable<String, Object>       allPropValues       = null;
    private Hashtable<String, String>       userPropValues      = null;
    protected Hashtable<String, String>       groupPropValues     = null;
    private Hashtable<String, String>       resPropValues       = null;
    private Hashtable<String, String>       rmiPropValues       = null;
    protected Hashtable<String, String> sqlPropValues = null;
    private final static Log log = LogFactory.getLog(PropertyHandler.class);

    private String configdirname;
    private Properties sysprops = null;
    
    private static final String PROPERTIES_USER             = "USER_PROP_CONFIG";
    protected static final String PROPERTIES_GROUP            = "GROUP_PROP_CONFIG";
    private static final String PROPERTIES_RESOURCE         = "RESOURCE_PROP_CONFIG";
    private static final String PROPERTIES_RMI              = "RMI_PROP_CONFIG";
    protected final static String PROPERTIES_SQL = "SQL_PROP_CONFIG";


    // The following lines define the property values for the database implementations
    public static final String GROUP_STORAGE = "GROUP_STORAGE";
    public static final String RESOURCE_STORAGE = "RESOURCE_STORAGE";
    public static final String TOOL_STORAGE = "TOOL_STORAGE";
    public static final String USER_STORAGE = "USER_STORAGE";
    
    
    
    public PropertyHandler(final Properties sysprops) {
        this.allPropValues = new Hashtable<String, Object>();
        this.sysprops = sysprops;
        try {
            loadProps(sysprops);
        } catch (final FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
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
            log.error("Property '" + key + "' not found in file " + this.configdirname +"! Using fallback :" + fallBack );
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
            if(log.isDebugEnabled()){
                log.debug( "Property '" + key + "' not found in the run script! Using fallback :" + fallBack );
            }
            retString = fallBack;
        }
        
        return retString; 
    }
    
    @SuppressWarnings("unchecked")
    public String getGroupProp( final String key, final String fallBack ) {
        String retBool = fallBack;
        
        if ( this.groupPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_GROUP ) ) {
                this.groupPropValues = (Hashtable<String, String>)this.allPropValues.get( PROPERTIES_GROUP );
            } else {
                if(log.isDebugEnabled()){
                    log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_GROUP + "' not found in file: " + this.configdirname ) );
                }
            }
        }
        
        if ( this.groupPropValues != null && this.groupPropValues.containsKey( key ) ) {
            retBool =  this.groupPropValues.get( key ).toString();
        } else {
            if(log.isDebugEnabled()){
                log.debug( "Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_GROUP_FILE ) +"! Using fallback :" + fallBack );
            }
        }
        
        return retBool; 
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean getGroupProp( final String key, final boolean fallBack ) {
        boolean retBool = fallBack;
        
        if ( this.groupPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_GROUP ) ) {
                this.groupPropValues = (Hashtable<String, String>)this.allPropValues.get( PROPERTIES_GROUP );
            } else {
                if(log.isDebugEnabled()){
                    log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_GROUP + "' not found in file: " + this.configdirname ) );
                }
            }
        }
        
        if ( this.groupPropValues != null && this.groupPropValues.containsKey( key ) ) {
            retBool = Boolean.parseBoolean( this.groupPropValues.get( key ).toString() ); 
        } else {
            if(log.isDebugEnabled()){
                log.debug("Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_GROUP_FILE ) +"! Using fallback :" + fallBack );
            }
        }
        
        return retBool; 
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean getUserProp( final String key, final boolean fallBack ) {
        boolean retBool = fallBack;
        
        if ( this.userPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_USER ) ) {
                this.userPropValues = (Hashtable<String, String>)this.allPropValues.get( PROPERTIES_USER );
            } else {
                if(log.isDebugEnabled()){
                    log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_USER + "' not found in file: " + this.configdirname ) );
                }
            }
        }
        
        if ( this.userPropValues != null && this.userPropValues.containsKey( key ) ) {
            final String val = this.userPropValues.get( key ).toString();
            retBool = Boolean.parseBoolean( val ); 
        } else {
            if(log.isDebugEnabled()){
                log.debug( "Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_USER_FILE ) +"! Using fallback :" + fallBack );
            }
        }
        
        return retBool; 
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getUserProp( final String key, final String fallBack ) {
        String retBool = fallBack;
        
        if ( this.userPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_USER ) ) {
                this.userPropValues = (Hashtable<String, String>)this.allPropValues.get( PROPERTIES_USER );
            } else {
                if(log.isDebugEnabled()){
                    log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_USER + "' not found in file: " + this.configdirname ) );
                }
            }
        }
        
        if ( this.userPropValues != null && this.userPropValues.containsKey( key ) ) {
            retBool =  this.userPropValues.get( key ).toString();
        } else {
            if(log.isDebugEnabled()){
                log.debug( "Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_USER_FILE ) +"! Using fallback :" + fallBack );
            }
        }
        
        return retBool; 
    }
    
    
    
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean getResourceProp( final String key, final boolean fallBack ) {
        boolean retBool = fallBack;
        
        if ( this.resPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_RESOURCE ) ) {
                this.resPropValues = (Hashtable<String, String>)this.allPropValues.get( PROPERTIES_RESOURCE );
            } else {
                if(log.isDebugEnabled()){
                    log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_RESOURCE + "' not found in file: " + this.configdirname ) );
                }
            }
        }
        
        if ( this.resPropValues != null && this.resPropValues.containsKey( key ) ) {
            retBool = Boolean.parseBoolean( this.resPropValues.get( key ).toString() ); 
        } else {
            if(log.isDebugEnabled()){
                log.debug( "Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_RESOURCE_FILE ) +"! Using fallback :" + fallBack );
            }
        }
        
        return retBool; 
    }
    
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    @SuppressWarnings("unchecked")
    public int getRmiProp( final String key, final int fallBack ) {
        int retInt = fallBack;
        
        if ( this.rmiPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_RMI ) ) {
                this.rmiPropValues = (Hashtable<String, String>)this.allPropValues.get( PROPERTIES_RMI );
            } else {
                if(log.isDebugEnabled()){
                    log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_RMI + "' not found in file: " + this.configdirname ) );
                }
            }
        }
        
        if ( this.rmiPropValues != null && this.rmiPropValues.containsKey( key ) ) {
            retInt = Integer.parseInt( this.rmiPropValues.get( key ).toString() ); 
        } else {
            if(log.isDebugEnabled()){
                log.debug( "Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_RMI_FILE ) +"! Using fallback :" + fallBack );
            }
        }
        
        return retInt; 
    }
    
    private void loadProps(final Properties sysprops) throws FileNotFoundException, IOException {
        this.allPropValues.put( AdminProperties.Prop.ADMINDAEMON_LOGLEVEL, "ALL" );
        
        if ( sysprops.getProperty( "configdir" ) != null ) {
            this.configdirname = sysprops.getProperty("configdir");
            addpropsfromfile(this.configdirname + File.separatorChar + "AdminDaemon.properties");
        } else {
            log.error("Parameter '-Dconfigdir' not given in system properties!");
            log.error("Now, using default parameter!");
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void addpropsfromfile(final String file) throws FileNotFoundException, IOException {
        final Properties configprops  = new Properties();
        configprops.load( new FileInputStream(file) );
        
        final Enumeration enumi = configprops.propertyNames();
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
                customprops.load( new FileInputStream( value ) );
                final Enumeration enuma = customprops.propertyNames();
                Hashtable<String, String> custconfig = new Hashtable<String, String>();
                if ( this.allPropValues.containsKey( param + "_CONFIG" ) ) {
                    custconfig = (Hashtable)this.allPropValues.get( param + "_CONFIG" );
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
    
    private String stringReplacer( String source, final String find, final String replacement ) {
        int i = 0;
        int j;
        final int k = find.length();
        final int m = replacement.length();
        
        while ( i < source.length() ) {
            j = source.indexOf( find, i );
            
            if ( j == -1 ) {
                break;
            }
            
            if ( j == 0 ) {
                source = replacement + source.substring( j + k );
            } else if ( j + k == source.length() ) {
                source = source.substring( 0, j ) + replacement;
            } else {
                source = source.substring( 0, j ) + replacement + source.substring( j + k );
            }
            i = j + m;
        }
        
        return source;
    }

    @SuppressWarnings("unchecked")
    public String getResourceProp(final String key, final String fallback) {
        String retval = fallback;
        if ( this.resPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_RESOURCE ) ) {
                this.resPropValues = (Hashtable<String, String>)this.allPropValues.get( PROPERTIES_RESOURCE );
            } else {
                if(log.isDebugEnabled()){
                    log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_RESOURCE + "' not found in file: " + this.configdirname ) );
                }
            }
        }
        
        if ( this.resPropValues != null && this.resPropValues.containsKey( key ) ) {
            retval = this.resPropValues.get( key ).toString(); 
        } else {
            if(log.isDebugEnabled()){
                log.debug("Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_RESOURCE_FILE ) +"! Using fallback :" + fallback );
            }
        }
        return retval;
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getSqlProp(final String key, final String fallBack) {
        String retString = fallBack;

        if (this.sqlPropValues == null) {
            if (this.allPropValues.containsKey(PROPERTIES_SQL)) {
                this.sqlPropValues = (Hashtable<String, String>) this.allPropValues.get(PROPERTIES_SQL);
            } else {
                log.error(OXGenericException.GENERAL_ERROR, new Exception("Property '" + PROPERTIES_SQL + "' not found."));
            }
        }

        if (this.sqlPropValues != null && this.sqlPropValues.containsKey(key)) {
            retString = this.sqlPropValues.get(key).toString();
        } else {
            log.error("Property '" + key + "' not found in file " + this.allPropValues.get(AdminProperties.Prop.PROPERTIES_SQL_FILE) + "! Using fallback :" + fallBack);
        }

        return retString;
    }


}
