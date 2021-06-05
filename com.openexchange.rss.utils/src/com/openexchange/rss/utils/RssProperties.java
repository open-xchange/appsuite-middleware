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

package com.openexchange.rss.utils;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link RssProperties} - Provides access to commonly used configuration properties for RSS communication.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@SingletonService
public interface RssProperties {

    /** The name of the property to specify black-listed hosts */
    public static final String PROP_HOST_BLACKLIST = "com.openexchange.messaging.rss.feed.blacklist";

    /** The default listing of black-listed hosts */
    public static final String DEFAULT_HOST_BLACKLIST = "127.0.0.1-127.255.255.255,localhost";

    /**
     * Checks if specified host name is black-listed.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param hostName The host name; either a machine name or a textual representation of its IP address
     * @return <code>true</code> if black-listed; otherwise <code>false</code>
     */
    boolean isBlacklisted(String hostName);

    // ---------------------------------------- Allowed ports ------------------------------------------------------------------------------

    /** The name of the property to specify white-listed ports */
    public static final String PROP_PORT_WHITELIST = "com.openexchange.messaging.rss.feed.whitelist.ports";

    /** The default listing of white-listed ports */
    public static final String DEFAULT_PORT_WHITELIST = "80,443";

    /**
     * Checks if specified port is allowed.
     *
     * @param port The port to check
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    boolean isAllowed(int port);

    // ------------------------------------------ Allowed schemes --------------------------------------------------------------------------

    /** The name of the property to specify white-listed schemes */
    public static final String PROP_SCHEMES_WHITELIST = "com.openexchange.messaging.rss.feed.schemes";

    /** The default listing of white-listed schemes */
    public static final String DEFAULT_SCHEMES_WHITELIST = "http, https, ftp";

    /**
     * Checks whether the schemes is allowed
     *
     * @param scheme The scheme to check
     * @return <code>true</code> if the scheme is allowed, <code>false</code> otherwise
     */
    boolean isAllowedScheme(String scheme);

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if specified host name and port are denied to connect against.
     * <p>
     * The host name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP address.
     *
     * @param uriString The URI (as String) of the RSS feed
     * @return <code>true</code> if denied; otherwise <code>false</code>
     */
    boolean isDenied(String uriString);

}
