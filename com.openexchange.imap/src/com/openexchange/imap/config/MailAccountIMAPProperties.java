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

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccountIMAPProperties.class);

    private static final int PRIMARY = MailAccount.DEFAULT_ID;

    private final int mailAccountId;

    /**
     * Initializes a new {@link MailAccountIMAPProperties}.
     *
     * @param mailAccount The mail account
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountIMAPProperties(MailAccount mailAccount, int userId, int contextId) {
        super(mailAccount, userId, contextId);
        mailAccountId = mailAccount.getId();
    }

    @Override
    public int getBlockSize() {
        String blockSizeStr = getAccountProperty("com.openexchange.imap.blockSize");
        if (null != blockSizeStr) {
            try {
                return Integer.parseInt(blockSizeStr.trim());
            } catch (final NumberFormatException e) {
                LOG.error("Block Size: Invalid value.", e);
                return IMAPProperties.getInstance().getBlockSize();
            }
        }

        if (mailAccountId == PRIMARY) {
            blockSizeStr = lookUpProperty("com.openexchange.imap.primary.blockSize");
            if (null != blockSizeStr) {
                try {
                    return Integer.parseInt(blockSizeStr.trim());
                } catch (final NumberFormatException e) {
                    LOG.error("Block Size: Invalid value.", e);
                    return IMAPProperties.getInstance().getBlockSize();
                }
            }
        }

        return lookUpIntProperty("com.openexchange.imap.blockSize", IMAPProperties.getInstance().getBlockSize());
    }

    @Override
    public int getMaxNumConnection() {
        String tmp = getAccountProperty("com.openexchange.imap.maxNumConnections");
        if (null != tmp) {
            try {
                return Integer.parseInt(tmp.trim());
            } catch (final NumberFormatException e) {
                LOG.error("Max. Number of connections: Invalid value.", e);
                return IMAPProperties.getInstance().getMaxNumConnection();
            }
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.maxNumConnections");
            if (null != tmp) {
                try {
                    return Integer.parseInt(tmp.trim());
                } catch (final NumberFormatException e) {
                    LOG.error("Max. Number of connections: Invalid value.", e);
                    return IMAPProperties.getInstance().getMaxNumConnection();
                }
            }
        }

        return lookUpIntProperty("com.openexchange.imap.maxNumConnections", IMAPProperties.getInstance().getMaxNumConnection());
    }

    @Override
    public String getImapAuthEnc() {
        String imapAuthEncStr = getAccountProperty("com.openexchange.imap.imapAuthEnc");
        if (null != imapAuthEncStr) {
            imapAuthEncStr = imapAuthEncStr.trim();
            if (Charset.isSupported(imapAuthEncStr)) {
                return imapAuthEncStr;
            }
            final String fallback = IMAPProperties.getInstance().getImapAuthEnc();
            LOG.error("Authentication Encoding: Unsupported charset \"{}\". Setting to fallback: {}", imapAuthEncStr, fallback);
            return fallback;
        }

        if (mailAccountId == PRIMARY) {
            imapAuthEncStr = lookUpProperty("com.openexchange.imap.primary.imapAuthEnc");
            if (null != imapAuthEncStr) {
                imapAuthEncStr = imapAuthEncStr.trim();
                if (Charset.isSupported(imapAuthEncStr)) {
                    return imapAuthEncStr;
                }
                final String fallback = IMAPProperties.getInstance().getImapAuthEnc();
                LOG.error("Authentication Encoding: Unsupported charset \"{}\". Setting to fallback: {}", imapAuthEncStr, fallback);
                return fallback;
            }
        }

        return lookUpProperty("com.openexchange.imap.imapAuthEnc", IMAPProperties.getInstance().getImapAuthEnc());
    }

    @Override
    public int getImapConnectionTimeout() {
        String tmp = getAccountProperty("com.openexchange.imap.imapConnectionTimeout");
        if (null != tmp) {
            try {
                return Integer.parseInt(tmp.trim());
            } catch (final NumberFormatException e) {
                LOG.error("IMAP Connection Timeout: Invalid value.", e);
                return IMAPProperties.getInstance().getImapConnectionTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.imapConnectionTimeout");
            if (null != tmp) {
                try {
                    return Integer.parseInt(tmp.trim());
                } catch (final NumberFormatException e) {
                    LOG.error("IMAP Connection Timeout: Invalid value.", e);
                    return IMAPProperties.getInstance().getImapConnectionTimeout();
                }
            }
        }

        return lookUpIntProperty("com.openexchange.imap.imapConnectionTimeout", IMAPProperties.getInstance().getImapConnectionTimeout());
    }

    @Override
    public int getImapTemporaryDown() {
        String tmp = getAccountProperty("com.openexchange.imap.imapTemporaryDown");
        if (null != tmp) {
            try {
                return Integer.parseInt(tmp.trim());
            } catch (final NumberFormatException e) {
                LOG.error("IMAP Temporary Down: Invalid value.", e);
                return IMAPProperties.getInstance().getImapTemporaryDown();
            }
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.imapTemporaryDown");
            if (null != tmp) {
                try {
                    return Integer.parseInt(tmp.trim());
                } catch (final NumberFormatException e) {
                    LOG.error("IMAP Temporary Down: Invalid value.", e);
                    return IMAPProperties.getInstance().getImapTemporaryDown();
                }
            }
        }

        return lookUpIntProperty("com.openexchange.imap.imapTemporaryDown", IMAPProperties.getInstance().getImapTemporaryDown());
    }

    @Override
    public int getImapFailedAuthTimeout() {
        String tmp = getAccountProperty("com.openexchange.imap.failedAuthTimeout");
        if (null != tmp) {
            try {
                return Integer.parseInt(tmp.trim());
            } catch (final NumberFormatException e) {
                LOG.error("IMAP Failed Auth Timeout: Invalid value.", e);
                return IMAPProperties.getInstance().getImapFailedAuthTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.failedAuthTimeout");
            if (null != tmp) {
                try {
                    return Integer.parseInt(tmp.trim());
                } catch (final NumberFormatException e) {
                    LOG.error("IMAP Failed Auth Timeout: Invalid value.", e);
                    return IMAPProperties.getInstance().getImapFailedAuthTimeout();
                }
            }
        }

        return lookUpIntProperty("com.openexchange.imap.failedAuthTimeout", IMAPProperties.getInstance().getImapFailedAuthTimeout());
    }

    @Override
    public int getImapTimeout() {
        String tmp = getAccountProperty("com.openexchange.imap.imapTimeout");
        if (null != tmp) {
            try {
                return Integer.parseInt(tmp.trim());
            } catch (final NumberFormatException e) {
                LOG.error("IMAP Timeout: Invalid value.", e);
                return IMAPProperties.getInstance().getImapTimeout();
            }
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.imapTimeout");
            if (null != tmp) {
                try {
                    return Integer.parseInt(tmp.trim());
                } catch (final NumberFormatException e) {
                    LOG.error("IMAP Timeout: Invalid value.", e);
                    return IMAPProperties.getInstance().getImapTimeout();
                }
            }
        }

        return lookUpIntProperty("com.openexchange.imap.imapTimeout", IMAPProperties.getInstance().getImapTimeout());
    }

    @Override
    public Map<String, Boolean> getNewACLExtMap() {
        return IMAPProperties.getInstance().getNewACLExtMap();
    }

    @Override
    public BoolCapVal getSupportsACLs() {
        String tmp = getAccountProperty("com.openexchange.imap.imapSupportsACL");
        if (null != tmp) {
            return BoolCapVal.parseBoolCapVal(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.imapSupportsACL");
            if (null != tmp) {
                return BoolCapVal.parseBoolCapVal(tmp.trim());
            }
        }

        tmp = lookUpProperty("com.openexchange.imap.imapSupportsACL");
        return null == tmp ? IMAPProperties.getInstance().getSupportsACLs() : BoolCapVal.parseBoolCapVal(tmp.trim());
    }

    @Override
    public boolean isFastFetch() {
        String tmp = getAccountProperty("com.openexchange.imap.imapFastFetch");
        if (null != tmp) {
            return Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.imapFastFetch");
            if (null != tmp) {
                return Boolean.parseBoolean(tmp.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.imap.imapFastFetch", IMAPProperties.getInstance().isFastFetch());
    }

    @Override
    public boolean isPropagateClientIPAddress() {
        String tmp = getAccountProperty("com.openexchange.imap.propagateClientIPAddress");
        if (null != tmp) {
            return Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.propagateClientIPAddress");
            if (null != tmp) {
                return Boolean.parseBoolean(tmp.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.imap.propagateClientIPAddress", IMAPProperties.getInstance().isPropagateClientIPAddress());
    }

    @Override
    public boolean isEnableTls() {
        String tmp = getAccountProperty("com.openexchange.imap.enableTls");
        if (null != tmp) {
            return Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.enableTls");
            if (null != tmp) {
                return Boolean.parseBoolean(tmp.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.imap.enableTls", IMAPProperties.getInstance().isEnableTls());
    }

    @Override
    public boolean isAuditLogEnabled() {
        String tmp = getAccountProperty("com.openexchange.imap.auditLog.enabled");
        if (null != tmp) {
            return Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.auditLog.enabled");
            if (null != tmp) {
                return Boolean.parseBoolean(tmp.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.imap.auditLog.enabled", IMAPProperties.getInstance().isAuditLogEnabled());
    }

    @Override
    public boolean isOverwritePreLoginCapabilities() {
        String tmp = getAccountProperty("com.openexchange.imap.overwritePreLoginCapabilities");
        if (null != tmp) {
            return Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.overwritePreLoginCapabilities");
            if (null != tmp) {
                return Boolean.parseBoolean(tmp.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.imap.overwritePreLoginCapabilities", IMAPProperties.getInstance().isOverwritePreLoginCapabilities());
    }

    @Override
    public Set<String> getPropagateHostNames() {
        String tmp = getAccountProperty("com.openexchange.imap.propagateHostNames");
        if (null != tmp) {
            return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(tmp.trim().split(" *, *"))));
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.propagateHostNames");
            if (null != tmp) {
                return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(tmp.trim().split(" *, *"))));
            }
        }

        tmp = lookUpProperty("com.openexchange.imap.propagateHostNames");
        if (null != tmp) {
            return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(tmp.trim().split(" *, *"))));
        }

        return IMAPProperties.getInstance().getPropagateHostNames();
    }

    @Override
    public boolean isImapSearch() {
        String tmp = getAccountProperty("com.openexchange.imap.imapSearch");
        if (null != tmp) {
            return "force-imap".equalsIgnoreCase(tmp) || Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.imapSearch");
            if (null != tmp) {
                return "force-imap".equalsIgnoreCase(tmp) || Boolean.parseBoolean(tmp.trim());
            }
        }

        tmp = lookUpProperty("com.openexchange.imap.imapSearch");
        if (null != tmp) {
            return "force-imap".equalsIgnoreCase(tmp) || Boolean.parseBoolean(tmp.trim());
        }

        return IMAPProperties.getInstance().isImapSearch();
    }

    @Override
    public boolean forceImapSearch() {
        return IMAPProperties.getInstance().forceImapSearch();
    }

    @Override
    public boolean isImapSort() {
        String tmp = getAccountProperty("com.openexchange.imap.imapSort");
        if (null != tmp) {
            return Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.imapSort");
            if (null != tmp) {
                return Boolean.parseBoolean(tmp.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.imap.imapSort", IMAPProperties.getInstance().isImapSort());
    }

    @Override
    public boolean allowFolderCaches() {
        String tmp = getAccountProperty("com.openexchange.imap.allowFolderCaches");
        if (null != tmp) {
            return Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.allowFolderCaches");
            if (null != tmp) {
                return Boolean.parseBoolean(tmp.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.imap.allowFolderCaches", IMAPProperties.getInstance().allowFolderCaches());
    }

    @Override
    public boolean allowFetchSingleHeaders() {
        String tmp = getAccountProperty("com.openexchange.imap.allowFetchSingleHeaders");
        if (null != tmp) {
            return Boolean.parseBoolean(tmp.trim());
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.allowFetchSingleHeaders");
            if (null != tmp) {
                return Boolean.parseBoolean(tmp.trim());
            }
        }

        return lookUpBoolProperty("com.openexchange.imap.allowFetchSingleHeaders", IMAPProperties.getInstance().allowFetchSingleHeaders());
    }

    @Override
    public String getSSLProtocols() {
        String tmp = getAccountProperty("com.openexchange.imap.ssl.protocols");
        if (null != tmp) {
            return tmp.trim();
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.ssl.protocols");
            if (null != tmp) {
                return tmp.trim();
            }
        }

        return lookUpProperty("com.openexchange.imap.ssl.protocols", IMAPProperties.getInstance().getSSLProtocols());
    }

    @Override
    public String getSSLCipherSuites() {
        String tmp = getAccountProperty("com.openexchange.imap.ssl.ciphersuites");
        if (null != tmp) {
            return tmp.trim();
        }

        if (mailAccountId == PRIMARY) {
            tmp = lookUpProperty("com.openexchange.imap.primary.ssl.ciphersuites");
            if (null != tmp) {
                return tmp.trim();
            }
        }

        return lookUpProperty("com.openexchange.imap.ssl.ciphersuites", IMAPProperties.getInstance().getSSLCipherSuites());
    }

}
