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

package com.openexchange.audit.impl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import org.apache.commons.lang.Validate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.Status;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.audit.configuration.AuditConfiguration;
import com.openexchange.audit.services.Services;
import com.openexchange.contact.ContactService;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.logback.extensions.ExtendedPatternLayoutEncoder;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a> - refactoring
 */
public class AuditEventHandler implements EventHandler {

    private Logger createLogger() throws OXException {
        ch.qos.logback.classic.Logger templateLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(AuditEventHandler.class);
        LoggerContext context = templateLogger.getLoggerContext();

        String filePattern = AuditConfiguration.getLogfileLocation();

        ExtendedPatternLayoutEncoder encoder = new ExtendedPatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%date{\"yyyy-MM-dd'T'HH:mm:ss,SSSZ\"} %-5level [%thread] %class.%method\\(%class{0}.java:%line\\)%n%sanitisedMessage%n%lmdc%exception{full}");

        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        triggeringPolicy.setContext(context);
        triggeringPolicy.setMaxFileSize(Integer.toString(AuditConfiguration.getLogfileLimit()));

        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(context);
        rollingPolicy.setFileNamePattern(filePattern + ".%i");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(AuditConfiguration.getLogfileLimit());

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setAppend(AuditConfiguration.getLogfileAppend());
        rollingFileAppender.setContext(context);
        rollingFileAppender.setEncoder(encoder);
        rollingFileAppender.setFile(filePattern);
        rollingFileAppender.setName("AuditAppender");
        rollingFileAppender.setPrudent(false);
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(triggeringPolicy);

        rollingPolicy.setParent(rollingFileAppender);

        encoder.start();
        rollingPolicy.start();
        rollingFileAppender.start();

        List<Status> statuses = context.getStatusManager().getCopyOfStatusList();
        if (null != statuses && false == statuses.isEmpty()) {
            for (Status status : statuses) {
                if (status instanceof ErrorStatus) {
                    ErrorStatus errorStatus = (ErrorStatus) status;
                    Throwable throwable = errorStatus.getThrowable();
                    if (null == throwable) {
                        class FastThrowable extends Throwable {

                            private static final long serialVersionUID = -6877996474956999361L;

                            FastThrowable(String msg) {
                                super(msg);
                            }

                            @Override
                            public synchronized Throwable fillInStackTrace() {
                                return this;
                            }
                        }
                        throwable = new FastThrowable(errorStatus.getMessage());
                    }
                    throw new OXException(OXExceptionConstants.CODE_DEFAULT, OXExceptionStrings.MESSAGE, throwable, new Object[0]);
                }
            }
        }

        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("AuditLogger");
        {
            ch.qos.logback.classic.Level l;
            int iLevel = AuditConfiguration.getLoglevel().intValue();
            if (java.util.logging.Level.ALL.intValue() == iLevel) {
                l = ch.qos.logback.classic.Level.ALL;
            } else if (java.util.logging.Level.SEVERE.intValue() == iLevel) {
                l = ch.qos.logback.classic.Level.ERROR;
            } else if (java.util.logging.Level.WARNING.intValue() == iLevel) {
                l = ch.qos.logback.classic.Level.WARN;
            } else if (java.util.logging.Level.INFO.intValue() == iLevel) {
                l = ch.qos.logback.classic.Level.INFO;
            } else if (java.util.logging.Level.CONFIG.intValue() == iLevel || java.util.logging.Level.FINE.intValue() == iLevel) {
                l = ch.qos.logback.classic.Level.DEBUG;
            } else if (java.util.logging.Level.FINER.intValue() == iLevel || java.util.logging.Level.FINEST.intValue() == iLevel) {
                l = ch.qos.logback.classic.Level.TRACE;
            } else {
                l = ch.qos.logback.classic.Level.ALL;
            }
            logbackLogger.setLevel(l);
        }
        logbackLogger.setAdditive(false);
        logbackLogger.addAppender(rollingFileAppender);

        return logbackLogger;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /** The logger to use */
    private final org.slf4j.Logger logger;

    /** The date format */
    private final SimpleDateFormat logDateFormat;

    /** The user service */
    private final UserService userService;

    /**
     * Initializes a new {@link AuditEventHandler}.
     *
     * @param userService
     * @throws OXException If initialization fails
     */
    public AuditEventHandler(UserService userService) throws OXException {
        super();
        this.userService = userService;
        logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Logger defaultLogger = org.slf4j.LoggerFactory.getLogger(AuditEventHandler.class);
        if (AuditConfiguration.getEnabled()) {
            defaultLogger.info("Using own Logging instance.");
            logger = createLogger();
        } else {
            defaultLogger.info("Using global Logging instance.");
            logger = defaultLogger;
        }
    }

    @Override
    public void handleEvent(final Event event) {
        Validate.notNull(event, "Event must not be null.");

        if (!logger.isInfoEnabled()) {
            // Not allowed to log
            return;
        }
        try {
            StringBuilder logBuilder = new StringBuilder(2048);
            String topic = event.getTopic();

            if (topic.startsWith("com/openexchange/groupware/infostore/")) {
                handleInfostoreEvent(event, logBuilder);
            } else if (topic.startsWith("com/openexchange/groupware/")) {
                handleGroupwareEvent(event, logBuilder);
            }

            if (logBuilder.length() > 0) {
                logger.info(logBuilder.toString());
            }
        } catch (final Exception e) {
            logger.error("", e);
        }
    }

    /**
     * Handles events that belong to the infostore.
     *
     * @param event - the {@link Event} that was received
     * @param logBuilder - the log to add information
     * @throws OXException
     */
    protected void handleInfostoreEvent(Event event, StringBuilder logBuilder) throws OXException {
        Validate.notNull(event, "Event mustn't be null.");
        Validate.notNull(logBuilder, "StringBuilder to write to mustn't be null.");

        String topic = event.getTopic();

        if (topic.equals(FileStorageEventConstants.ACCESS_TOPIC)) {
            if (AuditConfiguration.getFileAccessLogging()) {
                logBuilder.append("EVENT TYPE: ACCESS; ");
            } else {
                return;
            }
        }
        if (topic.equals(FileStorageEventConstants.CREATE_TOPIC)) {
            logBuilder.append("EVENT TYPE: INSERT; ");
        } else if (topic.equals(FileStorageEventConstants.UPDATE_TOPIC)) {
            logBuilder.append("EVENT TYPE: UPDATE; ");
        } else if (topic.equals(FileStorageEventConstants.DELETE_TOPIC)) {
            logBuilder.append("EVENT TYPE: DELETE; ");
        }
        synchronized (logDateFormat) {
            logBuilder.append("EVENT TIME: ").append(logDateFormat.format(new Date())).append("; ");
        }
        logBuilder.append("OBJECT TYPE: FILE; ");

        final Session session = (Session) event.getProperty(FileStorageEventConstants.SESSION);
        if (Boolean.TRUE.equals(session.getParameter(Session.PARAM_PUBLICATION))) {
            String remoteAddress = null;
            try {
                remoteAddress = (String) event.getProperty("remoteAddress");
            } catch (ClassCastException e) {
                remoteAddress = "unknown";
            }

            if (remoteAddress == null) {
                remoteAddress = "unknown";
            }

            logBuilder.append("PUBLISH: ");
            logBuilder.append(remoteAddress);
            logBuilder.append("; ");
        } else {
            appendUserInformation(session.getUserId(), session.getContextId(), logBuilder);
        }
        logBuilder.append("CONTEXT ID: ").append(session.getContextId()).append("; ");
        logBuilder.append("OBJECT ID: ").append(event.getProperty(FileStorageEventConstants.OBJECT_ID)).append("; ");
        {
            final Object fileName = event.getProperty(FileStorageEventConstants.FILE_NAME);
            if (null != fileName) {
                logBuilder.append("FILE NAME: ").append(fileName).append("; ");
            }
        }
        logBuilder.append("SERVICE ID: ").append(event.getProperty(FileStorageEventConstants.SERVICE)).append("; ");
        logBuilder.append("ACCOUNT ID: ").append(event.getProperty(FileStorageEventConstants.ACCOUNT_ID)).append("; ");
        {
            final String folderId = (String) event.getProperty(FileStorageEventConstants.FOLDER_ID);
            if (null != folderId) {
                try {
                    final int iFolderId = Integer.parseInt(folderId);
                    logBuilder.append("FOLDER: ").append(getPathToRoot(iFolderId, session)).append(';');
                } catch (NumberFormatException e) {
                    logBuilder.append("FOLDER: ").append(folderId).append(';');
                }
            }
        }
    }

    /**
     * Handles events that belong to other server parts
     *
     * @param event - the {@link Event} that was received
     * @param logBuilder - the log to add information
     * @throws OXException
     */
    protected void handleGroupwareEvent(Event event, StringBuilder logBuilder) throws OXException {
        Validate.notNull(event, "Event must not be null.");
        Validate.notNull(logBuilder, "StringBuilder to write to must not be null.");

        final CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);

        if (null != commonEvent) {
            final int contextId = commonEvent.getContextId();
            final Context context = ContextStorage.getInstance().getContext(contextId);

            handleMainCommmonEvent(commonEvent, logBuilder);

            ModuleSwitch: switch (commonEvent.getModule()) {
                default:
                    break ModuleSwitch;

                case Types.APPOINTMENT:
                    handleAppointmentCommonEvent(commonEvent, context, logBuilder);
                    break ModuleSwitch;

                case Types.CONTACT:
                    handleContactCommonEvent(commonEvent, context, logBuilder);
                    break ModuleSwitch;

                case Types.TASK:
                    handleTaskCommonEvent(commonEvent, context, logBuilder);
                    break ModuleSwitch;

                case Types.INFOSTORE:
                    handleInfostoreCommonEvent(commonEvent, context, logBuilder);
                    break ModuleSwitch;

                case Types.FOLDER:
                    handleFolderCommonEvent(commonEvent, context, logBuilder);
                    break ModuleSwitch;
            }
        }
    }

    /**
     * Handles the general information of a CommonEvent that should be logged for all action objects.
     *
     * @param commonEvent
     * @param logBuilder
     */
    protected void handleMainCommmonEvent(CommonEvent commonEvent, StringBuilder logBuilder) {
        Validate.notNull(commonEvent, "CommonEvent mustn't be null.");
        Validate.notNull(logBuilder, "StringBuilder to write to mustn't be null.");

        if (commonEvent.getAction() == CommonEvent.INSERT) {
            logBuilder.append("EVENT TYPE: INSERT; ");
        } else if (commonEvent.getAction() == CommonEvent.UPDATE) {
            logBuilder.append("EVENT TYPE: UPDATE; ");
        } else if (commonEvent.getAction() == CommonEvent.DELETE) {
            logBuilder.append("EVENT TYPE: DELETE; ");
        }

        synchronized (logDateFormat) {
            logBuilder.append("EVENT TIME: ").append(logDateFormat.format(new Date())).append("; ");
        }
    }

    /**
     * Handles appointment events.
     *
     * @param event - the {@link CommonEvent} that was received
     * @param context - the {@link Context}
     * @param logBuilder - the log to add information
     * @throws OXException
     */
    protected void handleFolderCommonEvent(CommonEvent commonEvent, Context context, StringBuilder logBuilder) throws OXException {
        Validate.notNull(commonEvent, "CommonEvent mustn't be null.");
        Validate.notNull(logBuilder, "StringBuilder to write to mustn't be null.");

        final FolderObject folder = (FolderObject) commonEvent.getActionObj();

        logBuilder.append("OBJECT TYPE: FOLDER; ");
        appendUserInformation(commonEvent.getUserId(), commonEvent.getContextId(), logBuilder);
        logBuilder.append("CONTEXT ID: ").append(commonEvent.getContextId()).append("; ");
        logBuilder.append("FOLDER ID: ").append(folder.getObjectID()).append("; ");
        logBuilder.append("CREATED BY: ").append(userService.getUser(folder.getCreatedBy(), context).getDisplayName()).append("; ");
        logBuilder.append("MODIFIED BY: ").append(userService.getUser(folder.getModifiedBy(), context).getDisplayName()).append("; ");
        logBuilder.append("NAME: ").append(folder.getFolderName()).append("; ");
    }

    /**
     * Handles appointment events.
     *
     * @param event - the {@link CommonEvent} that was received
     * @param context - the {@link Context}
     * @param logBuilder - the log to add information
     * @throws OXException
     */
    protected void handleAppointmentCommonEvent(CommonEvent commonEvent, Context context, StringBuilder logBuilder) throws OXException {
        Validate.notNull(commonEvent, "CommonEvent mustn't be null.");
        Validate.notNull(logBuilder, "StringBuilder to write to mustn't be null.");

        final Appointment appointment = (Appointment) commonEvent.getActionObj();
        Appointment oldAppointment = (Appointment) commonEvent.getOldObj();

        logBuilder.append("OBJECT TYPE: APPOINTMENT; ");
        appendUserInformation(commonEvent.getUserId(), commonEvent.getContextId(), logBuilder);
        logBuilder.append("CONTEXT ID: ").append(commonEvent.getContextId()).append("; ");
        logBuilder.append("OBJECT ID: ").append(appointment.getObjectID()).append("; ");
        {
            int createdBy = appointment.getCreatedBy();
            if (createdBy > 0) {
                try {
                    logBuilder.append("CREATED BY: ").append(userService.getUser(createdBy, context).getDisplayName()).append("; ");
                } catch (OXException e) {
                    logger.debug("Failed to load user {} in context {}", createdBy, context.getContextId(), e);
                    logBuilder.append("CREATED BY: <unknown>; ");
                }
            } else {
                logBuilder.append("CREATED BY: <unknown>; ");
            }
        }
        {
            int modifiedBy = appointment.getModifiedBy();
            if (modifiedBy > 0) {
                try {
                    logBuilder.append("MODIFIED BY: ").append(userService.getUser(modifiedBy, context).getDisplayName()).append("; ");
                } catch (OXException e) {
                    logger.debug("Failed to load user {} in context {}", modifiedBy, context.getContextId(), e);
                    logBuilder.append("MODIFIED BY: <unknown>; ");
                }
            } else {
                logBuilder.append("MODIFIED BY: <unknown>; ");
            }
        }
        logBuilder.append("TITLE: ").append(appointment.getTitle()).append("; ");
        logBuilder.append("START DATE: ").append(appointment.getStartDate()).append("; ");
        logBuilder.append("END DATE: ").append(appointment.getEndDate()).append("; ");
        logBuilder.append("FOLDER: ").append(getPathToRoot(appointment.getParentFolderID(), commonEvent.getSession())).append("; ");
        logBuilder.append("PARTICIPANTS: ").append(Arrays.toString(appointment.getParticipants())).append("; ");
        if (oldAppointment != null) {
            logBuilder.append("OLD PARTICIPANTS: ").append(Arrays.toString(oldAppointment.getParticipants())).append("; ");
        }
        if (commonEvent.getSession() != null) {
            logBuilder.append("CLIENT: ").append(commonEvent.getSession().getClient()).append("; ");
        }
    }

    /**
     * Handles contact events.
     *
     * @param event - the {@link CommonEvent} that was received
     * @param context - the {@link Context}
     * @param logBuilder - the log to add information
     * @throws OXException
     */
    protected void handleContactCommonEvent(CommonEvent commonEvent, Context context, StringBuilder logBuilder) throws OXException {
        Validate.notNull(commonEvent, "CommonEvent mustn't be null.");
        Validate.notNull(logBuilder, "StringBuilder to write to mustn't be null.");
        Contact contact = extractContact(commonEvent);
        logBuilder.append("OBJECT TYPE: CONTACT; ");
        appendUserInformation(commonEvent.getUserId(), commonEvent.getContextId(), logBuilder);
        logBuilder.append("CONTEXT ID: ").append(commonEvent.getContextId()).append("; ");
        if (null != contact) {
            logBuilder.append("OBJECT ID: ").append(contact.getObjectID()).append("; ");
            if (contact.containsCreatedBy()) {
                logBuilder.append("CREATED BY: ").append(
                    userService.getUser(contact.getCreatedBy(), context).getDisplayName()).append("; ");
            }
            if (contact.containsModifiedBy()) {
                logBuilder.append("MODIFIED BY: ").append(
                    userService.getUser(contact.getModifiedBy(), context).getDisplayName()).append("; ");
            }
            logBuilder.append("CONTACT FULLNAME: ").append(contact.getDisplayName()).append(';');
            logBuilder.append("FOLDER: ").append(getPathToRoot(contact.getParentFolderID(), commonEvent.getSession())).append(';');
        }
    }

    /**
     * Extracts the contact from the supplied common event.
     *
     * @param commonEvent The common event
     * @return The extracted contact, either as supplied by the event itself, or re-fetched from the contact service
     * @throws OXException
     */
    private Contact extractContact(CommonEvent commonEvent) throws OXException {
        Contact contact = (Contact) commonEvent.getActionObj();
        if (CommonEvent.DELETE != commonEvent.getAction() && null != commonEvent.getSession() && (
            null == contact || false == contact.containsDisplayName() || false == contact.containsCreatedBy() ||
                false == contact.containsModifiedBy() || false == contact.containsObjectID() || false == contact.containsParentFolderID())) {
            /*
             * try and get more details
             */
            ContactService contactService = Services.getService(ContactService.class);
            if (null != contactService) {
                ContactField[] requestedFields = {
                    ContactField.DISPLAY_NAME, ContactField.FOLDER_ID, ContactField.CREATED_BY, ContactField.MODIFIED_BY
                };
                return contactService.getContact(commonEvent.getSession(), String.valueOf(contact.getParentFolderID()),
                    String.valueOf(contact.getObjectID()), requestedFields);
            }
        }
        return contact;
    }

    /**
     * Handles task events.
     *
     * @param event - the {@link CommonEvent} that was received
     * @param context - the {@link Context}
     * @param logBuilder - the log to add information
     * @throws OXException
     */
    protected void handleTaskCommonEvent(CommonEvent commonEvent, Context context, StringBuilder logBuilder) throws OXException {
        Validate.notNull(commonEvent, "CommonEvent mustn't be null.");
        Validate.notNull(logBuilder, "StringBuilder to write to mustn't be null.");

        final Task task = (Task) commonEvent.getActionObj();

        logBuilder.append("OBJECT TYPE: TASK; ");
        appendUserInformation(commonEvent.getUserId(), commonEvent.getContextId(), logBuilder);
        logBuilder.append("CONTEXT ID: ").append(commonEvent.getContextId()).append("; ");
        logBuilder.append("OBJECT ID: ").append(task.getObjectID()).append("; ");
        logBuilder.append("CREATED BY: ").append(userService.getUser(task.getCreatedBy(), context).getDisplayName()).append("; ");
        logBuilder.append("MODIFIED BY: ").append(userService.getUser(task.getModifiedBy(), context).getDisplayName()).append("; ");
        logBuilder.append("TITLE: ").append(task.getTitle()).append("; ");
        logBuilder.append("FOLDER: ").append(getPathToRoot(task.getParentFolderID(), commonEvent.getSession())).append(';');
    }

    /**
     * Handles infostore events.
     *
     * @param event - the {@link CommonEvent} that was received
     * @param context - the {@link Context}
     * @param logBuilder - the log to add information
     * @throws OXException
     */
    protected void handleInfostoreCommonEvent(CommonEvent commonEvent, Context context, StringBuilder logBuilder) throws OXException {
        Validate.notNull(commonEvent, "CommonEvent mustn't be null.");
        Validate.notNull(logBuilder, "StringBuilder to write to mustn't be null.");

        final DocumentMetadata document = (DocumentMetadata) commonEvent.getActionObj();

        logBuilder.append("OBJECT TYPE: INFOSTORE; ");
        appendUserInformation(commonEvent.getUserId(), commonEvent.getContextId(), logBuilder);
        logBuilder.append("CONTEXT ID: ").append(commonEvent.getContextId()).append("; ");
        logBuilder.append("OBJECT ID: ").append(document.getId()).append("; ");
        logBuilder.append("CREATED BY: ").append(userService.getUser(document.getCreatedBy(), context).getDisplayName()).append("; ");
        logBuilder.append("MODIFIED BY: ").append(userService.getUser(document.getModifiedBy(), context).getDisplayName()).append("; ");
        logBuilder.append("TITLE: ").append(document.getTitle()).append("; ");
        logBuilder.append("TITLE: ").append(document.getFileName()).append("; ");
        logBuilder.append("FOLDER: ").append(getPathToRoot((int) document.getFolderId(), commonEvent.getSession())).append(';');
    }

    /**
     * This method will return the full folder path as String.
     *
     * @param folderId
     * @param sessionObj
     * @return String fullFolderPath
     */
    protected String getPathToRoot(final int folderId, final Session sessionObj) {
        String retval = "";

        try {
            final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(sessionObj));
            final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getPathToRoot(folderId)).asQueue();
            final int size = q.size();
            final Iterator<FolderObject> iter = q.iterator();
            for (int i = 0; i < size; i++) {
                retval = iter.next().getFolderName() + "/" + retval;
            }
        } catch (final OXException e) {
            logger.error("", e);
        }

        return retval;
    }

    private void appendUserInformation(final int userId, final int contextId, final StringBuilder logBuilder) {
        String displayName;
        try {
            displayName = userService.getUser(userId, ContextStorage.getInstance().getContext(contextId)).getDisplayName();
        } catch (final Exception e) {
            // Ignore
            displayName = null;
        }
        logBuilder.append("USER: ");
        if (null == displayName) {
            logBuilder.append(userId);
        } else {
            logBuilder.append(displayName);
            logBuilder.append(" (").append(userId).append(')');
        }
        logBuilder.append("; ");
    }

}
