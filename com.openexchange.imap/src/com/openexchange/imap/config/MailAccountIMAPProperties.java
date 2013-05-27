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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.imap.config;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.mail.api.MailConfig.BoolCapVal;
import com.openexchange.mail.config.MailAccountProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountIMAPProperties} - IMAP properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountIMAPProperties extends MailAccountProperties implements IIMAPProperties {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAccountIMAPProperties.class));

    /**
     * Initializes a new {@link MailAccountIMAPProperties}.
     *
     * @param mailAccount The mail account
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountIMAPProperties(final MailAccount mailAccount) {
        super(mailAccount);
    }

    @Override
    public int getBlockSize() {
        final String blockSizeStr = properties.get("com.openexchange.imap.blockSize");
        if (null == blockSizeStr) {
            return IMAPProperties.getInstance().getBlockSize();
        }

        try {
            return Integer.parseInt(blockSizeStr.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Block Size: Invalid value.", e);
            return IMAPProperties.getInstance().getBlockSize();
        }
    }

    @Override
    public int getMaxNumConnection() {
        final String tmp = properties.get("com.openexchange.imap.maxNumConnections");
        if (null == tmp) {
            return IMAPProperties.getInstance().getMaxNumConnection();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Max. Number of connections: Invalid value.", e);
            return IMAPProperties.getInstance().getMaxNumConnection();
        }
    }

    @Override
    public String getImapAuthEnc() {
        String imapAuthEncStr = properties.get("com.openexchange.imap.imapAuthEnc");
        if (null == imapAuthEncStr) {
            return IMAPProperties.getInstance().getImapAuthEnc();
        }

        imapAuthEncStr = imapAuthEncStr.trim();
        if (Charset.isSupported(imapAuthEncStr)) {
            return imapAuthEncStr;
        }
        final String fallback = IMAPProperties.getInstance().getImapAuthEnc();
        LOG.error(new com.openexchange.java.StringAllocator(64).append("Authentication Encoding: Unsupported charset \"").append(imapAuthEncStr).append(
            "\". Setting to fallback: ").append(fallback));
        return fallback;
    }

    @Override
    public int getImapConnectionTimeout() {
        final String tmp = properties.get("com.openexchange.imap.imapConnectionTimeout");
        if (null == tmp) {
            return IMAPProperties.getInstance().getImapConnectionTimeout();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("IMAP Connection Timeout: Invalid value.", e);
            return IMAPProperties.getInstance().getImapConnectionTimeout();
        }
    }

    @Override
    public int getImapTemporaryDown() {
        final String tmp = properties.get("com.openexchange.imap.imapTemporaryDown");
        if (null == tmp) {
            return IMAPProperties.getInstance().getImapTemporaryDown();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("IMAP Temporary Down: Invalid value.", e);
            return IMAPProperties.getInstance().getImapTemporaryDown();
        }
    }

    @Override
    public int getNotifyFrequencySeconds() {
        final String tmp = properties.get("com.openexchange.imap.notifyFrequencySeconds");
        if (null == tmp) {
            return IMAPProperties.getInstance().getNotifyFrequencySeconds();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Notify Frequency Seconds: Invalid value.", e);
            return IMAPProperties.getInstance().getNotifyFrequencySeconds();
        }
    }

    @Override
    public String getNotifyFullNames() {
        final String tmp = properties.get("com.openexchange.imap.notifyFullNames");
        if (null == tmp) {
            return IMAPProperties.getInstance().getNotifyFullNames();
        }

        return tmp.trim();
    }

    @Override
    public int getImapTimeout() {
        final String tmp = properties.get("com.openexchange.imap.imapTimeout");
        if (null == tmp) {
            return IMAPProperties.getInstance().getImapTimeout();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("IMAP Timeout: Invalid value.", e);
            return IMAPProperties.getInstance().getImapTimeout();
        }
    }

    @Override
    public Map<String, Boolean> getNewACLExtMap() {
        return IMAPProperties.getInstance().getNewACLExtMap();
    }

    @Override
    public BoolCapVal getSupportsACLs() {
        final String tmp = properties.get("com.openexchange.imap.imapSupportsACL");
        if (null == tmp) {
            return IMAPProperties.getInstance().getSupportsACLs();
        }

        return BoolCapVal.parseBoolCapVal(tmp.trim());
    }

    @Override
    public boolean isFastFetch() {
        final String tmp = properties.get("com.openexchange.imap.imapFastFetch");
        if (null == tmp) {
            return IMAPProperties.getInstance().isFastFetch();
        }

        return Boolean.parseBoolean(tmp.trim());
    }

    @Override
    public boolean notifyRecent() {
        final String tmp = properties.get("com.openexchange.imap.notifyRecent");
        if (null == tmp) {
            return IMAPProperties.getInstance().notifyRecent();
        }

        return Boolean.parseBoolean(tmp.trim());
    }

    @Override
    public boolean isPropagateClientIPAddress() {
        final String tmp = properties.get("com.openexchange.imap.propagateClientIPAddress");
        if (null == tmp) {
            return IMAPProperties.getInstance().isPropagateClientIPAddress();
        }

        return Boolean.parseBoolean(tmp.trim());
    }

    @Override
    public boolean isEnableTls() {
        final String tmp = properties.get("com.openexchange.imap.enableTls");
        if (null == tmp) {
            return IMAPProperties.getInstance().isEnableTls();
        }

        return Boolean.parseBoolean(tmp.trim());
    }

    @Override
    public Set<String> getPropagateHostNames() {
        final String tmp = properties.get("com.openexchange.imap.propagateHostNames");
        if (null == tmp) {
            return IMAPProperties.getInstance().getPropagateHostNames();
        }

        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(tmp.trim().split(" *, *"))));
    }

    @Override
    public boolean isImapSearch() {
        final String tmp = properties.get("com.openexchange.imap.imapSearch");
        if (null == tmp) {
            return IMAPProperties.getInstance().isImapSearch();
        }

        return Boolean.parseBoolean(tmp.trim());
    }

    @Override
    public boolean isImapSort() {
        final String tmp = properties.get("com.openexchange.imap.imapSort");
        if (null == tmp) {
            return IMAPProperties.getInstance().isImapSort();
        }

        return Boolean.parseBoolean(tmp.trim());
    }

}
