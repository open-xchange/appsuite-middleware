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

package com.openexchange.imap.acl;

import java.net.InetSocketAddress;
import java.util.Map;
import com.openexchange.imap.config.IMAPConfig;

/**
 * {@link ACLExtensionAutoDetector} - Auto-detects IMAP server's ACL extension.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ACLExtensionAutoDetector {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ACLExtensionAutoDetector.class);

    /**
     * Prevent instantiation
     */
    private ACLExtensionAutoDetector() {
        super();
    }

    /**
     * Determines the ACL extension dependent on IMAP server's capabilities.
     *
     * @param imapConfig The IMAP configuration
     * @return The IMAP server's ACL extension.
     */
    public static ACLExtension getACLExtension(final IMAPConfig imapConfig) {
        return parse(imapConfig.asMap(), imapConfig);
    }

    /**
     * Determines the ACL extension dependent on IMAP server's capabilities.
     *
     * @param capabilities The capabilities map
     * @param imapConfig The IMAP configuration
     * @return The IMAP server's ACL extension.
     */
    public static ACLExtension getACLExtension(final Map<String, String> capabilities, final IMAPConfig imapConfig) {
        return parse(capabilities, imapConfig);
    }

    private static final char[] RFC4314_CARACTERS_UPPER = { 'K', 'X', 'T', 'E' };

    private static ACLExtension parse(final Map<String, String> capabilities, final IMAPConfig imapConfig) {
        /*
         * Examine CAPABILITY response
         */
        if (!capabilities.containsKey("ACL")) {
            LOG.debug("\n\tIMAP server [{0}] CAPABILITY response indicates no support of ACL extension.", new InetSocketAddress(imapConfig.getServer(), imapConfig.getPort()));
            return NoACLExtension.getInstance();
        }
        /*
         * Check if newer ACL extension is supported
         */
        for (final String upperName : capabilities.keySet()) {
            if (upperName.startsWith("RIGHTS=", 0)) {
                /*
                 * Check if RIGHTS=... capability contains right characters specified in RFC4314
                 */
                final int fromIndex = 6; // -> upperName.indexOf('=');
                boolean containsRFC4314Character = false;
                for (int i = 0; !containsRFC4314Character && i < RFC4314_CARACTERS_UPPER.length; i++) {
                    containsRFC4314Character = (upperName.indexOf(RFC4314_CARACTERS_UPPER[i], fromIndex) >= 0);
                }
                LOG.debug("\n\tIMAP server [{0}] CAPABILITY response indicates support of ACL extension\n\tand specifies \"{}\" capability.\n\tACL extension according to {} is going to be used.\n", new InetSocketAddress(imapConfig.getServer(), imapConfig.getPort()).toString(), upperName, (containsRFC4314Character ? "RFC 4314" : "RFC 2086"));
                return containsRFC4314Character ? new RFC4314ACLExtension() : new RFC2086ACLExtension();
            }
        }
        LOG.debug("\n\tIMAP server [{}] CAPABILITY response indicates support of ACL extension\n\tbut does not specify \"RIGHTS=\" capability.\n\tACL extension according to RFC 2086 is going to be used.\n", new InetSocketAddress(imapConfig.getServer(), imapConfig.getPort()));
        return new RFC2086ACLExtension();
    }

    private static boolean containsRFC4314Character(final String rightsCapability) {
        final int fromIndex = rightsCapability.indexOf('=');
        boolean found = false;
        for (int i = 0; !found && i < RFC4314_CARACTERS_UPPER.length; i++) {
            found = (rightsCapability.indexOf(RFC4314_CARACTERS_UPPER[i], fromIndex) >= 0);
        }
        return found;
    }

}
