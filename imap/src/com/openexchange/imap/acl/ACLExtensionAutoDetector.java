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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    private static final ConcurrentMap<InetSocketAddress, ACLExtension> map = new ConcurrentHashMap<InetSocketAddress, ACLExtension>();

    /**
     * Prevent instantiation
     */
    private ACLExtensionAutoDetector() {
        super();
    }

    /**
     * Resets the auto-detector
     */
    static void resetACLExtensionMappings() {
        map.clear();
    }

    /**
     * Determines the ACL extension dependent on IMAP server's capabilities.
     * <p>
     * The IMAP server name can either be a machine name, such as <code>&quot;java.sun.com&quot;</code>, or a textual representation of its
     * IP address.
     * 
     * @param imapConfig The IMAP configuration
     * @return The IMAP server's ACL extension.
     * @throws IOException If an I/O error occurs
     */
    public static ACLExtension getACLExtension(final IMAPConfig imapConfig) throws IOException {
        final InetSocketAddress key = new InetSocketAddress(imapConfig.getServer(), imapConfig.getPort());
        final ACLExtension cached = map.get(key);
        if (null != cached) {
            return cached;
        }
        putACLExtension(key, imapConfig.isSecure(), imapConfig.getIMAPProperties());
        return map.get(key);
    }

    private static final Pattern PAT_ACL = Pattern.compile("(^|\\s)(ACL)(\\s+|$)");

    private static final Pattern PAT_RIGHTS = Pattern.compile("(?:^|\\s)(?:RIGHTS=)([a-zA-Z0-9]+)(?:\\s+|$)");

    private static void putACLExtension(final InetSocketAddress key, final boolean isSecure, final IIMAPProperties imapProperties) throws IOException {
        if (map.containsKey(key)) {
            return;
        }
        final String capabilities = IMAPCapabilityAndGreetingCache.getCapability(key, isSecure, imapProperties);
        /*
         * Examine CAPABILITY response
         */
        final boolean hasACL = PAT_ACL.matcher(capabilities).find();
        if (!hasACL) {
            map.put(key, NoACLExtension.getInstance());
            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(key).append(
                    "] CAPABILITY response indicates no support of ACL extension."));
            }
            return;
        }
        final Matcher m = PAT_RIGHTS.matcher(capabilities);
        if (m.find()) {
            final String allowedRights = m.group(1);
            /*
             * Check if "RIGHTS=" provides any of new characters "k", "x", "t", or "e" as defined in RFC 4314
             */
            final boolean containsRFC4314Character = containsRFC4314Character(allowedRights);
            map.put(key, containsRFC4314Character ? new RFC4314ACLExtension() : new RFC2086ACLExtension());
            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(key).append(
                    "] CAPABILITY response indicates support of ACL extension\n\tand specifies \"RIGHTS=").append(allowedRights).append(
                    "\" capability.").append("\n\tACL extension according to ").append(containsRFC4314Character ? "RFC 4314" : "RFC 2086").append(
                    " is going to be used.\n"));
            }
            return;
        }
        map.putIfAbsent(key, new RFC2086ACLExtension());
        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuilder(256).append("\n\tIMAP server [").append(key).append(
                "] CAPABILITY response indicates support of ACL extension\n\tbut does not specify \"RIGHTS=\" capability.").append(
                "\n\tACL extension according to RFC 2086 is going to be used.\n"));
        }
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
