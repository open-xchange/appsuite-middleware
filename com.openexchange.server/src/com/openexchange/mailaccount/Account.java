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

package com.openexchange.mailaccount;

import java.io.Serializable;

/**
 * {@link Account}
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
    public String getName();

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
    public String getPersonal();

    /**
     * Gets the reply-to address.
     *
     * @return The reply-to address
     */
    public String getReplyTo();

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
    public boolean isTransportSecure();

    /**
     * Checks if this mail account is a default account.
     *
     * @return <code>true</code> if this mail account is a default account; otherwise <code>false</code>
     */
    boolean isDefaultAccount();

    /**
     * Checks if STARTTLS should be used to connect to transport server
     *
     * @return
     */
    boolean isTransportStartTls();

    /**
     * Checks if this transport account is able to authenticate via oauth or not.
     * 
     * @return true if the account is able to authenticate via oauth, otherwise false.
     */
    boolean isOAuthAble();

    /**
     * Retrieves the oauthAccount identifier.
     * 
     * @return The oauthAccount id or null;
     */
    Long getOAuthID();

}
