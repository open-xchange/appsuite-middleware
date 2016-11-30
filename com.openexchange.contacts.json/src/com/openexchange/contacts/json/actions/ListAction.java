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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@OAuthAction(ContactActionFactory.OAUTH_READ_SCOPE)
public class ListAction extends ContactAction {

    /**
     * Initializes a new {@link ListAction}.
     *
     * @param serviceLookup
     */
    public ListAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest request) throws OXException {
        /*
         * get requested object and folder IDs
         */
        int[][] objectIdsAndFolderIds = request.getListRequestData();
        Map<String, List<String>> ids = new HashMap<String, List<String>>();
        for (int[] objectIdAndFolderId : objectIdsAndFolderIds) {
            String folderID = Integer.toString(objectIdAndFolderId[1]);
            if (false == ids.containsKey(folderID)) {
            	ids.put(folderID, new LinkedList<String>());
            }
            ids.get(folderID).add(Integer.toString(objectIdAndFolderId[0]));
        }
        /*
         * get contacts
         */
        List<Contact> contacts = new LinkedList<Contact>();
        Date lastModified = new Date(0);
        ContactField[] fields = request.getFields();
        for (final Entry<String, List<String>> entry : ids.entrySet()) {
            SearchIterator<Contact> searchIterator = null;
            try {
                searchIterator = getContactService().getContacts(request.getSession(), entry.getKey(),
                		entry.getValue().toArray(new String[entry.getValue().size()]), fields);
                int parentFolderID = Integer.parseInt(entry.getKey());
                while (searchIterator.hasNext()) {
                    Contact contact = searchIterator.next();
                    contact.setParentFolderID(parentFolderID);
                    lastModified = getLatestModified(lastModified, contact);
                    contacts.add(contact);
                }
            } finally {
            	if (null != searchIterator) {
            		searchIterator.close();
            	}
            }
        }
        if (null != contacts && 1 < contacts.size()) {
            /*
             * sort loaded contacts in the order they were requested
             */
            List<Contact> sortedContacts = new ArrayList<Contact>(contacts.size());
            for (int i = 0; i < objectIdsAndFolderIds.length; i++) {
                int[] objectIdsAndFolderId = objectIdsAndFolderIds[i];
                for (Contact contact : contacts) {
                    if (contact.getObjectID() == objectIdsAndFolderId[0] && contact.getParentFolderID() == objectIdsAndFolderId[1]) {
                        sortedContacts.add(contact);
                        break;
                    }
                }
            }
            return new AJAXRequestResult(sortedContacts, lastModified, "contact");
        } else {
        	return new AJAXRequestResult(contacts, lastModified, "contact");
        }
    }
}
