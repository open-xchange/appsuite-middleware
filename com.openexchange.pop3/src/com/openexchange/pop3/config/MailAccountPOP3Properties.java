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

package com.openexchange.pop3.config;

import com.openexchange.mail.config.MailAccountProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountPOP3Properties} - POP3 properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountPOP3Properties extends MailAccountProperties implements IPOP3Properties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccountPOP3Properties.class);

    /**
     * Initializes a new {@link MailAccountPOP3Properties}.
     *
     * @param mailAccount The mail account
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountPOP3Properties(MailAccount mailAccount, int userId, int contextId) {
        super(mailAccount, userId, contextId);
    }

    @Override
    public String getPOP3AuthEnc() {
        return lookUpProperty("com.openexchange.pop3.pop3AuthEnc", POP3Properties.getInstance().getPOP3AuthEnc());
    }

    @Override
    public int getPOP3ConnectionIdleTime() {
        return lookUpIntProperty("com.openexchange.pop3.pop3ConnectionIdleTime", POP3Properties.getInstance().getPOP3ConnectionIdleTime());
    }

    @Override
    public int getPOP3BlockSize() {
        return lookUpIntProperty("com.openexchange.pop3.pop3BlockSize", POP3Properties.getInstance().getPOP3BlockSize());
    }

    @Override
    public int getPOP3ConnectionTimeout() {
        return lookUpIntProperty("com.openexchange.pop3.pop3ConnectionTimeout", POP3Properties.getInstance().getPOP3ConnectionTimeout());
    }

    @Override
    public int getPOP3TemporaryDown() {
        return lookUpIntProperty("com.openexchange.pop3.pop3TemporaryDown", POP3Properties.getInstance().getPOP3TemporaryDown());
    }

    @Override
    public int getPOP3Timeout() {
        return lookUpIntProperty("com.openexchange.pop3.pop3Timeout", POP3Properties.getInstance().getPOP3Timeout());
    }

    @Override
    public String getSSLProtocols() {
        return lookUpProperty("com.openexchange.pop3.ssl.protocols", POP3Properties.getInstance().getSSLProtocols());
    }

    @Override
    public String getSSLCipherSuites() {
        return lookUpProperty("com.openexchange.pop3.ssl.ciphersuites", POP3Properties.getInstance().getSSLCipherSuites());
    }

}
