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

package com.openexchange.contacts.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdatesAction extends ContactAction {

    /**
     * Initializes a new {@link UpdatesAction}.
     * @param serviceLookup
     */
    public UpdatesAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest req) throws OXException {
        final ServerSession session = req.getSession();
        final int folder = req.getFolder();
        final int[] columns = req.getColumns();
        final long timestampLong = req.getTimestamp();
        Date timestamp = new Date(timestampLong);
        Date lastModified = null;
        final TimeZone timeZone = req.getTimeZone();

        String ignore = req.getIgnore();
        if (ignore == null) {
            ignore = "deleted";
        }

        boolean bIgnoreDelete = false;
        if (ignore.indexOf("deleted") != -1) {
            bIgnoreDelete = true;
        }

        final ContactInterface contactInterface = getContactInterfaceDiscoveryService().newContactInterface(folder, session);
        final List<Contact> modifiedList = new ArrayList<Contact>();
        final List<Contact> deletedList = new ArrayList<Contact>();
        final Map<String, List<Contact>> responseMap = new HashMap<String, List<Contact>>(2);
        SearchIterator<Contact> it = null;
        try {
            it = contactInterface.getModifiedContactsInFolder(folder, columns, timestamp);
            while (it.hasNext()) {
                final Contact contact = it.next();
                lastModified = contact.getLastModified();

                // Correct last modified and creation date with users timezone
                contact.setLastModified(getCorrectedTime(contact.getLastModified(), timeZone));
                contact.setCreationDate(getCorrectedTime(contact.getCreationDate(), timeZone));
                modifiedList.add(contact);

                if ((lastModified != null) && (timestamp.getTime() < lastModified.getTime())) {
                    timestamp = lastModified;
                }
            }

            if (!bIgnoreDelete) {
                it = contactInterface.getDeletedContactsInFolder(folder, columns, timestamp);
                while (it.hasNext()) {
                    final Contact contact = it.next();
                    lastModified = contact.getLastModified();

                    // Correct last modified and creation date with users timezone
                    contact.setLastModified(getCorrectedTime(contact.getLastModified(), timeZone));
                    contact.setCreationDate(getCorrectedTime(contact.getCreationDate(), timeZone));
                    deletedList.add(contact);

                    if ((lastModified != null) && (timestamp.getTime() < lastModified.getTime())) {
                        timestamp = lastModified;
                    }
                }
            }
            responseMap.put("modified", modifiedList);
            responseMap.put("deleted", deletedList);

        } finally {
            if (it != null) {
                it.close();
            }
        }

        return new AJAXRequestResult(responseMap, timestamp, "contact");
    }
}
