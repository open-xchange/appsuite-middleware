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

package com.openexchange.publish.services;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link SimContactService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimContactService implements ContactService {

    private final Map<String, List<Contact>> folders = new HashMap<String, List<Contact>>();

    public void simulateContact(final int cid, final int folderId, final int id1, final String surname) {
        final Contact contact = new Contact();
        contact.setContextId(cid);
        contact.setParentFolderID(folderId);
        contact.setObjectID(id1);
        contact.setSurName(surname);

        getFolderList(Integer.toString(folderId)).add(contact);
    }

    public void simulateDistributionList(final int cid, final int folderId, final int id1, final String name) {
        final Contact contact = new Contact();
        contact.setContextId(cid);
        contact.setParentFolderID(folderId);
        contact.setObjectID(id1);
        contact.setDisplayName(name);
        contact.setMarkAsDistributionlist(true);

        getFolderList(Integer.toString(folderId)).add(contact);
    }

    private List<Contact> getFolderList(final String folderId) {
        if (folders.containsKey(folderId)) {
            return folders.get(folderId);
        }
        folders.put(folderId, new LinkedList<Contact>());
        return folders.get(folderId);
    }


	@Override
	public Contact getContact(final Session session, final String folderId, final String id)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public Contact getContact(final Session session, final String folderId, final String id,
			final ContactField[] fields) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getAllContacts(final Session session, final String folderId) throws OXException {
		return getAllContacts(session, folderId, null, null);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(final Session session,
			final String folderId, final SortOptions sortOptions) throws OXException {
		return getAllContacts(session, folderId, null, sortOptions);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(final Session session,
			final String folderId, final ContactField[] fields) throws OXException {
		return getAllContacts(session, folderId, fields, null);
	}

	@Override
	public SearchIterator<Contact> getAllContacts(final Session session, final String folderId, final ContactField[] fields, final SortOptions sortOptions)
			throws OXException {
        return new SearchIteratorAdapter<Contact>(getFolderList(folderId).iterator());
	}

	@Override
	public SearchIterator<Contact> getContacts(final Session session,
			final String folderId, final String[] ids) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getContacts(final Session session,
			final String folderId, final String[] ids, final SortOptions sortOptions)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getContacts(final Session session,
			final String folderId, final String[] ids, final ContactField[] fields)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getContacts(final Session session,
			final String folderId, final String[] ids, final ContactField[] fields,
			final SortOptions sortOptions) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getDeletedContacts(final Session session,
			final String folderId, final Date since) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getDeletedContacts(final Session session,
			final String folderId, final Date since, final ContactField[] fields)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getDeletedContacts(final Session session,
			final String folderId, final Date since, final ContactField[] fields,
			final SortOptions sortOptions) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(final Session session,
			final String folderId, final Date since) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(final Session session,
			final String folderId, final Date since, final ContactField[] fields)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> getModifiedContacts(final Session session,
			final String folderId, final Date since, final ContactField[] fields,
			final SortOptions sortOptions) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(final Session session,
			final SearchTerm<O> term) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(final Session session,
			final SearchTerm<O> term, final SortOptions sortOptions) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(final Session session,
			final SearchTerm<O> term, final ContactField[] fields) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public <O> SearchIterator<Contact> searchContacts(final Session session,
			final SearchTerm<O> term, final ContactField[] fields, final SortOptions sortOptions)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public void createContact(final Session session, final String folderId, final Contact contact)
			throws OXException {
		// Nothing to do

	}

	@Override
	public void updateContact(final Session session, final String folderId, final String id,
			final Contact contact, final Date lastRead) throws OXException {
		// Nothing to do

	}

	@Override
	public void deleteContact(final Session session, final String folderId, final String id,
			final Date lastRead) throws OXException {
		// Nothing to do

	}

	@Override
	public Contact getUser(final Session session, final int userID) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public Contact getUser(final Session session, final int userID, final ContactField[] fields)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public String getOrganization(final Session session) throws OXException {
		// Nothing to do
		return null;
	}

    /* (non-Javadoc)
     * @see com.openexchange.contact.ContactService#getUsers(com.openexchange.session.Session, int[])
     */
    @Override
    public SearchIterator<Contact> getUsers(final Session session, final int[] userIDs) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.contact.ContactService#getUsers(com.openexchange.session.Session, int[], com.openexchange.groupware.contact.helpers.ContactField[])
     */
    @Override
    public SearchIterator<Contact> getUsers(final Session session, final int[] userIDs, final ContactField[] fields) throws OXException {
        // Nothing to do
        return null;
    }

	@Override
	public SearchIterator<Contact> getAllUsers(final Session session,
			final ContactField[] fields, final SortOptions sortOptions) throws OXException {
		// Nothing to do
		return null;
	}

    /* (non-Javadoc)
     * @see com.openexchange.contact.ContactService#searchUsers(com.openexchange.session.Session, com.openexchange.search.SearchTerm, com.openexchange.groupware.contact.helpers.ContactField[], com.openexchange.contact.SortOptions)
     */
    @Override
    public <O> SearchIterator<Contact> searchUsers(final Session session, final SearchTerm<O> term, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        return null;
    }

	@Override
	public SearchIterator<Contact> searchContacts(final Session session,
			final ContactSearchObject contactSearch) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> searchContacts(final Session session,
			final ContactSearchObject contactSearch, final SortOptions sortOptions)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> searchContacts(final Session session,
			final ContactSearchObject contactSearch, final ContactField[] fields)
			throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> searchContacts(final Session session,
			final ContactSearchObject contactSearch, final ContactField[] fields,
			final SortOptions sortOptions) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public SearchIterator<Contact> searchUsers(final Session session,
			final ContactSearchObject contactSearch, final ContactField[] fields,
			final SortOptions sortOptions) throws OXException {
		// Nothing to do
		return null;
	}

    @Override
    public void deleteContacts(final Session session, final String folderId) throws OXException {
        // Nothing to do

    }

    @Override
    public void deleteContacts(final Session session, final String folderId, final String[] ids, final Date lastRead) throws OXException {
        // Nothing to do

    }

    @Override
    public SearchIterator<Contact> searchContactsWithBirthday(final Session session, final Date from, final Date until, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public SearchIterator<Contact> searchContactsWithBirthday(final Session session, final List<String> folderIDs, final Date from, final Date until, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public SearchIterator<Contact> searchContactsWithAnniversary(final Session session, final Date from, final Date until, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public SearchIterator<Contact> searchContactsWithAnniversary(final Session session, final List<String> folderIDs, final Date from, final Date until, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public SearchIterator<Contact> getAllContacts(final Session session, final List<String> folderIDs, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public SearchIterator<Contact> getAllContacts(final Session session, final ContactField[] fields, final SortOptions sortOptions) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public void updateUser(final Session session, final String folderId, final String id, final Contact contact, final Date lastRead) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public int countContacts(Session session, String folderId) throws OXException {
        return 0;
    }

    @Override
    public boolean isFolderEmpty(Session session, String folderID) throws OXException {
        return false;
    }

    @Override
    public boolean containsForeignObjectInFolder(Session session, String folderID) throws OXException {
        return false;
    }

    @Override
    public SearchIterator<Contact> autocompleteContacts(Session session, List<String> folderIDs, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> autocompleteContacts(Session session, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean supports(Session session, String folderID, ContactField... fields) throws OXException {
        return false;
    }
}
