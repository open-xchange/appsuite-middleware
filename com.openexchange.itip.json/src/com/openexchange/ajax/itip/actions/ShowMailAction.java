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

package com.openexchange.ajax.itip.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.parser.AppointmentParser;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.itip.generators.ITipMailGenerator;
import com.openexchange.calendar.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ShowMailAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ShowMailAction implements AJAXActionService{

    private final ServiceLookup services;

    public ShowMailAction(final ServiceLookup services) {
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            // Parse Appointments
            final Appointment[] appointments = parse(request, session);
            if (appointments.length == 0) {
                final JSONObject object = new JSONObject();
                object.put("text", "Please send some appointments in the body");
                object.put("html", "Please send some appointments in the body");
                return new AJAXRequestResult(object);
            }
            // Generate Mail
            final NotificationMail mail = generateMail(appointments, request, session);

            // Put text and html into the response
            final JSONObject object = new JSONObject();
            object.put("text", mail.getText());
            object.put("html", mail.getHtml());

            return new AJAXRequestResult(object);
        } catch (final JSONException x) {
        	throw AjaxExceptionCodes.JSON_ERROR.create(x);
        }
    }

    private Appointment[] parse(final AJAXRequestData request, final ServerSession session) throws OXException, JSONException {
        final AppointmentParser parser = new AppointmentParser(true, TimeZone.getTimeZone(session.getUser().getTimeZone()));

        final Object data = request.getData();
        if (data == null) {
            return new Appointment[0];
        }

        if (data instanceof JSONObject) {
            final JSONObject appointment = (JSONObject) data;
            final Appointment parsed = new Appointment();
            parser.parse(parsed, appointment);
            correctConfirm(parsed, appointment);
            return new Appointment[]{parsed};
        }

        if (data instanceof JSONArray) {
            final JSONArray appointments = (JSONArray) data;
            final List<Appointment> list = new ArrayList<Appointment>();
            for(int i = 0, size = appointments.length(); i < size; i++) {
                final JSONObject object = appointments.getJSONObject(i);
                final Appointment parsed = new Appointment();
                parser.parse(parsed, object);
                correctConfirm(parsed, object);
                list.add(parsed);
            }
            return list.toArray(new Appointment[list.size()]);
        }

        return new Appointment[0];
    }

    private void correctConfirm(final Appointment parsed, final JSONObject object) throws JSONException {
        final Map<String, Integer> confirmMap = new HashMap<String, Integer>();
        if (object.has("participants")) {
            final JSONArray array = object.getJSONArray("participants");
            for(int i = 0, size = array.length(); i < size; i++) {
                final JSONObject participantSpec = array.getJSONObject(i);
                if (participantSpec.has("confirm")) {
                    final int confirm = participantSpec.getInt("confirm");
                    if (participantSpec.has("id")) {
                        confirmMap.put(participantSpec.get("id").toString(), confirm);
                    }
                    if (participantSpec.has("mail")) {
                        confirmMap.put(participantSpec.getString("mail").toLowerCase(), confirm);
                    }
                }
            }
        }

        if (object.has("users")) {
            final JSONArray array = object.getJSONArray("users");
            for(int i = 0, size = array.length(); i < size; i++) {
                final JSONObject participantSpec = array.getJSONObject(i);
                if (participantSpec.has("confirm")) {
                    final int confirm = participantSpec.getInt("confirm");
                    if (participantSpec.has("id")) {
                        confirmMap.put(participantSpec.get("id").toString(), confirm);
                    }
                    if (participantSpec.has("mail")) {
                        confirmMap.put(participantSpec.getString("mail").toLowerCase(), confirm);
                    }
                }
            }
        }

        if (object.has("confirmations")) {
            final JSONArray array = object.getJSONArray("confirmations");
            for(int i = 0, size = array.length(); i < size; i++) {
                final JSONObject participantSpec = array.getJSONObject(i);
                if (participantSpec.has("status")) {
                    final int confirm = participantSpec.getInt("status");
                    if (participantSpec.has("id")) {
                        confirmMap.put(participantSpec.get("id").toString(), confirm);
                    }
                    if (participantSpec.has("mail")) {
                        confirmMap.put(participantSpec.getString("mail").toLowerCase(), confirm);
                    }
                }
            }
        }


        final Participant[] participants = parsed.getParticipants();
        if (participants != null) {
            for (final Participant participant : participants) {
                if (participant instanceof UserParticipant) {
                    final UserParticipant up = (UserParticipant) participant;
                    Integer confirmStatus = confirmMap.get(Integer.toString(up.getIdentifier()));
                    if (confirmStatus == null) {
                        confirmStatus = confirmMap.get(up.getEmailAddress().toLowerCase());
                    }

                    if (confirmStatus != null) {
                        up.setConfirm(confirmStatus);
                    }

                }

                if (participant instanceof ExternalUserParticipant) {
                    final ExternalUserParticipant ep = (ExternalUserParticipant) participant;
                    Integer confirmStatus = confirmMap.get(Integer.toString(ep.getIdentifier()));
                    if (confirmStatus == null) {
                        confirmStatus = confirmMap.get(ep.getEmailAddress().toLowerCase());
                    }

                    if (confirmStatus != null) {
                        ep.setConfirm(confirmStatus);
                    }
                }
            }
        }

        final UserParticipant[] users = parsed.getUsers();
        if (users != null) {
            for (final UserParticipant up : users) {
                Integer confirmStatus = confirmMap.get(Integer.toString(up.getIdentifier()));
                if (confirmStatus == null && up.getEmailAddress() != null) {
                    confirmStatus = confirmMap.get(up.getEmailAddress().toLowerCase());
                }

                if (confirmStatus != null) {
                    up.setConfirm(confirmStatus);
                }
            }
        }

        final ConfirmableParticipant[] confirmations = parsed.getConfirmations();
        if (confirmations != null) {
            for (final ConfirmableParticipant ep : confirmations) {
                Integer confirmStatus = confirmMap.get(Integer.toString(ep.getIdentifier()));
                if (confirmStatus == null) {
                    confirmStatus = confirmMap.get(ep.getEmailAddress().toLowerCase());
                }

                if (confirmStatus != null) {
                    ep.setStatus(ConfirmStatus.byId(confirmStatus));
                }
            }
        }

    }

    private NotificationMail generateMail(final Appointment[] appointments, final AJAXRequestData request, final ServerSession session) throws OXException {
        final ITipMailGeneratorFactory service = services.getService(ITipMailGeneratorFactory.class);

        final Appointment original = (appointments.length > 1) ? appointments[0] : null;
        final Appointment appointment = (appointments.length > 1) ? appointments[1] : appointments[0];

        final ITipMailGenerator generator = service.create(original, appointment, session, session.getUserId());

        final String type = request.getParameter("type");
        if (type.equalsIgnoreCase("create")) {
            return generator.generateCreateMailFor(request.getParameter("mail"));
        } else if (type.equalsIgnoreCase("update")) {
            return generator.generateUpdateMailFor(request.getParameter("mail"));
        } else if (type.equalsIgnoreCase("createException")) {
            return generator.generateCreateExceptionMailFor(request.getParameter("mail"));
        } else if (type.equalsIgnoreCase("delete")) {
            return generator.generateDeleteMailFor(request.getParameter("mail"));
        }

        return null;
    }

}
