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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.utils;

import java.util.ArrayList;
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
                    ccs.memorizeAddresses(new ArrayList<InternetAddress>(addrs), false, session);
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
