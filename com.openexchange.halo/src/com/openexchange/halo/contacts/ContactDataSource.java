package com.openexchange.halo.contacts;

import java.util.ArrayList;
import java.util.List;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.halo.HaloData;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.session.ServerSession;

public class ContactDataSource implements HaloContactDataSource {

	@Override
	public HaloData investigate(HaloContactQuery query, ServerSession session)
			throws OXException {
		List<Contact> allContacts = new ArrayList<Contact>();
		
		allContacts.add(query.getContact());
		List<Contact> mergedContacts = query.getMergedContacts();
		allContacts.addAll(mergedContacts);
		
		return new HaloData("contact", new SearchIteratorAdapter<Contact>(allContacts.iterator(), allContacts.size()), "contact");
	}

}
