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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    	ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, };

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
	}

	private void updateMemberInfo(DistributionListEntryObject member, Contact referencedContact) {
	    if (null != referencedContact.getDisplayName()) {
	        member.setDisplayname(referencedContact.getDisplayName());
	    }
		member.setFolderID(referencedContact.getParentFolderID());
		member.setEntryID(referencedContact.getObjectID());
		member.setFirstname(referencedContact.getGivenName());
		member.setLastname(referencedContact.getSurName());
		String email = null;
		if (DistributionListEntryObject.EMAILFIELD1 == member.getEmailfield()) {
			email = referencedContact.getEmail1();
		} else if (DistributionListEntryObject.EMAILFIELD2 == member.getEmailfield()) {
			email = referencedContact.getEmail2();
		} else if (DistributionListEntryObject.EMAILFIELD3 == member.getEmailfield()) {
			email = referencedContact.getEmail3();
		}
		try {
			member.setEmailaddress(email);
		} catch (OXException e) {
			LOG.warn("Error setting e-mail address for updated distribution list member", e);
		}
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
					try {
						if (null != contact && this.accept(contact, null)) {
							/*
							 * add contact info for matching member in result
							 */
							for (int i = 0; i < members.length; i++) {
								if (members[i].getEntryID() == contact.getObjectID() && (0 == members[i].getFolderID() ||
										members[i].getFolderID() == contact.getParentFolderID())) {
									contacts[i] = contact;
								}
							}
						}
					} catch (OXException e) {
						LOG.warn("Error resolving referenced member for distribution list", e);
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
	private boolean accept(final Contact contact) throws OXException {
		return this.accept(contact, this.canReadAll);
	}

	private boolean accept(Contact contact, Boolean canReadAll) throws OXException {
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
				} catch (final OXException e) {
					LOG.debug("Unable to determine effective permissions for folder '{}'", folderID, e);
				}
				canReadAllMap.put(folderID, Boolean.valueOf(canReadAllObjects));
			}
			return canReadAllMap.get(folderID).booleanValue();
		}
	}

	@Override
	public boolean hasNext() throws OXException {
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
