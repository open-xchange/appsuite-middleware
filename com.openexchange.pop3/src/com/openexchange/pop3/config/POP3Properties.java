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

import static com.openexchange.pop3.services.POP3ServiceRegistry.getServiceRegistry;
import java.nio.charset.Charset;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.spamhandler.SpamHandler;

/**
 * {@link POP3Properties} - POP3 properties loaded from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Properties extends AbstractProtocolProperties implements IPOP3Properties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POP3Properties.class);

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

    private final IMailProperties mailProperties;

    private int pop3Timeout;

    private int pop3ConnectionTimeout;

    private int pop3ConnectionIdleTime;

    private int pop3TemporaryDown;

    private int pop3BlockSize;

    private String pop3AuthEnc;

    private String spamHandlerName;

    private String sslProtocols;

    private String cipherSuites;

    /**
     * Initializes a new {@link POP3Properties}
     */
    private POP3Properties() {
        super();
        mailProperties = MailProperties.getInstance();
    }

    @Override
    protected void loadProperties0() throws OXException {
        final StringBuilder logBuilder = new StringBuilder(1024);
        logBuilder.append("\nLoading global POP3 properties...\n");

        final ConfigurationService configuration = getServiceRegistry().getService(ConfigurationService.class);

        {
            final String pop3TimeoutStr = configuration.getProperty("com.openexchange.pop3.pop3Timeout", "0").trim();
            try {
                pop3Timeout = Integer.parseInt(pop3TimeoutStr);
                logBuilder.append("\tPOP3 Timeout: ").append(pop3Timeout).append('\n');
            } catch (final NumberFormatException e) {
                pop3Timeout = 0;
                logBuilder.append("\tPOP3 Timeout: Invalid value \"").append(pop3TimeoutStr).append("\". Setting to fallback: ").append(
                    pop3Timeout).append('\n');
            }
        }

        {
            final String pop3ConTimeoutStr = configuration.getProperty("com.openexchange.pop3.pop3ConnectionTimeout", "0").trim();
            try {
                pop3ConnectionTimeout = Integer.parseInt(pop3ConTimeoutStr);
                logBuilder.append("\tPOP3 Connection Timeout: ").append(pop3ConnectionTimeout).append('\n');
            } catch (final NumberFormatException e) {
                pop3ConnectionTimeout = 0;
                logBuilder.append("\tPOP3 Connection Timeout: Invalid value \"").append(pop3ConTimeoutStr).append(
                    "\". Setting to fallback: ").append(pop3ConnectionTimeout).append('\n');
            }
        }

        {
            final String pop3TempDownStr = configuration.getProperty("com.openexchange.pop3.pop3TemporaryDown", "0").trim();
            try {
                pop3TemporaryDown = Integer.parseInt(pop3TempDownStr);
                logBuilder.append("\tPOP3 Temporary Down: ").append(pop3TemporaryDown).append('\n');
            } catch (final NumberFormatException e) {
                pop3TemporaryDown = 0;
                logBuilder.append("\tPOP3 Temporary Down: Invalid value \"").append(pop3TempDownStr).append("\". Setting to fallback: ").append(
                    pop3TemporaryDown).append('\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.pop3.pop3BlockSize", "100").trim();
            try {
                pop3BlockSize = Integer.parseInt(tmp);
                if (pop3BlockSize <= 0) {
                    pop3BlockSize = 100;
                    logBuilder.append("\tPOP3 Block Size: Invalid value \"").append(tmp).append("\". Setting to fallback: ").append(
                        pop3BlockSize).append('\n');
                } else {
                    logBuilder.append("\tPOP3 Block Size: ").append(pop3BlockSize).append('\n');
                }
            } catch (final NumberFormatException e) {
                pop3BlockSize = 100;
                logBuilder.append("\tPOP3 Block Size: Invalid value \"").append(tmp).append("\". Setting to fallback: ").append(
                    pop3BlockSize).append('\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.pop3.pop3ConnectionIdleTime", "300000").trim();
            try {
                pop3ConnectionIdleTime = Integer.parseInt(tmp);
                logBuilder.append("\tPOP3 Connection Idle Time: ").append(pop3ConnectionIdleTime).append('\n');
            } catch (final NumberFormatException e) {
                pop3ConnectionIdleTime = 300000;
                logBuilder.append("\tPOP3 Connection Idle Time: Invalid value \"").append(tmp).append("\". Setting to fallback: ").append(
                    pop3ConnectionIdleTime).append('\n');
            }
        }

        {
            final String pop3AuthEncStr = configuration.getProperty("com.openexchange.pop3.pop3AuthEnc", "UTF-8").trim();
            if (Charset.isSupported(pop3AuthEncStr)) {
                pop3AuthEnc = pop3AuthEncStr;
                logBuilder.append("\tAuthentication Encoding: ").append(pop3AuthEnc).append('\n');
            } else {
                pop3AuthEnc = "UTF-8";
                logBuilder.append("\tAuthentication Encoding: Unsupported charset \"").append(pop3AuthEncStr).append(
                    "\". Setting to fallback: ").append(pop3AuthEnc).append('\n');
            }
        }
        spamHandlerName = configuration.getProperty("com.openexchange.pop3.spamHandler", SpamHandler.SPAM_HANDLER_FALLBACK).trim();
        logBuilder.append("\tSpam Handler: ").append(spamHandlerName).append('\n');

        sslProtocols = configuration.getProperty("com.openexchange.pop3.ssl.protocols", "SSLv3 TLSv1").trim();
        logBuilder.append("\tSupported SSL protocols: ").append(sslProtocols).append("\n");

        {
            final String tmp = configuration.getProperty("com.openexchange.pop3.ssl.ciphersuites", "").trim();
            this.cipherSuites = Strings.isEmpty(tmp) ? null : tmp;
            logBuilder.append("\tSupported SSL cipher suites: ").append(null == this.cipherSuites ? "<default>" : cipherSuites).append("\n");
        }

        logBuilder.append("Global POP3 properties successfully loaded!");
        LOG.info(logBuilder.toString());
    }

    @Override
    protected void resetFields() {
        pop3Timeout = 0;
        pop3ConnectionTimeout = 0;
        pop3ConnectionIdleTime = 0;
        pop3TemporaryDown = 0;
        pop3AuthEnc = null;
        spamHandlerName = null;
        pop3BlockSize = 100;
        sslProtocols = "SSLv3 TLSv1";
        cipherSuites = null;
    }

    @Override
    public String getPOP3AuthEnc() {
        return pop3AuthEnc;
    }

    @Override
    public int getPOP3ConnectionIdleTime() {
        return pop3ConnectionIdleTime;
    }

    @Override
    public int getPOP3ConnectionTimeout() {
        return pop3ConnectionTimeout;
    }

    @Override
    public int getPOP3TemporaryDown() {
        return pop3TemporaryDown;
    }

    @Override
    public int getPOP3Timeout() {
        return pop3Timeout;
    }

    @Override
    public int getPOP3BlockSize() {
        return pop3BlockSize;
    }

    /**
     * Gets the spam handler name.
     *
     * @return The spam handler name
     */
    public String getSpamHandlerName() {
        return spamHandlerName;
    }

    @Override
    public int getAttachDisplaySize() {
        return mailProperties.getAttachDisplaySize();
    }

    @Override
    public char getDefaultSeparator() {
        return mailProperties.getDefaultSeparator();
    }

    @Override
    public int getMailAccessCacheIdleSeconds() {
        return mailProperties.getMailAccessCacheIdleSeconds();
    }

    @Override
    public int getMailAccessCacheShrinkerSeconds() {
        return mailProperties.getMailAccessCacheShrinkerSeconds();
    }

    @Override
    public int getMailFetchLimit() {
        return mailProperties.getMailFetchLimit();
    }

    @Override
    public int getWatcherFrequency() {
        return mailProperties.getWatcherFrequency();
    }

    @Override
    public int getWatcherTime() {
        return mailProperties.getWatcherTime();
    }

    @Override
    public boolean isAllowNestedDefaultFolderOnAltNamespace() {
        return mailProperties.isAllowNestedDefaultFolderOnAltNamespace();
    }

    @Override
    public boolean isEnforceSecureConnection() {
        return mailProperties.isEnforceSecureConnection();
    }

    @Override
    public void setEnforceSecureConnection(boolean enforceSecureConnection) {
        mailProperties.setEnforceSecureConnection(enforceSecureConnection);
    }

    @Override
    public boolean isIgnoreSubscription() {
        return mailProperties.isIgnoreSubscription();
    }

    @Override
    public boolean isSupportSubscription() {
        return mailProperties.isSupportSubscription();
    }

    @Override
    public boolean isUserFlagsEnabled() {
        return mailProperties.isUserFlagsEnabled();
    }

    @Override
    public boolean isWatcherEnabled() {
        return mailProperties.isWatcherEnabled();
    }

    @Override
    public boolean isWatcherShallClose() {
        return mailProperties.isWatcherShallClose();
    }

    @Override
    public String getSSLProtocols() {
        return sslProtocols;
    }

    @Override
    public String getSSLCipherSuites() {
        return cipherSuites;
    }

}
