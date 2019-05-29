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

package com.openexchange.gdpr.dataexport.provider.mail.internal;

import static com.openexchange.gdpr.dataexport.DataExportProviders.getBoolOption;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isPermissionDenied;
import static com.openexchange.gdpr.dataexport.DataExportProviders.isRetryableExceptionAndMayFail;
import static com.openexchange.gdpr.dataexport.provider.general.SavePointAndReason.savePointFor;
import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
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
import com.openexchange.gdpr.dataexport.provider.mail.MailDataExportPropertyNames;
import com.openexchange.gdpr.dataexport.provider.mail.generator.FailedAuthenticationResult;
import com.openexchange.gdpr.dataexport.provider.mail.generator.SessionGenerator;
import com.openexchange.java.Collators;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageInfoSupport;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.service.MailService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MailDataExport}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class MailDataExport extends AbstractDataExportProviderTask {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailDataExport.class);

    private static final String ID_MAIL = "mail";

    private final SessionGeneratorRegistry generatorRegistry;

    /**
     * Initializes a new {@link MailDataExport}.
     *
     * @param sink The sink to output to
     * @param savepoint The optional save-point previously set by this provider
     * @param task The data export task providing needed arguments
     * @param locale The locale
     * @param generatorRegistry The generator registry
     * @param services The service look-up
     */
    public MailDataExport(DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale, SessionGeneratorRegistry generatorRegistry, ServiceLookup services) {
        super(ID_MAIL, sink, savepoint, task, locale, services);
        this.generatorRegistry = generatorRegistry;
    }

    /**
     * Exports mail messages.
     *
     * @return The export result
     * @throws OXException If export fails
     */
    @Override
    public ExportResult export() throws OXException {
        MailService mailService = services.getServiceSafe(MailService.class);

        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            Module mailModule = getModule();

            SessionGenerator sessionGenerator;
            {
                String generatorId = getProperty(MailDataExportPropertyNames.PROP_SESSION_GENERATOR, mailModule);
                sessionGenerator = generatorRegistry.getGeneratorById(generatorId);
            }

            GeneratedSession session = generateSession(mailModule, sessionGenerator);

            Options options;
            {
                boolean subscribedOnly = getBoolOption(MailDataExportPropertyNames.PROP_INCLUDE_UNSUBSCRIBED, false, mailModule);
                boolean includePublicFolders = getBoolOption(MailDataExportPropertyNames.PROP_INCLUDE_PUBLIC_FOLDERS, false, mailModule);
                boolean includeSharedFolders = getBoolOption(MailDataExportPropertyNames.PROP_INCLUDE_SHARED_FOLDERS, false, mailModule);
                boolean includeTrashFolder = getBoolOption(MailDataExportPropertyNames.PROP_INCLUDE_TRASH_FOLDER, false, mailModule);
                options = new Options(subscribedOnly, includePublicFolders, includeSharedFolders, includeTrashFolder);
            }

            StartInfo startInfo;
            if (savepoint.isPresent()) {
                JSONObject jSavePoint = savepoint.get();
                startInfo = new StartInfo(jSavePoint.optString("id", null), jSavePoint.getString("folder"));
            } else {
                startInfo = null;
            }

            tryTouch();

            // Connect
            boolean tryConnect = true;
            while (tryConnect) {
                try {
                    mailAccess = mailService.getMailAccess(session, 0);
                    mailAccess.connect();
                    tryConnect = false;
                } catch (OXException e) {
                    // Check for failed authentication
                    if (!com.openexchange.mail.api.MailAccess.isAuthFailed(e)) {
                        throw e;
                    }

                    // Retry with new generated session
                    FailedAuthenticationResult failedAuthenticationResult = sessionGenerator.onFailedAuthentication(e, session, mailModule.getProperties().get());
                    if (!failedAuthenticationResult.retry()) {
                        throw e;
                    }
                    session = failedAuthenticationResult.getOptionalSession().get();
                }
            }

            Folder rootFolder;
            {
                IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
                IMailFolderStorageInfoSupport infoSupport = folderStorage.supports(IMailFolderStorageInfoSupport.class);
                if (null != infoSupport && infoSupport.isInfoSupported()) {
                    rootFolder = new DefaultFolder(infoSupport.getFolderInfo(MailFolder.DEFAULT_FOLDER_ID), infoSupport);
                } else {
                    rootFolder = new DefaultFolder(folderStorage.getRootFolder(), folderStorage);
                }
            }

            mailAccess.setWaiting(true); // Mark as "waiting" to prevent from mail access watcher outputting false-positive warnings
            try {
                SavePointAndReason optSavePoint = traverseFolder(rootFolder, null, startInfo, options, mailAccess);
                if (optSavePoint != null) {
                    return optSavePoint.result();
                }
            } finally {
                mailAccess.setWaiting(false); // Unset "waiting" flag
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
        } finally {
            MailAccess.closeInstance(mailAccess);
        }
    }

    private SavePointAndReason traverseFolder(Folder folder, String path, StartInfo startInfo, Options options, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            if (startInfo != null) {
                JSONObject jSavePoint = new JSONObject(4);
                jSavePoint.putSafe("folder", startInfo.fullName);
                if (startInfo.mailId != null) {
                    jSavePoint.putSafe("id", startInfo.mailId);
                }
                return savePointFor(jSavePoint);
            }

            return savePointFor(new JSONObject(2).putSafe("folder", folder.getFullname()));
        }
        checkAborted();

        StartInfo info = startInfo;
        if (info == null || info.fullName.equals(folder.getFullname())) {
            if (info == null) {
                if (!exportFolder(folder, path)) {
                    return savePointFor(new JSONObject(2).putSafe("folder", folder.getFullname()));
                }
                LOG.debug("Exported mail directory {} for data export {} of user {} in context {}", folder.getFullname(), UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
            }

            if (folder.isHoldsMessages()) {
                String newPath = folder.isRootFolder() ? "" : (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
                SavePointAndReason jSavePoint = exportMessages(folder.getFullname(), newPath, info == null ? null : info.mailId, mailAccess);
                if (jSavePoint != null) {
                    return jSavePoint;
                }
            }

            info = null;
        }

        List<Folder> children;
        try {
            children = folder.getChildren(options.subscribedOnly);
        } catch (Exception e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                if (info != null) {
                    JSONObject jSavePoint = new JSONObject(4);
                    jSavePoint.putSafe("folder", info.fullName);
                    if (info.mailId != null) {
                        jSavePoint.putSafe("id", info.mailId);
                    }
                    return savePointFor(jSavePoint, e);
                }

                return savePointFor(new JSONObject(2).putSafe("folder", folder.getFullname()), e);
            }
            LOG.warn("Failed to retrieve subfolders of folder \"{}\" from primary mail account of user {} in context {}", folder.getFullname(), I(task.getUserId()), I(task.getContextId()), e);
            sink.addToReport(Message.builder().appendToMessage("Failed to retrieve subfolders of folder \"").appendToMessage(folder.getFullname()).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_MAIL).withTimeStamp(new Date()).build());
            children = Collections.emptyList();
        }

        if (!children.isEmpty()) {
            children = children.stream().filter(new FolderPredicate(options)).sorted(new FolderFullNameComparator(locale)).collect(Collectors.toList());

            String newPath = folder.isRootFolder() ? "" : (path == null ? "" : path) + sanitizeNameForZipEntry(folder.getName()) + "/";
            for (Folder child : children) {
                SavePointAndReason jSavePoint = traverseFolder(child, newPath, info, options, mailAccess);
                if (jSavePoint != null) {
                    return jSavePoint;
                }
            }
        }

        return null;
    }

    private boolean exportFolder(Folder folderInfo, String path) throws OXException {
        if (folderInfo.isRootFolder()) {
            return true;
        }

        return sink.export(new Directory(path, sanitizeNameForZipEntry(folderInfo.getName())));
    }

    private static final MailField[] FIELDS_ID = new MailField[] { MailField.ID };

    private SavePointAndReason exportMessages(String fullName, String path, String startMailId, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException, DataExportAbortedException {
        if (isPauseRequested()) {
            JSONObject jSavePoint = new JSONObject(4);
            jSavePoint.putSafe("folder", fullName);
            if (startMailId != null) {
                jSavePoint.putSafe("id", startMailId);
            }
            return savePointFor(jSavePoint);
        }
        checkAborted();

        MailMessage[] messages;
        try {
            messages = mailAccess.getMessageStorage().getAllMessages(fullName, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, FIELDS_ID);
            if (messages.length <= 0) {
                // No messages in given folder
                return null;
            }

            if (isPauseRequested()) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", fullName);
                if (startMailId != null) {
                    jSavePoint.putSafe("id", startMailId);
                }
                return savePointFor(jSavePoint);
            }
            checkAborted();

            if (startMailId != null) {
                boolean found = false;
                int index = 0;
                while (!found && index < messages.length) {
                    MailMessage m = messages[index];
                    if (m.getMailId().equals(startMailId)) {
                        found = true;
                    } else {
                        index++;
                    }
                }

                if (found) {
                    if (index > 0) {
                        int newlength = messages.length - index;
                        MailMessage[] tmp = new MailMessage[newlength];
                        System.arraycopy(messages, index, tmp, 0, newlength);
                        messages = tmp;
                    }
                }
            }

            LOG.debug("Going to export {} mails from directory {} for data export {} of user {} in context {}", I(messages.length), fullName, UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
        } catch (DataExportAbortedException e) {
            throw e;
        } catch (Exception e) {
            if (isRetryableExceptionAndMayFail(e, sink)) {
                JSONObject jSavePoint = new JSONObject(4).putSafe("folder", fullName);
                if (startMailId != null) {
                    jSavePoint.putSafe("id", startMailId);
                }
                return savePointFor(jSavePoint, e);
            }
            if (MailFolder.DEFAULT_FOLDER_ID.equals(fullName)) {
                LOG.debug("Failed to export messages from folder \"{}\" from primary mail account of user {} in context {}", fullName, I(task.getUserId()), I(task.getContextId()), e);
            } else {
                if (isPermissionDenied(e)) {
                    LOG.debug("Forbidden to export messages from folder \"{}\" from primary mail account of user {} in context {}", fullName, I(task.getUserId()), I(task.getContextId()), e);
                    sink.addToReport(Message.builder().appendToMessage("Insufficient permissions to export messages from folder \"").appendToMessage(fullName).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_MAIL).withTimeStamp(new Date()).build());
                } else {
                    LOG.warn("Failed to export messages from folder \"{}\" from primary mail account of user {} in context {}", fullName, I(task.getUserId()), I(task.getContextId()), e);
                    sink.addToReport(Message.builder().appendToMessage("Failed to export messages from folder \"").appendToMessage(fullName).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_MAIL).withTimeStamp(new Date()).build());
                }
            }
            return null;
        }

        int batchCount = 0;
        for (MailMessage message : messages) {
            String mailId = message.getMailId();

            if (isPauseRequested()) {
                JSONObject jSavePoint = new JSONObject(4);
                jSavePoint.putSafe("folder", fullName);
                jSavePoint.putSafe("id", mailId);
                return savePointFor(jSavePoint);
            }
            int count = incrementAndGetCount();
            checkAborted(count % 10 == 0);
            if (count % 100 == 0) {
                sink.setSavePoint(new JSONObject(4).putSafe("folder", fullName).putSafe("id", mailId));
            }
            batchCount++;

            InputStream stream = null;
            try {
                MailMessage m = mailAccess.getMessageStorage().getMessage(fullName, mailId, false);
                stream = MimeMessageUtility.getStreamFromMailPart(m);
                boolean exported = sink.export(stream, new Item(path, sanitizeNameForZipEntry(mailId + ".eml"), m.getSentDate()));
                if (!exported) {
                    return savePointFor(new JSONObject(4).putSafe("folder", fullName).putSafe("id", mailId));
                }
                LOG.debug("Exported mail {} ({} of {}) from directory {} for data export {} of user {} in context {}", mailId, I(batchCount), I(messages.length), fullName, UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
            } catch (Exception e) {
                if (isRetryableExceptionAndMayFail(e, sink)) {
                    return savePointFor(new JSONObject(4).putSafe("folder", fullName).putSafe("id", mailId), e);
                }
                LOG.warn("Failed to export message {} in folder \"{}\" from primary mail account of user {} in context {}", mailId, fullName, I(task.getUserId()), I(task.getContextId()), e);
                sink.addToReport(Message.builder().appendToMessage("Failed to export message \"").appendToMessage(mailId).appendToMessage("\" in folder \"").appendToMessage(fullName).appendToMessage("\": ").appendToMessage(e.getMessage()).withModuleId(ID_MAIL).withTimeStamp(new Date()).build());
            } finally {
                Streams.close(stream);
            }
        }

        return null;
    }

    // ------------------------------------------------------------ Helpers -------------------------------------------------------------

    private GeneratedSession generateSession(Module mailModule, SessionGenerator sessionGenerator) throws OXException {
        int contextId = task.getContextId();
        int userId = task.getUserId();
        return sessionGenerator.generateSession(userId, contextId, mailModule.getProperties().get());
    }

    private static String getProperty(String propName, Module mailModule) {
        Optional<Map<String, Object>> optionalProps = mailModule.getProperties();
        if (!optionalProps.isPresent()) {
            return null;
        }

        return (String) optionalProps.get().get(propName);
    }

    private static class FolderPredicate implements Predicate<Folder> {

        private final Options options;

        FolderPredicate(Options options) {
            super();
            this.options = options;
        }

        @Override
        public boolean test(Folder folder) {
            if (!options.includeSharedFolders && folder.isShared()) {
                return false;
            }
            if (!options.includePublicFolders && folder.isPublic()) {
                return false;
            }
            if (!options.includeTrashFolder && folder.isTrash()) {
                return false;
            }
            return true;
        }
    }

    private static final class FolderFullNameComparator implements Comparator<Folder> {

        private final Collator collator;

        FolderFullNameComparator(Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(Folder o1, Folder o2) {
            /*
             * Compare by full name
             */
            return collator.compare(o1.getFullname(), o2.getFullname());
        }
    }

    private static class Options {

        final boolean subscribedOnly;
        final boolean includePublicFolders;
        final boolean includeSharedFolders;
        final boolean includeTrashFolder;

        Options(boolean subscribedOnly, boolean includePublicFolders, boolean includeSharedFolders, boolean includeTrashFolder) {
            super();
            this.subscribedOnly = subscribedOnly;
            this.includePublicFolders = includePublicFolders;
            this.includeSharedFolders = includeSharedFolders;
            this.includeTrashFolder = includeTrashFolder;
        }
    }

    private static class StartInfo {

        final String fullName;
        final String mailId;

        StartInfo(String mailId, String fullName) {
            super();
            this.mailId = mailId;
            this.fullName = fullName;
        }
    }

}
