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

package com.openexchange.chronos.itip.json;

import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.calendar.json.actions.chronos.DefaultEventConverter;
import com.openexchange.calendar.json.compat.Appointment;
import com.openexchange.calendar.json.compat.AppointmentWriter;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ITipAnalysisWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ITipAnalysisWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(ITipAnalysisWriter.class);

    private final AppointmentWriter appointmentWriter;
    private final DefaultEventConverter   eventConverter;

    public ITipAnalysisWriter(final TimeZone timezone, final CalendarSession session, ServiceLookup services) throws OXException {
        super();
        this.appointmentWriter = new AppointmentWriter(timezone).setSession(new ServerSessionAdapter(session.getSession()));
        eventConverter = new DefaultEventConverter(services, session);
    }

    public void write(final ITipAnalysis analysis, final JSONObject object) throws JSONException, OXException {
        if (analysis.getMessage() != null && analysis.getMessage().getMethod() != null) {
            object.put("messageType", analysis.getMessage().getMethod().toString().toLowerCase());
        }
        if (analysis.getUid() != null) {
            object.put("uid", analysis.getUid());
        }
        writeAnnotations(analysis, object);
        writeChanges(analysis, object);
        writeActions(analysis, object);
        writeAttributes(analysis, object);
    }

    private void writeAttributes(final ITipAnalysis analysis, final JSONObject object) throws JSONException {
        if (analysis.getAttributes() == null || analysis.getAttributes().isEmpty()) {
            return;
        }

        JSONObject attributes = new JSONObject();
        for (Entry<String, Object> entry : analysis.getAttributes().entrySet()) {
            attributes.put(entry.getKey(), entry.getValue());
        }

        object.putOpt("attributes", attributes);
    }

    private void writeActions(final ITipAnalysis analysis, final JSONObject object) throws JSONException {
        final JSONArray actionsArray = new JSONArray();
        for (final ITipAction action : analysis.getActions()) {
            actionsArray.put(action.name().toLowerCase());
        }
        object.put("actions", actionsArray);
    }

    private void writeChanges(final ITipAnalysis analysis, final JSONObject object) throws JSONException, OXException {
        if (analysis.getChanges().isEmpty()) {
            return;
        }
        final JSONArray changesArray = new JSONArray();
        for (final ITipChange change : analysis.getChanges()) {
            final JSONObject changeObject = new JSONObject();
            writeChange(change, changeObject);
            changesArray.put(changeObject);
        }
        object.put("changes", changesArray);
    }

    private void writeChange(final ITipChange change, final JSONObject changeObject) throws JSONException, OXException {
        if (change.getIntroduction() != null) {
            changeObject.put("introduction", change.getIntroduction());
        }

        changeObject.put("type", change.getType().name().toLowerCase());
        changeObject.put("exception", change.isException());
        final Event newAppointment = change.getNewEvent();
        if (newAppointment != null) {
            final JSONObject newAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(eventConverter.getAppointment(newAppointment), newAppointmentObject);
            changeObject.put("newAppointment", newAppointmentObject);
        }

        final Event currentAppointment = change.getCurrentEvent();
        if (currentAppointment != null) {
            final JSONObject currentAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(eventConverter.getAppointment(currentAppointment), currentAppointmentObject);
            changeObject.put("currentAppointment", currentAppointmentObject);
        }

        final Event masterAppointment = change.getMasterEvent();
        if (masterAppointment != null) {
            final JSONObject masterAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(eventConverter.getAppointment(masterAppointment), masterAppointmentObject);
            changeObject.put("masterAppointment", masterAppointmentObject);
        }

        final Event deletedAppointment = change.getDeletedEvent();
        if (deletedAppointment != null) {
            final JSONObject deletedAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(eventConverter.getAppointment(deletedAppointment), deletedAppointmentObject);
            changeObject.put("deletedAppointment", deletedAppointmentObject);
        }

        final List<EventConflict> conflicts = change.getConflicts();
        if (conflicts != null && !conflicts.isEmpty()) {
            final JSONArray array = new JSONArray();
            for (EventConflict conflict : conflicts) {
                final JSONObject conflictObject = new JSONObject();
                appointmentWriter.writeAppointment(eventConverter.getAppointment(conflict.getConflictingEvent()), conflictObject);
                array.put(conflictObject);
            }
            changeObject.put("conflicts", array);
        }

        final ITipEventUpdate diff = change.getDiff();
        if (diff != null) {
            final JSONObject diffObject = new JSONObject();
            writeDiff(diffObject, diff);
            changeObject.put("diff", diffObject);
        }

        final List<String> diffDescription = change.getDiffDescription();
        if (diff != null && diffDescription != null && !diffDescription.isEmpty()) {
            final JSONArray array = new JSONArray();
            for (final String description : diffDescription) {
                array.put(description);
            }
            changeObject.put("diffDescription", array);
        }
    }

    private void writeDiff(final JSONObject diffObject, final ITipEventUpdate diff) throws JSONException, OXException {
        // Convert event
        Appointment original = eventConverter.getAppointment(diff.getOriginal());
        Appointment updated = eventConverter.getAppointment(diff.getUpdate());

        // Iterate over all columns and put diff into response
        for (int column : Appointment.ALL_COLUMNS) {
            try {
                if (original.contains(column) || updated.contains(column)) {
                    CalendarField calendarField = CalendarField.getByColumn(column);
                    if (null != calendarField) {
                        String fieldName = calendarField.getJsonName();
                        Object originalValue = original.get(column);
                        Object updatedValue = updated.get(column);
                        if (null == originalValue ? null != updatedValue : false == originalValue.equals(updatedValue)) {
                            JSONObject jDifference = new JSONObject(4);
                            writeField("old", originalValue, column, fieldName, jDifference);
                            writeField("new", updatedValue, column, fieldName, jDifference);

                            diffObject.put(fieldName, jDifference);
                        }
                    }
                }
            } catch (UnsupportedOperationException e) {
                // getUntil() and calculateRecurrence() not set for unlimited series
                LOGGER.debug("Could not convert field {}.", CalendarField.getByColumn(column).getJsonName(), e);
            }
        }
    }

    private void writeField(final String key, final Object value, final int fieldNumber, final String fieldName, final JSONObject jDifference) throws JSONException {
        final Appointment appointment = new Appointment();
        appointment.set(fieldNumber, value);
        final JSONObject json = new JSONObject();
        appointmentWriter.writeAppointment(appointment, json);
        final Object opt = json.opt(fieldName);
        if (opt != null) {
            jDifference.put(key, opt);
        }
    }

    private void writeAnnotations(final ITipAnalysis analysis, final JSONObject object) throws JSONException, OXException {
        final List<ITipAnnotation> annotations = analysis.getAnnotations();
        if (annotations == null || annotations.isEmpty()) {
            return;
        }
        final JSONArray array = new JSONArray();
        for (final ITipAnnotation annotation : annotations) {
            final JSONObject annotationObject = new JSONObject();
            writeAnnotation(annotation, annotationObject);
            array.put(annotationObject);
        }

        object.put("annotations", array);
    }

    private void writeAnnotation(final ITipAnnotation annotation, final JSONObject annotationObject) throws JSONException, OXException {
        annotationObject.put("message", annotation.getMessage());
        // TODO: i18n and message args
        final Event event = annotation.getEvent();
        if (event != null) {
            final JSONObject appointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(eventConverter.getAppointment(event), appointmentObject);
            annotationObject.put("appointment", appointmentObject);
        }
    }
}
