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

package com.openexchange.contact.internal;

import static com.openexchange.contact.internal.Tools.parse;
import java.util.Date;
import java.util.List;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link DefaultContactService}
 *
 * Abstract {@link ContactService} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultContactService implements ContactService {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultContactService.class);

    /**
     * Initializes a new {@link DefaultContactService}.
     */
    public DefaultContactService() {
    	super();
    	LOG.debug("initialized.");
    }

	/*
	 * -----------------------------------------------------------------------------------------------------------------------------------
	 */

	@Override
	public Contact getContact(Session session, String folderId, String id) throws OXException {
		return this.getContact(session, folderId, id, null);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(Session session, String folderId) throws OXException {
		return this.getAllContacts(session, folderId, null, null);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(Session session, String folderId, SortOptions sortOptions) throws OXException {
		return this.getAllContacts(session, folderId, null, sortOptions);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(Session session, String folderId, ContactField[] fields) throws OXException {
		return this.getAllContacts(session, folderId, fields, null);
	}

	@Override
	public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids) throws OXException {
		return this.getContacts(session, folderId, ids, null, null);
	}

	@Override
	public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, SortOptions sortOptions) throws OXException {
		return this.getContacts(session, folderId, ids, null, sortOptions);
	}

	@Override
	public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, ContactField[] fields) throws OXException {
		return this.getContacts(session, folderId, ids, fields, null);
	}

	@Override
	public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since) throws OXException {
		return this.getDeletedContacts(session, folderId, since, null);
	}

	@Override
	public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, final Date since, final ContactField[] fields) throws OXException {
		return this.getDeletedContacts(session, folderId, since, fields, null);
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since) throws OXException {
		return this.getModifiedContacts(session, folderId, since, null);
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, final ContactField[] fields) throws OXException {
		return this.getModifiedContacts(session, folderId, since, fields, SortOptions.EMPTY);
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term) throws OXException {
		return this.searchContacts(session, term, null, null);
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, SortOptions sortOptions) throws OXException {
		return this.searchContacts(session, term, null, sortOptions);
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields) throws OXException {
		return this.searchContacts(session, term, fields, null);
	}

	@Override
	public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch) throws OXException {
		return this.searchContacts(session, contactSearch, null, null);
	}

	@Override
	public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, SortOptions sortOptions) throws OXException {
		return this.searchContacts(session, contactSearch, null, sortOptions);
	}

	@Override
	public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, ContactField[] fields) throws OXException {
		return this.searchContacts(session, contactSearch, fields, null);
	}

	@Override
	public Contact getUser(Session session, int userID) throws OXException {
		return getUser(session, userID, null);
	}

	@Override
	public SearchIterator<Contact> getUsers(Session session, int[] userIDs) throws OXException {
		return getUsers(session, userIDs, null);
	}

    @Override
    public SearchIterator<Contact> searchContactsWithBirthday(Session session, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return searchContactsWithBirthday(session, null, from, until, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> searchContactsWithAnniversary(Session session, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return searchContactsWithAnniversary(session, null, from, until, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> autocompleteContacts(Session session, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return autocompleteContacts(session, null, query, parameters, fields, sortOptions);
    }

	/*
	 * -----------------------------------------------------------------------------------------------------------------------------------
	 */

	@Override
	public <O> SearchIterator<Contact> searchContacts(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(term, "term");
		return this.doSearchContacts(session, term, fields, sortOptions);
	}

	@Override
	public SearchIterator<Contact> searchContacts(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(contactSearch, "contactSearch");
		return this.doSearchContacts(session, Tools.prepareContactSearch(contactSearch), fields, sortOptions);
	}

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderId, "folderId");
        return this.doGetContacts(false, session, folderId, null, fields, sortOptions, null);
    }

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, ContactField[] fields, SortOptions sortOptions) throws OXException {
        Check.argNotNull(session, "session");
        return this.doGetContacts(session, null, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> getAllContacts(Session session, List<String> folderIDs, ContactField[] fields, SortOptions sortOptions) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderIDs, "folderIDs");
        return this.doGetContacts(session, folderIDs, fields, sortOptions);
    }

    @Override
    public int countContacts(Session session, String folderId) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderId, "folderId");
        return this.doCountContacts(session, folderId);
    }

    @Override
	public SearchIterator<Contact> getContacts(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(folderId, "folderId");
		Check.argNotNull(ids, "ids");
		return this.doGetContacts(false, session, folderId, ids, fields, sortOptions, null);
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(Session session, String folderId, Date since, ContactField[] fields, final SortOptions sortOptions) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(folderId, "folderId");
		Check.argNotNull(since, "since");
		return this.doGetContacts(false, session, folderId, null, fields, sortOptions, since);
	}

	@Override
	public SearchIterator<Contact> getDeletedContacts(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(folderId, "folderId");
		Check.argNotNull(since, "since");
		return this.doGetContacts(true, session, folderId, null, fields, sortOptions, since);
	}

	@Override
	public Contact getContact(final Session session, final String folderId, final String id, final ContactField[] fields) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(folderId, "folderId");
		Check.argNotNull(id, "id");
		Contact contact = null;
		SearchIterator<Contact> searchIterator = null;
		try {
			searchIterator = this.doGetContacts(false, session, folderId, new String[] { id }, fields, null, null);
			contact = searchIterator.next();
		} finally {
			if (null != searchIterator) {
				searchIterator.close();
			}
		}
		Check.contactNotNull(contact, session.getContextId(), session.getUserId());
		return contact;
	}

	@Override
	public void createContact(Session session, String folderId, Contact contact) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(folderId, "folderId");
		Check.argNotNull(contact, "contact");
		this.doCreateContact(session, folderId, contact);
	}

    @Override
    public void updateContact(final Session session, final String folderId, final String id, final Contact contact, final Date lastRead)
            throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderId, "folderId");
        Check.argNotNull(id, "id");
        Check.argNotNull(lastRead, "lastRead");
        Check.argNotNull(contact, "contact");
        if (contact.containsParentFolderID() && contact.getParentFolderID() != parse(folderId)) {
            this.doUpdateAndMoveContact(session, folderId, Integer.toString(contact.getParentFolderID()), id, contact, lastRead);
        } else {
            this.doUpdateContact(session, folderId, id, contact, lastRead);
        }
    }

    @Override
    public void updateUser(final Session session, final String folderId, final String id, final Contact contact, final Date lastRead)
            throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderId, "folderId");
        Check.argNotNull(id, "id");
        Check.argNotNull(lastRead, "lastRead");
        Check.argNotNull(contact, "contact");
        if (contact.containsParentFolderID() && contact.getParentFolderID() != parse(folderId)) {
            throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(parse(id), session.getContextId());
        } else {
            this.doUpdateUser(session, folderId, id, contact, lastRead);
        }
    }

    @Override
    public void deleteContact(Session session, String folderId, String id, Date lastRead) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderId, "folderId");
        Check.argNotNull(id, "id");
        Check.argNotNull(lastRead, "lastRead");
        this.doDeleteContact(session, folderId, id, lastRead);
    }

    @Override
    public void deleteContacts(Session session, String folderId, String[] ids, Date lastRead) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderId, "folderId");
        Check.argNotNull(ids, "ids");
        Check.argNotNull(lastRead, "lastRead");
        this.doDeleteContacts(session, folderId, ids, lastRead);
    }

    @Override
    public void deleteContacts(Session session, String folderId) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderId, "folderId");
        this.doDeleteContacts(session, folderId);
    }

	@Override
    public Contact getUser(Session session, int userID, ContactField[] fields) throws OXException {
		Check.argNotNull(session, "session");
		Contact contact = null;
		SearchIterator<Contact> searchIterator = null;
		try {
			searchIterator = this.doGetUsers(session, new int[] { userID }, (SearchTerm<?>)null, fields, null);
			contact = searchIterator.next();
		} finally {
			if (null != searchIterator) {
				searchIterator.close();
			}
		}
		Check.contactNotNull(contact, session.getContextId(), userID);
		return contact;
    }

	@Override
    public SearchIterator<Contact> getUsers(Session session, int[] userIDs, ContactField[] fields) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(userIDs, "userIDs");
		return this.doGetUsers(session, userIDs, (SearchTerm<?>)null, fields, null);
    }

	@Override
    public SearchIterator<Contact> getAllUsers(Session session, ContactField[] fields, final SortOptions sortOptions) throws OXException {
		Check.argNotNull(session, "session");
		return this.doGetUsers(session, null, (SearchTerm<?>)null, fields, sortOptions);
    }

	@Override
	public <O> SearchIterator<Contact> searchUsers(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(term, "term");
		return this.doGetUsers(session, null, term, fields, sortOptions);
	}

	@Override
	public SearchIterator<Contact> searchUsers(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
		Check.argNotNull(session, "session");
		Check.argNotNull(contactSearch, "contactSearch");
		return this.doGetUsers(session, null, contactSearch, fields, sortOptions);
	}

	@Override
    public String getOrganization(Session session) throws OXException {
		Check.argNotNull(session, "session");
		return this.doGetOrganization(session);
    }

    @Override
    public SearchIterator<Contact> searchContactsWithAnniversary(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(from, "from");
        Check.argNotNull(until, "until");
        return doSearchContactsWithAnniversary(session, from, until, folderIDs, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> searchContactsWithBirthday(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(from, "from");
        Check.argNotNull(until, "until");
        if (from.after(until)) {
            throw new IllegalArgumentException("'from' must not be after 'until'");
        }
        return doSearchContactsWithBirthday(session, from, until, folderIDs, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> autocompleteContacts(Session session, List<String> folderIDs, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(query, "pattern");
        return doAutocompleteContacts(session, folderIDs, query, parameters, fields, sortOptions);
    }

	@Override
    public boolean isFolderEmpty(Session session, String folderID) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderID, "folderID");
        return this.doCheckIfFolderIsEmpty(session, folderID);
    }

    @Override
    public boolean containsForeignObjectInFolder(Session session, String folderID) throws OXException {
        Check.argNotNull(session, "session");
        Check.argNotNull(folderID, "folderID");
        return this.doCheckIfFolderContainsForeignObjects(session, folderID);
    }

	/*
	 * -----------------------------------------------------------------------------------------------------------------------------------
	 */

	protected abstract void doCreateContact(Session session, String folderID, Contact contact) throws OXException;

	protected abstract void doUpdateAndMoveContact(Session session, String sourceFolderId, String targetFolderId, String objectID,
			Contact contact, Date lastRead) throws OXException;

    protected abstract void doUpdateContact(Session session, String folderID, String objectID, Contact contact, Date lastRead)
        throws OXException;

    protected abstract void doUpdateUser(Session session, String folderID, String objectID, Contact contact, Date lastRead)
        throws OXException;

    protected abstract void doDeleteContact(Session session, String folderID, String objectID, Date lastRead) throws OXException;

    protected abstract void doDeleteContacts(Session session, String folderID, String[] objectIDs, Date lastRead) throws OXException;

    protected abstract void doDeleteContacts(Session session, String folderID) throws OXException;

	protected abstract <O> SearchIterator<Contact> doGetContacts(boolean deleted, Session session, String folderID,
			String[] ids, ContactField[] fields, SortOptions sortOptions, Date since) throws OXException;

	protected abstract <O> SearchIterator<Contact> doSearchContacts(Session session, SearchTerm<O> term, ContactField[] fields,
			SortOptions sortOptions) throws OXException;

	protected abstract SearchIterator<Contact> doSearchContacts(Session session, ContactSearchObject contactSearch,
			ContactField[] fields, SortOptions sortOptions) throws OXException;

	protected abstract String doGetOrganization(Session session) throws OXException;

	protected abstract <O> SearchIterator<Contact> doGetUsers(Session session, int[] userIDs, SearchTerm<O> term,
			ContactField[] fields, SortOptions sortOptions) throws OXException;

	protected abstract SearchIterator<Contact> doGetUsers(Session session, int[] userIDs, ContactSearchObject contactSearch,
			ContactField[] fields, SortOptions sortOptions) throws OXException;

    protected abstract SearchIterator<Contact> doSearchContactsWithBirthday(Session session, Date from, Date until, List<String> folderIDs,
        ContactField[] fields, SortOptions sortOptions) throws OXException;

    protected abstract SearchIterator<Contact> doSearchContactsWithAnniversary(Session session, Date from, Date until, List<String> folderIDs,
        ContactField[] fields, SortOptions sortOptions) throws OXException;

    protected abstract SearchIterator<Contact> doAutocompleteContacts(Session session, List<String> folderIDs, String query,
		AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException;

    protected abstract SearchIterator<Contact> doGetContacts(Session session, List<String> folderIDs, ContactField[] fields,
        SortOptions sortOptions) throws OXException;

    protected abstract int doCountContacts(Session session, String folderId) throws OXException;

    protected abstract boolean doCheckIfFolderIsEmpty(Session session, String folderID) throws OXException;

    protected abstract boolean doCheckIfFolderContainsForeignObjects(Session session, String folderID) throws OXException;

}
