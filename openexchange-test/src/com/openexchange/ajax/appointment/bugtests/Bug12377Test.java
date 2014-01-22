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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.meterware.httpunit.WebConversation;
import com.openexchange.ajax.AttachmentClient;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonInsertResponse;
import com.openexchange.ajax.links.actions.AllRequest;
import com.openexchange.ajax.links.actions.AllResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.test.TestInit;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Bug12377Test extends AbstractAJAXSession {
    private Appointment appointment;
    private TimeZone timeZone;
    private Appointment exception;
    private File file;
    private Appointment linkedAppointment;

    /**
     * Default constructor.
     *
     * @param name name of the test.
     */
    public Bug12377Test(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        file = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        timeZone = getClient().getValues().getTimeZone();
        createAppointmentSeries();
        addAttachment();
        createAppointmentToLinkTo();
        addLink();
    }

    @Override
    public void tearDown() throws Exception {
        removeAppointment(appointment);
        removeAppointment(linkedAppointment);
        super.tearDown();
    }

    public void testShouldCopyAttachmentsWhenCreatingAnException() throws Exception {
        createException();
        verifyAttachments();
        verifyAttachmentCount();
        verifyLinks();
        verifyLinkCount();
    }

    private void createAppointmentSeries() throws JSONException, OXException, IOException, SAXException {
        this.appointment = new Appointment();
        appointment.setTitle("testBug12377");
        appointment.setStartDate(D("12/02/1999 10:00"));
        appointment.setEndDate(D("12/02/1999 12:00"));

        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setIgnoreConflicts(true);

        appointment.setParentFolderID(getPrivateAppointmentFolder());

        final InsertRequest request = new InsertRequest(appointment, timeZone);
        final CommonInsertResponse response = getClient().execute(request);
        response.fillObject(appointment);
        appointment.setLastModified(new Date(Long.MAX_VALUE));

    }

    private void createException() throws JSONException, OXException, IOException, SAXException {
        this.exception = new Appointment();
        exception.setStartDate(D("15/02/1999 13:00"));
        exception.setEndDate(D("15/02/1999 15:00"));
        exception.setRecurrencePosition(4);
        exception.setParentFolderID(appointment.getParentFolderID());
        exception.setObjectID(appointment.getObjectID());
        exception.setLastModified(appointment.getLastModified());

        final UpdateRequest request = new UpdateRequest(exception, timeZone);
        UpdateResponse response = getClient().execute(request);
        exception.setObjectID(response.getId());
        exception.setLastModified(new Date(Long.MAX_VALUE));
        appointment.setLastModified(new Date(Long.MAX_VALUE));

    }

    private void createAppointmentToLinkTo() throws JSONException, OXException, IOException, SAXException {
        this.linkedAppointment = new Appointment();
        linkedAppointment.setTitle("testBug12377 link to me");
        linkedAppointment.setStartDate(D("12/02/1999 16:00"));
        linkedAppointment.setEndDate(D("12/02/1999 17:00"));

        linkedAppointment.setIgnoreConflicts(true);

        linkedAppointment.setParentFolderID(getPrivateAppointmentFolder());

        final InsertRequest request = new InsertRequest(linkedAppointment, timeZone);
        final CommonInsertResponse response = getClient().execute(request);
        response.fillObject(linkedAppointment);
        linkedAppointment.setLastModified(new Date(Long.MAX_VALUE));
    }

    private void addAttachment() throws JSONException, IOException {
        AJAXClient client = getClient();
        AJAXSession session = client.getSession();
        WebConversation conversation = session.getConversation();
        String sessionId = session.getId();
        File file = getFile();

        AttachmentClient.attach(conversation, sessionId, appointment.getParentFolderID(), appointment.getObjectID(), Types.APPOINTMENT, file );
    }

    private void addLink() throws JSONException, OXException, IOException, SAXException {
        LinkObject link = new LinkObject();
        link.setLink(appointment.getObjectID(), Types.APPOINTMENT, appointment.getParentFolderID(), linkedAppointment.getObjectID(), Types.APPOINTMENT, linkedAppointment.getParentFolderID(), -1 );
        com.openexchange.ajax.links.actions.InsertRequest request = new com.openexchange.ajax.links.actions.InsertRequest(link, true);
        getClient().execute(request);
    }

    private void removeAppointment(Appointment appointment) throws JSONException, OXException, IOException, SAXException {
        appointment.setLastModified(new Date(Long.MAX_VALUE));
        DeleteRequest delete = new DeleteRequest(appointment);
        getClient().execute(delete);
    }


    private void verifyAttachments() throws JSONException, IOException, SAXException {
        AJAXClient client = getClient();
        AJAXSession session = client.getSession();
        WebConversation conversation = session.getConversation();
        String sessionId = session.getId();

        Response response = AttachmentClient.all(conversation, sessionId, exception.getParentFolderID(), exception.getObjectID(), Types.APPOINTMENT, new int[]{AttachmentField.FILENAME}, AttachmentField.FILENAME, "ASC");
        assertFalse(response.hasError());
        JSONArray arr = (JSONArray) response.getData();
        boolean found = false;
        for(int i = 0, size = arr.length(); i < size; i++) {
            JSONArray row = arr.getJSONArray(i);
            String filename = row.getString(0);
            if(filename.equals(getFile().getName())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private Appointment reloadException() throws JSONException, OXException, IOException, SAXException, OXException {
        AJAXClient client = getClient();
        GetRequest get = new GetRequest(exception.getParentFolderID(), exception.getObjectID());
        GetResponse response = client.execute(get);
        return response.getAppointment(TimeZone.getTimeZone("UTC"));
    }

    private void verifyAttachmentCount() throws JSONException, OXException, IOException, SAXException, OXException {
        Appointment reloadedException = reloadException();
        assertEquals(1, reloadedException.getNumberOfAttachments());
    }

    private void verifyLinks() throws JSONException, OXException, IOException, SAXException {
        AllRequest request = new AllRequest(exception.getObjectID(), Types.APPOINTMENT, exception.getParentFolderID(), true);
        AllResponse response = getClient().execute(request);
        LinkObject[] loadedLinks = response.getLinks();
        assertEquals(1, loadedLinks.length);

    }

    private void verifyLinkCount() throws JSONException, OXException, OXException, IOException, SAXException {
        // Unused and Dysfunctional
    }

    public int getPrivateAppointmentFolder() throws JSONException, OXException, IOException, SAXException {
        return getClient().getValues().getPrivateAppointmentFolder();
    }

    public File getFile() {
        return file;
    }
}
