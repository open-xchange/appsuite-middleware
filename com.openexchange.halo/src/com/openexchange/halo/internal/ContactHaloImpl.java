package com.openexchange.halo.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
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

	private final UserService userService;
	private final ContactInterfaceDiscoveryService contactDiscoveryService;
	private final ContactSearchMultiplexer contactSearchMultiplexer;

	private final ConfigViewFactory configViews;

	private final Map<String, HaloContactDataSource> contactDataSources = new ConcurrentHashMap<String, HaloContactDataSource>();

	public ContactHaloImpl(UserService userService,
			ContactInterfaceDiscoveryService contactDiscoveryService,
			SessionSpecificContainerRetrievalService sessionScope,
			ConfigViewFactory configViews) {
		this.userService = userService;
		this.contactDiscoveryService = contactDiscoveryService;
		this.contactSearchMultiplexer = new ContactSearchMultiplexer(
				contactDiscoveryService);
		this.configViews = configViews;
	}

	@Override
	public AJAXRequestResult investigate(String provider, Contact contact,
			AJAXRequestData req, ServerSession session) throws OXException {
		HaloContactDataSource dataSource = contactDataSources.get(provider);
		if (dataSource == null) {
			throw new OXException(1).setPrefix("HALO").setLogMessage(
					"Unknown halo provider '" + provider + "'");
		}
		return dataSource.investigate(buildQuery(contact, session), req,
				session);
	}

	private HaloContactQuery buildQuery(Contact contact, ServerSession session)
			throws OXException {
		HaloContactQuery contactQuery = new HaloContactQuery();

		// Try to find a user with a given eMail address

		User user = null;
		if (contact.getInternalUserId() > 0) {
			user = userService.getUser(contact.getInternalUserId(), session.getContext());
		}
		if (user == null) {
			try {
				user = userService.searchUser(contact.getEmail1(),
						session.getContext());
			} catch (OXException x) {
				// Don't care. This is all best effort anyway.
			}
		}

		if (user == null) {
			try {
				user = userService.searchUser(contact.getEmail2(),
						session.getContext());
			} catch (OXException x) {
				// Don't care. This is all best effort anyway.
			}
		}

		if (user == null) {
			try {
				user = userService.searchUser(contact.getEmail3(),
						session.getContext());
			} catch (OXException x) {
				// Don't care. This is all best effort anyway.
			}
		}


		contactQuery.setUser(user);
		List<Contact> contactsToMerge = new ArrayList<Contact>();
		if (user != null) {
			// Load the associated contact
			ContactInterfaceProvider contactInterfaceProvider = contactDiscoveryService
					.getContactInterfaceProvider(6, session.getContextId());
			ContactInterface contactInterface = contactInterfaceProvider
					.newContactInterface(session);
			contactsToMerge.add(contactInterface.getObjectById(
					user.getContactId(), 6));

		} else {
			// Try to find a contact
			ContactSearchObject cso = new ContactSearchObject();
			cso.setEmail1(contact.getEmail1());
			cso.setEmail2(contact.getEmail1());
			cso.setEmail3(contact.getEmail1());
			cso.setOrSearch(true);

			SearchIterator<Contact> result = contactSearchMultiplexer
					.extendedSearch(session, cso, -1, null, null,
							Contact.ALL_COLUMNS);
			while (result.hasNext()) {
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
	public List<String> getProviders(ServerSession session) throws OXException {
		ConfigView view = configViews.getView(session.getUserId(),
				session.getContextId());
		List<String> providers = new ArrayList<String>();
		for (String provider : contactDataSources.keySet()) {
			ComposedConfigProperty<Boolean> property = view.property(provider,
					boolean.class);
			if (property.isDefined() && property.get()) {
				providers.add(provider);
			}
		}
		return providers;
	}

	public void addContactDataSource(HaloContactDataSource ds) {
		contactDataSources.put(ds.getId(), ds);
	}

	public void removeContactDataSource(HaloContactDataSource ds) {
		contactDataSources.remove(ds.getId());
	}

}
