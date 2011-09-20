package com.openexchange.halo.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.ContactInterfaceProvider;
import com.openexchange.groupware.contact.ContactSearchMultiplexer;
import com.openexchange.groupware.contact.helpers.ContactMerger;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

public class ContactHaloImpl implements ContactHalo {
	
	private UserService userService;
	private ContactInterfaceDiscoveryService contactDiscoveryService;
	private ContactSearchMultiplexer contactSearchMultiplexer;
	
	
	private Map<String, HaloContactDataSource> contactDataSources = new ConcurrentHashMap<String, HaloContactDataSource>();
	
	public ContactHaloImpl(UserService userService, ContactInterfaceDiscoveryService contactDiscoveryService, SessionSpecificContainerRetrievalService sessionScope) {
		this.userService = userService;
		this.contactDiscoveryService = contactDiscoveryService;
		this.contactSearchMultiplexer = new ContactSearchMultiplexer(contactDiscoveryService);
	}
	
	/* (non-Javadoc)
	 * @see com.openexchange.halo.internal.ContactHalo#investigate(java.lang.String, com.openexchange.groupware.container.Contact, com.openexchange.tools.session.ServerSession)
	 */
	@Override
	public AJAXRequestResult investigate(String provider, Contact contact, ServerSession session) throws OXException {
		HaloContactDataSource dataSource = contactDataSources.get(provider);
		if (dataSource == null) {
			throw new OXException(1).setPrefix("HALO").setLogMessage("Unknown halo provider '"+provider+"'");
		}
		return dataSource.investigate(buildQuery(contact, session), session);
	}


	private HaloContactQuery buildQuery(Contact contact, ServerSession session)
			throws OXException {
		HaloContactQuery contactQuery = new HaloContactQuery();
		
		// Try to find a user with a given eMail address
		
		User user = null;
		try {
			user = userService.searchUser(contact.getEmail1(), session.getContext());
			contactQuery.setUser(user);
		} catch (OXException x) {
			// Don't care. This is all best effort anyway.
		}
		
		List<Contact> contactsToMerge = new ArrayList<Contact>();
		if (user != null) {
			// Load the associated contact
			ContactInterfaceProvider contactInterfaceProvider = contactDiscoveryService.getContactInterfaceProvider(6, session.getContextId());
			ContactInterface contactInterface = contactInterfaceProvider.newContactInterface(session);
			contactsToMerge.add(contactInterface.getObjectById(user.getContactId(), 6));
			
		} else {
			// Try to find a contact
			ContactSearchObject cso = new ContactSearchObject();
			cso.setEmail1(contact.getEmail1());
			cso.setEmail2(contact.getEmail1());
			cso.setEmail3(contact.getEmail1());
			cso.setOrSearch(true);
			
			SearchIterator<Contact> result = contactSearchMultiplexer.extendedSearch(session, cso, -1, null, null, Contact.ALL_COLUMNS);
			while(result.hasNext()) {
				contactsToMerge.add(result.next());
			}
		}
		contactQuery.setMergedContacts(contactsToMerge);
		
		ContactMerger contactMerger = new ContactMerger(false);
		for (Contact c : contactsToMerge) {
			contact = contactMerger.merge(contact, c);
		}
		contactQuery.setContact(contact);
		return contactQuery;
	}
	
	/* (non-Javadoc)
	 * @see com.openexchange.halo.internal.ContactHalo#getProviders(com.openexchange.tools.session.ServerSession)
	 */
	@Override
	public List<String> getProviders(ServerSession session) {
		return new ArrayList<String>(contactDataSources.keySet());
	}


	
	public void addContactDataSource(HaloContactDataSource ds) {
		contactDataSources.put(ds.getId(), ds);
	}
	
	public void removeContactDataSource(HaloContactDataSource ds) {
		contactDataSources.remove(ds.getId());
	}
	
}
