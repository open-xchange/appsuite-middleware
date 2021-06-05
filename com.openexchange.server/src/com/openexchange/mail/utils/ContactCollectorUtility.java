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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import com.openexchange.contactcollector.ContactCollectorService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactCollectorUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class ContactCollectorUtility {


    /**
     * Initializes a new {@link ContactCollectorUtility}.
     */
    private ContactCollectorUtility() {
        super();
    }

    /** The prefix for puny-code encoded mail addresses */
    private final static String ACE_PREFIX = "xn--";

    /**
     * Triggers the contact collector for specified mail's addresses.
     *
     * @param session The session
     * @param mails The mails
     * @param memorizeAddresses Whether contact-collector is supposed to be triggered
     * @param incrementUseCount Whether the associated contacts' use-count is supposed to be incremented
     * @throws OXException
     */
    public static void triggerContactCollector(ServerSession session, Collection<? extends MailMessage> mails, boolean memorizeAddresses, boolean incrementUseCount) throws OXException {
        // Sets to store user's alias addresses (which should not be considered) and cumulative addresses of specified mail collection
        Set<InternetAddress> addrs = null;
        Set<InternetAddress> aliases = null;

        // Check whether contact-collector is supposed to be triggered
        if (memorizeAddresses) {
            ContactCollectorService ccs = ServerServiceRegistry.getInstance().getService(ContactCollectorService.class);
            if (null != ccs) {
                for (MailMessage mail : mails) {
                    if (null != mail) {
                        if (null == aliases) {
                            aliases = AddressUtility.getAliases(session);
                        }

                        if (null == addrs) {
                            addrs = AddressUtility.getFilteredAddresses(mail, aliases);
                        } else {
                            addrs.addAll(AddressUtility.getFilteredAddresses(mail, aliases));
                        }
                    }
                }

                if (null != addrs && !addrs.isEmpty()) {
                    ccs.memorizeAddresses(addrs, false, session);
                }
            }
        }

        // Check whether to increment use-count
        if (incrementUseCount) {
            ObjectUseCountService useCountService = ServerServiceRegistry.getInstance().getService(ObjectUseCountService.class);
            if (null != useCountService) {
                if (null == addrs) {
                    for (MailMessage mail : mails) {
                        if (null != mail) {
                            if (null == aliases) {
                                aliases = AddressUtility.getAliases(session);
                            }

                            if (null == addrs) {
                                addrs = AddressUtility.getFilteredAddresses(mail, aliases);
                            } else {
                                addrs.addAll(AddressUtility.getFilteredAddresses(mail, aliases));
                            }
                        }
                    }
                }

                if (null != addrs && !addrs.isEmpty()) {
                    Set<String> addressesToIncrementBy = new HashSet<>(addrs.size());
                    for (InternetAddress addr : addrs) {
                        String address = addr.getAddress();
                        addressesToIncrementBy.add(address);
                        if (address.indexOf(ACE_PREFIX) >= 0) {
                            addressesToIncrementBy.add(QuotedInternetAddress.toIDN(address));
                        }
                    }

                    IncrementArguments.Builder builder = new IncrementArguments.Builder(addressesToIncrementBy);
                    useCountService.incrementObjectUseCount(session, builder.build());
                }
            }
        }
    }

}
