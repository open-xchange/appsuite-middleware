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

package com.openexchange.mail.utils;

import java.util.Set;
import javax.mail.internet.InternetAddress;
import com.openexchange.contact.ContactService;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.MsisdnCheck;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * Utility class to check and handle actions if MSISDN is enabled
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.2.2
 */
public class MsisdnUtility {

    /**
     * logger
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MsisdnUtility.class);

    /**
     * Prevent instantiation of a new {@link MsisdnUtility}.
     */
    private MsisdnUtility() {
        super();
    }

    /**
     * Adds the MSISDN number to the given address set.
     *
     * @param addresses - current address set to add the MSISDN number into
     * @param session - session to get the current contact and receive the number.
     */
    public static void addMsisdnAddress(Set<InternetAddress> addresses, Session session) {
        final ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        if (null != contactService) {
            try {
                final Contact contact = contactService.getUser(session, session.getUserId());
                final Set<String> set = ContactUtil.gatherTelephoneNumbers(contact);
                for (String number : set) {
                    try {
                        addresses.add(new QuotedInternetAddress(MsisdnCheck.cleanup(number)));
                    } catch (Exception e) {
                        // Ignore invalid number
                        LOG.debug("Ignoring invalid number: {}", number, e);
                    }
                }
            } catch (Exception e) {
                LOG.warn("Could not check for valid MSISDN numbers.", e);
            }
        }
    }

}
