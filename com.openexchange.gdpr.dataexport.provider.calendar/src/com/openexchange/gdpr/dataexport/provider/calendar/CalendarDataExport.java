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

package com.openexchange.gdpr.dataexport.provider.calendar;

import static com.openexchange.gdpr.dataexport.DataExportProviders.getBoolOption;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isPermissionDenied;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isRetryableExceptionAndMayFail;
import static com.openexchange.gdpr.dataexport.provider.general.SavePointAndReason.savePointFor;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Collections.toMultiMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
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
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.util.UUIDs;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link CalendarDataExport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class CalendarDataExport extends AbstractDataExportProviderTask {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarDataExport.class);

    private static final String PRIVATE_ID = Integer.toString(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
    private static final String PUBLIC_ID = Integer.toString(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
    private static final String SHARED_ID = Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID);
    private static final String ID_CALENDAR = "calendar";

    private StartInfo startInfo;
    /**
     * Initializes a new {@link CalendarDataExport}.
     *
     * @param sink The sink to output to
     * @param savepoint The optional save-point previously set by this provider
     * @param task The data export task providing needed arguments
     * @param locale The locale
     * @param services The service look-up
     * @throws OXException
     */
    public CalendarDataExport(DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale, ServiceLookup services) throws OXException {
        super(ID_CALENDAR, sink, savepoint, task, locale, services);
        if (savepoint.isPresent()) {
            JSONObject jSavePoint = savepoint.get();
            try {
                startInfo = new StartInfo(jSavePoint.optString("uid", null), jSavePoint.getString("folder"), jSavePoint.getString("root"));
            } catch (JSONException e) {
                throw DataExportExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        } else {
            startInfo = null;
        }
    }

    /**
     * Exports events.
     *
     * @return The export result
     * @throws OXException If export fails
     */
    @Override
    public ExportResult export() throws OXException {
        CalendarService calendarService = services.getServiceSafe(CalendarService.class);
        ICalService icalService = services.getServiceSafe(ICalService.class);
        FolderService folderService = services.getServiceSafe(FolderService.class);
        UserService userService = services.getServiceSafe(UserService.class);

        try {
            Session session = new GeneratedSession(task.getUserId(), task.getContextId());

            NeededServices neededServices = new NeededServices(folderService, calendarService, icalService);

            Options options;
            {
                Module calendarModule = getModule();
                boolean subscribedOnly = getBoolOption(CalendarDataExportPropertyNames.PROP_INCLUDE_UNSUBSCRIBED, false, calendarModule);
                boolean includePublicFolders = getBoolOption(CalendarDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, false, calendarModule);
                boolean includeSharedFolders = getBoolOption(CalendarDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, false, calendarModule);
                options = new Options(subscribedOnly, includePublicFolders, includeSharedFolders);
            }

            User user = userService.getUser(task.getUserId(), task.getContextId());
            TimeZone timeZone = TimeZoneUtils.getTimeZone(user.getTimeZone());
            ContentType contentType = folderService.parseContentType("event");
            DecoratorProvider decoratorProvider = new DecoratorProvider(locale, timeZone, contentType);

            Locale locale = this.locale;
            StringHelper helper = StringHelper.valueOf(locale);

            tryTouch();

            // Private
            if (startInfo == null || PRIVATE_ID.equals(startInfo.root)) {
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
            if (options.includeSharedFolders && (startInfo == null || SHARED_ID.equals(startInfo.root))) {
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
            if (options.includePublicFolders && (startInfo == null || PUBLIC_ID.equals(startInfo.root))) {
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
                if (startInfo.eventUid != null) {
                    jSavePoint.putSafe("uid", startInfo.eventUid);
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
                LOG.debug("Exported calendar directory {} for data export {} of user {} in context {}", folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
            }

            if (!folder.isRootFolder() && !folder.getFolderId().startsWith(SHARED_PREFIX)) {
                String newPath = (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
                SavePointAndReason jSavePoint = exportEvents(root, folder, newPath, startInfo == null ? null : startInfo.eventUid, session, neededServices);
                if (jSavePoint != null) {
                    return jSavePoint;
                }
            }

            startInfo = null;
        }

        if (folder.isRootFolder()) {
            List<Folder> children;
            try {
                FolderService folderService = neededServices.folderService;
                UserizedFolder[] visibleFolders = folderService.getVisibleFolders(folder.getFolderId(), FolderStorage.REAL_TREE_ID, decoratorProvider.contentType, type, !options.subscribedOnly, session, decoratorProvider.createDecorator()).getResponse();
                if (null == visibleFolders || visibleFolders.length <= 0) {
                    children = Collections.emptyList();
                } else {
                    Translator translator = null;
                    if (SharedType.getInstance().equals(type)) {
                        translator = services.getServiceSafe(TranslatorFactory.class).translatorFor(locale);
                    }

                    children = new ArrayList<>(visibleFolders.length);
                    for (UserizedFolder subfolder : visibleFolders) {
                        if (subfolder.getID().startsWith(CalendarUtils.DEFAULT_ACCOUNT_PREFIX)) {
                            String folderId = subfolder.getID().substring(CalendarUtils.DEFAULT_ACCOUNT_PREFIX.length());
                            String namePrefix = null;
                            if (translator != null) {
                                FullNameBuilderService fullNameBuilder = services.getServiceSafe(FullNameBuilderService.class);
                                namePrefix = generateFullNamePrefix(fullNameBuilder.buildFullName(subfolder.getCreatedBy(), task.getContextId(), translator));
                            }
                            children.add(new Folder(folderId, namePrefix == null ? subfolder.getLocalizedName(locale) : namePrefix + subfolder.getLocalizedName(locale), false));
                        }
                    }
                }
            } catch (Exception e) {
                if (isRetryableExceptionAndMayFail(e, sink)) {
                    if (startInfo != null) {
                        JSONObject jSavePoint = new JSONObject(4).putSafe("folder", startInfo.folderId).putSafe("root", root);
                        if (startInfo.eventUid != null) {
                            jSavePoint.putSafe("uid", startInfo.eventUid);
                        }
                        return savePointFor(jSavePoint, e);
                    }
                    return savePointFor(new JSONObject(2).putSafe("folder", folder.getFolderId()).putSafe("root", root), e);
                }
                LOG.warn("Failed to retrieve subfolders of folder \"{}\" from user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to retrieve subfolders of folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CALENDAR).withTimeStamp(new Date()).build());
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

    private SavePointAndReason exportEvents(String root, Folder folder, String path, String startEventUid, Session session, NeededServices neededServices) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
            if (startEventUid != null) {
                jSavePoint.putSafe("uid", startEventUid);
            }
            return savePointFor(jSavePoint);
        }
        checkAborted();

        ICalService icalService = neededServices.icalService;
        CalendarService calendarService = neededServices.calendarService;
        CalendarSession calendarSession = calendarService.init(session);

        NavigableMap<String, List<Event>> eventsByUid;
        try {
            calendarSession.set(CalendarParameters.PARAMETER_FIELDS, new EventField[] { EventField.FOLDER_ID, EventField.ID, EventField.UID, EventField.SERIES_ID });
            calendarSession.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
            List<Event> partlyFilledEvents = calendarService.getEventsInFolder(calendarSession, folder.getFolderId());
            if (partlyFilledEvents.isEmpty()) {
                // No events in given folder
                return null;
            }

            eventsByUid = toMultiMap(partlyFilledEvents, new TreeMap<>(), Event::getUid);

            if (isPauseRequested()) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
                if (startEventUid != null) {
                    jSavePoint.putSafe("uid", startEventUid);
                }
                return savePointFor(jSavePoint);
            }
            checkAborted();

            if (startEventUid != null) {
                boolean found = false;
                Iterator<Map.Entry<String, List<Event>>> iterator = eventsByUid.entrySet().iterator();
                String uid = null;
                while (!found && iterator.hasNext()) {
                    uid = iterator.next().getKey();
                    if (uid.equals(startEventUid)) {
                        found = true;
                    }
                }

                if (found) {
                    eventsByUid = eventsByUid.tailMap(uid, true);
                }
            }
            LOG.debug("Going to export {} events from directory {} for data export {} of user {} in context {}", I(eventsByUid.size()), folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
        } catch (DataExportAbortedException e) {
            throw e;
        } catch (Exception e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root);
                if (startEventUid != null) {
                    jSavePoint.putSafe("uid", startEventUid);
                }
                return savePointFor(jSavePoint, e);
            }
            if (isPermissionDenied(e)) {
                LOG.debug("Forbidden to export events from folder \"{}\" for user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builderWithPermissionDeniedType().appendToMessage("Insufficient permissions to export appointments from folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CALENDAR).withTimeStamp(new Date()).build());
            } else {
                LOG.warn("Failed to export events from folder \"{}\" for user {} in context {}", folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to export appointments from folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CALENDAR).withTimeStamp(new Date()).build());
            }
            return null;
        }

        // Reset previously set fields
        calendarSession.set(CalendarParameters.PARAMETER_FIELDS, null);
        calendarSession.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, null);

        // Initialize iCal parameters
        ICalParameters iCalParameters = icalService.initParameters();

        int batchCount = 0;
        Iterator<Map.Entry<String, List<Event>>> iterator = eventsByUid.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Event>> uid2events = iterator.next();
            if (uid2events == null || uid2events.getValue().isEmpty()) {
                continue;
            }
            String uid = uid2events.getKey();

            if (isPauseRequested()) {
                JSONObject jSavePoint = new JSONObject(4);
                jSavePoint.putSafe("folder", folder.getFolderId());
                jSavePoint.putSafe("root", root);
                jSavePoint.putSafe("uid", uid);
                return savePointFor(jSavePoint);
            }
            int count = incrementAndGetCount();
            checkAborted((count % 100 == 0));
            if (count % 1000 == 0) {
                sink.setSavePoint(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("uid", uid));
            }
            batchCount++;

            try {
                CalendarExport calendarExport = icalService.exportICal(iCalParameters);
                for (Event partlyFilledEvent : uid2events.getValue()) {
                    Event event = calendarService.getEvent(calendarSession, folder.getFolderId(), new EventID(folder.getFolderId(), partlyFilledEvent.getId()));
                    calendarExport.add(prepareForExport(calendarSession, event, calendarService));
                }

                boolean exported = sink.export(calendarExport.getClosingStream(), new Item(path, sanitizeNameForZipEntry(uid + ".ics"), null));
                if (!exported) {
                    return savePointFor(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("uid", uid));
                }
                LOG.debug("Exported event {} ({} of {}) from directory {} for data export {} of user {} in context {}", uid, I(batchCount), I(eventsByUid.size()), folder.getName(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
            } catch (Exception e) {
                if (isRetryableExceptionAndMayFail(e, sink)) {
                    return savePointFor(new JSONObject(4).putSafe("folder", folder.getFolderId()).putSafe("root", root).putSafe("uid", uid), e);
                }
                LOG.warn("Failed to export event {} in folder \"{}\" from primary mail account of user {} in context {}", uid, folder.getName(), I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to export event \"").appendToMessage(uid).appendToMessage("\" in folder \"").appendToMessage(folder.getName()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_CALENDAR).withTimeStamp(new Date()).build());
            }
        }

        return null;
    }

    /**
     * Prepares an event for export.
     *
     * @param event The event
     * @return The prepared event
     * @throws OXException
     */
    private static Event prepareForExport(CalendarSession session, Event event, CalendarService calendarService) throws OXException {
        Event retval = event;
        if (CalendarUtils.isPseudoGroupScheduled(event)) {
            Event copy;
            try {
                copy = EventMapper.getInstance().copy(event, null, (EventField[]) null);
            } catch (OXException e) {
                LOG.warn("Error copying event, falling back to original event data", e);
                return event;
            }
            copy.removeAttendees();
            copy.removeOrganizer();
            retval = copy;
        }

        if (retval.getAttachments() != null) {
            for (Attachment attachment : retval.getAttachments()) {
                if (attachment.getManagedId() > 0) {
                    attachment.setData(calendarService.getAttachment(session, new EventID(event.getFolderId(), event.getId()), attachment.getManagedId()));
                }
            }
        }
        return retval;
    }

    // ------------------------------------------------------------ Helpers -------------------------------------------------------------

    private static class DecoratorProvider {

        private final Locale locale;
        private final TimeZone timeZone;
        private final List<ContentType> allowedContentTypes;
        final ContentType contentType;

        DecoratorProvider(Locale locale, TimeZone timeZone, ContentType contentType) {
            super();
            this.locale = locale;
            this.timeZone = timeZone;
            this.allowedContentTypes = Collections.singletonList(contentType);
            this.contentType = contentType;
        }

        FolderServiceDecorator createDecorator() {
            return new FolderServiceDecorator().setLocale(locale).setTimeZone(timeZone).setAllowedContentTypes(allowedContentTypes).put("suppressUnifiedMail", Boolean.TRUE);
        }
    }

    private static class Options {

        final boolean subscribedOnly;
        final boolean includePublicFolders;
        final boolean includeSharedFolders;

        Options(boolean subscribedOnly, boolean includePublicFolders, boolean includeSharedFolders) {
            super();
            this.subscribedOnly = subscribedOnly;
            this.includePublicFolders = includePublicFolders;
            this.includeSharedFolders = includeSharedFolders;
        }
    }

    private static class StartInfo {

        final String root;
        final String folderId;
        final String eventUid;

        StartInfo(String uid, String folderId, String root) {
            super();
            this.root = root;
            this.eventUid = uid;
            this.folderId = folderId;
        }
    }

    private static class NeededServices {

        final CalendarService calendarService;
        final ICalService icalService;
        final FolderService folderService;

        NeededServices(FolderService folderService, CalendarService calendarService, ICalService icalService) {
            super();
            this.calendarService = calendarService;
            this.icalService = icalService;
            this.folderService = folderService;
        }
    }

}
