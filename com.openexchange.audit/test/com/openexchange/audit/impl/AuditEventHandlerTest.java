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

package com.openexchange.audit.impl;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import org.dmfs.rfc5545.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.Event;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.audit.configuration.AuditConfiguration;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.event.CommonEvent;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;
import com.openexchange.test.mock.MockUtils;
import com.openexchange.user.User;
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

    private final DateTime date = new DateTime(System.currentTimeMillis());

    private Contact contact;

    private UserService userService;

    private CalendarUser calendarUser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(AuditConfiguration.class);
        PowerMockito.mockStatic(UserStorage.class);
        PowerMockito.mockStatic(ContextStorage.class);

        userService = PowerMockito.mock(UserService.class);
        User user = PowerMockito.mock(com.openexchange.user.User.class);
        PowerMockito.when(user.getDisplayName()).thenReturn(this.objectTitle);
        PowerMockito.when(userService.getUser(ArgumentMatchers.anyInt(), (Context) ArgumentMatchers.any())).thenReturn(user);

        this.contact = PowerMockito.mock(Contact.class);
        this.event = PowerMockito.mock(Event.class);
        this.commonEvent = PowerMockito.mock(CommonEvent.class);
        this.context = PowerMockito.mock(Context.class);
        this.log = PowerMockito.mock(org.slf4j.Logger.class);

        this.calendarUser = new CalendarUser();
        this.calendarUser.setEntity(userId);
        this.calendarUser.setCn("user name");

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

        PowerMockito.when(B(log.isInfoEnabled())).thenReturn(Boolean.FALSE);
        MockUtils.injectValueIntoPrivateField(this.auditEventHandler, "logger", log);

        this.auditEventHandler.handleEvent(event);

        Mockito.verify(log, Mockito.never()).info(Mockito.anyString());
    }

    @Test
    public void testHandleEvent_InfoLoggingEnabledButWrongEvent_NothingToWrite() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        PowerMockito.when(B(log.isInfoEnabled())).thenReturn(Boolean.TRUE);
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

        PowerMockito.when(B(log.isInfoEnabled())).thenReturn(Boolean.TRUE);
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

        PowerMockito.when(B(log.isInfoEnabled())).thenReturn(Boolean.TRUE);
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

        PowerMockito.when(B(log.isInfoEnabled())).thenReturn(Boolean.TRUE);
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

        PowerMockito.when(B(log.isInfoEnabled())).thenReturn(Boolean.TRUE);
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

        Mockito.when(I(commonEvent.getAction())).thenReturn(I(CommonEvent.INSERT));

        this.auditEventHandler.handleMainCommmonEvent(commonEvent, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TYPE: INSERT; "));
    }

    @Test
    public void testHandleMainCommmonEvent_EventDelete_AddDeleteToLog() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        Mockito.when(I(commonEvent.getAction())).thenReturn(I(CommonEvent.DELETE));

        this.auditEventHandler.handleMainCommmonEvent(commonEvent, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TYPE: DELETE; "));
    }

    @Test
    public void testHandleMainCommmonEvent_EventUpdate_AddUpdateToLog() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        Mockito.when(I(commonEvent.getAction())).thenReturn(I(CommonEvent.UPDATE));

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

        Mockito.when(userService.getUser(userId, context).getDisplayName()).thenReturn("TestUser");

        com.openexchange.chronos.Event event = PowerMockito.mock(com.openexchange.chronos.Event.class);
        Mockito.when(I(commonEvent.getAction())).thenReturn(I(CommonEvent.INSERT));
        Mockito.when(commonEvent.getActionObj()).thenReturn(event);
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(event.getId()).thenReturn(String.valueOf(this.objectId));
        Mockito.when(event.getCreatedBy()).thenReturn(this.calendarUser);
        Mockito.when(event.getModifiedBy()).thenReturn(this.calendarUser);
        Mockito.when(event.getSummary()).thenReturn(this.objectTitle);
        Mockito.when(event.getStartDate()).thenReturn(this.date);
        Mockito.when(event.getEndDate()).thenReturn(this.date);
        Mockito.when(event.getAttendees()).thenReturn(Collections.singletonList(new Attendee()));

        this.auditEventHandler.handleAppointmentCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("OBJECT TYPE: EVENT; "));
    }

    @Test
    public void testHandleAppointmentCommonEvent_EverythingFine_ContainsAllInformation() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Mockito.when(userService.getUser(userId, context).getDisplayName()).thenReturn("TestUser");

        com.openexchange.chronos.Event event = PowerMockito.mock(com.openexchange.chronos.Event.class);
        Mockito.when(I(commonEvent.getAction())).thenReturn(I(CommonEvent.INSERT));
        Mockito.when(commonEvent.getActionObj()).thenReturn(event);
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(event.getId()).thenReturn(String.valueOf(this.objectId));
        Mockito.when(event.getCreatedBy()).thenReturn(this.calendarUser);
        Mockito.when(event.getModifiedBy()).thenReturn(this.calendarUser);
        Mockito.when(event.getSummary()).thenReturn(this.objectTitle);
        Mockito.when(event.getStartDate()).thenReturn(this.date);
        Mockito.when(event.getEndDate()).thenReturn(this.date);
        Attendee attendee = new Attendee();
        attendee.setCn("InvitedTestUser");
        Mockito.when(event.getAttendees()).thenReturn(Collections.singletonList(attendee));

        this.auditEventHandler.handleAppointmentCommonEvent(commonEvent, context, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().contains("CONTEXT ID: " + this.contextId));
        Assert.assertTrue(stringBuilder.toString().contains("END DATE: " + this.date));
    }

    @Test
    public void testHandleAppointmentCommonEvent_EverythingFine() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService) {

            @Override
            protected String getPathToRoot(int folderId, Session sessionObj) {
                return "";
            }
        };

        Mockito.when(userService.getUser(userId, context).getDisplayName()).thenReturn("TestUser");

        com.openexchange.chronos.Event event = PowerMockito.mock(com.openexchange.chronos.Event.class);
        Mockito.when(I(commonEvent.getAction())).thenReturn(I(CommonEvent.DELETE));
        Mockito.when(commonEvent.getActionObj()).thenReturn(event);
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(event.getId()).thenReturn(String.valueOf(this.objectId));
        Mockito.when(event.getCreatedBy()).thenReturn(this.calendarUser);
        Mockito.when(event.getModifiedBy()).thenReturn(this.calendarUser);
        Mockito.when(event.getSummary()).thenReturn(this.objectTitle);
        Mockito.when(event.getStartDate()).thenReturn(this.date);
        Mockito.when(event.getEndDate()).thenReturn(this.date);
        Attendee attendee = new Attendee();
        attendee.setCn("InvitedTestUser");
        Mockito.when(event.getAttendees()).thenReturn(Collections.singletonList(attendee));

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
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(I(contact.getObjectID())).thenReturn(I(this.objectId));
        Mockito.when(I(contact.getCreatedBy())).thenReturn(I(this.userId));
        Mockito.when(I(contact.getModifiedBy())).thenReturn(I(this.userId));
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
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(I(contact.getObjectID())).thenReturn(I(this.objectId));
        Mockito.when(I(contact.getCreatedBy())).thenReturn(I(this.userId));
        Mockito.when(I(contact.getModifiedBy())).thenReturn(I(this.userId));
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
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(I(contact.getObjectID())).thenReturn(I(this.objectId));
        Mockito.when(I(contact.getCreatedBy())).thenReturn(I(this.userId));
        Mockito.when(I(contact.getModifiedBy())).thenReturn(I(this.userId));
        Mockito.when(contact.getDisplayName()).thenReturn(this.objectTitle);
        Mockito.when(B(contact.containsCreatedBy())).thenReturn(Boolean.TRUE);
        Mockito.when(B(contact.containsModifiedBy())).thenReturn(Boolean.TRUE);

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
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(I(task.getObjectID())).thenReturn(I(this.objectId));
        Mockito.when(I(task.getCreatedBy())).thenReturn(I(this.userId));
        Mockito.when(I(task.getModifiedBy())).thenReturn(I(this.userId));
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
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(I(task.getObjectID())).thenReturn(I(this.objectId));
        Mockito.when(I(task.getCreatedBy())).thenReturn(I(this.userId));
        Mockito.when(I(task.getModifiedBy())).thenReturn(I(this.userId));
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
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(I(documentMetadata.getCreatedBy())).thenReturn(I(this.userId));
        Mockito.when(I(documentMetadata.getModifiedBy())).thenReturn(I(this.userId));
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
        Mockito.when(I(commonEvent.getContextId())).thenReturn(I(this.contextId));
        Mockito.when(I(commonEvent.getUserId())).thenReturn(I(this.userId));
        Mockito.when(I(documentMetadata.getCreatedBy())).thenReturn(I(this.userId));
        Mockito.when(I(documentMetadata.getModifiedBy())).thenReturn(I(this.userId));
        Mockito.when(documentMetadata.getTitle()).thenReturn(this.objectTitle);
        Mockito.when(I(documentMetadata.getId())).thenReturn(I(this.objectId));

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
        PowerMockito.when(session.getParameter(ArgumentMatchers.anyString())).thenReturn(Boolean.FALSE);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SESSION)).thenReturn(session);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.OBJECT_ID)).thenReturn(I(this.objectId));
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SERVICE)).thenReturn(this.objectTitle);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.ACCOUNT_ID)).thenReturn(I(this.userId));
        PowerMockito.when(event.getProperty(FileStorageEventConstants.FOLDER_ID)).thenReturn(this.objectTitle);

        this.auditEventHandler.handleInfostoreEvent(event, stringBuilder);

        Assert.assertFalse(stringBuilder.toString().startsWith("EVENT TYPE:"));
        Assert.assertFalse(stringBuilder.toString().contains("PUBLISH: "));
        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TIME: "));
        Assert.assertTrue(stringBuilder.toString().contains("FOLDER: " + this.objectTitle));
        Assert.assertTrue(stringBuilder.toString().contains("SERVICE ID: " + this.objectTitle));
    }

    @Test
    public void testHandleInfostoreEvent_AccessTopic_AppendLocalIp() throws Exception {
        this.auditEventHandler = new AuditEventHandler(userService);

        PowerMockito.when(B(AuditConfiguration.getFileAccessLogging())).thenReturn(Boolean.TRUE);

        PowerMockito.when(event.getTopic()).thenReturn(FileStorageEventConstants.ACCESS_TOPIC);
        Session session = PowerMockito.mock(Session.class);
        PowerMockito.when(session.getParameter(ArgumentMatchers.anyString())).thenReturn(Boolean.TRUE);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SESSION)).thenReturn(session);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.OBJECT_ID)).thenReturn(I(this.objectId));
        PowerMockito.when(event.getProperty(FileStorageEventConstants.SERVICE)).thenReturn(this.objectTitle);
        PowerMockito.when(event.getProperty(FileStorageEventConstants.ACCOUNT_ID)).thenReturn(I(this.userId));
        PowerMockito.when(event.getProperty(FileStorageEventConstants.FOLDER_ID)).thenReturn(this.objectTitle);
        PowerMockito.when(event.getProperty("remoteAddress")).thenReturn("172.16.13.71");

        this.auditEventHandler.handleInfostoreEvent(event, stringBuilder);

        Assert.assertTrue(stringBuilder.toString().startsWith("EVENT TYPE: ACCESS; "));
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
        PowerMockito.when(I(this.commonEvent.getContextId())).thenReturn(I(this.contextId));
        PowerMockito.when(I(this.commonEvent.getModule())).thenReturn(I(Types.APPOINTMENT));
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
        PowerMockito.when(I(this.commonEvent.getContextId())).thenReturn(I(this.contextId));
        PowerMockito.when(I(this.commonEvent.getModule())).thenReturn(I(Types.CONTACT));
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
        PowerMockito.when(I(this.commonEvent.getContextId())).thenReturn(I(this.contextId));
        PowerMockito.when(I(this.commonEvent.getModule())).thenReturn(I(Types.TASK));
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
        PowerMockito.when(I(this.commonEvent.getContextId())).thenReturn(I(this.contextId));
        PowerMockito.when(I(this.commonEvent.getModule())).thenReturn(I(Types.INFOSTORE));
        ContextStorage contextStorage = PowerMockito.mock(ContextStorage.class);
        PowerMockito.when(contextStorage.getContext(this.contextId)).thenReturn(this.context);

        PowerMockito.when(ContextStorage.getInstance()).thenReturn(contextStorage);

        this.auditEventHandler.handleGroupwareEvent(event, stringBuilder);
    }

}
