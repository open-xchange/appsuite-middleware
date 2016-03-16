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

package com.openexchange.contacts.json.actions;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ContactAction implements AJAXActionService {

    /** Named logger instance */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactAction.class);

    private final ServiceLookup serviceLookup;

    public static final int[] COLUMNS_ALIAS_ALL = new int[] { 20, 1, 5, 2, 602 };

    public static final int[] COLUMNS_ALIAS_LIST = new int[] {
        20, 1, 5, 2, 500, 501, 502, 505, 523, 525, 526, 527, 542, 555, 102, 602, 592, 101, 551, 552, 543, 547, 548, 549, 556, 569 };

    /**
     * Contact fields that are not persistent.
     */
    public static final EnumSet<ContactField> VIRTUAL_FIELDS =
        EnumSet.of(ContactField.IMAGE1_URL, ContactField.LAST_MODIFIED_UTC, ContactField.SORT_NAME);

    /**
     * Initializes a new {@link ContactAction}.
     *
     * @param serviceLookup The service lookup to use
     */
    public ContactAction(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
		return perform(new ContactRequest(requestData, session));
    }

    /**
     * Performs the request.
     *
     * @param request The request
     * @return The AJAX result
     * @throws OXException
     */
    protected abstract AJAXRequestResult perform(ContactRequest request) throws OXException;

    /**
     * Gets the contact service.
     *
     * @return the contact service
     * @throws OXException
     */
    protected ContactService getContactService() throws OXException {
        try {
            return serviceLookup.getService(ContactService.class);
        } catch (IllegalStateException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ContactService.class.getName());
        }
    }

    /**
     * Gets the vCard service.
     *
     * @return The vCard service
     */
    protected VCardService getVCardService() throws OXException {
        try {
            return serviceLookup.getService(VCardService.class);
        } catch (IllegalStateException e) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(VCardService.class.getName());
        }
    }

    /**
     * Optionally gets the vCard storage service.
     *
     * @return The vCard storage service, or <code>null</code> if not available
     */
    protected VCardStorageService optVCardStorageService(int contextId) {
        VCardStorageFactory vCardStorageFactory = serviceLookup.getOptionalService(VCardStorageFactory.class);
        if (vCardStorageFactory != null) {
            return vCardStorageFactory.getVCardStorageService(serviceLookup.getService(ConfigViewFactory.class), contextId);
        }
        return null;
    }

    /**
     * Gets the latest modification date of the contact compared to another date.
     *
     * @param lastModified the date to compare
     * @param contact the contact
     * @return
     */
    protected static Date getLatestModified(final Date lastModified, final Contact contact) {
    	final Date contactLastModified = contact.getLastModified();
    	return null == contactLastModified || lastModified.after(contactLastModified) ? lastModified : contactLastModified;
    }

    /**
     * Closes a search iterator silently
     *
     * @param searchIterator The search iterator to close
     * @throws OXException
     */
    protected static <T> void close(SearchIterator<T> searchIterator) {
        SearchIterators.close(searchIterator);
    }

    /**
     * Adds all contacts available from a search iterator into a collection.
     *
     * @param contacts The collection to add the contacts to
     * @param searchIterator The search iterator to get the contacts from
     * @param excludedUserID A user ID to exclude from the results, or a value <code>&lt; 0</code> to ignore
     * @return The latest last-modified timestamp of all added contacts
     * @throws OXException
     */
    protected static Date addContacts(Collection<Contact> contacts, SearchIterator<Contact> searchIterator, int excludedUserID) throws OXException {
        Date lastModified = new Date(0);
        if (null != searchIterator) {
            try {
                while (searchIterator.hasNext()) {
                    Contact contact = searchIterator.next();
                    if (0 < excludedUserID && excludedUserID == contact.getInternalUserId()) {
                        continue; // skip
                    }
                    lastModified = getLatestModified(lastModified, contact);
                    contacts.add(contact);
                }
            } finally {
                close(searchIterator);
            }
        }
        return lastModified;
    }

}
