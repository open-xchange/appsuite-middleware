package com.openexchange.groupware.notify;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.tools.conf.AbstractConfig;
import com.openexchange.configuration.ConfigurationException.Code;
import com.openexchange.configuration.SystemConfig.Property;


public class NotificationConfig extends AbstractConfig {

	private static final Log LOG = LogFactory.getLog(NotificationConfig.class);
	
	private static final Property KEY = Property.NOTIFICATION;
	
	enum NotificationProperty{
		
		NOTIFY_ON_DELETE("notify_participants_on_delete"),
		OBJECT_LINK("object_link");
		
		
		private String name;
		
		private NotificationProperty(final String name){
			this.name = name;
		}
		
		public String getName(){
			return name;
		}
		
	}
	
	private static final NotificationConfig INSTANCE = new NotificationConfig();
	
	@Override
	protected String getPropertyFileName() throws ConfigurationException {
		final String filename = SystemConfig.getProperty(KEY);
        if (null == filename) {
            throw new ConfigurationException(Code.PROPERTY_MISSING,
                KEY.getPropertyName());
        }
        return filename;
	}
	
	public static String getProperty(final NotificationProperty prop, final String def) {
		if(!INSTANCE.isPropertiesLoadInternal()) {
			try {
				INSTANCE.loadPropertiesInternal();
			} catch (final ConfigurationException e) {
				LOG.error(e);
				return def;
			}
		}
		if(!INSTANCE.isPropertiesLoadInternal()) {
			return def;
		}
		return INSTANCE.getPropertyInternal(prop.getName(), def);
	}
	
	public static boolean getPropertyAsBoolean(final NotificationProperty prop, final boolean def) {
		final String boolVal = getProperty(prop,null);
		if(boolVal == null) {
			return def;
		}
		return Boolean.parseBoolean(boolVal);
	}

}
