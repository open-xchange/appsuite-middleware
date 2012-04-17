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

package com.openexchange.contacts.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ListAction}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@Action(method = RequestMethod.PUT, name = "list", description = "Get a list of contacts.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for contacts are defined in Common object data and Detailed contact data. The alias \"list\" uses the predefined columnset [20, 1, 5, 2, 500, 501, 502, 505, 523, 525, 526, 527, 542, 555, 102, 602, 592, 101, 551, 552, 543, 547, 548, 549, 556, 569].") }, requestBody = "An array with id.", responseDescription = "Response with timestamp: An array with contact data. Each array element describes one contact and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter. The alias \"list\" uses the predefined columnset [600, 601, 614, 602, 611, 603, 612, 607, 652, 610, 608, 102].")
public class ListAction extends ContactAction {

    /**
     * Initializes a new {@link ListAction}.
     * 
     * @param serviceLookup
     */
    public ListAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest req) throws OXException {
        final ServerSession session = req.getSession();
        final int[] columns = req.getColumns();
        final int[][] objectIdsAndFolderIds = req.getListRequestData();
        final TimeZone timeZone = req.getTimeZone();

        boolean allInSameFolder = true;
        int lastFolder = -1;
        for (final int[] objectIdAndFolderId : objectIdsAndFolderIds) {
            final int folder = objectIdAndFolderId[1];
            if (lastFolder != -1 && folder != lastFolder) {
                allInSameFolder = false;
            }
            lastFolder = folder;
        }

        Date timestamp = new Date(0);
        Date lastModified = null;
        SearchIterator<Contact> it = null;

        final List<Contact> sortedContacts;
        try {
            final List<Contact> contacts = new ArrayList<Contact>();
            if (allInSameFolder) {
                final ContactInterface contactInterface = getContactInterfaceDiscoveryService().newContactInterface(lastFolder, session);
                it = contactInterface.getObjectsById(objectIdsAndFolderIds, columns);

                while (it.hasNext()) {
                    final Contact contact = it.next();
                    contacts.add(contact);

                    lastModified = contact.getLastModified();
                    // Correct last modified and creation date with users timezone
                    contact.setLastModified(getCorrectedTime(contact.getLastModified(), timeZone));
                    contact.setCreationDate(getCorrectedTime(contact.getCreationDate(), timeZone));

                    if (lastModified != null && timestamp.before(lastModified)) {
                        timestamp = lastModified;
                    }
                }
            } else {
                for (final int[] objectIdAndFolderId : objectIdsAndFolderIds) {
                    final int folder = objectIdAndFolderId[1];
                    final ContactInterface contactInterface = getContactInterfaceDiscoveryService().newContactInterface(folder, session);
                    it = contactInterface.getObjectsById(new int[][] { objectIdAndFolderId }, columns);

                    while (it.hasNext()) {
                        final Contact contact = it.next();
                        lastModified = contact.getLastModified();

                        // Correct last modified and creation date with users timezone
                        contact.setLastModified(getCorrectedTime(contact.getLastModified(), timeZone));
                        contact.setCreationDate(getCorrectedTime(contact.getCreationDate(), timeZone));
                        contacts.add(contact);
                    }

                    if ((lastModified != null) && (timestamp.getTime() < lastModified.getTime())) {
                        timestamp = lastModified;
                    }
                }
            }

            // Sort loaded contacts in the order they were requested
            sortedContacts = new ArrayList<Contact>(contacts.size());
            for (int i = 0; i < objectIdsAndFolderIds.length; i++) {
                final int[] objectIdsAndFolderId = objectIdsAndFolderIds[i];
                final int objectId = objectIdsAndFolderId[0];
                final int folderId = objectIdsAndFolderId[1];

                for (final Contact contact : contacts) {
                    if (contact.getObjectID() == objectId && contact.getParentFolderID() == folderId) {
                        sortedContacts.add(contact);
                        break;
                    }
                }
            }
        } finally {
            if (it != null) {
                it.close();
            }
        }

        return new AJAXRequestResult(sortedContacts, timestamp, "contact");
    }
    
    @Override
    protected AJAXRequestResult perform2(final ContactRequest request) throws OXException {
        /*
         * get requested object and folder IDs
         */
        final int[][] objectIdsAndFolderIds = request.getListRequestData();
        final Map<String, List<String>> ids = new HashMap<String, List<String>>();
        for (final int[] objectIdAndFolderId : objectIdsAndFolderIds) {
            final String folderID = Integer.toString(objectIdAndFolderId[1]);
            if (false == ids.containsKey(folderID)) {
            	ids.put(folderID, new ArrayList<String>());
            }
            ids.get(folderID).add(Integer.toString(objectIdAndFolderId[0]));
        }
        /*
         * get contacts
         */
        final List<Contact> contacts = new ArrayList<Contact>();
        Date lastModified = new Date(0);
        for (final Entry<String, List<String>> entry : ids.entrySet()) {
            SearchIterator<Contact> searchIterator = null;
            try {
                searchIterator = getContactService().getContacts(request.getSession(), entry.getKey(), 
                		entry.getValue().toArray(new String[entry.getValue().size()]), request.getFields());
                final int parentFolderID = Integer.parseInt(entry.getKey());
                while (searchIterator.hasNext()) {
                    final Contact contact = searchIterator.next();
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
            final List<Contact> sortedContacts = new ArrayList<Contact>(contacts.size());
            for (int i = 0; i < objectIdsAndFolderIds.length; i++) {
                final int[] objectIdsAndFolderId = objectIdsAndFolderIds[i];
                for (final Contact contact : contacts) {
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
