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

package com.openexchange.gdpr.dataexport.provider.contacts;

import static com.openexchange.gdpr.dataexport.DataExportProviders.getBoolOption;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isPermissionDenied;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isRetryableExceptionAndMayFail;
import static com.openexchange.gdpr.dataexport.provider.general.AttachmentLoader.PROPERTY_BINARY_ATTACHMENTS;
import static com.openexchange.gdpr.dataexport.provider.general.AttachmentLoader.loadAttachmentBinaries;
import static com.openexchange.gdpr.dataexport.provider.general.SavePointAndReason.savePointFor;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.contact.vcard.VCardService;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalculatePermission;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.gdpr.dataexport.DataExportAbortedException;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportSink;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.ExportResult;
import com.openexchange.gdpr.dataexport.GeneratedSession;
import com.openexchange.gdpr.dataexport.Item;
import com.openexchange.gdpr.dataexport.Message;
import com.openexchange.gdpr.dataexport.Module;
import com.openexchange.gdpr.dataexport.provider.general.AbstractDataExportProviderTask;
import com.openexchange.gdpr.dataexport.provider.general.Folder;
import com.openexchange.gdpr.dataexport.provider.general.SavePointAndReason;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link ContactsDataExport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ContactsDataExport extends AbstractDataExportProviderTask {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactsDataExport.class);

    private static final String GLOBAL_ADDRESS_BOOK_ID = Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID);
    private static final String PRIVATE_ID = Integer.toString(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
    private static final String PUBLIC_ID = Integer.toString(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
    private static final String SHARED_ID = Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID);
    private static final String ID_CONTACTS = "contacts";

    private StartInfo startInfo;

    /**
     * Initializes a new {@link ContactsDataExport}.
     *
     * @param sink The sink to output to
     * @param savepoint The optional save-point previously set by this provider
     * @param task The data export task providing needed arguments
     * @param locale The locale
     * @param services The service look-up
     */
    public ContactsDataExport(DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale, ServiceLookup services) {
        super(ID_CONTACTS, sink, savepoint, task, locale, services);
    }

    /**
     * Exports contacts.
     *
     * @return The export result
     * @throws OXException If export fails
     */
    @Override
    public ExportResult export() throws OXException {
        ContactService contactService = services.getServiceSafe(ContactService.class);
        VCardService vacrdService = services.getServiceSafe(VCardService.class);
        Optional<VCardStorageService> optionalVCardStorageService = Optional.ofNullable(services.getOptionalService(VCardStorageService.class));
        FolderService folderService = services.getServiceSafe(FolderService.class);
        UserService userService = services.getServiceSafe(UserService.class);
        ContextService contextService = services.getServiceSafe(ContextService.class);

        try {
            Session session = new GeneratedSession(task.getUserId(), task.getContextId());

            NeededServices neededServices = new NeededServices(folderService, contactService, vacrdService, optionalVCardStorageService);

            Options options;
            {
                Module contactsModule = getModule();
                boolean includePublicFolders = getBoolOption(ContactsDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, false, contactsModule);
                boolean includeSharedFolders = getBoolOption(ContactsDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, false, contactsModule);
                options = new Options(includePublicFolders, includeSharedFolders);
            }

            if (savepoint.isPresent()) {
                JSONObject jSavePoint = savepoint.get();
                int contactId = jSavePoint.optInt("id", 0);
                startInfo = new StartInfo(contactId != 0 ? I(contactId) : null, jSavePoint.getString("folder"), jSavePoint.getString("root"));
            } else {
                startInfo = null;
            }

            Context context = contextService.getContext(task.getContextId());
            User user = userService.getUser(task.getUserId(), task.getContextId());
            TimeZone timeZone = TimeZoneUtils.getTimeZone(user.getTimeZone());
            ContentType contentType = folderService.parseContentType(ID_CONTACTS);
            DecoratorProvider decoratorProvider = new DecoratorProvider(contentType, locale, timeZone);

            Locale locale = this.locale;
            StringHelper helper = StringHelper.valueOf(locale);

            boolean hasContact = services.getServiceSafe(UserConfigurationService.class).getUserConfiguration(session).hasContact();

            tryTouch();

            // Global address book
            if (startInfo == null || GLOBAL_ADDRESS_BOOK_ID.equals(startInfo.root)) {
                UserizedFolder globalAddressBook;
                if (hasContact) {
                    try {
                        globalAddressBook = folderService.getFolder(FolderStorage.REAL_TREE_ID, GLOBAL_ADDRESS_BOOK_ID, session, decoratorProvider.createDecorator());
                    } catch (Exception e) {
                        if (isRetryableExceptionAndMayFail(e, sink)) {
                            return ExportResult.incomplete(savepoint, Optional.of(e));
                        }
                        LOG.warn("Failed to load global address book folder", e);
                        globalAddressBook = null;
                    }
                } else {
                    globalAddressBook = null;
                }

                boolean exportSingleUserContact = true;
                if (globalAddressBook != null) {
                    Permission permission = CalculatePermission.calculate(globalAddressBook, user, context, Collections.singletonList(contentType));
                    if (permission.isVisible() && permission.getReadPermission() >= Permission.READ_ALL_OBJECTS) {
                        exportSingleUserContact = false;
                        String name = helper.getString(FolderStrings.SYSTEM_LDAP_FOLDER_NAME);
                        Folder folder = new Folder(Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID), name, false);
                        if (!exportFolder(folder, null)) {
                            return ExportResult.incomplete(Optional.empty(), Optional.empty());
                        }

                        SavePointAndReason optSavePoint = exportContacts(GLOBAL_ADDRESS_BOOK_ID, folder, name + "/", null, session, neededServices);
                        if (optSavePoint != null) {
                            return optSavePoint.result();
                        }
                    }
                }

                if (exportSingleUserContact) {
                    Contact userContact = contactService.getUser(session, session.getUserId());

                    String name = helper.getString(FolderStrings.SYSTEM_LDAP_FOLDER_NAME);
                    Folder folder = new Folder(Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID), name, false);
                    if (!exportFolder(folder, null)) {
                        return ExportResult.incomplete(Optional.empty(), Optional.empty());
                    }

                    if (!exportSingleContact(userContact, folder, name + "/", session, neededServices)) {
                        return ExportResult.incomplete(Optional.of(new JSONObject(4).putSafe("folder", GLOBAL_ADDRESS_BOOK_ID).putSafe("root", GLOBAL_ADDRESS_BOOK_ID)), Optional.empty());
                    }
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            // Private
            if (startInfo == null || PRIVATE_ID.equals(startInfo.root)) {
                if (hasContact) {
                    Folder folder = new Folder(PRIVATE_ID, helper.getString(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME), true);
                    SavePointAndReason optSavePoint = traverseFolder(PRIVATE_ID, PrivateType.getInstance(), folder, null, options, decoratorProvider, session, neededServices);
                    if (optSavePoint != null) {
                        return optSavePoint.result();
                    }
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            // Shared
            if (options.includeSharedFolders && (startInfo == null || SHARED_ID.equals(startInfo.root))) {
                if (hasContact) {
                    Folder folder = new Folder(SHARED_ID, helper.getString(FolderStrings.SYSTEM_SHARED_FOLDER_NAME), true);
                    SavePointAndReason optSavePoint = traverseFolder(SHARED_ID, SharedType.getInstance(), folder, null, options, decoratorProvider, session, neededServices);
                    if (optSavePoint != null) {
                        return optSavePoint.result();
                    }
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            // Public
            if (options.includePublicFolders && (startInfo == null || PUBLIC_ID.equals(startInfo.root))) {
                if (hasContact) {
                    Folder folder = new Folder(PUBLIC_ID, helper.getString(FolderStrings.SYSTEM_PUBLIC_FOLDER_NAME), true);
                    SavePointAndReason optSavePoint = traverseFolder(PUBLIC_ID, PublicType.getInstance(), folder, null, options, decoratorProvider, session, neededServices);
                    if (optSavePoint != null) {
                        return optSavePoint.result();
                    }
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            tryTouch();
            return ExportResult.completed();
        } catch (OXException e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                return ExportResult.incomplete(savepoint, Optional.of(e));
            }
            throw e;
        } catch (JSONException e) {
            throw DataExportExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DataExportAbortedException e) {
            return ExportResult.aborted();
        } catch (Exception e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                return ExportResult.incomplete(savepoint, Optional.of(e));
            }
            throw OXException.general(e.getMessage(), e);
        }
    }

    private SavePointAndReason traverseFolder(String root, Type type, Folder folder, String path, Options options, DecoratorProvider decoratorProvider, Session session, NeededServices neededServices) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            if (startInfo != null) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", startInfo.folderId).putSafe("root", root);
                if (startInfo.contactId != null) {
                    jSavePoint.putSafe("id", startInfo.contactId);
                }
                return savePointFor(jSavePoint);
            }

            return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", root));
        }
        checkAborted();

        if (startInfo == null || startInfo.folderId.equals(folder.getFolderId())) {
            if (startInfo == null) {
                if (!exportFolder(folder, path)) {
                    return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", root));
                }
                LOG.debug("Exported contact directory {} for data export {} of user {} in context {}", folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
            }

            if (!folder.isRootFolder() && !folder.getFolderId().startsWith(SHARED_PREFIX)) {
                String newPath = (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
                SavePointAndReason jSavePoint = exportContacts(root, folder, newPath, startInfo == null ? null : startInfo.contactId, session, neededServices);
                if (jSavePoint != null) {
                    return jSavePoint;
                }
            }

            startInfo = null;
        }

        if (folder.isRootFolder()) {
            List<Folder> allVisible;
            try {
                FolderService folderService = neededServices.folderService;
                FolderResponse<UserizedFolder[]> visibleFoldersResponse = folderService.getVisibleFolders(folder.getFolderId(), FolderStorage.REAL_TREE_ID, decoratorProvider.getContentType(), type, true, session, decoratorProvider.createDecorator());
                UserizedFolder[] visibleFolders = visibleFoldersResponse.getResponse();
                if (null == visibleFolders || visibleFolders.length <= 0) {
                    allVisible = Collections.emptyList();
                } else {
                    Translator translator = null;
                    if (SharedType.getInstance().equals(type)) {
                        translator = services.getServiceSafe(TranslatorFactory.class).translatorFor(locale);
                    }

                    allVisible = new ArrayList<>(visibleFolders.length);
                    for (UserizedFolder subfolder : visibleFolders) {
                        if (!GLOBAL_ADDRESS_BOOK_ID.equals(subfolder.getID())) {
                            String namePrefix = null;
                            if (translator != null) {
                                FullNameBuilderService fullNameBuilder = services.getServiceSafe(FullNameBuilderService.class);
                                namePrefix = generateFullNamePrefix(fullNameBuilder.buildFullName(subfolder.getCreatedBy(), task.getContextId(), translator));
                            }
                            allVisible.add(new Folder(subfolder.getID(), namePrefix == null ? subfolder.getLocalizedName(locale) : namePrefix + subfolder.getLocalizedName(locale), false));
                        }
                    }
                }
            } catch (Exception e) {
                if (isRetryableExceptionAndMayFail(e, sink)) {
                    if (startInfo != null) {
                        JSONObject jSavePoint = new JSONObject(4).putSafe("folder", startInfo.folderId).putSafe("root", root);
                        if (startInfo.contactId != null) {
                            jSavePoint.putSafe("id", startInfo.contactId);
                        }
                        return savePointFor(jSavePoint, e);
                    }

                    return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", root), e);
                }
                LOG.warn("Failed to retrieve subfolders of folder \"{}\" from user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to retrieve subfolders of folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CONTACTS).withTimeStamp(new Date()).build());
                allVisible = Collections.emptyList();
            }

            if (!allVisible.isEmpty()) {
                String newPath = (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
                for (Folder visibleFolder : allVisible) {
                    SavePointAndReason jSavePoint = traverseFolder(root, type, visibleFolder, newPath, options, decoratorProvider, session, neededServices);
                    if (jSavePoint != null) {
                        return jSavePoint;
                    }
                }
            }
        }

        return null;
    }

    private static final ContactField[] FIELDS_ID = new ContactField[] { ContactField.OBJECT_ID, ContactField.MARK_AS_DISTRIBUTIONLIST, ContactField.VCARD_ID };

    private static final Comparator<Contact> COMPARATOR = new Comparator<Contact>() {

        @Override
        public int compare(Contact o1, Contact o2) {
            int x = o1.getObjectID();
            int y = o2.getObjectID();
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };

    private SavePointAndReason exportContacts(String root, Folder folder, String path, Integer startContactId, Session session, NeededServices neededServices) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            JSONObject jSavePoint = new JSONObject(4);
            jSavePoint.putSafe("folder", folder.getFolderId());
            jSavePoint.putSafe("root", root);
            if (startContactId != null) {
                jSavePoint.putSafe("id", startContactId);
            }
            return savePointFor(jSavePoint);
        }
        checkAborted();

        ContactService contactService = neededServices.contactService;
        VCardService vcardService = neededServices.vcardService;
        Optional<VCardStorageService> optionalVCardStorageService = neededServices.optionalVCardStorageService;

        List<Contact> contacts;
        SearchIterator<Contact> queriedContacts = null;
        try {
            queriedContacts = contactService.getAllContacts(session, folder.getFolderId(), FIELDS_ID, SortOptions.EMPTY);
            if (!queriedContacts.hasNext()) {
                // No contacts in given folder
                return null;
            }

            if (isPauseRequested()) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
                if (startContactId != null) {
                    jSavePoint.putSafe("id", startContactId);
                }
                return savePointFor(jSavePoint);
            }
            checkAborted();

            contacts = SearchIterators.asList(queriedContacts);
            Collections.sort(contacts, COMPARATOR);

            if (startContactId != null) {
                boolean found = false;
                int index = 0;
                while (!found && index < contacts.size()) {
                    Contact c = contacts.get(index);
                    if (c.getObjectID() == startContactId.intValue()) {
                        found = true;
                    } else {
                        index++;
                    }
                }

                if (found && index > 0) {
                    contacts = contacts.subList(index, contacts.size());
                }
            }

            LOG.debug("Going to export {} contacts from directory {} for data export {} of user {} in context {}", I(contacts.size()), folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
        } catch (DataExportAbortedException e) {
            throw e;
        } catch (Exception e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
                if (startContactId != null) {
                    jSavePoint.putSafe("id", startContactId);
                }
                return savePointFor(jSavePoint, e);
            }
            if (isPermissionDenied(e)) {
                LOG.debug("Forbidden to export contacts from folder \"{}\" for user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Insufficient permissions to export contacts from folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CONTACTS).withTimeStamp(new Date()).build());
            } else {
                LOG.warn("Failed to export contacts from folder \"{}\" for user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to export contacts from folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CONTACTS).withTimeStamp(new Date()).build());
            }
            return null;
        } finally {
            SearchIterators.close(queriedContacts);
        }

        int batchCount = 0;
        for (Contact contact : contacts) {
            int contactId = contact.getObjectID();

            if (isPauseRequested()) {
                JSONObject jSavePoint = new JSONObject(4);
                jSavePoint.putSafe("folder", folder.getFolderId());
                jSavePoint.putSafe("root", root);
                jSavePoint.putSafe("id", I(contactId));
                return savePointFor(jSavePoint);
            }
            int count = incrementAndGetCount();
            checkAborted((count % 10 == 0));
            if (count % 100 == 0) {
                sink.setSavePoint(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("id", L(contactId)));
            }
            batchCount++;

            List<IFileHolder> attachmentBinaries = null;
            InputStream originalVCard = null;
            VCardExport vCardExport = null;
            try {
                // Query full contact
                Contact c = contactService.getContact(session, folder.getFolderId(), Integer.toString(contactId));
                if (c != null) {                    
                    if (c.getNumberOfAttachments() > 0) {
                        attachmentBinaries = loadContactAttachmentBinariesElseNull(contactId, folder, session);
                    }
                    
                    if (optionalVCardStorageService.isPresent() && c.getVCardId() != null) {
                        originalVCard = optionalVCardStorageService.get().getVCard(c.getVCardId(), session.getContextId());
                    }
                    
                    if (attachmentBinaries != null) {
                        c.setProperty(PROPERTY_BINARY_ATTACHMENTS, attachmentBinaries);
                    }
                    
                    vCardExport = vcardService.exportContact(c, originalVCard, null);
                    Streams.close(originalVCard);
                    originalVCard = null;
                    
                    boolean exported = sink.export(vCardExport.getClosingStream(), new Item(path, contactId + ".vcf", null));
                    if (!exported) {
                        return savePointFor(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("id", L(contactId)));
                    }
                    LOG.debug("Exported contact {} ({} of {}) from directory {} for data export {} of user {} in context {}", I(contactId), I(batchCount), I(contacts.size()), folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
                }
            } catch (Exception e) {
                if (isRetryableExceptionAndMayFail(e, sink)) {
                    return savePointFor(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("id", L(contactId)), e);
                }
                LOG.warn("Failed to export contact {} in folder \"{}\" from primary mail account of user {} in context {}", I(contactId), folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to export contact \"").appendToMessage(contactId).appendToMessage("\" in folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CONTACTS).withTimeStamp(new Date()).build());
            } finally {
                Streams.close(originalVCard, vCardExport);
                Streams.close(attachmentBinaries);
            }
        }

        return null;
    }

    private boolean exportSingleContact(Contact c, Folder folder, String path, Session session, NeededServices neededServices) throws OXException {
        VCardService vcardService = neededServices.vcardService;
        Optional<VCardStorageService> optionalVCardStorageService = neededServices.optionalVCardStorageService;
        int contactId = c.getObjectID();

        List<IFileHolder> attachmentBinaries = null;
        InputStream originalVCard = null;
        VCardExport vCardExport = null;
        try {
            if (c.getNumberOfAttachments() > 0) {
                attachmentBinaries = loadContactAttachmentBinariesElseNull(contactId, folder, session);
            }

            if (optionalVCardStorageService.isPresent() && c.getVCardId() != null) {
                originalVCard = optionalVCardStorageService.get().getVCard(c.getVCardId(), session.getContextId());
            }

            if (attachmentBinaries != null) {
                c.setProperty(PROPERTY_BINARY_ATTACHMENTS, attachmentBinaries);
            }

            vCardExport = vcardService.exportContact(c, originalVCard, null);
            Streams.close(originalVCard);
            originalVCard = null;

            boolean exported = sink.export(vCardExport.getClosingStream(), new Item(path, contactId + ".vcf", null));
            if (!exported) {
                return false;
            }
            LOG.debug("Exported contact {} from directory {} for data export {} of user {} in context {}", I(contactId), folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
        } catch (Exception e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                return false;
            }
            LOG.warn("Failed to export contact {} in folder \"{}\" from primary mail account of user {} in context {}", I(contactId), folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
            sink.addToReport(Message.builder().appendToMessage("Failed to export message \"").appendToMessage(contactId).appendToMessage("\" in folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CONTACTS).withTimeStamp(new Date()).build());
        } finally {
            Streams.close(originalVCard, vCardExport);
            Streams.close(attachmentBinaries);
        }

        return true;
    }

    // ------------------------------------------------------ Attachment stuff -------------------------------------------------------------

    private static final int MODULE_ID = com.openexchange.groupware.Types.CONTACT;

    private List<IFileHolder> loadContactAttachmentBinariesElseNull(int contactId, Folder folder, Session session) throws OXException {
        int folderId = Strings.getUnsignedInt(folder.getFolderId());
        if (folderId <= 0) {
            return null;
        }

        Optional<List<IFileHolder>> optionalAttachments = loadAttachmentBinaries(contactId, MODULE_ID, folderId, session);
        return optionalAttachments.isPresent() ? optionalAttachments.get() : null;
    }

    // ------------------------------------------------------------ Helpers -------------------------------------------------------------

    private static class DecoratorProvider {

        private final Locale locale;
        private final TimeZone timeZone;
        private final List<ContentType> allowedContentTypes;
        private final ContentType contentType;

        DecoratorProvider(ContentType contentType, Locale locale, TimeZone timeZone) {
            super();
            this.contentType = contentType;
            this.locale = locale;
            this.timeZone = timeZone;
            this.allowedContentTypes = Collections.singletonList(contentType);
        }

        ContentType getContentType() {
            return contentType;
        }

        FolderServiceDecorator createDecorator() {
            return new FolderServiceDecorator().setLocale(locale).setTimeZone(timeZone).setAllowedContentTypes(allowedContentTypes).put("suppressUnifiedMail", Boolean.TRUE);
        }
    }

    private static class Options {

        final boolean includePublicFolders;
        final boolean includeSharedFolders;

        Options(boolean includePublicFolders, boolean includeSharedFolders) {
            super();
            this.includePublicFolders = includePublicFolders;
            this.includeSharedFolders = includeSharedFolders;
        }
    }

    private static class StartInfo {

        final String root;
        final String folderId;
        final Integer contactId;

        StartInfo(Integer contactId, String folderId, String root) {
            super();
            this.root = root;
            this.contactId = contactId;
            this.folderId = folderId;
        }
    }

    private static class NeededServices {

        final ContactService contactService;
        final VCardService vcardService;
        final FolderService folderService;
        final Optional<VCardStorageService> optionalVCardStorageService;

        NeededServices(FolderService folderService, ContactService contactService, VCardService vcardService, Optional<VCardStorageService> optionalVCardStorageService) {
            super();
            this.contactService = contactService;
            this.vcardService = vcardService;
            this.folderService = folderService;
            this.optionalVCardStorageService = optionalVCardStorageService;
        }
    }

}
