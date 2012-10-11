package com.openexchange.mobile.configuration.generator.configuration;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.osgi.ServiceRegistry;


/**
 * Class for property handling
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class MobileConfigProperties {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MobileConfigProperties.class));

    /**
     * Fetches the property (convenience method)
     *
     * @param <T>
     * @param configuration
     * @param prop
     * @return
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T getProperty(final ServiceRegistry registry, final PropertyInterface prop) throws ConfigurationException {
        final ConfigurationService configuration = registry.getService(ConfigurationService.class);
        if (null == configuration) {
            throw new ConfigurationException("No configuration service found");
        }
        // Copy because otherwise compiler complains... :-(
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
     * Checks if all required properties are set and throws an exception if not. Also prints out the settings values
     * @param configuration the {@link ConfigurationService} from which the properties are read
     * @param props an array of props which should be checked
     * @param bundlename the bundlename (needed for output of the properties)
     *
     * @throws ConfigurationException
     */
    public static void check(final ServiceRegistry registry, final PropertyInterface[] props, final String bundlename) throws ConfigurationException {
        final ConfigurationService configuration = registry.getService(ConfigurationService.class);
        if (null == configuration) {
            throw new ConfigurationException("No configuration service found");
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("\nLoading global " + bundlename + " properties...\n");
        for (final PropertyInterface prop : props) {
            final Object property = getProperty(configuration, prop);
            sb.append('\t');
            sb.append(prop.getName());
            sb.append(": ");
            sb.append(property);
            sb.append('\n');
            if (Required.Value.TRUE.equals(prop.getRequired().getValue()) && null == property) {
                throw new ConfigurationException("Property " + prop.getName() + " not set but required.");
            }
            if (Required.Value.CONDITION.equals(prop.getRequired().getValue())) {
                final Condition[] condition = prop.getRequired().getCondition();
                if (null == condition || condition.length == 0) {
                    throw new ConfigurationException("Property " + prop.getName() + " claims to have condition but condition not set.");
                } else {
                    for (final Condition cond : condition) {
                        final PropertyInterface property3 = cond.getProperty();
                        final Object property2 = getProperty(configuration, property3);
                        final Object value = cond.getValue();
                        if (value.equals(property2) && null == property) {
                            throw new ConfigurationException("Property " + prop.getName() + " must be set if " + property3.getName() + " is set to " + value);
                        }
                    }
                }
            }
        }
        LOG.info(sb.toString());
    }

}
