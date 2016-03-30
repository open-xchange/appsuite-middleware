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

package com.openexchange.contact.storage;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.internal.SearchAdapter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.FilteringSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link DefaultContactStorage}
 *
 * Abstract {@link ContactStorage} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultContactStorage implements ContactStorage {

    /**
     * Named logger instance.
     */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultContactStorage.class);

    /**
     * Initializes a new {@link DefaultContactStorage}.
     */
    public DefaultContactStorage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean create(Session session, String folderId, Contact contact, String vCard) throws OXException {
        LOG.info("No appropriate implementation for storing VCard found. Will just create the contact.");
        this.create(session, folderId, contact);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean update(Session session, String folderId, String id, Contact contact, Date lastRead, String vCard) throws OXException {
        LOG.info("No appropriate implementation for storing VCard found. Will just update the contact.");
        this.update(session, folderId, id, contact, lastRead);
        return false;
    }

    @Override
    public SearchIterator<Contact> all(Session session, final String folderId, final ContactField[] fields) throws OXException {
        return this.all(session, folderId, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> list(Session session, final String folderId, final String[] ids, final ContactField[] fields) throws OXException {
        return this.list(session, folderId, ids, fields, SortOptions.EMPTY);
    }

    @Override
    public <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields) throws OXException {
        return this.search(session, term, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> search(Session session, ContactSearchObject contactSearch, ContactField[] fields) throws OXException {
        return this.search(session, contactSearch, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        return deleted(session, folderId, since, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> modified(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        return modified(session, folderId, since, fields, SortOptions.EMPTY);
    }

    /**
     * Default implementation converting the {@link ContactSearchObject}
     * to a {@link SearchTerm}. Override if applicable for storage.
     */
    @Override
    public SearchIterator<Contact> search(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return search(session, getSearchTerm(contactSearch), fields, sortOptions);
    }

    /**
     * Default implementation that first queries all contacts in the folder
     * and then deletes them one after the other. Override if applicable for
     * storage.
     */
    @Override
    public void delete(Session session, String folderId) throws OXException {
        Date simulatedLastRead = new Date(Long.MAX_VALUE); // on folder deletion, the client's last modified timestamp is not used
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator = this.all(session, folderId, new ContactField[] { ContactField.OBJECT_ID });
            if (null != searchIterator) {
                while (searchIterator.hasNext()) {
                    delete(session, folderId, String.valueOf(searchIterator.next().getObjectID()), simulatedLastRead);
                }
            }
        } finally {
            close(searchIterator);
        }
    }

    /**
     * Default implementation that first queries the contacts in the folder
     * and then deletes them one after the other. Override if applicable for
     * storage.
     */
    @Override
    public void delete(Session session, String folderId, String[] ids, Date lastRead) throws OXException {
        for (String id : ids) {
            delete(session, folderId, id, lastRead);
        }
    }

    /**
     * Default implementation that first queries the contacts that actually
     * contain a birthday, and then filters the results. Override if
     * applicable for storage.
     */
    @Override
    public SearchIterator<Contact> searchByBirthday(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        SearchIterator<Contact> searchIterator = this.search(
            session, getAnnualDateTerm(folderIDs, ContactField.BIRTHDAY), addUniquely(fields, ContactField.BIRTHDAY), sortOptions);
        return filterByAnnualDate(searchIterator, from, until, ContactField.BIRTHDAY);
    }

    /**
     * Default implementation that first queries the contacts that actually
     * contain an anniversary, and then filters the results. Override if
     * applicable for storage.
     */
    @Override
    public SearchIterator<Contact> searchByAnniversary(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        SearchIterator<Contact> searchIterator = this.search(
            session, getAnnualDateTerm(folderIDs, ContactField.ANNIVERSARY), addUniquely(fields, ContactField.ANNIVERSARY), sortOptions);
        return filterByAnnualDate(searchIterator, from, until, ContactField.ANNIVERSARY);
    }

    /**
     * Default implementation falls back to the contact search object. Override
     * if applicable for storage.
     */
    @Override
    public SearchIterator<Contact> autoComplete(Session session, List<String> folderIDs, String query, AutocompleteParameters parameters, ContactField[] fields, SortOptions sortOptions) throws OXException {
        String pattern = query;
        if (false == Strings.isEmpty(pattern) && false == "*".equals(pattern) && '*' != pattern.charAt(pattern.length() - 1)) {
            pattern = pattern + "*";
        }
        ContactSearchObject preparedSearchObject = new ContactSearchObject();
        preparedSearchObject.setOrSearch(true);
        preparedSearchObject.setEmailAutoComplete(parameters.getBoolean(AutocompleteParameters.REQUIRE_EMAIL, true));
        if (null != folderIDs && 0 < folderIDs.size()) {
            preparedSearchObject.setFolders(parse(folderIDs.toArray(new String[folderIDs.size()])));
        }
        preparedSearchObject.setDisplayName(pattern);
        preparedSearchObject.setEmail1(pattern);
        preparedSearchObject.setEmail2(pattern);
        preparedSearchObject.setEmail3(pattern);
        preparedSearchObject.setGivenName(pattern);
        preparedSearchObject.setSurname(pattern);
        return search(session, preparedSearchObject, fields, sortOptions);
    }

    /**
     * Default implementation that always returns <code>false</code>. Override if applicable for storage.
     */
    @Override
    public boolean supports(ContactField... fields) {
        return false;
    }

    /**
     * Constructs a search term to find contacts what have a value !=
     * <code>null</code> for the supplied date field, combined with an
     * additional restriction for the parent folder IDs. This does only work
     * for the 'birthday'- and 'anniversary' fields.
     *
     * @param folderIDs the possible folder IDs, or <code>null</code> if not relevant
     * @param dateField One of <code>ContactField.ANNIVERSARY</code> or <code>ContactField.BIRTHDAY</code>
     * @return A search term
     */
    private static SearchTerm<?> getAnnualDateTerm(List<String> folderIDs, ContactField dateField) {
        CompositeSearchTerm hasDateTerm = new CompositeSearchTerm(CompositeOperation.NOT);
        SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
        isNullTerm.addOperand(new ContactFieldOperand(dateField));
        hasDateTerm.addSearchTerm(isNullTerm);
        if (null != folderIDs && 0 < folderIDs.size()) {
            CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
            if (1 == folderIDs.size()) {
                SingleSearchTerm folderIDTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                folderIDTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
                folderIDTerm.addOperand(new ConstantOperand<String>(folderIDs.get(0)));
                andTerm.addSearchTerm(folderIDTerm);
            } else {
                CompositeSearchTerm folderIDsTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (String parentFolderID : folderIDs) {
                    SingleSearchTerm folderIDTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                    folderIDTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
                    folderIDTerm.addOperand(new ConstantOperand<String>(parentFolderID));
                    folderIDsTerm.addSearchTerm(folderIDTerm);
                }
                andTerm.addSearchTerm(folderIDsTerm);
            }
            andTerm.addSearchTerm(hasDateTerm);
            return andTerm;
        } else {
            return hasDateTerm;
        }
    }

    /**
     * Filters out contacts whose month/day portion of the date field falls
     * between the supplied period. This does only work for the 'birthday'-
     * and 'anniversary' fields.
     *
     * @param searchIterator The contact search iterator to filter
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @param dateField One of <code>ContactField.ANNIVERSARY</code> or <code>ContactField.BIRTHDAY</code>
     * @return A filtering search iterator
     * @throws OXException
     */
    private static SearchIterator<Contact> filterByAnnualDate(SearchIterator<Contact> searchIterator, final Date from, final Date until,
        final ContactField dateField) throws OXException {
        if (from.after(until)) {
            throw new IllegalArgumentException("from must not be after until");
        }
        /*
         * get from/until years
         */
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(from);
        final int fromYear = calendar.get(Calendar.YEAR);
        calendar.setTime(until);
        final int untilYear = calendar.get(Calendar.YEAR);
        /*
         * wrap condition into filtering iterator
         */
        return new FilteringSearchIterator<Contact>(searchIterator) {

            @Override
            public boolean accept(Contact thing) throws OXException {
                Date date = ContactField.ANNIVERSARY.equals(dateField) ? thing.getAnniversary() :
                    ContactField.BIRTHDAY.equals(dateField) ? thing.getBirthday() : null;
                if (null != date) {
                    calendar.setTime(date);
                    for (int y = fromYear; y <= untilYear; y++) {
                        calendar.set(Calendar.YEAR, y);
                        if (calendar.getTime().before(until) && false == calendar.getTime().before(from)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    /**
     * Adds one or more fields to a given field array uniquely.
     *
     * @param fields The fields
     * @param fieldsToAdd The fields to add uniquely
     * @return A new array containing both the <code>fields</code> and <code>fieldsToAdd</code>
     */
    protected static ContactField[] addUniquely(ContactField[] fields, ContactField...fieldsToAdd) {
        if (null == fields || 0 == fields.length) {
            return fieldsToAdd;
        } else if (null == fieldsToAdd || 0 == fieldsToAdd.length) {
            return fields;
        } else {
            Set<ContactField> contactFields = new HashSet<ContactField>(fields.length + fieldsToAdd.length);
            contactFields.addAll(Arrays.asList(fields));
            contactFields.addAll(Arrays.asList(fieldsToAdd));
            return contactFields.toArray(new ContactField[contactFields.size()]);
        }
    }

    /**
     * Gets all contact fields.
     *
     * @return the fields
     */
    protected static ContactField[] allFields() {
        return ContactField.values();
    }

    /**
     * Closes a search iterator silently.
     *
     * @param searchIterator The search iterator to close, or <code>null</code>
     */
    protected static <T> void close(SearchIterator<T> searchIterator) {
        SearchIterators.close(searchIterator);
    }

    /**
     * Creates a new {@link SearchIterator} for the supplied contact collection.
     *
     * @param contacts the contacts, or <code>null</code> to create an empty iterator
     * @return the contact search iterator
     */
    protected static SearchIterator<Contact> getSearchIterator(Collection<Contact> contacts) {
        if (null == contacts) {
            return new SearchIteratorAdapter<Contact>(Collections.<Contact>emptyList().iterator(), 0);
        }
        return new SearchIteratorAdapter<Contact>(contacts.iterator(), contacts.size());
    }

    /**
     * Parses a numerical identifier from a string, wrapping a possible
     * NumberFormatException into an OXException.
     *
     * @param id the id string
     * @return the parsed identifier
     * @throws OXException
     */
    protected static int parse(String id) throws OXException {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, id);
        }
    }

    /**
     * Parses an array of numerical identifiers from a string, wrapping a
     * possible NumberFormatException into an OXException.
     *
     * @param id the id string
     * @return the parsed identifier
     * @throws OXException
     */
    protected static int[] parse(String[] ids) throws OXException {
        if (null == ids) {
            return new int[0];
        }
        try {
            int[] intIDs = new int[ids.length];
            for (int i = 0; i < intIDs.length; i++) {
                intIDs[i] = Integer.parseInt(ids[i]);
            }
            return intIDs;
        } catch (NumberFormatException e) {
            throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, Arrays.toString(ids));
        }
    }

    /**
     * Converts the supplied contact search object into a search term.
     *
     * @param contactSearch the contact search
     * @return the search term
     * @throws OXException
     */
    protected static SearchTerm<?> getSearchTerm(ContactSearchObject contactSearch) throws OXException {
        return new SearchAdapter(contactSearch).getSearchTerm();
    }

}
