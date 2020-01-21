/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.oidc.http.outbound;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;

/**
 * {@link HttpConfig} - Permits setting of HTTP connect and read timeouts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class HttpConfig {

    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private static final int DEFAULT_READ_TIMEOUT = 15000;

    private static final AtomicReference<HttpConfig> INSTANCE_REFERENCE = new AtomicReference<>(new HttpConfig(DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT));

    /**
     * Gets the currently active HTTP configuration.
     *
     * @return The currently active HTTP configuration
     */
    public static HttpConfig getInstance() {
        return INSTANCE_REFERENCE.get();
    }

    /**
     * Resets the HTTP configuration.
     */
    public static void resetInstance() {
        INSTANCE_REFERENCE.set(new HttpConfig(DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT));
    }

    /**
     * Initializes the HTTP configuration using view from given configuration service.
     *
     * @param configService The configuration service
     */
    public static void initInstance(ConfigurationService configService) {
        int connectTimeout = optIntProperty("com.openexchange.oidc.http.outbound.connectTimeout", 5000, configService);
        int readTimeout = optIntProperty("com.openexchange.oidc.http.outbound.readTimeout", 15000, configService);

        INSTANCE_REFERENCE.set(new HttpConfig(connectTimeout, readTimeout));
    }

    /**
     * Gets the specified integer property.
     *
     * @param property The suffix/name of the property
     * @param def The default integer value to return if no such property exists
     * @param nameBuilder The name builder to use
     * @param config The configuration service to retrieve the value from
     * @return The integer property value or <code>def</code>
     */
    private static int optIntProperty(String property, int def, ConfigurationService configService) {
        String value = configService.getProperty(property);
        if (Strings.isNotEmpty(value)) {
            int intVal = Tools.getUnsignedInteger(value.trim());
            if (intVal >= 0) {
                return intVal;
            }
        }
        return def;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /** The HTTP connect timeout, in milliseconds. */
    private final int connectTimeout;

    /** The HTTP read timeout, in milliseconds. */
    private final int readTimeout;

    /**
     * Initializes a new {@link HttpConfig}.
     *
     * @param connectTimeout The HTTP connect timeout, in milliseconds
     * @param readTimeout The HTTP read timeout, in milliseconds
     */
    private HttpConfig(int connectTimeout, int readTimeout) {
        super();
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Gets the HTTP connect timeout.
     *
     * @return The HTTP connect timeout, in milliseconds, zero for
     *         infinite.
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Gets the HTTP read timeout.
     *
     * @return The HTTP read timeout, in milliseconds, zero for infinite.
     */
    public int getReadTimeout() {
        return readTimeout;
    }

}
