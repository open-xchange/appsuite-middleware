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
 *     Copyright (C) 2016-2016 OX Software GmbH
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

package com.openexchange.mail.api;


/**
 * {@link UrlInfo} - The URL information of an end-point for mail access or mail transport.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class UrlInfo {

    private final String serverURL;
    private final boolean requireStartTls;

    /**
     * Initializes a new {@link UrlInfo}.
     */
    public UrlInfo(String serverURL, boolean requireStartTls) {
        super();
        this.serverURL = serverURL;
        this.requireStartTls = requireStartTls;
    }

    /**
     * Gets the server URL; e.g. <code>"imaps://imap.hosting.org:993"</code>
     * <p>
     * The URL is supposed to provide:
     * <ul>
     * <li>The protocol/scheme.<br>
     * This includes the plain and the secure protocol identifier; e.g. <code>"imap"</code> or <code>"imap<b>s</b>"</code><br>
     * If the protocol/scheme is absent, then the one configured through property <code>"com.openexchange.mail.defaultMailProvider"</code>
     * is used.
     * </li>
     * <li>The host name or IP address of the end-point</li>
     * <li>(Optional) The port number of the end-point</li>
     * </ul>
     *
     * @return The server URL
     */
    public String getServerURL() {
        return serverURL;
    }

    /**
     * Whether STARTTLS is required (in case no SSL socket is supposed to be established).
     * <p>
     * If the server's URL signals using an SSL socket (that is URL's protocol/scheme identifies a secure protocol such as
     * <code>"imap<b>s</b>"</code> or <code>"smtp<b>s</b>"</code>), then this flag has no meaning. But if the server's URL
     * lets us create a plain socket connection, then this flag determines whether the STARTTLS hand-shake is mandatory being
     * performed over that plain connection.
     *
     * @return <code>true</code> if STARTTLS is required; otherwise <code>false</code> to allow establishing a plain connection
     */
    public boolean isRequireStartTls() {
        return requireStartTls;
    }

}
