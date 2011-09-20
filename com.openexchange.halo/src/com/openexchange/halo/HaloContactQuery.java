package com.openexchange.halo;

import java.util.List;

import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;

public class HaloContactQuery {
	private Contact contact;
	private User user;
	private List<Contact> merged;
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	
	public Contact getContact() {
		return contact;
	}

	public void setMergedContacts(List<Contact> contactsToMerge) {
		this.merged = contactsToMerge;
	}

	public List<Contact> getMergedContacts() {
		return merged;
	}
}
