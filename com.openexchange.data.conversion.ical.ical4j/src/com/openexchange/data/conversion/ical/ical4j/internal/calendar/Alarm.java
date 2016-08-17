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
package com.openexchange.data.conversion.ical.ical4j.internal.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.RelType;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RelatedTo;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.Mode;
import com.openexchange.data.conversion.ical.ical4j.internal.AbstractVerifyingAttributeConverter;
import com.openexchange.data.conversion.ical.ical4j.internal.EmitterTools;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Strings;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Alarm<T extends CalendarComponent, U extends CalendarObject> extends AbstractVerifyingAttributeConverter<T, U> {

    @Override
    public boolean isSet(U calendar) {
        return true;
    }

    @Override
    public void emit(Mode mode, int index, U calendar, T component, List<ConversionWarning> warnings, Context ctx, Object... args) throws ConversionError {
        if (Task.class.isAssignableFrom(calendar.getClass())) {
            emitTaskAlarm((Task) calendar, (VToDo) component, warnings);
        }  else if (Appointment.class.isAssignableFrom(calendar.getClass())) {
            emitAppointmentAlarm((Appointment) calendar, (VEvent) component, warnings);
        }
    }

    private void emitAppointmentAlarm(Appointment appointment, VEvent component, List<ConversionWarning> warnings) {
        if (false == appointment.containsAlarm() || -1 == appointment.getAlarm()) {
            if (Boolean.TRUE.equals(appointment.getProperty("com.openexchange.data.conversion.ical.alarm.emptyDefaultAlarm"))) {
                /*
                 * insert a dummy alarm to prevent Apple clients from adding it's own default alarms
                 */
                DateTime trigger = new DateTime(true);
                trigger.setTime(197168145000L); // 19760401T005545Z
                VAlarm vAlarm = new VAlarm(trigger);
                String uid = UUID.randomUUID().toString().toUpperCase();
                vAlarm.getProperties().add(new XProperty("X-WR-ALARMUID", uid));
                vAlarm.getProperties().add(new XProperty("UID", uid));
                vAlarm.getProperties().add(new XProperty("X-APPLE-LOCAL-DEFAULT-ALARM", "TRUE"));
                vAlarm.getProperties().add(new XProperty("ACTION", "NONE"));
                vAlarm.getProperties().add(new XProperty("X-APPLE-DEFAULT-ALARM", "TRUE"));
                component.getAlarms().add(vAlarm);
            }
            return;
        }
        /*
         * add VALARM component for stored reminder
         */
        VAlarm vAlarm = new VAlarm(new Dur(0, 0, -1 * appointment.getAlarm(), 0));
        vAlarm.getProperties().add(Action.DISPLAY);
        String description = Strings.isEmpty(appointment.getNote()) ? "Alarm" : appointment.getNote();
        vAlarm.getProperties().add(new Description(description));
        Date acknowledgedDate = appointment.getProperty("com.openexchange.data.conversion.ical.alarm.acknowledged");
        if (null != acknowledgedDate) {
            /*
             * store ACKNOWLEDGED & X-MOZ-LASTACK date in alarm
             */
            vAlarm.getProperties().add(new XProperty("ACKNOWLEDGED", formatAsUTC(acknowledgedDate)));
            vAlarm.getProperties().add(new XProperty("X-MOZ-LASTACK", formatAsUTC(acknowledgedDate)));
            /*
             * also store X-MOZ-LASTACK in parent component for recurring appointments
             */
            if (appointment.isMaster()) {
                component.getProperties().add(new XProperty("X-MOZ-LASTACK", formatAsUTC(acknowledgedDate)));
            }
        }
        component.getAlarms().add(vAlarm);
        Date snoozeDate = appointment.getProperty("com.openexchange.data.conversion.ical.alarm.snooze");
        if (null != snoozeDate) {
            /*
             * insert additional VALARM for snoozed alarm, linked to default alarm via UID and RELTYPE=SNOOZE
             */
            String uid = UUID.randomUUID().toString();
            vAlarm.getProperties().add(new Uid(uid));
            VAlarm snoozeAlarm = new VAlarm(EmitterTools.toDateTime(snoozeDate));
            snoozeAlarm.getProperties().add(Action.DISPLAY);
            snoozeAlarm.getProperties().add(new Description(description));
            RelatedTo relatedTo = new RelatedTo(uid);
            relatedTo.getParameters().add(new RelType("SNOOZE"));
            snoozeAlarm.getProperties().add(relatedTo);
            component.getAlarms().add(snoozeAlarm);
        }
        Date mozSnoozeDate = appointment.getProperty("com.openexchange.data.conversion.ical.alarm.mozSnooze");
        if (null != mozSnoozeDate) {
            /*
             * insert appropriate X-MOZ-SNOOZE-TIME / X-MOZ-SNOOZE-TIME-<timestamp> in parent component
             */
            Date mozSnoozeTimestamp = appointment.getProperty("com.openexchange.data.conversion.ical.alarm.mozSnoozeTimestamp");
            if (null != mozSnoozeTimestamp) {
                component.getProperties().add(new XProperty("X-MOZ-SNOOZE-TIME-" + String.valueOf(mozSnoozeTimestamp.getTime()) + "000", formatAsUTC(mozSnoozeDate)));
            } else {
                component.getProperties().add(new XProperty("X-MOZ-SNOOZE-TIME", formatAsUTC(mozSnoozeDate)));
            }
        }
        Integer relativeSnooze = appointment.getProperty("com.openexchange.data.conversion.ical.alarm.relativeSnooze");
        if (null != relativeSnooze) {
            /*
             * insert additional VALARM for snoozed alarm, linked to default alarm via UID and RELTYPE=SNOOZE
             */
            String uid = UUID.randomUUID().toString();
            vAlarm.getProperties().add(new Uid(uid));
            VAlarm snoozeAlarm = new VAlarm(new Dur(0, 0, relativeSnooze.intValue() / 60, relativeSnooze.intValue() % 60).negate());
            snoozeAlarm.getProperties().add(Action.DISPLAY);
            snoozeAlarm.getProperties().add(new Description(description));
            RelatedTo relatedTo = new RelatedTo(uid);
            relatedTo.getParameters().add(new RelType("SNOOZE"));
            snoozeAlarm.getProperties().add(relatedTo);
            component.getAlarms().add(snoozeAlarm);
        }
    }

    private void emitTaskAlarm(final Task task, final VToDo component, final List<ConversionWarning> warnings) {
        if(task.getAlarm() == null) {
            return;
        }
        final VAlarm alarm = new VAlarm();
        final Trigger trigger = new Trigger(EmitterTools.toDateTime(task.getAlarm()));
        alarm.getProperties().add(trigger);

        final Action action = new Action("DISPLAY");
        alarm.getProperties().add(action);

        String note = task.getNote();
        if(note == null) { note = "Open-XChange"; }

        final Description description = new Description(note);
        alarm.getProperties().add(description);

        component.getAlarms().add(alarm);
    }


    @Override
    public boolean hasProperty(final T t) {
        return true; // Not strictly true, but to inlcude the warning we have to enter #parse always
    }

    @Override
    public void parse(int index, T component, U calendarObject, TimeZone timeZone, Context ctx, List<ConversionWarning> warnings) throws ConversionError {
        List<VAlarm> vAlarms = getDisplayAlarms(index, component, warnings);
        if (null == vAlarms || 0 == vAlarms.size()) {
            applyAlarm(index, calendarObject, null, null, null, null, warnings);
            return;
        }
        Date selectedTrigger = null;
        Date selectedAcknowledged = null;
        Date selectedSnooze = null;
        Integer selectedRelativeSnooze = null;
        for (VAlarm vAlarm : vAlarms) {
            /*
             * determine trigger & acknowledged times from alarm component
             */
            Date triggerDate = parseTriggerDate(index, vAlarm.getTrigger(), component, timeZone, warnings);
            Date acknowledged = parseAcknowledgedDate(index, vAlarm, triggerDate, timeZone, warnings);
            /*
             * also consider mozilla x-props from parent component (used for recurring appointments)
             */
            Date parentAcknowledged = parseMozillaAcknowledgedDate(index, component, triggerDate, timeZone, warnings);
            if (null != parentAcknowledged && (null == acknowledged || acknowledged.before(parentAcknowledged))) {
                acknowledged = parentAcknowledged;
            }
            /*
             * check if this is a snoozed alarm: RELTYPE=SNOOZE, X-MOZ-SNOOZE in alarm component, or trigger-date equal to parent
             * component's X-MOZ-SNOOZE property
             */
            Date snooze = null;
            Integer relativeSnooze = null;
            Trigger snoozeTrigger = getSnoozeByRelated(index, vAlarm, vAlarms, component, timeZone, warnings);
            if (null != snoozeTrigger) {
                Dur duration = snoozeTrigger.getDuration();
                if (null != duration) {
                    relativeSnooze = Integer.valueOf((((duration.getWeeks() * 7 + duration.getDays()) * 24  + duration.getHours()) * 60 + duration.getMinutes()) * 60 + duration.getSeconds());
                } else {
                    snooze = parseTriggerDate(index, snoozeTrigger, component, timeZone, warnings);
                }
            } else {
                Date parentSnooze = parseMozillaSnooze(index, component, warnings);
                if (null != parentSnooze && parentSnooze.equals(triggerDate)) {
                    snooze = parentSnooze;
                } else {
                    Date parentSnoozeTime = parseMozillaSnoozeTime(index, component, warnings);
                    if (null != parentSnoozeTime) {
                        snooze = parentSnoozeTime;
                    }
                }
            }
            if (null != relativeSnooze) {
                if (null == selectedRelativeSnooze || relativeSnooze.intValue() > selectedRelativeSnooze.intValue()) {
                    selectedRelativeSnooze = relativeSnooze;
                }
            } else if (null != snooze && (null == selectedSnooze || snooze.after(selectedSnooze))) {
                selectedSnooze = snooze;
            }
            /*
             * choose "nearest", not acknowledged trigger in case of multiple alarms
             */
            if (null == triggerDate || isRelatedSnooze(vAlarm, vAlarms)) {
                continue;
            } else if (null == selectedTrigger) {
                selectedTrigger = triggerDate;
                selectedAcknowledged = acknowledged;
                continue;
            } else if (null != acknowledged && false == acknowledged.before(triggerDate)) {
                continue;
            } else if (null == snooze && null == relativeSnooze && selectedTrigger.before(triggerDate) && null != selectedAcknowledged && false == selectedAcknowledged.before(selectedTrigger)) {
                selectedTrigger = triggerDate;
                selectedAcknowledged = acknowledged;
            }
        }
        /*
         * apply alarm and "acknowledged" state
         */
        applyAlarm(index, calendarObject, selectedTrigger, selectedAcknowledged, selectedSnooze, selectedRelativeSnooze, warnings);
    }

    /**
     * Applies parsed alarm properties for the given calendar object.
     *
     * @param index The current <code>VEVENT</code> or <code>VTODO</code> component index in the converted iCal file
     * @param calendarObject The appointment or task to apply the alarm for
     * @param trigger The calculated trigger date of the parsed alarm, or <code>null</code> if there is none
     * @param acknowledged The parsed acknowledged date, or <code>null</code> if there is none
     * @param snooze The parsed snooze date, or <code>null</code> if there is none
     * @param relativeSnooze The parsed realtive snooze time, or <code>null</code> if there is none
     * @param warnings A reference to a collection of conversion warnings
     */
    private void applyAlarm(int index, U calendarObject, Date trigger, Date acknowledged, Date snooze, Integer relativeSnooze, List<ConversionWarning> warnings) {
        if (Appointment.class.isAssignableFrom(calendarObject.getClass())) {
            applyAlarm((Appointment) calendarObject, trigger, acknowledged, snooze, relativeSnooze);
        } else if (Task.class.isAssignableFrom(calendarObject.getClass())) {
            applyAlarm((Task) calendarObject, trigger, acknowledged);
        } else {
            warnings.add(new ConversionWarning(index, "Can only parse alarms for appointments and tasks"));
        }
    }

    private void applyAlarm(Appointment appointment, Date trigger, Date acknowledged, Date snooze, Integer relativeSnooze) {
        if (null != trigger) {
            Long reminderMinutes = new Long((appointment.getStartDate().getTime() - trigger.getTime()) / (1000 * 60));
            appointment.setAlarm(reminderMinutes.intValue());
            appointment.setAlarmFlag(true);
        } else {
            appointment.setAlarm(-1);
        }
        if (null != acknowledged) {
            appointment.setProperty("com.openexchange.data.conversion.ical.alarm.acknowledged", acknowledged);
        }
        if (null != snooze) {
            appointment.setProperty("com.openexchange.data.conversion.ical.alarm.snooze", snooze);
        }
        if (null != relativeSnooze) {
            appointment.setProperty("com.openexchange.data.conversion.ical.alarm.relativeSnooze", relativeSnooze);
        }
    }

    private void applyAlarm(Task task, Date trigger, Date acknowledged) {
        if (null != trigger && (null == acknowledged || acknowledged.before(trigger))) {
            task.setAlarmFlag(true);
            task.setAlarm(trigger);
        }
    }

    /**
     * Parses a custom <code>X-MOZ-SNOOZE</code> property from the supplied calendar component as
     * absolute date-time.
     *
     * @param index The current <code>VEVENT</code> or <code>VTODO</code> component index in the converted iCal file
     * @param component The calendar component to inspect, typically a <code>VALARM</code>
     * @param warnings A reference to a collection of conversion warnings
     * @return The mozilla snooze time, or <code>null</code> if none was found
     */
    private Date parseMozillaSnooze(int index, CalendarComponent component, List<ConversionWarning> warnings) {
        Property property = component.getProperty("X-MOZ-SNOOZE");
        return null != property ? parseUTCDate(index, property.getValue(), warnings) : null;
    }

    /**
     * Parses a custom <code>X-MOZ-SNOOZE-TIME...</code> property from the supplied calendar component as
     * absolute date-time.
     *
     * @param index The current <code>VEVENT</code> or <code>VTODO</code> component index in the converted iCal file
     * @param component The calendar component to inspect, typically a <code>VALARM</code>
     * @param warnings A reference to a collection of conversion warnings
     * @return The mozilla snooze time, or <code>null</code> if none was found
     */
    private Date parseMozillaSnoozeTime(int index, CalendarComponent component, List<ConversionWarning> warnings) {
        PropertyList properties = component.getProperties();
        for (int i = 0; i < properties.size(); i++) {
            Property property = (Property) properties.get(i);
            if (null != property.getName() && property.getName().startsWith("X-MOZ-SNOOZE-TIME")) {
                return parseUTCDate(index, property.getValue(), warnings);
            }
        }
        return null;
    }

    /**
     * Parses the target snooze date from the supplied <code>VALARM</code> component as absolute date-time, based on a present
     * <code>RELATED-TO</code> parameter decorated with <code>RELTYPE=SNOOZE</code>, or by matching the <code>RELATED-TO</code> parameter's
     * value with the <code>UID</code> of another <code>VALARM</code> component.
     *
     * @param index The current <code>VEVENT</code> or <code>VTODO</code> component index in the converted iCal file
     * @param vAlarm The vAlarm to inspect
     * @param vAlarm A collection holding all alarms of the event
     * @param component The parent calendar component
     * @param timeZone The timezone to consider, or <code>null</code> if not defined
     * @param warnings A reference to a collection of conversion warnings
     * @return The snooze time, or <code>null</code> if none was indicated
     */
    private Trigger getSnoozeByRelated(int index, VAlarm vAlarm, List<VAlarm> allVAlarms, T component, TimeZone timeZone, List<ConversionWarning> warnings) {
        Property uidProperty = vAlarm.getProperty("UID");
        if (null == uidProperty || Strings.isEmpty(uidProperty.getValue())) {
            return null;
        }
        String uid = uidProperty.getValue();
        for (VAlarm otherVAlarm : allVAlarms) {
            if (false == otherVAlarm.equals(vAlarm)) {
                Property relatedToProperty = otherVAlarm.getProperty("RELATED-TO");
                if (null != relatedToProperty) {
                    String relatedUid = relatedToProperty.getValue();
                    Parameter relTypeParameter = relatedToProperty.getParameter("RELTYPE");
                    if (null != relTypeParameter && "SNOOZE".equals(relTypeParameter.getValue()) || Strings.isNotEmpty(relatedUid) && relatedUid.equals(uid)) {
                        return otherVAlarm.getTrigger();
//                        return parseTriggerDate(index, otherVAlarm.getTrigger(), component, timeZone, warnings);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Parses a specific trigger as absolute date.
     *
     * @param index The current <code>VEVENT</code> or <code>VTODO</code> component index in the converted iCal file
     * @param trigger The trigger property to parse
     * @param component The parent calendar component
     * @param timeZone The timezone to consider, or <code>null</code> if not defined
     * @param warnings A reference to a collection of conversion warnings
     * @return The absolute trigger date, or <code>null</code> if there is none
     */
    private Date parseTriggerDate(int index, Trigger trigger, T component, TimeZone timeZone, List<ConversionWarning> warnings) {
        if (null != trigger.getDateTime()) {
            return ParserTools.recalculateAsNeeded(trigger.getDateTime(), trigger, timeZone);
        }
        if (null != trigger.getDate()) {
            return ParserTools.recalculateAsNeeded(trigger.getDate(), trigger, timeZone);
        }
        Dur duration = trigger.getDuration();
        if (null != duration) {
            if (false == duration.isNegative() && false == new Dur("PT0S").equals(duration)) {
                warnings.add(new ConversionWarning(index, "Ignoring non-negative duration for alarm trigger"));
                return null;
            }
            Parameter relatedParameter = trigger.getParameter(Parameter.RELATED);
            Property relatedProperty;
            if (null != relatedParameter && "END".equals(relatedParameter.getValue())) {
                relatedProperty = component.getProperty(Property.DTEND);
                if (null == relatedProperty) {
                    relatedProperty = component.getProperty(Property.DUE);
                }
            } else {
                relatedProperty = component.getProperty(Property.DTSTART);
            }
            if (null == relatedProperty || false == DateProperty.class.isInstance(relatedProperty)) {
                warnings.add(new ConversionWarning(index, "Can't get related parameter for trigger"));
                return null;
            }
            DateProperty relatedDateProperty = (DateProperty) relatedProperty;
            Date relatedDate = ParserTools.recalculateAsNeeded(relatedDateProperty.getDate(), relatedDateProperty, timeZone);
            return duration.getTime(relatedDate);
        }
        return null;
    }

    /**
     * Gets a value indicating whether this <code>VALARM</code> component represents the alarm holding the target trigger time of another
     * snoozed <code>VALARM</code> component or not by checking the <code>RELATED-TO</code> properties.
     *
     * @param vAlarm The vAlarm to inspect
     * @param vAlarm A collection holding all alarms of the event
     * @return <code>true</code> if this is the 'snoozed' trigger time, <code>false</code>, otherwise
     */
    private boolean isRelatedSnooze(VAlarm vAlarm, List<VAlarm> allVAlarms) {
        Property relatedToProperty = vAlarm.getProperty("RELATED-TO");
        if (null != relatedToProperty) {
            Parameter relTypeParameter = relatedToProperty.getParameter("RELTYPE");
            if (null != relTypeParameter && "SNOOZE".equals(relTypeParameter.getValue())) {
                return true;
            }
            String uid = relatedToProperty.getValue();
            if (Strings.isNotEmpty(uid) && null != allVAlarms && 0 < allVAlarms.size()) {
                for (VAlarm otherVAlarm : allVAlarms) {
                    if (false == otherVAlarm.equals(vAlarm)) {
                        Property uidProperty = otherVAlarm.getProperty("UID");
                        if (null != uidProperty && uid.equals(uidProperty.getValue())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Extracts those <code>VALARM</code> components that contain a <code>DISPLAY</code> action from the supplied <code>VEVENT</code> or
     * <code>VTODO</code> component.
     *
     * @param index The current <code>VEVENT</code> or <code>VTODO</code> component index in the converted iCal file
     * @param component The <code>VEVENT</code> or <code>VTODO</code> to extract the alarms from
     * @param warnings A reference to a collection of conversion warnings
     * @return The alarms with action <code>DISPLAY</code>, or an emoty list if there are none
     */
    private List<VAlarm> getDisplayAlarms(int index, T component, List<ConversionWarning> warnings) {
        ComponentList alarms;
        if (VEvent.class.isAssignableFrom(component.getClass())) {
            alarms = ((VEvent) component).getAlarms();
        } else if (VToDo.class.isAssignableFrom(component.getClass())) {
            alarms = ((VToDo) component).getAlarms();
        } else {
            warnings.add(new ConversionWarning(index, "Can only extract alarms from VTODO or VEVENT components"));
            return Collections.emptyList();
        }
        List<VAlarm> vAlarms = new ArrayList<VAlarm>(alarms.size());
        for (int i = 0; i < alarms.size(); i++) {
            VAlarm alarm = (VAlarm) alarms.get(i);
            if (null != alarm) {
                Trigger trigger = alarm.getTrigger();
                if (null != trigger) {
                    Action action = alarm.getAction();
                    if (null != action && Action.DISPLAY.getValue().equalsIgnoreCase(action.getValue())) {
                        vAlarms.add(alarm);
                    } else {
                        warnings.add(new ConversionWarning(index, "Can only convert DISPLAY alarms with triggers"));                    
                    }
                }
            }
        }
        return vAlarms;
    }

    /**
     * Parses the acknowledged date property of a specific <code>VALARM</code> component.
     *
     * @param index The current <code>VEVENT</code> or <code>VTODO</code> component index in the converted iCal file
     * @param component The calendar component to inspect, typically a <code>VALARM</code>
     * @param triggerDate The extracted trigger date from the component
     * @param timeZone The timezone to consider, or <code>null</code> if not defined
     * @param warnings A reference to a collection of conversion warnings
     * @return The date the alarm has last been acknowledged, or <code>null</code> if there is no such property
     */
    private Date parseAcknowledgedDate(int index, CalendarComponent component, java.util.Date triggerDate, TimeZone timeZone, List<ConversionWarning> warnings) {
        if (null != triggerDate) {
            Property acknowledgedProperty = component.getProperty("ACKNOWLEDGED");
            if (null != acknowledgedProperty && Strings.isNotEmpty(acknowledgedProperty.getValue())) {
                try {
                    DateProperty dateProperty = new DtStart(acknowledgedProperty.getValue());
                    return  ParserTools.recalculateAsNeeded(dateProperty.getDate(), dateProperty, timeZone);
                } catch (ParseException e) {
                    warnings.add(new ConversionWarning(index, ConversionWarning.Code.PARSE_EXCEPTION, e, e.getMessage()));
                }
            }
        }
        return null;
    }

    /**
     * Parses the custom mozilla acknowledged date property (<code>X-MOZ-LASTACK</code>) of a specific <code>VALARM</code> component.
     *
     * @param index The current <code>VEVENT</code> or <code>VTODO</code> component index in the converted iCal file
     * @param component The calendar component to inspect, typically a <code>VALARM</code>
     * @param triggerDate The extracted trigger date from the component
     * @param timeZone The timezone to consider, or <code>null</code> if not defined
     * @param warnings A reference to a collection of conversion warnings
     * @return The date the alarm has last been acknowledged, or <code>null</code> if there is no such property
     */
    private Date parseMozillaAcknowledgedDate(int index, CalendarComponent component, Date triggerDate, TimeZone timeZone, List<ConversionWarning> warnings) {
        if (null != triggerDate) {
            Property acknowledgedProperty = component.getProperty("X-MOZ-LASTACK");
            if (null != acknowledgedProperty && Strings.isNotEmpty(acknowledgedProperty.getValue())) {
                try {
                    DateProperty dateProperty = new DtStart(acknowledgedProperty.getValue());
                    return  ParserTools.recalculateAsNeeded(dateProperty.getDate(), dateProperty, timeZone);
                } catch (ParseException e) {
                    warnings.add(new ConversionWarning(index, ConversionWarning.Code.PARSE_EXCEPTION, e, e.getMessage()));
                }
            }
        }
        return null;
    }

    private static Date parseUTCDate(int index, String value, List<ConversionWarning> warnings) {
        try {
            DateProperty dateProperty = new DtStart(value);
            return ParserTools.recalculateAsNeeded(dateProperty.getDate(), dateProperty, TimeZone.getTimeZone("UTC"));
        } catch (ParseException e) {
            warnings.add(new ConversionWarning(index, ConversionWarning.Code.PARSE_EXCEPTION, e, e.getMessage()));
        }
        return null;
    }

    /**
     * Converts a date into the textual iCal 'UTC' representation (with the <code>Z</code> prefix) appended to the time value.
     *
     * @param date The date to format
     * @return The formatted date string
     */
    private static String formatAsUTC(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

}
