///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the Open-Xchange, Inc. group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2004-2012 Open-Xchange, Inc.
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//
//package com.openexchange.contact.storage.ldap.internal;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.List;
//import javax.naming.ldap.SortKey;
//import org.apache.commons.logging.Log;
//import com.openexchange.contact.ContactFieldOperand;
//import com.openexchange.contact.SortOptions;
//import com.openexchange.contact.storage.DefaultContactStorage;
//import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
//import com.openexchange.contact.storage.ldap.config.LdapConfig;
//import com.openexchange.contact.storage.ldap.config.LdapConfig.ContactTypes;
//import com.openexchange.contact.storage.ldap.config.LdapConfig.IDMapping;
//import com.openexchange.contact.storage.ldap.config.LdapConfig.Sorting;
//import com.openexchange.contact.storage.ldap.id.DbIDResolver;
//import com.openexchange.contact.storage.ldap.id.DynamicIDResolver;
//import com.openexchange.contact.storage.ldap.id.LdapIDResolver;
//import com.openexchange.contact.storage.ldap.id.StaticIDResolver;
//import com.openexchange.contact.storage.ldap.mapping.LdapMapper;
//import com.openexchange.context.ContextService;
//import com.openexchange.exception.OXException;
//import com.openexchange.groupware.contact.ContactExceptionCodes;
//import com.openexchange.groupware.contact.ContactMergerator;
//import com.openexchange.groupware.contact.helpers.ContactField;
//import com.openexchange.groupware.container.Contact;
//import com.openexchange.groupware.container.DistributionListEntryObject;
//import com.openexchange.log.LogFactory;
//import com.openexchange.search.CompositeSearchTerm;
//import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
//import com.openexchange.search.SearchTerm;
//import com.openexchange.search.SingleSearchTerm;
//import com.openexchange.search.SingleSearchTerm.SingleOperation;
//import com.openexchange.search.internal.operands.ConstantOperand;
//import com.openexchange.session.Session;
//import com.openexchange.tools.iterator.SearchIterator;
//
///**
// * {@link ExplicitDistListLdapContactStorage} 
// * 
// * LDAP storage for contacts.
// *
// * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
// */
//public class ExplicitDistListLdapContactStorage extends DefaultContactStorage {
//    
//    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ExplicitDistListLdapContactStorage.class));
//    private static final ContactField[] DISTLISTMEMBER_FIELDS = { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, 
//        ContactField.OBJECT_ID, ContactField.DISPLAY_NAME,ContactField.SUR_NAME, ContactField.GIVEN_NAME
//    };
//
//    protected final LdapConfig config;
//    protected final LdapMapper contactMapper;
//    protected final LdapMapper distListMapper;
//    protected final LdapFactory factory;
//    private LdapIDResolver idResolver;
//    private Integer adminID;
//    
//    /**
//     * Initializes a new {@link ExplicitDistListLdapContactStorage}.
//     * 
//     * @throws OXException 
//     */
//    public ExplicitDistListLdapContactStorage(LdapConfig config) throws OXException {
//    	super();
//    	this.config = config;
//    	this.factory = new LdapFactory(config);
//        this.contactMapper = new LdapMapper(config.getContactMappingFile(), LdapConfig.CONFIG_PREFIX);
//        this.distListMapper = isFetchDistLists() ? new LdapMapper(config.getDistListMappingFile(), LdapConfig.CONFIG_PREFIX) : null;
//    	LOG.debug("LdapContactStorage initialized.");
//    }
//    
//    public int getFolderID() throws OXException {
//        return parse(config.getFolderID());
//    }
//
//    public int getContextID() throws OXException {
//        return config.getContextID();
//    }
//
//    @Override
//    public boolean supports(Session session, String folderId) throws OXException {
//        checkContext(session.getContextId());
//        return this.config.getFolderID().equals(folderId);
//    }
//
//    @Override
//    public int getPriority() {
//        return this.config.getStoragePriority();
//    }
//
//    @Override
//    public Contact get(Session session, String folderId, String id, ContactField[] fields) throws OXException {
//        check(session.getContextId(), folderId);
//        SearchIterator<Contact> searchIterator = null; 
//        try {
//            searchIterator = this.list(session, folderId, new String[] { id }, fields);
//            if (false == searchIterator.hasNext()) {
//                throw ContactExceptionCodes.CONTACT_NOT_FOUND.create(parse(id), session.getContextId());
//            } else {
//                return searchIterator.next();            
//            }
//        } finally {
//            if (null != searchIterator) {
//                searchIterator.close();
//            }
//        }
//    }
//
//    @Override
//    public SearchIterator<Contact> all(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
////        check(session.getContextId(), folderId);
//        List<Contact> contacts = isFetchContacts() ? searchContacts(session, fields, null, sortOptions, false) : null;
//        List<Contact> distLists = isFetchDistLists() ? searchDistLists(session, fields, null, sortOptions, false) : null;
//        return merge(contacts, distLists, sortOptions);
//    }
//
//    @Override
//    public SearchIterator<Contact> list(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
//        check(session.getContextId(), folderId);
//        CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
//        for (String id : ids) {
//            SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
//            term.addOperand(new ContactFieldOperand(ContactField.OBJECT_ID));
//            term.addOperand(new ConstantOperand<String>(id));
//            orTerm.addSearchTerm(term);
//        }
//        return this.search(session, orTerm, fields, sortOptions);
//    }
//
//    @Override
//    public SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
//        check(session.getContextId(), folderId);
//        SingleSearchTerm term = new SingleSearchTerm(SingleOperation.GREATER_THAN);
//        term.addOperand(new ContactFieldOperand(ContactField.LAST_MODIFIED));
//        term.addOperand(new ConstantOperand<Date>(since));
//        return search(session, term, fields, sortOptions, true);
//    }
//
//    @Override
//    public SearchIterator<Contact> modified(Session session, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
//        check(session.getContextId(), folderID);
//        SingleSearchTerm term = new SingleSearchTerm(SingleOperation.GREATER_THAN);
//        term.addOperand(new ContactFieldOperand(ContactField.LAST_MODIFIED));
//        term.addOperand(new ConstantOperand<Date>(since));
//        return this.search(session, term, fields, sortOptions);
//    }
//
//    @Override
//    public <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
//        checkContext(session.getContextId());
//        return search(session, term, fields, sortOptions, false);
//    }
//
//    @Override
//    public void create(Session session, String folderId, Contact contact) throws OXException {
//        throw LdapExceptionCodes.INSERT_NOT_POSSIBLE.create();
//    }
//
//    @Override
//    public void update(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException {
//        throw LdapExceptionCodes.INSERT_NOT_POSSIBLE.create();
//    }
//
//    @Override
//    public void updateReferences(Session session, Contact contact) throws OXException {
//        // TODO Auto-generated method stub
//    }
//
//    @Override
//    public void delete(Session session, String folderId, String id, Date lastRead) throws OXException {
//        throw LdapExceptionCodes.DELETE_NOT_POSSIBLE.create();
//    }
//    
//    private <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions, boolean deleted) throws OXException {
//        LdapIDResolver idResolver = getIDResolver(session);
//        List<Contact> contacts = isFetchContacts() ? 
//            searchContacts(session, fields, new SearchTermAdapter(term, contactMapper, idResolver).getFilter(), sortOptions, deleted) : null;
//        List<Contact> distLists = isFetchDistLists() ? 
//            searchDistLists(session, fields, new SearchTermAdapter(term, distListMapper, idResolver).getFilter(), sortOptions, deleted) : null;
//        return merge(contacts, distLists, sortOptions);
//    }
//    
//    private SearchIterator<Contact> merge(List<Contact> contacts1, List<Contact> contacts2, SortOptions sortOptions) throws OXException {
//        if (null == contacts1 || 0 == contacts1.size()) {
//            return getSearchIterator(contacts2);
//        } else if (null == contacts2 || 0 == contacts2.size()) {
//            return getSearchIterator(contacts1);
//        } else {
//            Comparator<Contact> comparator = contactMapper.getComparator(sortOptions);
//            if (null != comparator) {
//                List<SearchIterator<Contact>> searchIterators = new ArrayList<SearchIterator<Contact>>(2);
//                searchIterators.add(getSearchIterator(contacts1));
//                searchIterators.add(getSearchIterator(contacts2));
//                return new ContactMergerator(comparator, searchIterators);
//            } else {
//                ArrayList<Contact> contacts = new ArrayList<Contact>();
//                contacts.addAll(contacts1);
//                contacts.addAll(contacts2);
//                return getSearchIterator(contacts);
//            }
//        }
//    }
//    
//    protected List<Contact> searchContacts(Session session, ContactField[] fields, String filter, SortOptions sortOptions, boolean deleted) throws OXException {
//        String searchFilter = config.getSearchfilter();
//        if (null != filter) {
//            searchFilter = "(&" + filter + searchFilter + ")";
//        }
//        return this.search(session, fields, contactMapper, config.getBaseDNUsers(), searchFilter, sortOptions, deleted);
//    }
//
//    protected List<Contact> searchDistLists(Session session, ContactField[] fields, String filter, SortOptions sortOptions, boolean deleted) throws OXException {
//        String searchFilter = null != config.getSearchfilterDistributionlist() ? config.getSearchfilterDistributionlist() : config.getSearchfilter();
//        if (null != filter) {
//            searchFilter = "(&" + filter + searchFilter + ")";
//        }
//        String baseDN = null != config.getBaseDNDistributionlist() ? config.getBaseDNDistributionlist() : config.getBaseDNUsers();        
//        return this.search(session, fields, distListMapper, baseDN, searchFilter, sortOptions, deleted);
//    }
//    
//    protected List<Contact> search(Session session, ContactField[] fields, LdapMapper mapper, String baseDN, String filter, 
//        SortOptions sortOptions, boolean deleted) throws OXException {
//        int limit = null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions) ? sortOptions.getLimit() : -1;
//        LdapIDResolver idResolver = getIDResolver(session);
//        SortKey[] sortKeys = Sorting.SERVER.equals(config.getSorting()) ? mapper.getSortKeys(sortOptions) : null;
//        //TODO rangeStart
//        LdapExecutor executor = null;
//        try {
//            executor = new LdapExecutor(factory, session);
//            List<LdapResult> results = executor.search(
//                baseDN, filter, mapper.getLdapAttributes(fields), sortKeys, limit, deleted);
//            return sort(resolveDistLists(idResolver, createContacts(idResolver, mapper, results, fields), executor), mapper, sortOptions);
//        } finally {
//            if (null != executor) {
//                executor.close();
//            }
//        }
//    }
//
//    private List<Contact> resolveDistLists(LdapIDResolver idResolver, List<Contact> contacts, LdapExecutor executor) throws OXException {
//        if (null != contacts && 0 < contacts.size()) {
//            String[] distlistAttributeNames = contactMapper.getLdapAttributes(DISTLISTMEMBER_FIELDS);
//            for (Contact contact : contacts) {
//                if (contact.getMarkAsDistribtuionlist() && null != contact.getDistributionList() && 0 < contact.getDistributionList().length) {
//                    for (DistributionListEntryObject member : contact.getDistributionList()) {
//                        LdapResult result = executor.getAttributes(member.getDisplayname(), distlistAttributeNames);
//                        Contact referencedContact = this.createContact(idResolver, contactMapper, result, DISTLISTMEMBER_FIELDS);
//                        updateMember(member, referencedContact);
//                    }
//                }
//            }
//        }
//        return contacts;
//    }
//
//    private List<Contact> sort(List<Contact> contacts, LdapMapper mapper, SortOptions sortOptions) {
//        if (false == Sorting.SERVER.equals(config.getSorting()) || null == contacts || 2 > contacts.size() || 
//            null == sortOptions || SortOptions.EMPTY.equals(sortOptions) || 
//            null == sortOptions.getOrder() || 0 == sortOptions.getOrder().length) {
//            return contacts;
//        } 
//        Collections.sort(contacts, mapper.getComparator(sortOptions));
//        return contacts;
//    }
//    
//    private List<Contact> createContacts(LdapIDResolver idResolver, LdapMapper mapper, List<LdapResult> results, ContactField[] fields) throws OXException {
//        if (null == results) {
//            return null;
//        }
//        List<Contact> contacts = new ArrayList<Contact>(results.size());
//        for (LdapResult result : results) {
//            contacts.add(createContact(idResolver, mapper, result, fields));
//        }
//        return contacts;
//    }
//    
//    private Contact createContact(LdapIDResolver idResolver, LdapMapper mapper, LdapResult result, ContactField[] fields) throws OXException {
//        Contact contact = mapper.createContact(result, idResolver, fields);
//        contact.setParentFolderID(getFolderID());
//        contact.setContextId(getContextID());
//        if (contact.containsImageLastModified() && contact.containsLastModified()) {
//            // use same last_modified if possible
//            contact.setImageLastModified(contact.getLastModified());
//        }
//        if (false == contact.containsCreatedBy()) {
//            contact.setCreatedBy(getAdminID());
//        }
//        if (false == contact.containsModifiedBy()) {
//            contact.setModifiedBy(getAdminID());
//        }
////        if (false == contact.getMarkAsDistribtuionlist()) {
////            //TEST: users: object IDs from 220000
////            int userID = contact.getObjectID() - 220000;
////            if (0 < userID && userID < 100) {
////                contact.setInternalUserId(userID);
////            }
////        }
//        return contact;
//    }
//
//    private LdapIDResolver getIDResolver(Session session) throws OXException {
//        if (null != this.idResolver) {
//            return idResolver;
//        } else if (IDMapping.DYNAMIC.equals(config.getIDMapping())) {
//            return new DynamicIDResolver(session, this);
//        } else {
//            synchronized (this) {
//                if (null == idResolver) {
//                    if (IDMapping.STATIC.equals(config.getIDMapping())) {
//                        idResolver = new StaticIDResolver(config.getContextID(), getFolderID());
//                    } else {
//                        idResolver = new DbIDResolver(config.getContextID(), getFolderID());
//                    }
//                }                
//            }
//            return idResolver;
//        }
//    }
//
//    private static void updateMember(DistributionListEntryObject member, Contact referencedContact) throws OXException {
//        if (referencedContact.containsObjectID()) {
//            member.setEntryID(referencedContact.getObjectID());
//        }
//        if (referencedContact.containsParentFolderID()) {
//            member.setFolderID(referencedContact.getParentFolderID());
//        }
//        if (referencedContact.containsDisplayName()) {
//            member.setDisplayname(referencedContact.getDisplayName());
//        }
//        if (referencedContact.containsSurName()) {
//            member.setLastname(referencedContact.getSurName());
//        }
//        if (referencedContact.containsGivenName()) {
//            member.setFirstname(referencedContact.getGivenName());
//        }
//        if (referencedContact.containsEmail1()) {
//            member.setEmailaddress(referencedContact.getEmail1(), false);
//            member.setEmailfield(DistributionListEntryObject.EMAILFIELD1);
//        } else if (referencedContact.containsEmail2()) {
//            member.setEmailaddress(referencedContact.getEmail2(), false);
//            member.setEmailfield(DistributionListEntryObject.EMAILFIELD2);
//        } else if (referencedContact.containsEmail3()) {
//            member.setEmailaddress(referencedContact.getEmail3(), false);
//            member.setEmailfield(DistributionListEntryObject.EMAILFIELD3);
//        }
//    }
//    
//    private boolean isFetchContacts() {
//        return ContactTypes.USERS.equals(config.getContactTypes()) || ContactTypes.BOTH.equals(config.getContactTypes()); 
//    }
//    
//    private boolean isFetchDistLists() {
//        return ContactTypes.DISTRIBUTIONLISTS.equals(config.getContactTypes()) || ContactTypes.BOTH.equals(config.getContactTypes()); 
//    }
//
//    private int getAdminID() throws OXException {
//        if (null == this.adminID) {
//            synchronized (this) {
//                if (null == adminID) {
//                    adminID = Integer.valueOf(
//                        LdapServiceLookup.getService(ContextService.class).getContext(getContextID()).getMailadmin());
//                }
//            }            
//        }
//        return adminID.intValue();
//    }
//
//    /**
//     * Checks if the supplied context and folder ID match this LDAP storage's 
//     * context and folder ID.
//     * 
//     * @param contextID the context ID to check
//     * @param folderID the folder ID to check
//     * @throws OXException
//     */
//    private void check(int contextID, String folderID) throws OXException {
//        checkContext(contextID);
//        checkFolder(folderID);
//    }
//    
//    /**
//     * Checks if the supplied context ID matches this LDAP storage's 
//     * context ID.
//     * 
//     * @param contextID the context ID to check
//     * @throws OXException
//     */
//    private void checkContext(int contextID) throws OXException {
//        if (this.config.getContextID() != contextID) {
//            throw LdapExceptionCodes.INVALID_CONTEXT.create(contextID);
//        }
//    }
//
//    /**
//     * Checks if the supplied folder ID matches this LDAP storage's 
//     * folder ID.
//     * 
//     * @param folderID the folder ID to check
//     * @throws OXException
//     */
//    private void checkFolder(String folderID) throws OXException {
//        if (false == this.config.getFolderID().equals(folderID)) {
//            throw LdapExceptionCodes.INVALID_FOLDER.create(folderID);
//        }
//    }
//
//}
//
