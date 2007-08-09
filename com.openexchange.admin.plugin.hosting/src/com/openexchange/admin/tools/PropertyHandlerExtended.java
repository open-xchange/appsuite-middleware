package com.openexchange.admin.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyHandlerExtended extends PropertyHandler {
    
    private final static Log log = LogFactory.getLog(PropertyHandlerExtended.class);
    
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
   
}
