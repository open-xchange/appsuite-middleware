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

package com.openexchange.mail.autoconfig;

/**
 * {@link Autoconfig} - Represents an auto-configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Autoconfig {

    /**
     * Gets the source
     *
     * @return The source
     */
    String getSource();

    /**
     * Gets the mail server
     *
     * @return The mail server
     */
    String getMailServer();

    /**
     * Gets the transport server
     *
     * @return The transport server
     */
    String getTransportServer();

    /**
     * Gets the mail protocol
     *
     * @return The mail protocol
     */
    String getMailProtocol();

    /**
     * Gets the transport protocol
     *
     * @return The transport protocol
     */
    String getTransportProtocol();

    /**
     * Gets the mail port
     *
     * @return The mail port
     */
    Integer getMailPort();

    /**
     * Gets the transport port
     *
     * @return The transport port
     */
    Integer getTransportPort();

    /**
     * Gets the mail secure flag
     *
     * @return The mail secure flag
     */
    Boolean isMailSecure();

    /**
     * Gets the transport secure flag
     *
     * @return The transport secure flag
     */
    Boolean isTransportSecure();

    /**
     * Gets the user name
     *
     * @return The user name
     */
    String getUsername();

    /**
     * Checks if STARTTLS is required for mail access.
     *
     * @return <code>true</code> if STARTTLS is required; otherwise <code>false</code>
     */
    boolean isMailStartTls();

    /**
     * Checks if STARTTLS is required for mail transport
     *
     * @return <code>true</code> if STARTTLS is required; otherwise <code>false</code>
     */
    boolean isTransportStartTls();

    /**
     * Gets the optional identifier for the OAuth account for mail access.
     *
     * @return The optional OAuth account identifier or <code>null</code>
     */
    Integer getMailOAuthId();

    /**
     * Gets the optional identifier for the OAuth account for mail transport.
     *
     * @return The optional OAuth account identifier or <code>null</code>
     */
    Integer getTransportOAuthId();

}
