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

package com.openexchange.contact.provider.composition.impl.idmangling;

import static com.openexchange.contact.ContactIDUtil.createContactID;
import static com.openexchange.groupware.contact.helpers.ContactField.getByAjaxName;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.common.AccountAwareContactsFolder;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.GroupwareContactsFolder;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.provider.basic.BasicContactsAccess;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.internal.operands.ConstantOperand;

/**
 * {@link IDMangler} - The account aware IDMangler for contact folder identifiers.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public final class IDMangler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IDMangler.class);

    /**
     * The AccountAwareIDMangler.java.
     */
    private static final int DEFAULT_ACCOUNT_ID = ContactsAccount.DEFAULT_ACCOUNT.getAccountId();

    /** The pattern to lookup folder place holders in contacts exception messages */
    private static final Pattern FOLDER_ARGUMENT_PATTERN = Pattern.compile("(?:\\[|,|)(?:[fF]older(?: | id |\\: )%(\\d)\\$[sd])(?:\\]|,| )");

    /** The fixed prefix used to quickly identify contacts folder identifiers. */
    public static final String CONTACTS_PREFIX = "con";

    // @formatter:off
    /** A set of fixed root folder identifiers excluded from ID mangling for the default account */
    protected static final Set<String> ROOT_FOLDER_IDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        null, // no parent
        "0",  // com.openexchange.folderstorage.FolderStorage.ROOT_ID
        "1",  // com.openexchange.folderstorage.FolderStorage.PRIVATE_ID
        "2",  // com.openexchange.folderstorage.FolderStorage.PUBLIC_ID
        "3",  // com.openexchange.folderstorage.FolderStorage.SHARED_ID
        "6"   // com.openexchange.folderstorage.FolderStorage.GLOBAL_ADDRESS_BOOK_ID
    )));
    // @formatter:on

    /** The prefix indicating a the virtual <i>shared</i> root (com.openexchange.groupware.container.FolderObject.SHARED_PREFIX) */
    protected static final String SHARED_PREFIX = "u:";

    private static final String DEFAULT_ACCOUNT_PREFIX = CONTACTS_PREFIX + "://" + DEFAULT_ACCOUNT_ID;

    /**
     * Initializes a new {@link AccountAwareIDMangler}.
     */
    public IDMangler() {
        super();
    }

    /**
     * Gets the relative representation of a specific unique composite folder identifier.
     * <p/>
     * {@link IDMangling#ROOT_FOLDER_IDS} are passed as-is implicitly, same goes for identifiers starting with {@link AccountAwareIDMangler#SHARED_PREFIX}.
     *
     * @param uniqueFolderId The unique composite folder identifier, e.g. <code>con://11/38</code>
     * @return The extracted relative folder identifier
     * @throws OXException {@link ContactsProviderExceptionCodes#UNSUPPORTED_FOLDER} if passed identifier can't be unmangled to its relative representation
     */
    public static String getRelativeFolderId(String uniqueFolderId) throws OXException {
        if (ROOT_FOLDER_IDS.contains(uniqueFolderId)) {
            return uniqueFolderId;
        }
        try {
            Integer.parseInt(uniqueFolderId);
            return uniqueFolderId;
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            //Ignore; carry on with unmangling
        }
        try {
            return unmangleFolderId(uniqueFolderId).get(2);
        } catch (IllegalArgumentException e) {
            throw ContactsProviderExceptionCodes.UNSUPPORTED_FOLDER.create(e, uniqueFolderId, null);
        }
    }

    /**
     * Gets the account-relative representation for the supplied contact with unique composite identifiers.
     *
     * @param contact The contact
     * @return The contact representation with relative identifiers
     */
    public static Contact withRelativeID(Contact contact) throws OXException {
        String newFolderId = getRelativeFolderId(getEffectiveFolderId(contact));
        return new IDManglingContact(contact, newFolderId);
    }

    /**
     * Gets the account-relative representation for the supplied contacts folder with unique composite identifiers.
     *
     * @param folder The contacts folder
     * @return The contacts folder representation with relative identifiers
     */
    public static ContactsFolder withRelativeID(ContactsFolder folder) throws OXException {
        String newId = getRelativeFolderId(folder.getId());
        if (GroupwareContactsFolder.class.isInstance(folder)) {
            GroupwareContactsFolder groupwareFolder = (GroupwareContactsFolder) folder;
            String newParentId = getRelativeFolderId(groupwareFolder.getParentId());
            return new IDManglingGroupwareContactsFolder(groupwareFolder, newId, newParentId);
        }
        return new IDManglingContactsFolder(folder, newId);
    }

    /**
     * Re-creates the specified {@link SearchTerm} with a relative folder id
     *
     * @param searchTerm The {@link SearchTerm} to re-create
     * @return The re-created {@link SearchTerm}
     */
    public static SearchTerm<?> withRelativeID(SearchTerm<?> searchTerm) throws OXException {
        return recreateTerm(searchTerm);
    }

    /**
     * Gets a contact equipped with unique composite identifiers representing a contact from a specific contacts account.
     *
     * @param contact The contact from the account, or <code>null</code> to pass through
     * @param accountId The identifier of the account
     * @return The contact representation with unique identifiers
     */
    public static Contact withUniqueID(Contact contact, int accountId) {
        if (null == contact) {
            return null;
        }
        if (DEFAULT_ACCOUNT_ID == accountId) {
            return contact;
        }

        String newFolderId = getUniqueFolderId(accountId, getEffectiveFolderId(contact));
        return new IDManglingContact(contact, newFolderId);
    }

    /**
     * Gets a list of contacts folders equipped with unique composite identifiers representing the supplied list of contacts folders from
     * a specific contacts account.
     *
     * @param folders The contacts folders from the account
     * @param account The contacts account
     * @return The contacts folder representations with unique identifiers
     */
    public static List<AccountAwareContactsFolder> withUniqueID(List<? extends ContactsFolder> folders, ContactsAccount account) {
        if (null == folders) {
            return null;
        }
        List<AccountAwareContactsFolder> foldersWithUniqueIDs = new ArrayList<>(folders.size());
        for (ContactsFolder folder : folders) {
            foldersWithUniqueIDs.add(withUniqueID(folder, account));
        }
        return foldersWithUniqueIDs;
    }

    /**
     * Gets a contacts folder equipped with unique composite identifiers representing a contacts folder from a specific contacts account.
     *
     * @param folders The contacts folder from the account
     * @param account The contacts account
     * @return The contacts folder representation with unique identifiers
     */
    public static AccountAwareContactsFolder withUniqueID(ContactsFolder folder, ContactsAccount account) {
        if (GroupwareContactsFolder.class.isInstance(folder)) {
            GroupwareContactsFolder groupwareFolder = (GroupwareContactsFolder) folder;
            String newId = getUniqueFolderId(account.getAccountId(), folder.getId(), true);
            String newParentId = getUniqueFolderId(account.getAccountId(), groupwareFolder.getParentId(), true);
            return new IDManglingContactsAccountAwareGroupwareFolder(groupwareFolder, account, newId, newParentId);
        }
        return new IDManglingContactsAccountAwareFolder(folder, account, getUniqueFolderId(account.getAccountId(), folder.getId()));
    }

    /**
     * Gets a map of contact results equipped with unique composite identifiers representing results from a specific contacts account.
     *
     * @param relativeResults The contacts from the account
     * @param accountId The identifier of the account
     * @return The contact representations with unique identifiers
     */
    public static List<Contact> withUniqueIDs(List<Contact> relativeResults, int accountId) {
        if (null == relativeResults || relativeResults.isEmpty()) {
            return relativeResults;
        }
        if (DEFAULT_ACCOUNT_ID == accountId) {
            return relativeResults;
        }
        List<Contact> contacts = new ArrayList<>(relativeResults.size());
        for (Contact contact : relativeResults) {
            contacts.add(new IDManglingContact(contact, getUniqueFolderId(accountId, getEffectiveFolderId(contact))));
        }
        return contacts;
    }

    /**
     * Adjusts an exception raised by a specific contacts account so that any referenced identifiers appear in their unique composite
     * representation.
     *
     * @param e The exception to adjust, or <code>null</code> to do nothing
     * @param accountId The identifier of the account
     * @return The possibly adjusted exception
     */
    public static OXException withUniqueIDs(OXException e, int accountId) {
        if (null == e || false == e.isPrefix("CON")) {
            return e;
        }
        return adjustFolderArguments(e, accountId);
    }

    /**
     * Gets the relative representation of a list of unique full contact identifier, mapped to their associated account identifier.
     *
     * @param uniqueFolderIds The unique composite folder identifiers, e.g. <code>con://11/38</code>
     * @return The relative folder identifiers, mapped to their associated contacts account identifier
     * @throws OXException {@link ContactsProviderExceptionCodes#UNSUPPORTED_FOLDER} if the account identifier can't be extracted from a passed composite identifier
     */
    public static Map<Integer, List<ContactID>> getRelativeIdsPerAccountId(List<ContactID> uniqueContactIDs) throws OXException {
        Map<Integer, List<ContactID>> idsPerAccountId = new HashMap<>();
        for (ContactID contactID : uniqueContactIDs) {
            Integer accountId = I(getAccountId(contactID.getFolderID()));
            ContactID relativeContactId = getRelativeId(contactID);
            com.openexchange.tools.arrays.Collections.put(idsPerAccountId, accountId, relativeContactId);
        }
        return idsPerAccountId;
    }

    /**
     * Gets the relative representation of a list of unique composite folder identifier, mapped to their associated account identifier.
     * <p/>
     * {@link IDMangler#ROOT_FOLDER_IDS} are passed as-is implicitly, mapped to the default account.
     *
     * @param uniqueFolderIds The unique composite folder identifiers, e.g. <code>con://11/38</code>
     * @param errorsPerFolderId A map to track possible errors that occurred when parsing the supplied identifiers
     * @return The relative folder identifiers, mapped to their associated contacts account identifier
     */
    public static Map<Integer, List<String>> getRelativeFolderIdsPerAccountId(List<String> uniqueFolderIds) {
        if (null == uniqueFolderIds || uniqueFolderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, List<String>> foldersPerAccountId = new HashMap<>(uniqueFolderIds.size());
        for (String uniqueFolderId : uniqueFolderIds) {
            try {
                List<String> unmangledId = unmangleFolderId(uniqueFolderId);
                Integer accountId = Integer.valueOf(unmangledId.get(1));
                String relativeFolderId = unmangledId.get(2);
                com.openexchange.tools.arrays.Collections.put(foldersPerAccountId, accountId, relativeFolderId);
            } catch (IllegalArgumentException e) {
                //Ignore; carry on with unmangling
                LOGGER.debug(ContactsProviderExceptionCodes.UNSUPPORTED_FOLDER.create(e, uniqueFolderId, null).getMessage());
                com.openexchange.tools.arrays.Collections.put(foldersPerAccountId, I(DEFAULT_ACCOUNT_ID), uniqueFolderId);
            }
        }
        return foldersPerAccountId;
    }

    /**
     * Gets the relative representation of a specific unique full contact identifier consisting of composite parts.
     *
     * @param uniqueId The unique full contact identifier
     * @return The relative full contact identifier
     */
    public static ContactID getRelativeId(ContactID uniqueContactID) throws OXException {
        if (null == uniqueContactID) {
            return uniqueContactID;
        }
        return createContactID(getRelativeFolderId(uniqueContactID.getFolderID()), uniqueContactID.getObjectID());
    }

    /**
     * Gets the fully qualified composite representation of a specific relative folder identifier.
     * <p/>
     * {@link IDMangler#ROOT_FOLDER_IDS} as well as identifiers starting with {@link IDMangler#SHARED_PREFIX} are passed as-is implicitly.
     *
     * @param accountId The identifier of the account the folder originates in
     * @param relativeFolderId The relative folder identifier
     * @return The unique folder identifier
     */
    public static String getUniqueFolderId(int accountId, String relativeFolderId) {
        return getUniqueFolderId(accountId, relativeFolderId, ContactsAccount.DEFAULT_ACCOUNT.getAccountId() == accountId);
    }

    /**
     * Gets the fully qualified composite representation of a specific relative folder identifier.
     * <p/>
     * {@link IDMangler#ROOT_FOLDER_IDS} as well as identifiers starting with {@link IDMangler#SHARED_PREFIX} are passed as-is implicitly,
     * in case a <i>groupware</i> contacts access is indicated.
     *
     * @param accountId The identifier of the account the folder originates in
     * @param relativeFolderId The relative folder identifier
     * @param groupwareAccess <code>true</code> if the identifier originates from a <i>groupware</i> contacts access, <code>false</code>, otherwise
     * @return The unique folder identifier
     */
    public static String getUniqueFolderId(int accountId, String relativeFolderId, boolean groupwareAccess) {
        if (groupwareAccess || ContactsAccount.DEFAULT_ACCOUNT.getAccountId() == accountId) {
            if (ROOT_FOLDER_IDS.contains(relativeFolderId) || relativeFolderId.startsWith(SHARED_PREFIX)) {
                return relativeFolderId;
            }
        } else if (null == relativeFolderId) {
            return mangleFolderId(accountId, BasicContactsAccess.FOLDER_ID);
        }
        return mangleFolderId(accountId, relativeFolderId);
    }

    /**
     * Gets the account identifier of a specific unique composite folder identifier.
     * <p/>
     * {@link IDMangler#ROOT_FOLDER_IDS} as well as identifiers starting with {@link IDMangler#SHARED_PREFIX} will always yield the
     * identifier of the default account.
     *
     * @param uniqueFolderId The unique composite folder identifier, e.g. <code>con://11/38</code>
     * @return The extracted account identifier
     * @throws OXException {@link ContactsProviderExceptionCodes#UNSUPPORTED_FOLDER} if the account identifier can't be extracted from the passed composite identifier
     */
    public static int getAccountId(String uniqueFolderId) throws OXException {
        if (ROOT_FOLDER_IDS.contains(uniqueFolderId) || uniqueFolderId.startsWith(SHARED_PREFIX) || uniqueFolderId.startsWith(DEFAULT_ACCOUNT_PREFIX)) {
            return DEFAULT_ACCOUNT_ID;
        }
        try {
            Integer.parseInt(uniqueFolderId);
            return DEFAULT_ACCOUNT_ID;
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            //Ignore; carry on with unmangling
        }
        try {
            return Integer.parseInt(unmangleFolderId(uniqueFolderId).get(1));
        } catch (IllegalArgumentException e) {
            throw ContactsProviderExceptionCodes.UNSUPPORTED_FOLDER.create(e, uniqueFolderId, null);
        }
    }

    ////////////////////////////////// INTERNAL HELPERS //////////////////////////////////

    /**
     * <i>Mangles</i> the supplied relative folder identifier, together with its corresponding account information.
     *
     * @param accountId The identifier of the account the folder originates in
     * @param relativeFolderId The relative folder identifier
     * @return The mangled folder identifier
     */
    private static String mangleFolderId(int accountId, String relativeFolderId) {
        if (DEFAULT_ACCOUNT_ID == accountId) {
            return relativeFolderId;
        }
        return com.openexchange.tools.id.IDMangler.mangle(CONTACTS_PREFIX, String.valueOf(accountId), relativeFolderId);
    }

    /**
     * <i>Unmangles</i> the supplied unique folder identifier into its distinct components.
     *
     * @param uniqueFolderId The unique composite folder identifier, e.g. <code>con://11/38</code>
     * @return The unmangled components of the folder identifier
     * @throws IllegalArgumentException If passed identifier can't be unmangled into its distinct components
     */
    private static List<String> unmangleFolderId(String uniqueFolderId) {
        if (null == uniqueFolderId || false == uniqueFolderId.startsWith(CONTACTS_PREFIX)) {
            throw new IllegalArgumentException(uniqueFolderId);
        }
        List<String> unmangled = com.openexchange.tools.id.IDMangler.unmangle(uniqueFolderId);
        if (null == unmangled || 3 > unmangled.size() || false == CONTACTS_PREFIX.equals(unmangled.get(0))) {
            throw new IllegalArgumentException(uniqueFolderId);
        }
        return unmangled;
    }

    /**
     * Adjusts the log arguments indicating a <code>folder</code> in an exception raised by a specific contacts account so that any
     * referenced folder identifiers appear in their unique composite representation.
     *
     * @param e The contacts exception to adjust
     * @param accountId The identifier of the account
     * @return The possibly adjusted exception
     */
    private static OXException adjustFolderArguments(OXException e, int accountId) {
        try {
            OXExceptionCode exceptionCode = e.getExceptionCode();
            Object[] logArgs = e.getLogArgs();
            if (null != logArgs && 0 < logArgs.length && null != exceptionCode && null != exceptionCode.getMessage()) {
                boolean adjusted = false;
                Matcher matcher = FOLDER_ARGUMENT_PATTERN.matcher(exceptionCode.getMessage());
                while (matcher.find()) {
                    int argumentIndex = Integer.parseInt(matcher.group(1));
                    if (0 < argumentIndex && argumentIndex <= logArgs.length && String.class.isInstance(logArgs[argumentIndex - 1])) {
                        logArgs[argumentIndex - 1] = getUniqueFolderId(accountId, (String) logArgs[argumentIndex - 1]);
                        adjusted = true;
                    }
                }
                if (adjusted) {
                    e.setLogMessage(exceptionCode.getMessage(), logArgs);
                }
            }
        } catch (Exception x) {
            LOGGER.warn("Unexpected error while attempting to replace exception log arguments for {}", e.getLogMessage(), x);
        }
        return e;
    }

    /**
     * Recreates the specified {@link SearchTerm} with the relative folder id
     *
     * @param term The {@link SearchTerm} to recreate
     * @return The recreated {@link SearchTerm}
     * @throws OXException if the search term is invalid
     */
    private static SearchTerm<?> recreateTerm(SearchTerm<?> term) throws OXException {
        if (SingleSearchTerm.class.isInstance(term)) {
            return recreateTerm((SingleSearchTerm) term);
        } else if (CompositeSearchTerm.class.isInstance(term)) {
            return recreateTerm((CompositeSearchTerm) term);
        } else {
            throw new IllegalArgumentException("Need either a 'SingleSearchTerm' or 'CompositeSearchTerm'.");
        }
    }

    /**
     * Recreates the specified {@link CompositeSearchTerm} with the relative folder id
     *
     * @param term The {@link SearchTerm} to recreate
     * @return The recreated {@link SearchTerm}
     * @throws OXException if the search term is invalid
     */
    private static CompositeSearchTerm recreateTerm(CompositeSearchTerm term) throws OXException {
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm((CompositeOperation) term.getOperation());
        for (SearchTerm<?> operand : term.getOperands()) {
            compositeTerm.addSearchTerm(recreateTerm(operand));
        }
        return compositeTerm;
    }

    /**
     * Recreates the specified {@link SingleSearchTerm} with the relative folder id
     *
     * @param term The {@link SearchTerm} to recreate
     * @return The recreated {@link SearchTerm}
     * @throws OXException if the search term is invalid
     */
    @SuppressWarnings("deprecation")
    private static SingleSearchTerm recreateTerm(SingleSearchTerm term) throws OXException {
        SingleSearchTerm newTerm = new SingleSearchTerm(term.getOperation());
        Operand<?>[] operands = term.getOperands();
        for (int i = 0; i < operands.length; i++) {
            if (Operand.Type.COLUMN != operands[i].getType()) {
                newTerm.addOperand(operands[i]);
                continue;
            }
            ContactField field = null;
            Object value = operands[i].getValue();
            if (null == value) {
                throw new IllegalArgumentException("column operand without value: " + operands[i]);
            } else if (ContactField.class.isInstance(value)) {
                field = (ContactField) value;
            } else {
                //TODO: This is basically for backwards compatibility until AJAX names are no longer used in search terms.
                field = getByAjaxName(value.toString());
            }
            if (false == containsFolderId(operands, i, field)) {
                newTerm.addOperand(operands[i]);
                continue;
            }
            newTerm.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
            String folderId = (String) operands[i + 1].getValue();
            i++;
            try {
                Integer.parseInt(folderId);
                newTerm.addOperand(new ConstantOperand<>(folderId));
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                newTerm.addOperand(new ConstantOperand<>(getRelativeFolderId(folderId)));
            }
        }
        return newTerm;
    }

    /**
     * Checks whether the specified {@link Operand}s contain the {@link ContactField#FOLDER_ID}
     * column as well as a value for that.
     *
     * @param operands The {@link Operand}s to check
     * @param i The array position
     * @param field The field
     * @return <code>true</code> if the operands contain the {@link ContactField#FOLDER_ID} and its value is not <code>null</code>;
     *         <code>false</code> otherwise
     */
    private static boolean containsFolderId(Operand<?>[] operands, int i, ContactField field) {
        return null != field && ContactField.FOLDER_ID.equals(field) && i + 1 < operands.length && null != operands[i + 1] && null != operands[i + 1].getValue();
    }

    /**
     * Gets the parent folder identifier of the supplied contact, first probing {@link Contact#getFolderId()}, then falling back to
     * {@link Contact#getParentFolderID()}.
     *
     * @param contact The contact to get the parent folder identifier for
     * @return The parent folder identifier, or <code>null</code> if not set
     */
    private static String getEffectiveFolderId(Contact contact) {
        if (contact.containsFolderId()) {
            return contact.getFolderId();
        }
        if (contact.containsParentFolderID()) {
            return String.valueOf(contact.getParentFolderID());
        }
        return null;
    }

}
