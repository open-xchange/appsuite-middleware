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

import java.util.Date;
import java.util.TimeZone;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.RdbContactSQLImpl;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetUserAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.GET, name = "getuser", description = "Get contact by user ID.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "User ID (not Object ID) of the requested user.")
}, responseDescription = "Response with timestamp: An object containing all data of the requested contact. The fields of the object are listed in Common object data and Detailed contact data.")
public class GetUserAction extends ContactAction {

    /**
     * Initializes a new {@link GetUserAction}.
     * @param serviceLookup
     */
    public GetUserAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(final ContactRequest req) throws OXException {
        final ServerSession session = req.getSession();
        final TimeZone timeZone = req.getTimeZone();
        final int uid = req.getId();
        final Context ctx = session.getContext();

        final ContactInterface contactInterface = new RdbContactSQLImpl(session, ctx);
        final Contact contact = contactInterface.getUserById(uid);
        final Date lastModified = contact.getLastModified();

        // Correct last modified and creation date with users timezone
        contact.setLastModified(getCorrectedTime(contact.getLastModified(), timeZone));
        contact.setCreationDate(getCorrectedTime(contact.getCreationDate(), timeZone));

        return new AJAXRequestResult(contact, lastModified, "contact");
    }
    
    @Override
    protected AJAXRequestResult perform2(final ContactRequest request) throws OXException {
    	final int userID = Integer.parseInt(request.getObjectID());    	
        final SearchIterator<Contact> contacts = getContactService().searchContacts(request.getSession(), getSearchTermForUser(userID));
    	final Contact contact;
        if (null != contacts && contacts.hasNext()) {
    		contact = contacts.next();
    	} else {
    		throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(userID, request.getSession().getContextId());
    	}
        final Date lastModified = contact.getLastModified();
        applyTimezoneOffset(contact, request.getTimeZone());
        return new AJAXRequestResult(contact, lastModified, "contact");
    }
    
    private static CompositeSearchTerm getSearchTermForUser(final int userID) {
    	final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
		final SingleSearchTerm folderIDTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		folderIDTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID)); 
		folderIDTerm.addOperand(new ConstantOperand<String>(Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID)));
		andTerm.addSearchTerm(folderIDTerm);
    	final SingleSearchTerm userIDTerm = new SingleSearchTerm(SingleSearchTerm.SingleOperation.EQUALS);
		userIDTerm.addOperand(new ContactFieldOperand(ContactField.INTERNAL_USERID)); 
		userIDTerm.addOperand(new ConstantOperand<Integer>(userID));
    	andTerm.addSearchTerm(userIDTerm);
		return andTerm;
    }

}
