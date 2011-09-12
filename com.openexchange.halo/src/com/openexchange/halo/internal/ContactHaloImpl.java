package com.openexchange.halo.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.contact.ContactInterfaceProvider;
import com.openexchange.groupware.contact.ContactSearchMultiplexer;
import com.openexchange.groupware.contact.helpers.ContactMerger;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.halo.AsynchronousHaloContactDataSource;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.halo.HaloData;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.RandomTokenContainer;
import com.openexchange.session.SessionScopedContainer;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.session.SessionSpecificContainerRetrievalService.Lifecycle;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

public class ContactHaloImpl implements ContactHalo {
	
	private UserService userService;
	private ContactInterfaceDiscoveryService contactDiscoveryService;
	private ContactSearchMultiplexer contactSearchMultiplexer;
	
	private RandomTokenContainer<Future<HaloData>> tokens;
	
	private List<HaloContactDataSource> contactDataSources = Collections.synchronizedList(new LinkedList<HaloContactDataSource>());
	private List<AsynchronousHaloContactDataSource> asyncContactDataSources = Collections.synchronizedList(new LinkedList<AsynchronousHaloContactDataSource>());
	
	public ContactHaloImpl(UserService userService, ContactInterfaceDiscoveryService contactDiscoveryService, SessionSpecificContainerRetrievalService sessionScope) {
		this.userService = userService;
		this.contactDiscoveryService = contactDiscoveryService;
		this.contactSearchMultiplexer = new ContactSearchMultiplexer(contactDiscoveryService);
		this.tokens = sessionScope.getRandomTokenContainer("com.openexchange.halo.futures", Lifecycle.HIBERNATE, null);
	}
	
	@Override
	public List<Object> investigate(Contact contact, ServerSession session) throws OXException {
		List<Object> retval = new ArrayList<Object>();
		
		HaloContactQuery contactQuery = buildQuery(contact, session);
		
		for(HaloContactDataSource ds : contactDataSources) {
			retval.add(ds.investigate(contactQuery, session));
		}
		
		for(AsynchronousHaloContactDataSource ds : asyncContactDataSources) {
			Future<HaloData> future = ds.investigate(contactQuery, session);
			String token = tokens.rememberForSession(session, future);
			retval.add(token);
		}
		
		return retval;
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


	@Override
	public HaloData resolveToken(String token, ServerSession session)
			throws OXException {

		Future<HaloData> future = tokens.remove(token);
		if (future == null) {
			return new HaloData("null", null, null);
		}
		
		try {
			return future.get();
		} catch (InterruptedException e) {
			return new HaloData("null", null, null);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof OXException) {
				throw (OXException) cause;
			}
			return new HaloData("null", null, null);
		}
	}
	
	public void addContactDataSource(HaloContactDataSource ds) {
		contactDataSources.add(ds);
	}
	
	public void removeContactDataSource(HaloContactDataSource ds) {
		contactDataSources.remove(ds);
	}
	
	public void addAsyncContactDataSource(AsynchronousHaloContactDataSource ds) {
		asyncContactDataSources.add(ds);
	}
	
	public void removeAsyncContactDataSource(AsynchronousHaloContactDataSource ds) {
		asyncContactDataSources.remove(ds);
	}
}
