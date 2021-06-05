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

package com.openexchange.chronos.itip.json.actions;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.calendar.json.AppointmentAJAXRequest;
import com.openexchange.calendar.json.actions.AppointmentAction;
import com.openexchange.calendar.json.actions.chronos.EventConverter;
import com.openexchange.calendar.json.compat.Appointment;
import com.openexchange.calendar.json.compat.AppointmentParser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.itip.generators.ITipMailGenerator;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.itip.tools.ITipUtils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 *
 * {@link ShowMailAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ShowMailAction extends AppointmentAction {

    /**
     * Initializes a new {@link ShowMailAction}.
     *
     * @param services
     */
    public ShowMailAction(ServiceLookup services) {
        super(services);
        this.services = services;
    }

    private final ServiceLookup services;

    @Override
    public AJAXRequestResult perform(CalendarSession session, AppointmentAJAXRequest request) throws OXException {
        try {
            // Parse Appointments
            final Event[] appointments = parse(request, session);
            if (appointments.length == 0) {
                final JSONObject object = new JSONObject();
                object.put("text", "Please send some appointments in the body");
                object.put("html", "Please send some appointments in the body");
                return new AJAXRequestResult(object);
            }
            // Generate Mail
            final NotificationMail mail = generateMail(appointments, request, session);
            if (null == mail) {
                throw OXException.general("Mail could not be generated");
            }

            // Put text and html into the response
            final JSONObject object = new JSONObject();
            object.put("text", mail.getText());
            object.put("html", mail.getHtml());

            return new AJAXRequestResult(object);
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x);
        }
    }

    private Event[] parse(final AppointmentAJAXRequest request, final CalendarSession session) throws OXException, JSONException {
        final AppointmentParser parser = new AppointmentParser(true, TimeZone.getTimeZone(new ServerSessionAdapter(session.getSession()).getUser().getTimeZone()));

        final Object data = request.getData();
        if (data == null) {
            return new Event[0];
        }

        EventConverter eventConverter = getEventConverter(session);

        if (data instanceof JSONObject) {
            final JSONObject appointment = (JSONObject) data;
            final Appointment parsed = new Appointment();
            parser.parse(parsed, appointment);
            correctConfirm(parsed, appointment);

            return new Event[] { eventConverter.getEvent(parsed, null) };
        }

        if (data instanceof JSONArray) {
            final JSONArray appointments = (JSONArray) data;
            final List<Appointment> list = new ArrayList<Appointment>();
            for (int i = 0, size = appointments.length(); i < size; i++) {
                final JSONObject object = appointments.getJSONObject(i);
                final Appointment parsed = new Appointment();
                parser.parse(parsed, object);
                correctConfirm(parsed, object);
                list.add(parsed);
            }
            return list.toArray(new Event[list.size()]);
        }

        return new Event[0];
    }

    private void correctConfirm(final Appointment parsed, final JSONObject object) throws JSONException {
        final Map<String, Integer> confirmMap = new HashMap<String, Integer>();
        if (object.has("participants")) {
            final JSONArray array = object.getJSONArray("participants");
            for (int i = 0, size = array.length(); i < size; i++) {
                final JSONObject participantSpec = array.getJSONObject(i);
                if (participantSpec.has("confirm")) {
                    final Integer confirm = I(participantSpec.getInt("confirm"));
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
            for (int i = 0, size = array.length(); i < size; i++) {
                final JSONObject participantSpec = array.getJSONObject(i);
                if (participantSpec.has("confirm")) {
                    final Integer confirm = I(participantSpec.getInt("confirm"));
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
            for (int i = 0, size = array.length(); i < size; i++) {
                final JSONObject participantSpec = array.getJSONObject(i);
                if (participantSpec.has("status")) {
                    final Integer confirm = I(participantSpec.getInt("status"));
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
                        up.setConfirm(confirmStatus.intValue());
                    }

                }

                if (participant instanceof ExternalUserParticipant) {
                    final ExternalUserParticipant ep = (ExternalUserParticipant) participant;
                    Integer confirmStatus = confirmMap.get(Integer.toString(ep.getIdentifier()));
                    if (confirmStatus == null) {
                        confirmStatus = confirmMap.get(ep.getEmailAddress().toLowerCase());
                    }

                    if (confirmStatus != null) {
                        ep.setConfirm(confirmStatus.intValue());
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
                    up.setConfirm(confirmStatus.intValue());
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
                    ep.setStatus(ConfirmStatus.byId(confirmStatus.intValue()));
                }
            }
        }

    }

    private NotificationMail generateMail(final Event[] appointments, final AppointmentAJAXRequest request, final CalendarSession session) throws OXException {
        final ITipMailGeneratorFactory service = services.getService(ITipMailGeneratorFactory.class);

        final Event original = (appointments.length > 1) ? appointments[0] : null;
        final Event appointment = (appointments.length > 1) ? appointments[1] : appointments[0];

        final ITipMailGenerator generator = service.create(original, appointment, session, session.getUserId(), ITipUtils.getPrincipal(session));

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
