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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.contact.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.json.ContactRequest;
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
public class ListAction extends ContactAction {

    /**
     * Initializes a new {@link ListAction}.
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
        final Map<String, List<Contact>> contactMap = new HashMap<String, List<Contact>>(1);
        final List<Contact> contacts = new ArrayList<Contact>();
        try {
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
            
            contactMap.put("contacts", contacts);
        } finally {
            if (it != null) {
                it.close();
            }
        }
        
        return new AJAXRequestResult(contactMap, timestamp, "contacts");
    }
}
