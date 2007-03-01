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
    private Hashtable       userPropValues      = null;
    protected Hashtable       groupPropValues     = null;
    private Hashtable       resPropValues       = null;
    private Hashtable       resGroupPropValues  = null;
    private Hashtable       rmiPropValues       = null;
    private Log log = LogFactory.getLog(this.getClass());
    
    private String configdirname;
    private Properties sysprops = null;
    
    private static final String PROPERTIES_USER             = "USER_PROP_CONFIG";
    protected static final String PROPERTIES_GROUP            = "GROUP_PROP_CONFIG";
    private static final String PROPERTIES_RESOURCE         = "RESOURCE_PROP_CONFIG";
    private static final String PROPERTIES_RESOURCE_GROUP   = "RESOURCE_GROUP_PROP_CONFIG";
    private static final String PROPERTIES_RMI              = "RMI_PROP_CONFIG";
    
    // The following lines define the property values for the database implementations
    public static final String GROUP_STORAGE = "GROUP_STORAGE";
    public static final String RESOURCE_STORAGE = "RESOURCE_STORAGE";
    public static final String RESOURCEGROUP_STORAGE = "RESOURCEGROUP_STORAGE";
    public static final String TOOL_STORAGE = "TOOL_STORAGE";
    public static final String USER_STORAGE = "USER_STORAGE";
    
    
    
    public PropertyHandler(final Properties sysprops) {
        allPropValues = new Hashtable<String, Object>();
        this.sysprops = sysprops;
        try {
            loadProps(sysprops);
        } catch ( Exception e ) {
            log.fatal("Error loading properties!",e);
        }
    }
    
    /**
     * Get String value from Properties-File. If not set or not found, use given fallback!
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public String getProp( String key, String fallBack ) {
        String retString = fallBack;
        
        if ( allPropValues.containsKey( key ) ) {
            retString = allPropValues.get( key ).toString(); 
        } else {
            log.debug( "Property '" + key + "' not found in file " + configdirname +"! Using fallback :" + fallBack );
        }
        
        return retString; 
    }
    
    
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public String getSysProp( String key, String fallBack ) {
        String retString = fallBack;
        Properties syprops     = new Properties( this.sysprops );
        retString = syprops.getProperty( key );
        
        if ( retString == null ) {
            log.debug( "Property '" + key + "' not found in the run script! Using fallback :" + fallBack );
            retString = fallBack;
        }
        
        return retString; 
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public boolean getGroupProp( String key, boolean fallBack ) {
        boolean retBool = fallBack;
        
        if ( groupPropValues == null ) {
            if ( allPropValues.containsKey( PROPERTIES_GROUP ) ) {
                groupPropValues = (Hashtable)allPropValues.get( PROPERTIES_GROUP );
            } else {
                log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_GROUP + "' not found in file: " + configdirname ) );
            }
        }
        
        if ( groupPropValues != null && groupPropValues.containsKey( key ) ) {
            retBool = Boolean.parseBoolean( groupPropValues.get( key ).toString() ); 
        } else {
            log.debug( "Property '" + key + "' not found in file " + allPropValues.get( AdminProperties.Prop.PROPERTIES_GROUP_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retBool; 
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public boolean getUserProp( String key, boolean fallBack ) {
        boolean retBool = fallBack;
        
        if ( userPropValues == null ) {
            if ( allPropValues.containsKey( PROPERTIES_USER ) ) {
                userPropValues = (Hashtable)allPropValues.get( PROPERTIES_USER );
            } else {
                log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_USER + "' not found in file: " + configdirname ) );
            }
        }
        
        if ( userPropValues != null && userPropValues.containsKey( key ) ) {
            String val = userPropValues.get( key ).toString();
            retBool = Boolean.parseBoolean( val ); 
        } else {
            log.debug( "Property '" + key + "' not found in file " + allPropValues.get( AdminProperties.Prop.PROPERTIES_USER_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retBool; 
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public String getUserProp( String key, String fallBack ) {
        String retBool = fallBack;
        
        if ( userPropValues == null ) {
            if ( allPropValues.containsKey( PROPERTIES_USER ) ) {
                userPropValues = (Hashtable)allPropValues.get( PROPERTIES_USER );
            } else {
                log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_USER + "' not found in file: " + configdirname ) );
            }
        }
        
        if ( userPropValues != null && userPropValues.containsKey( key ) ) {
            retBool =  userPropValues.get( key ).toString();
        } else {
            log.debug( "Property '" + key + "' not found in file " + allPropValues.get( AdminProperties.Prop.PROPERTIES_USER_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retBool; 
    }
    
    
    
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public boolean getResourceProp( String key, boolean fallBack ) {
        boolean retBool = fallBack;
        
        if ( resPropValues == null ) {
            if ( allPropValues.containsKey( PROPERTIES_RESOURCE ) ) {
                resPropValues = (Hashtable)allPropValues.get( PROPERTIES_RESOURCE );
            } else {
                log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_RESOURCE + "' not found in file: " + configdirname ) );
            }
        }
        
        if ( resPropValues != null && resPropValues.containsKey( key ) ) {
            retBool = Boolean.parseBoolean( resPropValues.get( key ).toString() ); 
        } else {
            log.debug( "Property '" + key + "' not found in file " + allPropValues.get( AdminProperties.Prop.PROPERTIES_RESOURCE_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retBool; 
    }
    
    
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public boolean getResourceGroupProp( String key, boolean fallBack ) {
        boolean retBool = fallBack;
        
        if ( resGroupPropValues == null ) {
            if ( allPropValues.containsKey( PROPERTIES_RESOURCE_GROUP ) ) {
                resGroupPropValues = (Hashtable)allPropValues.get( PROPERTIES_RESOURCE_GROUP );
            } else {
                log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_RESOURCE_GROUP + "' not found in file: " + configdirname ) );
            }
        }
        
        if ( resGroupPropValues != null && resGroupPropValues.containsKey( key ) ) {
            retBool = Boolean.parseBoolean( resGroupPropValues.get( key ).toString() ); 
        } else {
            log.debug( "Property '" + key + "' not found in file " + allPropValues.get( AdminProperties.Prop.PROPERTIES_RESOURCE_GROUP_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retBool; 
    }
    
    
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public int getRmiProp( String key, int fallBack ) {
        int retInt = fallBack;
        
        if ( rmiPropValues == null ) {
            if ( allPropValues.containsKey( PROPERTIES_RMI ) ) {
                rmiPropValues = (Hashtable)allPropValues.get( PROPERTIES_RMI );
            } else {
                log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_RMI + "' not found in file: " + configdirname ) );
            }
        }
        
        if ( rmiPropValues != null && rmiPropValues.containsKey( key ) ) {
            retInt = Integer.parseInt( rmiPropValues.get( key ).toString() ); 
        } else {
            log.debug( "Property '" + key + "' not found in file " + allPropValues.get( AdminProperties.Prop.PROPERTIES_RMI_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retInt; 
    }
    
    private void loadProps(final Properties sysprops) throws Exception {
        allPropValues.put( AdminProperties.Prop.ADMINDAEMON_LOGLEVEL, "ALL" );
        
        if ( sysprops.getProperty( "configdir" ) != null ) {
            configdirname = sysprops.getProperty("configdir");
            addpropsfromfile(configdirname + File.separatorChar + "AdminDaemon.properties");
        } else {
            log.fatal( "Parameter '-Dconfigdir' not given in system properties!" );
            log.fatal( "Now, using default parameter!" );
        }
    }
    
    protected void addpropsfromfile(final String file) throws FileNotFoundException, IOException {
        final Properties configprops  = new Properties();
        configprops.load( new FileInputStream(file) );
        
        Enumeration enumi = configprops.propertyNames();
        while ( enumi.hasMoreElements() ) {
            final String param = (String)enumi.nextElement();
            String value = configprops.getProperty( param );

            if ( value.startsWith( "$PWD" ) ) {
                // FIXME: Set a parsed value here instead of working dir
                // A new File without any content point to the current working dir
                value = stringReplacer( value, "$PWD", new File( "" ).getAbsolutePath() );
            }
            
            allPropValues.put( param, value );
            
            if ( param.toLowerCase().endsWith( "_prop" ) ) {
                try {
                    Properties customprops = new Properties();
                    customprops.load( new FileInputStream( value ) );
                    Enumeration enuma = customprops.propertyNames();
                    Hashtable<String, String> custconfig = new Hashtable<String, String>();
                    if ( allPropValues.containsKey( param + "_CONFIG" ) ) {
                        custconfig = (Hashtable)allPropValues.get( param + "_CONFIG" );
                    }
                    while ( enuma.hasMoreElements() ){
                        String param_ = (String)enuma.nextElement();
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
                    allPropValues.put( param + "_CONFIG", custconfig );
                } catch ( Exception e ) {
                    log.debug( "File not found. Use default values for the file: " + value, e );
                }
            }
        }
    }
    
    private String stringReplacer( String source, String find, String replacement ) {
        int i = 0;
        int j;
        int k = find.length();
        int m = replacement.length();
        
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
    

}
