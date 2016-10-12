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
