/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
    public static ACLExtension getACLExtension(IMAPConfig imapConfig) {
        return parse(imapConfig.asMap(), imapConfig);
    }

    /**
     * Determines the ACL extension dependent on IMAP server's capabilities.
     *
     * @param capabilities The capabilities map
     * @param imapConfig The IMAP configuration
     * @return The IMAP server's ACL extension.
     */
    public static ACLExtension getACLExtension(Map<String, String> capabilities, IMAPConfig imapConfig) {
        return parse(capabilities, imapConfig);
    }

    private static final char[] RFC4314_CARACTERS_UPPER = { 'K', 'X', 'T', 'E' };

    private static ACLExtension parse(Map<String, String> capabilities, IMAPConfig imapConfig) {
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
        for (String upperName : capabilities.keySet()) {
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

    /*-
     *
    private static boolean containsRFC4314Character(String rightsCapability) {
        final int fromIndex = rightsCapability.indexOf('=');
        boolean found = false;
        for (int i = 0; !found && i < RFC4314_CARACTERS_UPPER.length; i++) {
            found = (rightsCapability.indexOf(RFC4314_CARACTERS_UPPER[i], fromIndex) >= 0);
        }
        return found;
    }
     *
     */

}
