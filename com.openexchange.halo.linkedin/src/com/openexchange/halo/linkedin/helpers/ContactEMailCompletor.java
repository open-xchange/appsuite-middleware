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
