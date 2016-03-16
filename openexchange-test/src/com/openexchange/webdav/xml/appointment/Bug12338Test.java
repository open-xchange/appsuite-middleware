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

package com.openexchange.webdav.xml.appointment;

import static com.openexchange.webdav.xml.XmlServlet.NS;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.jdom2.Element;
import org.jdom2.Namespace;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.webdav.AbstractWebDAVSession;
import com.openexchange.webdav.xml.appointment.actions.InsertRequest;
import com.openexchange.webdav.xml.appointment.actions.InsertResponse;
import com.openexchange.webdav.xml.framework.WebDAVClient;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12338Test extends AbstractWebDAVSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug12338Test.class);

    public Bug12338Test(final String name) {
        super(name);
    }

    public void testStrangeExternal() throws Throwable {
        final WebDAVClient client = getClient();
        final FolderObject folder = client.getFolderTools().getDefaultAppointmentFolder();
        final TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        final Calendar calendar = Calendar.getInstance(tz);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final Appointment appointment = new Appointment();
        appointment.setTitle("Test appointment for bug 12338");
        appointment.setParentFolderID(folder.getObjectID());
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.setIgnoreConflicts(true);
        appointment.setParticipants(new Participant[] {
            new UserParticipant(client.getGroupUserTools().getUserId()),
            new ExternalUserParticipant("/O=COMPARIS/OU=FIRST ADMINISTRATIVE GROUP/CN=RECIPIENTS/CN=Johann.burkhard")
        });
        final InsertRequest request = new SpecialInsertRequest(appointment, client.getGroupUserTools().getUserId());
        final InsertResponse response = client.execute(request);
        response.fillObject(appointment);
        LOG.info("Identifier: " + appointment.getObjectID());
        LOG.info("Timestamp: " + appointment.getLastModified());
//        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, PROTOCOL + getHostName(), getLogin(), getPassword());
    }

    private class SpecialInsertRequest extends InsertRequest {

        private final int userId;

        public SpecialInsertRequest(final Appointment appointment, final int userId) {
            super(appointment);
            this.userId = userId;
        }

        @Override
        protected Element createProp() throws OXException, IOException {
            final Element eProp = super.createProp();
            eProp.removeChild("participants", NS);
            final Element participants = new Element("participants", NS);
            eProp.addContent(participants);
//            participants.addContent(new Text(
//                "<ox:user " +
//                "xmlns:ox=\"http://www.open-xchange.org\" ox:displayname=\"comparis.ch - Ralf Beyeler\" " +
//                "xmlns:ox=\"http://www.open-xchange.org\" ox:external=\"true\" " +
//                "xmlns:ox=\"http://www.open-xchange.org\" ox:confirm=\"none\" " +
//                "xmlns:ox=\"http://www.open-xchange.org\" >/O=COMPARIS/OU=FIRST ADMINISTRATIVE GROUP/CN=RECIPIENTS/CN=Ralf.beyeler" +
//                "</ox:user>" +
//                "<ox:user " +
//                "xmlns:ox=\"http://www.open-xchange.org\" ox:external=\"false\" " +
//                "xmlns:ox=\"http://www.open-xchange.org\" ox:confirm=\"accept\" " +
//                "xmlns:ox=\"http://www.open-xchange.org\" >" + userId +
//                "</ox:user>"));
            final Element user1 = new Element("user", createNS());
            participants.addContent(user1);
            user1.addNamespaceDeclaration(createNS());
            user1.setAttribute("displayname", "comparis.ch - Ralf Beyeler", createNS());
            user1.addNamespaceDeclaration(createNS());
            user1.setAttribute("external", "true", createNS());
            user1.addNamespaceDeclaration(createNS());
            user1.setAttribute("confirm", "none", createNS());
            user1.setText("/O=COMPARIS/OU=FIRST ADMINISTRATIVE GROUP/CN=RECIPIENTS/CN=Ralf.beyeler");
            final Element user2 = new Element("user", createNS());
            participants.addContent(user2);
            user2.setAttribute("external", "false", NS);
            user2.setAttribute("confirm", "accept", NS);
            user2.setText(String.valueOf(userId));
            return eProp;
        }

        private final Namespace createNS() {
            return Namespace.getNamespace("ox", "http://www.open-xchange.org");
        }
    }
}
