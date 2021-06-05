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

package com.openexchange.mailaccount;

import java.io.Serializable;

/**
 * {@link Account} - The super interface for both - mail and transport account.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public interface Account extends Serializable {

    /**
     * The ID to identify a default mail account.
     */
    public static final int DEFAULT_ID = 0;

    /**
     * The ID to identify a default mail account.
     */
    public static final String DEFAULT_ID_STR = Integer.toString(DEFAULT_ID);

    /**
     * Generates the transport server URL; e.g. <code>&quot;smtp://smtp.somewhere.com:225&quot;</code>.
     *
     * @return The generated transport server URL
     */
    String generateTransportServerURL();

    /**
     * Gets the unique ID of this mail account.
     *
     * @return The unique ID of this mail account
     */
    int getId();

    /**
     * Gets the login.
     *
     * @return The login
     */
    String getLogin();

    /**
     * Gets the (display) name of this mail account; e.g. <code>&quot;My mail account&quot;</code>.
     *
     * @return The (display) name
     */
    String getName();

    /**
     * Gets the password.
     * <p>
     * Beware that password might be encoded when fetching from storage. Use one of the <code>decrypt()</code> methods of
     * {@link com.openexchange.mail.utils.MailPasswordUtil MailPasswordUtil} plus session password to obtain plain-text password.
     *
     * @return The encoded password
     */
    String getPassword();

    /**
     * Gets the primary email address.
     *
     * @return The primary email address
     */
    String getPrimaryAddress();

    /**
     * Gets the personal part of primary email address; e.g.<br>
     * <code>Jane Doe &lt;jane.doe@somewhere.com&gt;</code>
     *
     * @return The personal
     */
    String getPersonal();

    /**
     * Gets the reply-to address.
     *
     * @return The reply-to address
     */
    String getReplyTo();

    /**
     * Gets the transport-auth value.
     *
     * @return The transport-auth value
     */
    TransportAuth getTransportAuth();

    /**
     * Gets the transport login
     *
     * @return The transport login
     */
    String getTransportLogin();

    /**
     * Gets the transport password
     *
     * @return The transport password
     */
    String getTransportPassword();

    /**
     * Gets the transport server port.
     *
     * @return The transport server port
     */
    int getTransportPort();

    /**
     * Gets the transport server protocol.
     *
     * @return The transport server protocol
     */
    String getTransportProtocol();

    /**
     * Gets the transport server name.
     * <p>
     * The transport server name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP
     * address.
     *
     * @return The transport server name
     */
    String getTransportServer();

    /**
     * Checks if a secure connection to transport server shall be established.
     *
     * @return <code>true</code> if a secure connection to transport server shall be established; otherwise <code>false</code>
     */
    boolean isTransportSecure();

    /**
     * Checks if this mail account is a default account.
     *
     * @return <code>true</code> if this mail account is a default account; otherwise <code>false</code>
     */
    boolean isDefaultAccount();

    /**
     * Checks if STARTTLS should be used to connect to transport server
     *
     * @return <code>true</code> if STARTTLS is mandatory; otherwise <code>false</code>
     */
    boolean isTransportStartTls();

    /**
     * Checks if transport server expects to authenticate via OAuth or not.
     *
     * @return <code>true</code> for OAuth authentication, otherwise <code>false</code>.
     */
    boolean isTransportOAuthAble();

    /**
     * Gets the identifier of the associated OAuth account (if any) to authenticate against transport server.
     *
     * @return The OAuth account identifier or <code>-1</code> if there is no associated OAuth account
     */
    int getTransportOAuthId();

    /**
     * Checks whether mail transport is disabled
     *
     * @return <code>true</code> if disabled; otherwise <code>false</code>
     */
    boolean isTransportDisabled();

}
