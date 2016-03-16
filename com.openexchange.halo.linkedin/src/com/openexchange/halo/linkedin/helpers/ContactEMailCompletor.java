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

package com.openexchange.halo.linkedin.helpers;

import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

public class ContactEMailCompletor {

	private final ServerSession session;
	private final ContactService contactService;
	private final UserService userService;

	public ContactEMailCompletor(ServerSession session, ContactService contactService, UserService userService) {
		this.session = session;
		this.contactService = contactService;
		this.userService = userService;
	}

	protected void completeFromContactData(Contact contact) throws OXException {
		final ContactField[] mailFields = new ContactField[] { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 };
		try {
			final Contact fullContact = contactService.getContact(session, Integer.toString(contact.getParentFolderID()),
					Integer.toString(contact.getObjectID()), mailFields);
			if(fullContact.containsEmail1()) {
	            contact.setEmail1(fullContact.getEmail1());
	        }
			if(fullContact.containsEmail2()) {
	            contact.setEmail2(fullContact.getEmail2());
	        }
			if(fullContact.containsEmail3()) {
	            contact.setEmail3(fullContact.getEmail3());
	        }
		} catch (OXException x) {
			// IGNORE, we're trying to be robust here.
		}
	}

	protected void completeFromUserData(Contact contact) throws OXException {
		User user = userService.getUser(contact.getInternalUserId(), session.getContext());
		contact.setEmail1(user.getMail());
	}

	public void complete(Contact contact) throws OXException {
		if(contact.containsInternalUserId()){
			completeFromUserData(contact);
			return;
		}
		if(contact.containsParentFolderID() && contact.containsObjectID()){
			completeFromContactData(contact);
			return;
		}
	}

}
