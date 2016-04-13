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

package com.openexchange.contact.storage.ldap.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.naming.ldap.SortKey;
import org.slf4j.Logger;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.DefaultContactStorage;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.config.LdapConfig;
import com.openexchange.contact.storage.ldap.config.LdapConfig.IDMapping;
import com.openexchange.contact.storage.ldap.config.LdapConfig.Sorting;
import com.openexchange.contact.storage.ldap.id.DbIDResolver;
import com.openexchange.contact.storage.ldap.id.DynamicIDResolver;
import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
import com.openexchange.contact.storage.ldap.id.StaticIDResolver;
import com.openexchange.contact.storage.ldap.mapping.LdapMapper;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link LdapContactStorage}
 *
 * LDAP storage for contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapContactStorage extends DefaultContactStorage {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LdapContactStorage.class);

    private static final ContactField[] DISTLISTMEMBER_FIELDS = { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3,
        ContactField.OBJECT_ID, ContactField.DISPLAY_NAME, ContactField.SUR_NAME, ContactField.GIVEN_NAME
    };
    private static final Date FALLBACK_DATE = new Date(1000);

    private final LdapConfig config;
    protected final LdapMapper mapper;
    private final LdapFactory factory;
    private volatile LdapIDResolver idResolver;
    private volatile Integer adminID;

    /**
     * Initializes a new {@link LdapContactStorage}.
     *
     * @throws OXException
     */
    public LdapContactStorage(LdapConfig config) throws OXException {
        super();
        this.config = config;
        this.factory = new LdapFactory(config);
        this.mapper = new LdapMapper(config.getContactMappingFile(), LdapConfig.CONFIG_PREFIX);
        LOG.debug("LdapContactStorage initialized.");
    }

    public int getFolderID() throws OXException {
        return parse(config.getFolderID());
    }

    public int getContextID() throws OXException {
        return config.getContextID();
    }

    @Override
    public boolean supports(Session session, String folderId) throws OXException {
        return session.getContextId() == config.getContextID() && config.getFolderID().equals(folderId);
    }

    @Override
    public int getPriority() {
        return this.config.getStoragePriority();
    }

    @Override
    public Contact get(Session session, String folderId, String id, ContactField[] fields) throws OXException {
        check(session.getContextId(), folderId);
        return doGet(session, folderId, id, fields);
    }

    @Override
    public SearchIterator<Contact> all(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
        check(session.getContextId(), folderId);
        return doAll(session, folderId, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> list(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
        check(session.getContextId(), folderId);
        return doList(session, folderId, ids, fields, sortOptions);
    }

    @Override
    public int count(Session session, String folderId, boolean canReadAll) throws OXException {
        check(session.getContextId(), folderId);
        return count(session, config.getBaseDN(), config.getSearchfilter());
    }

    @Override
    public SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        check(session.getContextId(), folderId);
        if (null == mapper.opt(ContactField.LAST_MODIFIED)) {
            LOG.warn("No LDAP mapping for {}, unable to get deleted contacts in period.", ContactField.LAST_MODIFIED);
            return getSearchIterator(null);
        } else if (false == config.isAdsDeletionSupport()) {
            LOG.warn("No ADS deletion support available, unable to get deleted contacts in period.");
            return getSearchIterator(null);
        }
        return doDeleted(session, folderId, since, fields, sortOptions);
    }

    @Override
    public SearchIterator<Contact> modified(Session session, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        check(session.getContextId(), folderID);
        if (null == mapper.opt(ContactField.LAST_MODIFIED)) {
            LOG.warn("No LDAP mapping for {}, unable to get modified contacts in period.", ContactField.LAST_MODIFIED);
            return getSearchIterator(null);
        }
        return doModified(session, folderID, since, fields, sortOptions);
    }

    @Override
    public <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        checkContext(session.getContextId());
        return doSearch(session, term, fields, sortOptions);
    }

    @Override
    public void create(Session session, String folderId, Contact contact) throws OXException {
        throw LdapExceptionCodes.INSERT_NOT_POSSIBLE.create();
    }

    @Override
    public void update(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException {
        throw LdapExceptionCodes.INSERT_NOT_POSSIBLE.create();
    }

    @Override
    public void updateReferences(Session session, Contact originalContact, Contact updatedContact) throws OXException {
        // Nothing to do
    }

    @Override
    public void delete(Session session, String folderId, String id, Date lastRead) throws OXException {
        throw LdapExceptionCodes.DELETE_NOT_POSSIBLE.create();
    }

    @Override
    public SearchIterator<Contact> searchByBirthday(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        if (null == mapper.opt(ContactField.BIRTHDAY)) {
            LOG.warn("No LDAP mapping for {}, unable to search contacts by birthday.", ContactField.BIRTHDAY);
            return getSearchIterator(null);
        } else {
            // use default implementation for now
            return super.searchByBirthday(session, folderIDs, from, until, fields, sortOptions);
        }
    }

    @Override
    public SearchIterator<Contact> searchByAnniversary(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        if (null == mapper.opt(ContactField.ANNIVERSARY)) {
            LOG.warn("No LDAP mapping for {}, unable to search contacts by anniversary.", ContactField.ANNIVERSARY);
            return getSearchIterator(null);
        } else {
            // use default implementation for now
            return super.searchByAnniversary(session, folderIDs, from, until, fields, sortOptions);
        }
    }

    protected Contact doGet(Session session, String folderId, String id, ContactField[] fields) throws OXException {
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator = doList(session, folderId, new String[] { id }, fields, SortOptions.EMPTY);
            if (false == searchIterator.hasNext()) {
                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(parse(id), session.getContextId());
            } else {
                return searchIterator.next();
            }
        } finally {
            Tools.close(searchIterator);
        }
    }

    protected SearchIterator<Contact> doAll(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return getSearchIterator(search(session, fields, null, sortOptions, false));
    }

    protected SearchIterator<Contact> doList(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
        CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        for (String id : ids) {
            SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
            term.addOperand(new ContactFieldOperand(ContactField.OBJECT_ID));
            term.addOperand(new ConstantOperand<String>(id));
            orTerm.addSearchTerm(term);
        }
        return doSearch(session, orTerm, fields, sortOptions);
    }

    protected SearchIterator<Contact> doDeleted(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        SingleSearchTerm term = new SingleSearchTerm(SingleOperation.GREATER_THAN);
        term.addOperand(new ContactFieldOperand(ContactField.LAST_MODIFIED));
        term.addOperand(new ConstantOperand<Date>(since));
        return getSearchIterator(search(session, term, fields, sortOptions, true));
    }

    protected SearchIterator<Contact> doModified(Session session, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        SingleSearchTerm term = new SingleSearchTerm(SingleOperation.GREATER_THAN);
        term.addOperand(new ContactFieldOperand(ContactField.LAST_MODIFIED));
        term.addOperand(new ConstantOperand<Date>(since));
        return getSearchIterator(search(session, term, fields, sortOptions, false));
    }

    protected <O> SearchIterator<Contact> doSearch(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return getSearchIterator(search(session, term, fields, sortOptions, false));
    }

    protected <O> List<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions, boolean deleted) throws OXException {
        LdapIDResolver idResolver = getIDResolver(session);
        String filter = new SearchTermAdapter(term, mapper, idResolver).getFilter();
        return search(session, fields, filter, sortOptions, deleted);
    }

    protected List<Contact> search(Session session, ContactField[] fields, String filter, SortOptions sortOptions, boolean deleted) throws OXException {
        String searchFilter = config.getSearchfilter();
        if (null != filter) {
            searchFilter = "(&" + filter + searchFilter + ")";
        }
        return this.search(session, fields, mapper, config.getBaseDN(), searchFilter, sortOptions, deleted);
    }

    protected List<Contact> getContacts(Session session, ContactField[] fields, SortOptions sortOptions, boolean deleted) throws OXException {
        return this.search(session, fields, mapper, config.getBaseDN(), config.getSearchfilter(), sortOptions, deleted);
    }

    protected List<Contact> search(Session session, ContactField[] fields, LdapMapper mapper, String baseDN, String filter,
        SortOptions sortOptions, boolean deleted) throws OXException {
        SortKey[] sortKeys = null;
        if (Sorting.SERVER.equals(config.getSorting())) {
            try {
                sortKeys = mapper.getSortKeys(sortOptions);
            } catch (OXException e) {
                if (e.isNotFound()) {
                    LOG.debug("Unable to generate LDAP sort keys, falling back to groupware sorting.", e);
                }
            }
        }
        if (null != sortKeys) {
            return search(session, fields, mapper, baseDN, filter, sortKeys, getMaxResults(sortOptions), deleted);
        } else {
            return sort(search(session, fields, mapper, baseDN, filter, null, getMaxResults(sortOptions), deleted), mapper, sortOptions);
        }
    }

    protected int count(Session session, String baseDN, String filter) throws OXException {
        LdapExecutor executor = null;
        try {
            executor = new LdapExecutor(factory, session);
            List<LdapResult> results = executor.search(baseDN, filter, new String[0], null, getMaxResults(SortOptions.EMPTY));
            return results.size();
        } finally {
            if (null != executor) {
                executor.close();
            }
        }
    }

    private List<Contact> search(Session session, ContactField[] fields, LdapMapper mapper, String baseDN, String filter,
        SortKey[] sortKeys, int maxResults, boolean deleted) throws OXException {
        LdapIDResolver idResolver = getIDResolver(session);
        LdapExecutor executor = null;
        try {
            executor = new LdapExecutor(factory, session);
            List<LdapResult> results;
            if (false == deleted) {
                results = executor.search(baseDN, filter, mapper.getLdapAttributes(fields), sortKeys, maxResults);
            } else {
                results = executor.searchDeleted(filter, mapper.getLdapAttributes(fields), sortKeys, maxResults);
            }
            return createContacts(executor, idResolver, mapper, results, fields);
        } finally {
            if (null != executor) {
                executor.close();
            }
        }
    }

    private static int getMaxResults(SortOptions sortOptions) {
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions) && 0 < sortOptions.getLimit()) {
            if (0 < sortOptions.getRangeStart()) {
                return sortOptions.getRangeStart() + sortOptions.getLimit();
            } else {
                return sortOptions.getLimit();
            }
        }
        return -1;
    }

    protected Contact resolveDistList(LdapExecutor executor, LdapIDResolver idResolver, Contact contact) throws OXException {
        if (contact.getMarkAsDistribtuionlist() && null != contact.getDistributionList() && 0 < contact.getDistributionList().length) {
            /*
             * try to resolve all members
             */
            String[] distlistAttributeNames = mapper.getLdapAttributes(DISTLISTMEMBER_FIELDS);
            for (DistributionListEntryObject member : contact.getDistributionList()) {
                LdapResult result = null;
                try {
                    result = executor.getAttributes(member.getDisplayname(), distlistAttributeNames);
                } catch (OXException e) {
                    LOG.warn("Error resolving distribution list member {}", member.getDisplayname(), e);
                }
                if (null != result) {
                    Contact referencedContact = this.createContact(executor, idResolver, mapper, result, DISTLISTMEMBER_FIELDS);
                    if (null != referencedContact) {
                        updateMember(member, referencedContact);
                    }
                }
            }
            /*
             * remove invalid members without e-mail address
             */
            contact.setDistributionList(filterInvalidMembers(contact.getDistributionList()));
        }
        return contact;
    }

    private static DistributionListEntryObject[] filterInvalidMembers(DistributionListEntryObject[] members) {
        if (null != members && 0 < members.length) {
            List<DistributionListEntryObject> validMembers = new ArrayList<DistributionListEntryObject>(members.length);
            for (DistributionListEntryObject member : members) {
                if (isValid(member)) {
                    validMembers.add(member);
                }
            }
            return validMembers.toArray(new DistributionListEntryObject[validMembers.size()]);
        } else {
            return members;
        }
    }

    /**
     * Checks whether a distribution list member is valid, i.e. contains an e-mail address.
     *
     * @param member The distribution list member check
     * @return <code>true</code>, if the member is valid, <code>false</code>, otherwise
     */
    private static boolean isValid(DistributionListEntryObject member) {
        return null != member && null != member.getEmailaddress() && 0 < member.getEmailaddress().trim().length();
    }

    private List<Contact> sort(List<Contact> contacts, LdapMapper mapper, SortOptions sortOptions) {
        if (Sorting.GROUPWARE.equals(config.getSorting()) && null != contacts && 1 < contacts.size() && null != sortOptions
            && false == SortOptions.EMPTY.equals(sortOptions) && null != sortOptions.getOrder() && 0 < sortOptions.getOrder().length) {
            Collections.sort(contacts, mapper.getComparator(sortOptions));
        }
        if (null != contacts && 0 < contacts.size() && null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions) &&
            (0 < sortOptions.getLimit() || 0 < sortOptions.getRangeStart())) {
            int fromIndex = 0 < sortOptions.getRangeStart() ? sortOptions.getRangeStart() : 0;
            if (fromIndex >= contacts.size()) {
                return Collections.emptyList();
            }
            int toIndex = 0 < sortOptions.getLimit() ? fromIndex + sortOptions.getLimit() : 0;
            if (0 == toIndex || toIndex >= contacts.size()) {
                toIndex = contacts.size();
            }
            return contacts.subList(fromIndex, toIndex);
        } else {
            return contacts;
        }
    }

    private List<Contact> createContacts(LdapExecutor executor, LdapIDResolver idResolver, LdapMapper mapper, List<LdapResult> results, ContactField[] fields) throws OXException {
        if (null == results) {
            return null;
        }
        List<Contact> contacts = new ArrayList<Contact>(results.size());
        for (LdapResult result : results) {
            Contact contact = createContact(executor, idResolver, mapper, result, fields);
            if (null != contact) {
                contacts.add(contact);
            }
        }
        return contacts;
    }

    private Contact createContact(LdapExecutor executor, LdapIDResolver idResolver, LdapMapper mapper, LdapResult result, ContactField[] fields) throws OXException {
        Contact contact = mapper.createContact(result, idResolver, fields);
        contact.setParentFolderID(getFolderID());
        contact.setContextId(getContextID());
        if (false == contact.containsLastModified()) {
            contact.setLastModified(FALLBACK_DATE);
        }
        if (false == contact.containsCreationDate()) {
            contact.setLastModified(FALLBACK_DATE);
        }
        if (false == contact.containsCreatedBy()) {
            contact.setCreatedBy(getAdminID());
        }
        if (false == contact.containsModifiedBy()) {
            contact.setModifiedBy(getAdminID());
        }
        if (contact.containsImageLastModified() && contact.containsLastModified()) {
            // use same last_modified if possible
            contact.setImageLastModified(contact.getLastModified());
        }
        if (contact.getMarkAsDistribtuionlist()) {
            resolveDistList(executor, idResolver, contact);
            if (config.isExcludeEmptyLists() && (null == contact.getDistributionList() || 0 == contact.getDistributionList().length)) {
                LOG.debug("Skipping empty distribution list '{}'.", result);
                return null;
            }
            if (false == contact.containsSurName() && contact.containsDisplayName()) {
                contact.setSurName(contact.getDisplayName());
            }
        }
        return contact;
    }

    private LdapIDResolver getIDResolver(Session session) throws OXException {
        if (null != this.idResolver) {
            return idResolver;
        } else if (IDMapping.DYNAMIC.equals(config.getIDMapping())) {
            return new DynamicIDResolver(session, this);
        } else {
            synchronized (this) {
                if (null == idResolver) {
                    if (IDMapping.STATIC.equals(config.getIDMapping())) {
                        idResolver = new StaticIDResolver(config.getContextID(), getFolderID());
                    } else {
                        idResolver = new DbIDResolver(config.getContextID(), getFolderID());
                    }
                }
            }
            return idResolver;
        }
    }

    private static void updateMember(DistributionListEntryObject member, Contact referencedContact) throws OXException {
        if (referencedContact.containsObjectID()) {
            member.setEntryID(referencedContact.getObjectID());
        }
        if (referencedContact.containsParentFolderID()) {
            member.setFolderID(referencedContact.getParentFolderID());
        }
        if (referencedContact.containsDisplayName()) {
            member.setDisplayname(referencedContact.getDisplayName());
        }
        if (referencedContact.containsSurName()) {
            member.setLastname(referencedContact.getSurName());
        }
        if (referencedContact.containsGivenName()) {
            member.setFirstname(referencedContact.getGivenName());
        }
        if (referencedContact.containsEmail1()) {
            member.setEmailaddress(referencedContact.getEmail1(), false);
            member.setEmailfield(DistributionListEntryObject.EMAILFIELD1);
        } else if (referencedContact.containsEmail2()) {
            member.setEmailaddress(referencedContact.getEmail2(), false);
            member.setEmailfield(DistributionListEntryObject.EMAILFIELD2);
        } else if (referencedContact.containsEmail3()) {
            member.setEmailaddress(referencedContact.getEmail3(), false);
            member.setEmailfield(DistributionListEntryObject.EMAILFIELD3);
        }
    }

    private int getAdminID() throws OXException {
        if (null == this.adminID) {
            synchronized (this) {
                if (null == adminID) {
                    adminID = Integer.valueOf(
                        LdapServiceLookup.getService(ContextService.class).getContext(getContextID()).getMailadmin());
                }
            }
        }
        return adminID.intValue();
    }

    /**
     * Checks if the supplied context and folder ID match this LDAP storage's
     * context and folder ID.
     *
     * @param contextID the context ID to check
     * @param folderID the folder ID to check
     * @throws OXException
     */
    protected void check(int contextID, String folderID) throws OXException {
        checkContext(contextID);
        checkFolder(folderID);
    }

    /**
     * Checks if the supplied context ID matches this LDAP storage's
     * context ID.
     *
     * @param contextID the context ID to check
     * @throws OXException
     */
    protected void checkContext(int contextID) throws OXException {
        if (this.config.getContextID() != contextID) {
            throw LdapExceptionCodes.INVALID_CONTEXT.create(contextID);
        }
    }

    /**
     * Checks if the supplied folder ID matches this LDAP storage's
     * folder ID.
     *
     * @param folderID the folder ID to check
     * @throws OXException
     */
    protected void checkFolder(String folderID) throws OXException {
        if (false == this.config.getFolderID().equals(folderID)) {
            throw LdapExceptionCodes.INVALID_FOLDER.create(folderID);
        }
    }

    @Override
    public boolean supports(ContactField... fields) {
        return false;
    }
}
