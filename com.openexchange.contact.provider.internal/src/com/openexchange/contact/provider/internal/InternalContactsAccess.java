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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.contact.provider.internal;

import static com.openexchange.contact.ContactSessionParameterNames.getParamReadOnlyConnection;
import static com.openexchange.contact.ContactSessionParameterNames.getParamWritableConnection;
import static com.openexchange.contact.common.ContactsParameters.PARAMETER_CONNECTION;
import static com.openexchange.contact.provider.internal.Constants.ACCOUNT_ID;
import static com.openexchange.contact.provider.internal.Constants.CONTENT_TYPE;
import static com.openexchange.contact.provider.internal.Constants.PRIVATE_FOLDER_ID;
import static com.openexchange.contact.provider.internal.Constants.PUBLIC_FOLDER_ID;
import static com.openexchange.contact.provider.internal.Constants.SHARED_FOLDER_ID;
import static com.openexchange.contact.provider.internal.Constants.TREE_ID;
import static com.openexchange.contact.provider.internal.Constants.USER_PROPERTY_PREFIX;
import static com.openexchange.folderstorage.ContactsFolderConverter.getStorageFolder;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.common.ContactsSession;
import com.openexchange.contact.common.DefaultGroupwareContactsFolder;
import com.openexchange.contact.common.ExtendedProperties;
import com.openexchange.contact.common.ExtendedProperty;
import com.openexchange.contact.common.GroupwareContactsFolder;
import com.openexchange.contact.common.GroupwareFolderType;
import com.openexchange.contact.common.UsedForSync;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.provider.composition.impl.idmangling.IDMangler;
import com.openexchange.contact.provider.folder.AnnualDateFolderSearchAware;
import com.openexchange.contact.provider.folder.FolderSearchAware;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContactsFolderConverter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Collators;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InternalContactsAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class InternalContactsAccess implements com.openexchange.contact.provider.groupware.InternalContactsAccess, FolderSearchAware, AnnualDateFolderSearchAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalContactsAccess.class);

    private static final ContactField[] ALL_CONTACT_FIELDS = ContactField.values();

    private final ServiceLookup services;
    private final ContactsSession session;

    /**
     * Initializes a new {@link InternalContactsAccess}.
     *
     * @param session The session
     * @param services The services lookup instance
     */
    public InternalContactsAccess(ContactsSession session, ServiceLookup services) {
        super();
        this.session = session;
        this.services = services;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public int countContacts(String folderId) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return getContactService().countContacts(session.getSession(), folderId);
    }

    @Override
    public void createContact(String folderId, Contact contact) throws OXException {
        decorateSessionWithWritableConnection();
        getContactService().createContact(session.getSession(), folderId, contact);
        contact.setId(asString(contact.getObjectID()));
    }

    @Override
    public void updateContact(ContactID contactId, Contact contact, long clientTimestamp) throws OXException {
        decorateSessionWithWritableConnection();
        getContactService().updateContact(session.getSession(), contactId.getFolderID(), contactId.getObjectID(), contact, new Date(clientTimestamp));
        contact.setId(asString(contact.getObjectID()));
    }

    @Override
    public void deleteContact(ContactID contactId, long clientTimestamp) throws OXException {
        decorateSessionWithWritableConnection();
        getContactService().deleteContact(session.getSession(), contactId.getFolderID(), contactId.getObjectID(), new Date(clientTimestamp));
    }

    @Override
    public void deleteContacts(List<ContactID> contactsIds, long clientTimestamp) throws OXException {
        decorateSessionWithWritableConnection();
        for (Entry<String, List<String>> entry : separateContactIdsPerFolder(contactsIds).entrySet()) {
            getContactService().deleteContacts(session.getSession(), entry.getKey(), entry.getValue().toArray(new String[0]), new Date(clientTimestamp));
        }
    }

    @Override
    public void deleteContacts(String folderId) throws OXException {
        decorateSessionWithWritableConnection();
        getContactService().deleteContacts(session.getSession(), folderId);
    }

    ///////////////////////////// FOLDERS /////////////////////////////////

    @Override
    public String createFolder(ContactsFolder folder) throws OXException {
        String folderId;
        {
            DefaultGroupwareContactsFolder plainFolder = new DefaultGroupwareContactsFolder(folder);
            plainFolder.setExtendedProperties(null);
            ParameterizedFolder folderToCreate = getStorageFolder(TREE_ID, CONTENT_TYPE, plainFolder, null, ACCOUNT_ID, null);
            FolderResponse<String> response = getFolderService().createFolder(folderToCreate, session.getSession(), initDecorator());
            folderId = response.getResponse();
        }
        if (null != folder.getExtendedProperties()) {
            updateProperties(getFolder(folderId), folder.getExtendedProperties());
        }
        return folderId;
    }

    @Override
    public String updateFolder(String folderId, ContactsFolder folder, long clientTimestamp) throws OXException {
        GroupwareContactsFolder originalFolder = getFolder(folderId);
        if (null != folder.getExtendedProperties()) {
            updateProperties(originalFolder, folder.getExtendedProperties());
            DefaultGroupwareContactsFolder folderUpdate = new DefaultGroupwareContactsFolder(folder);
            folderUpdate.setExtendedProperties(null);
            // Update extended properties as needed; 'hide' the change in folder update afterwards
            folder = folderUpdate;
        }
        // Perform common folder update
        ParameterizedFolder storageFolder = getStorageFolder(TREE_ID, CONTENT_TYPE, folder, null, ACCOUNT_ID, null);
        getFolderService().updateFolder(storageFolder, new Date(clientTimestamp), session.getSession(), initDecorator());
        return storageFolder.getID();
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        getFolderService().deleteFolder(TREE_ID, folderId, new Date(clientTimestamp), session.getSession(), initDecorator());
    }

    @Override
    public GroupwareContactsFolder getDefaultFolder() throws OXException {
        UserizedFolder folder = getFolderService().getDefaultFolder(ServerSessionAdapter.valueOf(session.getSession()).getUser(), TREE_ID, CONTENT_TYPE, PrivateType.getInstance(), session.getSession(), initDecorator());
        return getContactsFolder(folder);
    }

    @Override
    public GroupwareContactsFolder getFolder(String folderId) throws OXException {
        return getContactsFolder(getFolderService().getFolder(TREE_ID, folderId, session.getSession(), initDecorator()));
    }

    @Override
    public List<GroupwareContactsFolder> getVisibleFolders(GroupwareFolderType type) throws OXException {
        switch (type) {
            case PRIVATE:
                return getContactFolders(getSubfoldersRecursively(getFolderService(), initDecorator(), PRIVATE_FOLDER_ID));
            case SHARED:
                return getContactFolders(getSubfoldersRecursively(getFolderService(), initDecorator(), SHARED_FOLDER_ID));
            case PUBLIC:
                return getContactFolders(getSubfoldersRecursively(getFolderService(), initDecorator(), PUBLIC_FOLDER_ID));
            default:
                throw ContactsProviderExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(Constants.PROVIDER_ID);
        }
    }

    @Override
    public List<ContactsFolder> getVisibleFolders() throws OXException {
        List<ContactsFolder> folders = new ArrayList<>();
        for (GroupwareFolderType type : GroupwareFolderType.values()) {
            folders.addAll(getVisibleFolders(type));
        }
        return folders;
    }

    @Override
    public Contact getContact(String folderId, String contactId) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return transferIds(getContactService().getContact(session.getSession(), folderId, contactId, getFields()));
    }

    @Override
    public List<Contact> getContacts(List<ContactID> contactIDs) throws OXException {
        decorateSessionWithReadOnlyConnection();
        Map<String, List<String>> ids = separateContactIdsPerFolder(contactIDs);
        List<Contact> contacts = new LinkedList<>();
        for (Entry<String, List<String>> entry : ids.entrySet()) {
            iterateContacts(getContactService().getContacts(session.getSession(), entry.getKey(), entry.getValue().toArray(new String[0]), getFields()), contacts);
        }
        return contacts;
    }

    @Override
    public List<Contact> getContacts(String folderId) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().getAllContacts(session.getSession(), folderId, getFields(), getSortOptions()));
    }

    @Override
    public List<Contact> getDeletedContacts(String folderId, Date from) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().getDeletedContacts(session.getSession(), folderId, from, getFields(), getSortOptions()));
    }

    @Override
    public List<Contact> getModifiedContacts(String folderId, Date from) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().getModifiedContacts(session.getSession(), folderId, from, getFields(), getSortOptions()));
    }

    @Override
    public boolean isFolderEmpty(String folderId) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return getContactService().isFolderEmpty(session.getSession(), folderId);
    }

    @Override
    public boolean containsForeignObjectInFolder(String folderId) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return getContactService().containsForeignObjectInFolder(session.getSession(), folderId);
    }

    @Override
    public List<Contact> getUserContacts(int[] userIds) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().getUsers(session.getSession(), userIds, getFields()));
    }

    @Override
    public List<Contact> getUserContacts() throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().getAllUsers(session.getSession(), getFields(), getSortOptions()));
    }

    @Override
    public List<Contact> searchUserContacts(ContactsSearchObject contactSearch) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().searchUsers(session.getSession(), convert(contactSearch), getFields(), getSortOptions()));
    }

    @Override
    public List<Contact> searchUserContacts(SearchTerm<?> searchTerm) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().searchUsers(session.getSession(), searchTerm, getFields(), getSortOptions()));
    }

    @Override
    public boolean supports(String folderId, ContactField... fields) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return getContactService().supports(session.getSession(), folderId, fields);
    }

    ////////////////////////////////// SEARCH ////////////////////////////////////

    @Override
    public List<Contact> searchContacts(ContactsSearchObject contactSearch) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().searchContacts(session.getSession(), convert(contactSearch), getFields(), getSortOptions()));
    }

    @Override
    public <O> List<Contact> searchContacts(List<String> folderIds, SearchTerm<O> term) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().searchContacts(session.getSession(), folderIds, term, getFields(), getSortOptions()));
    }

    @Override
    public List<Contact> autocompleteContacts(List<String> folderIds, String query) throws OXException {
        decorateSessionWithReadOnlyConnection();
        AutocompleteParameters parameters = AutocompleteParameters.newInstance();
        parameters.put(AutocompleteParameters.REQUIRE_EMAIL, session.get(ContactsParameters.PARAMETER_REQUIRE_EMAIL, Boolean.class));
        parameters.put(AutocompleteParameters.IGNORE_DISTRIBUTION_LISTS, session.get(ContactsParameters.PARAMETER_IGNORE_DISTRIBUTION_LISTS, Boolean.class));
        return iterateContacts(getContactService().autocompleteContacts(session.getSession(), folderIds, query, parameters, getFields(), getSortOptions()));
    }

    @Override
    public List<Contact> searchContactsWithBirthday(List<String> folderIds, Date from, Date until) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().searchContactsWithBirthday(session.getSession(), folderIds, from, until, getFields(), getSortOptions()));
    }

    @Override
    public List<Contact> searchContactsWithAnniversary(List<String> folderIds, Date from, Date until) throws OXException {
        decorateSessionWithReadOnlyConnection();
        return iterateContacts(getContactService().searchContactsWithAnniversary(session.getSession(), folderIds, from, until, getFields(), getSortOptions()));
    }

    ///////////////////////////////// CONTACTS PARAMETERS /////////////////////////////

    /**
     * Gets the {@link ContactsParameters#PARAMETER_ORDER_BY} parameter.
     *
     * @return The order by {@link ContactField}
     */
    private ContactField getOrderBy() {
        return session.get(ContactsParameters.PARAMETER_ORDER_BY, ContactField.class);
    }

    /**
     * Gets the {@link ContactsParameters#PARAMETER_ORDER} parameter. If absent falls back to the {@link Order#ASCENDING}
     *
     * @return The {@link Order}
     */
    private Order getOrder() {
        Order order = session.get(ContactsParameters.PARAMETER_ORDER, Order.class);
        if (order == null) {
            order = Order.ASCENDING;
        }
        return order;
    }

    /**
     * Gets the {@link ContactsParameters#PARAMETER_FIELDS} parameter. If absent falls back to the {@link ALL_CONTACT_FIELDS}
     *
     * @return The {@link ContactField}s array
     */
    private ContactField[] getFields() {
        ContactField[] fields = session.get(ContactsParameters.PARAMETER_FIELDS, ContactField[].class);
        if (fields == null) {
            fields = ALL_CONTACT_FIELDS;
        }
        return fields;
    }

    /**
     * Gets the {@link SortOptions} based on the orderBy and order or
     * on the sortOptions parameters.
     *
     * @return The {@link SortOptions}
     * @throws OXException if the leftHandLimit is greater than the rightHandLimit
     */
    private SortOptions getSortOptions() throws OXException {
        SortOrder order = SortOptions.Order(getOrderBy(), getOrder());
        SortOptions options = order == null ? new SortOptions() : new SortOptions(order);
        String collation = getCollation();
        if (Strings.isNotEmpty(collation)) {
            options.setCollation(collation);
        }

        Integer leftHandLimit = getLeftHandLimit();
        if (leftHandLimit.intValue() >= 0) {
            options.setRangeStart(i(leftHandLimit));
        }

        Integer rightHandLimit = getRightHandLimit();
        if (rightHandLimit.intValue() >= 0) {
            if (leftHandLimit.intValue() > rightHandLimit.intValue()) {
                throw ContactsProviderExceptionCodes.INVALID_RANGE_LIMITS.create();
            }
            options.setLimit(rightHandLimit.intValue() - leftHandLimit.intValue());
        }

        return options;
    }

    /**
     * Gets the {@link ContactsParameters#PARAMETER_COLLATION}
     *
     * @return the integer value for the start parameter, -1 if not present
     */
    private String getCollation() {
        return session.get(ContactsParameters.PARAMETER_COLLATION, String.class);
    }

    /**
     * Gets the {@link ContactsParameters#PARAMETER_LEFT_HAND_LIMIT}
     *
     * @return the integer value for the left-hand-limit parameter, -1 if not present
     */
    private Integer getLeftHandLimit() {
        return session.get(ContactsParameters.PARAMETER_LEFT_HAND_LIMIT, Integer.class, I(0));
    }

    /**
     * Gets the {@link ContactsParameters#PARAMETER_RIGHT_HAND_LIMIT}
     *
     * @return the integer value for the right-hand-limit parameter, -1 if not present
     */
    private Integer getRightHandLimit() {
        return session.get(ContactsParameters.PARAMETER_RIGHT_HAND_LIMIT, Integer.class, I(0));
    }

    /////////////////////////////// FOLDER STUFF //////////////////////////////////

    /**
     * Separates the specified contact ids per folder
     *
     * @param contactsIds The contact ids to separate
     * @return The separated contact ids per folder
     */
    private Map<String, List<String>> separateContactIdsPerFolder(List<ContactID> contactsIds) {
        Map<String, List<String>> ids = new LinkedHashMap<>();
        for (ContactID id : contactsIds) {
            List<String> cids = new LinkedList<>();
            cids.add(id.getObjectID());
            List<String> absent = ids.putIfAbsent(id.getFolderID(), cids);
            if (absent != null) {
                absent.add(id.getObjectID());
                ids.put(id.getFolderID(), absent);
            }
        }
        return ids;
    }

    /**
     * Gets a list of groupware contacts folders representing the folders in the supplied userized folders.
     *
     * @param folders The folders from the folder service
     * @return The groupware contacts folders
     * @throws OXException if an error is occurred
     */
    private List<GroupwareContactsFolder> getContactFolders(List<UserizedFolder> folders) throws OXException {
        if (null == folders || 0 == folders.size()) {
            return Collections.emptyList();
        }
        List<GroupwareContactsFolder> contactFolders = new ArrayList<>(folders.size());
        for (UserizedFolder userizedFolder : folders) {
            contactFolders.add(getContactsFolder(userizedFolder));
        }
        return sort(contactFolders, ((ServerSession) session.getSession()).getUser().getLocale());
    }

    /**
     * Gets the groupware contacts folder representing the userized folders in the supplied folder response.
     *
     * @param folderResponse The response from the folder service
     * @return The groupware contacts folder
     * @throws OXException if an error is occurred
     */
    private GroupwareContactsFolder getContactsFolder(UserizedFolder userizedFolder) throws OXException {
        DefaultGroupwareContactsFolder contactsFolder = ContactsFolderConverter.getContactsFolder(userizedFolder);
        Map<String, String> userProperties = loadUserProperties(session.getContextId(), userizedFolder.getID(), session.getUserId());
        contactsFolder.setExtendedProperties(getExtendedProperties(userProperties));

        if (userizedFolder.isDefault() && PrivateType.getInstance().equals(userizedFolder.getType())) {
            contactsFolder.setUsedForSync(UsedForSync.FORCED_ACTIVE);
        }

        return contactsFolder;
    }

    /**
     * Collects all contacts subfolders from a parent folder recursively.
     *
     * @param folderService A reference to the folder service
     * @param decorator The optional folder service decorator to use
     * @param parentId The parent folder identifier to get the subfolders from
     * @return The collected subfolders, or an empty list if there are none
     * @throws OXException if an error is occurred
     */
    private List<UserizedFolder> getSubfoldersRecursively(FolderService folderService, FolderServiceDecorator decorator, String parentId) throws OXException {
        UserizedFolder[] subfolders = folderService.getSubfolders(TREE_ID, parentId, true, session.getSession(), decorator).getResponse();
        if (null == subfolders || 0 == subfolders.length) {
            return Collections.emptyList();
        }
        List<UserizedFolder> allFolders = new ArrayList<>();
        for (UserizedFolder subfolder : subfolders) {
            if (CONTENT_TYPE.equals(subfolder.getContentType())) {
                allFolders.add(subfolder);
            }
            if (subfolder.hasSubscribedSubfolders()) {
                allFolders.addAll(getSubfoldersRecursively(folderService, decorator, subfolder.getID()));
            }
        }
        return allFolders;
    }

    /**
     * Creates and initializes a folder service decorator ready to use with calls to the underlying folder service.
     *
     * @return A new folder service decorator
     */
    private FolderServiceDecorator initDecorator() {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Connection connection = optConnection();
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.setLocale(((ServerSession) session.getSession()).getUser().getLocale());
        decorator.put("altNames", Boolean.TRUE.toString());
        decorator.setTimeZone(TimeZones.UTC);
        decorator.setAllowedContentTypes(Collections.<ContentType> singletonList(CONTENT_TYPE));
        return decorator;
    }

    /**
     * Sorts the specified list of contacts folders by name. The default folders will end up
     * in the first places.
     *
     * @param contactsFolders The contacts folders to sort
     * @param locale The locale
     * @return The sorted contacts folders
     */
    private List<GroupwareContactsFolder> sort(List<GroupwareContactsFolder> contactsFolders, Locale locale) {
        if (null == contactsFolders || 2 > contactsFolders.size()) {
            return contactsFolders;
        }
        Collator collator = Collators.getSecondaryInstance(locale);
        contactsFolders.sort((folder1, folder2) -> {
            if (folder1.isDefaultFolder() != folder2.isDefaultFolder()) {
                // Default folders first
                return folder1.isDefaultFolder() ? -1 : 1;
            }
            // Otherwise, compare folder names
            return collator.compare(folder1.getName(), folder2.getName());
        });
        return contactsFolders;
    }

    //////////////////////////////// EXTENDED PROPERTIES //////////////////////////////////////

    /**
     * Gets the extended contacts properties for a storage folder.
     *
     * @param folder The folder to get the extended contacts properties for
     * @return The extended properties
     */
    private ExtendedProperties getExtendedProperties(Map<String, String> userProperties) {
        ExtendedProperties properties = new ExtendedProperties();
        for (Entry<String, String> entry : userProperties.entrySet()) {
            properties.add(new ExtendedProperty(entry.getKey(), entry.getValue()));
        }
        return properties;
    }

    /**
     * Updates extended contacts properties of a groupware contacts folder.
     *
     * @param originalFolder The original folder being updated
     * @param properties The properties as passed by the client
     * @throws OXException if an error is occurred
     */
    private void updateProperties(GroupwareContactsFolder originalFolder, ExtendedProperties properties) throws OXException {
        ExtendedProperties originalProperties = originalFolder.getExtendedProperties();
        List<ExtendedProperty> propertiesToStore = new ArrayList<>();
        for (ExtendedProperty property : properties) {
            ExtendedProperty originalProperty = originalProperties.get(property.getName());
            if (null == originalProperty) {
                throw OXException.noPermissionForFolder();
            }
            if (originalProperty.equals(property)) {
                continue;
            }
            propertiesToStore.add(property);
        }
        if (0 == propertiesToStore.size()) {
            return;
        }
        Map<String, String> updatedProperties = new HashMap<>(propertiesToStore.size());
        Set<String> removedProperties = new HashSet<>();
        for (ExtendedProperty property : propertiesToStore) {
            String name = USER_PROPERTY_PREFIX + property.getName();
            if (null == property.getValue()) {
                removedProperties.add(name);
                continue;
            }
            if (false == String.class.isInstance(property.getValue())) {
                throw OXException.noPermissionForFolder();
            }
            updatedProperties.put(name, (String) property.getValue());
        }
        removeUserProperties(session.getContextId(), originalFolder.getId(), session.getUserId(), removedProperties);
        storeUserProperties(session.getContextId(), originalFolder.getId(), session.getUserId(), updatedProperties);
    }

    /**
     * Stores the user properties for the specified folder
     *
     * @param contextId The context identifier
     * @param folderId The folder identifier
     * @param userId The user identifier
     * @param properties The properties to store
     * @throws OXException if an error is occurred
     */
    private void storeUserProperties(int contextId, String folderId, int userId, Map<String, String> properties) throws OXException {
        if (null == properties || properties.isEmpty()) {
            return;
        }
        FolderUserPropertyStorage propertyStorage = requireService(FolderUserPropertyStorage.class, services);
        Connection connection = optConnection();
        if (null == connection) {
            propertyStorage.setFolderProperties(contextId, asInt(folderId), userId, properties);
        } else {
            propertyStorage.setFolderProperties(contextId, asInt(folderId), userId, properties, connection);
        }
    }

    /**
     * Removes the user properties for the specified folder
     *
     * @param contextId The context identifier
     * @param folderId The folder identifier
     * @param userId The user identifier
     * @param propertyNames The names of the properties that shall be removed
     * @throws OXException if an error is occurred
     */
    private void removeUserProperties(int contextId, String folderId, int userId, Set<String> propertyNames) throws OXException {
        if (null == propertyNames || propertyNames.isEmpty()) {
            return;
        }
        FolderUserPropertyStorage propertyStorage = requireService(FolderUserPropertyStorage.class, services);
        Connection connection = optConnection();
        if (null == connection) {
            propertyStorage.deleteFolderProperties(contextId, asInt(folderId), userId, propertyNames);
        } else {
            propertyStorage.deleteFolderProperties(contextId, asInt(folderId), userId, propertyNames, connection);
        }
    }

    /**
     * Loads the user properties for the specified folder
     *
     * @param contextId The context identifier
     * @param folderId The folder identifier
     * @param userId The user identifier
     * @return A {@link Map} with all user properties for the specified folder
     * @throws OXException if an error is occurred
     */
    private Map<String, String> loadUserProperties(int contextId, String folderId, int userId) throws OXException {
        FolderUserPropertyStorage propertyStorage = requireService(FolderUserPropertyStorage.class, services);
        Connection connection = optConnection();
        if (null == connection) {
            return propertyStorage.getFolderProperties(contextId, asInt(folderId), userId);
        }
        return propertyStorage.getFolderProperties(contextId, asInt(folderId), userId, connection);
    }

    /////////////////////////////// UTILITIES ////////////////////////////////

    /**
     * Iterates over the specified {@link SearchIterator} to compile
     * a {@link List} with {@link Contact}s
     *
     * @param iterator The {@link SearchIterator} to use
     * @return A {@link List} with the {@link Contact}s
     */
    private List<Contact> iterateContacts(SearchIterator<Contact> iterator) {
        return iterateContacts(iterator, new LinkedList<>());
    }

    /**
     * Iterates over the specified {@link SearchIterator} to compile
     * a {@link List} with {@link Contact}s
     *
     * @param iterator The {@link SearchIterator} to use
     * @param contacts The optional list to store the iterated contacts
     * @return A {@link List} with the {@link Contact}s
     */
    private List<Contact> iterateContacts(SearchIterator<Contact> iterator, List<Contact> contacts) {
        try {
            while (iterator.hasNext()) {
                contacts.add(transferIds(iterator.next()));
            }
        } catch (OXException e) {
            LOGGER.error("Could not retrieve contact from folder using a FolderIterator, exception was: ", e);
        } finally {
            SearchIterators.close(iterator);
        }
        return contacts;
    }

    /**
     * Decorates the {@link ServerSession} with an optional writeable database connection
     */
    private void decorateSessionWithWritableConnection() {
        session.getSession().setParameter(getParamWritableConnection(), optConnection());
    }

    /**
     * Decorates the {@link ServerSession} with an optional read-only database connection
     */
    private void decorateSessionWithReadOnlyConnection() {
        session.getSession().setParameter(getParamReadOnlyConnection(), optConnection());
    }

    /**
     * Optionally gets the {@link Connection} that was passed as a
     * {@link Session} parameter
     *
     * @return The {@link Connection} or <code>null</code>
     */
    private Connection optConnection() {
        return session.get(PARAMETER_CONNECTION(), Connection.class);
    }

    /**
     * Transfers the integer based identifiers of the supplied {@link Contact}
     * to their corresponding string fields
     *
     * @param contact The contact
     * @return the contact for chained calls
     */
    private Contact transferIds(Contact contact) {
        contact.setId(asString(contact.getObjectID()));
        contact.setFolderId(asString(contact.getParentFolderID()));
        return contact;
    }

    /**
     * Parses the supplied identifier to its numerical integer value.
     *
     * @param id The identifier to get the integer value for
     * @return The integer value of the supplied identifier.
     * @throws NumberFormatException
     */
    private int asInt(String id) {
        return Integer.parseInt(id);
    }

    /**
     * Parses the supplied identifier to its string value
     *
     * @param id The identifier to get the string value for
     * @return The string value of the supplied identifier.
     */
    private String asString(int id) {
        return Integer.toString(id);
    }

    /**
     * Converts the new {@link ContactsSearchObject} to its legacy counter-part {@link ContactSearchObject}
     *
     * @param contactSearch the object to convert
     * @return The converted object
     */
    private ContactSearchObject convert(ContactsSearchObject contactSearch) {
        ContactSearchObject cso = new ContactSearchObject();
        if (null != contactSearch.getFolders()) {
            for (String folderId : contactSearch.getFolders()) {
                try {
                    cso.addFolder(i(Integer.valueOf(IDMangler.getRelativeFolderId(folderId))));
                } catch (@SuppressWarnings("unused") OXException e) {
                    LOGGER.warn("Ignoring malformed folder id {}", folderId);
                }
            }
        }
        if (null != contactSearch.getExcludeFolders()) {
            for (String folderId : contactSearch.getExcludeFolders()) {
                try {
                    cso.addExcludeFolder(i(Integer.valueOf(IDMangler.getRelativeFolderId(folderId))));
                } catch (@SuppressWarnings("unused") OXException e) {
                    LOGGER.warn("Ignoring malformed excluded folder id {}", folderId);
                }
            }
        }
        cso.setPattern(contactSearch.getPattern());
        cso.setStartLetter(contactSearch.isStartLetter());
        cso.setEmailAutoComplete(contactSearch.isEmailAutoComplete());
        cso.setOrSearch(contactSearch.isOrSearch());
        cso.setExactMatch(contactSearch.isExactMatch());
        cso.setSurname(contactSearch.getSurname());
        cso.setDisplayName(contactSearch.getDisplayName());
        cso.setGivenName(contactSearch.getGivenName());
        cso.setCompany(contactSearch.getCompany());
        cso.setEmail1(contactSearch.getEmail1());
        cso.setEmail2(contactSearch.getEmail2());
        cso.setEmail3(contactSearch.getEmail3());
        cso.setCatgories(contactSearch.getCatgories());
        cso.setSubfolderSearch(contactSearch.isSubfolderSearch());
        return cso;
    }

    //////////////////////////////// SERVICES ///////////////////////////

    /**
     * Returns the {@link ContactService}
     *
     * @return the {@link ContactService}
     * @throws OXException if the service is absent
     */
    private ContactService getContactService() throws OXException {
        return services.getServiceSafe(ContactService.class);
    }

    /**
     * Returns the {@link FolderService}
     *
     * @return the {@link FolderService}
     * @throws OXException if the service is absent
     */
    private FolderService getFolderService() throws OXException {
        return services.getServiceSafe(FolderService.class);
    }

}
