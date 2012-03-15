package com.openexchange.halo;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.session.ServerSession;

public abstract class AbstractContactHalo implements HaloContactDataSource{

	public AbstractContactHalo() {
		super();
	}

	protected boolean isUserThemselves(User user, List<String> addresses) {
		List<String> ownAddresses = new LinkedList<String>();
		ownAddresses.addAll( Arrays.asList(user.getAliases()));
		ownAddresses.add(user.getMail());
		for(String requested: addresses){
			if(!ownAddresses.contains(requested)){
				return false;
			}
		}
		return true;
	}

	protected List<String> getEMailAddresses(Contact contact) {
		List<String> addresses = new LinkedList<String>();
		if (contact.containsEmail1()) {
			addresses.add(contact.getEmail1());
		}
		if (contact.containsEmail2()) {
			addresses.add(contact.getEmail2());
		}
		if (contact.containsEmail3()) {
			addresses.add(contact.getEmail3());
		}
		return addresses;
	}

	@Override
	public boolean isAvailable(ServerSession session) throws OXException {
		return true;
	}

}