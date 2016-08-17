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

package com.openexchange.imap;

import java.io.IOException;
import java.util.Map;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.ping.IMAPCapabilityAndGreetingCache;
import com.openexchange.mail.MailExceptionCode;

/**
 * {@link IMAPServerInfo} - IMAP server information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class IMAPServerInfo {

    /**
     * Gets the IMAP server information for given configuration
     *
     * @param imapConfig The configuration
     * @param accountId The account identifier
     * @return The IMAP server information
     * @throws OXException If IMAP server information cannot be returned
     */
    public static IMAPServerInfo instanceFor(IMAPConfig imapConfig, int accountId) throws OXException {
        try {
            String serverUrl = new StringBuilder(36).append(IDNA.toASCII(imapConfig.getServer())).append(':').append(imapConfig.getPort()).toString();
            String greeting = IMAPCapabilityAndGreetingCache.getGreeting(serverUrl, imapConfig.isSecure(), imapConfig.getIMAPProperties());
            Map<String, String> capabilities = imapConfig.asMap();
            return new IMAPServerInfo(greeting, capabilities, accountId);
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private final String greeting;
    private final Map<String, String> capabilities;
    private final int accountId;

    /**
     * Initializes a new {@link IMAPServerInfo}.
     */
    private IMAPServerInfo(String greeting, Map<String, String> capabilities, int accountId) {
        super();
        this.greeting = greeting;
        this.capabilities = capabilities;
        this.accountId = accountId;
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the greeting
     *
     * @return The greeting
     */
    public String getGreeting() {
        return greeting;
    }

    /**
     * Gets the capabilities
     *
     * @return The capabilities
     */
    public Map<String, String> getCapabilities() {
        return capabilities;
    }

}
