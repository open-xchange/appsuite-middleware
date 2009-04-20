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

package com.openexchange.mail.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailConfig.LoginSource;
import com.openexchange.mail.api.MailConfig.PasswordSource;
import com.openexchange.mail.api.MailConfig.ServerSource;
import com.openexchange.mail.partmodifier.DummyPartModifier;
import com.openexchange.mail.partmodifier.PartModifier;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MailProperties} - Global mail properties read from configuration file
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailProperties {

    private static final Log LOG = LogFactory.getLog(MailProperties.class);

    private static final String STR_FALSE = Boolean.FALSE.toString();

    private static final String STR_TRUE = Boolean.TRUE.toString();

    private static final MailProperties instance = new MailProperties();

    /**
     * Gets the singleton instance of {@link MailProperties}
     * 
     * @return The singleton instance of {@link MailProperties}
     */
    public static MailProperties getInstance() {
        return instance;
    }

    private final AtomicBoolean loaded;

    /*
     * Fields for global properties
     */
    private LoginSource loginSource;

    private PasswordSource passwordSource;

    private ServerSource mailServerSource;

    private ServerSource transportServerSource;

    private String mailServer;

    private String transportServer;

    private String masterPassword;

    private int mailFetchLimit;

    private int attachDisplaySize;

    private boolean userFlagsEnabled;

    private boolean allowNestedDefaultFolderOnAltNamespace;

    private String defaultMimeCharset;

    private boolean ignoreSubscription;

    private char defaultSeparator;

    private int maxNumOfConnections;

    private String[] quoteLineColors;

    private Properties javaMailProperties;

    private boolean watcherEnabled;

    private int watcherTime;

    private int watcherFrequency;

    private boolean watcherShallClose;

    private boolean supportSubscription;

    private String[] phishingHeaders;

    private String defaultMailProvider;

    private boolean adminMailLoginEnabled;

    private int mailAccessCacheShrinkerSeconds;

    private int mailAccessCacheIdleSeconds;

    /**
     * Initializes a new {@link MailProperties}
     */
    private MailProperties() {
        super();
        loaded = new AtomicBoolean();
    }

    /**
     * Exclusively loads the global mail properties
     * 
     * @throws MailConfigException If loading of global mail properties fails
     */
    public void loadProperties() throws MailConfigException {
        if (!loaded.get()) {
            synchronized (loaded) {
                if (!loaded.get()) {
                    loadProperties0();
                    loaded.set(true);
                    loaded.notifyAll();
                }
            }
        }
    }

    /**
     * Exclusively resets the global mail properties
     */
    public void resetProperties() {
        if (loaded.get()) {
            synchronized (loaded) {
                if (loaded.get()) {
                    resetFields();
                    loaded.set(false);
                }
            }
        }
    }

    /**
     * Waits for loading this properties.
     * 
     * @throws InterruptedException If another thread interrupted the current thread before or while the current thread was waiting for
     *             loading the properties.
     */
    public void waitForLoading() throws InterruptedException {
        if (!loaded.get()) {
            synchronized (loaded) {
                while (!loaded.get()) {
                    loaded.wait();
                }
            }
        }
    }

    private void resetFields() {
        loginSource = null;
        passwordSource = null;
        mailServerSource = null;
        transportServerSource = null;
        mailServer = null;
        transportServer = null;
        masterPassword = null;
        mailFetchLimit = 0;
        attachDisplaySize = 0;
        userFlagsEnabled = false;
        allowNestedDefaultFolderOnAltNamespace = false;
        defaultMimeCharset = null;
        ignoreSubscription = false;
        defaultSeparator = '\0';
        maxNumOfConnections = 0;
        quoteLineColors = null;
        javaMailProperties = null;
        watcherEnabled = false;
        watcherTime = 0;
        watcherFrequency = 0;
        watcherShallClose = false;
        supportSubscription = false;
        defaultMailProvider = null;
        adminMailLoginEnabled = false;
        mailAccessCacheShrinkerSeconds = 0;
        mailAccessCacheIdleSeconds = 0;
    }

    private void loadProperties0() throws MailConfigException {
        final StringBuilder logBuilder = new StringBuilder(1024);
        logBuilder.append("\nLoading global mail properties...\n");

        final ConfigurationService configuration = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);

        {
            final String loginStr = configuration.getProperty("com.openexchange.mail.loginSource");
            if (loginStr == null) {
                throw new MailConfigException("Property \"com.openexchange.mail.loginSource\" not set");
            }
            final LoginSource loginSource = LoginSource.parse(loginStr.trim());
            if (null == loginSource) {
                throw new MailConfigException(new StringBuilder(256).append(
                    "Unknown value in property \"com.openexchange.mail.loginSource\": ").append(loginStr).toString());
            }
            this.loginSource = loginSource;
            logBuilder.append("\tLogin Source: ").append(this.loginSource.toString()).append('\n');
        }

        {
            final String pwStr = configuration.getProperty("com.openexchange.mail.passwordSource");
            if (pwStr == null) {
                throw new MailConfigException("Property \"com.openexchange.mail.passwordSource\" not set");
            }
            final PasswordSource pwSource = PasswordSource.parse(pwStr.trim());
            if (null == pwSource) {
                throw new MailConfigException(new StringBuilder(256).append(
                    "Unknown value in property \"com.openexchange.mail.passwordSource\": ").append(pwStr).toString());
            }
            passwordSource = pwSource;
            logBuilder.append("\tPassword Source: ").append(passwordSource.toString()).append('\n');
        }

        {
            final String mailSrcStr = configuration.getProperty("com.openexchange.mail.mailServerSource");
            if (mailSrcStr == null) {
                throw new MailConfigException("Property \"com.openexchange.mail.mailServerSource\" not set");
            }
            final ServerSource mailServerSource = ServerSource.parse(mailSrcStr.trim());
            if (null == mailServerSource) {
                throw new MailConfigException(new StringBuilder(256).append(
                    "Unknown value in property \"com.openexchange.mail.mailServerSource\": ").append(mailSrcStr).toString());
            }
            this.mailServerSource = mailServerSource;
            logBuilder.append("\tMail Server Source: ").append(this.mailServerSource.toString()).append('\n');
        }

        {
            final String transSrcStr = configuration.getProperty("com.openexchange.mail.transportServerSource");
            if (transSrcStr == null) {
                throw new MailConfigException("Property \"com.openexchange.mail.transportServerSource\" not set");
            }
            final ServerSource transportServerSource = ServerSource.parse(transSrcStr.trim());
            if (null == transportServerSource) {
                throw new MailConfigException(new StringBuilder(256).append(
                    "Unknown value in property \"com.openexchange.mail.transportServerSource\": ").append(transSrcStr).toString());
            }
            this.transportServerSource = transportServerSource;
            logBuilder.append("\tTransport Server Source: ").append(this.transportServerSource.toString()).append('\n');
        }

        {
            mailServer = configuration.getProperty("com.openexchange.mail.mailServer");
            if (mailServer != null) {
                mailServer = mailServer.trim();
            }
        }

        {
            transportServer = configuration.getProperty("com.openexchange.mail.transportServer");
            if (transportServer != null) {
                transportServer = transportServer.trim();
            }
        }

        {
            masterPassword = configuration.getProperty("com.openexchange.mail.masterPassword");
            if (masterPassword != null) {
                masterPassword = masterPassword.trim();
            }
        }

        final String fallbackPrefix = "\". Setting to fallback: ";
        {
            final String mailFetchLimitStr = configuration.getProperty("com.openexchange.mail.mailFetchLimit", "1000").trim();
            try {
                mailFetchLimit = Integer.parseInt(mailFetchLimitStr);
                logBuilder.append("\tMail Fetch Limit: ").append(mailFetchLimit).append('\n');
            } catch (final NumberFormatException e) {
                mailFetchLimit = 1000;
                logBuilder.append("\tMail Fetch Limit: Non parseable value \"").append(mailFetchLimitStr).append(fallbackPrefix).append(
                    mailFetchLimit).append('\n');
            }
        }

        {
            final String attachDisplaySizeStr = configuration.getProperty("com.openexchange.mail.attachmentDisplaySizeLimit", "8192").trim();
            try {
                attachDisplaySize = Integer.parseInt(attachDisplaySizeStr);
                logBuilder.append("\tAttachment Display Size Limit: ").append(attachDisplaySize).append('\n');
            } catch (final NumberFormatException e) {
                attachDisplaySize = 8192;
                logBuilder.append("\tAttachment Display Size Limit: Non parseable value \"").append(attachDisplaySizeStr).append(
                    fallbackPrefix).append(attachDisplaySize).append('\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.mailAccessCacheShrinkerSeconds", "3").trim();
            try {
                mailAccessCacheShrinkerSeconds = Integer.parseInt(tmp);
                logBuilder.append("\tMail Access Cache shrinker-interval seconds: ").append(mailAccessCacheShrinkerSeconds).append('\n');
            } catch (final NumberFormatException e) {
                mailAccessCacheShrinkerSeconds = 3;
                logBuilder.append("\tMail Access Cache shrinker-interval seconds: Non parseable value \"").append(tmp).append(
                    fallbackPrefix).append(mailAccessCacheShrinkerSeconds).append('\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.mail.mailAccessCacheIdleSeconds", "7").trim();
            try {
                mailAccessCacheIdleSeconds = Integer.parseInt(tmp);
                logBuilder.append("\tMail Access Cache idle seconds: ").append(mailAccessCacheIdleSeconds).append('\n');
            } catch (final NumberFormatException e) {
                mailAccessCacheIdleSeconds = 7;
                logBuilder.append("\tMail Access Cache idle seconds: Non parseable value \"").append(tmp).append(fallbackPrefix).append(
                    mailAccessCacheIdleSeconds).append('\n');
            }
        }

        {
            final String userFlagsStr = configuration.getProperty("com.openexchange.mail.userFlagsEnabled", STR_FALSE).trim();
            userFlagsEnabled = Boolean.parseBoolean(userFlagsStr);
            logBuilder.append("\tUser Flags Enabled: ").append(userFlagsEnabled).append('\n');
        }

        {
            final String allowNestedStr = configuration.getProperty(
                "com.openexchange.mail.allowNestedDefaultFolderOnAltNamespace",
                STR_FALSE).trim();
            allowNestedDefaultFolderOnAltNamespace = Boolean.parseBoolean(allowNestedStr);
            logBuilder.append("\tAllow Nested Default Folders on AltNamespace: ").append(allowNestedDefaultFolderOnAltNamespace).append(
                '\n');
        }

        {
            final String defaultMimeCharsetStr = configuration.getProperty("mail.mime.charset", "UTF-8").trim();
            /*
             * Check validity
             */
            try {
                Charset.forName(defaultMimeCharsetStr);
                defaultMimeCharset = defaultMimeCharsetStr;
                logBuilder.append("\tDefault MIME Charset: ").append(defaultMimeCharset).append('\n');
            } catch (final Throwable t) {
                defaultMimeCharset = "UTF-8";
                logBuilder.append("\tDefault MIME Charset: Unsupported charset \"").append(defaultMimeCharsetStr).append(fallbackPrefix).append(
                    defaultMimeCharset).append('\n');
            }
            /*
             * Add to system properties, too
             */
            System.getProperties().setProperty("mail.mime.charset", defaultMimeCharset);
        }

        {
            final String defaultMailProviderStr = configuration.getProperty("com.openexchange.mail.defaultMailProvider", "imap").trim();
            defaultMailProvider = defaultMailProviderStr;
            logBuilder.append("\tDefault Mail Provider: ").append(defaultMailProvider).append('\n');
        }

        {
            final String adminMailLoginEnabledStr = configuration.getProperty("com.openexchange.mail.adminMailLoginEnabled", STR_FALSE).trim();
            adminMailLoginEnabled = Boolean.parseBoolean(adminMailLoginEnabledStr);
            logBuilder.append("\tAdmin Mail Login Enabled: ").append(adminMailLoginEnabled).append('\n');
        }

        {
            final String ignoreSubsStr = configuration.getProperty("com.openexchange.mail.ignoreSubscription", STR_FALSE).trim();
            ignoreSubscription = Boolean.parseBoolean(ignoreSubsStr);
            logBuilder.append("\tIgnore Folder Subscription: ").append(ignoreSubscription).append('\n');
        }

        {
            final String supSubsStr = configuration.getProperty("com.openexchange.mail.supportSubscription", STR_TRUE).trim();
            supportSubscription = Boolean.parseBoolean(supSubsStr);
            logBuilder.append("\tSupport Subscription: ").append(supportSubscription).append('\n');
        }

        {
            final char defaultSep = configuration.getProperty("com.openexchange.mail.defaultSeparator", "/").trim().charAt(0);
            if (defaultSep <= 32) {
                defaultSeparator = '/';
                logBuilder.append("\tDefault Separator: Invalid separator (decimal ascii value=").append((int) defaultSep).append(
                    "). Setting to fallback: ").append(defaultSeparator).append('\n');
            } else {
                defaultSeparator = defaultSep;
                logBuilder.append("\tDefault Separator: ").append(defaultSeparator).append('\n');
            }
        }

        {
            final String maxNum = configuration.getProperty("com.openexchange.mail.maxNumOfConnections", "0").trim();
            try {
                maxNumOfConnections = Integer.parseInt(maxNum);
                logBuilder.append("\tMax Number of Connections: ").append(maxNumOfConnections).append('\n');
            } catch (final NumberFormatException e) {
                maxNumOfConnections = 0;
                logBuilder.append("\tMax Number of Connections: Invalid value \"").append(maxNum).append(fallbackPrefix).append(
                    maxNumOfConnections).append('\n');
            }
        }

        {
            final String partModifierStr = configuration.getProperty(
                "com.openexchange.mail.partModifierImpl",
                DummyPartModifier.class.getName()).trim();
            try {
                PartModifier.init(partModifierStr);
                logBuilder.append("\tPartModifier Implementation: ").append(PartModifier.getInstance().getClass().getName()).append('\n');
            } catch (final MailException e) {
                try {
                    PartModifier.init(DummyPartModifier.class.getName());
                } catch (final MailException e1) {
                    /*
                     * Cannot occur
                     */
                    LOG.error(e.getMessage(), e);
                }
                logBuilder.append("\tPartModifier Implementation: Unknown class \"").append(partModifierStr).append(fallbackPrefix).append(
                    DummyPartModifier.class.getName()).append('\n');
            }
        }

        {
            final String quoteColors = configuration.getProperty("com.openexchange.mail.quoteLineColors", "#666666").trim();
            if (Pattern.matches("((#[0-9a-fA-F&&[^,]]{6})(?:\r?\n|\\z|\\s*,\\s*))+", quoteColors)) {
                quoteLineColors = quoteColors.split("\\s*,\\s*");
                logBuilder.append("\tHTML Quote Colors: ").append(quoteColors).append('\n');
            } else {
                quoteLineColors = new String[] { "#666666" };
                logBuilder.append("\tHTML Quote Colors: Invalid sequence of colors \"").append(quoteColors).append(
                    "\". Setting to fallback: #666666").append('\n');
            }
        }

        {
            final String watcherEnabledStr = configuration.getProperty("com.openexchange.mail.watcherEnabled", STR_FALSE).trim();
            watcherEnabled = Boolean.parseBoolean(watcherEnabledStr);
            logBuilder.append("\tWatcher Enabled: ").append(watcherEnabled).append('\n');
        }

        {
            final String watcherTimeStr = configuration.getProperty("com.openexchange.mail.watcherTime", "60000").trim();
            try {
                watcherTime = Integer.parseInt(watcherTimeStr);
                logBuilder.append("\tWatcher Time: ").append(watcherTime).append('\n');
            } catch (final NumberFormatException e) {
                watcherTime = 60000;
                logBuilder.append("\tWatcher Time: Invalid value \"").append(watcherTimeStr).append(fallbackPrefix).append(watcherTime).append(
                    '\n');
            }
        }

        {
            final String watcherFeqStr = configuration.getProperty("com.openexchange.mail.watcherFrequency", "10000").trim();
            try {
                watcherFrequency = Integer.parseInt(watcherFeqStr);
                logBuilder.append("\tWatcher Frequency: ").append(watcherFrequency).append('\n');
            } catch (final NumberFormatException e) {
                watcherFrequency = 10000;
                logBuilder.append("\tWatcher Frequency: Invalid value \"").append(watcherFeqStr).append(fallbackPrefix).append(
                    watcherFrequency).append('\n');
            }
        }

        {
            final String watcherShallCloseStr = configuration.getProperty("com.openexchange.mail.watcherShallClose", STR_FALSE).trim();
            watcherShallClose = Boolean.parseBoolean(watcherShallCloseStr);
            logBuilder.append("\tWatcher Shall Close: ").append(watcherShallClose).append('\n');
        }

        {
            final String phishingHdrsStr = configuration.getProperty("com.openexchange.mail.phishingHeader", "").trim();
            if (null != phishingHdrsStr && phishingHdrsStr.length() > 0) {
                phishingHeaders = phishingHdrsStr.split(" *, *");
            } else {
                phishingHeaders = null;
            }
        }

        {
            String javaMailPropertiesStr = configuration.getProperty("com.openexchange.mail.JavaMailProperties");
            if (null != javaMailPropertiesStr) {
                javaMailPropertiesStr = javaMailPropertiesStr.trim();
                if (javaMailPropertiesStr.indexOf("@oxgroupwaresysconfdir@") != -1) {
                    final String configPath = configuration.getProperty("CONFIGPATH");
                    if (null == configPath) {
                        throw new MailConfigException("Missing property \"CONFIGPATH\" in system.properties");
                    }
                    javaMailPropertiesStr = javaMailPropertiesStr.replaceFirst("@oxgroupwaresysconfdir@", configPath);
                }
                javaMailProperties = readPropertiesFromFile(javaMailPropertiesStr);
                if (javaMailProperties.size() == 0) {
                    javaMailProperties = null;
                }
            }
            logBuilder.append("\tJavaMail Properties loaded: ").append(javaMailProperties != null).append('\n');
        }

        logBuilder.append("Global mail properties successfully loaded!");
        if (LOG.isInfoEnabled()) {
            LOG.info(logBuilder.toString());
        }
    }

    /**
     * Reads the properties from specified property file and returns an appropriate instance of {@link Properties}
     * 
     * @param propFile The property file
     * @return The appropriate instance of {@link Properties}
     * @throws MailConfigException If reading property file fails
     */
    protected static Properties readPropertiesFromFile(final String propFile) throws MailConfigException {
        final Properties properties = new Properties();
        final FileInputStream fis;
        try {
            fis = new FileInputStream(new File(propFile));
        } catch (final FileNotFoundException e) {
            throw new MailConfigException(
                new StringBuilder(256).append("Properties not found at location: ").append(propFile).toString(),
                e);
        }
        try {
            properties.load(fis);
            return properties;
        } catch (final IOException e) {
            throw new MailConfigException(
                new StringBuilder(256).append("I/O error while reading properties from file \"").append(propFile).append("\": ").append(
                    e.getMessage()).toString(),
                e);
        } finally {
            try {
                fis.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Checks if default folders (e.g. "Sent Mail", "Drafts") are supposed to be created below personal namespace folder (INBOX) even though
     * mail server indicates to create them on the same level as personal namespace folder.
     * <p>
     * <b>Note</b> that personal namespace folder must allow subfolder creation.
     * 
     * @return <code>true</code> if default folders are supposed to be created below personal namespace folder; otherwise <code>false</code>
     */
    public boolean isAllowNestedDefaultFolderOnAltNamespace() {
        return allowNestedDefaultFolderOnAltNamespace;
    }

    /**
     * Gets the attachDisplaySize
     * 
     * @return the attachDisplaySize
     */
    public int getAttachDisplaySize() {
        return attachDisplaySize;
    }

    /**
     * Gets the defaultMimeCharset
     * 
     * @return the defaultMimeCharset
     */
    public String getDefaultMimeCharset() {
        return defaultMimeCharset;
    }

    /**
     * Gets the defaultMailProvider
     * 
     * @return the defaultMailProvider
     */
    public String getDefaultMailProvider() {
        return defaultMailProvider;
    }

    /**
     * Indicates if admin mail login is enabled; meaning whether admin user's try to login to mail system is permitted or not.
     * 
     * @return <code>true</code> if admin mail login is enabled; otherwise <code>false</code>
     */
    public boolean isAdminMailLoginEnabled() {
        return adminMailLoginEnabled;
    }

    /**
     * Gets the defaultSeparator
     * 
     * @return the defaultSeparator
     */
    public char getDefaultSeparator() {
        return defaultSeparator;
    }

    /**
     * Gets the ignoreSubscription
     * 
     * @return the ignoreSubscription
     */
    public boolean isIgnoreSubscription() {
        return ignoreSubscription;
    }

    /**
     * Gets the supportSubscription
     * 
     * @return the supportSubscription
     */
    public boolean isSupportSubscription() {
        return supportSubscription;
    }

    /**
     * Gets the javaMailProperties
     * 
     * @return the javaMailProperties
     */
    public Properties getJavaMailProperties() {
        return javaMailProperties;
    }

    /**
     * Gets the login source
     * 
     * @return the login source
     */
    public LoginSource getLoginSource() {
        return loginSource;
    }

    /**
     * Gets the password source
     * 
     * @return the password source
     */
    public PasswordSource getPasswordSource() {
        return passwordSource;
    }

    /**
     * Gets the mail server source
     * 
     * @return the mail server source
     */
    public ServerSource getMailServerSource() {
        return mailServerSource;
    }

    /**
     * Gets the transport server source
     * 
     * @return the transport server source
     */
    public ServerSource getTransportServerSource() {
        return transportServerSource;
    }

    /**
     * Gets the mailFetchLimit
     * 
     * @return the mailFetchLimit
     */
    public int getMailFetchLimit() {
        return mailFetchLimit;
    }

    /**
     * Gets the mailServer
     * 
     * @return the mailServer
     */
    public String getMailServer() {
        return mailServer;
    }

    /**
     * Gets the masterPassword
     * 
     * @return the masterPassword
     */
    public String getMasterPassword() {
        return masterPassword;
    }

    /**
     * Gets the maxNumOfConnections
     * 
     * @return the maxNumOfConnections
     */
    public int getMaxNumOfConnections() {
        return maxNumOfConnections;
    }

    /**
     * Gets the quoteLineColors
     * 
     * @return the quoteLineColors
     */
    public String[] getQuoteLineColors() {
        return quoteLineColors;
    }

    /**
     * Gets the transportServer
     * 
     * @return the transportServer
     */
    public String getTransportServer() {
        return transportServer;
    }

    /**
     * Gets the userFlagsEnabled
     * 
     * @return the userFlagsEnabled
     */
    public boolean isUserFlagsEnabled() {
        return userFlagsEnabled;
    }

    /**
     * Gets the watcherEnabled
     * 
     * @return the watcherEnabled
     */
    public boolean isWatcherEnabled() {
        return watcherEnabled;
    }

    /**
     * Gets the watcherFrequency
     * 
     * @return the watcherFrequency
     */
    public int getWatcherFrequency() {
        return watcherFrequency;
    }

    /**
     * Gets the watcherShallClose
     * 
     * @return the watcherShallClose
     */
    public boolean isWatcherShallClose() {
        return watcherShallClose;
    }

    /**
     * Gets the watcherTime
     * 
     * @return the watcherTime
     */
    public int getWatcherTime() {
        return watcherTime;
    }

    /**
     * Gets the phishing headers
     * 
     * @return The phishing headers or <code>null</code> if none defined
     */
    public String[] getPhishingHeaders() {
        if (null == phishingHeaders) {
            return null;
        }
        final String[] retval = new String[phishingHeaders.length];
        System.arraycopy(phishingHeaders, 0, retval, 0, phishingHeaders.length);
        return retval;
    }

    /**
     * Gets the mail access cache shrinker-interval seconds.
     * 
     * @return The mail access cache shrinker-interval seconds
     */
    public int getMailAccessCacheShrinkerSeconds() {
        return mailAccessCacheShrinkerSeconds;
    }

    /**
     * Gets the mail access cache idle seconds.
     * 
     * @return The mail access cache idle seconds.
     */
    public int getMailAccessCacheIdleSeconds() {
        return mailAccessCacheIdleSeconds;
    }

}
