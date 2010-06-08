package com.openexchange.mobileconfig.configuration;

import com.openexchange.config.ConfigurationService;


public class MobileConfigProperties {
    public enum Property {
        iPhoneRegex(String.class, true, "com.openexchange.mobileconfig.iPhoneRegex"),
        WinMobRegex(String.class, true, "com.openexchange.mobileconfig.WinMobRegex"),
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
    public static <T extends Object> T getProperty(final ConfigurationService configuration, final Property prop) throws ConfigurationException {
        final Class<? extends Object> clazz = prop.getClazz();
        final String completePropertyName = prop.getName();
        if (String.class.equals(clazz)) {
            // no conversion done just output
            return (T) clazz.cast(configuration.getProperty(completePropertyName));
        } else if (Integer.class.equals(clazz)) {
            try {
                return (T) clazz.cast(Integer.valueOf(configuration.getProperty(completePropertyName)));
            } catch (final NumberFormatException e) {
                throw new ConfigurationException("The value given in the property " + completePropertyName + " is no integer value");
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
    public static void check(final ConfigurationService configuration) throws ConfigurationException {
        for (final MobileConfigProperties.Property prop : MobileConfigProperties.Property.values()) {
            final Object property = MobileConfigProperties.getProperty(configuration, prop);
            if (prop.isRequired() && null == property) {
                throw new ConfigurationException("Property " + prop.getName() + " not set but required.");
            }
        }

    }

}
