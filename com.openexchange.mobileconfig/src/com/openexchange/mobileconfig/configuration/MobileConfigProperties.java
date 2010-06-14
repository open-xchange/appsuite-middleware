package com.openexchange.mobileconfig.configuration;

import com.openexchange.config.ConfigurationService;


public class MobileConfigProperties {
    
    public interface PropertyInterface {
        
        public Class<? extends Object> getClazz();
        
        public boolean isRequired();
        
        public String getName();
    }

    public enum Property implements PropertyInterface {
        iPhoneRegex(String.class, true, "com.openexchange.mobileconfig.iPhoneRegex"),
        WinMobRegex(String.class, true, "com.openexchange.mobileconfig.WinMobRegex"),
        OnlySecureConnect(Boolean.class, true, "com.openexchange.mobileconfig.OnlySecureConnect"),
        DomainUser(String.class, true, "com.openexchange.usm.eas.login_pattern.domain_user");
        
        private final Class<?> clazz;
        
        private final boolean required;
        
        private final String name;
        
        private Property(final Class<?> clazz, final boolean required, final String name) {
            this.clazz = clazz;
            this.required = required;
            this.name = name;
        }

        public Class<? extends Object> getClazz() {
            return clazz;
        }

        public boolean isRequired() {
            return required;
        }

        public String getName() {
            return name;
        }

    }

    /**
     * Fetches the property
     * 
     * @param <T>
     * @param configuration
     * @param prop
     * @return
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T getProperty(final ConfigurationService configuration, final PropertyInterface prop) throws ConfigurationException {
        final Class<? extends Object> clazz = prop.getClazz();
        final String completePropertyName = prop.getName();
        final String property = configuration.getProperty(completePropertyName);
        if (null != property && property.length() != 0) {
            if (String.class.equals(clazz)) {
                // no conversion done just output
                return (T) clazz.cast(property);
            } else if (Integer.class.equals(clazz)) {
                try {
                    return (T) clazz.cast(Integer.valueOf(property));
                } catch (final NumberFormatException e) {
                    throw new ConfigurationException("The value given in the property " + completePropertyName + " is no integer value");
                }
            } else if (Boolean.class.equals(clazz)) {
                return (T) clazz.cast(Boolean.valueOf(property));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * Checks if all required properties are set and throws an exception if not
     * 
     * @throws ConfigurationException
     */
    public static void check(final ConfigurationService configuration, final PropertyInterface[] props) throws ConfigurationException {
        for (final PropertyInterface prop : props) {
            final Object property = getProperty(configuration, prop);
            if (prop.isRequired() && null == property) {
                throw new ConfigurationException("Property " + prop.getName() + " not set but required.");
            }
        }

    }

}
