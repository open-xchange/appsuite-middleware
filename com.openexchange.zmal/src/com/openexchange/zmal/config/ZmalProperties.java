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

package com.openexchange.zmal.config;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.spamhandler.SpamHandler;
import com.openexchange.zmal.Services;

/**
 * {@link ZmalProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalProperties extends AbstractProtocolProperties implements IZmalProperties {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ZmalProperties.class));

    private static final ZmalProperties instance = new ZmalProperties();

    /**
     * Gets the singleton instance of {@link ZmalProperties}
     *
     * @return The singleton instance of {@link ZmalProperties}
     */
    public static ZmalProperties getInstance() {
        return instance;
    }

    /*-
     * Fields for global properties
     */

    private final IMailProperties mailProperties;

    private int zmalTimeout;

    private int zmalConnectionTimeout;

    private String spamHandlerName;

    /**
     * Initializes a new {@link ZmalProperties}
     */
    private ZmalProperties() {
        super();
        mailProperties = MailProperties.getInstance();
    }

    @Override
    protected void loadProperties0() throws OXException {
        final StringBuilder logBuilder = new StringBuilder(1024);
        logBuilder.append("\nLoading global Zimbra MAL properties...\n");

        final ConfigurationService configuration = Services.getService(ConfigurationService.class);

        {
            final String imapTimeoutStr = configuration.getProperty("com.openexchange.zmal.zmalTimeout", "0").trim();
            try {
                zmalTimeout = Integer.parseInt(imapTimeoutStr);
                logBuilder.append("\tZimbra MAL Timeout: ").append(zmalTimeout).append('\n');
            } catch (final NumberFormatException e) {
                zmalTimeout = 0;
                logBuilder.append("\tZimbra MAL Timeout: Invalid value \"").append(imapTimeoutStr).append("\". Setting to fallback: ").append(
                    zmalTimeout).append('\n');
            }
        }

        {
            final String imapConTimeoutStr = configuration.getProperty("com.openexchange.zmal.zmalConnectionTimeout", "0").trim();
            try {
                zmalConnectionTimeout = Integer.parseInt(imapConTimeoutStr);
                logBuilder.append("\tZimbra MAL Connection Timeout: ").append(zmalConnectionTimeout).append('\n');
            } catch (final NumberFormatException e) {
                zmalConnectionTimeout = 0;
                logBuilder.append("\tZimbra MAL Connection Timeout: Invalid value \"").append(imapConTimeoutStr).append(
                    "\". Setting to fallback: ").append(zmalConnectionTimeout).append('\n');
            }
        }

        spamHandlerName = configuration.getProperty("com.openexchange.zmal.spamHandler", SpamHandler.SPAM_HANDLER_FALLBACK).trim();
        logBuilder.append("\tSpam Handler: ").append(spamHandlerName).append('\n');

        logBuilder.append("Global Zimbra MAL properties successfully loaded!");
        if (LOG.isInfoEnabled()) {
            LOG.info(logBuilder.toString());
        }
    }

    @Override
    protected void resetFields() {
        zmalTimeout = 0;
        zmalConnectionTimeout = 0;
        spamHandlerName = null;
    }

    @Override
    public int getZmalConnectionTimeout() {
        return zmalConnectionTimeout;
    }

    @Override
    public int getZmalTimeout() {
        return zmalTimeout;
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

}
