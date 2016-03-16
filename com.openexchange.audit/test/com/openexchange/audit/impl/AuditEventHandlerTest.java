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

import java.sql.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.Event;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.audit.configuration.AuditConfiguration;
import com.openexchange.event.CommonEvent;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.user.UserService;

/**
 * Unit tests for {@link AuditEventHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Event.class, UserStorage.class, AuditConfiguration.class, ContextStorage.class })
public class AuditEventHandlerTest {

    /**
     * Class under test
     */
    private AuditEventHandler auditEventHandler;

    /**
     * Mock for the event to handle
     */
    private Event event;

    /**
     * Mock for the commonEvent to handle
     */
    private CommonEvent commonEvent;

    /**
     * Mock for the context
     */
    private Context context;

    /**
     * Mock for the logger
     */
    private org.slf4j.Logger log;

    /**
     * StringBuilder
     */
    private StringBuilder stringBuilder;

    private final int userId = 9999;

    private final int contextId = 111111;

    private final int objectId = 555555555;

    private final String objectTitle = "theObjectTitle";

    private final Date date = new Date(2011, 12, 12);

    private Contact contact;

    private UserService userService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(AuditConfiguration.class);
        PowerMockito.mockStatic(UserStorage.class);
        PowerMockito.mockStatic(ContextStorage.class);

        userService = PowerMockito.mock(UserService.class);
        User user = PowerMockito.mock(com.openexchange.groupware.ldap.User.class);
        PowerMockito.when(user.getDisplayName()).thenReturn(this.objectTitle);
        PowerMockito.when(userService.getUser(Matchers.anyInt(), (Context) Matchers.any())).thenReturn(user);

        this.contact = PowerMockito.mock(Contact.class);
        this.event = PowerMockito.mock(Event.class);
        this.commonEvent = PowerMockito.mock(CommonEvent.class);
        this.context = PowerMockito.mock(Context.class);
        this.log = PowerMockito.mock(org.slf4j.Logger.class);

        this.stringBuilder = new StringBuilder();
    }

    @Test
    public void testGetInstance_Fine_ReturnInstance() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        Assert.assertNotNull(this.auditEventHandler);
    }

    @Test
    public void testHandleEvent_InfoLoggingDisabled_Return() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        PowerMockito.when(log.isInfoEnabled()).thenReturn(false);
        MockUtils.injectValueIntoPrivateField(this.auditEventHandler, "logger", log);

        this.auditEventHandler.handleEvent(event);

        Mockito.verify(log, Mockito.never()).info(Mockito.anyString());
    }

    @Test
    public void testHandleEvent_InfoLoggingEnabledButWrongEvent_NothingToWrite() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        PowerMockito.when(log.isInfoEnabled()).thenReturn(true);
        MockUtils.injectValueIntoPrivateField(this.auditEventHandler, "logger", log);
        PowerMockito.when(this.event.getTopic()).thenReturn("topicOfAnyOtherEvent");

        this.auditEventHandler.handleEvent(event);

        Mockito.verify(log, Mockito.never()).info(Mockito.anyString());
    }

    @Test
    public void testHandleEvent_IsInfoStoreEventButLogEmpty_NotLogged() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleInfostoreEvent(Event event, StringBuilder log) {
                return;
            }
        };

        PowerMockito.when(log.isInfoEnabled()).thenReturn(true);
        MockUtils.injectValueIntoPrivateField(this.auditEventHandler, "logger", log);
        PowerMockito.when(this.event.getTopic()).thenReturn("com/openexchange/groupware/infostore/");

        this.auditEventHandler.handleEvent(event);

        Mockito.verify(log, Mockito.never()).info(Mockito.anyString());
    }

    @Test
    public void testHandleEvent_IsGroupwareEventButLogEmpty_NotLogged() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleGroupwareEvent(Event event, StringBuilder log) {
                return;
            }
        };

        PowerMockito.when(log.isInfoEnabled()).thenReturn(true);
        MockUtils.injectValueIntoPrivateField(this.auditEventHandler, "logger", log);
        PowerMockito.when(this.event.getTopic()).thenReturn("com/openexchange/groupware/");

        this.auditEventHandler.handleEvent(event);

        Mockito.verify(log, Mockito.never()).info(Mockito.anyString());
    }

    @Test
    public void testHandleEvent_IsInfoStoreEventAndLogNotEmpty_Logged() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleInfostoreEvent(Event event, StringBuilder log) {
                log.append("isInfostoreEvent");
                return;
            }
        };

        PowerMockito.when(log.isInfoEnabled()).thenReturn(true);
        MockUtils.injectValueIntoPrivateField(this.auditEventHandler, "logger", log);
        PowerMockito.when(this.event.getTopic()).thenReturn("com/openexchange/groupware/infostore/");

        this.auditEventHandler.handleEvent(event);

        Mockito.verify(log, Mockito.times(1)).info("isInfostoreEvent");
    }

    @Test
    public void testHandleEvent_IsGroupwareEventAndLogNotEmpty_Logged() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleGroupwareEvent(Event event, StringBuilder log) {
                log.append("isGroupwareEvent");
                return;
            }
        };

        PowerMockito.when(log.isInfoEnabled()).thenReturn(true);
        MockUtils.injectValueIntoPrivateField(this.auditEventHandler, "logger", log);
        PowerMockito.when(this.event.getTopic()).thenReturn("com/openexchange/groupware/");

        this.auditEventHandler.handleEvent(event);

        Mockito.verify(log, Mockito.times(1)).info("isGroupwareEvent");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleMainCommmonEvent_CommonEventNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleMainCommmonEvent(null, stringBuilder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleMainCommmonEvent_StringBuilderNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleMainCommmonEvent(commonEvent, null);
    }

    @Test
    public void testHandleMainCommmonEvent_EventInsert_AddInsertToLog() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        Mockito.when(commonEvent.getAction()).thenReturn(CommonEvent.INSERT);

        this.auditEventHandler.handleMainCommmonEvent(commonEvent, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TYPE: INSERT; "));
    }

    @Test
    public void testHandleMainCommmonEvent_EventDelete_AddDeleteToLog() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        Mockito.when(commonEvent.getAction()).thenReturn(CommonEvent.DELETE);

        this.auditEventHandler.handleMainCommmonEvent(commonEvent, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TYPE: DELETE; "));
    }

    @Test
    public void testHandleMainCommmonEvent_EventUpdate_AddUpdateToLog() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        Mockito.when(commonEvent.getAction()).thenReturn(CommonEvent.UPDATE);

        this.auditEventHandler.handleMainCommmonEvent(commonEvent, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TYPE: UPDATE; "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleAppointmentCommmonEvent_CommonEventNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleAppointmentCommonEvent(null, context, stringBuilder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleAppointmentCommmonEvent_StringBuilderNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleAppointmentCommonEvent(commonEvent, context, null);
    }

    @Test
    public void testHandleAppointmentCommonEvent_EverythingFine_LogStartWithCorrect() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Appointment appointment = PowerMockito.mock(Appointment.class);
        Mockito.when(commonEvent.getActionObj()).thenReturn(appointment);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(appointment.getObjectID()).thenReturn(this.objectId);
        Mockito.when(appointment.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(appointment.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(appointment.getTitle()).thenReturn(this.objectTitle);
        Mockito.when(appointment.getStartDate()).thenReturn(this.date);
        Mockito.when(appointment.getEndDate()).thenReturn(this.date);

        this.auditEventHandler.handleAppointmentCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("OBJECT TYPE: APPOINTMENT; "));
    }

    @Test
    public void testHandleAppointmentCommonEvent_EverythingFine_ContainsAllInformation() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Appointment appointment = PowerMockito.mock(Appointment.class);
        Mockito.when(commonEvent.getActionObj()).thenReturn(appointment);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(appointment.getObjectID()).thenReturn(this.objectId);
        Mockito.when(appointment.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(appointment.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(appointment.getTitle()).thenReturn(this.objectTitle);
        Mockito.when(appointment.getStartDate()).thenReturn(this.date);
        Mockito.when(appointment.getEndDate()).thenReturn(this.date);

        this.auditEventHandler.handleAppointmentCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().contains("CONTEXT ID: " + this.contextId));
        Assert.assertTrue(stringBuilder.toString().contains("END DATE: " + this.date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleContactCommmonEvent_CommonEventNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleContactCommonEvent(null, context, stringBuilder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleContactCommmonEvent_StringBuilderNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleContactCommonEvent(commonEvent, context, null);
    }

    @Test
    public void testHandleContactCommonEvent_EverythingFine_LogStartWithCorrect() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Mockito.when(commonEvent.getActionObj()).thenReturn(contact);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(contact.getObjectID()).thenReturn(this.objectId);
        Mockito.when(contact.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(contact.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(contact.getTitle()).thenReturn(this.objectTitle);

        this.auditEventHandler.handleContactCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("OBJECT TYPE: CONTACT; "));
    }

    @Test
    public void testHandleContactCommonEvent_EverythingFine_ContainsDesiredInformation() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Mockito.when(commonEvent.getActionObj()).thenReturn(contact);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(contact.getObjectID()).thenReturn(this.objectId);
        Mockito.when(contact.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(contact.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(contact.getDisplayName()).thenReturn(this.objectTitle);

        this.auditEventHandler.handleContactCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().contains("OBJECT ID: " + this.objectId));
        Assert.assertTrue(stringBuilder.toString().contains("CONTACT FULLNAME: " + this.objectTitle));
        Assert.assertFalse(stringBuilder.toString().contains("MODIFIED BY: " + this.objectTitle));
    }

    @Test
    public void testHandleContactCommonEvent_EverythingFine_ContainsAllInformation() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Mockito.when(commonEvent.getActionObj()).thenReturn(contact);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(contact.getObjectID()).thenReturn(this.objectId);
        Mockito.when(contact.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(contact.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(contact.getDisplayName()).thenReturn(this.objectTitle);
        Mockito.when(contact.containsCreatedBy()).thenReturn(true);
        Mockito.when(contact.containsModifiedBy()).thenReturn(true);

        this.auditEventHandler.handleContactCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().contains("OBJECT ID: " + this.objectId));
        Assert.assertTrue(stringBuilder.toString().contains("CONTACT FULLNAME: " + this.objectTitle));
        Assert.assertTrue(stringBuilder.toString().contains("CREATED BY: " + this.objectTitle));
        Assert.assertTrue(stringBuilder.toString().contains("MODIFIED BY: " + this.objectTitle));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleTaskCommmonEvent_CommonEventNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleTaskCommonEvent(null, context, stringBuilder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleTaskCommmonEvent_StringBuilderNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleTaskCommonEvent(commonEvent, context, null);
    }

    @Test
    public void testHandleTaskCommonEvent_EverythingFine_LogStartWithCorrect() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Task task = PowerMockito.mock(Task.class);
        Mockito.when(commonEvent.getActionObj()).thenReturn(task);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(task.getObjectID()).thenReturn(this.objectId);
        Mockito.when(task.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(task.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(task.getTitle()).thenReturn(this.objectTitle);

        this.auditEventHandler.handleTaskCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("OBJECT TYPE: TASK; "));
    }

    @Test
    public void testHandleTaskCommonEvent_EverythingFine_ContainsAllInformation() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Task task = PowerMockito.mock(Task.class);
        Mockito.when(commonEvent.getActionObj()).thenReturn(task);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(task.getObjectID()).thenReturn(this.objectId);
        Mockito.when(task.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(task.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(task.getTitle()).thenReturn(this.objectTitle);

        this.auditEventHandler.handleTaskCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().contains("OBJECT ID: " + this.objectId));
        Assert.assertTrue(stringBuilder.toString().contains("TITLE: " + this.objectTitle));
        Assert.assertFalse(stringBuilder.toString().contains("MODIFIED BY: " + this.userId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleInfostoreCommmonEvent_CommonEventNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleInfostoreCommonEvent(null, context, stringBuilder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleInfostoreCommmonEvent_StringBuilderNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleInfostoreCommonEvent(commonEvent, context, null);
    }

    @Test
    public void testHandleInfostoreCommonEvent_EverythingFine_LogStartWithCorrect() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        DocumentMetadata documentMetadata = PowerMockito.mock(DocumentMetadata.class);
        Mockito.when(commonEvent.getActionObj()).thenReturn(documentMetadata);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(documentMetadata.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(documentMetadata.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(documentMetadata.getTitle()).thenReturn(this.objectTitle);

        this.auditEventHandler.handleInfostoreCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("OBJECT TYPE: INFOSTORE; "));
    }

    @Test
    public void testHandleInfostoreCommonEvent_EverythingFine_ContainsAllInformation() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        DocumentMetadata documentMetadata = PowerMockito.mock(DocumentMetadata.class);
        Mockito.when(commonEvent.getActionObj()).thenReturn(documentMetadata);
        Mockito.when(commonEvent.getContextId()).thenReturn(this.contextId);
        Mockito.when(commonEvent.getUserId()).thenReturn(this.userId);
        Mockito.when(documentMetadata.getCreatedBy()).thenReturn(this.userId);
        Mockito.when(documentMetadata.getModifiedBy()).thenReturn(this.userId);
        Mockito.when(documentMetadata.getTitle()).thenReturn(this.objectTitle);
        Mockito.when(documentMetadata.getId()).thenReturn(this.objectId);

        this.auditEventHandler.handleInfostoreCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().contains("OBJECT ID: " + this.objectId));
        Assert.assertTrue(stringBuilder.toString().contains("TITLE: " + this.objectTitle));
        Assert.assertFalse(stringBuilder.toString().contains("MODIFIED BY: " + this.userId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleInfostoreEvent_EventNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleInfostoreEvent(null, stringBuilder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleInfostoreEvent_StringBuilderNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleInfostoreEvent(event, null);
    }

    @Test
    public void testHandleInfostoreEvent_TopicNotRelevant_OnlyAppendDefault() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        PowerMockito.when(event.getTopic()).thenReturn(this.objectTitle);
        Session session = PowerMockito.mock(Session.class);
        PowerMockito.when(session.getParameter(Matchers.anyString())).thenReturn(Boolean.FALSE);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SESSION)).thenReturn(session);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.OBJECT_ID)).thenReturn(this.objectId);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SERVICE)).thenReturn(this.objectTitle);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.ACCOUNT_ID)).thenReturn(this.userId);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.FOLDER_ID)).thenReturn(this.objectTitle);

        this.auditEventHandler.handleInfostoreEvent(event, stringBuilder);

        Assert.assertFalse(stringBuilder.toString().startsWith("EVENT TYPE:"));
        Assert.assertFalse(stringBuilder.toString().contains("PUBLISH: "));
        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TIME: "));
        Assert.assertTrue(stringBuilder.toString().contains("FOLDER: " + this.objectTitle));
        Assert.assertTrue(stringBuilder.toString().contains("SERVICE ID: " + this.objectTitle));
    }

    @Test
    public void testHandleInfostoreEvent_TopicRelevantAndPublication_AppendLocalIp() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        PowerMockito.when(event.getTopic()).thenReturn(FileStorageEventConstants.UPDATE_TOPIC);
        Session session = PowerMockito.mock(Session.class);
        PowerMockito.when(session.getParameter(Matchers.anyString())).thenReturn(Boolean.TRUE);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SESSION)).thenReturn(session);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.OBJECT_ID)).thenReturn(this.objectId);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SERVICE)).thenReturn(this.objectTitle);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.ACCOUNT_ID)).thenReturn(this.userId);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.FOLDER_ID)).thenReturn(this.objectTitle);

        this.auditEventHandler.handleInfostoreEvent(event, stringBuilder);

        Assert.assertFalse(stringBuilder.toString().startsWith("EVENT TYPE: ACCESS; "));
        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TYPE: UPDATE; "));
        Assert.assertTrue(stringBuilder.toString().contains("PUBLISH: " + "unknown"));
    }

    @Test
    public void testHandleInfostoreEvent_AccessTopic_AppendLocalIp() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        PowerMockito.when(AuditConfiguration.getFileAccessLogging()).thenReturn(Boolean.TRUE);

        PowerMockito.when(event.getTopic()).thenReturn(FileStorageEventConstants.ACCESS_TOPIC);
        Session session = PowerMockito.mock(Session.class);
        PowerMockito.when(session.getParameter(Matchers.anyString())).thenReturn(Boolean.TRUE);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SESSION)).thenReturn(session);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.OBJECT_ID)).thenReturn(this.objectId);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SERVICE)).thenReturn(this.objectTitle);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.ACCOUNT_ID)).thenReturn(this.userId);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.FOLDER_ID)).thenReturn(this.objectTitle);
        PowerMockito.when(event.getProperty("remoteAddress")).thenReturn("172.16.13.71");

        this.auditEventHandler.handleInfostoreEvent(event, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TYPE: ACCESS; "));
        Assert.assertTrue(stringBuilder.toString().contains("PUBLISH: " + "172.16.13.71"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleGroupwareEvent_EventNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleGroupwareEvent(null, stringBuilder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleGroupwareEvent_StringBuilderNull_ThrowException() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        this.auditEventHandler.handleGroupwareEvent(event, null);
    }

    @Test
    public void testHandleGroupwareEvent_CommonEventNull_Return() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleMainCommmonEvent(CommonEvent commonEvent, StringBuilder log) {
                return;
            }
        };

        PowerMockito.when(event.getProperty(CommonEvent.EVENT_KEY)).thenReturn(null);

        this.auditEventHandler.handleGroupwareEvent(event, stringBuilder);
    }

    @Test
    public void testHandleGroupwareEvent_TypeAppointment_InvokeAppointment() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleMainCommmonEvent(CommonEvent commonEvent, StringBuilder log) {
                return;
            }

            @Override
            protected void handleAppointmentCommonEvent(CommonEvent commonEvent, Context context, StringBuilder log) {
                return;
            }
        };

        PowerMockito.when(event.getProperty(CommonEvent.EVENT_KEY)).thenReturn(this.commonEvent);
        PowerMockito.when(this.commonEvent.getContextId()).thenReturn(this.contextId);
        PowerMockito.when(this.commonEvent.getModule()).thenReturn(Types.APPOINTMENT);
        ContextStorage contextStorage = PowerMockito.mock(ContextStorage.class);
        PowerMockito.when(contextStorage.getContext(this.contextId)).thenReturn(this.context);

        PowerMockito.when(ContextStorage.getInstance()).thenReturn(contextStorage);

        this.auditEventHandler.handleGroupwareEvent(event, stringBuilder);
    }

    @Test
    public void testHandleGroupwareEvent_TypeContact_InvokeContact() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleMainCommmonEvent(CommonEvent commonEvent, StringBuilder log) {
                return;
            }

            @Override
            protected void handleContactCommonEvent(CommonEvent commonEvent, Context context, StringBuilder log) {
                return;
            }
        };

        PowerMockito.when(event.getProperty(CommonEvent.EVENT_KEY)).thenReturn(this.commonEvent);
        PowerMockito.when(this.commonEvent.getContextId()).thenReturn(this.contextId);
        PowerMockito.when(this.commonEvent.getModule()).thenReturn(Types.CONTACT);
        ContextStorage contextStorage = PowerMockito.mock(ContextStorage.class);
        PowerMockito.when(contextStorage.getContext(this.contextId)).thenReturn(this.context);

        PowerMockito.when(ContextStorage.getInstance()).thenReturn(contextStorage);

        this.auditEventHandler.handleGroupwareEvent(event, stringBuilder);
    }

    @Test
    public void testHandleGroupwareEvent_TypeTask_InvokeTask() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleMainCommmonEvent(CommonEvent commonEvent, StringBuilder log) {
                return;
            }

            @Override
            protected void handleTaskCommonEvent(CommonEvent commonEvent, Context context, StringBuilder log) {
                return;
            }
        };

        PowerMockito.when(event.getProperty(CommonEvent.EVENT_KEY)).thenReturn(this.commonEvent);
        PowerMockito.when(this.commonEvent.getContextId()).thenReturn(this.contextId);
        PowerMockito.when(this.commonEvent.getModule()).thenReturn(Types.TASK);
        ContextStorage contextStorage = PowerMockito.mock(ContextStorage.class);
        PowerMockito.when(contextStorage.getContext(this.contextId)).thenReturn(this.context);

        PowerMockito.when(ContextStorage.getInstance()).thenReturn(contextStorage);

        this.auditEventHandler.handleGroupwareEvent(event, stringBuilder);
    }

    @Test
    public void testHandleGroupwareEvent_TypeInfostore_InvokeInfostore() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected void handleMainCommmonEvent(CommonEvent commonEvent, StringBuilder log) {
                return;
            }

            @Override
            protected void handleInfostoreCommonEvent(CommonEvent commonEvent, Context context, StringBuilder log) {
                return;
            }
        };

        PowerMockito.when(event.getProperty(CommonEvent.EVENT_KEY)).thenReturn(this.commonEvent);
        PowerMockito.when(this.commonEvent.getContextId()).thenReturn(this.contextId);
        PowerMockito.when(this.commonEvent.getModule()).thenReturn(Types.INFOSTORE);
        ContextStorage contextStorage = PowerMockito.mock(ContextStorage.class);
        PowerMockito.when(contextStorage.getContext(this.contextId)).thenReturn(this.context);

        PowerMockito.when(ContextStorage.getInstance()).thenReturn(contextStorage);

        this.auditEventHandler.handleGroupwareEvent(event, stringBuilder);
    }

}
