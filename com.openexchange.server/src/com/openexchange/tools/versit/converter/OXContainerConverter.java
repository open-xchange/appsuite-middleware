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

package com.openexchange.tools.versit.converter;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.versit.Parameter;
import com.openexchange.tools.versit.ParameterValue;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.values.DateTimeValue;
import com.openexchange.tools.versit.values.DurationValue;
import com.openexchange.tools.versit.values.RecurrenceValue;
import com.openexchange.tools.versit.values.RecurrenceValue.Weekday;

/**
 * This class transforms VersitObjects to OX Contacts, Appointments and Tasks and back. If you want to translate more fields used in ICAL or
 * VCard, you're at the right place - but don't forget to do it in both directions.
 * <p>
 * <a href="http://tools.ietf.org/html/rfc2426">vCard MIME Directory Profile</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> (adapted Viktor's parser for OX6)
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> (bugfixing and refactoring)
 */
public class OXContainerConverter {

    private static final String P_ORGANIZER = "ORGANIZER";

    private static final String P_DESCRIPTION = "DESCRIPTION";

    private static final String P_RRULE = "RRULE";

    private static final String P_CATEGORIES = "CATEGORIES";

    private static final String P_ATTENDEE = "ATTENDEE";

    private static final String P_DTSTART = "DTSTART";

    private static final String P_SUMMARY = "SUMMARY";

    private static final String P_COMPLETED = "COMPLETED";

    private static final String P_CLASS = "CLASS";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXContainerConverter.class);

    private static final String atdomain;

    static {
        String domain = "localhost";
        try {
            domain = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            LOG.error("", e);
        }
        atdomain = new StringBuilder().append('@').append(domain).toString();
    }

    private final Context ctx;
    private final TimeZone timezone;
    private final String organizerMailAddress;
    private boolean sendUTC = true;
    private boolean sendFloating;
    private boolean addDisplayName4DList;
    private boolean skipOxCTypeAttribute;

    public OXContainerConverter(final TimeZone timezone, final String organizerMailAddress) {
        super();
        this.timezone = timezone;
        this.organizerMailAddress = organizerMailAddress;
        ctx = null;
    }

    public OXContainerConverter(final Session session) throws ConverterException, OXException {
        super();
        if (session instanceof ServerSession) {
            ctx = ((ServerSession) session).getContext();
        } else {
            try {
                ctx = ContextStorage.getStorageContext(session.getContextId());
            } catch (final OXException e) {
                throw new ConverterException(e);
            }
        }
        timezone = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone());
        this.organizerMailAddress = null;
    }

    public OXContainerConverter(final Session session, final Context ctx) throws OXException {
        super();
        this.ctx = ctx;
        timezone = TimeZoneUtils.getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone());
        this.organizerMailAddress = null;
    }

    public OXContainerConverter(final Context ctx, final TimeZone tz) {
        super();
        this.ctx = ctx;
        timezone = tz;
        this.organizerMailAddress = null;
    }

    public void close() {
        LOG.trace("OXContainerConverter.close()");
    }

    public boolean isSendFloating() {
        return sendFloating;
    }

    public void setSendFloating(final boolean sendFloating) {
        this.sendFloating = sendFloating;
    }

    /**
     * Gets the addDisplayName4DList
     *
     * @return The addDisplayName4DList
     */
    public boolean isAddDisplayName4DList() {
        return addDisplayName4DList;
    }

    /**
     * Sets the addDisplayName4DList
     *
     * @param addDisplayName4DList The addDisplayName4DList to set
     */
    public void setAddDisplayName4DList(final boolean addDisplayName4DList) {
        this.addDisplayName4DList = addDisplayName4DList;
    }

    /**
     * Gets a value whether the <code>X-OPEN-XCHANGE-CTYPE</code> attribute is set in vCards to distinguish between distribution lists
     * and contacts.
     *
     * @return <code>true</code> if the <code>X-OPEN-XCHANGE-CTYPE</code> attribute is skipped, <code>false</code>, otherwise
     */
    public boolean isSkipOxCTypeAttribute() {
        return skipOxCTypeAttribute;
    }

    /**
     * Configures if the <code>X-OPEN-XCHANGE-CTYPE</code> attribute will be set in vCards to distinguish between distribution lists
     * and contacts.
     *
     * @param <code>true</code> to skip the <code>X-OPEN-XCHANGE-CTYPE</code> attribute, <code>false</code>, otherwise
     */
    public void setSkipOxCTypeAttribute(final boolean skipOxCTypeAttribute) {
        this.skipOxCTypeAttribute = skipOxCTypeAttribute;
    }

    public boolean isSendUTC() {
        return sendUTC;
    }

    public void setSendUTC(final boolean sendUTC) {
        this.sendUTC = sendUTC;
    }

    public Task convertTask(final VersitObject object) throws ConverterException {
        if (null == object) {
            return null;
        }
        try {
            final Task taskContainer = new Task();
            // CLASS
            PrivacyProperty(taskContainer, object, P_CLASS, CommonObject.PRIVATE_FLAG);
            // COMPLETED
            DateTimeProperty(taskContainer, object, P_COMPLETED, Task.DATE_COMPLETED);
            // GEO is ignored
            // LAST-MODIFIED is ignored
            // LOCATION is ignored
            // ORGANIZER is ignored
            // PERCENT-COMPLETE
            IntegerProperty(taskContainer, object, "PERCENT-COMPLETE", Task.PERCENT_COMPLETED);
            // PRIORITY
            Property property = object.getProperty("PRIORITY");
            if (property != null) {
                final int priority = ((Integer) property.getValue()).intValue();
                final int[] priorities = { Task.HIGH, Task.HIGH, Task.HIGH, Task.HIGH, Task.NORMAL, Task.LOW, Task.LOW, Task.LOW, Task.LOW };
                if (priority >= 1 && priority <= 9) {
                    taskContainer.setPriority(priorities[priority - 1]);
                } else if (priority != 0) {
                    throw new ConverterException("Invalid priority");
                }
            }
            // TODO RECURRENCE-ID
            // TODO SEQUENCE
            // STATUS
            property = object.getProperty("STATUS");
            if (property != null) {
                final String status = ((String) property.getValue()).toUpperCase();
                if ("NEEDS-ACTION".equals(status)) {
                    taskContainer.setStatus(Task.NOT_STARTED);
                } else if ("IN-PROCESS".equals(status)) {
                    taskContainer.setStatus(Task.IN_PROGRESS);
                } else if (P_COMPLETED.equals(status)) {
                    taskContainer.setStatus(Task.DONE);
                } else if ("CANCELLED".equals(status)) {
                    taskContainer.setStatus(Task.DEFERRED);
                } else {
                    throw new ConverterException("Unknown status: \"" + status + "\"");
                }
            }
            // SUMMARY
            StringProperty(taskContainer, object, P_SUMMARY, CalendarObject.TITLE);
            // TODO UID
            // property = object.getProperty("UID");
            // if (property != null) {
            // String uid = property.getValue().toString();
            // if (uid.endsWith(atdomain))
            // task.setObjectID(Integer.parseInt(uid.substring(0, uid.length()
            // - atdomain.length())));
            // }
            // URL is ignored
            // DUE and DURATION
            if (!DateTimeProperty(taskContainer, object, "DUE", CalendarObject.END_DATE)) {
                DurationProperty(taskContainer, object, "DURATION", P_DTSTART, CalendarObject.END_DATE);
            }
            // Multiple properties
            final int count = object.getPropertyCount();
            final StringBuilder cats = new StringBuilder();
            for (int i = 0; i < count; i++) {
                property = object.getProperty(i);
                // ATTACH is ignored
                // ATTENDEE
                if (P_ATTENDEE.equals(property.name)) {
                    AttendeeProperty(taskContainer, property);
                }
                // CATEGORIES
                else if (P_CATEGORIES.equals(property.name)) {
                    final ArrayList<?> al = ((ArrayList<?>) property.getValue());
                    final int size = al.size();
                    final Iterator<?> j = al.iterator();
                    for (int k = 0; k < size; k++) {
                        cats.append(j.next());
                        cats.append(',');
                    }
                }
                // COMMENT is ignored
                // CONTACT is ignored
                // EXDATE is ignored
                // EXRULE is ignored
                // REQUEST-STATUS is ignored
                // TODO RELATED-TO
                // RESOURCES is ignored
                // RDATE is ignored
                // RRULE
                else if (P_RRULE.equals(property.name)) {
                    RecurrenceProperty(taskContainer, property, object.getProperty(P_DTSTART));
                }
            }
            if (cats.length() != 0) {
                cats.deleteCharAt(cats.length() - 1);
                taskContainer.setCategories(cats.toString());
            }
            // DESCRIPTION (fix: 7718)
            StringProperty(taskContainer, object, P_DESCRIPTION, CalendarObject.NOTE);
            // VALARM
            AddAlarms(taskContainer, object);
            return taskContainer;
        } catch (final Exception e) {
            LOG.error(e.toString());
            throw new ConverterException(e);
        }
    }

    public CalendarDataObject convertAppointment(final VersitObject object) throws ConverterException {
        if (null == object) {
            return null;
        }
        final CalendarDataObject appContainer = new CalendarDataObject();
        // CLASS
        PrivacyProperty(appContainer, object, P_CLASS, CommonObject.PRIVATE_FLAG);
        // CREATED is ignored
        // DESCRIPTION
        StringProperty(appContainer, object, P_DESCRIPTION, CalendarObject.NOTE);
        // DTSTART
        Property property = object.getProperty(P_DTSTART);
        if (property != null) {
            final DateTimeValue date = (DateTimeValue) property.getValue();
            if (date.isFloating) {
                date.calendar.setTimeZone(timezone);
            }
            date.calendar.set(Calendar.SECOND, 0);
            date.calendar.set(Calendar.MILLISECOND, 0);
            appContainer.setStartDate(date.calendar.getTime());
            appContainer.setFullTime(!date.hasTime);
        }
        // GEO is ignored
        // LAST-MODIFIED is ignored
        // LOCATION
        StringProperty(appContainer, object, "LOCATION", Appointment.LOCATION);
        // ORGANIZER is ignored
        // PRIORITY is ignored
        // DTSTAMP is ignored
        // TODO SEQUENCE
        // STATUS is ignored
        // SUMMARY
        StringProperty(appContainer, object, P_SUMMARY, CalendarObject.TITLE);
        // TRANSP
        property = object.getProperty("TRANSP");
        if (property != null) {
            final String transp = ((String) property.getValue()).toUpperCase();
            if ("OPAQUE".equals(transp)) {
                appContainer.setShownAs(Appointment.RESERVED);
            } else if ("TRANSPARENT".equals(transp)) {
                appContainer.setShownAs(Appointment.FREE);
            } else {
                throw new ConverterException("Invalid transparency");
            }
        }
        // TODO UID
        // property = object.getProperty("UID");
        // if (property != null) {
        // String uid = property.getValue().toString();
        // if (uid.endsWith(atdomain))
        // app.setObjectID(Integer.parseInt(uid.substring(0, uid.length()
        // - atdomain.length())));
        // }
        // URL is ignored
        // TODO RECURRENCE-ID
        // DTEND and DURATION
        if (!DateTimeProperty(appContainer, object, "DTEND", CalendarObject.END_DATE) && !DurationProperty(
            appContainer,
            object,
            "DURATION",
            P_DTSTART,
            CalendarObject.END_DATE)) {
            DateTimeProperty(appContainer, object, "DSTART", CalendarObject.END_DATE);
        }
        // Multiple properties
        final StringBuilder cats = new StringBuilder();
        final ArrayList exdates = new ArrayList<Object>();
        final int count = object.getPropertyCount();
        for (int i = 0; i < count; i++) {
            property = object.getProperty(i);
            // ATTACH is ignored
            // ATTENDEE
            if (P_ATTENDEE.equals(property.name)) {
                AttendeeProperty(appContainer, property);
            }
            // CATEGORIES
            else if (P_CATEGORIES.equals(property.name)) {
                final ArrayList<?> al = ((ArrayList<?>) property.getValue());
                final int size = al.size();
                final Iterator<?> j = al.iterator();
                for (int k = 0; k < size; k++) {
                    cats.append(j.next());
                    cats.append(',');
                }
            }
            // COMMENT is ignored
            // CONTACT is ignored
            // EXDATE
            else if ("EXDATE".equals(property.name)) {
                exdates.addAll((ArrayList) property.getValue());
            }
            // EXRULE is ignored
            // REQUEST-STATUS is ignored
            // TODO RELATED-TO
            // RESOURCES
            else if ("RESOURCES".equals(property.name)) {
                final ArrayList<?> al = ((ArrayList<?>) property.getValue());
                final int size = al.size();
                final Iterator<?> j = al.iterator();
                for (int k = 0; k < size; k++) {
                    final ResourceParticipant p = new ResourceParticipant();
                    p.setDisplayName((String) j.next());
                    appContainer.addParticipant(p);
                }
            }
            // RDATE is ignored
            // RRULE
            else if (P_RRULE.equals(property.name)) {
                RecurrenceProperty(appContainer, property, object.getProperty(P_DTSTART));
            }
        }
        if (cats.length() != 0) {
            cats.deleteCharAt(cats.length() - 1);
            appContainer.setCategories(cats.toString());
        }
        if (!exdates.isEmpty()) {
            final Date[] dates = new Date[exdates.size()];
            for (int i = 0; i < dates.length; i++) {
                dates[i] = ((DateTimeValue) exdates.get(i)).calendar.getTime();
            }
            appContainer.setDeleteExceptions(dates);
        }
        // VALARM
        AddAlarms(appContainer, object);
        return appContainer;
    }

    private static boolean IntegerProperty(final CommonObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            if (property.getValue() instanceof Integer) {
                final Integer val = (Integer) property.getValue();
                containerObj.set(fieldNumber, val);
                return true;
            }
            return false;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean StringProperty(final CommonObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            containerObj.set(fieldNumber, property.getValue().toString());
            return true;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean PrivacyProperty(final CalendarObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            final String privacy = (String) property.getValue();

            boolean isPrivate = false;
            if ("PRIVATE".equals(privacy)) {
                isPrivate = true;
            }
            if ("CONFIDENTIAL".equals(privacy)) {
                throw new ConverterPrivacyException();
            }
            containerObj.set(fieldNumber, Boolean.valueOf(isPrivate));
            return false;
        } catch (final ConverterPrivacyException e) {
            throw e;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private boolean DateTimeProperty(final CommonObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            final DateTimeValue date = (DateTimeValue) property.getValue();
            if (date.isFloating) {
                date.calendar.setTimeZone(timezone);
            }
            date.calendar.set(Calendar.SECOND, 0);
            date.calendar.set(Calendar.MILLISECOND, 0);
            containerObj.set(fieldNumber, date.calendar.getTime());
            return true;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean DurationProperty(final CommonObject containerObj, final VersitObject object, final String DurationName, final String StartName, final int fieldNumber) throws ConverterException {
        try {
            Property property = object.getProperty(DurationName);
            if (property == null) {
                return false;
            }
            final DurationValue dur = (DurationValue) property.getValue();
            property = object.getProperty(StartName);
            if (property == null) {
                throw new ConverterException("Duration without start is not supported.");
            }
            final Calendar cal = (Calendar) ((DateTimeValue) property.getValue()).calendar.clone();
            cal.add(Calendar.WEEK_OF_YEAR, dur.Negative ? -dur.Weeks : dur.Weeks);
            cal.add(Calendar.DATE, dur.Negative ? -dur.Days : dur.Days);
            cal.add(Calendar.HOUR, dur.Negative ? -dur.Hours : dur.Hours);
            cal.add(Calendar.MINUTE, dur.Negative ? -dur.Minutes : dur.Minutes);
            cal.add(Calendar.SECOND, dur.Negative ? -dur.Seconds : dur.Seconds);
            containerObj.set(fieldNumber, cal.getTime());
            return true;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private void AttendeeProperty(final CalendarObject calContainerObj, final Property property) throws ConverterException {
        try {
            final String mail = ((URI) property.getValue()).getSchemeSpecificPart();
            final Participant participant;
            if (isInternalUser(mail)) {
                // fix for bug 8475
                participant = new UserParticipant(getInternalUser(mail).getId());
                // end:fix
            } else {
                participant = new ExternalUserParticipant(mail);
                participant.setDisplayName(mail);
            }
            calContainerObj.addParticipant(participant);
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    /**
     * Finds out whether a user is internal, since internal users get treated differently when entering appointments or tasks.
     *
     * @param mail - Mail address as string
     * @return true if is internal user, false otherwise
     */
    public boolean isInternalUser(final String mail) {
        try {
            final User uo = UserStorage.getInstance().searchUser(mail, ctx);
            return uo != null;
        } catch (final OXException e) {
            return false;
        }
    }

    /**
     * Finds an internal user by its e-mail address. Note that an e-mail address is unique, but the identifier for an internal user is its
     * id. Should only be called after using <code>isInternalUser</code> or you have to live with the OXException.
     */
    public User getInternalUser(final String mail) throws OXException {
        return UserStorage.getInstance().searchUser(mail, ctx);
    }

    private static void RecurrenceProperty(final CalendarObject calContainerObj, final Property property, final Property start) throws ConverterException {
        final RecurrenceValue recur = (RecurrenceValue) property.getValue();
        if (start == null) {
            throw new ConverterException("RRULE without DTSTART");
        }
        final Calendar cal = ((DateTimeValue) start.getValue()).calendar;
        final int[] recurTypes = { CalendarObject.NONE, CalendarObject.NONE, CalendarObject.NONE, CalendarObject.DAILY, CalendarObject.WEEKLY, CalendarObject.MONTHLY, CalendarObject.YEARLY };
        calContainerObj.setRecurrenceType(recurTypes[recur.Freq]);
        if (recur.Until != null) {
            calContainerObj.setUntil(recur.Until.calendar.getTime());
        }
        if (recur.Count != -1) {
            calContainerObj.setOccurrence(recur.Count);
            // throw new ConverterException("COUNT is not supported.");
        }
        calContainerObj.setInterval(recur.Interval);
        switch (recur.Freq) {
            case RecurrenceValue.YEARLY:
                int month;
                if (recur.ByMonth.length > 0) {
                    if (recur.ByMonth.length > 1) {
                        throw new ConverterException("Multiple months of the year are not supported.");
                    }
                    month = recur.ByMonth[0] - 1 + Calendar.JANUARY;
                } else {
                    month = cal.get(Calendar.MONTH);
                }
                calContainerObj.setMonth(month);
                //$FALL-THROUGH$
            case RecurrenceValue.MONTHLY:
                if (recur.ByMonthDay.length > 0) {
                    if (recur.ByDay.size() != 0) {
                        throw new ConverterException("Simultaneous day in month and weekday in month are not supported.");
                    }
                    if (recur.ByMonthDay.length > 1) {
                        throw new ConverterException("Multiple days of the month are not supported.");
                    }
                    final int dayOfMonth = recur.ByMonthDay[0];
                    if (dayOfMonth <= 0) {
                        throw new ConverterException("Counting days from end of the month is not supported.");
                    }
                    calContainerObj.setDayInMonth(dayOfMonth);
                } else if (recur.ByDay.size() > 0) {
                    int days = 0, week = 0;
                    final int size = recur.ByDay.size();
                    final Iterator<?> j = recur.ByDay.iterator();
                    for (int k = 0; k < size; k++) {
                        final RecurrenceValue.Weekday wd = (RecurrenceValue.Weekday) j.next();
                        days |= 1 << (wd.day - Calendar.SUNDAY);
                        if (week != 0 && week != wd.week) {
                            throw new ConverterException("Multiple weeks of month are not supported.");
                        }
                        week = wd.week;
                        if (week < 0) {
                            if (week == -1) {
                                week = 5;
                            } else {
                                throw new ConverterException(
                                    "Only the last week of a month is supported. Counting from the end of the month above the first is not supported.");
                            }
                        }
                    }
                    calContainerObj.setDays(days);
                    calContainerObj.setDayInMonth(week);
                } else {
                    calContainerObj.setDayInMonth(cal.get(Calendar.DAY_OF_MONTH));
                }
                break;
            case RecurrenceValue.WEEKLY:
            case RecurrenceValue.DAILY: // fix: 7703
                int days = 0;
                final int size = recur.ByDay.size();
                final Iterator<?> j = recur.ByDay.iterator();
                for (int k = 0; k < size; k++) {
                    days |= 1 << ((RecurrenceValue.Weekday) j.next()).day - Calendar.SUNDAY;
                }
                if (days == 0) {
                    days = 1 << cal.get(Calendar.DAY_OF_WEEK);
                }
                calContainerObj.setDays(days);
                break;
            default:
                throw new ConverterException("Unknown Recurrence Property: " + recur.Freq);
        }
    }

    private static void AddAlarms(final CalendarObject calContainerObj, final VersitObject object) throws ConverterException {
        final int count = object.getChildCount();
        for (int i = 0; i < count; i++) {
            final VersitObject alarm = object.getChild(i);
            Property property = alarm.getProperty("ACTION");
            // if (property != null &&
            // property.getValue().toString().equalsIgnoreCase("EMAIL")) {
            if (property != null && property.getValue().toString().equalsIgnoreCase("DISPLAY")) { // bugfix
                // :
                // 7473
                property = alarm.getProperty("TRIGGER");
                if (property != null) {
                    int time;
                    if (property.getValue() instanceof DurationValue) {
                        final DurationValue trigger = (DurationValue) property.getValue();
                        if (trigger.Months != 0 || trigger.Years != 0) {
                            throw new ConverterException("Irregular durations not supported");
                        }
                        time = trigger.Minutes + (trigger.Hours + (trigger.Days + 7 * trigger.Weeks) * 24) * 60;
                        if (trigger.Negative) { // note: This does not make
                            // sense currently, because
                            // "NEGATIVE" is never set
                            time = -time;
                        }
                        /*
                         * fix for 7473: TRIGGERs in ICAL are always negative (because they are _before_ the event), alarms in OX are always
                         * positive (because there is no reason for them to be _after_ the event).
                         */
                        time = -time;
                        // fix:end
                    } else {
                        final DateTimeValue trigger = (DateTimeValue) property.getValue();
                        property = object.getProperty(P_DTSTART);
                        if (property == null) {
                            throw new ConverterException("VALARM without DTSTART not supported");
                        }
                        time = (int) (((DateTimeValue) property.getValue()).calendar.getTimeInMillis() - trigger.calendar.getTimeInMillis());
                    }
                    if (calContainerObj instanceof Appointment) {
                        final Appointment appObj = (Appointment) calContainerObj;
                        appObj.setAlarm(time);
                        appObj.setAlarmFlag(true); // bugfix: 7473
                    } else if (calContainerObj instanceof Task) {
                        final Task taskObj = (Task) calContainerObj;
                        taskObj.setAlarm(new Date(taskObj.getStartDate().getTime() - (time * 60 * 1000)));
                        taskObj.setAlarmFlag(true); // bugfix: 7473
                    }
                }
            }
        }
    }

    public VersitObject convertTask(final Task task) throws ConverterException {
        if (null == task) {
            return null;
        }
        final VersitObject object = new VersitObject("VTODO");
        // TODO CLASS
        addProperty(object, P_CLASS, "PUBLIC");
        // COMPLETED
        addDateTime(object, P_COMPLETED, task.getDateCompleted());
        // CREATED
        addDateTime(object, "CREATED", task.getCreationDate());
        // DESCRIPTION
        addProperty(object, P_DESCRIPTION, task.getNote());
        // DTSTAMP
        addDateTime(object, "DTSTAMP", new Date());
        // DTSTART
        addWeirdTaskDate(object, P_DTSTART, task.getStartDate());
        // GEO is ignored
        // LAST-MODIFIED
        addDateTime(object, "LAST-MODIFIED", task.getLastModified());
        // LOCATION is ignored
        // ORGANIZER
        if (organizerMailAddress != null) {
            addAddress(object, P_ORGANIZER, organizerMailAddress);
        } else {
            addAddress(object, P_ORGANIZER, task.getCreatedBy());
        }
        // PERCENT-COMPLETE
        addProperty(object, "PERCENT-COMPLETE", Integer.valueOf(task.getPercentComplete()));
        // PRIORITY
        final int[] priorities = { 9, 5, 1 };
        final int priority = task.getPriority();
        /*
         * TODO REMOVED DUE REMOVAL OF com.openexchange.groupware.links if (priority >= OXTask.LOW && priority <= OXTask.HIGH)
         * addProperty(object, "PRIORITY", new Integer(priorities[priority - OXTask.LOW])); else throw new
         * ConverterException("Invalid priority");
         */
        // TODO RECURRENCE-ID
        // TODO SEQUENCE
        // STATUS
        final String[] statuses = { "NEEDS-ACTION", "IN-PROCESS", P_COMPLETED, "NEEDS-ACTION", "CANCELLED" };
        final int status = task.getStatus();
        /*
         * TODO REMOVED DUE REMOVAL OF com.openexchange.groupware.tasks if (status >= OXTask.NOT_STARTED && status <= OXTask.DEFERRED)
         * addProperty(object, "STATUS", statuses[status - OXTask.NOT_STARTED]); else throw new ConverterException("Invlaid status");
         */
        // SUMMARY
        addProperty(object, P_SUMMARY, task.getTitle());
        // UID
        addProperty(object, "UID", task.getObjectID() + atdomain);
        // URL is ignored
        // DUE and DURATION
        addWeirdTaskDate(object, "DUE", task.getEndDate());
        // ATTACH
        // TODO addAttachments(object, task, OXAttachment.TASK);
        // ATTENDEE
        if (task.containsParticipants()) {
            final int length = task.getParticipants().length;
            final Iterator<?> i = new ArrayIterator(task.getParticipants());
            for (int k = 0; k < length; k++) {
                final Participant p = (Participant) i.next();
                if (p.getType() == Participant.USER) {
                    addAddress(object, P_ATTENDEE, p.getEmailAddress());
                }
            }
        }
        // CATEGORIES
        final ArrayList<String> categories = new ArrayList<String>();
        if (task.getCategories() != null) {
            final StringTokenizer tokenizer = new StringTokenizer(task.getCategories(), ",");
            while (tokenizer.hasMoreTokens()) {
                categories.add(tokenizer.nextToken());
            }
        }
        addProperty(object, P_CATEGORIES, categories);
        // COMMENT is ignored
        // CONTACT is ignored
        // EXDATE is ignored
        // EXRULE is ignored
        // REQUEST-STATUS is ignored
        // TODO RELATED-TO
        // RESOURCES is ignored
        // RDATE is ignored
        // RRULE
        addRecurrence(object, P_RRULE, task);
        // TODO VALARM
        return object;
    }

    public VersitObject convertAppointment(final Appointment app) throws ConverterException {
        if (null == app) {
            return null;
        }
        modifyRecurring(app);
        final VersitObject object = new VersitObject("VEVENT");
        // TODO CLASS
        addProperty(object, P_CLASS, "PUBLIC");
        // CREATED
        addDateTime(object, "CREATED", app.getCreationDate());
        // DESCRIPTION
        addProperty(object, P_DESCRIPTION, app.getNote());
        // DTSTART
        if (app.getFullTime()) {
            addWeirdTaskDate(object, P_DTSTART, app.getStartDate());
        } else {
            addDateTime(object, P_DTSTART, app.getStartDate());
        }
        // GEO is ignored
        // LAST-MODIFIED
        addDateTime(object, "LAST-MODIFIED", app.getLastModified());
        // LOCATION
        addProperty(object, "LOCATION", app.getLocation());
        // ORGANIZER
        if (organizerMailAddress == null) {
            addAddress(object, P_ORGANIZER, app.getCreatedBy());
        } else {
            addAddress(object, P_ORGANIZER, organizerMailAddress);
        }
        // PRIORITY is ignored
        // DTSTAMP
        addDateTime(object, "DTSTAMP", new Date());
        // TODO SEQUENCE
        // STATUS is ignored
        // SUMMARY
        addProperty(object, P_SUMMARY, app.getTitle());
        // TRANSP
        addProperty(object, "TRANSP", app.getShownAs() == Appointment.FREE ? "TRANSPARENT" : "OPAQUE");
        // UID
        addProperty(object, "UID", app.getObjectID() + atdomain);
        // URL is ignored
        // TODO RECURRENCE-ID
        // DTEND and DURATION
        if (app.getFullTime()) {
            final Calendar cal = new GregorianCalendar();
            cal.setTimeZone(timezone);
            cal.setTime(app.getEndDate());
            cal.add(Calendar.HOUR_OF_DAY, -24);
            final Date end = cal.getTime();
            if (end.after(app.getStartDate())) {
                addWeirdTaskDate(object, "DTEND", end);
            }
        } else {
            addDateTime(object, "DTEND", app.getEndDate());
        }
        // ATTACH
        // TODO addAttachments(object, app, OXAttachment.APPOINTMENT);
        // ATTENDEE
        Iterator<?> i = null;
        if (app.containsParticipants()) {
            final int length = app.getParticipants().length;
            i = new ArrayIterator(app.getParticipants());
            for (int k = 0; k < length; k++) {
                final Participant p = (Participant) i.next();
                if (p.getType() == Participant.USER) {
                    addAddress(object, P_ATTENDEE, p.getEmailAddress());
                }
            }
        }
        // CATEGORIES
        final String cat_str = app.getCategories();
        if (cat_str != null) {
            final ArrayList<String> categories = new ArrayList<String>();
            final StringTokenizer tokenizer = new StringTokenizer(cat_str, ",");
            while (tokenizer.hasMoreTokens()) {
                categories.add(tokenizer.nextToken());
            }
            addProperty(object, P_CATEGORIES, categories);
        }
        // COMMENT is ignored
        // CONTACT is ignored
        // EXDATE
        final ArrayList<DateTimeValue> exlist = new ArrayList<DateTimeValue>();
        addExceptions(exlist, app.getDeleteException());
        addExceptions(exlist, app.getChangeException());
        if (!exlist.isEmpty()) {
            addProperty(object, "EXDATE", exlist);
        }
        // EXRULE is ignored
        // REQUEST-STATUS is ignored
        // TODO RELATED-TO
        // RESOURCES
        final ArrayList<String> resources = new ArrayList<String>();
        if (app.containsParticipants()) {
            final int length = app.getParticipants().length;
            i = new ArrayIterator(app.getParticipants());
            for (int k = 0; k < length; k++) {
                final Participant p = (Participant) i.next();
                if (p.getType() == Participant.RESOURCE) {
                    resources.add(String.valueOf(p.getIdentifier()));
                }
            }
            if (!resources.isEmpty()) {
                addProperty(object, "RESOURCES", resources);
            }
        }
        // RDATE is ignored
        // RRULE
        addRecurrence(object, P_RRULE, app);
        // TODO VALARM
        return object;
    }

    private static void modifyRecurring(final Appointment app) throws ConverterException {
        if (app.getRecurrenceType() != CalendarObject.NONE) {
            RecurringResultsInterface result;
            try {
                final CalendarCollectionService calColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
                result = calColl.calculateFirstRecurring(app);
            } catch (final OXException e) {
                LOG.error("", e);
                throw new ConverterException(e);
            }
            if (result.size() == 1) {
                app.setStartDate(new Date(result.getRecurringResult(0).getStart()));
                app.setEndDate(new Date(result.getRecurringResult(0).getEnd()));
            } else {
                throw new ConverterException("Unable to calculate first occurence of an appointment.");
            }
        }
    }

    private static class ArrayIterator implements Iterator<Object> {

        private final int size;

        private int cursor;

        private final Object array;

        public ArrayIterator(final Object array) {
            final Class<?> type = array.getClass();
            if (!type.isArray()) {
                throw new IllegalArgumentException("Invalid type: " + type);
            }
            this.array = array;
            size = Array.getLength(array);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return (cursor < size);
        }

        @Override
        public Object next() {
            if (cursor < size) {
                return Array.get(array, cursor++);
            }
            throw new NoSuchElementException("No next element present in underlying array");
        }
    }

    private static void addProperty(final VersitObject object, final String name, final Object value) {
        if (value == null) {
            return;
        }
        final Property property = new Property(name);
        property.setValue(value);
        object.addProperty(property);
    }

    private void addDateTime(final VersitObject object, final String name, final Date value) {
        if (value == null) {
            return;
        }
        final DateTimeValue dt = new DateTimeValue();
        dt.calendar.setTimeZone(sendUTC ? DateTimeValue.GMT : timezone);
        dt.calendar.setTime(value);
        dt.isUTC = sendUTC;
        dt.isFloating = sendFloating;
        final Property property = new Property(name);
        property.setValue(dt);
        object.addProperty(property);
    }

    private void addWeirdTaskDate(final VersitObject object, final String name, final Date value) {
        if (value == null) {
            return;
        }
        final DateTimeValue dt = new DateTimeValue();
        dt.calendar.setTimeZone(timezone);
        dt.calendar.setTime(value);
        dt.hasTime = false;
        dt.isFloating = true;
        dt.isUTC = false;
        final Property property = new Property(name);
        final Parameter parameter = new Parameter("VALUE");
        parameter.addValue(new ParameterValue("DATE"));
        property.addParameter(parameter);
        property.setValue(dt);
        object.addProperty(property);
    }

    private void addExceptions(final ArrayList<DateTimeValue> list, final Date[] exceptions) {
        if (exceptions == null) {
            return;
        }
        for (int i = 0; i < exceptions.length; i++) {
            final DateTimeValue dtv = new DateTimeValue();
            dtv.calendar.setTime(exceptions[i]);
            dtv.hasTime = false;
            list.add(dtv);
        }
    }

    private void addAddress(final VersitObject object, final String name, final String address) throws ConverterException {
        try {
            final Property property = new Property(name);
            if (address != null) {
                try {
                    property.setValue(new URI("mailto:" + address));
                } catch (final URISyntaxException e) {
                    final ConverterException ce = new ConverterException(e.getMessage());
                    ce.initCause(e);
                    throw ce;
                }
                object.addProperty(property);
            }
        } catch (final Exception e) {
            LOG.error(e.toString());
            throw new ConverterException(e);
        }
    }

    private void addAddress(final VersitObject object, final String name, final int userId) throws ConverterException {
        try {
            final User userObj = UserStorage.getInstance().getUser(userId, ctx);
            if (userObj == null) {
                return;
            }
            final Property property = new Property(name);
            final String address = userObj.getMail();
            if (address != null) {
                try {
                    property.setValue(new URI("mailto:" + IDNA.toACE(address)));
                } catch (final URISyntaxException e) {
                    final ConverterException ce = new ConverterException(e.getMessage());
                    ce.initCause(e);
                    throw ce;
                }
                object.addProperty(property);
            }
        } catch (final Exception e) {
            LOG.error(e.toString());
            throw new ConverterException(e);
        }
    }

    private static void addRecurrence(final VersitObject object, final String name, final CalendarObject oxobject) {
        if (oxobject.getRecurrenceType() != CalendarObject.NONE) {
            final RecurrenceValue recur = new RecurrenceValue();
            final Date until = oxobject.getUntil();
            if (until != null) {
                recur.Until = new DateTimeValue();
                recur.Until.calendar.setTime(until);
            }
            final int interval = oxobject.getInterval();
            if (interval != 1) {
                recur.Interval = interval;
            }
            final int type = oxobject.getRecurrenceType();
            switch (oxobject.getRecurrenceType()) {
                case CalendarObject.YEARLY:
                    final int[] byMonth = { oxobject.getMonth() - Calendar.JANUARY + 1 };
                    recur.ByMonth = byMonth;
                    // no break
                case CalendarObject.MONTHLY:
                    final int monthDay = oxobject.getDayInMonth();
                    final int mdays = oxobject.getDays();
                    if (mdays == 0) {
                        final int[] byMonthDay = { monthDay };
                        recur.ByMonthDay = byMonthDay;
                    } else {
                        for (int i = 0; i < 7; i++) {
                            if ((mdays & (1 << i)) != 0) {
                                recur.ByDay.add(new Weekday(monthDay, Calendar.SUNDAY + i));
                            }
                        }
                    }
                    break;
                case CalendarObject.WEEKLY:
                    final int days = oxobject.getDays();
                    for (int i = 0; i < 7; i++) {
                        if ((days & (1 << i)) != 0) {
                            recur.ByDay.add(new Weekday(0, Calendar.SUNDAY + i));
                        }
                    }
            }
            final int[] freqs = { RecurrenceValue.DAILY, RecurrenceValue.WEEKLY, RecurrenceValue.MONTHLY, RecurrenceValue.YEARLY };
            recur.Freq = freqs[type - CalendarObject.DAILY];
            addProperty(object, name, recur);
        }
    }

    public static VersitObject newCalendar(final String version) {
        final VersitObject object = new VersitObject("VCALENDAR");
        Property property = new Property("VERSION");
        property.setValue(version);
        object.addProperty(property);
        property = new Property("PRODID");
        property.setValue("OPEN-XCHANGE");
        object.addProperty(property);
        return object;
    }

    private static final BitSet PRINTABLE_CHARS = new BitSet(256);
    // Static initializer for printable chars collection
    static {
        for (int i = '0'; i <= '9'; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (int i = 'a'; i <= 'z'; i++) {
            PRINTABLE_CHARS.set(i);
        }
    }

}
