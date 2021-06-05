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

package com.openexchange.database.internal;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.database.JdbcProperties;

/**
 * {@link JdbcPropertiesImpl} - Provides the currently active JDBC properties as specified in <code>"dbconnector.yaml"</code> file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class JdbcPropertiesImpl implements JdbcProperties {

    private static final JdbcPropertiesImpl INSTANCE = new JdbcPropertiesImpl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static JdbcPropertiesImpl getInstance() {
        return INSTANCE;
    }

    /**
     * Removes possible parameters appended to specified JDBC URL and returns it.
     *
     * @param url The URL to remove possible parameters from
     * @return The parameter-less JDBC URL
     */
    public static String doRemoveParametersFromJdbcUrl(String url) {
        if (null == url) {
            return url;
        }

        int paramStart = url.indexOf('?');
        return paramStart >= 0 ? url.substring(0, paramStart) : url;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<Properties> jdbcPropsReference;

    /**
     * Initializes a new {@link JdbcPropertiesImpl}.
     */
    private JdbcPropertiesImpl() {
        super();
        jdbcPropsReference = new AtomicReference<Properties>();
    }

    /**
     * Gets the reference to the currently active JDBC properties.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Modifying the returned <code>java.util.Properties</code> instance is reflected in JDBC properties
     * </div>
     *
     * @return The JDBC properties or <code>null</code> if not yet initialized
     */
    @Override
    public Properties getJdbcPropertiesRaw() {
        return jdbcPropsReference.get();
    }

    /**
     * Gets a copy of the currently active JDBC properties.
     *
     * @return The JDBC properties copy or <code>null</code> if not yet initialized
     */
    @Override
    public Properties getJdbcPropertiesCopy() {
        Properties properties = jdbcPropsReference.get();
        if (null == properties) {
            return null;
        }

        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    /**
     * Sets the JDBC properties to use.
     *
     * @param jdbcProperties The JDBC properties or <code>null</code> to clear them
     */
    public void setJdbcProperties(Properties jdbcProperties) {
        jdbcPropsReference.set(jdbcProperties);
    }

}
