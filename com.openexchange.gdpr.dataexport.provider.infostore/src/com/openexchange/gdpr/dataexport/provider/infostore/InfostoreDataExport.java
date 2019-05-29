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

package com.openexchange.gdpr.dataexport.provider.infostore;

import static com.openexchange.gdpr.dataexport.DataExportProviders.getBoolOption;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isPermissionDenied;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isRetryableExceptionAndMayFail;
import static com.openexchange.gdpr.dataexport.provider.general.SavePointAndReason.savePointFor;
import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.TrashType;
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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.UserService;

/**
 * {@link InfostoreDataExport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class InfostoreDataExport extends AbstractDataExportProviderTask {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreDataExport.class);

    private static enum Root {
        PRIVATE(Integer.toString(FolderObject.SYSTEM_PRIVATE_FOLDER_ID)),
        PUBLIC(Integer.toString(FolderObject.SYSTEM_PUBLIC_FOLDER_ID)),
        SHARED(Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID)),
        TRASH(Integer.toString(FolderObject.TRASH));

        private final String id;

        private Root(String id) {
            this.id = id;
        }

        String getId() {
            return id;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    private static final String ID_INFOSTORE = "infostore";

    /**
     * The constant for InfoStore's file storage service.
     */
    private static final String SERVICE_INFOSTORE = "com.openexchange.infostore";

    /**
     * Initializes a new {@link InfostoreDataExport}.
     *
     * @param sink The sink to output to
     * @param savepoint The optional save-point previously set by this provider
     * @param task The data export task providing needed arguments
     * @param locale The locale
     * @param services The service look-up
     */
    public InfostoreDataExport(DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale, ServiceLookup services) {
        super(ID_INFOSTORE, sink, savepoint, task, locale, services);
    }

    /**
     * Exports files.
     *
     * @return The export result
     * @throws OXException If export fails
     */
    @Override
    public ExportResult export() throws OXException {
        FileStorageServiceRegistry fileStorageServiceRegistry = services.getServiceSafe(FileStorageServiceRegistry.class);

        try {
            Session session = new GeneratedSession(task.getUserId(), task.getContextId());

            Options options;
            {
                Module calendarModule = getModule();
                boolean includePublicFolders = getBoolOption(InfostoreDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, false, calendarModule);
                boolean includeSharedFolders = getBoolOption(InfostoreDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, false, calendarModule);
                boolean includeTrashFolder = getBoolOption(InfostoreDataExportPropertyNames.PROP_INCLUDE_TRASH, false, calendarModule);
                boolean includeAllVersions = getBoolOption(InfostoreDataExportPropertyNames.PROP_INCLUDE_ALL_VERSIONS, false, calendarModule);

                options = new Options(includePublicFolders, includeSharedFolders, includeTrashFolder, includeAllVersions);
            }

            StartInfo startInfo;
            if (savepoint.isPresent()) {
                JSONObject jSavePoint = savepoint.get();
                startInfo = new StartInfo(jSavePoint.optString("version", null), jSavePoint.optString("id", null), jSavePoint.getString("folder"), jSavePoint.getString("root"));
            } else {
                startInfo = null;
            }

            FileStorageService infostoreService = fileStorageServiceRegistry.getFileStorageService(SERVICE_INFOSTORE);
            FileStorageAccountAccess infostoreAccountAccess = infostoreService.getAccountAccess(ID_INFOSTORE, session);

            Locale locale = this.locale;
            StringHelper helper = StringHelper.valueOf(locale);

            tryTouch();

            // Private
            if (startInfo == null || Root.PRIVATE.getId().equals(startInfo.root)) {
                Folder folder = new Folder(Root.PRIVATE.getId(), helper.getString(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME), true);
                SavePointAndReason optSavePoint = traverseFolder(Root.PRIVATE, folder, null, startInfo, options, session, infostoreAccountAccess);
                if (optSavePoint != null) {
                    return optSavePoint.result();
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            // Shared
            if (options.includeSharedFolders && (startInfo == null || Root.SHARED.getId().equals(startInfo.root))) {
                Folder folder = new Folder(Root.SHARED.getId(), helper.getString(FolderStrings.SYSTEM_SHARED_FOLDER_NAME), true);
                SavePointAndReason optSavePoint = traverseFolder(Root.SHARED, folder, null, startInfo, options, session, infostoreAccountAccess);
                if (optSavePoint != null) {
                    return optSavePoint.result();
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            // Public
            if (options.includePublicFolders && (startInfo == null || Root.PUBLIC.getId().equals(startInfo.root))) {
                Folder folder = new Folder(Root.PUBLIC.getId(), helper.getString(FolderStrings.SYSTEM_PUBLIC_FOLDER_NAME), true);
                SavePointAndReason optSavePoint = traverseFolder(Root.PUBLIC, folder, null, startInfo, options, session, infostoreAccountAccess);
                if (optSavePoint != null) {
                    return optSavePoint.result();
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            // Trash
            if (options.includeTrashFolder && (startInfo == null || Root.TRASH.getId().equals(startInfo.root))) {
                Folder folder = new Folder(Root.TRASH.getId(), helper.getString(FolderStrings.SYSTEM_TRASH_FILES_FOLDER_NAME), true);
                SavePointAndReason optSavePoint = traverseFolder(Root.TRASH, folder, null, startInfo, options, session, infostoreAccountAccess);
                if (optSavePoint != null) {
                    return optSavePoint.result();
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

    private SavePointAndReason traverseFolder(Root root, Folder folder, String path, StartInfo startInfo, Options options, Session session, FileStorageAccountAccess infostoreAccountAccess) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            if (startInfo != null) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", startInfo.folderId).putSafe("root", root);
                if (startInfo.fileId != null) {
                    jSavePoint.putSafe("id", startInfo.fileId);
                }
                if (startInfo.version != null) {
                    jSavePoint.putSafe("id", startInfo.version);
                }
                return savePointFor(jSavePoint);
            }

            return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", root));
        }
        checkAborted();

        StartInfo info = startInfo;
        if (info == null || info.folderId.equals(folder.getFolderId())) {
            if (info == null) {
                if (!exportFolder(folder, path)) {
                    return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", root));
                }
                LOG.debug("Exported infostore directory {} for data export {} of user {} in context {}", folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
            }

            if (!folder.isRootFolder() && !folder.getFolderId().startsWith(SHARED_PREFIX)) {
                String newPath = (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
                SavePointAndReason jSavePoint = exportFiles(root, folder, newPath, info == null ? null : info.version, info == null ? null : info.fileId, options, infostoreAccountAccess);
                if (jSavePoint != null) {
                    return jSavePoint;
                }
            }

            info = null;
        }

        List<Folder> children;
        try {
            if (folder.isRootFolder()) {
                switch (root) {
                    case PRIVATE:
                        FileStorageFolder personalFolder = infostoreAccountAccess.getFolderAccess().getPersonalFolder();
                        children = Collections.singletonList(new Folder(personalFolder.getId(), personalFolder.getLocalizedName(locale), false));
                        break;
                    case PUBLIC:
                        FileStorageFolder[] publicFolders = infostoreAccountAccess.getFolderAccess().getPublicFolders();
                        children = new ArrayList<>(publicFolders.length);
                        for (FileStorageFolder publicFolder : publicFolders) {
                            children.add(new Folder(publicFolder.getId(), publicFolder.getLocalizedName(locale), false));
                        }
                        break;
                    case SHARED:
                        {
                            FolderService folderService = services.getServiceSafe(FolderService.class);
                            UserService userService = services.getServiceSafe(UserService.class);

                            FolderServiceDecorator decorator = new FolderServiceDecorator();
                            Object connection = session.getParameter(Connection.class.getName());
                            if (null != connection) {
                                decorator.put(Connection.class.getName(), connection);
                            }
                            decorator.put("altNames", Boolean.TRUE.toString());
                            decorator.setLocale(locale);

                            UserizedFolder[] subfolders = folderService.getSubfolders("1", "10", true, session, decorator).getResponse();
                            User user = userService.getUser(session.getUserId(), session.getContextId());
                            UserizedFolder persFolder = folderService.getDefaultFolder(user, "1", InfostoreContentType.getInstance(), PublicType.getInstance(), session, decorator);
                            UserizedFolder trashFolder = folderService.getDefaultFolder(user, "1", InfostoreContentType.getInstance(), TrashType.getInstance(), session, decorator);

                            Translator translator = services.getServiceSafe(TranslatorFactory.class).translatorFor(locale);

                            children = new ArrayList<>(subfolders.length);
                            for (UserizedFolder subfolder : subfolders) {
                                String id = subfolder.getID();
                                if (!id.equals(persFolder.getID()) && !id.equals(trashFolder.getID())) {
                                    FullNameBuilderService fullNameBuilder = services.getServiceSafe(FullNameBuilderService.class);
                                    String namePrefix = generateFullNamePrefix(fullNameBuilder.buildFullName(subfolder.getCreatedBy(), task.getContextId(), translator));
                                    children.add(new Folder(id, namePrefix + subfolder.getLocalizedName(locale), false));
                                }
                            }
                        }
                        break;
                    case TRASH:
                        FileStorageFolder trashFolder =infostoreAccountAccess.getFolderAccess().getTrashFolder();
                        children = Collections.singletonList(new Folder(trashFolder.getId(), trashFolder.getLocalizedName(locale), false));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknwn root: " + root.getId());
                }
            } else {
                FileStorageFolder[] subfolders = infostoreAccountAccess.getFolderAccess().getSubfolders(folder.getFolderId(), true);
                if (null == subfolders || subfolders.length <= 0) {
                    children = Collections.emptyList();
                } else {
                    children = new ArrayList<>(subfolders.length);
                    for (FileStorageFolder subfolder : subfolders) {
                        children.add(new Folder(subfolder.getId(), subfolder.getLocalizedName(locale), false));
                    }
                }
            }
        } catch (Exception e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                if (info != null) {
                    JSONObject jSavePoint = new JSONObject(4).putSafe("folder", info.folderId).putSafe("root", root);
                    if (info.fileId != null) {
                        jSavePoint.putSafe("id", info.fileId);
                    }
                    if (info.version != null) {
                        jSavePoint.putSafe("id", info.version);
                    }
                    return savePointFor(jSavePoint, e);
                }

                return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", root), e);
            }
            LOG.warn("Failed to retrieve subfolders of folder \"{}\" from user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
            sink.addToReport(Message.builder().appendToMessage("Failed to retrieve subfolders of folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_INFOSTORE).withTimeStamp(new Date()).build());
            children = Collections.emptyList();
        }

        if (!children.isEmpty()) {
            String newPath = (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
            for (Folder child : children) {
                SavePointAndReason jSavePoint = traverseFolder(root, child, newPath, info, options, session, infostoreAccountAccess);
                if (jSavePoint != null) {
                    return jSavePoint;
                }
            }
        }

        return null;
    }

    private static final List<Field> FIELDS = Arrays.asList(Field.ID, Field.NUMBER_OF_VERSIONS, Field.VERSION, Field.CURRENT_VERSION, Field.CAPTURE_DATE, Field.LAST_MODIFIED, Field.FILENAME);

    private static final Comparator<File> COMPARATOR = new Comparator<File>() {

        @Override
        public int compare(File o1, File o2) {
            String x = o1.getId();
            String y = o2.getId();
            return x.compareTo(y);
        }
    };

    private static final Comparator<File> COMPARATOR_VERSION = new Comparator<File>() {

        @Override
        public int compare(File o1, File o2) {
            String x = o1.getVersion();
            String y = o2.getVersion();
            return x.compareTo(y);
        }
    };

    private SavePointAndReason exportFiles(Root root, Folder folder, String path, String startVersion, String startFileId, Options options, FileStorageAccountAccess infostoreAccountAccess) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
            if (startFileId != null) {
                jSavePoint.putSafe("id", startFileId);
            }
            if (startVersion != null) {
                jSavePoint.putSafe("version", startVersion);
            }
            return savePointFor(jSavePoint);
        }
        checkAborted();

        FileStorageFileAccess fileAccess = infostoreAccountAccess.getFileAccess();

        Map<File, List<File>> fileToVersions;
        {
            SearchIterator<File> queriedFiles = null;
            try {
                queriedFiles = fileAccess.getDocuments(folder.getFolderId(), FIELDS, Field.FILENAME, SortDirection.ASC).results();
                if (!queriedFiles.hasNext()) {
                    // No files in given folder
                    return null;
                }

                if (isPauseRequested()) {
                    JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
                    if (startFileId != null) {
                        jSavePoint.putSafe("id", startFileId);
                    }
                    if (startVersion != null) {
                        jSavePoint.putSafe("version", startVersion);
                    }
                    return savePointFor(jSavePoint);
                }
                checkAborted();

                List<File> filesInCurrentVersion = SearchIterators.asList(queriedFiles);
                Collections.sort(filesInCurrentVersion, COMPARATOR);
                queriedFiles = null;

                fileToVersions = new LinkedHashMap<File, List<File>>(filesInCurrentVersion.size());
                if (options.includeAllVersions) {
                    if (FileStorageCapabilityTools.supports(fileAccess, FileStorageCapability.FILE_VERSIONS)) {
                        FileStorageVersionedFileAccess versionedFileAccess = ((FileStorageVersionedFileAccess) fileAccess);

                        for (File fileInCurrentVersion : filesInCurrentVersion) {
                            if (fileInCurrentVersion.getNumberOfVersions() > 1) {
                                SearchIterator<File> queriedVersions = versionedFileAccess.getVersions(folder.getFolderId(), fileInCurrentVersion.getId(), FIELDS, Field.FILENAME, SortDirection.ASC).results();
                                try {
                                    List<File> versions = SearchIterators.asList(queriedVersions);
                                    queriedVersions = null;

                                    // Keep only those items that reference a real file
                                    versions = versions.stream().filter(f -> f.getFileName() != null).collect(Collectors.toList());
                                    Collections.sort(versions, COMPARATOR_VERSION);

                                    fileToVersions.put(fileInCurrentVersion, versions);
                                } finally {
                                    SearchIterators.close(queriedVersions);
                                }
                            } else {
                                if (fileInCurrentVersion.getFileName() != null) {
                                    fileToVersions.put(fileInCurrentVersion, Collections.singletonList(fileInCurrentVersion));
                                }
                            }
                        }

                        if (isPauseRequested()) {
                            JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
                            if (startFileId != null) {
                                jSavePoint.putSafe("id", startFileId);
                            }
                            if (startVersion != null) {
                                jSavePoint.putSafe("version", startVersion);
                            }
                            return savePointFor(jSavePoint);
                        }
                        checkAborted();
                    } else {
                        LOG.warn("Failed to export all versions in folder \"{}\" from primary mail account of user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()));
                        sink.addToReport(Message.builder().appendToMessage("Failed to export all versions in folder \"").appendToMessage(folder.getName()).appendToMessage("\"").withModuleId(ID_INFOSTORE).withTimeStamp(new Date()).build());

                        // Keep only those items that reference a real file
                        filesInCurrentVersion = filesInCurrentVersion.stream().filter(f -> f.getFileName() != null).collect(Collectors.toList());
                        for (File fileInCurrentVersion : filesInCurrentVersion) {
                            fileToVersions.put(fileInCurrentVersion, Collections.singletonList(fileInCurrentVersion));
                        }
                    }
                } else {
                    // Keep only those items that reference a real file
                    filesInCurrentVersion = filesInCurrentVersion.stream().filter(f -> f.getFileName() != null).collect(Collectors.toList());
                    for (File fileInCurrentVersion : filesInCurrentVersion) {
                        fileToVersions.put(fileInCurrentVersion, Collections.singletonList(fileInCurrentVersion));
                    }
                }

                if (startFileId != null && startVersion != null) {
                    Map<File, List<File>> copy = new LinkedHashMap<File, List<File>>(fileToVersions);
                    boolean found = false;
                    for (Iterator<Map.Entry<File, List<File>>> it = fileToVersions.entrySet().iterator(); !found && it.hasNext();) {
                        Map.Entry<File, List<File>> entry = it.next();
                        if (!entry.getKey().getId().equals(startFileId)) {
                            it.remove();
                        } else {
                            List<File> versions = new ArrayList<File>(entry.getValue()); // copy versions
                            for (Iterator<File> versionsIter = versions.iterator(); !found && versionsIter.hasNext();) {
                                File version = versionsIter.next();
                                if (version.getVersion().equals(startVersion)) {
                                    found = true;
                                } else {
                                    versionsIter.remove();
                                }
                            }
                            if (found) {
                                entry.setValue(versions);
                            } else {
                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        fileToVersions = copy;
                    }
                }
            } catch (DataExportAbortedException e) {
                throw e;
            } catch (Exception e) {
                if (isRetryableExceptionAndMayFail(e, sink)) {
                    JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
                    if (startFileId != null) {
                        jSavePoint.putSafe("id", startFileId);
                    }
                    if (startVersion != null) {
                        jSavePoint.putSafe("version", startVersion);
                    }
                    return savePointFor(jSavePoint, e);
                }
                if (isPermissionDenied(e)) {
                    LOG.debug("Forbidden to export files from folder \"{}\" for user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                    sink.addToReport(Message.builder().appendToMessage("Insufficient permissions to export files from folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_INFOSTORE).withTimeStamp(new Date()).build());
                } else {
                    LOG.warn("Failed to export files from folder \"{}\" for user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                    sink.addToReport(Message.builder().appendToMessage("Failed to export files from folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_INFOSTORE).withTimeStamp(new Date()).build());
                }
                return null;
            } finally {
                SearchIterators.close(queriedFiles);
            }
        }

        int total = 0;
        for (List<File> versions : fileToVersions.values()) {
            total += versions.size();
        }
        LOG.debug("Going to export {} file versions from directory {} for data export {} of user {} in context {}", I(total), folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));

        int batchCount = 0;
        for (List<File> versions : fileToVersions.values()) {
            for (File version : versions) {
                if (isPauseRequested()) {
                    JSONObject jSavePoint = new JSONObject(4);
                    jSavePoint.putSafe("folder", folder.getFolderId());
                    jSavePoint.putSafe("root", root);
                    jSavePoint.putSafe("id", version.getId());
                    jSavePoint.putSafe("version", version.getVersion());
                    return savePointFor(jSavePoint);
                }
                int count = incrementAndGetCount();
                checkAborted((count % 10 == 0));
                if (count % 100 == 0) {
                    sink.setSavePoint(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("id", version.getId()).putSafe("version", version.getVersion()));
                }
                batchCount++;

                InputStream document = null;
                try {
                    document = fileAccess.getDocument(folder.getFolderId(), version.getId(), version.getVersion());

                    Date date = version.getCaptureDate();
                    if (null == date) {
                        date = version.getLastModified();
                    }

                    boolean exported = sink.export(document, new Item(path, sanitizeNameForZipEntry(version.getFileName()), date));
                    if (!exported) {
                        return savePointFor(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("id", version.getId()).putSafe("version", version.getVersion()));
                    }
                    LOG.debug("Exported version {} of file {} ({} of {}) from directory {} for data export {} of user {} in context {}", version.getVersion(), version.getId(), I(batchCount), I(total), folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
                } catch (Exception e) {
                    if (isRetryableExceptionAndMayFail(e, sink)) {
                        return savePointFor(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("id", version.getId()).putSafe("version", version.getVersion()), e);
                    }
                    LOG.warn("Failed to export version {} of file {} in folder \"{}\" from primary mail account of user {} in context {}", version.getVersion(), version.getId(), folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                    sink.addToReport(Message.builder().appendToMessage("Failed to export version \"").appendToMessage(version.getVersion()).appendToMessage("\" of file \"").appendToMessage(version.getId()).appendToMessage("\" in folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_INFOSTORE).withTimeStamp(new Date()).build());
                } finally {
                    Streams.close(document);
                }
            }
        }

        return null;
    }

    // ------------------------------------------------------------ Helpers -------------------------------------------------------------

    private static class Options {

        final boolean includePublicFolders;
        final boolean includeSharedFolders;
        final boolean includeTrashFolder;
        final boolean includeAllVersions;

        Options(boolean includePublicFolders, boolean includeSharedFolders, boolean includeTrashFolder, boolean includeAllVersions) {
            super();
            this.includeTrashFolder = includeTrashFolder;
            this.includeAllVersions = includeAllVersions;
            this.includePublicFolders = includePublicFolders;
            this.includeSharedFolders = includeSharedFolders;
        }
    }

    private static class StartInfo {

        final String root;
        final String folderId;
        final String fileId;
        final String version;

        StartInfo(String version, String fileId, String folderId, String root) {
            super();
            this.root = root;
            this.version = version;
            this.fileId = fileId;
            this.folderId = folderId;
        }
    }

}
