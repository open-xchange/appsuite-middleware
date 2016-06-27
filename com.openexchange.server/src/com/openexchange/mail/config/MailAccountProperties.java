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

package com.openexchange.mail.config;

import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailAccountProperties} - Mail properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAccountProperties implements IMailProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailAccountProperties.class);

    protected Boolean enforceSecureConnection;
    protected final Map<String, String> properties;
    protected final String url;

    /**
     * Initializes a new {@link MailAccountProperties}.
     *
     * @param mailAccount The mail account
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountProperties(final MailAccount mailAccount) {
        super();
        if (null == mailAccount) {
            throw new IllegalArgumentException("mail account is null.");
        }
        properties = mailAccount.getProperties();
        String tmp;
        try {
            tmp = mailAccount.generateMailServerURL();
        } catch (final Exception e) {
            tmp = null;
        }
        url = tmp;
    }

    /**
     * Looks-up the denoted property.
     *
     * @param name The property name
     * @return The looked-up value or <code>null</code>
     */
    protected String lookUpProperty(String name) {
        return lookUpProperty(name, null);
    }

    /**
     * Looks-up the denoted property.
     *
     * @param name The property name
     * @param defaultValue The default value to return if absent
     * @return The looked-up value or given <code>defaultValue</code>
     */
    protected String lookUpProperty(String name, String defaultValue) {
        ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        return null == service ? defaultValue : service.getProperty(name, defaultValue);
    }

    @Override
    public int getAttachDisplaySize() {
        final String attachDisplaySizeStr = properties.get("com.openexchange.mail.attachmentDisplaySizeLimit");
        if (null == attachDisplaySizeStr) {
            return MailProperties.getInstance().getAttachDisplaySize();
        }

        try {
            return Integer.parseInt(attachDisplaySizeStr.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Attachment Display Size: Non parseable value.", e);
            return MailProperties.getInstance().getMailFetchLimit();
        }
    }

    @Override
    public char getDefaultSeparator() {
        final String defaultSepStr = properties.get("com.openexchange.mail.defaultSeparator");
        if (null == defaultSepStr) {
            return MailProperties.getInstance().getDefaultSeparator();
        }

        final char defaultSep = defaultSepStr.trim().charAt(0);
        if (defaultSep <= 32) {
            final char fallback = MailProperties.getInstance().getDefaultSeparator();
            LOG.error("\tDefault Separator: Invalid separator (decimal ascii value={}). Setting to fallback: {}{}", (int) defaultSep, fallback, '\n');
            return fallback;
        }
        return defaultSep;
    }

    @Override
    public int getMailAccessCacheIdleSeconds() {
        final String tmp = properties.get("com.openexchange.mail.mailAccessCacheIdleSeconds");
        if (null == tmp) {
            return MailProperties.getInstance().getMailAccessCacheIdleSeconds();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Mail Access Cache idle seconds: Non parseable value.", e);
            return MailProperties.getInstance().getMailAccessCacheIdleSeconds();
        }
    }

    @Override
    public int getMailAccessCacheShrinkerSeconds() {
        final String tmp = properties.get("com.openexchange.mail.mailAccessCacheShrinkerSeconds");
        if (null == tmp) {
            return MailProperties.getInstance().getMailAccessCacheShrinkerSeconds();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Mail Access Cache shrinker-interval seconds: Non parseable value.", e);
            return MailProperties.getInstance().getMailAccessCacheShrinkerSeconds();
        }
    }

    @Override
    public int getMailFetchLimit() {
        final String mailFetchLimitStr = properties.get("com.openexchange.mail.mailFetchLimit");
        if (null == mailFetchLimitStr) {
            return MailProperties.getInstance().getMailFetchLimit();
        }

        try {
            return Integer.parseInt(mailFetchLimitStr.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Mail Fetch Limit: Non parseable value.", e);
            return MailProperties.getInstance().getMailFetchLimit();
        }
    }

    @Override
    public int getWatcherFrequency() {
        final String watcherFreqStr = properties.get("com.openexchange.mail.watcherFrequency");
        if (null == watcherFreqStr) {
            return MailProperties.getInstance().getWatcherFrequency();
        }

        try {
            return Integer.parseInt(watcherFreqStr.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Watcher frequency: Non parseable value.", e);
            return MailProperties.getInstance().getWatcherFrequency();
        }
    }

    @Override
    public int getWatcherTime() {
        final String watcherTimeStr = properties.get("com.openexchange.mail.watcherTime");
        if (null == watcherTimeStr) {
            return MailProperties.getInstance().getWatcherTime();
        }

        try {
            return Integer.parseInt(watcherTimeStr.trim());
        } catch (final NumberFormatException e) {
            LOG.error("Watcher time: Non parseable value.", e);
            return MailProperties.getInstance().getWatcherTime();
        }
    }

    @Override
    public boolean isAllowNestedDefaultFolderOnAltNamespace() {
        final String allowNestedStr = properties.get("com.openexchange.mail.allowNestedDefaultFolderOnAltNamespace");
        if (null == allowNestedStr) {
            return MailProperties.getInstance().isAllowNestedDefaultFolderOnAltNamespace();
        }

        return Boolean.parseBoolean(allowNestedStr.trim());
    }

    @Override
    public boolean isEnforceSecureConnection() {
        Boolean b = this.enforceSecureConnection;
        if (null != b) {
            return b.booleanValue();
        }
        return MailProperties.getInstance().isEnforceSecureConnection();
    }

    @Override
    public void setEnforceSecureConnection(boolean enforceSecureConnection) {
        this.enforceSecureConnection = Boolean.valueOf(enforceSecureConnection);
    }

    @Override
    public boolean isIgnoreSubscription() {
        final String ignoreSubsStr = properties.get("com.openexchange.mail.ignoreSubscription");
        if (null == ignoreSubsStr) {
            return MailProperties.getInstance().isIgnoreSubscription();
        }

        return Boolean.parseBoolean(ignoreSubsStr.trim());
    }

    @Override
    public boolean isSupportSubscription() {
        final String supportSubsStr = properties.get("com.openexchange.mail.supportSubscription");
        if (null == supportSubsStr) {
            return MailProperties.getInstance().isSupportSubscription();
        }

        return Boolean.parseBoolean(supportSubsStr.trim());
    }

    @Override
    public boolean isUserFlagsEnabled() {
        final String userFlagsStr = properties.get("com.openexchange.mail.userFlagsEnabled");
        if (null == userFlagsStr) {
            return MailProperties.getInstance().isUserFlagsEnabled();
        }

        return Boolean.parseBoolean(userFlagsStr.trim());
    }

    @Override
    public boolean isWatcherEnabled() {
        final String watcherEnabledStr = properties.get("com.openexchange.mail.watcherEnabled");
        if (null == watcherEnabledStr) {
            return MailProperties.getInstance().isWatcherEnabled();
        }

        return Boolean.parseBoolean(watcherEnabledStr.trim());
    }

    @Override
    public boolean isWatcherShallClose() {
        final String watcherShallCloseStr = properties.get("com.openexchange.mail.watcherShallClose");
        if (null == watcherShallCloseStr) {
            return MailProperties.getInstance().isWatcherShallClose();
        }

        return Boolean.parseBoolean(watcherShallCloseStr.trim());
    }

    @Override
    public void waitForLoading() throws InterruptedException {
        MailProperties.getInstance().waitForLoading();
    }

}
