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

package com.openexchange.gdpr.dataexport.provider.tasks;

import static com.openexchange.gdpr.dataexport.DataExportProviders.getBoolOption;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isPermissionDenied;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isRetryableExceptionAndMayFail;
import static com.openexchange.gdpr.dataexport.provider.general.AttachmentLoader.PROPERTY_BINARY_ATTACHMENTS;
import static com.openexchange.gdpr.dataexport.provider.general.AttachmentLoader.loadAttachmentBinaries;
import static com.openexchange.gdpr.dataexport.provider.general.SavePointAndReason.savePointFor;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Strings.parsePositiveInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.context.ContextService;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.gdpr.dataexport.DataExportAbortedException;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportSink;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.Directory;
import com.openexchange.gdpr.dataexport.ExportResult;
import com.openexchange.gdpr.dataexport.GeneratedSession;
import com.openexchange.gdpr.dataexport.Item;
import com.openexchange.gdpr.dataexport.Message;
import com.openexchange.gdpr.dataexport.Module;
import com.openexchange.gdpr.dataexport.provider.general.AbstractDataExportProviderTask;
import com.openexchange.gdpr.dataexport.provider.general.SavePointAndReason;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.BlockingAtomicReference;
import com.openexchange.java.ExceptionAwarePipedInputStream;
import com.openexchange.java.ExceptionForwardingPipedOutputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link TaskDataExport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class TaskDataExport extends AbstractDataExportProviderTask {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskDataExport.class);

    private static final int PRIVATE_ID = FolderObject.SYSTEM_PRIVATE_FOLDER_ID;
    private static final int PUBLIC_ID = FolderObject.SYSTEM_PUBLIC_FOLDER_ID;
    private static final int SHARED_ID = FolderObject.SYSTEM_SHARED_FOLDER_ID;
    private static final String ID_TASKS = "tasks";

    private StartInfo startInfo;

    /**
     * Initializes a new {@link TaskDataExport}.
     *
     * @param sink The sink to output to
     * @param savepoint The optional save-point previously set by this provider
     * @param task The data export task providing needed arguments
     * @param locale The locale
     * @param services The service look-up
     */
    public TaskDataExport(DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale, ServiceLookup services) {
        super(ID_TASKS, sink, savepoint, task, locale, services);
    }

    /**
     * Exports tasks.
     *
     * @return The export result
     * @throws OXException If export fails
     */
    @Override
    public ExportResult export() throws OXException {
        ICalEmitter icalEmitter = services.getServiceSafe(ICalEmitter.class);
        FolderService folderService = services.getServiceSafe(FolderService.class);
        UserService userService = services.getServiceSafe(UserService.class);
        ContextService contextService = services.getServiceSafe(ContextService.class);
        ThreadPoolService threadPool = services.getServiceSafe(ThreadPoolService.class);

        try {
            Session session = new GeneratedSession(task.getUserId(), task.getContextId());

            NeededServices neededServices = new NeededServices(folderService, icalEmitter, contextService, threadPool);

            Options options;
            {
                Module tasksModule = getModule();
                boolean includePublicFolders = getBoolOption(TasksDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, false, tasksModule);
                boolean includeSharedFolders = getBoolOption(TasksDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, false, tasksModule);
                options = new Options(includePublicFolders, includeSharedFolders);
            }

            if (savepoint.isPresent()) {
                JSONObject jSavePoint = savepoint.get();
                int taskId = jSavePoint.optInt("id", 0);
                startInfo = new StartInfo(taskId > 0 ? I(taskId) : null, jSavePoint.getInt("folder"), jSavePoint.getInt("root"));
            } else {
                startInfo = null;
            }

            User user = userService.getUser(task.getUserId(), task.getContextId());
            TimeZone timeZone = TimeZoneUtils.getTimeZone(user.getTimeZone());
            ContentType contentType = folderService.parseContentType(ID_TASKS);
            DecoratorProvider decoratorProvider = new DecoratorProvider(locale, timeZone, contentType);

            Locale locale = this.locale;
            StringHelper helper = StringHelper.valueOf(locale);

            tryTouch();

            // Private
            if (startInfo == null || PRIVATE_ID == startInfo.root) {
                Folder folder = new Folder(PRIVATE_ID, helper.getString(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME), true);
                SavePointAndReason optSavePoint = traverseFolder(PRIVATE_ID, PrivateType.getInstance(), folder, null, options, decoratorProvider, session, neededServices);
                if (optSavePoint != null) {
                    return optSavePoint.result();
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            // Shared
            if (options.includeSharedFolders && (startInfo == null || SHARED_ID == startInfo.root)) {
                Folder folder = new Folder(SHARED_ID, helper.getString(FolderStrings.SYSTEM_SHARED_FOLDER_NAME), true);
                SavePointAndReason optSavePoint = traverseFolder(SHARED_ID, SharedType.getInstance(), folder, null, options, decoratorProvider, session, neededServices);
                if (optSavePoint != null) {
                    return optSavePoint.result();
                }

                if (startInfo != null) {
                    startInfo = null;
                }
            }

            // Public
            if (options.includePublicFolders && (startInfo == null || PUBLIC_ID == startInfo.root)) {
                Folder folder = new Folder(PUBLIC_ID, helper.getString(FolderStrings.SYSTEM_PUBLIC_FOLDER_NAME), true);
                SavePointAndReason optSavePoint = traverseFolder(PUBLIC_ID, PublicType.getInstance(), folder, null, options, decoratorProvider, session, neededServices);
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

    private SavePointAndReason traverseFolder(int root, Type type, Folder folder, String path, Options options, DecoratorProvider decoratorProvider, Session session, NeededServices neededServices) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            if (startInfo != null) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", startInfo.folderId).putSafe("root", I(root));
                if (startInfo.taskId != null) {
                    jSavePoint.putSafe("id", startInfo.taskId);
                }
                return savePointFor(jSavePoint);
            }

            return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", I(root)));
        }
        checkAborted();

        StartInfo info = startInfo;
        if (info == null || info.folderId.equals(folder.getFolderId())) {
            if (info == null) {
                if (!exportFolder(folder, path)) {
                    return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", I(root)));
                }
                LOG.debug("Exported task directory {} for data export {} of user {} in context {}", folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
            }

            if (!folder.isRootFolder()) {
                String newPath = (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
                SavePointAndReason jSavePoint = exportTasks(root, folder, newPath, info == null ? null : info.taskId, session, neededServices);
                if (jSavePoint != null) {
                    return jSavePoint;
                }
            }

            info = null;
            startInfo = null;
        }

        if (folder.isRootFolder()) {
            List<Folder> children;
            try {
                FolderService folderService = neededServices.folderService;
                UserizedFolder[] visibleFolders = folderService.getVisibleFolders(folder.getFolderId(), FolderStorage.REAL_TREE_ID, decoratorProvider.contentType, type, true, session, decoratorProvider.createDecorator()).getResponse();
                if (null == visibleFolders || visibleFolders.length <= 0) {
                    children = Collections.emptyList();
                } else {
                    Translator translator = null;
                    if (SharedType.getInstance().equals(type)) {
                        translator = services.getServiceSafe(TranslatorFactory.class).translatorFor(locale);
                    }

                    children = new ArrayList<>(visibleFolders.length);
                    for (UserizedFolder subfolder : visibleFolders) {
                        String namePrefix = null;
                        if (translator != null) {
                            FullNameBuilderService fullNameBuilder = services.getServiceSafe(FullNameBuilderService.class);
                            namePrefix = generateFullNamePrefix(fullNameBuilder.buildFullName(subfolder.getCreatedBy(), task.getContextId(), translator));
                        }
                        children.add(new Folder(subfolder.getID(), namePrefix == null ? subfolder.getLocalizedName(locale) : namePrefix + subfolder.getLocalizedName(locale), false));
                    }
                }
            } catch (Exception e) {
                if (isRetryableExceptionAndMayFail(e, sink)) {
                    if (info != null) {
                        JSONObject jSavePoint = new JSONObject(4).putSafe("folder", info.folderId).putSafe("root", I(root));
                        if (info.taskId != null) {
                            jSavePoint.putSafe("id", info.taskId);
                        }
                        return savePointFor(jSavePoint, e);
                    }

                    return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", I(root)), e);
                }
                LOG.warn("Failed to retrieve subfolders of folder \"{}\" from user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to retrieve subfolders of folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_TASKS).withTimeStamp(new Date()).build());
                children = Collections.emptyList();
            }

            if (!children.isEmpty()) {
                String newPath = (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
                for (Folder child : children) {
                    SavePointAndReason jSavePoint = traverseFolder(root, type, child, newPath, options, decoratorProvider, session, neededServices);
                    if (jSavePoint != null) {
                        return jSavePoint;
                    }
                }
            }
        }

        return null;
    }

    private boolean exportFolder(Folder folderInfo, String path) throws OXException {
        return sink.export(new Directory(path, sanitizeNameForZipEntry(folderInfo.getName())));
    }

    private static final Date DATE_ZERO = new Date(0);

    private static final int[] FIELDS_ID = new int[] { DataObject.OBJECT_ID };

    private static final Comparator<Task> COMPARATOR = new Comparator<Task>() {

        @Override
        public int compare(Task o1, Task o2) {
            int x = o1.getObjectID();
            int y = o2.getObjectID();
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };

    private SavePointAndReason exportTasks(int root, Folder folder, String path, Integer startTaskId, Session session, NeededServices neededServices) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", I(root));
            if (startTaskId != null) {
                jSavePoint.putSafe("id", startTaskId);
            }
            return savePointFor(jSavePoint);
        }
        checkAborted();

        ICalEmitter icalEmitter = neededServices.icalEmitter;
        TasksSQLInterface tasksSql = new TasksSQLImpl(session);
        Context context = neededServices.contextService.getContext(this.task.getContextId());
        ThreadPoolService threadPool = neededServices.threadPool;

        int iFolderId = parsePositiveInt(folder.getFolderId());
        List<Task> tasks;
        SearchIterator<Task> queriedTasks = null;
        try {
            queriedTasks = tasksSql.getModifiedTasksInFolder(iFolderId, FIELDS_ID, DATE_ZERO);
            if (!queriedTasks.hasNext()) {
                // No tasks in given folder
                return null;
            }

            if (isPauseRequested()) {
                JSONObject jSavePoint = new JSONObject(4);
                jSavePoint.putSafe("folder", folder.getFolderId());
                jSavePoint.putSafe("root", I(root));
                jSavePoint.putSafe("id", startTaskId != null ? startTaskId : I(queriedTasks.next().getObjectID()));
                return savePointFor(jSavePoint);
            }
            checkAborted();

            tasks = SearchIterators.asList(queriedTasks);
            Collections.sort(tasks, COMPARATOR);

            if (startTaskId != null) {
                boolean found = false;
                int index = 0;
                while (!found && index < tasks.size()) {
                    Task t = tasks.get(index);
                    if (t.getObjectID() == startTaskId.intValue()) {
                        found = true;
                    } else {
                        index++;
                    }
                }

                if (found && index > 0) {
                    tasks = tasks.subList(index, tasks.size());
                }
            }

            LOG.debug("Going to export {} tasks from directory {} for data export {} of user {} in context {}", I(tasks.size()), folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
        } catch (DataExportAbortedException e) {
            throw e;
        } catch (Exception e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", I(root));
                if (startTaskId != null) {
                    jSavePoint.putSafe("id", startTaskId);
                }
                return savePointFor(jSavePoint, e);
            }
            if (isPermissionDenied(e)) {
                LOG.debug("Forbidden to export tasks from folder \"{}\" for user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Insufficient permissions to export tasks from folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_TASKS).withTimeStamp(new Date()).build());
            } else {
                LOG.warn("Failed to export tasks from folder \"{}\" for user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to export tasks from folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_TASKS).withTimeStamp(new Date()).build());
            }
            return null;
        } finally {
            SearchIterators.close(queriedTasks);
        }

        int batchCount = 0;
        List<ConversionError> errors = new LinkedList<>();
        List<ConversionWarning> warnings = new LinkedList<>();
        for (Task task : tasks) {
            int taskId = task.getObjectID();

            if (isPauseRequested()) {
                JSONObject jSavePoint = new JSONObject(4);
                jSavePoint.putSafe("folder", folder.getFolderId());
                jSavePoint.putSafe("root", I(root));
                jSavePoint.putSafe("id", I(taskId));
                return savePointFor(jSavePoint);
            }
            int count = incrementAndGetCount();
            checkAborted((count % 10 == 0));
            if (count % 100 == 0) {
                sink.setSavePoint(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", I(root)).putSafe("id", I(taskId)));
            }
            batchCount++;

            List<IFileHolder> attachments = null;
            try {
                // Query full task
                Task t = tasksSql.getTaskById(taskId, iFolderId);

                // Prepare attachments
                if (t.getNumberOfAttachments() > 0) {
                    int folderId = Strings.getUnsignedInt(folder.getFolderId());
                    if (folderId > 0) {
                        Optional<List<IFileHolder>> optionalAttachments = loadAttachmentBinaries(taskId, com.openexchange.groupware.Types.TASK, folderId, session);
                        if (optionalAttachments != null && optionalAttachments.isPresent()) {
                            attachments = optionalAttachments.get();
                            t.setProperty(PROPERTY_BINARY_ATTACHMENTS, attachments);
                        }
                    }
                }

                // Write task as iCal to iCal session
                ICalSession iCalSession = icalEmitter.createSession();
                icalEmitter.writeTask(iCalSession, t, context, errors, warnings);

                // Initiate piped streams to pipe iCal output to sink
                ExceptionAwarePipedInputStream in = new ExceptionAwarePipedInputStream(65536);
                ExceptionForwardingPipedOutputStream out = new ExceptionForwardingPipedOutputStream(in);
                try {
                    BlockingAtomicReference<Boolean> exportedFlag = new BlockingAtomicReference<>();
                    DataExportSink sink = this.sink;
                    AbstractTask<Void> fileStorageWriter = new AbstractTask<Void>() {

                        @Override
                        public Void call() throws OXException {
                            boolean exported = sink.export(in, new Item(path, taskId + ".ics", null));
                            exportedFlag.set(Boolean.valueOf(exported));
                            return null;
                        }
                    };
                    threadPool.submit(fileStorageWriter);
                    icalEmitter.writeSession(iCalSession, out);
                    Boolean exported = exportedFlag.getUninterruptibly();
                    if (!exported.booleanValue()) {
                        return savePointFor(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", I(root)).putSafe("id", I(taskId)));
                    }
                    LOG.debug("Exported task {} ({} of {}) from directory {} for data export {} of user {} in context {}", I(taskId), I(batchCount), I(tasks.size()), folder.getName(), UUIDs.getUnformattedString(this.task.getId()), I(this.task.getUserId()), I(this.task.getContextId()));
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    if (null == cause) {
                        in.setException(e);
                    } else if (cause instanceof Exception) {
                        in.setException((Exception) cause);
                    } else {
                        in.setException(new Exception(e));
                    }
                    throw e;
                } finally {
                    Streams.close(in, out);
                }
            } catch (Exception e) {
                if (isRetryableExceptionAndMayFail(e, sink)) {
                    return savePointFor(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", I(root)).putSafe("id", I(taskId)), e);
                }
                LOG.warn("Failed to export task {} in folder \"{}\" from primary mail account of user {} in context {}", I(taskId), folder.getName(), I(this.task.getUserId()), I(this.task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to export task \"").appendToMessage(taskId).appendToMessage("\" in folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_TASKS).withTimeStamp(new Date()).build());
            } finally {
                Streams.close(attachments);
            }
        }

        return null;
    }

    // ------------------------------------------------------------ Helpers -------------------------------------------------------------

    private static class Folder extends com.openexchange.gdpr.dataexport.provider.general.Folder {

        Folder(int folderId, String name, boolean rootFolder) {
            this(Integer.toString(folderId), name, rootFolder);
        }

        Folder(String folderId, String name, boolean rootFolder) {
            super(folderId, name, rootFolder);
        }
    }

    private static class DecoratorProvider {

        private final Locale locale;
        private final TimeZone timeZone;
        private final List<ContentType> allowedContentTypes;
        final ContentType contentType;

        DecoratorProvider(Locale locale, TimeZone timeZone, ContentType contentTypes) {
            super();
            this.locale = locale;
            this.timeZone = timeZone;
            this.allowedContentTypes = Collections.singletonList(contentTypes);
            this.contentType = contentTypes;
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

        final int root;
        final String folderId;
        final Integer taskId;

        StartInfo(Integer taskId, int folderId, int root) {
            this(taskId, Integer.toString(folderId), root);
        }

        StartInfo(Integer taskId, String folderId, int root) {
            super();
            this.root = root;
            this.taskId = taskId;
            this.folderId = folderId;
        }
    }

    private static class NeededServices {

        final ICalEmitter icalEmitter;
        final FolderService folderService;
        final ContextService contextService;
        final ThreadPoolService threadPool;

        NeededServices(FolderService folderService, ICalEmitter icalEmitter, ContextService contextService, ThreadPoolService threadPool) {
            super();
            this.icalEmitter = icalEmitter;
            this.folderService = folderService;
            this.contextService = contextService;
            this.threadPool = threadPool;
        }
    }

}
