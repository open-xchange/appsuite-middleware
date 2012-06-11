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
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactMerger;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

public class ContactHaloImpl implements ContactHalo {

	private final UserService userService;
	private final ContactService contactService;
	private final ConfigViewFactory configViews;

	private final Map<String, HaloContactDataSource> contactDataSources = new ConcurrentHashMap<String, HaloContactDataSource>();

	public ContactHaloImpl(final UserService userService,
			final ContactService contactService,
			final SessionSpecificContainerRetrievalService sessionScope,
			final ConfigViewFactory configViews) {
		this.userService = userService;
		this.contactService = contactService;
		this.configViews = configViews;
	}

	@Override
	public AJAXRequestResult investigate(final String provider, final Contact contact,
			final AJAXRequestData req, final ServerSession session) throws OXException {
		final HaloContactDataSource dataSource = contactDataSources.get(provider);
		if (dataSource == null) {
			throw new OXException(1).setPrefix("HALO").setLogMessage(
					"Unknown halo provider '" + provider + "'");
		}
		if(! (contact.getInternalUserId() > 0) && ! contact.containsEmail1() & ! contact.containsEmail2() & ! contact.containsEmail3()){
			throw new OXException(2).setPrefix("HALO").setLogMessage("Cannot search a contact that is neither an internal user nor has an e-mail address!");
		}
		return dataSource.investigate(buildQuery(contact, session), req,
				session);
	}

	private HaloContactQuery buildQuery(Contact contact, final ServerSession session)
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
			} catch (final OXException x) {
				// Don't care. This is all best effort anyway.
			}
		}

		if (user == null) {
			try {
				user = userService.searchUser(contact.getEmail2(),
						session.getContext());
			} catch (final OXException x) {
				// Don't care. This is all best effort anyway.
			}
		}

		if (user == null) {
			try {
				user = userService.searchUser(contact.getEmail3(),
						session.getContext());
			} catch (final OXException x) {
				// Don't care. This is all best effort anyway.
			}
		}


		contactQuery.setUser(user);
		final List<Contact> contactsToMerge = new ArrayList<Contact>();
		if (user != null) {
			// Load the associated contact
			contact = contactService.getUser(session, user.getId());
			contactsToMerge.add(contact);
		} else {
			// Try to find a contact
			final ContactField[] searchFields = { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 };
			final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        	for (final ContactField field : searchFields) {        		
        		final SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
        		term.addOperand(new ContactFieldOperand(field));
        		term.addOperand(new ConstantOperand<String>(contact.getEmail1()));
        		orTerm.addSearchTerm(term);
        	}
        	SearchIterator<Contact> iterator = null;
        	try {
            	iterator = contactService.searchContacts(session, orTerm);
    			while (iterator.hasNext()) {
    				contactsToMerge.add(iterator.next());
    			}
        	} finally {
        		if (null != iterator) {
        			iterator.close();
        		}
        	}
		}
		contactQuery.setMergedContacts(contactsToMerge);

		final ContactMerger contactMerger = new ContactMerger(false);
		for (final Contact c : contactsToMerge) {
			contact = contactMerger.merge(contact, c);
		}
		contactQuery.setContact(contact);
		return contactQuery;
	}

	@Override
	public List<String> getProviders(final ServerSession session) throws OXException {
		final ConfigView view = configViews.getView(session.getUserId(),
				session.getContextId());
		final List<String> providers = new ArrayList<String>();
		for (final String provider : contactDataSources.keySet()) {
			final ComposedConfigProperty<Boolean> property = view.property(provider,
					boolean.class);
			if (!property.isDefined() || property.get().booleanValue()) {
				providers.add(provider);
			}
		}
		return providers;
	}

	public void addContactDataSource(final HaloContactDataSource ds) {
		contactDataSources.put(ds.getId(), ds);
	}

	public void removeContactDataSource(final HaloContactDataSource ds) {
		contactDataSources.remove(ds.getId());
	}

}
