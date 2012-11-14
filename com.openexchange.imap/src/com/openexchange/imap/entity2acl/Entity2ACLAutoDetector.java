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

package com.openexchange.imap.entity2acl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.MessagingException;
import javax.mail.internet.IDNA;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.cache.RightsCache;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.imap.ping.IMAPCapabilityAndGreetingCache;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link Entity2ACLAutoDetector} - Auto-detects {@link Entity2ACL} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Entity2ACLAutoDetector {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(Entity2ACLAutoDetector.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static ConcurrentMap<InetSocketAddress, Future<Entity2ACL>> map;

    /**
     * Prevent instantiation
     */
    private Entity2ACLAutoDetector() {
        super();
    }

    /**
     * Initializes the auto-detector
     */
    static void initEntity2ACLMappings() {
        map = new NonBlockingHashMap<InetSocketAddress, Future<Entity2ACL>>();
    }

    /**
     * Resets the auto-detector
     */
    static void resetEntity2ACLMappings() {
        map.clear();
        map = null;
    }

    /**
     * Determines the {@link Entity2ACL} implementation dependent on IMAP server's greeting.
     * <p>
     * The IMAP server name can either be a machine name, such as <code>&quot;java.sun.com&quot;</code>, or a textual representation of its
     * IP address.
     *
     * @param imapConfig The IMAP configuration
     * @return the IMAP server's depending {@link Entity2ACL} implementation
     * @throws IOException - if an I/O error occurs
     * @throws OXException - if a server greeting could not be mapped to a supported IMAP server
     */
    public static Entity2ACL getEntity2ACLImpl(final IMAPConfig imapConfig) throws IOException, OXException {
        final InetSocketAddress key = new InetSocketAddress(IDNA.toASCII(imapConfig.getServer()), imapConfig.getPort());
        Future<Entity2ACL> cached = map.get(key);
        if (null == cached) {
            final FutureTask<Entity2ACL> ft = new FutureTask<Entity2ACL>(new Entity2ACLCallable(key, imapConfig));
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
            if (cause instanceof OXException) {
                throw ((OXException) cause);
            }
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

    private static final class Entity2ACLCallable implements Callable<Entity2ACL> {

        private final InetSocketAddress key;

        private final IMAPConfig imapConfig;

        public Entity2ACLCallable(final InetSocketAddress key, final IMAPConfig imapConfig) {
            super();
            this.imapConfig = imapConfig;
            this.key = key;
        }

        @Override
        public Entity2ACL call() throws Exception {
            final String greeting = IMAPCapabilityAndGreetingCache.getGreeting(key, imapConfig.isSecure(), imapConfig.getIMAPProperties());
            return implFor(greeting, imapConfig);
        }

    } // End of Entity2ACLCallable

    /**
     * Gets the appropriate {@link Entity2ACL} implementation.
     *
     * @param greeting The greeting
     * @param imapConfig The IMAP configuration
     * @return The appropriate {@link Entity2ACL} implementation
     * @throws OXException If an error occurs
     */
    protected static Entity2ACL implFor(final String greeting, final IMAPConfig imapConfig) throws OXException {
        /*
         * Map greeting to a known IMAP server
         */
        final IMAPServer imapServer = mapInfo2IMAPServer(greeting, imapConfig);
        final Entity2ACL entity2Acl = imapServer.getImpl();
        if (DEBUG) {
            LOG.debug(new StringBuilder(256).append("\n\tIMAP server [").append(imapConfig.getServer()).append(
                "] greeting successfully mapped to: ").append(imapServer.getName()));
        }
        return entity2Acl;
    }

    private static final Map<InetSocketAddress, IMAPServer> CACHE = new NonBlockingHashMap<InetSocketAddress, IMAPServer>();

    private static IMAPServer mapInfo2IMAPServer(final String info, final IMAPConfig imapConfig) throws OXException {
        for (final IMAPServer imapServer : IMAPServer.getIMAPServers()) {
            if (imapServer.matches(info)) {
                return imapServer;
            }
        }
        /*
         * No known IMAP server found, check if ACLs are disabled anyway. If yes entity2acl is never used and can safely be mapped to
         * default implementation.
         */
        if (!imapConfig.getACLExtension().aclSupport()) {
            /*
             * Return fallback implementation
             */
            if (LOG.isWarnEnabled()) {
                final StringBuilder warnBuilder =
                    new StringBuilder(512).append("No IMAP server found ").append("that corresponds to greeting:\n\"").append(
                        info.replaceAll("\r?\n", "")).append("\" on ").append(imapConfig.getServer()).append(
                        ".\nSince ACLs are disabled (through IMAP configuration) or not supported by IMAP server, \"").append(
                        IMAPServer.CYRUS.getName()).append("\" is used as fallback.");
                LOG.warn(warnBuilder.toString());
            }
            return IMAPServer.CYRUS;
        }
        /*
         * First look-up in cache
         */
        final InetSocketAddress socketAddress;
        try {
            socketAddress = imapConfig.getImapServerSocketAddress();
        } catch (final IMAPException e) {
            throw Entity2ACLExceptionCode.UNKNOWN_IMAP_SERVER.create(e, info);
        }
        IMAPServer imapServer = CACHE.get(socketAddress);
        if (null != imapServer) {
            if (IMAPServer.UNKNOWN.equals(imapServer)) {
                throw Entity2ACLExceptionCode.UNKNOWN_IMAP_SERVER.create(info);
            }
            return imapServer;
        }
        synchronized (CACHE) {
            imapServer = CACHE.get(socketAddress);
            if (null != imapServer) {
                return imapServer;
            }
            /*
             * Try to determine ACL entities by simple checking for alias "owner"
             */
            final IMAPStore imapStore = imapConfig.optImapStore();
            if (null == imapStore) {
                throw Entity2ACLExceptionCode.UNKNOWN_IMAP_SERVER.create(info);
            }
            try {
                final IMAPFolder folder = (IMAPFolder) imapStore.getFolder("INBOX");
                if (imapConfig.getACLExtension().canGetACL(
                    RightsCache.getCachedRights(folder, true, imapConfig.getSession(), imapConfig.getAccountId()))) {
                    final ACL[] acls = folder.getACL();
                    boolean owner = false;
                    for (int i = 0; !owner && i < acls.length; i++) {
                        owner = "owner".equalsIgnoreCase(acls[i].getName());
                    }
                    imapServer = owner ? IMAPServer.COURIER : IMAPServer.CYRUS;
                    CACHE.put(socketAddress, imapServer);
                    return imapServer;
                }
                CACHE.put(socketAddress, IMAPServer.UNKNOWN);
                throw Entity2ACLExceptionCode.UNKNOWN_IMAP_SERVER.create(info);
            } catch (final MessagingException e) {
                CACHE.put(socketAddress, IMAPServer.UNKNOWN);
                throw Entity2ACLExceptionCode.UNKNOWN_IMAP_SERVER.create(e, info);
            } catch (final RuntimeException e) {
                CACHE.put(socketAddress, IMAPServer.UNKNOWN);
                throw Entity2ACLExceptionCode.UNKNOWN_IMAP_SERVER.create(e, info);
            }
        }
    }

}
