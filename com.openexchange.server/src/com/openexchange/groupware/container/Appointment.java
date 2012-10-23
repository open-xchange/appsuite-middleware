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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.container;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.groupware.calendar.Constants;

/**
 * The appointment object.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class Appointment extends CalendarObject implements Cloneable {

    private static final long serialVersionUID = 8822932551489524746L;

    public static final int LOCATION = 400;

    public static final int FULL_TIME = 401;

    public static final int SHOWN_AS = 402;

    public static final int TIMEZONE = 408;

    public static final int RECURRENCE_START = 410;

    public static final int[] ALL_COLUMNS = {
        // From AppointmentObject itself
        LOCATION, FULL_TIME, SHOWN_AS,
        TIMEZONE,
        RECURRENCE_START,
        // From CalendarObject
        TITLE, START_DATE, END_DATE, NOTE, ALARM, RECURRENCE_ID, RECURRENCE_POSITION, RECURRENCE_DATE_POSITION, RECURRENCE_TYPE,
        CHANGE_EXCEPTIONS, DELETE_EXCEPTIONS, DAYS, DAY_IN_MONTH, MONTH, INTERVAL, UNTIL, NOTIFICATION, RECURRENCE_CALCULATOR,
        PARTICIPANTS, USERS, CONFIRMATIONS, RECURRENCE_COUNT, UID, ORGANIZER, SEQUENCE, ORGANIZER_ID, PRINCIPAL, PRINCIPAL_ID,
        // From CommonObject
        CATEGORIES, PRIVATE_FLAG,
        COLOR_LABEL, NUMBER_OF_LINKS, NUMBER_OF_ATTACHMENTS,
        // From FolderChildObject
        FOLDER_ID,
        // From DataObject
        OBJECT_ID, CREATED_BY, MODIFIED_BY, CREATION_DATE, LAST_MODIFIED, LAST_MODIFIED_UTC };

    public static final int RESERVED = 1;

    public static final int TEMPORARY = 2;

    public static final int ABSENT = 3;

    public static final int FREE = 4;

    protected int DEFAULTFOLDER = -1;

    protected String location;

    protected boolean fulltime;

    protected int shown_as;

    protected int alarm;

    protected long recurring_start;

    protected boolean ignoreConflicts;

    protected String timezone;

    protected boolean ignoreOutdatedSequence;

    protected boolean b_location;

    protected boolean b_fulltime;

    protected boolean b_shown_as;

    protected boolean b_Alarm;

    protected boolean b_timezone;

    protected boolean b_recurring_start;

    /**
     * Initializes a new {@link Appointment}
     */
    public Appointment() {
        super();
    }

    // GET METHODS
    public String getLocation() {
        return location;
    }

    public boolean getFullTime() {
        return fulltime;
    }

    public int getShownAs() {
        return shown_as;
    }

    public int getAlarm() {
        return alarm;
    }

    public boolean getIgnoreConflicts() {
        return ignoreConflicts;
    }

    public final long getRecurringStart() {
        return recurring_start;
    }

    /**
     * Returns the time zone essential for recurring appointments.
     *
     * @return the time zone if it has been set otherwise <code>null</code>.
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Returns the time zone essential for recurring appointments.
     *
     * @return the time zone if it has been set otherwise UTC.
     * @deprecated use {@link #getTimezone()} and handle fallback to UTC yourself.
     */
    @Deprecated
    public String getTimezoneFallbackUTC() {
        if (timezone != null) {
            return timezone;
        }
        return "UTC";
    }

    /**
     * TODO Recalculation to UTC start of day must be done outside this data object.
     */
    public final void setRecurringStart(final long recurring_start) {
        this.recurring_start = recurring_start - (recurring_start % Constants.MILLI_DAY);
        b_recurring_start = true;
    }

    // SET METHODS
    public void setLocation(final String location) {
        this.location = location;
        b_location = true;
    }

    public void setFullTime(final boolean fulltime) {
        this.fulltime = fulltime;
        b_fulltime = true;
    }

    public void setShownAs(final int shown_as) {
        this.shown_as = shown_as;
        b_shown_as = true;
    }

    public void setAlarm(final int alarm) {
        this.alarm = alarm;
        b_Alarm = true;
    }

    public void setIgnoreConflicts(final boolean ignoreConflicts) {
        this.ignoreConflicts = ignoreConflicts;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
        b_timezone = true;
    }

    // REMOVE METHODS
    public void removeLocation() {
        location = null;
        b_location = false;
    }

    public void removeFullTime() {
        fulltime = false;
        b_fulltime = false;
    }

    public void removeShownAs() {
        shown_as = 0;
        b_shown_as = false;
    }

    public void removeAlarm() {
        alarm = 0;
        b_Alarm = false;
    }

    public void removeTimezone() {
        timezone = null;
        b_timezone = false;
    }

    public void removeRecurringStart() {
        recurring_start = 0;
        b_recurring_start = false;
    }

    // CONTAINS METHODS

    public boolean containsRecurringStart() {
        return b_recurring_start;
    }

    public boolean containsLocation() {
        return b_location;
    }

    public boolean containsFullTime() {
        return b_fulltime;
    }

    public boolean containsShownAs() {
        return b_shown_as;
    }

    public boolean containsAlarm() {
        return b_Alarm;
    }

    public boolean containsTimezone() {
        return b_timezone;
    }

    public boolean isIgnoreOutdatedSequence() {
        return ignoreOutdatedSequence;
    }

    public void setIgnoreOutdatedSequence(boolean ignoreOutdatedSequence) {
        this.ignoreOutdatedSequence = ignoreOutdatedSequence;
    }

    @Override
    public void reset() {
        super.reset();

        location = null;
        fulltime = false;
        shown_as = 0;
        alarm = 0;
        timezone = null;

        b_location = false;
        b_fulltime = false;
        b_shown_as = false;
        b_Alarm = false;
        b_timezone = false;
    }

    @Override
    public int hashCode() {
        return objectId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Appointment) {
            if (((Appointment) o).hashCode() == hashCode()) {
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public Appointment clone() {
        Appointment appointmentobject = (Appointment) super.clone();

        for (int field : ALL_COLUMNS) {
            if (contains(field)) {
                appointmentobject.set(field, get(field));
            }
        }

        if (containsLabel()) {
            appointmentobject.setLabel(getLabel());
        }
        if (containsFullTime()) {
            appointmentobject.setFullTime(getFullTime());
        }
        if (containsLocation()) {
            appointmentobject.setLocation(getLocation());
        }
        if (containsShownAs()) {
            appointmentobject.setShownAs(getShownAs());
        }
        if (containsOccurrence()) {
            appointmentobject.setOccurrence(getOccurrence());
        }
        if (containsTimezone()) {
            appointmentobject.setTimezone(getTimezoneFallbackUTC());
        }
        if (containsUid()) {
            appointmentobject.setUid(getUid());
        }
        if (containsOrganizer()) {
            appointmentobject.setOrganizer(getOrganizer());
        }
        if (containsPrincipal()) {
            appointmentobject.setPrincipal(getPrincipal());
        }
        if (containsOrganizerId()) {
            appointmentobject.setOrganizerId(getOrganizerId());
        }
        if (containsPrincipalId()) {
            appointmentobject.setPrincipalId(getPrincipalId());
        }
        return appointmentobject;
    }

    @Override
    public Set<Integer> findDifferingFields(DataObject dataObject) {
        Set<Integer> differingFields = super.findDifferingFields(dataObject);

        if (!getClass().isAssignableFrom(dataObject.getClass())) {
            return differingFields;
        }

        Appointment other = (Appointment) dataObject;

        if ((!containsAlarm() && other.containsAlarm()) || (containsAlarm() && other.containsAlarm() && getAlarm() != other.getAlarm())) {
            differingFields.add(ALARM);
        }

        if ((!containsFullTime() && other.containsFullTime()) || (containsFullTime() && other.containsFullTime() && getFullTime() != other.getFullTime())) {
            differingFields.add(FULL_TIME);
        }

        if ((!containsLocation() && other.containsLocation()) || (containsLocation() && other.containsLocation() && getLocation() != other.getLocation() && (getLocation() == null || !getLocation().equals(
            other.getLocation())))) {
            differingFields.add(LOCATION);
        }

        if ((!containsRecurringStart() && other.containsRecurringStart()) || (containsRecurringStart() && other.containsRecurringStart() && getRecurringStart() != other.getRecurringStart())) {
            differingFields.add(RECURRENCE_START);
        }

        if ((!containsShownAs() && other.containsShownAs()) || (containsShownAs() && other.containsShownAs() && getShownAs() != other.getShownAs())) {
            differingFields.add(SHOWN_AS);
        }

        if ((!containsTimezone() && other.containsTimezone()) || (containsTimezone() && other.containsTimezone() && getTimezone() != other.getTimezone() && (getTimezone() == null || !getTimezone().equals(
            other.getTimezone())))) {
            differingFields.add(TIMEZONE);
        }
        return differingFields;
    }

    @Override
    public void set(int field, Object value) {
        switch (field) {
        case SHOWN_AS:
            setShownAs((Integer) value);
            break;
        case ALARM:
            setAlarm((Integer) value);
            break;
        case FULL_TIME:
            setFullTime((Boolean) value);
            break;
        case TIMEZONE:
            setTimezone((String) value);
            break;
        case RECURRENCE_START:
            setRecurringStart((Long) value);
            break;
        case LOCATION:
            setLocation((String) value);
            break;
        default:
            super.set(field, value);
        }
    }

    @Override
    public Object get(int field) {
        switch (field) {
        case SHOWN_AS:
            return getShownAs();
        case ALARM:
            return getAlarm();
        case FULL_TIME:
            return getFullTime();
        case TIMEZONE:
            return getTimezone();
        case RECURRENCE_START:
            return getRecurringStart();
        case LOCATION:
            return getLocation();
        default:
            return super.get(field);
        }
    }

    @Override
    public boolean contains(int field) {
        switch (field) {
        case SHOWN_AS:
            return containsShownAs();
        case ALARM:
            return containsAlarm();
        case FULL_TIME:
            return containsFullTime();
        case TIMEZONE:
            return containsTimezone();
        case RECURRENCE_START:
            return containsRecurringStart();
        case LOCATION:
            return containsLocation();
        default:
            return super.contains(field);
        }
    }

    @Override
    public void remove(int field) {
        switch (field) {
        case SHOWN_AS:
            removeShownAs();
            break;
        case ALARM:
            removeAlarm();
            break;
        case FULL_TIME:
            removeFullTime();
            break;
        case TIMEZONE:
            removeTimezone();
            break;
        case RECURRENCE_START:
            removeRecurringStart();
            break;
        case LOCATION:
            removeLocation();
            break;
        default:
            super.remove(field);
        }
    }

    public static Set<Differ<? super Appointment>> differ = new HashSet<Differ<? super Appointment>>();
    
    static {
        differ.addAll(CalendarObject.differ);
    }

}
