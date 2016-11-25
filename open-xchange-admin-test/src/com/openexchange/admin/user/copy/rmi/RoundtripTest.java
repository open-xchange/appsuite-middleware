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

package com.openexchange.admin.user.copy.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.admin.rmi.AbstractRMITest;
import com.openexchange.admin.rmi.AbstractTest;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.attach.actions.AllResponse;
import com.openexchange.ajax.attach.actions.AttachRequest;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.framework.ListID;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountAllRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountAllResponse;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertRequest;
import com.openexchange.ajax.mailaccount.actions.MailAccountInsertResponse;
import com.openexchange.ajax.reminder.actions.RangeRequest;
import com.openexchange.ajax.reminder.actions.RangeResponse;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.session.actions.LogoutRequest;
import com.openexchange.ajax.subscribe.actions.AllSubscriptionsRequest;
import com.openexchange.ajax.subscribe.actions.AllSubscriptionsResponse;
import com.openexchange.ajax.subscribe.actions.NewSubscriptionRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link RoundtripTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class RoundtripTest extends AbstractRMITest {

    private OXContextInterface ci;

    private OXUserInterface ui;

    private final OXUserCopyInterface oxu;

    private User admin;

    private Context srcCtx;

    private Context dstCtx;

    private User srcUser;

    private User moved;

    private AJAXSession userSession;

    private AJAXClient userClient, origClient, copiedClient;

    public RoundtripTest() throws Exception {
        super();
        oxu = getUserCopyClient();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AJAXConfig.init();
        superAdminCredentials = AbstractTest.DummyMasterCredentials();
        ci = getContextInterface();
        Context[] contexts = ci.list("UserMove*", superAdminCredentials);
        for (Context ctx : contexts) {
            System.out.println("Deleting context " + ctx.getName() + " in schema " + ctx.getReadDatabase().getScheme());
            try {
                ci.delete(ctx, superAdminCredentials);
            } catch (Exception e) {
                System.out.println("Error during context deletion.");
            }
        }

        admin = newUser("oxadmin", "secret", "Admin User", "Admin", "User", "oxadmin@netline.de");
        srcCtx = TestTool.createContext(ci, "UserMoveSourceCtx_", admin, "all", superAdminCredentials);
        dstCtx = TestTool.createContext(ci, "UserMoveDestinationCtx_", admin, "all", superAdminCredentials);

        ui = getUserInterface();
        final User test = newUser("user", "secret", "Test User", "Test", "User", "test.user@netline.de");
        test.setImapServer("devel-mail.netline.de");
        test.setImapLogin("steffen.templin424242669");
        test.setSmtpServer("devel-mail.netline.de");
        srcUser = ui.create(srcCtx, test, getCredentials());

        userSession = performLogin("user@" + srcCtx.getName(), "secret");
        userClient = new AJAXClient(userSession, false);

        Create.createPrivateFolder("Private folder test", Types.APPOINTMENT, userClient.getValues().getUserId());

        SetRequest setLanguage = new SetRequest(Tree.Language, "de_DE");
        userClient.execute(setLanguage);
        SetRequest setTimezone = new SetRequest(Tree.TimeZone, "Europe/Berlin");
        userClient.execute(setTimezone);

        Task newTask = new Task();
        newTask.setTitle("Test task");
        newTask.setCreationDate(new Date());
        newTask.setStartDate(newTask.getCreationDate());
        newTask.setEndDate(new Date(System.currentTimeMillis() + 604800000));
        newTask.setAlarm(new Date(System.currentTimeMillis() + 518400000));
        newTask.setParentFolderID(userClient.getValues().getPrivateTaskFolder());
        ExternalUserParticipant external = new ExternalUserParticipant("test@example.org");
        newTask.addParticipant(external);
        com.openexchange.ajax.task.actions.InsertRequest newTaskRequest = new com.openexchange.ajax.task.actions.InsertRequest(newTask, TimeZone.getTimeZone("Europe/Berlin"));
        com.openexchange.ajax.task.actions.InsertResponse newTaskResponse = userClient.execute(newTaskRequest);
        newTaskResponse.fillTask(newTask);

        FolderObject taskFolder = new FolderObject("Task folder", 90, FolderObject.TASK, FolderObject.PRIVATE, userClient.getValues().getUserId());
        taskFolder.setParentFolderID(userClient.getValues().getPrivateTaskFolder());
        OCLPermission taskPerm = new OCLPermission();
        taskPerm.setEntity(userClient.getValues().getUserId());
        taskPerm.setGroupPermission(false);
        taskPerm.setFolderAdmin(true);
        taskPerm.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        taskFolder.setPermissionsAsArray(new OCLPermission[] { taskPerm });
        InsertRequest insertTaskFolderRequest = new InsertRequest(EnumAPI.OX_OLD, taskFolder);
        InsertResponse insertTaskFolderResponse = userClient.execute(insertTaskFolderRequest);
        insertTaskFolderResponse.fillObject(taskFolder);

        Task newFolderTask = new Task();
        newFolderTask.setTitle("Test folder task");
        newFolderTask.setCreationDate(new Date());
        newFolderTask.setStartDate(newTask.getCreationDate());
        newFolderTask.setEndDate(new Date(System.currentTimeMillis() + 604800000));
        newFolderTask.setAlarm(new Date(System.currentTimeMillis() + 518400000));
        newFolderTask.setParentFolderID(taskFolder.getObjectID());
        ExternalUserParticipant external2 = new ExternalUserParticipant("bla@blubber.de");
        newTask.addParticipant(external2);
        com.openexchange.ajax.task.actions.InsertRequest newFolderTaskRequest = new com.openexchange.ajax.task.actions.InsertRequest(newFolderTask, TimeZone.getTimeZone("Europe/Berlin"));
        com.openexchange.ajax.task.actions.InsertResponse newFolderTaskResponse = userClient.execute(newFolderTaskRequest);
        newFolderTaskResponse.fillTask(newFolderTask);

        Appointment newAppointment = new Appointment();
        newAppointment.setTitle("Test appointment");
        newAppointment.setCreationDate(new Date());
        newAppointment.setStartDate(new Date(System.currentTimeMillis() + 1018400000));
        newAppointment.setEndDate(new Date(System.currentTimeMillis() + 1018400000 * 2));
        newAppointment.setParentFolderID(userClient.getValues().getPrivateAppointmentFolder());
        newAppointment.setIgnoreConflicts(true);
        com.openexchange.ajax.appointment.action.InsertRequest newAppointmentRequest = new com.openexchange.ajax.appointment.action.InsertRequest(newAppointment, TimeZone.getTimeZone("Europe/Berlin"));
        AppointmentInsertResponse appointmentResponse = userClient.execute(newAppointmentRequest);
        appointmentResponse.fillObject(newAppointment);

        FolderObject appointmentFolder = com.openexchange.ajax.folder.Create.createPrivateFolder(UUID.randomUUID().toString(), FolderObject.CALENDAR, userClient.getValues().getUserId());
        appointmentFolder.setParentFolderID(userClient.getValues().getPrivateAppointmentFolder());
        OCLPermission appointmentPerm = new OCLPermission();
        appointmentPerm.setEntity(userClient.getValues().getUserId());
        appointmentPerm.setGroupPermission(false);
        appointmentPerm.setFolderAdmin(true);
        appointmentPerm.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        appointmentFolder.setPermissionsAsArray(new OCLPermission[] { appointmentPerm });
        InsertRequest insertAppointmentFolderRequest = new InsertRequest(EnumAPI.OX_OLD, appointmentFolder);
        InsertResponse insertAppointmentFolderResponse = userClient.execute(insertAppointmentFolderRequest);
        insertAppointmentFolderResponse.fillObject(appointmentFolder);

        Appointment newFolderAppointment = new Appointment();
        newFolderAppointment.setTitle("Test folder appointment");
        newFolderAppointment.setCreationDate(new Date());
        newFolderAppointment.setStartDate(new Date(System.currentTimeMillis() + 518400000));
        newFolderAppointment.setEndDate(new Date(System.currentTimeMillis() + 518400000 * 2));
        newFolderAppointment.setParentFolderID(appointmentFolder.getObjectID());
        newFolderAppointment.setIgnoreConflicts(true);
        com.openexchange.ajax.appointment.action.InsertRequest newFolderAppointmentRequest = new com.openexchange.ajax.appointment.action.InsertRequest(newFolderAppointment, TimeZone.getTimeZone("Europe/Berlin"), true);
        AppointmentInsertResponse folderAppointmentResponse = userClient.execute(newFolderAppointmentRequest);
        folderAppointmentResponse.fillObject(newFolderAppointment);

        FileInputStream is = new FileInputStream("ext/attach.png");
        AttachRequest attach = new AttachRequest(newAppointment, "attach.png", is, "image/png");
        userClient.execute(attach);

        Contact c1 = new Contact();
        c1.setEmail1("c1@example.org");
        c1.setSurName("Contact1");
        c1.setCityHome("Olpe");
        c1.setParentFolderID(userClient.getValues().getPrivateContactFolder());
        com.openexchange.ajax.contact.action.InsertRequest contactRequest1 = new com.openexchange.ajax.contact.action.InsertRequest(c1);
        com.openexchange.ajax.contact.action.InsertResponse response1 = userClient.execute(contactRequest1);
        response1.fillObject(c1);

        Contact c2 = new Contact();
        c2.setEmail1("c2@example.org");
        c2.setSurName("Contact2");
        c2.setCityHome("Olpe");
        c2.setParentFolderID(userClient.getValues().getPrivateContactFolder());
        com.openexchange.ajax.contact.action.InsertRequest contactRequest2 = new com.openexchange.ajax.contact.action.InsertRequest(c2);
        com.openexchange.ajax.contact.action.InsertResponse response2 = userClient.execute(contactRequest2);
        response2.fillObject(c2);

        FolderObject contactFolder = new FolderObject("Contact folder", 92, FolderObject.CONTACT, FolderObject.PRIVATE, userClient.getValues().getUserId());
        contactFolder.setParentFolderID(userClient.getValues().getPrivateContactFolder());
        OCLPermission contactPerm = new OCLPermission();
        contactPerm.setEntity(userClient.getValues().getUserId());
        contactPerm.setGroupPermission(false);
        contactPerm.setFolderAdmin(true);
        contactPerm.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        contactFolder.setPermissionsAsArray(new OCLPermission[] { contactPerm });
        InsertRequest insertContactFolderRequest = new InsertRequest(EnumAPI.OX_OLD, contactFolder);
        InsertResponse insertContactFolderResponse = userClient.execute(insertContactFolderRequest);
        insertContactFolderResponse.fillObject(contactFolder);

        Contact c3 = new Contact();
        c3.setEmail1("c3@example.org");
        c3.setSurName("Contact3");
        c3.setCityHome("Olpe");
        c3.setParentFolderID(contactFolder.getObjectID());
        com.openexchange.ajax.contact.action.InsertRequest contactRequest3 = new com.openexchange.ajax.contact.action.InsertRequest(c3);
        com.openexchange.ajax.contact.action.InsertResponse response3 = userClient.execute(contactRequest3);
        response3.fillObject(c3);

        //TODO this has to be read from config file
        MailAccountDescription mail = new MailAccountDescription();
        mail.setLogin("jan.bauerdick424242669");
        mail.setName("jan.bauerdick@premium");
        mail.setPassword("secret");
        mail.setMailProtocol("imap");
        mail.setMailPort(143);
        mail.setMailServer("mail.devel.open-xchange.com");
        mail.setPrimaryAddress("jan.bauerdick@premium");
        mail.setSpamHandler("NoSpamHandler");
        mail.setTransportLogin("jan.bauerdick424242669");
        mail.setTransportPassword("secret");
        mail.setTransportPort(25);
        mail.setTransportProtocol("smtp");
        mail.setTransportServer("mail.devel.open-xchange.com");
        mail.setConfirmedHamFullname("default0/confirmed_ham_fullname");
        MailAccountInsertRequest mailRequest = new MailAccountInsertRequest(mail);
        MailAccountInsertResponse mailResponse = userClient.execute(mailRequest);
        mailResponse.fillObject(mail);

        File infostore = new File("ext/infostore.doc");
        DocumentMetadata meta = new DocumentMetadataImpl();
        meta.setFileName(infostore.getName());
        meta.setFolderId(userClient.getValues().getPrivateInfostoreFolder());
        NewInfostoreRequest request = new NewInfostoreRequest(new com.openexchange.file.storage.infostore.InfostoreFile(meta), infostore);
        userClient.execute(request);

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("url", "URL", true, null));
        Subscription sub = new Subscription();
        sub.setFolderId(userClient.getValues().getPrivateContactFolder());
        sub.setDisplayName("Test subscription");
        SubscriptionSource source = new SubscriptionSource();
        source.setId("com.openexchange.subscribe.microformats.contacts.http");
        source.setFormDescription(form);
        sub.setSource(source);
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("url", "https://ox6.open-xchange.com");
        sub.setConfiguration(config);
        NewSubscriptionRequest subscriptionRequest = new NewSubscriptionRequest(sub, form);
        userClient.execute(subscriptionRequest);

        LogoutRequest logout = new LogoutRequest();
        userClient.execute(logout);
    }

    @Test
    public final void testMoveUser() throws Throwable {
        moved = oxu.copyUser(srcUser, srcCtx, dstCtx, superAdminCredentials);
        final User dstUser = ui.getData(dstCtx, moved, getCredentials());
        compareUsers(srcUser, dstUser);
    }

    @Override
    public void tearDown() throws Exception {
        if (ui != null) {
            try {
                ui.delete(srcCtx, srcUser, null, getCredentials());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (ci != null) {
            try {
                ci.delete(srcCtx, superAdminCredentials);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                ci.delete(dstCtx, superAdminCredentials);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.tearDown();
    }

    private AJAXSession performLogin(final String login, final String password) throws OXException, IOException, JSONException {
        final AJAXSession session = new AJAXSession();
        final LoginRequest loginRequest = new LoginRequest(login, password, LoginTools.generateAuthId(), "Usermovetest", "6.20");
        final LoginResponse loginResponse = Executor.execute(session, loginRequest);
        session.setId(loginResponse.getSessionId());
        return session;
    }

    private void compareUsers(final User orig, final User copied) throws Exception {
        final AJAXSession origSession = performLogin("user@" + srcCtx.getName(), "secret");
        final AJAXSession copiedSession = performLogin("user@" + dstCtx.getName(), "secret");
        origClient = new AJAXClient(origSession, true);
        copiedClient = new AJAXClient(copiedSession, true);

        final GetRequest origLocaleRequest = new GetRequest(Tree.Language);
        final GetRequest copiedLocaleRequest = new GetRequest(Tree.Language);
        final GetResponse origLocaleResponse = origClient.execute(origLocaleRequest);
        final GetResponse copiedLocaleResponse = copiedClient.execute(copiedLocaleRequest);
        final String origLocale = origLocaleResponse.getString();
        final String copiedLocale = copiedLocaleResponse.getString();
        assertEquals("Locale are not equal.", origLocale, copiedLocale);

        final GetRequest origTimezoneRequest = new GetRequest(Tree.TimeZone);
        final GetRequest copiedTimezoneRequest = new GetRequest(Tree.TimeZone);
        final GetResponse origTimezoneResponse = origClient.execute(origTimezoneRequest);
        final GetResponse copiedTimezoneResponse = copiedClient.execute(copiedTimezoneRequest);
        final String origTimezone = origTimezoneResponse.getString();
        final String copiedTimezone = copiedTimezoneResponse.getString();
        assertEquals("Timezones are not equal.", origTimezone, copiedTimezone);

        final com.openexchange.ajax.contact.action.AllRequest origContactsRequest = new com.openexchange.ajax.contact.action.AllRequest(origClient.getValues().getPrivateContactFolder(), Contact.ALL_COLUMNS);
        final CommonAllResponse origContactsResponse = origClient.execute(origContactsRequest);
        final List<CommonObject> origContacts = new ArrayList<CommonObject>();
        final ListIDs origContactIds = origContactsResponse.getListIDs();
        for (final ListID l : origContactIds) {
            final com.openexchange.ajax.contact.action.GetRequest getContact = new com.openexchange.ajax.contact.action.GetRequest(Integer.valueOf(l.getFolder()), Integer.valueOf(l.getObject()), origClient.getValues().getTimeZone());
            origContacts.add(origClient.execute(getContact).getContact());
        }
        final com.openexchange.ajax.contact.action.AllRequest copiedContactsRequest = new com.openexchange.ajax.contact.action.AllRequest(copiedClient.getValues().getPrivateContactFolder(), Contact.ALL_COLUMNS);
        final CommonAllResponse copiedContactsResponse = copiedClient.execute(copiedContactsRequest);
        final List<CommonObject> copiedContacts = new ArrayList<CommonObject>();
        final ListIDs copiedContactIds = copiedContactsResponse.getListIDs();
        for (final ListID l : copiedContactIds) {
            final com.openexchange.ajax.contact.action.GetRequest getContact = new com.openexchange.ajax.contact.action.GetRequest(Integer.valueOf(l.getFolder()), Integer.valueOf(l.getObject()), copiedClient.getValues().getTimeZone());
            copiedContacts.add(copiedClient.execute(getContact).getContact());
        }
        Collections.sort(origContacts, new CommonObjectComparator());
        Collections.sort(copiedContacts, new CommonObjectComparator());
        if (origContacts.size() != copiedContacts.size()) {
            fail("Contact lists are not equal.");
        }
        compareLists(origContacts, copiedContacts);

        final AllRequest origAppointmentRequest = new AllRequest(origClient.getValues().getPrivateAppointmentFolder(), Appointment.ALL_COLUMNS, new Date(0), new Date(Long.MAX_VALUE), origClient.getValues().getTimeZone());
        final AllRequest copiedAppointmentRequest = new AllRequest(copiedClient.getValues().getPrivateAppointmentFolder(), Appointment.ALL_COLUMNS, new Date(0), new Date(Long.MAX_VALUE), copiedClient.getValues().getTimeZone());
        final CommonAllResponse origAppointmentsResponse = origClient.execute(origAppointmentRequest);
        final CommonAllResponse copiedAppointmentsResponse = copiedClient.execute(copiedAppointmentRequest);
        final List<CommonObject> origAppointments = new ArrayList<CommonObject>();
        final List<CommonObject> copiedAppointments = new ArrayList<CommonObject>();
        final ListIDs origAppointmentIds = origAppointmentsResponse.getListIDs();
        final ListIDs copiedAppointmentIds = copiedAppointmentsResponse.getListIDs();
        for (final ListID l : origAppointmentIds) {
            final com.openexchange.ajax.appointment.action.GetRequest getAppointment = new com.openexchange.ajax.appointment.action.GetRequest(Integer.valueOf(l.getFolder()), Integer.valueOf(l.getObject()), true);
            final com.openexchange.ajax.appointment.action.GetResponse getAppointmentResponse = origClient.execute(getAppointment);
            origAppointments.add(getAppointmentResponse.getAppointment(origClient.getValues().getTimeZone()));
        }
        for (final ListID l : copiedAppointmentIds) {
            final com.openexchange.ajax.appointment.action.GetRequest getAppointment = new com.openexchange.ajax.appointment.action.GetRequest(Integer.valueOf(l.getFolder()), Integer.valueOf(l.getObject()), true);
            final com.openexchange.ajax.appointment.action.GetResponse getAppointmentResponse = copiedClient.execute(getAppointment);
            copiedAppointments.add(getAppointmentResponse.getAppointment(copiedClient.getValues().getTimeZone()));
        }
        Collections.sort(origAppointments, new CommonObjectComparator());
        Collections.sort(copiedAppointments, new CommonObjectComparator());
        if (origAppointments.size() != copiedAppointments.size()) {
            fail("Appointment lists are not equal.");
        }
        compareLists(origAppointments, copiedAppointments);

        final com.openexchange.ajax.task.actions.AllRequest origTasksRequest = new com.openexchange.ajax.task.actions.AllRequest(origClient.getValues().getPrivateTaskFolder(), Task.ALL_COLUMNS, 0, Order.NO_ORDER);
        final com.openexchange.ajax.task.actions.AllRequest copiedTasksRequest = new com.openexchange.ajax.task.actions.AllRequest(copiedClient.getValues().getPrivateTaskFolder(), Task.ALL_COLUMNS, 0, Order.NO_ORDER);
        final CommonAllResponse origTasksResponse = origClient.execute(origTasksRequest);
        final CommonAllResponse copiedTasksResponse = copiedClient.execute(copiedTasksRequest);
        final List<CommonObject> origTasks = new ArrayList<CommonObject>();
        final List<CommonObject> copiedTasks = new ArrayList<CommonObject>();
        final ListIDs origTaskIds = origTasksResponse.getListIDs();
        final ListIDs copiedTaskIds = copiedTasksResponse.getListIDs();
        for (final ListID l : origTaskIds) {
            final com.openexchange.ajax.task.actions.GetRequest getTask = new com.openexchange.ajax.task.actions.GetRequest(Integer.valueOf(l.getFolder()), Integer.valueOf(l.getObject()), true);
            final com.openexchange.ajax.task.actions.GetResponse getTaskResponse = origClient.execute(getTask);
            origTasks.add(getTaskResponse.getTask(origClient.getValues().getTimeZone()));
        }
        for (final ListID l : copiedTaskIds) {
            final com.openexchange.ajax.task.actions.GetRequest getTask = new com.openexchange.ajax.task.actions.GetRequest(Integer.valueOf(l.getFolder()), Integer.valueOf(l.getObject()), true);
            final com.openexchange.ajax.task.actions.GetResponse getTaskResponse = copiedClient.execute(getTask);
            copiedTasks.add(getTaskResponse.getTask(copiedClient.getValues().getTimeZone()));
        }
        Collections.sort(origTasks, new CommonObjectComparator());
        Collections.sort(copiedTasks, new CommonObjectComparator());
        if (origTasks.size() != copiedTasks.size()) {
            fail("Task lists are not equal.");
        }
        compareLists(origTasks, copiedTasks);

        final RangeRequest origReminderRequest = new RangeRequest(new Date(Long.MAX_VALUE));
        final RangeRequest copiedReminderRequest = new RangeRequest(new Date(Long.MAX_VALUE));
        final RangeResponse origReminderResponse = origClient.execute(origReminderRequest);
        final RangeResponse copiedReminderResponse = copiedClient.execute(copiedReminderRequest);
        final ReminderObject[] origReminders = origReminderResponse.getReminder(origClient.getValues().getTimeZone());
        final ReminderObject[] copiedReminders = copiedReminderResponse.getReminder(copiedClient.getValues().getTimeZone());
        final List<ReminderObject> origRemindersList = Arrays.asList(origReminders);
        final List<ReminderObject> copiedReminderList = Arrays.asList(copiedReminders);
        Collections.sort(origRemindersList, new ReminderObjectComparator());
        Collections.sort(copiedReminderList, new ReminderObjectComparator());
        if (origRemindersList.size() != copiedReminderList.size()) {
            fail("Reminder lists are not equal.");
        }
        compareReminders(origRemindersList, copiedReminderList);

        final ListRequest origFoldersRequest = new ListRequest(EnumAPI.OX_NEW, 1);
        final ListRequest copiedFoldersRequest = new ListRequest(EnumAPI.OX_NEW, 1);
        final ListResponse origFoldersResponse = origClient.execute(origFoldersRequest);
        final ListResponse copiedFoldersResponse = copiedClient.execute(copiedFoldersRequest);
        final Iterator<FolderObject> origFolders = origFoldersResponse.getFolder();
        final Iterator<FolderObject> copiedFolders = copiedFoldersResponse.getFolder();
        final List<FolderObject> origFolderList = new ArrayList<FolderObject>();
        final List<FolderObject> copiedFolderList = new ArrayList<FolderObject>();
        while (origFolders.hasNext()) {
            origFolderList.add(origFolders.next());
        }
        while (copiedFolders.hasNext()) {
            copiedFolderList.add(copiedFolders.next());
        }
        Collections.sort(origFolderList, new FolderComparator());
        Collections.sort(copiedFolderList, new FolderComparator());
        if (origFolderList.size() != copiedFolderList.size()) {
            fail("Folder lists are not equal.");
        }
        compareFolders(origFolderList, copiedFolderList);

        final AllInfostoreRequest origInfostoreRequest = new AllInfostoreRequest(origClient.getValues().getPrivateInfostoreFolder(), new int[] { 700, 701, 702, 703, 704, 705, 706, 707, 708, 709, 710, 711 }, 1, Order.ASCENDING);
        final AbstractColumnsResponse origInfostoreResponse = origClient.execute(origInfostoreRequest);
        Iterator<Object[]> origInfostoreIterator = origInfostoreResponse.iterator();
        final AllInfostoreRequest copiedInfostoreRequest = new AllInfostoreRequest(copiedClient.getValues().getPrivateInfostoreFolder(), new int[] { 700, 701, 702, 703, 704, 705, 706, 707, 708, 709, 710, 711 }, 1, Order.ASCENDING);
        final AbstractColumnsResponse copiedInfostoreResponse = copiedClient.execute(copiedInfostoreRequest);
        Iterator<Object[]> copiedInfostoreIterator = copiedInfostoreResponse.iterator();
        if (origInfostoreResponse.size() != copiedInfostoreResponse.size()) {
            fail("Infostore elements are not equal.");
        }
        while (origInfostoreIterator.hasNext()) {
            Object[] origNext = origInfostoreIterator.next();
            Object[] copiedNext = copiedInfostoreIterator.next();
            assertEquals("Title is not equal.", String.valueOf(origNext[0]), String.valueOf(copiedNext[0]));
            assertEquals("URL is not equal.", String.valueOf(origNext[1]), String.valueOf(copiedNext[1]));
            assertEquals("Filename is not equal.", String.valueOf(origNext[2]), String.valueOf(copiedNext[2]));
            assertEquals("MIME type is not equal.", String.valueOf(origNext[3]), String.valueOf(copiedNext[3]));
            assertEquals("Size is not equal.", Long.valueOf(String.valueOf(origNext[4])), Long.valueOf(String.valueOf(copiedNext[4])));
            assertEquals("Version is not equal.", Integer.valueOf(String.valueOf(origNext[5])), Integer.valueOf(String.valueOf(copiedNext[5])));
            assertEquals("Description is not equal.", String.valueOf(origNext[6]), String.valueOf(copiedNext[6]));
            assertEquals("Locked until is not equal.", new Date(Long.valueOf(String.valueOf(origNext[7]))), new Date(Long.valueOf(String.valueOf(copiedNext[7]))));
            assertEquals("MD5sum is not equal.", String.valueOf(origNext[8]), String.valueOf(copiedNext[8]));
            assertEquals("Version comment is not equal.", String.valueOf(origNext[9]), String.valueOf(copiedNext[9]));
            assertEquals("Is current version is not equal.", Boolean.valueOf(String.valueOf(origNext[10])), Boolean.valueOf(String.valueOf(copiedNext[10])));
            assertEquals("Number of versions is not equal.", Integer.valueOf(String.valueOf(origNext[11])), Integer.valueOf(String.valueOf(copiedNext[11])));
        }

        MailAccountAllRequest origMailAccountRequest = new MailAccountAllRequest(new int[] { 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025, 1026, 1027, 1028, 1029, 1030, 1031, 1032, 1033, 1034, 1035, 1036, 1037, 1038 });
        MailAccountAllRequest copiedMailAccountRequest = new MailAccountAllRequest(new int[] { 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025, 1026, 1027, 1028, 1029, 1030, 1031, 1032, 1033, 1034, 1035, 1036, 1037, 1038 });
        MailAccountAllResponse origMailAccountResponse = origClient.execute(origMailAccountRequest);
        MailAccountAllResponse copiedMailAccountAllResponse = copiedClient.execute(copiedMailAccountRequest);
        List<MailAccountDescription> origMailAccounts = origMailAccountResponse.getDescriptions();
        List<MailAccountDescription> copiedMailAccounts = copiedMailAccountAllResponse.getDescriptions();
        if (origMailAccounts.size() != copiedMailAccounts.size()) {
            fail("Mail account lists are not equal.");
        }
        Collections.sort(origMailAccounts, new MailAccountComparator());
        Collections.sort(copiedMailAccounts, new MailAccountComparator());
        compareMailAccounts(origMailAccounts, copiedMailAccounts);

        AllSubscriptionsRequest origSubscriptionsRequest = new AllSubscriptionsRequest();
        AllSubscriptionsRequest copiedSubscriptionsRequest = new AllSubscriptionsRequest();
        AllSubscriptionsResponse origSubscriptionsResponse = origClient.execute(origSubscriptionsRequest);
        AllSubscriptionsResponse copiedSubscriptionsResponse = copiedClient.execute(copiedSubscriptionsRequest);
        JSONArray origSubscriptions = origSubscriptionsResponse.getAll();
        JSONArray copiedSubcriptions = copiedSubscriptionsResponse.getAll();
        if (origSubscriptions.length() != copiedSubcriptions.length()) {
            fail("Subscriptions are not equal.");
        }
        compareSubscriptions(origSubscriptions, copiedSubcriptions);
    }

    private void compareLists(final List<CommonObject> orig, final List<CommonObject> copied) throws Exception {
        for (int i = 0; i < orig.size(); i++) {
            final CommonObject o1 = orig.get(i);
            final CommonObject o2 = copied.get(i);
            if (o1.getNumberOfAttachments() > 0) {
                final com.openexchange.ajax.attach.actions.AllRequest origAttachmentsRequest = new com.openexchange.ajax.attach.actions.AllRequest(o1, new int[] { 800, 801, 802, 803, 804, 805, 806 }, 0, Order.ASCENDING);
                final com.openexchange.ajax.attach.actions.AllRequest copiedAttachmentsRequest = new com.openexchange.ajax.attach.actions.AllRequest(o2, new int[] { 800, 801, 802, 803, 804, 805, 806 }, 0, Order.ASCENDING);
                final AllResponse origAttachmentsResponse = origClient.execute(origAttachmentsRequest);
                final AllResponse copiedAttachmentsResponse = copiedClient.execute(copiedAttachmentsRequest);
                final List<AttachmentMetadata> origAttachmentList = origAttachmentsResponse.getAttachments();
                final List<AttachmentMetadata> copiedAttachmentList = copiedAttachmentsResponse.getAttachments();
                if (origAttachmentList.size() != copiedAttachmentList.size()) {
                    fail("Attachment lists are not equal.");
                }
                Collections.sort(origAttachmentList, new AttachmentComparator());
                Collections.sort(copiedAttachmentList, new AttachmentComparator());
                compareAttachments(origAttachmentList, copiedAttachmentList);
            }
            if (o1 instanceof Appointment && o2 instanceof Appointment) {
                final Appointment a1 = (Appointment) o1;
                final Appointment a2 = (Appointment) o2;
                assertEquals("Alarm is not equal.", a1.getAlarm(), a2.getAlarm());
                assertEquals("Alarm flag is not equal.", a1.getAlarmFlag(), a2.getAlarmFlag());
                assertEquals("Categories is not equal.", a1.getCategories(), a2.getCategories());
                assertEquals("Change exception is not equal.", a1.getChangeException(), a2.getChangeException());
                assertEquals("Confirm is not equal.", a1.getConfirm(), a2.getConfirm());
                assertEquals("Confirmations is not equal.", a1.getConfirmations(), a2.getConfirmations());
                assertEquals("Confirm message is not equal.", a1.getConfirmMessage(), a2.getConfirmMessage());
                assertEquals("Creation date is not equal.", a1.getCreationDate(), a2.getCreationDate());
                assertEquals("Day in month is not equal.", a1.getDayInMonth(), a2.getDayInMonth());
                assertEquals("Days is not equal.", a1.getDays(), a2.getDays());
                assertEquals("Delete exception is not equal.", a1.getDeleteException(), a2.getDeleteException());
                assertEquals("End date is not equal.", a1.getEndDate(), a2.getEndDate());
                assertEquals("Full time is not equal.", a1.getFullTime(), a2.getFullTime());
                assertEquals("Ignore Conflicts is not equal.", a1.getIgnoreConflicts(), a2.getIgnoreConflicts());
                assertEquals("Interval is not equal.", a1.getInterval(), a2.getInterval());
                assertEquals("Label is not equal.", a1.getLabel(), a2.getLabel());
                assertEquals("Last modified is not equal.", a1.getLastModified(), a2.getLastModified());
                assertEquals("Last modified of newest attachment is not equal.", a1.getLastModifiedOfNewestAttachment(), a2.getLastModifiedOfNewestAttachment());
                assertEquals("Location is not equal.", a1.getLocation(), a2.getLocation());
                assertEquals("Month is not equal.", a1.getMonth(), a2.getMonth());
                assertEquals("Note is not equal.", a1.getNote(), a2.getNote());
                assertEquals("Notification is not equal.", a1.getNotification(), a2.getNotification());
                assertEquals("Number of attachments is not equal.", a1.getNumberOfAttachments(), a2.getNumberOfAttachments());
                assertEquals("Occurence is not equal.", a1.getOccurrence(), a2.getOccurrence());
                assertEquals("Organizer is not equal.", a1.getOrganizer(), a2.getOrganizer());
                assertEquals("Private flag is not equal.", a1.getPrivateFlag(), a2.getPrivateFlag());
                assertEquals("Participants is not equal.", a1.getParticipants(), a2.getParticipants());
                assertEquals("Recurrence calculator is not equal.", a1.getRecurrenceCalculator(), a2.getRecurrenceCalculator());
                assertEquals("Recurrence count is not equal.", a1.getRecurrenceCount(), a2.getRecurrenceCount());
                assertEquals("Recurrence date position is not equal.", a1.getRecurrenceDatePosition(), a2.getRecurrenceDatePosition());
                assertEquals("Recurrence ID is not equal.", a1.getRecurrenceID(), a2.getRecurrenceID());
                assertEquals("Recurrence position is not equal.", a1.getRecurrencePosition(), a2.getRecurrencePosition());
                assertEquals("Recurrence type is not equal.", a1.getRecurrenceType(), a2.getRecurrenceType());
                assertEquals("Recurring start is not equal.", a1.getRecurringStart(), a2.getRecurringStart());
                assertEquals("Sequence is not equal.", a1.getSequence(), a2.getSequence());
                assertEquals("Shown as is not equal.", a1.getShownAs(), a2.getShownAs());
                assertEquals("Start date is not equal.", a1.getStartDate(), a2.getStartDate());
                assertEquals("Timezone is not equal.", a1.getTimezone(), a2.getTimezone());
                assertEquals("Title is not equal.", a1.getTitle(), a2.getTitle());
                assertEquals("Until is not equal.", a1.getUntil(), a2.getUntil());
            }
            if (o1 instanceof Task && o2 instanceof Task) {
                final Task t1 = (Task) o1;
                final Task t2 = (Task) o2;
                assertEquals("Actual costs is not equal.", t1.getActualCosts(), t2.getActualCosts());
                assertEquals("Actual duration is not equal.", t1.getActualDuration(), t2.getActualDuration());
                assertEquals("After complete is not equal.", t1.getAfterComplete(), t2.getAfterComplete());
                assertEquals("Alarm is not equal.", t1.getAlarm(), t2.getAlarm());
                assertEquals("Alarm flag is not equal.", t1.getAlarmFlag(), t2.getAlarmFlag());
                assertEquals("Billing information is not equal.", t1.getBillingInformation(), t2.getBillingInformation());
                assertEquals("Categories is not equal.", t1.getCategories(), t2.getCategories());
                assertEquals("Change exception is not equal.", t1.getChangeException(), t2.getChangeException());
                assertEquals("Companies is not equal.", t1.getCompanies(), t2.getCompanies());
                assertEquals("Confirm is not equal.", t1.getConfirm(), t2.getConfirm());
                assertEquals("Confirmations is not equal.", t1.getConfirmations(), t2.getConfirmations());
                assertEquals("Confirm message is not equal.", t1.getConfirmMessage(), t2.getConfirmMessage());
                assertEquals("Creation date is not equal.", t1.getCreationDate(), t2.getCreationDate());
                assertEquals("Currency is not equal.", t1.getCurrency(), t2.getCurrency());
                assertEquals("Date completed is not equal.", t1.getDateCompleted(), t2.getDateCompleted());
                assertEquals("Day in month is not equal.", t1.getDayInMonth(), t2.getDayInMonth());
                assertEquals("Days is not equal.", t1.getDays(), t2.getDays());
                assertEquals("Delete exception is not equal.", t1.getDeleteException(), t2.getDeleteException());
                assertEquals("End date is not equal.", t1.getEndDate(), t2.getEndDate());
                assertEquals("Interval is not equal.", t1.getInterval(), t2.getInterval());
                assertEquals("Label is not equal.", t1.getLabel(), t2.getLabel());
                assertEquals("Last modified is not equal.", t1.getLastModified(), t2.getLastModified());
                assertEquals("Last modified of newest attachment is not equal.", t1.getLastModifiedOfNewestAttachment(), t2.getLastModifiedOfNewestAttachment());
                assertEquals("Month is not equal.", t1.getMonth(), t2.getMonth());
                assertEquals("Note is not equal.", t1.getNote(), t2.getNote());
                assertEquals("Notification is not equal.", t1.getNotification(), t2.getNotification());
                assertEquals("Number of attachments is not equal.", t1.getNumberOfAttachments(), t2.getNumberOfAttachments());
                assertEquals("Occurence is not equal.", t1.getOccurrence(), t2.getOccurrence());
                assertEquals("Organizer is not equal.", t1.getOrganizer(), t2.getOrganizer());
                assertEquals("Participants is not equal.", t1.getParticipants(), t2.getParticipants());
                assertEquals("Percent complete is not equal.", t1.getPercentComplete(), t2.getPercentComplete());
                assertEquals("Priority is not equal.", t1.getPriority(), t2.getPriority());
                assertEquals("Private flag is not equal.", t1.getPrivateFlag(), t2.getPrivateFlag());
                assertEquals("Project ID is not equal.", t1.getProjectID(), t2.getProjectID());
                assertEquals("Recurrence calculator is not equal.", t1.getRecurrenceCalculator(), t2.getRecurrenceCalculator());
                assertEquals("Recurrence count is not equal.", t1.getRecurrenceCount(), t2.getRecurrenceCount());
                assertEquals("Recurrence date position is not equal.", t1.getRecurrenceDatePosition(), t2.getRecurrenceDatePosition());
                assertEquals("Recurrence ID is not equal.", t1.getRecurrenceID(), t2.getRecurrenceID());
                assertEquals("Recurrence position is not equal.", t1.getRecurrencePosition(), t2.getRecurrencePosition());
                assertEquals("Recurrence type is not equal.", t1.getRecurrenceType(), t2.getRecurrenceType());
                assertEquals("Sequence is not equal.", t1.getSequence(), t2.getSequence());
                assertEquals("Start date is not equal.", t1.getStartDate(), t2.getStartDate());
                assertEquals("Status is not equal.", t1.getStatus(), t2.getStatus());
                assertEquals("Target costs is not equal.", t1.getTargetCosts(), t2.getTargetCosts());
                assertEquals("Target duration is not equal.", t1.getTargetDuration(), t2.getTargetDuration());
                assertEquals("Title is not equal.", t1.getTitle(), t2.getTitle());
                assertEquals("Tripmeter is not equal.", t1.getTripMeter(), t2.getTripMeter());
                assertEquals("Until is not equal.", t1.getUntil(), t2.getUntil());
            }
            if (o1 instanceof Contact && o2 instanceof Contact) {
                final Contact c1 = (Contact) o1;
                final Contact c2 = (Contact) o2;
                assertEquals("Address business is not equal.", c1.getAddressBusiness(), c2.getAddressBusiness());
                assertEquals("Address home is not equal.", c1.getAddressHome(), c2.getAddressHome());
                assertEquals("Address other is not equal.", c1.getAddressOther(), c2.getAddressOther());
                assertEquals("Anniversary is not equal.", c1.getAnniversary(), c2.getAnniversary());
                assertEquals("Assistant name is not equal.", c1.getAssistantName(), c2.getAssistantName());
                assertEquals("Birthday is not equal.", c1.getBirthday(), c2.getBirthday());
                assertEquals("Branches is not equal.", c1.getBranches(), c2.getBranches());
                assertEquals("Business category is not equal.", c1.getBusinessCategory(), c2.getBusinessCategory());
                assertEquals("Categories is not equal.", c1.getCategories(), c2.getCategories());
                assertEquals("Cellular phone 1 is not equal.", c1.getCellularTelephone1(), c2.getCellularTelephone1());
                assertEquals("Cellular phone 2 is not equal.", c1.getCellularTelephone2(), c2.getCellularTelephone2());
                assertEquals("Default address is not equal.", c1.getDefaultAddress(), c2.getDefaultAddress());
                assertEquals("Department is not equal.", c1.getDepartment(), c2.getDepartment());
                assertEquals("Display name is not equal.", c1.getDisplayName(), c2.getDisplayName());
                assertEquals("Email 1 is not equal.", c1.getEmail1(), c2.getEmail1());
                assertEquals("Email 2 is not equal.", c1.getEmail2(), c2.getEmail2());
                assertEquals("Email 3 is not equal.", c1.getEmail3(), c2.getEmail3());
                assertEquals("Employee type is not equal.", c1.getEmployeeType(), c2.getEmployeeType());
                assertEquals("Fax business is not equal.", c1.getFaxBusiness(), c2.getFaxBusiness());
                assertEquals("Fax home is not equal.", c1.getFaxHome(), c2.getFaxHome());
                assertEquals("Fax other is not equal.", c1.getFaxOther(), c2.getFaxOther());
                assertEquals("File is not equal.", c1.getFileAs(), c2.getFileAs());
                assertEquals("Given name is not equal.", c1.getGivenName(), c2.getGivenName());
                assertEquals("Image 1 is not equal.", c1.getImage1(), c2.getImage1());
                assertEquals("Image content type is not equal.", c1.getImageContentType(), c2.getImageContentType());
                assertEquals("Image last modified is not equal.", c1.getImageLastModified(), c2.getImageLastModified());
                assertEquals("Info is not equal.", c1.getInfo(), c2.getInfo());
                assertEquals("Instant messenger 1 is not equal.", c1.getInstantMessenger1(), c2.getInstantMessenger1());
                assertEquals("Instant messenger 2 not equal.", c1.getInstantMessenger2(), c2.getInstantMessenger2());
                assertEquals("Label is not equal.", c1.getLabel(), c2.getLabel());
                assertEquals("Last modified is not equal.", c1.getLastModified(), c2.getLastModified());
                assertEquals("Last modified of newest attachment is not equal.", c1.getLastModifiedOfNewestAttachment(), c2.getLastModifiedOfNewestAttachment());
                assertEquals("Manager name is not equal.", c1.getManagerName(), c2.getManagerName());
                assertEquals("Marital status is not equal.", c1.getMaritalStatus(), c2.getMaritalStatus());
                assertEquals("Mark as distribution list is not equal.", c1.getMarkAsDistribtuionlist(), c2.getMarkAsDistribtuionlist());
                assertEquals("Middle name is not equal.", c1.getMiddleName(), c2.getMiddleName());
                assertEquals("Nickname is not equal.", c1.getNickname(), c2.getNickname());
                assertEquals("Note is not equal.", c1.getNote(), c2.getNote());
                assertEquals("Number of attachments is not equal.", c1.getNumberOfAttachments(), c2.getNumberOfAttachments());
                assertEquals("Number of children is not equal.", c1.getNumberOfChildren(), c2.getNumberOfChildren());
                assertEquals("Number of distribution lists is not equal.", c1.getNumberOfDistributionLists(), c2.getNumberOfDistributionLists());
                assertEquals("Employee ID is not equal.", c1.getNumberOfEmployee(), c2.getNumberOfEmployee());
                assertEquals("Number of images is not equal.", c1.getNumberOfImages(), c2.getNumberOfImages());
                assertEquals("Position is not equal.", c1.getPosition(), c2.getPosition());
                assertEquals("Postal code business is not equal.", c1.getPostalCodeBusiness(), c2.getPostalCodeBusiness());
                assertEquals("Postal code home is not equal.", c1.getPostalCodeHome(), c2.getPostalCodeHome());
                assertEquals("Postal code other is not equal.", c1.getPostalCodeOther(), c2.getPostalCodeOther());
                assertEquals("Private flag is not equal.", c1.getPrivateFlag(), c2.getPrivateFlag());
                assertEquals("Profession is not equal.", c1.getProfession(), c2.getProfession());
                assertEquals("Room number is not equal.", c1.getRoomNumber(), c2.getRoomNumber());
                assertEquals("Sales volume is not equal.", c1.getSalesVolume(), c2.getSalesVolume());
                assertEquals("Size of distribution list array is not equal.", c1.getSizeOfDistributionListArray(), c2.getSizeOfDistributionListArray());
                assertEquals("Spouse name is not equal.", c1.getSpouseName(), c2.getSpouseName());
                assertEquals("State business is not equal.", c1.getStateBusiness(), c2.getStateBusiness());
                assertEquals("State home is not equal.", c1.getStateHome(), c2.getStateHome());
                assertEquals("State other is not equal.", c1.getStateOther(), c2.getStateOther());
                assertEquals("Street business is not equal.", c1.getStreetBusiness(), c2.getStreetBusiness());
                assertEquals("Street home is not equal.", c1.getStreetHome(), c2.getStreetHome());
                assertEquals("Street other is not equal.", c1.getStreetOther(), c2.getStreetOther());
                assertEquals("Suffix is not equal.", c1.getSuffix(), c2.getSuffix());
                assertEquals("Sur name is not equal.", c1.getSurName(), c2.getSurName());
                assertEquals("Tax Id is not equal.", c1.getTaxID(), c2.getTaxID());
                assertEquals("Telephone assistant is not equal.", c1.getTelephoneAssistant(), c2.getTelephoneAssistant());
                assertEquals("Telephone business 1is not equal.", c1.getTelephoneBusiness1(), c2.getTelephoneBusiness1());
                assertEquals("Telephone business 2 is not equal.", c1.getTelephoneBusiness2(), c2.getTelephoneBusiness2());
                assertEquals("Telephone callback is not equal.", c1.getTelephoneCallback(), c2.getTelephoneCallback());
                assertEquals("Telephone car is not equal.", c1.getTelephoneCar(), c2.getTelephoneCar());
                assertEquals("Telephone company is not equal.", c1.getTelephoneCompany(), c2.getTelephoneCompany());
                assertEquals("Telephone home 1 is not equal.", c1.getTelephoneHome1(), c2.getTelephoneHome1());
                assertEquals("Telephone home 2 is not equal.", c1.getTelephoneHome2(), c2.getTelephoneHome2());
                assertEquals("Telephone IP is not equal.", c1.getTelephoneIP(), c2.getTelephoneIP());
                assertEquals("Telephone ISDN is not equal.", c1.getTelephoneISDN(), c2.getTelephoneISDN());
                assertEquals("Telephone other is not equal.", c1.getTelephoneOther(), c2.getTelephoneOther());
                assertEquals("Telephone pager is not equal.", c1.getTelephonePager(), c2.getTelephonePager());
                assertEquals("Telephone primary is not equal.", c1.getTelephonePrimary(), c2.getTelephonePrimary());
                assertEquals("Telephone radio is not equal.", c1.getTelephoneRadio(), c2.getTelephoneRadio());
                assertEquals("Telephone telex is not equal.", c1.getTelephoneTelex(), c2.getTelephoneTelex());
                assertEquals("Telephone TTY/TTD is not equal.", c1.getTelephoneTTYTTD(), c2.getTelephoneTTYTTD());
                assertEquals("Title is not equal.", c1.getTitle(), c2.getTitle());
                assertEquals("Yomi Company is not equal.", c1.getYomiCompany(), c2.getYomiCompany());
                assertEquals("Yomi first name is not equal.", c1.getYomiFirstName(), c2.getYomiFirstName());
                assertEquals("Yomi last is not equal.", c1.getYomiLastName(), c2.getYomiLastName());
            }
        }
    }

    private void compareFolders(final List<FolderObject> orig, final List<FolderObject> copied) {
        for (int i = 0; i < orig.size(); i++) {
            final FolderObject f1 = orig.get(i);
            final FolderObject f2 = copied.get(i);
            assertEquals("Creation date is not equal.", f1.getCreationDate(), f2.getCreationDate());
            assertEquals("Folder name is not equal.", f1.getFolderName(), f2.getFolderName());
            assertEquals("Full name is not equal.", f1.getFullName(), f2.getFullName());
            assertEquals("Last modified is not equal.", f1.getLastModified(), f2.getLastModified());
            assertEquals("Module is not equal.", f1.getModule(), f2.getModule());
            assertEquals("Type last is not equal.", f1.getType(), f2.getType());
        }
    }

    private void compareReminders(final List<ReminderObject> orig, final List<ReminderObject> copied) {
        for (int i = 0; i < orig.size(); i++) {
            final ReminderObject r1 = orig.get(i);
            final ReminderObject r2 = copied.get(i);
            assertEquals("Date is not equal.", r1.getDate(), r2.getDate());
            assertEquals("Description is not equal.", r1.getDescription(), r2.getDescription());
            assertEquals("Last modified is not equal.", r1.getLastModified(), r2.getLastModified());
            assertEquals("Module is not equal.", r1.getModule(), r2.getModule());
            assertEquals("Recurrence position is not equal.", r1.getRecurrencePosition(), r2.getRecurrencePosition());
        }
    }

    private void compareAttachments(final List<AttachmentMetadata> orig, final List<AttachmentMetadata> copied) {
        for (int i = 0; i < orig.size(); i++) {
            AttachmentMetadata a1 = orig.get(i);
            AttachmentMetadata a2 = orig.get(i);
            assertEquals("Comment is not equal.", a1.getComment(), a2.getComment());
            assertEquals("Creation date is not equal.", a1.getCreationDate(), a2.getCreationDate());
            assertEquals("Mime type is not equal.", a1.getFileMIMEType(), a2.getFileMIMEType());
            assertEquals("Filename is not equal.", a1.getFilename(), a2.getFilename());
            assertEquals("Filesize is not equal.", a1.getFilesize(), a2.getFilesize());
            assertEquals("Module is not equal.", a1.getModuleId(), a2.getModuleId());
            assertEquals("RTF flag is not equal.", a1.getRtfFlag(), a2.getRtfFlag());
        }
    }

    private void compareMailAccounts(final List<MailAccountDescription> orig, final List<MailAccountDescription> copied) {
        for (int i = 0; i < orig.size(); i++) {
            MailAccountDescription m1 = orig.get(i);
            MailAccountDescription m2 = copied.get(i);
            assertEquals("Confirmed ham is not equal.", m1.getConfirmedHam(), m2.getConfirmedHam());
            assertEquals("Confirmed ham full name is not equal.", m1.getConfirmedHamFullname(), m2.getConfirmedHamFullname());
            assertEquals("Confirmed spam is not equal.", m1.getConfirmedSpam(), m2.getConfirmedSpam());
            assertEquals("Confirmed spam full name is not equal.", m1.getConfirmedSpamFullname(), m2.getConfirmedSpamFullname());
            assertEquals("Drafts is not equal.", m1.getDrafts(), m2.getDrafts());
            assertEquals("Drafts full name is not equal.", m1.getDraftsFullname(), m2.getDraftsFullname());
            assertEquals("Id is not equal.", m1.getId(), m2.getId());
            assertEquals("Login is not equal.", m1.getLogin(), m2.getLogin());
            assertEquals("Mail port is not equal.", m1.getMailPort(), m2.getMailPort());
            assertEquals("Mail protocol is not equal.", m1.getMailProtocol(), m2.getMailProtocol());
            assertEquals("Mail server is not equal.", m1.getMailServer(), m2.getMailServer());
            assertEquals("Name is not equal.", m1.getName(), m2.getName());
            assertEquals("Password is not equal.", m1.getPassword(), m2.getPassword());
            assertEquals("Personal is not equal.", m1.getPersonal(), m2.getPersonal());
            assertEquals("Primary address is not equal.", m1.getPrimaryAddress(), m2.getPrimaryAddress());
            assertEquals("Sent is not equal.", m1.getSent(), m2.getSent());
            assertEquals("Sent full name is not equal.", m1.getSentFullname(), m2.getSentFullname());
            assertEquals("Spam is not equal.", m1.getSpam(), m2.getSpam());
            assertEquals("Spam full name is not equal.", m1.getSpamFullname(), m2.getSpamFullname());
            assertEquals("Spam handler is not equal.", m1.getSpamHandler(), m2.getSpamHandler());
            assertEquals("Transport login is not equal.", m1.getTransportLogin(), m2.getTransportLogin());
            assertEquals("Transport password is not equal.", m1.getTransportPassword(), m2.getTransportPassword());
            assertEquals("Transport port is not equal.", m1.getTransportPort(), m2.getTransportPort());
            assertEquals("Transport protocol is not equal.", m1.getTransportProtocol(), m2.getTransportProtocol());
            assertEquals("Transport server is not equal.", m1.getTransportServer(), m2.getTransportServer());
            assertEquals("Trash is not equal.", m1.getTrash(), m2.getTrash());
            assertEquals("Trash full name is not equal.", m1.getTrashFullname(), m2.getTrashFullname());
            assertEquals("Default flag is not equal.", m1.isDefaultFlag(), m2.isDefaultFlag());
            assertEquals("Mail secure is not equal.", m1.isMailSecure(), m2.isMailSecure());
            assertEquals("Transport secure is not equal.", m1.isTransportSecure(), m2.isTransportSecure());
            assertEquals("Unified inbox is not equal.", m1.isUnifiedINBOXEnabled(), m2.isUnifiedINBOXEnabled());
        }
    }

    private void compareSubscriptions(final JSONArray orig, final JSONArray copied) throws Exception {
        for (int i = 0; i < orig.length(); i++) {
            JSONArray origSub = orig.getJSONArray(i);
            JSONArray copiedSub = copied.getJSONArray(i);
            for (int j = 0; j < origSub.length(); j++) {
                if (j == 1) {
                    continue; //Skip folder id
                }
                assertEquals("Field " + j + " is not equal.", origSub.get(j), copiedSub.get(j));
            }
        }
    }

    private final class FolderComparator implements Comparator<FolderObject> {

        @Override
        public int compare(final FolderObject o1, final FolderObject o2) {
            return o1.getFolderName().compareTo(o2.getFolderName());
        }

    }

    private final class CommonObjectComparator implements Comparator<CommonObject> {

        @Override
        public int compare(final CommonObject o1, final CommonObject o2) {
            return o1.getObjectID() - o2.getObjectID();
        }

    }

    private final class ReminderObjectComparator implements Comparator<ReminderObject> {

        @Override
        public int compare(final ReminderObject o1, final ReminderObject o2) {
            return o1.getObjectId() - o2.getObjectId();
        }

    }

    private final class AttachmentComparator implements Comparator<AttachmentMetadata> {

        @Override
        public int compare(final AttachmentMetadata o1, final AttachmentMetadata o2) {
            return o1.getId() - o2.getId();
        }

    }

    private final class MailAccountComparator implements Comparator<MailAccountDescription> {

        @Override
        public int compare(final MailAccountDescription o1, final MailAccountDescription o2) {
            return o1.getId() - o2.getId();
        }

    }
}
