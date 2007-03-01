package com.openexchange.admin.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.properties.AdminProperties;

public class PropertyHandlerExtended extends PropertyHandler {
    
    private Log log = LogFactory.getLog(this.getClass());
    
    private Hashtable       sqlPropValues       = null;

    private static final String PROPERTIES_SQL              = "SQL_PROP_CONFIG";

    // The following lines define the property values for the database implementations
    public static final String CONTEXT_STORAGE = "CONTEXT_STORAGE";
    public static final String UTIL_STORAGE = "UTIL_STORAGE";

    private PropertyHandlerExtended() {
        super(null);
    }
    
    public PropertyHandlerExtended(final Properties sysprops) {
        super(sysprops);
        final StringBuilder configfile = new StringBuilder(); 
        configfile.append(sysprops.getProperty("configdir"));
        configfile.append(File.separatorChar);
        configfile.append("plugin");
        configfile.append(File.separatorChar);
        configfile.append("hosting.properties");
        try {
            addpropsfromfile(configfile.toString());
        } catch (FileNotFoundException e) {
            log.fatal("Unable to read file: " + configfile);
        } catch (IOException e) {
            log.fatal("Problems reading file: " + configfile);
        }
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public String getSqlProp( String key, String fallBack ) {
        String retString = fallBack;
        
        if ( sqlPropValues == null ) {
            if ( allPropValues.containsKey( PROPERTIES_SQL ) ) {
                sqlPropValues = (Hashtable)allPropValues.get( PROPERTIES_SQL );
            } else {
                log.error( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_SQL + "' not found.") );
            }
        }
        
        if ( sqlPropValues != null && sqlPropValues.containsKey( key ) ) {
            retString = sqlPropValues.get( key ).toString(); 
        } else {
            log.debug( "Property '" + key + "' not found in file " + allPropValues.get( AdminProperties.Prop.PROPERTIES_SQL_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retString; 
    }

    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public String getGroupProp( String key, String fallBack ) {
        String retBool = fallBack;
        
        if ( groupPropValues == null ) {
            if ( allPropValues.containsKey( PROPERTIES_GROUP ) ) {
                groupPropValues = (Hashtable)allPropValues.get( PROPERTIES_GROUP );
            } else {
                log.debug( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_GROUP + "' not found. ") );
            }
        }
        
        if ( groupPropValues != null && groupPropValues.containsKey( key ) ) {
            retBool =  groupPropValues.get( key ).toString(); 
        } else {
            log.debug( "Property '" + key + "' not found in file " + allPropValues.get( AdminProperties.Prop.PROPERTIES_GROUP_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retBool; 
    }

}
