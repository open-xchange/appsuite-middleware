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

package com.openexchange.subscribe.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i2I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.internet.AddressException;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.groupware.tools.mappings.MappedIncorrectString;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.subscribe.TargetFolderSession;
import com.openexchange.subscribe.osgi.SubscriptionServiceRegistry;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ContactFolderUpdaterStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactFolderUpdaterStrategy implements FolderUpdaterStrategy<Contact> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactFolderUpdaterStrategy.class);

    private static final int SQL_INTERFACE = 1;

    private static final int TARGET = 2;

    private static final int SESSION = 3;

    private static final ContactField[] COMPARISON_FIELDS = {
        ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.GIVEN_NAME, ContactField.SUR_NAME, ContactField.BIRTHDAY,
        ContactField.DISPLAY_NAME, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.USERFIELD20, ContactField.CELLULAR_TELEPHONE1 };

    private static final int[] MATCH_COLUMNS = I2i(Arrays.remove(i2I(Contact.CONTENT_COLUMNS), I(Contact.USERFIELD20)));


    /**
     * Initializes a new {@link ContactFolderUpdaterStrategy}.
     */
    public ContactFolderUpdaterStrategy() {
        super();
    }

    @Override
    public int calculateSimilarityScore(final Contact original, final Contact candidate, final Object session) {
        int score = 0;
        final int threshold = getThreshold(session);

        if(isReasonablyEmpty(original) && isReasonablyEmpty(candidate)) {
        	return threshold + 1;
        }
        // For the sake of simplicity we assume that equal names mean equal contacts
        // TODO: This needs to be diversified in the form of "unique-in-context" later (if there is only one "Max Mustermann" in a folder it
        // is unique and qualifies as identifier. If there are two "Max Mustermann" it does not.)
        if ((isset(original.getGivenName()) || isset(candidate.getGivenName())) && eq(original.getGivenName(), candidate.getGivenName())) {
            score += 5;
        }
        if ((isset(original.getSurName()) || isset(candidate.getSurName())) && eq(original.getSurName(), candidate.getSurName())) {
            score += 5;
        }
        if ((isset(original.getDisplayName()) || isset(candidate.getDisplayName())) && eq(original.getDisplayName(), candidate.getDisplayName())) {
            score += 10;
        }
        // an email-address is unique so if this is identical the contact should be the same
        if (eq(original.getEmail1(), candidate.getEmail1())) {
            score += 10;
        }
        if (eq(original.getEmail2(), candidate.getEmail2())) {
            score += 10;
        }
        if (eq(original.getEmail3(), candidate.getEmail3())) {
            score += 10;
        }
        if (eq(original.getCellularTelephone1(), candidate.getCellularTelephone1())) {
            score += 10;
        }
        if (original.containsBirthday() && candidate.containsBirthday() && eq(original.getBirthday(), candidate.getBirthday())) {
            score += 5;
        }

        if( score < threshold && original.matches(candidate, MATCH_COLUMNS)) { //the score check is only to speed the process up
            score = threshold + 1;
        }
        return score;
    }

    private boolean isReasonablyEmpty(Contact c) {
		return !(c.containsEmail1()
		|| c.containsEmail2()
		|| c.containsEmail3()
		|| c.containsSurName()
		|| c.containsGivenName()
		|| c.containsYomiFirstName()
		|| c.containsYomiLastName()
		|| c.containsCompany()
		|| c.containsYomiCompany()
		|| c.containsDisplayName()
		|| c.containsNickname());
	}

	private boolean isset(final String s) {
        return s == null || s.length() > 0;
    }

    protected boolean eq(final Object o1, final Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    @Override
    public void closeSession(final Object session) {
        // Nothing to do
    }

    @Override
    public Collection<Contact> getData(final TargetFolderDefinition target, final Object session) throws OXException {
        List<Contact> contacts = new ArrayList<Contact>();
        Object sqlInterface = getFromSession(SQL_INTERFACE, session);
        Object targetFolderSession = getFromSession(SESSION, session);
        if (sqlInterface instanceof ContactService && targetFolderSession instanceof TargetFolderSession) {
            SearchIterator<Contact> searchIterator = null;
            try {
                searchIterator = ((ContactService)sqlInterface).getAllContacts(
                    (TargetFolderSession)targetFolderSession, target.getFolderId(), COMPARISON_FIELDS);
                if (null != searchIterator) {
                    while (searchIterator.hasNext()) {
                        contacts.add(searchIterator.next());
                    }
                }
            } finally {
                if (null != searchIterator) {
                    searchIterator.close();
                }
            }
        }
        return contacts;
    }

    @Override
    public int getThreshold(final Object session) {
        return 9;
    }

    @Override
    public boolean handles(final FolderObject folder) {
        return folder.getModule() == FolderObject.CONTACT;
    }

    @Override
    public void save(final Contact newElement, final Object session, Collection<OXException> errors) throws OXException {
        Object sqlInterface = getFromSession(SQL_INTERFACE, session);
        Object targetFolderSession = getFromSession(SESSION, session);
        if ((sqlInterface instanceof ContactService) && (targetFolderSession instanceof TargetFolderSession)) {
            TargetFolderDefinition target = (TargetFolderDefinition) getFromSession(TARGET, session);
            newElement.setParentFolderID(target.getFolderIdAsInt());
            ContactService contactService = (ContactService)sqlInterface;
            TargetFolderSession tfs = (TargetFolderSession)targetFolderSession;

            int MAX_RETRIES = 5;
            for (int i = 0; i < MAX_RETRIES; i++) {
                try {
                    contactService.createContact(tfs, target.getFolderId(), newElement);
                    return;
                } catch (OXException e) {
                    if (handle(e, newElement, errors)) {
                        continue;
                    }
                    throw e;
                }
            }
        }
    }

    private Object getFromSession(final int key, final Object session) {
        return ((Map<Integer, Object>) session).get(key);
    }

    @Override
    public Object startSession(final TargetFolderDefinition target) throws OXException {
        final Map<Integer, Object> userInfo = new HashMap<Integer, Object>();
        final TargetFolderSession session = new TargetFolderSession(target);
        ContactService contactService = SubscriptionServiceRegistry.getInstance().getService(ContactService.class);
        userInfo.put(SQL_INTERFACE, contactService);
        userInfo.put(TARGET, target);
        userInfo.put(SESSION, session);
        return userInfo;
    }

    @Override
    public void update(final Contact original, final Contact update, final Object session) throws OXException {
        Object sqlInterface = getFromSession(SQL_INTERFACE, session);
        Object targetFolderSession = getFromSession(SESSION, session);
        if (sqlInterface instanceof ContactService && targetFolderSession instanceof TargetFolderSession) {
            update.setParentFolderID(original.getParentFolderID());
            update.setObjectID(original.getObjectID());
            update.setLastModified(new Date(System.currentTimeMillis()));
            // We need to carry over the UUID to keep existing relations
            update.setUserField20(original.getUserField20());
            ((ContactService)sqlInterface).updateContact((TargetFolderSession)targetFolderSession,
                String.valueOf(update.getParentFolderID()), String.valueOf(update.getObjectID()), update, update.getLastModified());
        }
    }

    /**
     * Tries to handle an exception that occurred when saving a contact, i.e. corrects problematic data in the contact automatically based
     * on the exception if possible.
     *
     * @param e The exception
     * @param contact The contact
     * @param errors The optional errors
     * @return <code>true</code> if problematic data was corrected and the operation may be tried again, <code>false</code>, otherwise
     */
    private boolean handle(OXException e, Contact contact, Collection<OXException> errors) {
        if (ContactExceptionCodes.DATA_TRUNCATION.equals(e)) {
            try {
                return MappedTruncation.truncate(e.getProblematics(), contact);
            } catch (OXException x) {
                LOG.warn("error trying to handle truncated attributes", x);
            }
        }
        if (ContactExceptionCodes.INCORRECT_STRING.equals(e)) {
            try {
                return MappedIncorrectString.replace(e.getProblematics(), contact, "");
            } catch (OXException x) {
                LOG.warn("error trying to handle incorrect strings", x);
            }
        }
        if (ContactExceptionCodes.INVALID_EMAIL.equals(e)) {
            if (contact.containsEmail1()) {
                String value = contact.getEmail1();
                if (false == Strings.isEmpty(value)) {
                    try {
                        new QuotedInternetAddress(value).validate();
                    } catch (final AddressException x) {
                        contact.setEmail1("");
                        if (null != errors) {
                            errors.add(e);
                        }
                    }
                }
            }
            if (contact.containsEmail2()) {
                String value = contact.getEmail2();
                if (false == Strings.isEmpty(value)) {
                    try {
                        new QuotedInternetAddress(value).validate();
                    } catch (final AddressException x) {
                        contact.setEmail2("");
                        if (null != errors) {
                            errors.add(e);
                        }
                    }
                }
            }
            if (contact.containsEmail3()) {
                String value = contact.getEmail3();
                if (false == Strings.isEmpty(value)) {
                    try {
                        new QuotedInternetAddress(value).validate();
                    } catch (final AddressException x) {
                        contact.setEmail3("");
                        if (null != errors) {
                            errors.add(e);
                        }
                    }
                }
            }
        }
        return false;
    }

}
