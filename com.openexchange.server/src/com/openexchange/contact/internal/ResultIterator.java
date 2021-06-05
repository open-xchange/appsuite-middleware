/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.contact.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ResultIterator} - Search iterator for contacts fetched through the
 * contact service, performing additional operations on the contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ResultIterator implements SearchIterator<Contact> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResultIterator.class);

    private static final ContactField[] DLISTMEMBER_FIELDS = {
    	ContactField.FOLDER_ID, ContactField.OBJECT_ID, ContactField.CREATED_BY, ContactField.PRIVATE_FLAG, ContactField.LAST_MODIFIED,
    	ContactField.DISPLAY_NAME, ContactField.SUR_NAME, ContactField.GIVEN_NAME,
        ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.YOMI_FIRST_NAME, ContactField.YOMI_LAST_NAME,
        ContactField.UID };

    private final SearchIterator<Contact> delegate;
    private final boolean needsAttachmentInfo;
    private Contact next;
	private final Map<String, Boolean> canReadAllMap;
	private final Boolean canReadAll;
	private final Session session;

	/**
	 * Initializes a new {@link ResultIterator} where the 'can read all'
	 * information is evaluated dynamically based on the contact's parent
	 * folders.
	 *
	 * @param delegate
	 * @param needsAttachmentInfo
	 * @param contextID
	 * @param userID
	 * @throws OXException
	 */
	public ResultIterator(SearchIterator<Contact> delegate, boolean needsAttachmentInfo, Session session) throws OXException {
		this(delegate, needsAttachmentInfo, session, null);
	}

	/**
	 * Initializes a new {@link ResultIterator} where the supplied 'can read
	 * all' information is used statically.
	 *
	 * @param delegate
	 * @param needsAttachmentInfo
	 * @param contextID
	 * @param userID
	 * @param canReadAll
	 * @throws OXException
	 */
	public ResultIterator(SearchIterator<Contact> delegate, boolean needsAttachmentInfo, Session session, boolean canReadAll) throws OXException {
		this(delegate, needsAttachmentInfo, session, Boolean.valueOf(canReadAll));
	}

	private ResultIterator(SearchIterator<Contact> delegate, boolean needsAttachmentInfo, Session session, Boolean canReadAll) throws OXException {
		super();
		this.delegate = delegate;
		this.needsAttachmentInfo = needsAttachmentInfo;
		this.session = session;
		this.canReadAll = canReadAll;
		this.canReadAllMap = new HashMap<String, Boolean>();
		initNext();
	}

	private void initNext() throws OXException {
        while (delegate.hasNext()) {
            next = delegate.next();
            if (this.accept(next)) {
            	addAttachmentInfo(next);
            	addDistributionListInfo(next);
                return;
            }
        }
        next = null;
	}

	/**
	 * Adds the date of the last modification to attachments of the given
	 * contact when needed, i.e. the information is not already present.
	 *
	 * @param contact the contact to add the attachment information for
	 * @throws OXException
	 */
	private void addAttachmentInfo(Contact contact) throws OXException {
		if (this.needsAttachmentInfo && false == contact.containsLastModifiedOfNewestAttachment() && 0 < contact.getNumberOfAttachments()) {
			contact.setLastModifiedOfNewestAttachment(Attachments.getInstance().getNewestCreationDate(
					Tools.getContext(session.getContextId()), Types.CONTACT, contact.getObjectID()));
		}
	}

	/**
	 * Adds distribution list member information dynamically by querying the
	 * referenced contacts.
	 *
	 * @param contact
	 */
	private void addDistributionListInfo(Contact contact) {
		if (null != contact && 0 < contact.getNumberOfDistributionLists()) {
			DistributionListEntryObject[] members = contact.getDistributionList();
			if (null != members && 0 < members.length) {
				Contact[] referencedContacts = getReferencedContacts(members);
				for (int i = 0; i < referencedContacts.length; i++) {
					if (null != referencedContacts[i]) {
						/*
						 * update member info dynamically
						 */
						updateMemberInfo(members[i], referencedContacts[i]);
					} else {
						/*
						 * 'dead' reference, convert into one-off address entry
						 */
						convertToOneOff(members[i]);
					}
				}
			}
		}
	}

	private void convertToOneOff(DistributionListEntryObject member) {
		member.removeFolderld();
		member.removeEntryID();
		member.setEmailfield(DistributionListEntryObject.INDEPENDENT);
		
		Contact contact = new Contact();
		contact.setDisplayName(member.getDisplayname());
		contact.setEmail1(member.getEmailaddress());
		member.setSortName(contact.getSortName());
	}

    private void updateMemberInfo(DistributionListEntryObject member, Contact contact) {
        if (null != contact.getDisplayName()) {
            member.setDisplayname(contact.getDisplayName());
	    }
        member.setFolderID(contact.getParentFolderID());
        member.setEntryID(contact.getObjectID());
        member.setFirstname(contact.getGivenName());
        member.setLastname(contact.getSurName());
		String email = null;
		if (DistributionListEntryObject.EMAILFIELD1 == member.getEmailfield()) {
            email = contact.getEmail1();
		} else if (DistributionListEntryObject.EMAILFIELD2 == member.getEmailfield()) {
            email = contact.getEmail2();
		} else if (DistributionListEntryObject.EMAILFIELD3 == member.getEmailfield()) {
            email = contact.getEmail3();
		}
		try {
			member.setEmailaddress(email);
		} catch (OXException e) {
			LOG.warn("Error setting e-mail address for updated distribution list member", e);
		}
        member.setSortName(getSortedName(contact));
        member.setContactUid(contact.getUid());
	}
	
    private String getSortedName(Contact con) {

        Locale locale;
        try {
            locale = ServerSessionAdapter.valueOf(session).getUser().getLocale();
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            locale = null;
        }
        
        return con.getSortName(locale);
    }

	private Contact[] getReferencedContacts(DistributionListEntryObject[] members) {
		Contact[] contacts = new Contact[members.length];
		/*
		 * determine queried folders
		 */
		Map<String, List<String>> folderAndObjectIDs = new HashMap<String, List<String>>();
		for (DistributionListEntryObject member : members) {
			if (DistributionListEntryObject.INDEPENDENT != member.getEmailfield()) {
				String folderID = Integer.toString(member.getFolderID());
				if (null == folderAndObjectIDs.get(folderID)) {
					folderAndObjectIDs.put(folderID, new ArrayList<String>());
				}
				folderAndObjectIDs.get(folderID).add(Integer.toString(member.getEntryID()));
			}
		}
		/*
		 * query needed contacts from each folder
		 */
		for (Entry<String, List<String>> entry : folderAndObjectIDs.entrySet()) {
			SearchIterator<Contact> searchIterator = null;
			try {
				ContactStorage storage = Tools.getStorage(session, entry.getKey());
				searchIterator = storage.list(session, "0".equals(entry.getKey()) ? null : entry.getKey(),
                    entry.getValue().toArray(new String[entry.getValue().size()]), DLISTMEMBER_FIELDS);
                while (searchIterator.hasNext()) {
                    Contact contact = searchIterator.next();
                    if (null != contact && this.accept(contact, null)) {
                        /*
                         * add contact info for matching member in result
                         */
                        for (int i = 0; i < members.length; i++) {
                            if (members[i].getEntryID() == contact.getObjectID() && (0 == members[i].getFolderID() || members[i].getFolderID() == contact.getParentFolderID())) {
                                contacts[i] = contact;
                            }
                        }
                    }
                }
			} catch (OXException e) {
				LOG.warn("Error resolving referenced members for distribution list", e);
			} finally {
			    SearchIterators.close(searchIterator);
			}
		}
		return contacts;
	}

	/**
	 * Gets a value indicating whether the supplied contact should be passed
	 * through from the delegate or not.
	 *
	 * @param contact
	 * @return
	 * @throws OXException
	 */
    private boolean accept(final Contact contact) {
		return this.accept(contact, this.canReadAll);
	}

    private boolean accept(Contact contact, Boolean canReadAll) {
		if (contact.getCreatedBy() == session.getUserId()) {
			return true;
		} else if (contact.containsPrivateFlag()) {
			return false;
		} else if (null != canReadAll) {
			// use supplied 'can read all' information
			return canReadAll.booleanValue();
		} else {
			// query 'can read all' permissions dynamically
			String folderID = Integer.toString(contact.getParentFolderID());
			if (false == canReadAllMap.containsKey(folderID)) {
				boolean canReadAllObjects = false;
				try {
					EffectivePermission permission = Tools.getPermission(session.getContextId(), folderID, session.getUserId());
					canReadAllObjects = permission.canReadAllObjects();
				} catch (OXException e) {
					LOG.debug("Unable to determine effective permissions for folder '{}'", folderID, e);
				}
				canReadAllMap.put(folderID, Boolean.valueOf(canReadAllObjects));
			}
			return canReadAllMap.get(folderID).booleanValue();
		}
	}

	@Override
    public boolean hasNext() {
		return null != this.next;
	}

	@Override
	public Contact next() throws OXException {
        final Contact current = next;
        initNext();
        return current;
	}

	@Override
	public void close() {
	    SearchIterators.close(delegate);
	}

	@Override
	public int size() {
		return -1;
	}

	@Override
	public boolean hasWarnings() {
		return delegate.hasWarnings();
	}

	@Override
	public void addWarning(final OXException warning) {
        delegate.addWarning(warning);
	}

	@Override
	public OXException[] getWarnings() {
		return delegate.getWarnings();
	}

}
