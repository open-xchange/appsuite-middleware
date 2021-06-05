/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.config.lean;

import java.util.List;
import java.util.Map;
import com.openexchange.config.PropertyFilter;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link LeanConfigurationService} - A service combining the ConfigView (config cascade) and ConfigurationService
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.4
 */
@SingletonService
public interface LeanConfigurationService {

    /**
     * Fetches the string value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The string value of the property
     */
    String getProperty(Property property);

    /**
     * Fetches the integer value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The integer value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Integer</code>
     */
    int getIntProperty(Property property);

    /**
     * Fetches the boolean value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The boolean value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Boolean</code>
     */
    boolean getBooleanProperty(Property property);

    /**
     * Fetches the float value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The float value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Float</code>
     */
    float getFloatProperty(Property property);

    /**
     * Fetches the long value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @return The long value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Long</code>
     */
    long getLongProperty(Property property);

    /**
     * Fetches the {@link String} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link String} value of the property
     */
    String getProperty(int userId, int contextId, Property property);

    /**
     * Fetches the {@link Integer} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link Integer} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Integer</code>
     */
    int getIntProperty(int userId, int contextId, Property property);

    /**
     * Fetches the {@link Boolean} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link Boolean} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Boolean</code>
     */
    boolean getBooleanProperty(int userId, int contextId, Property property);

    /**
     * Fetches the {@link Float} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link Float} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Float</code>
     */
    float getFloatProperty(int userId, int contextId, Property property);

    /**
     * Fetches the {@link Long} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @return The {@link Long} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Long</code>
     */
    long getLongProperty(int userId, int contextId, Property property);

    /**
     * Fetches the string value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The string value of the property
     */
    String getProperty(Property property, Map<String, String> optionals);

    /**
     * Fetches the integer value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     *
     * @return The integer value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Integer</code>
     */
    int getIntProperty(Property property, Map<String, String> optionals);

    /**
     * Fetches the boolean value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The boolean value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Boolean</code>
     */
    boolean getBooleanProperty(Property property, Map<String, String> optionals);

    /**
     * Fetches the float value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The float value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Float</code>
     */
    float getFloatProperty(Property property, Map<String, String> optionals);

    /**
     * Fetches the long value of the {@link Property}. If the property is not found, then the default value
     * of that property is returned.
     *
     * @param property The {@link Property} to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The long value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Long</code>
     */
    long getLongProperty(Property property, Map<String, String> optionals);

    /////////////////////////////////////// CONFIG VIEW AWARE /////////////////////////////////

    /**
     * Fetches the {@link String} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link String} value of the property
     */
    String getProperty(int userId, int contextId, Property property, Map<String, String> optionals);

    /**
     * Fetches the {@link Integer} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link Integer} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Integer</code>
     */
    int getIntProperty(int userId, int contextId, Property property, Map<String, String> optionals);

    /**
     * Fetches the {@link Boolean} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link Boolean} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Boolean</code>
     */
    boolean getBooleanProperty(int userId, int contextId, Property property, Map<String, String> optionals);

    /**
     * Fetches the {@link Float} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link Float} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Float</code>
     */
    float getFloatProperty(int userId, int contextId, Property property, Map<String, String> optionals);

    /**
     * Fetches the {@link Long} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link Long} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Long</code>
     */
    long getLongProperty(int userId, int contextId, Property property, Map<String, String> optionals);

    //////////////////////////////////////// CONFIG VIEW SCOPE AWARE ///////////////////////////////

    /**
     * Fetches the {@link String} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param scopes The list of scopes that should be considered,
     *            with first element being the most specific and last element being the least specific scope
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link String} value of the property
     */
    String getProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals);

    /**
     * Fetches the {@link Integer} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param scopes The list of scopes that should be considered,
     *            with first element being the most specific and last element being the least specific scope
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link Integer} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Integer</code>
     */
    int getIntProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals);

    /**
     * Fetches the {@link Boolean} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param scopes The list of scopes that should be considered,
     *            with first element being the most specific and last element being the least specific scope
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link Boolean} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Boolean</code>
     */
    boolean getBooleanProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals);

    /**
     * Fetches the {@link Float} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param scopes The list of scopes that should be considered,
     *            with first element being the most specific and last element being the least specific scope
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link Float} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Float</code>
     */
    float getFloatProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals);

    /**
     * Fetches the {@link Long} value of specified {@link Property} for
     * the specified user in the specified context via ConfigCascade
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param property The {@link Property} name to fetch
     * @param scopes The list of scopes that should be considered,
     *            with first element being the most specific and last element being the least specific scope
     * @param optionals A {@link Map} containing optional path parameters. The parameters will be used to replace
     *            optional parameters in the full qualified name of the property with the value stored for
     *            each path parameter.
     * @return The {@link Long} value of the property
     * @throws IllegalArgumentException If value cannot be converted to <code>Long</code>
     */
    long getLongProperty(int userId, int contextId, Property property, List<String> scopes, Map<String, String> optionals);

    /**
     * Returns all properties that fulfill the given filter's acceptance criteria.
     *
     * @param filter The property filter
     * @return The appropriate properties or an empty {@link Map} if no properties were matched
     */
    Map<String, String> getProperties(PropertyFilter propertyFilter);
}
