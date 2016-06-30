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

package com.openexchange.ajax.itip;

import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.writer.AppointmentWriter;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.AppointmentDiff.FieldUpdate;
import com.openexchange.calendar.CalendarField;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnnotation;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ParticipantChange;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.Difference;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ITipAnalysisWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ITipAnalysisWriter {

    private final AppointmentWriter appointmentWriter;

    public ITipAnalysisWriter(final TimeZone timezone, final ServerSession session) {
        super();
        this.appointmentWriter = new AppointmentWriter(timezone).setSession(session);
    }

    public void write(final ITipAnalysis analysis, final JSONObject object) throws JSONException {
    	if (analysis.getMessage() != null && analysis.getMessage().getMethod() != null) {
            object.put("messageType", analysis.getMessage().getMethod().toString().toLowerCase());
    	}
    	if (analysis.getUid() != null) {
    		object.put("uid", analysis.getUid());
    	}
        writeAnnotations(analysis, object);
        writeChanges(analysis, object);
        writeActions(analysis, object);
    }

    private void writeActions(final ITipAnalysis analysis, final JSONObject object) throws JSONException {
        final JSONArray actionsArray = new JSONArray();
        for(final ITipAction action : analysis.getActions()) {
            actionsArray.put(action.name().toLowerCase());
        }
        object.put("actions", actionsArray);
    }

    private void writeChanges(final ITipAnalysis analysis, final JSONObject object) throws JSONException {
        if (analysis.getChanges().isEmpty()) {
            return;
        }
        final JSONArray changesArray = new JSONArray();
        for(final ITipChange change : analysis.getChanges()) {
            final JSONObject changeObject = new JSONObject();
            writeChange(change, changeObject);
            changesArray.put(changeObject);
        }
        object.put("changes", changesArray);
    }


    private void writeChange(final ITipChange change, final JSONObject changeObject) throws JSONException {
        if (change.getIntroduction() != null) {
            changeObject.put("introduction", change.getIntroduction());
        }

        changeObject.put("type", change.getType().name().toLowerCase());
        changeObject.put("exception", change.isException());
        final CalendarDataObject newAppointment = change.getNewAppointment();
        if (newAppointment != null) {
            final JSONObject newAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(newAppointment, newAppointmentObject);
            changeObject.put("newAppointment", newAppointmentObject);
        }

        final Appointment currentAppointment = change.getCurrentAppointment();
        if (currentAppointment != null) {
            final JSONObject currentAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(currentAppointment, currentAppointmentObject);
            changeObject.put("currentAppointment", currentAppointmentObject);
        }

        final CalendarDataObject masterAppointment = change.getMasterAppointment();
        if (masterAppointment != null) {
            final JSONObject masterAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(masterAppointment, masterAppointmentObject);
            changeObject.put("masterAppointment", masterAppointmentObject);
        }

        final Appointment deletedAppointment = change.getDeletedAppointment();
        if (deletedAppointment != null) {
            final JSONObject deletedAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(deletedAppointment, deletedAppointmentObject);
            changeObject.put("deletedAppointment", deletedAppointmentObject);
        }

        final List<Appointment> conflicts = change.getConflicts();
        if (conflicts != null && !conflicts.isEmpty()) {
            final JSONArray array = new JSONArray();
            for (final Appointment appointment : conflicts) {
                final JSONObject conflictObject = new JSONObject();
                appointmentWriter.writeAppointment(appointment, conflictObject);
                array.put(conflictObject);
            }
            changeObject.put("conflicts", array);
        }

        final ParticipantChange participantChange = change.getParticipantChange();
        // TODO

        final AppointmentDiff diff = change.getDiff();
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

    private void writeDiff(final JSONObject diffObject, final AppointmentDiff diff) throws JSONException {
        final List<FieldUpdate> updates = diff.getUpdates();
        for (final FieldUpdate fieldUpdate : updates) {
            final JSONObject difference = new JSONObject();

            writeField("old", fieldUpdate.getOriginalValue(), fieldUpdate.getFieldNumber(), fieldUpdate.getFieldName(), difference);
            writeField("new", fieldUpdate.getNewValue(), fieldUpdate.getFieldNumber(), fieldUpdate.getFieldName(), difference);

            final Object extraInfo = fieldUpdate.getExtraInfo();
            if (extraInfo != null) {
                final JSONObject extraInfoObject = new JSONObject();
                writeExtraInfo(extraInfo, extraInfoObject);
                difference.put("extra", extraInfoObject);
            }

            diffObject.put(fieldUpdate.getFieldName(), difference);

        }

    }

    private void writeField(final String key, final Object value, final int fieldNumber, final String fieldName, final JSONObject difference) throws JSONException {
        final CalendarDataObject calendarDataObject = new CalendarDataObject();
        calendarDataObject.set(fieldNumber, value);
        final JSONObject object = new JSONObject();
        appointmentWriter.writeAppointment(calendarDataObject, object);
        final Object opt = object.opt(fieldName);
        if (opt != null) {
            difference.put(key, opt);
        }
    }

    private void writeExtraInfo(final Object extraInfo, final JSONObject extraInfoObject) throws JSONException {
        if (!Difference.class.isInstance(extraInfo)) {
            return;
        }

        final Difference difference = (Difference) extraInfo;
        if (difference.getField() != Difference.COMMON) {
            JSONObject json = new JSONObject();
            CalendarDataObject calendarDataObject = new CalendarDataObject();
            calendarDataObject.set(difference.getField(), difference.getAdded());
            appointmentWriter.writeAppointment(calendarDataObject, json);
            extraInfoObject.put("added", json.get(CalendarField.getByColumn(difference.getField()).getJsonName()));

            json = new JSONObject();
            calendarDataObject = new CalendarDataObject();
            calendarDataObject.set(difference.getField(), difference.getRemoved());
            appointmentWriter.writeAppointment(calendarDataObject, json);
            extraInfoObject.put("removed", json.get(CalendarField.getByColumn(difference.getField()).getJsonName()));

            final List<Change> changed = difference.getChanged();
            final JSONArray jsonChanges = new JSONArray();
            for (final Change change : changed) {
                if (!ConfirmationChange.class.isInstance(change)) {
                    continue;
                }
                final ConfirmationChange confirmationChange = (ConfirmationChange) change;
                final JSONObject jsonChange = new JSONObject();
                jsonChange.put("id", confirmationChange.getIdentifier());
                if (confirmationChange.getNewMessage() != null) {
                    jsonChange.put("oldMessage", confirmationChange.getOldMessage());
                    jsonChange.put("newMessage", confirmationChange.getNewMessage());
                }
                if (confirmationChange.getNewStatus() != -1) {
                    jsonChange.put("oldStatus", confirmationChange.getOldStatus());
                    jsonChange.put("newStatus", confirmationChange.getNewStatus());
                }
                jsonChanges.put(jsonChange);
            }

            if (jsonChanges.length() > 0) {
                extraInfoObject.put("changed", jsonChanges);
            }
        }
    }

    private void writeAnnotations(final ITipAnalysis analysis, final JSONObject object) throws JSONException {
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

        object.put("annotations",array);
    }


    private void writeAnnotation(final ITipAnnotation annotation, final JSONObject annotationObject) throws JSONException {
        annotationObject.put("message", annotation.getMessage());
        // TOOD: i18n and message args
        final Appointment appointment = annotation.getAppointment();
        if (appointment != null) {
            final JSONObject appointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(appointment, appointmentObject);
            annotationObject.put("appointment", appointmentObject);
        }
    }

}
