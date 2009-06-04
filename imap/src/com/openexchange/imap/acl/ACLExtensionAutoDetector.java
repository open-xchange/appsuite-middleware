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

package com.openexchange.imap.acl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.imap.config.IIMAPProperties;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.ping.IMAPCapabilityAndGreetingCache;

/**
 * {@link ACLExtensionAutoDetector} - Auto-detects IMAP server's ACL extension.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ACLExtensionAutoDetector {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ACLExtensionAutoDetector.class);

    private static ConcurrentMap<InetSocketAddress, Future<ACLExtension>> map;

    /**
     * Prevent instantiation
     */
    private ACLExtensionAutoDetector() {
        super();
    }

    /**
     * Initializes the auto-detector
     */
    static void initACLExtensionMappings() {
        map = new ConcurrentHashMap<InetSocketAddress, Future<ACLExtension>>();
    }

    /**
     * Resets the auto-detector
     */
    static void resetACLExtensionMappings() {
        map.clear();
        map = null;
    }

    /**
     * Determines the ACL extension dependent on IMAP server's capabilities.
     * 
     * @param imapConfig The IMAP configuration
     * @return The IMAP server's ACL extension.
     * @throws IOException If an I/O error occurs
     */
    public static ACLExtension getACLExtension(final IMAPConfig imapConfig) throws IOException {
        final InetSocketAddress key = new InetSocketAddress(imapConfig.getServer(), imapConfig.getPort());
        Future<ACLExtension> cached = map.get(key);
        if (null == cached) {
            final FutureTask<ACLExtension> ft = new FutureTask<ACLExtension>(new ACLExtensionCallable(
                key,
                imapConfig.isSecure(),
                imapConfig.getIMAPProperties(),
                LOG));
            cached = map.putIfAbsent(key, ft);
            if (null == cached) {
                cached = ft;
                ft.run();
            }
        }
        try {
            return cached.get();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw new IOException(e.getMessage());
        } catch (final CancellationException e) {
            throw new IOException(e.getMessage());
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw ((IOException) cause);
            }
            if (cause instanceof RuntimeException) {
                throw new IOException(e.getMessage());
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    private static final class ACLExtensionCallable implements Callable<ACLExtension> {

        private static final Pattern PAT_ACL = Pattern.compile("(^|\\s)(ACL)(\\s+|$)");

        private static final Pattern PAT_RIGHTS = Pattern.compile("(?:^|\\s)(?:RIGHTS=)([a-zA-Z0-9]+)(?:\\s+|$)");

        private final org.apache.commons.logging.Log logger;

        private final InetSocketAddress key;

        private final boolean isSecure;

        private final IIMAPProperties imapProperties;

        public ACLExtensionCallable(final InetSocketAddress key, final boolean isSecure, final IIMAPProperties imapProperties, final org.apache.commons.logging.Log logger) {
            super();
            this.logger = logger;
            this.imapProperties = imapProperties;
            this.isSecure = isSecure;
            this.key = key;
        }

        public ACLExtension call() throws Exception {
            final String capabilities = IMAPCapabilityAndGreetingCache.getCapability(key, isSecure, imapProperties);
            /*
             * Examine CAPABILITY response
             */
            final boolean hasACL = PAT_ACL.matcher(capabilities).find();
            if (!hasACL) {
                if (logger.isInfoEnabled()) {
                    logger.info(new StringBuilder(256).append("\n\tIMAP server [").append(key).append(
                        "] CAPABILITY response indicates no support of ACL extension."));
                }
                return NoACLExtension.getInstance();
            }
            final Matcher m = PAT_RIGHTS.matcher(capabilities);
            if (m.find()) {
                final String allowedRights = m.group(1);
                /*
                 * Check if "RIGHTS=" provides any of new characters "k", "x", "t", or "e" as defined in RFC 4314
                 */
                final boolean containsRFC4314Character = containsRFC4314Character(allowedRights);
                if (logger.isInfoEnabled()) {
                    logger.info(new StringBuilder(256).append("\n\tIMAP server [").append(key).append(
                        "] CAPABILITY response indicates support of ACL extension\n\tand specifies \"RIGHTS=").append(allowedRights).append(
                        "\" capability.").append("\n\tACL extension according to ").append(
                        containsRFC4314Character ? "RFC 4314" : "RFC 2086").append(" is going to be used.\n"));
                }
                return containsRFC4314Character ? new RFC4314ACLExtension() : new RFC2086ACLExtension();
            }
            if (logger.isInfoEnabled()) {
                logger.info(new StringBuilder(256).append("\n\tIMAP server [").append(key).append(
                    "] CAPABILITY response indicates support of ACL extension\n\tbut does not specify \"RIGHTS=\" capability.").append(
                    "\n\tACL extension according to RFC 2086 is going to be used.\n"));
            }
            return new RFC2086ACLExtension();
        }

        private static final char[] RFC4314_CARACTERS = { 'k', 'x', 't', 'e' };

        private static boolean containsRFC4314Character(final String allowedRights) {
            boolean found = false;
            for (int i = 0; !found && i < RFC4314_CARACTERS.length; i++) {
                found = (allowedRights.indexOf(RFC4314_CARACTERS[i]) >= 0);
            }
            return found;
        }

    }

}
