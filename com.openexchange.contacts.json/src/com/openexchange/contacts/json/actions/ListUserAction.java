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
import java.util.List;
import java.util.TimeZone;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.RdbContactSQLImpl;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ListUserAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "listuser", description = "Get a list of users.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for contacts are defined in Common object data and Detailed contact data.")
}, requestBody = "An array with id.",
responseDescription = "Response with timestamp: An array with contact data. Each array element describes one contact and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
public class ListUserAction extends ContactAction {

    /**
     * Initializes a new {@link ListUserAction}.
     * @param serviceLookup
     */
    public ListUserAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest req) throws OXException {
        final ServerSession session = req.getSession();
        final int[] uids = req.getUserIds();
        final Context ctx = session.getContext();
        Date timestamp = new Date(0);
        Date lastModified = null;
        final TimeZone timeZone = req.getTimeZone();

        final ContactInterface contactInterface = new RdbContactSQLImpl(session, ctx);
        final List<Contact> contacts = new ArrayList<Contact>();
        for (final int uid : uids) {
            final Contact contact = contactInterface.getUserById(uid);
            lastModified = contact.getLastModified();

            // Correct last modified and creation date with users timezone
            contact.setLastModified(getCorrectedTime(contact.getLastModified(), timeZone));
            contact.setCreationDate(getCorrectedTime(contact.getCreationDate(), timeZone));
            contacts.add(contact);

            if (lastModified != null && timestamp.before(lastModified)) {
                timestamp = lastModified;
            }
        }

        return new AJAXRequestResult(contacts, timestamp, "contact");
    }
    
    @Override
    protected AJAXRequestResult perform2(final ContactRequest request) throws OXException {
        final int[] userIDs = request.getUserIds();
        final List<Contact> contacts = new ArrayList<Contact>();
        Date lastModified = new Date(0);
        final ContactService contactService = getContactService();
        final ContactField[] fields = request.getFields();
        for (final int userID : userIDs) {
            final Contact contact = contactService.getUser(request.getSession(), userID, fields);
            lastModified = getLatestModified(lastModified, contact);
            contacts.add(contact);
        }
        return new AJAXRequestResult(contacts, lastModified, "contact");
    }

}
