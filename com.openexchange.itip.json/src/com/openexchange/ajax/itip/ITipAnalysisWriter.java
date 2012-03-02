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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.Set;
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


/**
 * {@link ITipAnalysisWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ITipAnalysisWriter {
    private AppointmentWriter appointmentWriter;

    public ITipAnalysisWriter(TimeZone timezone) {
        this.appointmentWriter = new AppointmentWriter(timezone);
    }
    
    public void write(ITipAnalysis analysis, JSONObject object) throws JSONException {
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

    private void writeActions(ITipAnalysis analysis, JSONObject object) throws JSONException {
        JSONArray actionsArray = new JSONArray();
        for(ITipAction action : analysis.getActions()) {
            actionsArray.put(action.name().toLowerCase());
        }
        object.put("actions", actionsArray);
    }

    private void writeChanges(ITipAnalysis analysis, JSONObject object) throws JSONException {
        if (analysis.getChanges().isEmpty()) {
            return;
        }
        JSONArray changesArray = new JSONArray();
        for(ITipChange change : analysis.getChanges()) {
            JSONObject changeObject = new JSONObject();
            writeChange(change, changeObject);
            changesArray.put(changeObject);
        }
        object.put("changes", changesArray);
    }


    private void writeChange(ITipChange change, JSONObject changeObject) throws JSONException {
    	if (change.getIntroduction() != null) {
        	changeObject.put("introduction", change.getIntroduction());
    	}
    	
        changeObject.put("type", change.getType().name().toLowerCase());
        changeObject.put("exception", change.isException());
        CalendarDataObject newAppointment = change.getNewAppointment();
        if (newAppointment != null) {
            JSONObject newAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(newAppointment, newAppointmentObject);
            changeObject.put("newAppointment", newAppointmentObject);
        }
        
        Appointment currentAppointment = change.getCurrentAppointment();
        if (currentAppointment != null) {
            JSONObject currentAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(currentAppointment, currentAppointmentObject);
            changeObject.put("currentAppointment", currentAppointmentObject);
        }
        
        CalendarDataObject masterAppointment = change.getMasterAppointment();
        if (masterAppointment != null) {
            JSONObject masterAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(masterAppointment, masterAppointmentObject);
            changeObject.put("masterAppointment", masterAppointmentObject);
        }
        
        Appointment deletedAppointment = change.getDeletedAppointment();
        if (deletedAppointment != null) {
            JSONObject deletedAppointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(deletedAppointment, deletedAppointmentObject);
            changeObject.put("deletedAppointment", deletedAppointmentObject);
        }
        
        List<Appointment> conflicts = change.getConflicts();
        if (conflicts != null && !conflicts.isEmpty()) {
            JSONArray array = new JSONArray();
            for (Appointment appointment : conflicts) {
                JSONObject conflictObject = new JSONObject();
                appointmentWriter.writeAppointment(appointment, conflictObject);
                array.put(conflictObject);
            }
            changeObject.put("conflicts", array);
        }
        
        ParticipantChange participantChange = change.getParticipantChange();
        // TODO
        
        AppointmentDiff diff = change.getDiff();
        if (diff != null) {
            JSONObject diffObject = new JSONObject();
            writeDiff(diffObject, diff);
            changeObject.put("diff", diffObject);
        }
        
        List<String> diffDescription = change.getDiffDescription();
        if (diff != null && diffDescription != null && !diffDescription.isEmpty()) {
            JSONArray array = new JSONArray();
            for (String description : diffDescription) {
                array.put(description);
            }
            changeObject.put("diffDescription", array);
        }
    }

    private void writeDiff(JSONObject diffObject, AppointmentDiff diff) throws JSONException {
        List<FieldUpdate> updates = diff.getUpdates();
        for (FieldUpdate fieldUpdate : updates) {
            JSONObject difference = new JSONObject();

            writeField("old", fieldUpdate.getOriginalValue(), fieldUpdate.getFieldNumber(), fieldUpdate.getFieldName(), difference);
            writeField("new", fieldUpdate.getNewValue(), fieldUpdate.getFieldNumber(), fieldUpdate.getFieldName(), difference);
            
            Object extraInfo = fieldUpdate.getExtraInfo();
            if (extraInfo != null) {
                JSONObject extraInfoObject = new JSONObject();
                writeExtraInfo(extraInfo, extraInfoObject);
                difference.put("extra", extraInfoObject);
            }
            
            diffObject.put(fieldUpdate.getFieldName(), difference);
            
        }
        
    }

    private void writeField(String key, Object value, int fieldNumber, String fieldName, JSONObject difference) throws JSONException {
        CalendarDataObject calendarDataObject = new CalendarDataObject();
        calendarDataObject.set(fieldNumber, value);
        JSONObject object = new JSONObject();
        appointmentWriter.writeAppointment(calendarDataObject, object);
        Object opt = object.opt(fieldName);
        if (opt != null) {
            difference.put(key, opt);
        }
    }

    private void writeExtraInfo(Object extraInfo, JSONObject extraInfoObject) throws JSONException {
        if (!Difference.class.isInstance(extraInfo)) {
            return;
        }
        
        Difference difference = (Difference) extraInfo;
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
            
            List<Change> changed = difference.getChanged();
            JSONArray jsonChanges = new JSONArray();
            for (Change change : changed) {
                if (!ConfirmationChange.class.isInstance(change)) {
                    continue;
                }
                ConfirmationChange confirmationChange = (ConfirmationChange) change;
                JSONObject jsonChange = new JSONObject();
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

    private void writeAnnotations(ITipAnalysis analysis, JSONObject object) throws JSONException {
        List<ITipAnnotation> annotations = analysis.getAnnotations();
        if (annotations == null || annotations.isEmpty()) {
            return;
        }
        JSONArray array = new JSONArray();
        for (ITipAnnotation annotation : annotations) {
            JSONObject annotationObject = new JSONObject();
            writeAnnotation(annotation, annotationObject);
            array.put(annotationObject);
        }
        
        object.put("annotations",array);
    }


    private void writeAnnotation(ITipAnnotation annotation, JSONObject annotationObject) throws JSONException {
        annotationObject.put("message", annotation.getMessage());
        // TOOD: i18n and message args
        Appointment appointment = annotation.getAppointment();
        if (appointment != null) {
            JSONObject appointmentObject = new JSONObject();
            appointmentWriter.writeAppointment(appointment, appointmentObject);
            annotationObject.put("appointment", appointmentObject);
        }
    }
}
