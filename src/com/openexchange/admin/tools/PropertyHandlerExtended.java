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
    
    private final static Log log = LogFactory.getLog(PropertyHandlerExtended.class);
    
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
        } catch (final FileNotFoundException e) {
            log.error("Unable to read file: " + configfile);
        } catch (final IOException e) {
            log.error("Problems reading file: " + configfile);
        }
    }
    
    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    public String getSqlProp( final String key, final String fallBack ) {
        String retString = fallBack;
        
        if ( this.sqlPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_SQL ) ) {
                this.sqlPropValues = (Hashtable)this.allPropValues.get( PROPERTIES_SQL );
            } else {
                log.error( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_SQL + "' not found.") );
            }
        }
        
        if ( this.sqlPropValues != null && this.sqlPropValues.containsKey( key ) ) {
            retString = this.sqlPropValues.get( key ).toString(); 
        } else {
            log.error( "Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_SQL_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retString; 
    }

    /**
     * 
     * @param key
     * @param fallBack
     * @return
     */
    @Override
    public String getGroupProp( final String key, final String fallBack ) {
        String retBool = fallBack;
        
        if ( this.groupPropValues == null ) {
            if ( this.allPropValues.containsKey( PROPERTIES_GROUP ) ) {
                this.groupPropValues = (Hashtable)this.allPropValues.get( PROPERTIES_GROUP );
            } else {
                log.error( OXGenericException.GENERAL_ERROR, new Exception( "Property '" + PROPERTIES_GROUP + "' not found. ") );
            }
        }
        
        if ( this.groupPropValues != null && this.groupPropValues.containsKey( key ) ) {
            retBool =  this.groupPropValues.get( key ).toString(); 
        } else {
            log.error( "Property '" + key + "' not found in file " + this.allPropValues.get( AdminProperties.Prop.PROPERTIES_GROUP_FILE ) +"! Using fallback :" + fallBack );
        }
        
        return retBool; 
    }

}
