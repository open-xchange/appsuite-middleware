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

package com.openexchange.subscribe.parser;

import java.util.Collection;
import java.util.Date;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;
import com.openexchange.subscribe.osgi.SubscriptionServiceRegistry;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;


/**
 * {@link ContactHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactHandler.class);

    /**
     * Update or insert contacts from a subscription
     * @param subscription
     * @throws OXException
     * @throws OXException
     */
    protected void storeContacts(final Session session, final int folderId, final Collection<Contact> updatedContacts) throws OXException{

        ContactService contactService = SubscriptionServiceRegistry.getInstance().getService(ContactService.class);

        for (final Contact updatedContact : updatedContacts) {
            final SearchIterator<Contact> existingContacts = contactService.getAllContacts(session, String.valueOf(folderId));
            boolean foundMatch = false;
            while (!foundMatch && existingContacts.hasNext()) {
                Contact existingContact = null;
                try {
                    existingContact = existingContacts.next();
                } catch (final SearchIteratorException e) {
                    LOG.error("", e);
                }
                if (existingContact == null) {
                    continue;
                }
                if (isSame(existingContact, updatedContact)) {
                    foundMatch = true;
                    updatedContact.setObjectID(existingContact.getObjectID());
                    contactService.updateContact(
                        session,
                        String.valueOf(folderId),
                        String.valueOf(existingContact.getObjectID()),
                        updatedContact,
                        new Date());
                }
            }
            if (foundMatch) {
                continue;
            }
            updatedContact.setParentFolderID(folderId);
            try {
                contactService.createContact(session, String.valueOf(folderId), updatedContact);
            } catch (final OXException x) {
                LOG.error("", x);
            }
        }
    }


    protected boolean isSame(final Contact first, final Contact second){
        if(first.containsGivenName()) {
            if(!first.getGivenName().equals(second.getGivenName())) {
                return false;
            }
        }

        if(first.containsSurName()) {
            if(!first.getSurName().equals(second.getSurName())) {
                return false;
            }
        }

        if(first.containsDisplayName()) {
            if(!first.getDisplayName().equals(second.getDisplayName())) {
                return false;
            }
        }
        return true;
    }

}
