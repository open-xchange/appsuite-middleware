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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static com.openexchange.pop3.services.POP3ServiceRegistry.getServiceRegistry;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.MailConfig.BoolCapVal;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link POP3Properties}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Properties extends AbstractProtocolProperties {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(POP3Properties.class);

    private static final POP3Properties instance = new POP3Properties();

    /**
     * Gets the singleton instance of {@link POP3Properties}
     * 
     * @return The singleton instance of {@link POP3Properties}
     */
    public static POP3Properties getInstance() {
        return instance;
    }

    /*
     * Fields for global properties
     */
    private boolean imapSort;

    private boolean imapSearch;

    private boolean fastFetch;

    private BoolCapVal supportsACLs;

    private int imapTimeout;

    private int imapConnectionTimeout;

    private int imapConnectionIdleTime;

    private int imapTemporaryDown;

    private String imapAuthEnc;

    private String entity2AclImpl;

    private boolean mboxEnabled;

    private int blockSize;

    private final Map<String, Boolean> newACLExtMap;

    private String spamHandlerName;

    /**
     * Initializes a new {@link POP3Properties}
     */
    private POP3Properties() {
        super();
        newACLExtMap = new ConcurrentHashMap<String, Boolean>();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.config.AbstractProtocolProperties#loadProperties0()
     */
    @Override
    protected void loadProperties0() throws MailConfigException {
        final StringBuilder logBuilder = new StringBuilder(1024);
        logBuilder.append("\nLoading global POP3 properties...\n");

        final ConfigurationService configuration = getServiceRegistry().getService(ConfigurationService.class);
        {
            final String imapSortStr = configuration.getProperty("com.openexchange.imap.imapSort", "application").trim();
            imapSort = "imap".equalsIgnoreCase(imapSortStr);
            logBuilder.append("\tIMAP-Sort: ").append(imapSort).append('\n');
        }

        {
            final String imapSearchStr = configuration.getProperty("com.openexchange.imap.imapSearch", "imap").trim();
            imapSearch = "imap".equalsIgnoreCase(imapSearchStr);
            logBuilder.append("\tIMAP-Search: ").append(imapSearch).append('\n');
        }

        {
            final String fastFetchStr = configuration.getProperty("com.openexchange.imap.imapFastFetch", STR_TRUE).trim();
            fastFetch = Boolean.parseBoolean(fastFetchStr);
            logBuilder.append("\tFast Fetch Enabled: ").append(fastFetch).append('\n');
        }

        {
            final String supportsACLsStr = configuration.getProperty("com.openexchange.imap.imapSupportsACL", STR_FALSE).trim();
            supportsACLs = BoolCapVal.parseBoolCapVal(supportsACLsStr);
            logBuilder.append("\tSupport ACLs: ").append(supportsACLs).append('\n');
        }

        {
            final String imapTimeoutStr = configuration.getProperty("com.openexchange.imap.imapTimeout", "0").trim();
            try {
                imapTimeout = Integer.parseInt(imapTimeoutStr);
                logBuilder.append("\tIMAP Timeout: ").append(imapTimeout).append('\n');
            } catch (final NumberFormatException e) {
                imapTimeout = 0;
                logBuilder.append("\tIMAP Timeout: Invalid value \"").append(imapTimeoutStr).append("\". Setting to fallback: ").append(
                    imapTimeout).append('\n');
            }
        }

        {
            final String imapConTimeoutStr = configuration.getProperty("com.openexchange.imap.imapConnectionTimeout", "0").trim();
            try {
                imapConnectionTimeout = Integer.parseInt(imapConTimeoutStr);
                logBuilder.append("\tIMAP Connection Timeout: ").append(imapConnectionTimeout).append('\n');
            } catch (final NumberFormatException e) {
                imapConnectionTimeout = 0;
                logBuilder.append("\tIMAP Connection Timeout: Invalid value \"").append(imapConTimeoutStr).append(
                    "\". Setting to fallback: ").append(imapConnectionTimeout).append('\n');
            }
        }

        {
            final String imapTempDownStr = configuration.getProperty("com.openexchange.imap.imapTemporaryDown", "0").trim();
            try {
                imapTemporaryDown = Integer.parseInt(imapTempDownStr);
                logBuilder.append("\tIMAP Temporary Down: ").append(imapTemporaryDown).append('\n');
            } catch (final NumberFormatException e) {
                imapTemporaryDown = 0;
                logBuilder.append("\tIMAP Temporary Down: Invalid value \"").append(imapTempDownStr).append("\". Setting to fallback: ").append(
                    imapTemporaryDown).append('\n');
            }
        }

        {
            final String maxConIdleTime = configuration.getProperty("com.openexchange.imap.maxIMAPConnectionIdleTime", "60000").trim();
            try {
                imapConnectionIdleTime = Integer.parseInt(maxConIdleTime);
                logBuilder.append("\tMax IMAP Connection Idle Time: ").append(imapConnectionIdleTime).append('\n');
            } catch (final NumberFormatException e) {
                imapConnectionIdleTime = 60000;
                logBuilder.append("\tMax IMAP Connection Idle Time: Invalid value \"").append(maxConIdleTime).append(
                    "\". Setting to fallback: ").append(imapConnectionIdleTime).append('\n');
            }
        }

        {
            final String imapAuthEncStr = configuration.getProperty("com.openexchange.imap.imapAuthEnc", "UTF-8").trim();
            if (Charset.isSupported(imapAuthEncStr)) {
                imapAuthEnc = imapAuthEncStr;
                logBuilder.append("\tAuthentication Encoding: ").append(imapAuthEnc).append('\n');
            } else {
                imapAuthEnc = "UTF-8";
                logBuilder.append("\tAuthentication Encoding: Unsupported charset \"").append(imapAuthEncStr).append(
                    "\". Setting to fallback: ").append(imapAuthEnc).append('\n');
            }
        }

        {
            entity2AclImpl = configuration.getProperty("com.openexchange.imap.User2ACLImpl");
            if (null == entity2AclImpl) {
                throw new MailConfigException("Missing IMAP property \"com.openexchange.imap.User2ACLImpl\"");
            }
            entity2AclImpl = entity2AclImpl.trim();
        }

        {
            final String mboxEnabledStr = configuration.getProperty("com.openexchange.imap.mboxEnabled", STR_FALSE).trim();
            mboxEnabled = Boolean.parseBoolean(mboxEnabledStr);
            logBuilder.append("\tMBox Enabled: ").append(mboxEnabled).append('\n');
        }

        {
            final String blockSizeStr = configuration.getProperty("com.openexchange.imap.blockSize", "1000").trim();
            try {
                blockSize = Integer.parseInt(blockSizeStr);
                logBuilder.append("\tBlock Size: ").append(blockSize).append('\n');
            } catch (final NumberFormatException e) {
                blockSize = 1000;
                logBuilder.append("\tBlock Size: Invalid value \"").append(blockSizeStr).append("\". Setting to fallback: ").append(
                    blockSize).append('\n');
            }
        }

        spamHandlerName = configuration.getProperty("com.openexchange.imap.spamHandler", SpamHandler.SPAM_HANDLER_FALLBACK).trim();
        logBuilder.append("\tSpam Handler: ").append(spamHandlerName).append('\n');

        logBuilder.append("Global IMAP properties successfully loaded!");
        if (LOG.isInfoEnabled()) {
            LOG.info(logBuilder.toString());
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.config.AbstractProtocolProperties#resetFields()
     */
    @Override
    protected void resetFields() {
        imapSort = false;
        imapSearch = false;
        fastFetch = false;
        supportsACLs = null;
        imapTimeout = 0;
        imapConnectionTimeout = 0;
        imapConnectionIdleTime = 0;
        imapTemporaryDown = 0;
        imapAuthEnc = null;
        entity2AclImpl = null;
        mboxEnabled = false;
        blockSize = 0;
        spamHandlerName = null;
    }

    /**
     * Gets the fastFetch
     * 
     * @return the fastFetch
     */
    public boolean isFastFetch() {
        return fastFetch;
    }

    /**
     * Gets the imapAuthEnc
     * 
     * @return the imapAuthEnc
     */
    public String getImapAuthEnc() {
        return imapAuthEnc;
    }

    /**
     * Gets the imapConnectionIdleTime
     * 
     * @return the imapConnectionIdleTime
     */
    public int getImapConnectionIdleTime() {
        return imapConnectionIdleTime;
    }

    /**
     * Gets the imapConnectionTimeout
     * 
     * @return the imapConnectionTimeout
     */
    public int getPOP3ConnectionTimeout() {
        return imapConnectionTimeout;
    }

    /**
     * Gets the imapTemporaryDown
     * 
     * @return the imapTemporaryDown
     */
    public int getImapTemporaryDown() {
        return imapTemporaryDown;
    }

    /**
     * Gets the imapSearch
     * 
     * @return the imapSearch
     */
    public boolean isImapSearch() {
        return imapSearch;
    }

    /**
     * Gets the imapSort
     * 
     * @return the imapSort
     */
    public boolean isImapSort() {
        return imapSort;
    }

    /**
     * Gets the imapTimeout
     * 
     * @return the imapTimeout
     */
    public int getPOP3Timeout() {
        return imapTimeout;
    }

    /**
     * Gets the supportsACLs
     * 
     * @return the supportsACLs
     */
    public BoolCapVal getSupportsACLs() {
        return supportsACLs;
    }

    /**
     * Gets the entity2AclImpl
     * 
     * @return the entity2AclImpl
     */
    public String getEntity2AclImpl() {
        return entity2AclImpl;
    }

    /**
     * Gets the mboxEnabled
     * 
     * @return The mboxEnabled
     */
    public boolean isMBoxEnabled() {
        return mboxEnabled;
    }

    /**
     * Gets the block size in which large IMAP commands' UIDs/sequence numbers arguments get splitted
     * 
     * @return The block size
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Gets the newACLExtMap
     * 
     * @return the newACLExtMap
     */
    public Map<String, Boolean> getNewACLExtMap() {
        return newACLExtMap;
    }

    /**
     * Gets the spam handler name
     * 
     * @return The spam handler name
     */
    public String getSpamHandlerName() {
        return spamHandlerName;
    }
}
