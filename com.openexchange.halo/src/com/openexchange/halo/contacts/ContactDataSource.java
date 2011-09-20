package com.openexchange.halo.contacts;

import java.util.ArrayList;
import java.util.List;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.tools.session.ServerSession;

public class ContactDataSource implements HaloContactDataSource {

	@Override
	public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req, ServerSession session)
			throws OXException {
		List<Contact> allContacts = new ArrayList<Contact>();
		
		allContacts.add(query.getContact());
		List<Contact> mergedContacts = query.getMergedContacts();
		allContacts.addAll(mergedContacts);
		
		return new AJAXRequestResult(allContacts, "contact");
	}

	@Override
	public String getId() {
		return "contact";
	}

}
