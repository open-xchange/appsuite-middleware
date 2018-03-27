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

import static com.openexchange.java.Autoboxing.I;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.imap.HostExtractingGreetingListener;
import com.openexchange.imap.IMAPProtocol;
import com.openexchange.imap.entity2acl.Entity2ACL;
import com.openexchange.imap.services.Services;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailConfig.BoolCapVal;
import com.openexchange.mail.config.MailConfigException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.UserAndContext;
import com.openexchange.spamhandler.SpamHandler;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link IMAPProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPProperties extends AbstractProtocolProperties implements IIMAPProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPProperties.class);

    private static final IMAPProperties instance = new IMAPProperties();

    /**
     * Gets the singleton instance of {@link IMAPProperties}
     *
     * @return The singleton instance of {@link IMAPProperties}
     */
    public static IMAPProperties getInstance() {
        return instance;
    }

    private static final class PrimaryIMAPProperties {

        private static class Params {

            HostExtractingGreetingListener hostExtractingGreetingListener;
            Boolean rootSubfoldersAllowed;
            boolean namespacePerUser;
            int umlautFilterThreshold;
            int maxMailboxNameLength;
            TIntSet invalidChars;
            boolean allowESORT;
            boolean allowSORTDISPLAY;
            boolean fallbackOnFailedSORT;
            boolean useMultipleAddresses;
            boolean useMultipleAddressesUserHash;
            int useMultipleAddressesMaxRetryAttempts;

            Params() {
                super();
                useMultipleAddressesMaxRetryAttempts = -1;
            }
        }

        // -------------------------------------------------------------------------------------------------------

        final HostExtractingGreetingListener hostExtractingGreetingListener;
        final Boolean rootSubfoldersAllowed;
        final boolean namespacePerUser;
        final int umlautFilterThreshold;
        final int maxMailboxNameLength;
        final TIntSet invalidChars;
        final boolean allowESORT;
        final boolean allowSORTDISPLAY;
        final boolean fallbackOnFailedSORT;
        final boolean useMultipleAddresses;
        final boolean useMultipleAddressesUserHash;
        final int useMultipleAddressesMaxRetryAttempts;

        PrimaryIMAPProperties(Params params) {
            super();
            this.hostExtractingGreetingListener = params.hostExtractingGreetingListener;
            this.rootSubfoldersAllowed = params.rootSubfoldersAllowed;
            this.namespacePerUser = params.namespacePerUser;
            this.umlautFilterThreshold = params.umlautFilterThreshold;
            this.maxMailboxNameLength = params.maxMailboxNameLength;
            this.invalidChars = params.invalidChars;
            this.allowESORT = params.allowESORT;
            this.allowSORTDISPLAY = params.allowSORTDISPLAY;
            this.fallbackOnFailedSORT = params.fallbackOnFailedSORT;
            this.useMultipleAddresses = params.useMultipleAddresses;
            this.useMultipleAddressesUserHash = params.useMultipleAddressesUserHash;
            this.useMultipleAddressesMaxRetryAttempts = params.useMultipleAddressesMaxRetryAttempts;
        }
    }

    private static final Cache<UserAndContext, PrimaryIMAPProperties> CACHE_PRIMARY_PROPS = CacheBuilder.newBuilder().maximumSize(65536).expireAfterAccess(30, TimeUnit.MINUTES).build();

    /**
     * Clears the cache.
     */
    public static void invalidateCache() {
        CACHE_PRIMARY_PROPS.invalidateAll();
    }

    private static PrimaryIMAPProperties getPrimaryIMAPProps(final int userId, final int contextId) throws OXException {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        PrimaryIMAPProperties primaryMailProps = CACHE_PRIMARY_PROPS.getIfPresent(key);
        if (null != primaryMailProps) {
            return primaryMailProps;
        }

        Callable<PrimaryIMAPProperties> loader = new Callable<PrimaryIMAPProperties>() {

            @Override
            public PrimaryIMAPProperties call() throws Exception {
                return doGetPrimaryIMAPProps(userId, contextId);
            }
        };

        try {
            return CACHE_PRIMARY_PROPS.get(key, loader);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw cause instanceof OXException ? (OXException) cause : new OXException(cause);
        }
    }

    static PrimaryIMAPProperties doGetPrimaryIMAPProps(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);

        PrimaryIMAPProperties.Params params = new PrimaryIMAPProperties.Params();

        StringBuilder logMessageBuilder = new StringBuilder(1024);
        List<Object> args = new ArrayList<>(16);

        logMessageBuilder.append("Primary IMAP properties successfully loaded for user {} in context {}{}");
        args.add(Integer.valueOf(userId));
        args.add(Integer.valueOf(contextId));
        args.add(Strings.getLineSeparator());

        {
            String tmp = ConfigViews.getNonEmptyPropertyFrom("com.openexchange.imap.greeting.host.regex", view);
            tmp = Strings.isEmpty(tmp) ? null : tmp;
            if (null != tmp) {
                try {
                    Pattern pattern = Pattern.compile(tmp);
                    params.hostExtractingGreetingListener = new HostExtractingGreetingListener(pattern);

                    logMessageBuilder.append("  Host name regular expression: {}{}");
                    args.add(tmp);
                    args.add(Strings.getLineSeparator());
                } catch (PatternSyntaxException e) {
                    LOG.warn("Invalid expression for host name", e);
                    logMessageBuilder.append("  Host name regular expression: {}{}");
                    args.add("<none>");
                    args.add(Strings.getLineSeparator());
                }
            }
        }

        {
            String tmp = ConfigViews.getNonEmptyPropertyFrom("com.openexchange.imap.rootSubfoldersAllowed", view);
            if (null == tmp) {
                params.rootSubfoldersAllowed = null;
            } else {
                params.rootSubfoldersAllowed = Boolean.valueOf(tmp);

                logMessageBuilder.append("  Root sub-folders allowed: {}{}");
                args.add(params.rootSubfoldersAllowed);
                args.add(Strings.getLineSeparator());
            }
        }

        {
            params.namespacePerUser = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.imap.namespacePerUser", false, view);

            logMessageBuilder.append("  Namespace per User: {}{}");
            args.add(Autoboxing.valueOf(params.namespacePerUser));
            args.add(Strings.getLineSeparator());
        }

        {
            params.umlautFilterThreshold = ConfigViews.getDefinedIntPropertyFrom("com.openexchange.imap.umlautFilterThreshold", 50, view);

            logMessageBuilder.append("  Umlaut filter threshold: {}{}");
            args.add(Autoboxing.valueOf(params.umlautFilterThreshold));
            args.add(Strings.getLineSeparator());
        }

        {
            params.maxMailboxNameLength = ConfigViews.getDefinedIntPropertyFrom("com.openexchange.imap.maxMailboxNameLength", 60, view);

            logMessageBuilder.append("  Max. Mailbox Name Length: {}{}");
            args.add(Autoboxing.valueOf(params.maxMailboxNameLength));
            args.add(Strings.getLineSeparator());
        }

        {
            String invalids = ConfigViews.getNonEmptyPropertyFrom("com.openexchange.imap.invalidMailboxNameCharacters", view);
            if (Strings.isEmpty(invalids)) {
                params.invalidChars = new TIntHashSet(0);
            } else {
                final String[] sa = Strings.splitByWhitespaces(Strings.unquote(invalids));
                final int length = sa.length;
                TIntSet invalidChars = new TIntHashSet(length);
                for (int i = 0; i < length; i++) {
                    invalidChars.add(sa[i].charAt(0));
                }

                params.invalidChars = invalidChars;

                logMessageBuilder.append("  Invalid Mailbox Characters: {}{}");
                args.add(invalids);
                args.add(Strings.getLineSeparator());
            }

        }

        {
            params.allowESORT = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.imap.allowESORT", true, view);

            logMessageBuilder.append("  Allow ESORT: {}{}");
            args.add(Autoboxing.valueOf(params.allowESORT));
            args.add(Strings.getLineSeparator());
        }

        {
            params.allowSORTDISPLAY = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.imap.allowSORTDISPLAY", false, view);

            logMessageBuilder.append("  Allow SORT-DSIPLAY: {}{}");
            args.add(Autoboxing.valueOf(params.allowSORTDISPLAY));
            args.add(Strings.getLineSeparator());
        }

        {
            params.fallbackOnFailedSORT = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.imap.fallbackOnFailedSORT", false, view);

            logMessageBuilder.append("  Fallback On Failed SORT: {}{}");
            args.add(Autoboxing.valueOf(params.fallbackOnFailedSORT));
            args.add(Strings.getLineSeparator());
        }

        {
            params.useMultipleAddresses = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.imap.useMultipleAddresses", false, view);

            logMessageBuilder.append("  Use Multiple IP addresses: {}{}");
            args.add(Autoboxing.valueOf(params.useMultipleAddresses));
            args.add(Strings.getLineSeparator());
        }

        if (params.useMultipleAddresses) {
            params.useMultipleAddressesUserHash = ConfigViews.getDefinedBoolPropertyFrom("com.openexchange.imap.useMultipleAddressesUserHash", false, view);

            logMessageBuilder.append("  Use User Hash for Multiple IP addresses: {}{}");
            args.add(Autoboxing.valueOf(params.useMultipleAddressesUserHash));
            args.add(Strings.getLineSeparator());
        }

        if (params.useMultipleAddresses) {
            params.useMultipleAddressesMaxRetryAttempts = ConfigViews.getDefinedIntPropertyFrom("com.openexchange.imap.useMultipleAddressesMaxRetries", 3, view);

            logMessageBuilder.append("  Use max. retry attempts for Multiple IP addresses: {}{}");
            args.add(Autoboxing.valueOf(params.useMultipleAddressesMaxRetryAttempts));
            args.add(Strings.getLineSeparator());
        }

        PrimaryIMAPProperties primaryIMAPProps = new PrimaryIMAPProperties(params);
        LOG.debug(logMessageBuilder.toString(), args.toArray(new Object[args.size()]));
        return primaryIMAPProps;
    }

    // --------------------------------------------------------------------------------------------------------------

    /*-
     * Fields for global properties
     */

    private final IMailProperties mailProperties;

    private boolean imapSort;

    private boolean imapSearch;

    private boolean forceImapSearch;

    private boolean fastFetch;

    private BoolCapVal supportsACLs;

    private int imapTimeout;

    private int imapConnectionTimeout;

    private int imapTemporaryDown;

    private int imapFailedAuthTimeout;

    private String imapAuthEnc;

    private String entity2AclImpl;

    private int blockSize;

    private int maxNumConnection;

    private final Map<String, Boolean> newACLExtMap;

    private String spamHandlerName;

    private boolean propagateClientIPAddress;

    private boolean enableTls;

    private boolean auditLogEnabled;

    private boolean overwritePreLoginCapabilities;

    private Set<String> propagateHostNames;

    private boolean allowFolderCaches;

    private boolean allowFetchSingleHeaders;

    private String sContainerType;

    private String sslProtocols;

    private String cipherSuites;

    private HostExtractingGreetingListener hostExtractingGreetingListener;

    /**
     * Initializes a new {@link IMAPProperties}
     */
    private IMAPProperties() {
        super();
        sContainerType = "boundary-aware";
        enableTls = true;
        auditLogEnabled = false;
        overwritePreLoginCapabilities = false;
        maxNumConnection = -1;
        newACLExtMap = new NonBlockingHashMap<String, Boolean>();
        mailProperties = MailProperties.getInstance();
        propagateHostNames = Collections.emptySet();
        allowFetchSingleHeaders = true;
        allowFolderCaches = true;
        hostExtractingGreetingListener = null;
    }

    @Override
    protected void loadProperties0() throws OXException {
        final StringBuilder logBuilder = new StringBuilder(1024);
        logBuilder.append("\nLoading global IMAP properties...\n");

        final ConfigurationService configuration = Services.getService(ConfigurationService.class);
        {
            final String allowFolderCachesStr = configuration.getProperty("com.openexchange.imap.allowFolderCaches", "true").trim();
            allowFolderCaches = "true".equalsIgnoreCase(allowFolderCachesStr);
            logBuilder.append("\tIMAP allow folder caches: ").append(allowFolderCaches).append('\n');
        }

        {
            final String str = configuration.getProperty("com.openexchange.imap.allowFetchSingleHeaders", "true").trim();
            allowFetchSingleHeaders = "true".equalsIgnoreCase(str);
            logBuilder.append("\tIMAP allow FETCH single headers: ").append(allowFetchSingleHeaders).append('\n');
        }

        {
            final String imapSortStr = configuration.getProperty("com.openexchange.imap.imapSort", "application").trim();
            imapSort = "imap".equalsIgnoreCase(imapSortStr);
            logBuilder.append("\tIMAP-Sort: ").append(imapSort).append('\n');
        }

        {
            final String imapSearchStr = configuration.getProperty("com.openexchange.imap.imapSearch", "force-imap").trim();
            forceImapSearch = "force-imap".equalsIgnoreCase(imapSearchStr);
            imapSearch = forceImapSearch || "imap".equalsIgnoreCase(imapSearchStr);
            logBuilder.append("\tIMAP-Search: ").append(imapSearch).append(forceImapSearch ? " (forced)\n" : "\n");
        }

        {
            final String fastFetchStr = configuration.getProperty("com.openexchange.imap.imapFastFetch", STR_TRUE).trim();
            fastFetch = Boolean.parseBoolean(fastFetchStr);
            logBuilder.append("\tFast Fetch Enabled: ").append(fastFetch).append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.imap.propagateClientIPAddress", STR_FALSE).trim();
            propagateClientIPAddress = Boolean.parseBoolean(tmp);
            logBuilder.append("\tPropagate Client IP Address: ").append(propagateClientIPAddress).append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.imap.enableTls", STR_TRUE).trim();
            enableTls = Boolean.parseBoolean(tmp);
            logBuilder.append("\tEnable TLS: ").append(enableTls).append('\n');
        }

        {
            String tmp = configuration.getProperty("com.openexchange.imap.auditLog.enabled", STR_FALSE).trim();
            auditLogEnabled = Boolean.parseBoolean(tmp);
            logBuilder.append("\tAudit Log Enabled: ").append(auditLogEnabled).append('\n');
        }

        {
            String tmp = configuration.getProperty("com.openexchange.imap.overwritePreLoginCapabilities", STR_FALSE).trim();
            overwritePreLoginCapabilities = Boolean.parseBoolean(tmp);
            logBuilder.append("\tOverwrite Pre-Login Capabilities: ").append(overwritePreLoginCapabilities).append('\n');
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.imap.propagateHostNames", "").trim();
            if (tmp.length() > 0) {
                propagateHostNames = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(tmp.split(" *, *"))));
            } else {
                propagateHostNames = Collections.emptySet();
            }
            logBuilder.append("\tPropagate Host Names: ").append(propagateHostNames.isEmpty() ? "<none>" : propagateHostNames.toString()).append(
                '\n');
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

        /*{
            final String fetchTimeoutMillisStr = configuration.getProperty("com.openexchange.imap.fetchTimeoutMillis", "10000").trim();
            try {
                fetchTimeout = Integer.parseInt(fetchTimeoutMillisStr);
                logBuilder.append("\tIMAP FETCH Timeout: ").append(fetchTimeout).append('\n');
            } catch (final NumberFormatException e) {
                fetchTimeout = 10000;
                logBuilder.append("\tIMAP FETCH Timeout: Invalid value \"").append(fetchTimeoutMillisStr).append(
                    "\". Setting to fallback: ").append(fetchTimeout).append('\n');
            }
        }*/

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
            final String imapFailedAuthTimeoutStr = configuration.getProperty("com.openexchange.imap.failedAuthTimeout", "10000").trim();
            try {
                imapFailedAuthTimeout = Integer.parseInt(imapFailedAuthTimeoutStr);
                logBuilder.append("\tIMAP Failed Auth Timeout: ").append(imapFailedAuthTimeout).append('\n');
            } catch (final NumberFormatException e) {
                imapFailedAuthTimeout = 10000;
                logBuilder.append("\tIMAP Failed Auth Timeout: Invalid value \"").append(imapFailedAuthTimeoutStr).append("\". Setting to fallback: ").append(
                    imapFailedAuthTimeout).append('\n');
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
                throw MailConfigException.create("Missing IMAP property \"com.openexchange.imap.User2ACLImpl\"");
            }
            entity2AclImpl = entity2AclImpl.trim();
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

        {
            String tmp = configuration.getProperty("com.openexchange.imap.maxNumExternalConnections");
            if (null != tmp) {
                tmp = tmp.trim();
                if (0 == tmp.length()) {
                    IMAPProtocol.getInstance().setOverallExternalMaxCount(-1);
                    logBuilder.append("\tMax. Number of External Connections: ").append("No restrictions").append('\n');
                } else if (tmp.indexOf(':') > 0) {
                    // Expect a comma-separated list
                    final String[] sa = tmp.split(" *, *");
                    if (sa.length > 0) {
                        try {
                            final IMAPProtocol imapProtocol = IMAPProtocol.getInstance();
                            imapProtocol.initExtMaxCountMap();
                            final StringBuilder sb = new StringBuilder(128).append("\tMax. Number of External Connections: ");
                            boolean first = true;
                            for (final String desc : sa) {
                                final int pos = desc.indexOf(':');
                                if (pos > 0) {
                                    try {
                                        imapProtocol.putIfAbsent(desc.substring(0, pos), Integer.parseInt(desc.substring(pos + 1).trim()));
                                        if (first) {
                                            first = false;
                                        } else {
                                            sb.append(", ");
                                        }
                                        sb.append(desc);
                                    } catch (final RuntimeException e) {
                                        LOG.warn("Max. Number of External Connections: Invalid entry: {}", desc, e);
                                    }
                                }
                            }
                            logBuilder.append(sb).append('\n');
                        } catch (final NumberFormatException e) {
                            IMAPProtocol.getInstance().setOverallExternalMaxCount(-1);
                            logBuilder.append("\tMax. Number of External Connections: Invalid value \"").append(tmp).append(
                                "\". Setting to fallback: No restrictions").append('\n');
                        } catch (final RuntimeException e) {
                            IMAPProtocol.getInstance().setOverallExternalMaxCount(-1);
                            logBuilder.append("\tMax. Number of External Connections: Invalid value \"").append(tmp).append(
                                "\". Setting to fallback: No restrictions").append('\n');
                        }
                    }
                } else {
                    // Expect a single integer value
                    try {
                        IMAPProtocol.getInstance().setOverallExternalMaxCount(Integer.parseInt(tmp));
                        logBuilder.append("\tMax. Number of External Connections: ").append(tmp).append(
                            " (applied to all external IMAP accounts)").append('\n');
                    } catch (final NumberFormatException e) {
                        IMAPProtocol.getInstance().setOverallExternalMaxCount(-1);
                        logBuilder.append("\tMax. Number of External Connections: Invalid value \"").append(tmp).append(
                            "\". Setting to fallback: No restrictions").append('\n');
                    }
                }
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.imap.maxNumConnections", "-1").trim();
            try {
                maxNumConnection = Integer.parseInt(tmp);
                logBuilder.append("\tMax. Number of connections: ").append(maxNumConnection).append('\n');
                if (maxNumConnection > 0) {
                    IMAPProtocol.getInstance().setMaxCount(maxNumConnection);
                }
            } catch (final NumberFormatException e) {
                maxNumConnection = -1;
                logBuilder.append("\tMax. Number of connections: Invalid value \"").append(tmp).append("\". Setting to fallback: ").append(
                    maxNumConnection).append('\n');
            }
        }

        {
            final String tmp = configuration.getProperty("com.openexchange.imap.storeContainerType", "boundary-aware").trim();
            sContainerType = tmp;
            logBuilder.append("\tStore container type: ").append(sContainerType).append('\n');
        }

        spamHandlerName = configuration.getProperty("com.openexchange.imap.spamHandler", SpamHandler.SPAM_HANDLER_FALLBACK).trim();
        logBuilder.append("\tSpam Handler: ").append(spamHandlerName).append('\n');

        sslProtocols = configuration.getProperty("com.openexchange.imap.ssl.protocols", "SSLv3 TLSv1").trim();
        logBuilder.append("\tSupported SSL protocols: ").append(sslProtocols).append("\n");

        {
            final String tmp = configuration.getProperty("com.openexchange.imap.ssl.ciphersuites", "").trim();
            this.cipherSuites = Strings.isEmpty(tmp) ? null : tmp;
            logBuilder.append("\tSupported SSL cipher suites: ").append(null == this.cipherSuites ? "<default>" : cipherSuites).append("\n");
        }

        {
            String tmp = configuration.getProperty("com.openexchange.imap.greeting.host.regex", "").trim();
            tmp = Strings.isEmpty(tmp) ? null : tmp;
            if (null != tmp) {
                try {
                    Pattern pattern = Pattern.compile(tmp);
                    hostExtractingGreetingListener = new HostExtractingGreetingListener(pattern);
                    logBuilder.append("\tHost name regular expression: ").append(tmp).append("\n");
                } catch (PatternSyntaxException e) {
                    logBuilder.append("\tHost name regular expression: Invalid value \"").append(tmp).append("\". Using no host name extraction\n");
                }
            }
        }

        logBuilder.append("Global IMAP properties successfully loaded!");

        LOG.info(logBuilder.toString());
    }

    @Override
    protected void resetFields() {
        imapSort = false;
        imapSearch = false;
        forceImapSearch = false;
        fastFetch = true;
        propagateClientIPAddress = false;
        enableTls = true;
        auditLogEnabled = false;
        overwritePreLoginCapabilities = false;
        propagateHostNames = Collections.emptySet();
        supportsACLs = null;
        imapTimeout = 0;
        imapConnectionTimeout = 0;
        imapTemporaryDown = 0;
        imapFailedAuthTimeout = 10000;
        imapAuthEnc = null;
        entity2AclImpl = null;
        blockSize = 0;
        maxNumConnection = -1;
        sContainerType = "boundary-aware";
        spamHandlerName = null;
        sslProtocols = "SSLv3 TLSv1";
        cipherSuites = null;
        hostExtractingGreetingListener = null;
    }

    /**
     * Gets the container type.
     *
     * @return The container type
     */
    public String getsContainerType() {
        return sContainerType;
    }

    /**
     * Gets the {@link Entity2ACL}.
     *
     * @return The {@link Entity2ACL}
     */
    public String getEntity2AclImpl() {
        return entity2AclImpl;
    }

    /**
     * Gets the spam handler name.
     *
     * @return The spam handler name
     */
    public String getSpamHandlerName() {
        return spamHandlerName;
    }

    /**
     * Gets the greeting listener to parse the host name information from <b><i>primary</i></b> IMAP server's greeting string.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The host name extractor or <code>null</code>
     */
    public HostExtractingGreetingListener getHostNameRegex(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.hostExtractingGreetingListener;
        } catch (Exception e) {
            LOG.error("Failed to get host name expression for user {} in context {}. Using default default {} instead.", I(userId), I(contextId), hostExtractingGreetingListener, e);
            return hostExtractingGreetingListener;
        }
    }

    /**
     * Checks whether possible multiple IP addresses for a host name are supposed to be considered.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> to use multiple IP addresses; otherwise <code>false</code>
     */
    public boolean isUseMultipleAddresses(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.useMultipleAddresses;
        } catch (Exception e) {
            LOG.error("Failed to check for usage of multiple addresses for user {} in context {}. Using default default {} instead.", I(userId), I(contextId), Boolean.FALSE.toString(), e);
            return false;
        }
    }

    /**
     * Checks whether a user hash should be used for selecting one of possible multiple IP addresses for a host name.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Only effective if {@link #isUseMultipleAddresses(int, int)} returns <code>true</code>!
     * </div>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> to use multiple IP addresses; otherwise <code>false</code>
     */
    public boolean isUseMultipleAddressesUserHash(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.useMultipleAddressesUserHash;
        } catch (Exception e) {
            LOG.error("Failed to get hash for multiple addresses for user {} in context {}. Using default default {} instead.", I(userId), I(contextId), Boolean.FALSE.toString(), e);
            return false;
        }
    }

    /**
     * Gets the max. number of retry attempts when failing over to another IP address-
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Only effective if {@link #isUseMultipleAddresses(int, int)} returns <code>true</code>!
     * </div>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The max. wait timeout or <code>-1</code>
     */
    public int getMultipleAddressesMaxRetryAttempts(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.useMultipleAddressesMaxRetryAttempts;
        } catch (Exception e) {
            LOG.error("Failed to get max. retry attempts for multiple addresses for user {} in context {}. Using default default {} instead.", I(userId), I(contextId), Integer.valueOf(-1), e);
            return -1;
        }
    }

    /**
     * Checks whether root sub-folders are allowed for primary IMAP server.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public Boolean areRootSubfoldersAllowed(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.rootSubfoldersAllowed;
        } catch (Exception e) {
            LOG.error("Failed to get rootSubfoldersAllowed for user {} in context {}. Using default default instead.", I(userId), I(contextId), e);
            return null;
        }
    }

    /**
     * Checks whether to assume a namespace per user for primary IMAP server.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> to assume a namespace per user; otherwise <code>false</code>
     */
    public boolean isNamespacePerUser(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.namespacePerUser;
        } catch (Exception e) {
            LOG.error("Failed to get namespacePerUser for user {} in context {}. Using default default instead.", I(userId), I(contextId), e);
            return true;
        }
    }

    /**
     * Gets the threshold when to manually search with respect to umlauts.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The threshold
     */
    public int getUmlautFilterThreshold(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.umlautFilterThreshold;
        } catch (Exception e) {
            LOG.error("Failed to get umlautFilterThreshold for user {} in context {}. Using default default instead.", I(userId), I(contextId), e);
            return 50;
        }
    }

    /**
     * Gets max. length for a mailbox name.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The length
     */
    public int getMaxMailboxNameLength(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.maxMailboxNameLength;
        } catch (Exception e) {
            LOG.error("Failed to get maxMailboxNameLength for user {} in context {}. Using default default instead.", I(userId), I(contextId), e);
            return 60;
        }
    }

    /**
     * Gets the threshold when to manually search with respect to umlauts.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The threshold
     */
    public TIntSet getInvalidChars(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.invalidChars;
        } catch (Exception e) {
            LOG.error("Failed to get invalidChars for user {} in context {}. Using default default instead.", I(userId), I(contextId), e);
            return new TIntHashSet(0);
        }
    }

    /**
     * Whether ESORT is allowed to be utilized
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public boolean allowESORT(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.allowESORT;
        } catch (Exception e) {
            LOG.error("Failed to get allowESORT for user {} in context {}. Using default default instead.", I(userId), I(contextId), e);
            return true;
        }
    }

    /**
     * Whether ESORT is allowed to be utilized
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public boolean allowSORTDISPLAY(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.allowSORTDISPLAY;
        } catch (Exception e) {
            LOG.error("Failed to get allowSORTDISPLAY for user {} in context {}. Using default default instead.", I(userId), I(contextId), e);
            return false;
        }
    }

    /**
     * Whether in-app sort is supposed to be utilized if IMAP-side SORT fails with a "NO" response
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public boolean fallbackOnFailedSORT(int userId, int contextId) {
        try {
            PrimaryIMAPProperties primaryIMAPProps = getPrimaryIMAPProps(userId, contextId);
            return primaryIMAPProps.fallbackOnFailedSORT;
        } catch (Exception e) {
            LOG.error("Failed to get fallbackOnFailedSORT for user {} in context {}. Using default default instead.", I(userId), I(contextId), e);
            return false;
        }
    }

    // -----------------------------------------------------------------------------------------------------------

    @Override
    public boolean isFastFetch() {
        return fastFetch;
    }

    @Override
    public boolean isPropagateClientIPAddress() {
        return propagateClientIPAddress;
    }

    @Override
    public boolean isEnableTls() {
        return enableTls;
    }

    @Override
    public boolean isAuditLogEnabled() {
        return auditLogEnabled;
    }

    @Override
    public boolean isOverwritePreLoginCapabilities() {
        return overwritePreLoginCapabilities;
    }

    @Override
    public Set<String> getPropagateHostNames() {
        return propagateHostNames;
    }

    @Override
    public String getImapAuthEnc() {
        return imapAuthEnc;
    }

    @Override
    public int getImapConnectionTimeout() {
        return imapConnectionTimeout;
    }

    @Override
    public int getImapTemporaryDown() {
        return imapTemporaryDown;
    }

    @Override
    public int getImapFailedAuthTimeout() {
        return imapFailedAuthTimeout;
    }

    @Override
    public boolean isImapSearch() {
        return imapSearch;
    }

    @Override
    public boolean forceImapSearch() {
        return forceImapSearch;
    }

    @Override
    public boolean isImapSort() {
        return imapSort;
    }

    @Override
    public int getImapTimeout() {
        return imapTimeout;
    }

    @Override
    public BoolCapVal getSupportsACLs() {
        return supportsACLs;
    }

    @Override
    public int getBlockSize() {
        return blockSize;
    }

    @Override
    public int getMaxNumConnection() {
        return maxNumConnection;
    }

    @Override
    public Map<String, Boolean> getNewACLExtMap() {
        return newACLExtMap;
    }

    @Override
    public int getMailFetchLimit() {
        return mailProperties.getMailFetchLimit();
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
    public boolean allowFolderCaches() {
        return allowFolderCaches;
    }

    @Override
    public boolean allowFetchSingleHeaders() {
        return allowFetchSingleHeaders;
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
