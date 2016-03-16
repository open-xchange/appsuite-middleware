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

import java.nio.charset.Charset;
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
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountPOP3Properties(final MailAccount mailAccount) {
        super(mailAccount);
    }

    @Override
    public String getPOP3AuthEnc() {
        final String pop3AuthEncStr = properties.get("com.openexchange.pop3.pop3AuthEnc");
        if (null == pop3AuthEncStr) {
            return POP3Properties.getInstance().getPOP3AuthEnc();
        }

        if (Charset.isSupported(pop3AuthEncStr)) {
            return pop3AuthEncStr;
        }

        final String fallback = POP3Properties.getInstance().getPOP3AuthEnc();
        LOG.error("POP3 Authentication Encoding: Unsupported charset \"{}\". Setting to fallback: {}{}", pop3AuthEncStr, fallback, '\n');
        return fallback;
    }

    @Override
    public int getPOP3ConnectionIdleTime() {
        final String tmp = properties.get("com.openexchange.pop3.pop3ConnectionIdleTime");
        if (null == tmp) {
            return POP3Properties.getInstance().getPOP3ConnectionIdleTime();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("POP3 Connection Idle Time: Invalid value.", e);
            return POP3Properties.getInstance().getPOP3ConnectionIdleTime();
        }
    }

    @Override
    public int getPOP3BlockSize() {
        final String tmp = properties.get("com.openexchange.pop3.pop3BlockSize");
        if (null == tmp) {
            return POP3Properties.getInstance().getPOP3BlockSize();
        }

        try {
            final int blockSize = Integer.parseInt(tmp.trim());
            if (blockSize <= 0) {
                LOG.error("POP3 Block Size: Invalid value.");
                return POP3Properties.getInstance().getPOP3BlockSize();
            }
            return blockSize;
        } catch (final NumberFormatException e) {
            LOG.error("POP3 Block Size: Invalid value.", e);
            return POP3Properties.getInstance().getPOP3BlockSize();
        }
    }

    @Override
    public int getPOP3ConnectionTimeout() {
        final String tmp = properties.get("com.openexchange.pop3.pop3ConnectionTimeout");
        if (null == tmp) {
            return POP3Properties.getInstance().getPOP3ConnectionTimeout();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("POP3 Connection Timeout: Invalid value.", e);
            return POP3Properties.getInstance().getPOP3ConnectionTimeout();
        }
    }

    @Override
    public int getPOP3TemporaryDown() {
        final String tmp = properties.get("com.openexchange.pop3.pop3TemporaryDown");
        if (null == tmp) {
            return POP3Properties.getInstance().getPOP3TemporaryDown();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("POP3 Temporary Down: Invalid value.", e);
            return POP3Properties.getInstance().getPOP3TemporaryDown();
        }
    }

    @Override
    public int getPOP3Timeout() {
        final String tmp = properties.get("com.openexchange.pop3.pop3Timeout");
        if (null == tmp) {
            return POP3Properties.getInstance().getPOP3Timeout();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("POP3 Timeout: Invalid value.", e);
            return POP3Properties.getInstance().getPOP3Timeout();
        }
    }

    @Override
    public String getSSLProtocols() {
        final String tmp = properties.get("com.openexchange.pop3.ssl.protocols");
        if (null == tmp) {
            return POP3Properties.getInstance().getSSLProtocols();
        }

        return tmp.trim();
    }

    @Override
    public String getSSLCipherSuites() {
        final String tmp = properties.get("com.openexchange.pop3.ssl.ciphersuites");
        if (null == tmp) {
            return POP3Properties.getInstance().getSSLCipherSuites();
        }

        return tmp.trim();
    }

}
