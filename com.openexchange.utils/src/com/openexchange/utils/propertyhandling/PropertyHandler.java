
package com.openexchange.utils.propertyhandling;

import javax.naming.ConfigurationException;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.server.ServiceLookup;
import com.openexchange.utils.propertyhandling.internal.ConfigurationExceptionCodes;


/**
 * Class for property handling
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class PropertyHandler {

    private static final Log LOG = LogFactory.getLog(PropertyHandler.class);

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
    public static <T extends Object> T getProperty(final ServiceLookup registry, final PropertyInterface prop) throws OXException {
        final ConfigurationService configuration = registry.getService(ConfigurationService.class);
        if (null == configuration) {
            throw ConfigurationExceptionCodes.NO_CONFIGURATION_SERVICE_FOUND.create();
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
                    throw ConfigurationExceptionCodes.NO_INTEGER_VALUE.create(completePropertyName);
                }
            } else if (Boolean.class.equals(clazz)) {
                return (T) clazz.cast(Boolean.valueOf(property));
            } else if (Enum.class.equals(clazz.getSuperclass())) {
                try {
                    final Enum<?> valueOf = Enum.valueOf(clazz.asSubclass(Enum.class), property);
                    return (T) valueOf;
                } catch (final IllegalArgumentException e) {
                    return null;
                }
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
    public static <T extends Object> T getProperty(final ConfigurationService configuration, final PropertyInterface prop) throws OXException {
        if (null == configuration) {
            throw ConfigurationExceptionCodes.NO_CONFIGURATION_SERVICE_FOUND.create();
        }

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
                    throw ConfigurationExceptionCodes.NO_INTEGER_VALUE.create(completePropertyName);
                }
            } else if (Boolean.class.equals(clazz)) {
                return (T) clazz.cast(Boolean.valueOf(property));
            } else if (Enum.class.equals(clazz.getSuperclass())) {
                try {
                    final Enum<?> valueOf = Enum.valueOf(clazz.asSubclass(Enum.class), property);
                    return (T) valueOf;
                } catch (final IllegalArgumentException e) {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Checks if all required properties are set and throws an exception if not. Also prints out the settings values
     * @param registry the {@link ServiceLookup} to get the {@link ConfigurationService}
     * @param props an array of props which should be checked
     * @param bundlename the bundlename (needed for output of the properties)
     * 
     * @throws ConfigurationException
     */
    public static void check(final ServiceLookup registry, final PropertyInterface[] props, final String bundlename) throws OXException {
        final ConfigurationService configuration = registry.getService(ConfigurationService.class);
        check(configuration, props, bundlename);
    }

    /**
     * Checks if all required properties are set and throws an exception if not. Also prints out the settings values
     * @param configuration the {@link ConfigurationService} from which the properties are read
     * @param props an array of props which should be checked
     * @param bundlename the bundlename (needed for output of the properties)
     * 
     * @throws ConfigurationException
     */
    public static void check(final ConfigurationService configuration, final PropertyInterface[] props, final String bundlename) throws OXException {
        if (null == configuration) {
            throw ConfigurationExceptionCodes.NO_CONFIGURATION_SERVICE_FOUND.create();
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("\nLoading global " + bundlename + " properties...\n");
        for (final PropertyInterface prop : props) {
            final Object property = getProperty(configuration, prop);
            if (prop.isLog()) {
                sb.append("\t");
                sb.append(prop.getName());
                sb.append(": ");
                sb.append(property);
                sb.append("\n");
            }
            if (Required.Value.TRUE.equals(prop.getRequired().getValue()) && null == property) {
                throw ConfigurationExceptionCodes.REQUIRED_PROPERTY_NOT_SET.create(prop.getName());
            }
            if (Required.Value.CONDITION.equals(prop.getRequired().getValue())) {
                final Condition[] condition = prop.getRequired().getCondition();
                if (null == condition || condition.length == 0) {
                    throw ConfigurationExceptionCodes.CONDITION_NOT_SET.create(prop.getName());
                } else {
                    for (final Condition cond : condition) {
                        final PropertyInterface property3 = cond.getProperty();
                        final Object property2 = getProperty(configuration, property3);
                        final Object value = cond.getValue();
                        if (value.equals(property2) && null == property) {
                            throw ConfigurationExceptionCodes.MUST_BE_SET_TO.create(prop.getName(),property3.getName(),value);
                        }
                    }
                }
            }
        }
        LOG.info(sb.toString());
    }

}
