/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
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

package com.openexchange.calendar.api;

import static com.openexchange.calendar.Tools.getSqlInString;
import static com.openexchange.java.Autoboxing.I;
import gnu.trove.set.TIntSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.api2.ReminderService;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.calendar.CachedCalendarIterator;
import com.openexchange.calendar.CalendarMySQL;
import com.openexchange.calendar.CalendarOperation;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.calendar.CalendarSqlImp;
import com.openexchange.calendar.RecurringResult;
import com.openexchange.calendar.Tools;
import com.openexchange.calendar.recurrence.RecurringCalculation;
import com.openexchange.databaseold.Database;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarConfig;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.MBoolean;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.data.Check;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.Strings;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;

/**
 * {@link CalendarCollection} - Provides calculation routines for recurring calendar items.
 *
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalendarCollection implements CalendarCollectionService {

    private static final char DELIMITER_PIPE = '|';

    private static int NO_END_YEARS = 4;

    /**
     * @deprecated use {@link Constants#MILLI_HOUR}.
     */
    @Deprecated
    public final long MILLI_HOUR = Constants.MILLI_HOUR;

    /**
     * @deprecated use {@link Constants#MILLI_DAY}.
     */
    @Deprecated
    public final long MILLI_DAY = Constants.MILLI_DAY;

    /**
     * @deprecated use {@link Constants#MILLI_WEEK}.
     */
    @Deprecated
    public final long MILLI_WEEK = Constants.MILLI_WEEK;

    /**
     * @deprecated use {@link Constants#MILLI_MONTH}.
     */
    @Deprecated
    public final long MILLI_MONTH = Constants.MILLI_MONTH;

    /**
     * @deprecated use {@link Constants#MILLI_YEAR}.
     */
    @Deprecated
    public final long MILLI_YEAR = Constants.MILLI_YEAR;

    /**
     * 'UTC' time zone
     */
    public static final TimeZone ZONE_UTC = TimeZone.getTimeZone("UTC");

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarCollection.class);

    public CalendarCollection() {
        super();
        fieldMap.put(Integer.valueOf(CalendarObject.TITLE), "field01");

        fieldMap.put(Integer.valueOf(Appointment.LOCATION), "field02");
        fieldMap.put(Integer.valueOf(CalendarObject.NOTE), "field04");
        fieldMap.put(Integer.valueOf(CalendarObject.RECURRENCE_TYPE), "field06");
        fieldMap.put(Integer.valueOf(CalendarObject.DELETE_EXCEPTIONS), "field07");
        fieldMap.put(Integer.valueOf(CalendarObject.CHANGE_EXCEPTIONS), "field08");
        fieldMap.put(Integer.valueOf(CommonObject.CATEGORIES), "field09");

        fieldMap.put(Integer.valueOf(CalendarObject.START_DATE), "timestampfield01");
        fieldMap.put(Integer.valueOf(CalendarObject.END_DATE), "timestampfield02");

        fieldMap.put(Integer.valueOf(DataObject.OBJECT_ID), "intfield01");
        fieldMap.put(Integer.valueOf(CalendarObject.RECURRENCE_ID), "intfield02");
        fieldMap.put(Integer.valueOf(CommonObject.COLOR_LABEL), "intfield03");
        fieldMap.put(Integer.valueOf(CalendarObject.RECURRENCE_CALCULATOR), "intfield04");
        fieldMap.put(Integer.valueOf(CalendarObject.RECURRENCE_POSITION), "intfield05");
        fieldMap.put(Integer.valueOf(Appointment.SHOWN_AS), "intfield06");
        fieldMap.put(Integer.valueOf(Appointment.FULL_TIME), "intfield07");
        fieldMap.put(Integer.valueOf(CommonObject.NUMBER_OF_ATTACHMENTS), "intfield08");
        fieldMap.put(Integer.valueOf(CommonObject.PRIVATE_FLAG), "pflag");

        fieldMap.put(Integer.valueOf(DataObject.CREATED_BY), "pd.created_from");
        fieldMap.put(Integer.valueOf(DataObject.MODIFIED_BY), "pd.changed_from");
        fieldMap.put(Integer.valueOf(DataObject.CREATION_DATE), "pd.creating_date");
        fieldMap.put(Integer.valueOf(DataObject.LAST_MODIFIED), "pd.changing_date");

        fieldMap.put(Integer.valueOf(FolderChildObject.FOLDER_ID), "fid");
        fieldMap.put(Integer.valueOf(Appointment.TIMEZONE), "timezone");

        fieldMap.put(Integer.valueOf(CalendarObject.ORGANIZER), "organizer");
        fieldMap.put(Integer.valueOf(CommonObject.UID), "uid");
        fieldMap.put(Integer.valueOf(CalendarObject.SEQUENCE), "sequence");
        fieldMap.put(Integer.valueOf(CalendarObject.ORGANIZER_ID), "organizerId");
        fieldMap.put(Integer.valueOf(CalendarObject.PRINCIPAL), "principal");
        fieldMap.put(Integer.valueOf(CalendarObject.PRINCIPAL_ID), "principalId");
        fieldMap.put(Integer.valueOf(CommonObject.FILENAME), "filename");
    }

    /**
     * <code>getMAX_END_YEARS</code> returns NO_END_YEARS.
     * NO_END_YEARS means if no end date is given we calculate the start date
     * PLUS NO_END_YEARS to have an end date ...
     *
     * @return an <code>int</code> value
     */
    @Override
    public int getMAX_END_YEARS() {
        return NO_END_YEARS;
    }

    /**
     * <code>setMAX_END_YEARS</code> sets the max
     * number of years a sequence can run if no end date
     * is given
     *
     * @return an <code>int</code> value
     */
    @Override
    public void setMAX_END_YEARS(final int MAX_END_YEARS) {
        NO_END_YEARS = MAX_END_YEARS;
    }

    /**
     * Converts the recurring pattern from specified recurring appointment
     * into its corresponding recurring fields to properly reflect pattern's
     * recurrence.
     *
     * @param cdao The recurring appointment
     * @throws OXException If recurring appointment's pattern is invalid
     */
    private void convertDSString(final CalendarDataObject cdao) throws OXException {
        char name;
        String value;
        final String ds = cdao.getRecurrence();
        if (ds != null) {
            int s = 0;
            int f = 0;
            while ((f = ds.indexOf(DELIMITER_PIPE, s)) != -1) {
                name = ds.charAt(s);
                s = f + 1;
                f = ds.indexOf(DELIMITER_PIPE, s);
                if (f == -1) {
                    value = ds.substring(s, ds.length());
                    encodeNameValuePair(name, value, cdao);
                    break;
                }
                value = ds.substring(s, f);
                encodeNameValuePair(name, value, cdao);
                s = f + 1;
            }
        }
        checkAndCorrectErrors(cdao);
    }

    private void checkAndCorrectErrors(final CalendarDataObject cdao) {
        if (cdao.getInterval() > MAX_OCCURRENCESE) {
            final OXException exc = OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(cdao.getInterval()), Integer.valueOf(MAX_OCCURRENCESE));
            LOG.warn("{} Auto-corrected to {}", exc.getMessage(), MAX_OCCURRENCESE, exc);
            cdao.setInterval(MAX_OCCURRENCESE);
        }
        if (cdao.getOccurrence() > MAX_OCCURRENCESE) {
            final OXException exc = OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(cdao.getOccurrence()), Integer.valueOf(MAX_OCCURRENCESE));
            LOG.warn("{} Auto-corrected to {}", exc.getMessage(), MAX_OCCURRENCESE, exc);
            cdao.setOccurrence(MAX_OCCURRENCESE);
        }
        if (cdao.getRecurrenceType() == CalendarObject.DAILY) {
            if (cdao.getInterval() < 1) {
                LOG.debug("Auto correction (daily), set interval to 1, the given interval was: {}", cdao.getInterval());
                cdao.setInterval(1);
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.WEEKLY) {
            if (cdao.getInterval() < 1) {
                LOG.debug("Auto correction (weekly), set interval to 1, the given interval was: {}", cdao.getInterval());
                cdao.setInterval(1);
            }
            if (cdao.getDays() < 1) {
                LOG.debug("Auto correction (weekly), set day to CalendarDataObject.MONDAY, the given day was: {}", cdao.getDays());
                cdao.setDays(CalendarObject.MONDAY);
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.MONTHLY) {
            if (cdao.getInterval() < 1) {
                LOG.debug("Auto correction (montly), set interval to 1, the given interval was: {}", cdao.getInterval());
                cdao.setInterval(1);
            }
            if (cdao.containsDays() && cdao.getDays() != 0 && (getDay(cdao.getDays()) == -1)) {
                // if (getDay(cdao.getDays()) == -1) {
                LOG.debug("Auto correction (monthly), set day to CalendarDataObject.MONDAY, the given day was: {}", cdao.getDays());
                cdao.setDays(CalendarObject.MONDAY);
                // }
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.YEARLY) {
            if (cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                LOG.debug("Auto correction (monthy), set month to 1, the given interval was: {}", cdao.getMonth());
                cdao.setMonth(Calendar.JANUARY);
            }
            if (cdao.containsDays() && (getDay(cdao.getDays()) == -1)) {
                // if (getDay(cdao.getDays()) == -1) {
                LOG.debug("Auto correction (yearly), set day to CalendarDataObject.MONDAY, the given day was: {}", cdao.getDays());
                cdao.setDays(CalendarObject.MONDAY);
                // }
            }
        }
    }

    /**
     * Applies the given name-value-pair to specified calendar object
     *
     * @param name
     *            The name identifier
     * @param value
     *            The value
     * @param cdao
     *            The calendar object
     * @throws OXException
     *             If an unknown name-value-pair occurs
     */
    private void encodeNameValuePair(final char name, final String value, final CalendarDataObject cdao)
        throws OXException {
        if (name == 't') {
            int t = Integer.parseInt(value);
            if (t == 5) {
                t = 3;
            } else if (t == 6) {
                t = 4;
            }
            cdao.setRecurrenceType(t);
        } else if (name == 'i') {
            cdao.setInterval(Integer.parseInt(value));
        } else if (name == 'a') {
            cdao.setDays(Integer.parseInt(value));
        } else if (name == 'b') {
            cdao.setDayInMonth(Integer.parseInt(value));
        } else if (name == 'c') {
            cdao.setMonth(Integer.parseInt(value));
        } else if (name == 'e') {
            final long u = Long.parseLong(value);
            cdao.setUntil(new java.util.Date(u));
        } else if (name == 's') {
            final long s = Long.parseLong(value);
            cdao.setRecurringStart(s);
        } else if (name == 'o') {
            cdao.setOccurrence(Integer.parseInt(value));
        } else {
            throw OXCalendarExceptionCodes.UNKNOWN_NVP_IN_REC_STR.create(Character.valueOf(name),
                value);
        }
    }

    /**
     * Checks if given calendar data object denotes a recurring master.
     *
     * @param edao
     *            The calendar data object to check
     * @return <code>true</code> if given calendar data object denotes a
     *         recurring master; otherwise <code>false</code>
     */
    @Override
    public boolean isRecurringMaster(final CalendarDataObject edao) {
        if (edao == null) {
            return false;
        }
        return (edao.containsRecurrenceID() && edao.containsObjectID() && (edao.getRecurrenceID() > 0)
            && (edao.getObjectID() > 0) && (edao.getRecurrenceID() == edao.getObjectID()));
    }

    /**
     * <code>getRecurringAppointmentDeleteAction</code> detects and returns
     * the action type
     *
     * @param cdao a <code>CalendarDataObject</code> object (tranfered)
     * @param edao a <code>CalendarDataObject</code> object (loaded)
     * @return a <code>int</code> value
     */
    @Override
    public int getRecurringAppointmentDeleteAction(final CalendarDataObject cdao, final CalendarDataObject edao) {
        int rada = RECURRING_NO_ACTION;
        if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0) {
            if (edao.getRecurrenceID() == edao.getObjectID() && edao.getRecurrencePosition() == 0) {
                if (cdao.containsRecurrencePosition()) {
                    // virtual delete
                    rada = RECURRING_VIRTUAL_ACTION;
                } else if (cdao.containsRecurrenceDatePosition()) {
                    rada = RECURRING_VIRTUAL_ACTION;
                } else {
                    rada = RECURRING_FULL_DELETE;
                }
            } else if (edao.getRecurrenceID() != edao.getObjectID()) {
                // real exception delete
                rada = RECURRING_EXCEPTION_ACTION;
            }
        }
        return rada;
    }

    /**
     * <code>getRecurringAppoiontmentUpdateAction</code> detects and returns
     * the action type
     *
     * @param cdao a <code>CalendarDataObject</code> object (transfered)
     * @param edao a <code>CalendarDataObject</code> object (loaded)
     * @return a <code>int</code> value
     */
    @Override
    public int getRecurringAppoiontmentUpdateAction(final CalendarDataObject cdao, final CalendarDataObject edao) {
        int rada = RECURRING_NO_ACTION;
        /*
         * Check if edao denotes the main recurring appointment
         */
        if (edao.containsRecurrenceID() && edao.getRecurrenceID() > 0 && edao.getRecurrenceID() == edao.getObjectID()) {
            /*
             * Check if cdao denotes a change exception of a recurring appointment
             */
            if (cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) {
                rada = RECURRING_CREATE_EXCEPTION;
            } else if (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null) {
                rada = RECURRING_CREATE_EXCEPTION;
            }
            if (cdao.containsDeleteExceptions() && edao.containsChangeExceptions()) {
                if (checkIfArrayKeyExistInArray(cdao.getDeleteException(), edao
                    .getChangeException())) {
                    rada = RECURRING_EXCEPTION_DELETE_EXISTING;
                }
            }
        }
        return rada;
    }

    /**
     * <code>getLongByPosition</code> return the long value
     * for the given CalendarDataObject and the given recurring
     * position. The method return 0 if the long can not be calculated.
     *
     * @param cdao a <code>CalendarDataObject</code>
     * @param pos a <code>int</code>
     * @return a <code>long</code> value
     */
    @Override
    public long getLongByPosition(final CalendarDataObject cdao, final int pos) throws OXException {
        fillDAO(cdao);
        final RecurringResultsInterface rrs = calculateRecurring(cdao, 0, 0, pos);
        if (rrs != null && rrs.size() == 1) {
            final RecurringResultInterface rr = rrs.getRecurringResult(0);
            if (pos == rr.getPosition()) {
                return rr.getNormalized();
            }
            return 0;
        }
        return 0;
    }

    static Date[] mergeExceptions(final Date[] new_dates, final Date[] old_dates) {
        if (new_dates != null && old_dates == null) {
            return new_dates;
        } else if (new_dates != null && old_dates != null) {
            final Date dates[] = new Date[new_dates.length + old_dates.length];
            System.arraycopy(old_dates, 0, dates, 0, old_dates.length);
            System.arraycopy(new_dates, 0, dates, old_dates.length, new_dates.length);
            return dates;
        }
        return null;
    }

    @Override
    public void setRecurrencePositionOrDateInDAO(final CalendarDataObject cdao)
        throws OXException {
        setRecurrencePositionOrDateInDAO(cdao, false);
    }

    @Override
    public void setRecurrencePositionOrDateInDAO(final CalendarDataObject cdao, final boolean ignore_exceptions) throws OXException {
        if (cdao.containsRecurrencePosition() && cdao.getRecurrencePosition() > 0) {
            /*
             * Determine recurrence date position from recurrence position
             */
            fillDAO(cdao);
            RecurringResultsInterface rrs = calculateRecurring(cdao, 0, 0, cdao.getRecurrencePosition());
            RecurringResultInterface rr = rrs.getRecurringResult(0);
            if (rr == null && ignore_exceptions) {
                rrs = calculateRecurring(cdao, 0, 0, cdao.getRecurrencePosition(), MAX_OCCURRENCESE, true);
                if (rrs == null) {
                    throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_POSITION.create();
                }
                rr = rrs.getRecurringResult(0);
            }
            if (rr != null) {
                cdao.setRecurrenceDatePosition(new Date(rr.getNormalized()));
                return;
            }

            throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_POSITION.create();
        } else if (cdao.containsRecurrenceDatePosition() && cdao.getRecurrenceDatePosition() != null) {
            /*
             * Determine recurrence position from recurrence date position
             */
            fillDAO(cdao);

            final long normalized = normalizeLong(cdao.getRecurrenceDatePosition().getTime());
            final long rangeStart = normalized - Constants.MILLI_WEEK;
            final long rangeEnd = normalized + Constants.MILLI_WEEK;

            final RecurringResultsInterface rrs = calculateRecurring(cdao, rangeStart, rangeEnd, 0);
            if (rrs == null) {
                return;
            }
            final int x = rrs.getPositionByLong(cdao.getRecurrenceDatePosition().getTime());
            if (x > 0) {
                cdao.setRecurrencePosition(x);
                return;
            }

            throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_POSITION.create();
        } else {
            throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_RECURRING_POSITION_NO_INPUT.create();
        }
    }

    /**
     * Removes hours and minutes for the given date.
     *
     * @param millis
     *            milliseconds since January 1, 1970, 00:00:00 GMT not to exceed
     *            the milliseconds representation for the year 8099. A negative
     *            number indicates the number of milliseconds before January 1,
     *            1970, 00:00:00 GMT.
     * @return The normalized <code>long</code> value
     */
    @Override
    public long normalizeLong(final long millis) {
        return millis - (millis % Constants.MILLI_DAY);
    }

    /**
     * Checks if specified UTC date increases day in month if adding given time
     * zone's offset.
     *
     * @param millis
     *            The time millis
     * @param timeZoneID
     *            The time zone ID
     * @return <code>true</code> if specified date in increases day in month if
     *         adding given time zone's offset; otherwise <code>false</code>
     */
    @Override
    public boolean exceedsHourOfDay(final long millis, final String timeZoneID) {
        return exceedsHourOfDay(millis, Tools.getTimeZone(timeZoneID));
    }

    /**
     * Checks if specified UTC date increases day in month if adding given time
     * zone's offset.
     *
     * @param millis
     *            The time millis
     * @param zone
     *            The time zone
     * @return <code>true</code> if specified date in increases day in month if
     *         adding given time zone's offset; otherwise <code>false</code>
     */
    @Override
    public boolean exceedsHourOfDay(final long millis, final TimeZone zone) {
        final Calendar cal = Calendar.getInstance(CalendarCollection.ZONE_UTC);
        cal.setTimeInMillis(millis);
        final long hours = cal.get(Calendar.HOUR_OF_DAY) + (zone.getOffset(millis) / Constants.MILLI_HOUR);
        return hours >= 24 || hours < 0;
    }

    /**
     * Creates the recurring pattern for given (possibly recurring) appointment
     * if needed and fills its recurring information according to generated
     * pattern.
     *
     * @param cdao
     *            The (possibly recurring) appointment
     * @return <code>true</code> if specified appointment denotes a proper
     *         recurring appointment whose recurring information could be
     *         successfully filled; otherwise <code>false</code> to indicate a failure
     */
    @Override
    public boolean fillDAO(final CalendarDataObject cdao) throws OXException {
        if (cdao.getRecurrence() == null || cdao.getRecurrence().indexOf(DELIMITER_PIPE) == -1) {
            if (cdao.getRecurrenceType() == 0) {
                // No recurring appointment
                return false;
            }
            if ((cdao.getInterval() == 0 && cdao.getMonth() == 0) || cdao.getStartDate() == null
                || cdao.getEndDate() == null) {
                // Insufficient information
                return false;
            }
            changeRecurrenceString(cdao);
        }
        try {
            convertDSString(cdao);
            return true;
        } catch (final OXException e) {
            LOG.error("fillDAO:convertDSString error.", e);
        }
        return false;
    }

    public void changeRecurrenceString(final CalendarDataObject cdao) throws OXException {
        final String recString = createDSString(cdao);
        if (recString == null) {
            cdao.removeRecurrenceID();
        }
        cdao.setRecurrence(recString);
    }

    /**
     * Creates the recurring string for specified recurring appointment
     *
     * @param cdao The recurring appointment whose recurring string shall be created
     * @return The recurring string for specified recurring appointment
     * @throws OXException If recurring appointment contains insufficient or invalid recurring information
     */
    @Override
    public String createDSString(final CalendarDataObject cdao) throws OXException {
        if (cdao.containsStartDate()) {
            checkRecurring(cdao);
            StringBuilder recStrBuilder = new StringBuilder(64);
            final int recurrenceType = cdao.getRecurrenceType();
            int interval = cdao.getInterval(); // i
            if (interval > MAX_OCCURRENCESE) {
                final OXException exc = OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(interval), Integer
                    .valueOf(MAX_OCCURRENCESE));
                LOG.warn("{} Auto-corrected to {}", exc.getMessage(), MAX_OCCURRENCESE, exc);
                interval = MAX_OCCURRENCESE;
            }
            final int weekdays = cdao.getDays();
            final int monthday = cdao.getDayInMonth();
            final int month = cdao.getMonth();
            int occurrences = cdao.getOccurrence();
            if (occurrences > MAX_OCCURRENCESE) {
                final OXException exc = OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(occurrences),
                    Integer.valueOf(MAX_OCCURRENCESE));
                LOG.warn("{} Auto-corrected to {}", exc.getMessage(), MAX_OCCURRENCESE, exc);
                occurrences = MAX_OCCURRENCESE;
            }
            if (!cdao.containsUntil() && !cdao.containsOccurrence()) {
                occurrences = -1;
            }
            if (recurrenceType == CalendarObject.DAILY) {
                dsf(recStrBuilder, 1);
                dsf(recStrBuilder, 'i', interval);
                dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (occurrences > 0) {
                    cdao.setUntil(calculateUntilOfSequence(cdao));
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    dsf(recStrBuilder, 'o', occurrences);
                } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                }
            } else if (recurrenceType == CalendarObject.WEEKLY) {
                dsf(recStrBuilder, 2);
                dsf(recStrBuilder, 'i', interval);
                dsf(recStrBuilder, 'a', weekdays);
                dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                cdao.setRecurringStart(cdao.getStartDate().getTime());
                if (occurrences > 0) {
                    cdao.setUntil(calculateUntilOfSequence(cdao));
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    dsf(recStrBuilder, 'o', occurrences);
                } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                    dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                }
            } else if (recurrenceType == CalendarObject.MONTHLY) {
                if (monthday <= 0) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL.create(Integer.valueOf(monthday));
                }
                if (weekdays <= 0) {
                    if (monthday > 31) {
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL.create(Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 3);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilOfSequence(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                } else {
                    if (monthday > 5) {
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY_2.create(Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 5);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('a').append(DELIMITER_PIPE).append(weekdays).append(DELIMITER_PIPE);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilOfSequence(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                }
            } else if (recurrenceType == CalendarObject.YEARLY) {
                if (weekdays <= 0) {
                    if (monthday <= 0 || monthday > 31) {
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_INTERVAL.create(Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 4);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 'c', month);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilOfSequence(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                } else {
                    if (monthday < 1 || monthday > 5) {
                        throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_TYPE.create(Integer.valueOf(monthday));
                    }
                    dsf(recStrBuilder, 6);
                    dsf(recStrBuilder, 'i', interval);
                    recStrBuilder.append('a').append(DELIMITER_PIPE).append(weekdays).append(DELIMITER_PIPE);
                    recStrBuilder.append('b').append(DELIMITER_PIPE).append(monthday).append(DELIMITER_PIPE);
                    dsf(recStrBuilder, 'c', month);
                    dsf(recStrBuilder, 's', cdao.getStartDate().getTime());
                    cdao.setRecurringStart(cdao.getStartDate().getTime());
                    if (occurrences > 0) {
                        cdao.setUntil(calculateUntilOfSequence(cdao));
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                        dsf(recStrBuilder, 'o', occurrences);
                    } else if (cdao.containsUntil() && cdao.getUntil() != null) {
                        dsf(recStrBuilder, 'e', cdao.getUntil().getTime());
                    }
                }
            } else {
                recStrBuilder = null;
            }
            return recStrBuilder == null ? null : recStrBuilder.toString();
        }
        throw OXCalendarExceptionCodes.RECURRING_MISSING_START_DATE.create();
    }

    private Date calculateUntilOfSequence(final CalendarDataObject cdao) throws OXException {
        final Date temp = getOccurenceDate(cdao);
        temp.setTime(temp.getTime() - cdao.getRecurrenceCalculator() * Constants.MILLI_DAY);
        return temp;
    }

    /**
     * Gets the specified occurrence's end date within recurring appointment.
     *
     * @param cdao The recurring appointment
     * @return The first occurrence's end date
     * @throws OXException If calculating the first occurrence fails
     */
    @Override
    public Date getOccurenceDate(final CalendarDataObject cdao) throws OXException {
        return getOccurenceDate(cdao, cdao.getOccurrence());
    }

    /**
     * Gets the given occurrence's end date within specified recurring appointment.
     *
     * @param cdao The recurring appointment
     * @param occurrence The occurrence
     * @return The first occurrence's end date
     * @throws OXException If calculating the first occurrence fails
     */
    @Override
    public Date getOccurenceDate(final CalendarDataObject cdao, final int occurrence) throws OXException {
        final RecurringResultsInterface rss = calculateRecurring(cdao, 0, 0, occurrence, 1, true, true);
        final RecurringResultInterface rs = rss.getRecurringResult(0);
        if (rs != null) {
            return new Date(rs.getEnd());
        }
        LOG.warn("Unable to calculate until date :{}", cdao);
        return new Date(cdao.getStartDate().getTime() + Constants.MILLI_YEAR);
    }

    /**
     * Checks if normalized date of given time millis is contained in either specified change exceptions or delete exceptions.
     *
     * @param t The time millis to check
     * @param ce The change exceptions
     * @param de The delete exceptions
     * @return <code>true</code>if normalized date of given time millis denotes an exception; otherwise <code>false</code>
     */
    @Override
    public boolean isException(final long t, final Set<Long> ce, final Set<Long> de) {
        final Long check = Long.valueOf(normalizeLong(t));
        return (null == ce ? false : ce.contains(check)) || (null == de ? false : de.contains(check));
    }

    /**
     * Tests if specified date is covered by any occurrence of given recurring appointment ignoring second specified date.
     * <p>
     * This method is useful when creating a new change exception within specified recurring appointment and checking that change
     * exception's destination date is not already occupied by either a regular recurrence's occurrence. or an existing change exception
     *
     * @param date The date to check
     * @param ignoreDate The date to ignore
     * @param cdao The recurring appointment to check against
     * @param changeExceptions The recurring appointment's change exception dates
     * @return <code>true</code> if specified time millis is covered by any occurrence; otherwise <code>false</code>
     * @throws OXException If calculating the occurrences fails
     */
    @Override
    public boolean isOccurrenceDate(final long date, final long ignoreDate, final CalendarDataObject cdao, final long[] changeExceptions) throws OXException {
        /*
         * Since we check dates here, normalize given time millis
         */
        final long check = normalizeLong(date);
        final long ign = normalizeLong(ignoreDate);
        if (check == ign) {
            /*
             * Original and new date are equal
             */
            return false;
        }
        final RecurringResultsInterface rss = calculateRecurring(cdao, check - Constants.MILLI_WEEK, check + Constants.MILLI_WEEK, 0);
        /*
         * Check regular occurrences
         */
        {
            final int size = rss.size();
            for (int i = 0; i < size; i++) {
                final long cur = rss.getRecurringResult(i).getNormalized();
                if (cur != ign && cur == check) {
                    /*
                     * Date already occupied by a regular occurrence
                     */
                    return true;
                }
            }
        }
        /*
         * Check change exceptions
         */
        for (int i = 0; i < changeExceptions.length; i++) {
            if (changeExceptions[i] == check) {
                /*
                 * Date already occupied by a change exception
                 */
                return true;
            }
        }
        return false;
    }

    /**
     * This method calculates the first occurrence and stores it within the returned {@link RecurringResultsInterface} collection.
     *
     * @param cdao The recurring appointment whose first occurrence shall be calculated
     * @return The calculated first occurrence kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the first occurrence fails
     */
    @Override
    public RecurringResultsInterface calculateFirstRecurring(final CalendarObject cdao) throws OXException {
        return calculateRecurring(cdao, 0, 0, 1, MAX_OCCURRENCESE, true, true);
    }

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResultsInterface} collection.
     * <p>
     * <b>! This method returns max. {@link #MAX_OCCURRENCESE} results AND ignores exceptions !</b>
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     *
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @return The calculated occurrences including change/delete exceptions kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the occurrences fails
     */
    @Override
    public RecurringResultsInterface calculateRecurringIgnoringExceptions(final CalendarObject cdao, final long range_start, final long range_end, final int pos) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, MAX_OCCURRENCESE, true, false);
    }

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResultsInterface} collection.
     * <p>
     * <b>! This method returns max. {@link #MAX_OCCURRENCESE} results !</b>
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     *
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @return The calculated occurrences without change/delete exceptions kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the occurrences fails
     */
    @Override
    public RecurringResultsInterface calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, MAX_OCCURRENCESE, false, false);
    }

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResultsInterface} collection.
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     *
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @param PMAXTC The max. number of occurrences to calculate; mostly set to {@link #MAX_OCCURRENCESE}
     * @param ignore_exceptions <code>true</code> to ignore change and delete exceptions during calculation, meaning corresponding occurrences do not appear in returned {@link RecurringResultsInterface} collection; otherwise <code>false</code>
     * @return The calculated occurrences kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the occurrences fails
     */
    @Override
    public RecurringResultsInterface calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions) throws OXException {
        return calculateRecurring(cdao, range_start, range_end, pos, PMAXTC, ignore_exceptions, false);
    }

    /**
     * This method calculates the recurring occurrences and stores them within the returned {@link RecurringResultsInterface} collection.
     * <p>
     * A certain occurrence can be calculated by setting parameter {@code pos}.
     * <p>
     * A range query is performed when setting parameter {@code range_start} and {@code range_end}.
     *
     * @param cdao The recurring appointment whose occurrences shall be calculated
     * @param range_start The (optional) range start from which occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param range_end The (optional) range end until occurrences shall be calculated; leave to <code>0</code> to ignore
     * @param pos The (optional) one-based occurrence position to calculate; leave to <code>0</code> to ignore
     * @param PMAXTC The max. number of occurrences to calculate; mostly set to {@link #MAX_OCCURRENCESE}
     * @param ignore_exceptions <code>true</code> to ignore change and delete exceptions during calculation, meaning corresponding occurrences do not appear in returned {@link RecurringResultsInterface} collection; otherwise <code>false</code>
     * @param calc_until This parameter is not used, yet
     * @return The calculated occurrences kept in a {@link RecurringResultsInterface} collection
     * @throws OXException If calculating the occurrences fails
     */
    @Override
    public RecurringResultsInterface calculateRecurring(final CalendarObject cdao, final long range_start, final long range_end, final int pos, final int PMAXTC, final boolean ignore_exceptions, final boolean calc_until) throws OXException {
        String change_exceptions = null;
        String delete_exceptions = null;
        String calc_timezone = "UTC";
        final long recurringStart;
        if (cdao instanceof CalendarDataObject) {
            final CalendarDataObject calDataObject = (CalendarDataObject) cdao;
            if (!ignore_exceptions) {
                change_exceptions = calDataObject.getExceptions();
                delete_exceptions = calDataObject.getDelExceptions();
            }
            if (!calDataObject.getFullTime()) {
                if (calDataObject.containsTimezone()) {
                    calc_timezone = calDataObject.getTimezone();
                } else {
                    final OXException e = OXCalendarExceptionCodes.TIMEZONE_MISSING.create();
                    LOG.warn("", e);
                }
            }
            recurringStart = calDataObject.getRecurringStart();
        } else {
            recurringStart = ((cdao.getStartDate().getTime() / Constants.MILLI_DAY) * Constants.MILLI_DAY);
        }

        final RecurringCalculation rc = new RecurringCalculation(cdao.getRecurrenceType(), cdao.getInterval(), cdao
            .getRecurrenceCalculator());
        rc.setCalculationTimeZone(calc_timezone);
        rc.setCalculationPosition(pos);
        rc.setRange(range_start, range_end);
        rc.setMaxCalculation(PMAXTC);
        rc.setMaxOperations(CalendarConfig.getMaxOperationsInRecurrenceCalculations());
        rc.setExceptions(change_exceptions, delete_exceptions);
        rc.setStartAndEndTime(cdao.getStartDate().getTime(), cdao.getEndDate().getTime());
        rc.setRecurringStart(recurringStart);

        if (cdao.containsUntil() && cdao.getUntil() != null) {
            rc.setUntil(cdao.getUntil().getTime());
        }

        if (cdao.containsOccurrence() && cdao.getOccurrence() > 0) {
            rc.setOccurrence(cdao.getOccurrence());
        }
        if (cdao.containsDays()) {
            rc.setDays(cdao.getDays());
        }
        if (cdao.containsDayInMonth()) {
            rc.setDayInMonth(cdao.getDayInMonth());
        }
        if (cdao.containsMonth()) {
            rc.setMonth(cdao.getMonth());
        }
        return rc.calculateRecurrence();
    }

    private static Map<Integer, Integer> DAY_MAP = new HashMap<Integer, Integer>(10);

    static {
        DAY_MAP.put(Integer.valueOf(CalendarObject.SATURDAY), Integer.valueOf(Calendar.SATURDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.FRIDAY), Integer.valueOf(Calendar.FRIDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.THURSDAY), Integer.valueOf(Calendar.THURSDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.WEDNESDAY), Integer.valueOf(Calendar.WEDNESDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.TUESDAY), Integer.valueOf(Calendar.TUESDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.MONDAY), Integer.valueOf(Calendar.MONDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.SUNDAY), Integer.valueOf(Calendar.SUNDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.DAY), Integer.valueOf(CalendarObject.DAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.WEEKDAY), Integer.valueOf(CalendarObject.WEEKDAY));
        DAY_MAP.put(Integer.valueOf(CalendarObject.WEEKENDDAY), Integer.valueOf(CalendarObject.WEEKENDDAY));
    }

    /**
     * Maps given day constant from {@link CalendarObject} to the corresponding
     * day from {@link Calendar}.
     *
     * @param cd
     *            The day constant from {@link CalendarObject}: {@link CalendarObject#SUNDAY}, {@link CalendarObject#MONDAY}, {@link CalendarObject#TUESDAY}, {@link CalendarObject#WEDNESDAY}, {@link CalendarObject#THURSDAY},
     *            {@link CalendarObject#FRIDAY}, {@link CalendarObject#SATURDAY},{@link CalendarObject#DAY}, {@link CalendarObject#WEEKDAY}, or {@link CalendarObject#WEEKENDDAY}
     * @return The corresponding day from {@link Calendar} or <code>-1</code>.
     */
    private int getDay(final int cd) {
        final Integer retval = DAY_MAP.get(Integer.valueOf(cd));
        if (retval == null) {
            LOG.error("Unusable getDay parameter (days) :{}", cd, new Throwable());
            return -1;
        }
        return retval.intValue();
    }

    @Override
    public void fillMap(final RecurringResultsInterface rss, final long s, final long diff, final int d, final int counter) {
        final RecurringResult rs = new RecurringResult(s, diff, d, counter);
        rss.add(rs);
    }

    private void dsf(final StringBuilder sb, final char c, final int v) {
        if (v >= 0) {
            sb.append(c);
            sb.append(DELIMITER_PIPE);
            sb.append(v);
            sb.append(DELIMITER_PIPE);
        }
    }

    private void dsf(final StringBuilder sb, final char c, final long l) {
        sb.append(c);
        sb.append(DELIMITER_PIPE);
        sb.append(l);
        sb.append(DELIMITER_PIPE);
    }

    private void dsf(final StringBuilder sb, final int type) {
        dsf(sb, 't', type);
    }

    @Override
    public Date calculateRecurringDate(final long date, final long time, final int timeZoneOffsetDiff) {
        return new Date((date - (date % Constants.MILLI_DAY)) + time + timeZoneOffsetDiff);
    }

    /**
     * Checks if recurring information provided in specified calendar object is complete.<br>
     * Fields <b>Until</b> and <b>Occurrence</b> may be ignored since an infinite recurring appointment may omit this information.<br>
     * This is the dependency table as defined by {@link #createDSString(CalendarDataObject)}:
     * <p>
     * <table border="1">
     * <tr>
     * <th>Recurrence type</th>
     * <th>Interval</th>
     * <th>Until or Occurrence</th>
     * <th>Weekday</th>
     * <th>Monthday</th>
     * <th>Month</th>
     * </tr>
     * <tr>
     * <td align="center">DAILY<br>
     * &nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">WEEKLY<br>
     * &nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">MONTHLY 1<br>
     * (without weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">MONTHLY 2<br>
     * (with weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * <td align="center">&nbsp;</td>
     * </tr>
     * <tr>
     * <td align="center">YEARLY 1<br>
     * (without weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">&nbsp;</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * </tr>
     * <tr>
     * <td align="center">YEARLY 2<br>
     * (with weekday)</td>
     * <td align="center">x</td>
     * <td align="center">x (dependent on argument <code>ignoreUntilAndOccurrence</code>)</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * <td align="center">x</td>
     * </tr>
     * </table>
     *
     * @param cdao The calendar object to check
     * @param ignoreUntilAndOccurrence <code>true</code> to ignore whether until or occurrence is contained in specified calendar object;
     *            otherwise <code>false</code>
     * @throws OXException If check fails
     */
    @Override
    public void checkRecurringCompleteness(final CalendarObject cdao, final boolean ignoreUntilAndOccurrence) throws OXException {
        final int recType = cdao.getRecurrenceType();
        if (CalendarObject.NO_RECURRENCE == recType) {
            return;
        }
        if (!cdao.containsInterval()) {
            /*
             * Every recurrence type needs interval information
             */
            throw OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_INTERVAL.create();
        }
        if (CalendarObject.DAILY == recType) {
            /*
             * Interval and until or occurrence information is sufficient for daily
             */
            return;
        }
        if (CalendarObject.WEEKLY == recType) {
            if (!cdao.containsDays()) {
                /*
                 * Weekday needed for weekly recurrence
                 */
                throw OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_WEEKDAY.create();
            }
        } else if (CalendarObject.MONTHLY == recType) {
            if (!cdao.containsDayInMonth()) {
                /*
                 * Monthday needed for monthly recurrence
                 */
                throw OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create();
            }
            if (cdao.getDays() > 0 && !cdao.containsDays()) {
                /*
                 * Weekday needed for monthly2 recurrence
                 */
                throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY.create();
            }
        } else if (CalendarObject.YEARLY == recType) {
            if (!cdao.containsDayInMonth()) {
                /*
                 * Monthday needed for yearly recurrence
                 */
                throw OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create();
            }
            if (!cdao.containsMonth()) {
                /*
                 * Month needed for yearly recurrence
                 */
                throw OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTH.create();
            }
            if (cdao.getDays() > 0 && !cdao.containsDays()) {
                /*
                 * Weekday needed for yearly recurrence with weekdays greater than zero
                 */
                throw OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_WEEKDAY.create();
            }
        }
    }

    @Override
    public void checkRecurring(final CalendarObject cdao) throws OXException {
        if (cdao.getInterval() > MAX_OCCURRENCESE) {
            throw OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(cdao
                .getInterval()), Integer.valueOf(MAX_OCCURRENCESE));
        }
        if (cdao.getOccurrence() > MAX_OCCURRENCESE) {
            throw OXCalendarExceptionCodes.RECURRING_VALUE_CONSTRAINT.create(Integer.valueOf(cdao
                .getOccurrence()), Integer.valueOf(MAX_OCCURRENCESE));
        }
        if (cdao.getRecurrenceType() == CalendarObject.DAILY) {
            if (cdao.getInterval() < 1) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL.create(Integer.valueOf(cdao.getInterval()));
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.WEEKLY) {
            if (cdao.getInterval() < 1) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL.create(Integer.valueOf(cdao.getInterval()));
            }
            if (cdao.getDays() < 1) {
                throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_DAYS.create(Integer.valueOf(cdao.getDays()));
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.MONTHLY) {
            if (cdao.containsDays()) {
                if (cdao.getInterval() < 1) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL.create(Integer.valueOf(cdao.getInterval()));
                }
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 5) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_DAY_2.create(Integer.valueOf(cdao.getDayInMonth()));
                }
            } else {
                if (cdao.getInterval() < 1) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_OR_WRONG_VALUE_INTERVAL.create(Integer.valueOf(cdao.getInterval()));
                }
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 999) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_MONTLY_INTERVAL.create(Integer.valueOf(cdao.getDayInMonth()));
                }
            }
        } else if (cdao.getRecurrenceType() == CalendarObject.YEARLY) {
            if (cdao.containsDays()) {
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 5) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_TYPE.create(Integer.valueOf(cdao.getDayInMonth()));
                }
                if (!cdao.containsMonth() || cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_MONTH.create(Integer.valueOf(cdao.getMonth()));
                }
            } else {
                if (cdao.getDayInMonth() < 1 || cdao.getDayInMonth() > 32) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_INTERVAL.create(Integer.valueOf(cdao.getDayInMonth()));
                }
                if (!cdao.containsMonth() || cdao.getMonth() < 0 || cdao.getMonth() > 12) {
                    throw OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_MONTH.create(Integer.valueOf(cdao.getMonth()));
                }
            }
        }
    }

    /**
     * Creates a cloned version from given calendar object ready for being used to create the denoted change exception
     *
     * @param cdao The current calendar object denoting the change exception
     * @param edao The calendar object's storage version
     * @param ctx The context
     * @param session The session
     * @param inFolder The folder the action is performed in
     * @return A cloned version ready for being used to create the denoted change exception
     * @throws OXException If cloned version cannot be created
     */
    @Override
    public CalendarDataObject cloneObjectForRecurringException(final CalendarDataObject cdao, final CalendarDataObject edao, Context ctx, final Session session, int inFolder) throws OXException {
        final CalendarDataObject clone = edao.clone();
        // Recurrence exceptions MUST contain the position and date position.
        // This is necessary for further handling of the series.
        if (cdao.containsRecurrencePosition()) {
            clone.setRecurrencePosition(cdao.getRecurrencePosition());
        }
        if (cdao.containsRecurrenceDatePosition()) {
            clone.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
        }
        setRecurrencePositionOrDateInDAO(clone);
        /*
         * Check that change exception's date is contained in recurring
         * appointment's range
         */
        if (!checkIfDateOccursInRecurrence(clone.getRecurrenceDatePosition(), edao)) {
            throw OXCalendarExceptionCodes.FOREIGN_EXCEPTION_DATE.create();
        }
        {
            final Date[] newChangeExcs = addException(edao.getChangeException(), clone
                .getRecurrenceDatePosition());
            /*
             * Check that no other change exception exists on specified date;
             * meaning another user already created a change exception on this
             * date in the meantime
             */
            if (Arrays.equals(edao.getChangeException(), newChangeExcs)) {
                throw OXException.conflict();
            }
            cdao.setChangeExceptions(newChangeExcs);
        }
        fillObject(cdao, clone);
        clone.setCreatedBy(edao.getCreatedBy());
        if (!cdao.containsUserParticipants() && checkForReconfirmation(cdao, edao)) {
            /*
             * Turn cloned appointment's confirmation information to initial
             * status since obviously no confirmation information were set in
             * cdao that tells us what to do.
             */
            final UserParticipant[] users = clone.getUsers();
            for (final UserParticipant userParticipant : users) {
                if (userParticipant.getIdentifier() == session.getUserId()) {
                    userParticipant.setConfirm(CalendarObject.ACCEPT);
                } else {
                    userParticipant.setConfirm(CalendarObject.NONE);
                }
            }
        }
        clone.removeObjectID();
        clone.removeDeleteExceptions();
        clone.removeChangeExceptions();
        // We store the date_position in the exception field
        clone.setChangeExceptions(new java.util.Date[] { clone.getRecurrenceDatePosition() });
        // Calculate real times !!!!
        fillDAO(edao);
        final RecurringResultsInterface rss = calculateRecurring(edao, 0, 0, clone.getRecurrencePosition());
        if (rss == null) {
            throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_RECURRING_POSITION.create(clone.getRecurrencePosition());
        }
        final RecurringResultInterface rs = rss.getRecurringResult(0);
        if (!cdao.containsStartDate()) {
            clone.setStartDate(new Date(rs.getStart()));   
        }
        if (!cdao.containsEndDate()) {
            clone.setEndDate(new Date(rs.getEnd()));
        }

        ensureOriginFolder(edao, clone);

        // Reset Confirmation status on time change.
        CalendarDataObject originalTimeContainer = new CalendarDataObject();
        originalTimeContainer.setStartDate(new Date(rs.getStart()));
        originalTimeContainer.setEndDate(new Date(rs.getEnd()));
        CalendarDataObject newTimeContainer = new CalendarDataObject();
        newTimeContainer.setStartDate(clone.getStartDate());
        newTimeContainer.setEndDate(clone.getEndDate());
        if (detectTimeChange(newTimeContainer, originalTimeContainer)) {
            removeConfirmations(clone, session.getUserId());
            updateDefaultStatus(clone, ctx, session.getUserId(), inFolder);
        }

        return clone;
    }

    private void ensureOriginFolder(final CalendarDataObject edao, final CalendarDataObject clone) {
        int originFolder = 0;
        for (final UserParticipant userParticipant : edao.getUsers()) {
            if (userParticipant.getIdentifier() == edao.getCreatedBy()) {
                originFolder = userParticipant.getPersonalFolderId();
                break;
            }
        }
        if (originFolder != 0) {
            for (final UserParticipant userParticipant : clone.getUsers()) {
                if (userParticipant.getIdentifier() == edao.getCreatedBy()) {
                    userParticipant.setPersonalFolderId(originFolder);
                    break;
                }
            }
        }
    }

    /**
     * Checks if changes reflected through given {@code cdao} enforce a
     * re-confirmation for participants.
     *
     * @param cdao The calendar data object containing the changes to apply
     * @param edao The storage's calendar data object
     * @return <code>true</code> if changes reflected through given {@code cdao} enforce a re-confirmation for participants; otherwise
     *         <code>false</code>
     * @throws OXException If calculating occurrence's start/end date fails
     */
    private boolean checkForReconfirmation(final CalendarDataObject cdao, final CalendarDataObject edao)
        throws OXException {
        final long startDate;
        final long endDate;
        if ((cdao.getRecurrencePosition() > 0 || cdao.getRecurrenceDatePosition() != null)
            && (edao.getObjectID() == edao.getRecurrenceID())) {
            // Calculate occurrence's start/end in recurring appointment
            final RecurringResultInterface rs;
            if (cdao.getRecurrencePosition() > 0) {
                final RecurringResultsInterface rrs = calculateRecurringIgnoringExceptions(edao, 0,
                    0, cdao.getRecurrencePosition());
                rs = rrs.getRecurringResult(0);
            } else {
                final long normalized = normalizeLong(cdao.getStartDate().getTime());

                final RecurringResultsInterface rrs = calculateRecurringIgnoringExceptions(
                    edao,
                    normalized - Constants.MILLI_WEEK,
                    normalized + Constants.MILLI_WEEK,
                    0);
                final int pos = rrs.getPositionByLong(normalized);
                RecurringResultInterface tmp = rrs.getRecurringResult(pos - 1);
                if (null == tmp) {
                    tmp = rrs.getRecurringResult(0);
                }
                rs = tmp;
            }
            if (rs == null) {
                startDate = cdao.containsStartDate() ? cdao.getStartDate().getTime() : 0L;
                endDate = cdao.containsEndDate() ? cdao.getEndDate().getTime() : 0L;
            } else {
                startDate = rs.getStart();
                endDate = rs.getEnd();
            }
        } else {
            startDate = edao.getStartDate().getTime();
            endDate = edao.getEndDate().getTime();
        }

        if (cdao.containsStartDate()
            && check(Long.valueOf(cdao.getStartDate().getTime()), Long.valueOf(startDate))) {
            // Start date changed
            return true;
        }
        if (cdao.containsEndDate()
            && check(Long.valueOf(cdao.getEndDate().getTime()), Long.valueOf(endDate))) {
            // End date changed
            return true;
        }
        if (cdao.containsFullTime()
            && check(Boolean.valueOf(cdao.getFullTime()), Boolean.valueOf(edao
                .getFullTime()))) {
            // Full-time changed
            return true;
        }
        if (cdao.containsLocation() && check(cdao.getLocation(), edao.getLocation())) {
            // Location changed
            return true;
        }
        if (cdao.containsRecurrenceString()
            && check(cdao.getRecurrence(), edao.getRecurrence())) {
            // Recurring pattern/type changed
            return true;
        }
        return false;
    }

    /**
     * Replaces the start date and end date of specified recurring appointment
     * with the start date and end date of its first occurrence.
     * <p>
     * <b>Note</b> that neither <i>recurrence position</i> nor <i>recurrence
     * date position</i> is set.
     *
     * @param appointment The recurring appointment whose start date and end
     *            date shall be replaced
     * @throws OXException If calculating the first occurrence fails
     */
    @Override
    public void replaceDatesWithFirstOccurence(final Appointment appointment) throws OXException {
        final RecurringResultsInterface results = calculateFirstRecurring(appointment);
        if (0 == results.size()) {
            throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_FIRST_RECURRING.create(appointment.getObjectID());
        }
        final RecurringResultInterface result = results.getRecurringResult(0);
        appointment.setStartDate(new Date(result.getStart()));
        appointment.setEndDate(new Date(result.getEnd()));
    }

    /**
     * Sets the start/end date of specified recurring appointment to its first occurrence. A possible exception is swallowed and recurring
     * information is removed.
     *
     * @param cdao The recurring appointment whose start/end date shall be set to its first occurrence
     */
    @Override
    public void safelySetStartAndEndDateForRecurringAppointment(final CalendarDataObject cdao) {
        if (cdao.getRecurrenceType() != CalendarObject.NO_RECURRENCE) {
            try {
                final RecurringResultsInterface rrs = calculateRecurring(
                    cdao,
                    0,
                    0,
                    1,
                    MAX_OCCURRENCESE,
                    true);
                final RecurringResultInterface rr = rrs.getRecurringResultByPosition(1);
                if (rr != null) {
                    cdao.setStartDate(new Date(rr.getStart()));
                    cdao.setEndDate(new Date(rr.getEnd()));
                }
            } catch (final OXException x) {
                LOG.error("Can not load appointment ''{}'' with id {}:{} due to invalid recurrence pattern", cdao.getTitle(), cdao.getObjectID(), cdao.getContextID(), x);
                recoverForInvalidPattern(cdao);
            }
        }
    }

    // Copied from CalendarCommonCollection

    private static final AtomicInteger unique_session_int = new AtomicInteger();
    private static final String calendar_session_name = "CalendarSession";

    private static final Map<Integer, String> fieldMap = new HashMap<Integer, String>(24);

    private static volatile CalendarCache cache;

    private static CalendarCollection recColl = new CalendarCollection();

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getMaxUntilDate(com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public Date getMaxUntilDate(final CalendarDataObject cdao) {
        /*
         * Determine max. end date
         */
        long maxEnd;
        if (cdao.getRecurrenceType() == CalendarObject.YEARLY) {
            maxEnd = normalizeLong(addYears(cdao.getStartDate().getTime(), CalendarCollectionService.MAX_OCCURRENCESE));
            //maxEnd = normalizeLong(cdao.getStartDate().getTime() + (Constants.MILLI_YEAR * CalendarCollectionService.MAX_OCCURRENCESE));
        } else {
            maxEnd = normalizeLong(addYears(cdao.getStartDate().getTime(), recColl.getMAX_END_YEARS()));
            //maxEnd = normalizeLong(cdao.getStartDate().getTime() + (Constants.MILLI_YEAR * recColl.getMAX_END_YEARS()));
        }

        /*
         * Create a clone for calculation purpose
         */
        final CalendarDataObject clone = cdao.clone();
        final RecurringResultsInterface rresults;
        try {
            rresults = calculateRecurringIgnoringExceptions(clone, 0, 0, CalendarCollectionService.MAX_OCCURRENCESE);
        } catch (final OXException e) {
            LOG.error("", e);
            return new Date(maxEnd);
        }
        if (rresults == null) {
            return new Date(maxEnd);
        }
        final RecurringResultInterface rresult = rresults.getRecurringResult(0);
        if (rresult != null) {
            return new Date(normalizeLong(rresult.getEnd()));
        }
        return new Date(maxEnd);
    }

    @Override
    public long addYears(final long base, final int years) {
        final Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(base);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTimeInMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getFieldName(int)
     */
    @Override
    public String getFieldName(final int fieldId) {
        return fieldMap.get(Integer.valueOf(fieldId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getFieldNames(int[])
     */
    @Override
    public String[] getFieldNames(final int[] fieldIds) {
        if (null == fieldIds) {
            return null;
        }
        final String[] retval = new String[fieldIds.length];
        for (int i = 0; i < fieldIds.length; i++) {
            retval[i] = fieldMap.get(Integer.valueOf(fieldIds[i]));
        }
        return retval;
    }

    @Override
    public int getFieldId(final String fieldName) {
        if (null == fieldName) {
            return -1;
        }
        final int size = fieldMap.size();
        final Iterator<Map.Entry<Integer, String>> iter = fieldMap.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Map.Entry<Integer, String> e = iter.next();
            if (fieldName.equalsIgnoreCase(e.getValue())) {
                return e.getKey().intValue();
            }
        }
        return -1;
    }

    @Override
    public boolean checkPermissions(final CalendarDataObject cdao, final Session so, final Context ctx, final Connection readcon, final int action, final int inFolder) throws OXException {
        try {
            if (inFolder <= 0) {
                return false;
            }
            final OXFolderAccess access = new OXFolderAccess(readcon, cdao.getContext());
            cdao.setFolderType(access.getFolderType(inFolder, so.getUserId()));
            //cdao.setFolderType(OXFolderTools.getFolderType(inFolder, so.getUserObject().getId(), cdao.getContext(), readcon));

            if (action == CalendarOperation.READ) {
                if (cdao.getFolderType() != FolderObject.SHARED) {
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    if (oclp.canReadAllObjects()) {
                        return true;
                    } else if (oclp.canReadOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                } else {
                    cdao.setSharedFolderOwner(access.getFolderOwner(inFolder));

                    final EffectivePermission oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    if (oclp.canReadAllObjects() || oclp.canReadOwnObjects()) {
                        return true;
                    }
                }
            } else if (action == CalendarOperation.INSERT) {
                EffectivePermission oclp = null;
                oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                if (cdao.getFolderType() == FolderObject.SHARED) {
                    cdao.setSharedFolderOwner(access.getFolderOwner(inFolder));
                    //cdao.setSharedFolderOwner(OXFolderTools.getFolderOwner(inFolder, cdao.getContext(), readcon));
                }
                return oclp.canCreateObjects();
            } else if (action == CalendarOperation.UPDATE) {
                if (cdao.getFolderType() == FolderObject.SHARED) {
                    cdao.setSharedFolderOwner(access.getFolderOwner(inFolder));
                    //cdao.setSharedFolderOwner(OXFolderTools.getFolderOwner(inFolder, cdao.getContext(), readcon));
                    if (cdao.getPrivateFlag() && !isParticipant(cdao, so.getUserId())) {
                        return false;
                    }
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canWriteAllObjects()) {
                        return true;
                    } else if (oclp.canWriteOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                } else {
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canWriteAllObjects()) {
                        return true;
                    } else if (oclp.canWriteOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                }
            } else if (action == CalendarOperation.DELETE) {
                if (cdao.getFolderType() == FolderObject.SHARED) {
                    cdao.setSharedFolderOwner(access.getFolderOwner(inFolder));
                    //cdao.setSharedFolderOwner(OXFolderTools.getFolderOwner(inFolder, cdao.getContext(), readcon));
                    if (cdao.getPrivateFlag() && !isParticipant(cdao, so.getUserId())) {
                        return false;
                    }
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canDeleteAllObjects()) {
                        return true;
                    } else if (oclp.canDeleteOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                } else {
                    EffectivePermission oclp = null;
                    oclp = access.getFolderPermission(inFolder, so.getUserId(), UserConfigurationStorage.getInstance().getUserConfigurationSafe(so.getUserId(), ctx));
                    //oclp = OXFolderTools.getEffectiveFolderOCL(inFolder, so.getUserObject().getId(), so.getUserObject().getGroups(), so.getContext(), so.getUserConfiguration());
                    if (oclp.canDeleteAllObjects()) {
                        return true;
                    } else if (oclp.canDeleteOwnObjects()) {
                        if (cdao.getCreatedBy() == so.getUserId()) {
                            return true;
                        }
                    }
                }
            }
        } catch (final OXException e) {
            LOG.warn("ERROR getting read permissions.", e);
            return false;
        } catch (final RuntimeException e) {
            LOG.error("ERROR getting read permissions", e);
            return false;
        }
        return false;
    }

    private boolean isParticipant(CalendarDataObject cdao, int id) {
        for (Participant participant : cdao.getParticipants()) {
            if (participant.getIdentifier() == id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getReadPermission(final int oid, final int fid, final Session so, final Context ctx)
        throws OXException {
        try {
            final OXFolderAccess access = new OXFolderAccess(ctx);
            final int type = access.getFolderType(fid, so.getUserId());
            // int type = OXFolderTools.getFolderType(fid,
            // so.getUserObject().getId(), so.getContext());
            if (type != FolderObject.SHARED) {
                EffectivePermission oclp = null;
                oclp = access.getFolderPermission(fid, so.getUserId(), UserConfigurationStorage.getInstance()
                    .getUserConfigurationSafe(so.getUserId(), ctx));
                // oclp = OXFolderTools.getEffectiveFolderOCL(fid,
                // so.getUserObject().getId(), so.getUserObject().getGroups(),
                // so.getContext(), so.getUserConfiguration());
                if (oclp.canReadAllObjects()) {
                    return true;
                }
                return loadObjectAndCheckPermisions(oid, fid, so, ctx, CalendarOperation.READ);
            }
            return loadObjectAndCheckPermisions(oid, fid, so, ctx, CalendarOperation.READ);
        } catch (final OXException e) {
            throw e;
        } catch (final SQLException ex) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(ex);
        }
    }

    @Override
    public boolean getWritePermission(final int oid, final int fid, final Session so, final Context ctx)
        throws OXException {
        try {
            final OXFolderAccess access = new OXFolderAccess(ctx);
            final int type = access.getFolderType(fid, so.getUserId());
            // int type = OXFolderTools.getFolderType(fid,
            // so.getUserObject().getId(), so.getContext());
            if (type != FolderObject.SHARED) {
                EffectivePermission oclp = null;
                oclp = access.getFolderPermission(fid, so.getUserId(), UserConfigurationStorage.getInstance()
                    .getUserConfigurationSafe(so.getUserId(), ctx));
                // oclp = OXFolderTools.getEffectiveFolderOCL(fid,
                // so.getUserObject().getId(), so.getUserObject().getGroups(),
                // so.getContext(), so.getUserConfiguration());
                if (oclp.canWriteAllObjects()) {
                    return true;
                }
                return loadObjectAndCheckPermisions(oid, fid, so, ctx, CalendarOperation.UPDATE);
            }
            return loadObjectAndCheckPermisions(oid, fid, so, ctx, CalendarOperation.UPDATE);
        } catch (final OXException e) {
            throw e;
        } catch (final SQLException ex) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(ex);
        }
    }

    private boolean loadObjectAndCheckPermisions(final int oid, final int fid, final Session so, final Context ctx, final int type) throws OXException, SQLException {
        Connection readcon = null;
        try {
            readcon = DBPool.pickup(ctx);
            final CalendarSql csql = new CalendarSql(so);
            final CalendarDataObject cdao = csql.getObjectById(oid, fid);
            return checkPermissions(cdao, so, ctx, readcon, type, fid);
        } catch (final OXException x) {
            if (x.isGeneric(Generic.NO_PERMISSION)) {
                return false; // Thrown when the user has no READ access.
            } else {
                throw x;
            }
        } finally {
            if (readcon != null) {
                DBPool.push(ctx, readcon);
            }
        }
    }

    @Override
    public final boolean checkIfUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up) {
        final UserParticipant check[] = cdao.getUsers();
        if (check != null && check.length > 0) {
            Arrays.sort(check);
            final int x = Arrays.binarySearch(check, up);
            if (x >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkAndFillIfUserIsParticipant(final CalendarDataObject cdao, final UserParticipant up) {
        if (cdao.getParticipants() != null) {
            for (final Participant p : cdao.getParticipants()) {
                if (p.getType() == Participant.USER && p.getIdentifier() == up.getIdentifier()) {
                    final UserParticipant userFromParticipant = (UserParticipant) p;
                    if (userFromParticipant.containsConfirm()) {
                        up.setConfirm(userFromParticipant.getConfirm());
                    }
                    if (userFromParticipant.containsConfirmMessage()) {
                        up.setConfirmMessage(userFromParticipant.getConfirmMessage());
                    }
                }
            }
        }
        final UserParticipant check[] = cdao.getUsers();
        if (check != null && check.length > 0) {
            Arrays.sort(check);
            final int x = Arrays.binarySearch(check, up);
            if (x < 0) {
                final UserParticipant newup[] = new UserParticipant[check.length + 1];
                System.arraycopy(check, 0, newup, 0, check.length);
                newup[check.length] = up;
                cdao.setUsers(newup);
            } else if (!cdao.containsObjectID() && !check[x].containsConfirm() && check[x].getConfirm() == CalendarObject.NONE) {
                check[x].setConfirm(CalendarObject.ACCEPT);
            }
        } else {
            final UserParticipant newup[] = new UserParticipant[1];
            newup[0] = up;
            cdao.setUsers(newup);
        }
    }

    @Override
    public void checkAndConfirmIfUserUserIsParticipantInPublicFolder(final CalendarDataObject cdao, final UserParticipant up) {
        final Participant[] participants = cdao.getParticipants();
        boolean isInParticipants = false;
        if (participants != null) {
            for (final Participant participant : participants) {
                if (participant.getIdentifier() == up.getIdentifier()) {
                    isInParticipants = true;
                    break;
                }
            }
        }

        final UserParticipant check[] = cdao.getUsers();
        if (check == null || check.length == 0) {
            if (isInParticipants) {
                cdao.setUsers(new UserParticipant[] { up });
            }
            return;
        }

        for (final UserParticipant user : check) {
            if (user.getIdentifier() == up.getIdentifier()) {
                if (!user.containsConfirm()) {
                    user.setConfirm(CalendarObject.ACCEPT);
                    cdao.setUsers(check);
                }
            }
        }
    }

    @Override
    public void removeConfirmations(CalendarDataObject cdao, int uid) {
        if (cdao.getUsers() == null) {
            return;
        }
        for (UserParticipant up : cdao.getUsers()) {
            if (up.getIdentifier() != uid) {
                up.removeConfirm();
                up.removeConfirmMessage();
            }
        }
    }

    @Override
    public void updateDefaultStatus(final CalendarDataObject cdao, final Context ctx, final int uid, final int inFolder) throws OXException {
        if (cdao.getUsers() == null) {
            return;
        }
        for (final UserParticipant user : cdao.getUsers()) {
            if (user.getIdentifier() == uid) {
                if (!user.containsConfirm()) {
                    user.setConfirm(ServerUserSetting.getInstance().getDefaultStatusPublic(ctx.getContextId(), user.getIdentifier()));
                }
                continue;
            }
            if (user.containsConfirm()) {
                continue;
            }
            {
                switch (cdao.getFolderType()) {
                    case FolderObject.SHARED:
                        final int folderOwner = new OXFolderAccess(ctx).getFolderOwner(inFolder);
                        if (user.getIdentifier() == folderOwner) {
                            continue;
                        } else {
                            user.setConfirm(ServerUserSetting.getInstance().getDefaultStatusPrivate(ctx.getContextId(), user.getIdentifier()));
                        }
                        break;
                    case FolderObject.PRIVATE:
                        user.setConfirm(ServerUserSetting.getInstance().getDefaultStatusPrivate(ctx.getContextId(), user.getIdentifier()));
                        break;
                    case FolderObject.PUBLIC:
                        user.setConfirm(ServerUserSetting.getInstance().getDefaultStatusPublic(ctx.getContextId(), user.getIdentifier()));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public Set<UserParticipant> checkAndModifyAlarm(final CalendarDataObject cdao, Set<UserParticipant> check, final int uid, final Set<UserParticipant> orig) {
        if (cdao.containsAlarm()) {
            final UserParticipant up = new UserParticipant(uid);
            if (check == null) {
                check = new HashSet<UserParticipant>();
                check.add(up);
            }

            UserParticipant current = null;
            for (UserParticipant c : check) {
                if (c.getIdentifier() == uid) {
                    current = c;
                }
            }
            UserParticipant old = null;
            for (UserParticipant o : orig) {
                if (o.getIdentifier() == uid) {
                    old = o;
                }
            }
            UserParticipant owner = null;
            if (cdao.getFolderType() == FolderObject.SHARED) {
                for (UserParticipant c : check) {
                    if (c.getIdentifier() == cdao.getSharedFolderOwner()) {
                        owner = c;
                    }
                }
            }

            if (current != null) {
                if (cdao.getFolderType() == FolderObject.SHARED && owner != null) {
                    owner.setAlarmMinutes(cdao.getAlarm());
                    owner.setIsModified(true);
                } else if (cdao.getFolderType() != FolderObject.SHARED) {
                    current.setAlarmMinutes(cdao.getAlarm());
                    current.setIsModified(true);
                }
                if (old != null) {
                    if (!current.containsConfirm()) {
                        current.setConfirm(old.getConfirm());
                    }
                    if (!current.containsConfirmMessage()) {
                        current.setConfirmMessage(old.getConfirmMessage());
                    }
                    current.setPersonalFolderId(old.getPersonalFolderId());
                }

                return check;
            }
        }
        return check;
    }

    @Override
    public void simpleParticipantCheck(final CalendarDataObject cdao) throws OXException {
        // TODO: Maybe we have to enhance this simple check
        final Participant check[] = cdao.getParticipants();
        if (check != null && check.length > 0) {
            for (int a = 0; a < check.length; a++) {
                if (check[a].getType() == Participant.EXTERNAL_USER) {
                    if (check[a].getIdentifier() != 0) {
                        check[a].setIdentifier(0); // auto correction ! should not happen !
                    }
                    if (check[a].getEmailAddress() == null) {
                        throw OXCalendarExceptionCodes.EXTERNAL_PARTICIPANTS_MANDATORY_FIELD.create();
                    }
                }
            }
        }
    }

    @Override
    public void checkAndFillIfUserIsUser(final CalendarDataObject cdao, final Participant p) throws OXException {
        final Participant check[] = cdao.getParticipants();
        if (check != null && check.length > 0) {
            if (!containsParticipant(check, p, cdao.getContext())) {
                final Participant newp[] = new Participant[check.length + 1];
                System.arraycopy(check, 0, newp, 0, check.length);
                newp[check.length] = p;
                cdao.setParticipants(newp);
            }
        } else {
            final Participant newp[] = new Participant[1];
            newp[0] = p;
            cdao.setParticipants(newp);
        }
    }

    private boolean containsParticipant(final Participant[] participants, final Participant p, final Context ctx) throws OXException {
        for (final Participant part : participants) {
            if (part.getType() == p.getType()) {
                if (part.getIdentifier() == p.getIdentifier()) {
                    return true;
                }
            } else {
                if (part.getType() == Participant.GROUP) {
                    final GroupStorage groups = GroupStorage.getInstance();
                    final Group group = groups.getGroup(part.getIdentifier(), ctx);
                    final int[] member = group.getMember();
                    for (final int memberId : member) {
                        if (memberId == p.getIdentifier()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void removeUserParticipant(final CalendarDataObject cdao, final int uid) throws OXException {
        final UserParticipant check[] = cdao.getUsers();
        if (check != null && check.length > 0) {
            final UserParticipant ret[] = new UserParticipant[check.length - 1];
            int x = 0;
            for (int a = 0; a < check.length; a++) {
                if (check[a].getIdentifier() != uid) {
                    if (x < ret.length) {
                        ret[x++] = check[a];
                    } else {
                        throw OXCalendarExceptionCodes.UNABLE_TO_REMOVE_PARTICIPANT.create(uid);
                    }
                }
            }
            cdao.setUsers(ret);
        } else {
            throw OXCalendarExceptionCodes.UNABLE_TO_REMOVE_PARTICIPANT_2.create(2);
        }
    }

    @Override
    public void removeParticipant(final CalendarDataObject cdao, final int uid) throws OXException {
        final Participant check[] = cdao.getParticipants();
        if (check != null && check.length > 0) {
            final Participant ret[] = new Participant[check.length - 1];
            int x = 0;
            for (int a = 0; a < check.length; a++) {
                if (check[a].getIdentifier() != uid) {
                    if (x < ret.length) {
                        ret[x++] = check[a];
                    } else {
                        throw OXCalendarExceptionCodes.UNABLE_TO_REMOVE_PARTICIPANT.create(uid);
                    }
                }
            }
            cdao.setParticipants(ret);
        } else {
            throw OXCalendarExceptionCodes.UNABLE_TO_REMOVE_PARTICIPANT_2.create(2);
        }
    }

    @Override
    public Date getNextReminderDate(final int oid, final int fid, final Session so) throws OXException, SQLException {
        return getNextReminderDate(oid, fid, so, 0L);
    }

    @Override
    public Date getNextReminderDate(final int oid, final int fid, final Session so, final long last) throws OXException, SQLException {
        final CalendarSql csql = new CalendarSql(so);
        final CalendarDataObject cdao = csql.getObjectById(oid, fid);
        final int alarm = cdao.getAlarm();
        long start = System.currentTimeMillis();
        if (last > 0) {
            start = last;
            start = ((start / Constants.MILLI_DAY) * Constants.MILLI_DAY);
            start += Constants.MILLI_DAY;
        } else {
            start = ((start / Constants.MILLI_DAY) * Constants.MILLI_DAY);
        }
        final long end = (start + (Constants.MILLI_YEAR * 10L));
        final RecurringResultsInterface rss = calculateRecurring(cdao, start, end, 0, 1, false);
        if (rss != null && rss.size() >= 1) {
            final RecurringResultInterface rs = rss.getRecurringResult(0);
            return new Date(rs.getStart() - (alarm * 60 * 1000L));
        }
        return null;
    }

    @Override
    public boolean existsReminder(final Context c, final int oid, final int uid) {
        final ReminderService rsql = new ReminderHandler(c);
        try {
            return rsql.existsReminder(oid, uid, Types.APPOINTMENT);
        } catch (final OXException ex) {
            LOG.error("", ex);
        }
        return false;
    }

    @Override
    public void debugActiveDates(final long start, final long end, final boolean activeDates[]) {
        System.out.println("\n\nRange : " + new Date(start) + "  -  " + new Date(end));
        int a = 1;
        long s = start;
        for (; s < end; s += Constants.MILLI_DAY) {
            if (a <= activeDates.length) {
                System.out.print(activeDates[a - 1]);
                System.out.print(' ');
                if (a % 7 == 0) {
                    System.out.println("");
                }
            } else {
                System.out.println("a == " + a + " activeDates == " + activeDates.length);
            }
            a++;
        }
        System.out.println("\n\n\n");
    }

    @Override
    public void debugRecurringResult(final RecurringResultInterface rr) {
        LOG.debug("{} : {} {}", Integer.toString(rr.getPosition()), StringCollection.date2String(new Date(rr.getStart())), StringCollection.date2String(new Date(rr.getEnd())));
    }

    @Override
    public String getUniqueCalendarSessionName() {
        return calendar_session_name + unique_session_int.incrementAndGet();
    }

    @Override
    public int[] checkAndAlterCols(int cols[]) {
        if (null == cols) {
            return cols;
        }
        final int[] sorted = new int[cols.length];
        System.arraycopy(cols, 0, sorted, 0, cols.length);
        Arrays.sort(sorted);
        int c = 0;
        final int ara[] = new int[3];
        if (Arrays.binarySearch(sorted, CalendarObject.RECURRENCE_TYPE) >= 0) {
            if (Arrays.binarySearch(sorted, CalendarObject.CHANGE_EXCEPTIONS) < 0) {
                ara[c++] = CalendarObject.CHANGE_EXCEPTIONS;
            }
            if (Arrays.binarySearch(sorted, CalendarObject.DELETE_EXCEPTIONS) < 0) {
                ara[c++] = CalendarObject.DELETE_EXCEPTIONS;
            }
            if (Arrays.binarySearch(sorted, CalendarObject.RECURRENCE_CALCULATOR) < 0) {
                ara[c++] = CalendarObject.RECURRENCE_CALCULATOR;
            }
            cols = enhanceCols(cols, ara, c);
        }

        return cols;
    }

    /**
     * An array holding the column IDs of attributes that are preserved when storing a 'tombstone' representing a deleted appointment.
     * This includes properties to identify the deleted appointment, as well as all other mandatory fields.
     */
    private final int[] TOMBSTONE_COLUMS = {
        CalendarObject.CREATION_DATE, CalendarObject.CREATED_BY, CalendarObject.LAST_MODIFIED, CalendarObject.MODIFIED_BY,
        CalendarObject.FOLDER_ID, CalendarObject.PRIVATE_FLAG, CalendarObject.OBJECT_ID, CalendarObject.RECURRENCE_ID,
        CalendarObject.UID, CalendarObject.FILENAME
    };

    @Override
    public int[] checkAndAlterColsForDeleted(int cols[]) {
        if (null == cols) {
            return cols;
        }
        int idx = 0;
        int[] alteredColumns = new int[cols.length];
        for (int col : cols) {
            if (com.openexchange.tools.arrays.Arrays.contains(TOMBSTONE_COLUMS, col)) {
                alteredColumns[idx++] = col;
            }
        }
        if (idx < alteredColumns.length) {
            int[] trimmedToLength = new int[idx];
            System.arraycopy(alteredColumns, 0, trimmedToLength, 0, idx);
            return trimmedToLength;
        }
        return alteredColumns;
    }

    @Override
    public int[] enhanceCols(final int cols[], final int ara[], final int i) {
        final int ncols[] = new int[cols.length + i];
        System.arraycopy(cols, 0, ncols, 0, cols.length);
        System.arraycopy(ara, 0, ncols, cols.length, i);
        return ncols;
    }

    @Override
    public void triggerEvent(final Session session, final int action, final Appointment appointmentobject) throws OXException {
        final EventClient eventclient = new EventClient(session);
        switch (action) {
            case CalendarOperation.INSERT:
                try {
                    eventclient.create(appointmentobject); // TODO
                } catch (final RuntimeException e) {
                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(16));
                }
                break;
            case CalendarOperation.UPDATE:
                try {
                    eventclient.modify(appointmentobject); // TODO
                } catch (final RuntimeException e) {
                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(17));
                }
                break;
            case CalendarOperation.DELETE:
                try {
                    eventclient.delete(appointmentobject); // TODO
                } catch (final RuntimeException e) {
                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(18));
                }
                break;
            case CalendarOperation.CONFIRM_ACCEPTED:
                try {
                    eventclient.accepted(appointmentobject); // TODO
                } catch (final RuntimeException e) {
                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(18));
                }
                break;
            case CalendarOperation.CONFIRM_DELINED:
                try {
                    eventclient.declined(appointmentobject); // TODO
                } catch (final RuntimeException e) {
                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(18));
                }
                break;
            case CalendarOperation.CONFIRM_TENTATIVELY_ACCEPTED:
                try {
                    eventclient.tentative(appointmentobject); // TODO
                } catch (final RuntimeException e) {
                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(18));
                }
                break;
            case CalendarOperation.CONFIRM_WAITING:
                try {
                    eventclient.waiting(appointmentobject); // TODO
                } catch (final RuntimeException e) {
                    throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, Integer.valueOf(18));
                }
                break;
            default:
                throw OXCalendarExceptionCodes.UNSUPPORTED_ACTION_TYPE.create(Integer.valueOf(action));
        }
    }

    @Override
    public void triggerModificationEvent(final Session session, final CalendarDataObject oldAppointment,
        final CalendarDataObject newAppointment) throws OXException {
        final EventClient eventclient = new EventClient(session);
        {
            int folderId = oldAppointment.getEffectiveFolderId();
            if (folderId == 0) {
                final OXFolderAccess folderAccess = new OXFolderAccess(oldAppointment.getContext());
                folderId = folderAccess.getDefaultFolderID(session.getUserId(), FolderObject.CALENDAR);
            }
            final FolderObject sourceFolder = getFolder(session, folderId);
            eventclient.modify(oldAppointment, newAppointment, sourceFolder); // TODO
        }

    }

    private FolderObject getFolder(final Session session, final int fid) throws OXException, OXException {
        final Context ctx = ContextStorage.getStorageContext(session);

        if (FolderCacheManager.isEnabled()) {
            return FolderCacheManager.getInstance().getFolderObject(fid, true, ctx, null);
        }
        return FolderObject.loadFolderObjectFromDB(fid, ctx, null);
    }

    @Override
    public String getSQLInStringForParticipants(final List<UserParticipant> userParticipant) {
        final StringBuilder sb = new StringBuilder(32);
        if (userParticipant != null && userParticipant.size() > 0) {
            sb.append('(');
            for (int a = 0; a < userParticipant.size(); a++) {
                if (a > 0) {
                    sb.append(',');
                    sb.append(userParticipant.get(a).getIdentifier());
                } else {
                    sb.append(userParticipant.get(a).getIdentifier());
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String getSQLInStringForParticipants(final Participant[] participant) {
        final StringBuilder sb = new StringBuilder(32);
        if (participant != null && participant.length > 0) {
            sb.append('(');
            for (int a = 0; a < participant.length; a++) {
                if (a > 0) {
                    sb.append(',');
                    sb.append(participant[a].getIdentifier());
                } else {
                    sb.append(participant[a].getIdentifier());
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String getSQLInStringForResources(final Participant[] participant) {
        final StringBuilder sb = new StringBuilder(32);
        boolean containsResources = false;
        if (participant != null && participant.length > 0) {
            for (int a = 0; a < participant.length; a++) {
                if (participant[a].getType() == Participant.RESOURCE) {
                    containsResources = true;
                    break;
                }
            }
        }
        if (containsResources) {
            int x = 0;
            sb.append('(');
            for (int a = 0; a < participant.length; a++) {
                if (participant[a].getType() == Participant.RESOURCE) {
                    if (x > 0) {
                        sb.append(',');
                        sb.append(participant[a].getIdentifier());
                    } else {
                        sb.append(participant[a].getIdentifier());
                        x = 1;
                    }
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public boolean inBetween(final long check_start, final long check_end, final long range_start,
        final long range_end) {
        return (check_start < range_end) && (check_end > range_start);

        /*
         * if (check_start <= range_start && check_start >= range_start) {
         * return true;
         * } else if (check_start >= range_start && check_end <= range_end) {
         * return true;
         * } else if (check_start > range_start && check_end > range_end && check_start < range_end) {
         * return true;
         * } else if (check_start < range_start && check_end > range_start && check_start < range_end) {
         * return true;
         * }
         * return false;
         */
    }

    @Override
    public Date[] convertString2Dates(final String s) {
        if (s == null) {
            return null;
        } else if (s.length() == 0) {
            return new Date[0];
        }
        final String[] sa = Strings.splitByComma(s);
        final Date dates[] = new Date[sa.length];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = new Date(Long.parseLong(sa[i]));
        }
        return dates;
    }

    @Override
    public String convertDates2String(final Date[] d) {
        if (d == null || d.length == 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(d.length << 4);
        Arrays.sort(d);
        sb.append(d[0].getTime());
        for (int i = 1; i < d.length; i++) {
            sb.append(',').append(d[i].getTime());
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#check(java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean check(final Object a, final Object b) {
        if (a == b) {
            return false;
        }
        if (a != null && a.equals(b)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkParticipants(com.openexchange.groupware.container.Participant[], com.openexchange.groupware.container.Participant[])
     */
    @Override
    public boolean checkParticipants(final Participant[] newParticipants, final Participant[] oldParticipants) {
        if (newParticipants == oldParticipants) {
            return false;
        }
        if (newParticipants == null) {
            return true;
        }
        if (oldParticipants == null) {
            return true;
        }
        if (newParticipants.length != oldParticipants.length) {
            return true;
        }
        for (final Participant newP : newParticipants) {
            boolean found = false;
            for (final Participant oldP : oldParticipants) {
                if (newP.getIdentifier() == oldP.getIdentifier()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CalendarFolderObject getVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, SearchIteratorException, OXException {
        return _getVisibleAndReadableFolderObject(uid, groups, c, uc, readcon, false);
    }

    @Override
    public CalendarFolderObject getAllVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc) throws SQLException, SearchIteratorException, OXException {
        return _getVisibleAndReadableFolderObject(uid, groups, c, uc, null, true);
    }

    @Override
    public CalendarFolderObject getAllVisibleAndReadableFolderObject(final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection con) throws SQLException, SearchIteratorException, OXException {
        return _getVisibleAndReadableFolderObject(uid, groups, c, uc, con, true);
    }

    private CalendarFolderObject _getVisibleAndReadableFolderObject(final int uid, final int groups[],
        final Context c, final UserConfiguration uc, final Connection readcon, final boolean fillShared) throws SQLException,
        SearchIteratorException, OXException {
        final CalendarFolderObject check = new CalendarFolderObject(uid, c.getContextId(), fillShared);
        CalendarCache cache = CalendarCollection.cache;
        if (cache == null) {
            cache = CalendarCache.getInstance();
            CalendarCollection.cache = cache;
        }

        final Object o = cache.get(check.getObjectKey(), check.getGroupKey());

        final CalendarFolderObject cfo;
        if (o == null) {
            final Queue<FolderObject> queue = ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(uid,
                groups, uc.getAccessibleModules(), FolderObject.CALENDAR, c, readcon)).asQueue();
            cfo = new CalendarFolderObject(uid, c.getContextId(), fillShared);
            for (final FolderObject fo : queue) {
                final EffectivePermission oclp = fo.getEffectiveUserPermission(uid, uc);
                cfo.addFolder(oclp.canReadAllObjects(), oclp.canReadOwnObjects(), fo.isShared(uid), fo
                    .getObjectID(), fo.getType());
            }
            try {
                cache.add(cfo.getObjectKey(), cfo.getGroupKey(), cfo);
            } catch (final com.openexchange.exception.OXException ex) {
                LOG.error("", ex);
            }
        } else {
            cfo = (CalendarFolderObject) o;
        }
        return cfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getVisibleFolderSQLInString(java.lang.StringBuilder, int, int[], com.openexchange.groupware.contexts.Context, com.openexchange.groupware.userconfiguration.UserConfiguration,
     * java.sql.Connection)
     */
    @Override
    public void getVisibleFolderSQLInString(final StringBuilder sb, final int uid, final int groups[], final Context c, final UserConfiguration uc, final Connection readcon) throws SQLException, OXException, OXException {
        CalendarFolderObject cfo = null;
        try {
            cfo = getVisibleAndReadableFolderObject(uid, groups, c, uc, readcon);
        } catch (final SearchIteratorException sie) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(sie, Integer.valueOf(1));
        }
        if (cfo == null) {
            throw OXCalendarExceptionCodes.CFO_NOT_INITIALIZIED.create();
        }
        final TIntSet private_read_all = cfo.getPrivateReadableAll();
        final TIntSet private_read_own = cfo.getPrivateReadableOwn();
        final TIntSet public_read_all = cfo.getPublicReadableAll();
        final TIntSet public_read_own = cfo.getPublicReadableOwn();

        boolean private_query = false;
        boolean public_query = false;
        int brack = 0;
        if (!private_read_all.isEmpty()) {
            sb.append(" AND (pdm.pfid IN ");
            brack++;
            sb.append(getSqlInString(private_read_all));
            private_query = true;
        }

        if (!private_read_own.isEmpty()) {
            if (private_query) {
                sb.append("OR (pd.created_from = ");
            } else {
                sb.append(" AND (pd.created_from = ");
            }
            sb.append(Integer.toString(uid));
            sb.append(" AND (pdm.pfid IN ");
            sb.append(getSqlInString(private_read_own));
            sb.append("))");
            private_query = true;
        }

        if (!public_read_all.isEmpty()) {
            if (private_query) {
                sb.append(" OR pd.fid IN ");
                sb.append(getSqlInString(public_read_all));
                public_query = true;
            } else {
                sb.append(" AND pd.fid IN ");
                sb.append(getSqlInString(public_read_all));
                public_query = true;
            }
        }

        if (!public_read_own.isEmpty()) {
            if (private_query || public_query) {
                sb.append(" OR (pd.fid IN ");
                sb.append(getSqlInString(public_read_own));
                sb.append(" AND (pd.created_from = ");
                sb.append(uid);
                sb.append("))");
            } else {
                sb.append(" AND (pd.fid IN ");
                sb.append(getSqlInString(public_read_own));
                sb.append(" AND (pd.created_from = ");
                sb.append(uid);
                sb.append("))");
            }
        }
        for (int a = 0; a < brack; a++) {
            sb.append(')');
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#removeException(java.util.Date[], java.util.Date)
     */
    @Override
    public Date[] removeException(final Date[] dates, final Date d) {
        return removeException(dates, d.getTime());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#removeException(java.util.Date[], long)
     */
    @Override
    public Date[] removeException(final Date[] dates, final long dateTime) {
        if (dates != null && dates.length > 0) {
            final Date ret[] = new Date[dates.length - 1];
            int x = 0;
            for (int a = 0; a < dates.length; a++) {
                if (dates[a].getTime() != dateTime) {
                    if (x < ret.length) {
                        ret[x++] = dates[a];
                    }
                }
            }
            if (x > 0) {
                return ret;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#addException(java.util.Date[], java.util.Date)
     */
    @Override
    public Date[] addException(final Date[] dates, final Date d) {
        if (dates != null && dates.length > 0) {
            for (int i = 0; i < dates.length; i++) {
                if (dates[i].equals(d)) {
                    return dates;
                }
            }
            final Date ret[] = new Date[dates.length + 1];
            System.arraycopy(dates, 0, ret, 1, dates.length);
            ret[0] = d;
            Arrays.sort(ret);
            return ret;
        }
        return new Date[] { d };
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#fillObject(com.openexchange.calendar.CalendarDataObject, com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public CalendarDataObject fillObject(final CalendarDataObject source, final CalendarDataObject destination) {
        if (source.containsTitle()) {
            destination.setTitle(source.getTitle());
        }
        if (source.containsLocation()) {
            destination.setLocation(source.getLocation());
        }
        if (source.containsShownAs()) {
            destination.setShownAs(source.getShownAs());
        }
        if (source.containsCategories()) {
            destination.setCategories(source.getCategories());
        }
        if (source.containsStartDate()) {
            destination.setStartDate(source.getStartDate());
        }
        if (source.containsEndDate()) {
            destination.setEndDate(source.getEndDate());
        }
        if (source.containsRecurrencePosition()) {
            destination.setRecurrencePosition(source.getRecurrencePosition());
        }
        if (source.containsFullTime()) {
            destination.setFullTime(source.getFullTime());
        }
        if (source.containsLabel()) {
            destination.setLabel(source.getLabel());
        }
        if (source.containsNote()) {
            destination.setNote(source.getNote());
        }
        //        if (source.containsParticipants()) {
        //            destination.setParticipants(source.getParticipants());
        //        }
        //        if (source.containsUserParticipants()) {
        //            destination.setUsers(source.getUsers());
        //        }
        if (source.containsParticipants()) {
            try {
                List<Participant> participants = new ArrayList<Participant>();
                for (Participant participant : source.getParticipants()) {
                    participants.add(participant.getClone());
                }
                destination.setParticipants(participants);
            } catch (CloneNotSupportedException e) {
                destination.setParticipants(source.getParticipants());
            }
        }
        if (source.containsUserParticipants()) {
            try {
                List<UserParticipant> users = new ArrayList<UserParticipant>();
                for (UserParticipant user : source.getUsers()) {
                    users.add(user.getClone());
                }
                destination.setUsers(users);
            } catch (CloneNotSupportedException e) {
                destination.setUsers(source.getUsers());
            }
        }
        if (source.containsPrivateFlag()) {
            destination.setPrivateFlag(source.getPrivateFlag());
        }
        return destination;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#removeFieldsFromObject(com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public void removeFieldsFromObject(final CalendarDataObject cdao) {
        cdao.removeTitle();
        cdao.removeLocation();
        cdao.removeShownAs();
        cdao.removeCategories();
        cdao.removeStartDate();
        cdao.removeEndDate();
        cdao.removeNote();
        cdao.removeFullTime();
        cdao.removeLabel();
        cdao.removePrivateFlag();
        cdao.removeUsers();
        cdao.removeParticipants();

        cdao.removeRecurrencePosition();
        cdao.removeRecurrenceDatePosition();
        cdao.removeRecurrenceType();
        cdao.removeRecurrenceCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#purgeExceptionFieldsFromObject(com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public void purgeExceptionFieldsFromObject(final CalendarDataObject cdao) {
        cdao.setRecurrenceID(0);
        cdao.setRecurrencePosition(0);
        cdao.setRecurrence(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#isInThePast(java.util.Date)
     */
    @Override
    public boolean isInThePast(final java.util.Date check) {
        return checkMillisInThePast(check.getTime());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkMillisInThePast(long)
     */
    @Override
    public boolean checkMillisInThePast(final long check) {
        return check < (normalizeLong(System.currentTimeMillis()));
    }

    CalendarDataObject[] copyAndExpandCalendarDataObjectArray(final CalendarDataObject source[], final CalendarDataObject dest[]) {
        if (source != null && dest != null && source.length > 0) {
            final CalendarDataObject ret[] = new CalendarDataObject[dest.length + source.length];
            System.arraycopy(dest, 0, ret, 0, dest.length);
            System.arraycopy(source, 0, ret, dest.length, source.length);
            return ret;
        }
        return dest;
    }

    void executeStatement(final String statement, final Object[] fields, final int[] types, Connection writecon, final Context context) throws SQLException, OXException {
        boolean close_write = false;
        try {
            if (writecon == null) {
                writecon = DBPool.pickupWriteable(context);
                close_write = true;
            }
            final PreparedStatement pst = writecon.prepareStatement(statement);
            if (types != null && fields != null && types.length > 0 && fields.length > 0) {
                for (int a = 0; a < types.length; a++) {
                    if (fields[a] == null) {
                        pst.setNull(a + 1, types[a]);
                    } else {
                        pst.setObject(a + 1, fields[a], types[a]);
                    }
                }
            }
            pst.executeBatch();
            pst.close();
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle);
        } finally {
            if (close_write && writecon != null) {
                DBPool.pushWrite(context, writecon);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#removeRecurringType(com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public void removeRecurringType(final CalendarDataObject cdao) {
        cdao.setRecurrenceType(CalendarObject.NONE);
        cdao.removeInterval();
        cdao.removeUntil();
        cdao.removeOccurrence();
        cdao.removeDays();
        cdao.removeDayInMonth();
        cdao.removeMonth();
        cdao.removeRecurrenceCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#closeResultSet(java.sql.ResultSet)
     */
    @Override
    public void closeResultSet(final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException sqle) {
                LOG.warn("Error closing ResultSet", sqle);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#closePreparedStatement(java.sql.PreparedStatement)
     */
    @Override
    public void closePreparedStatement(final PreparedStatement prep) {
        if (prep != null) {
            try {
                prep.close();
            } catch (final SQLException sqle) {
                LOG.error("Error closing PreparedStatement.", sqle);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#closeStatement(java.sql.Statement)
     */
    @Override
    public void closeStatement(final Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (final SQLException sqle) {
                LOG.error("Error closing Statement.", sqle);
            }
        }
    }

    private void setStartAndEndDate(CalendarDataObject target, CalendarDataObject source) throws OXException {
        if ((target.getRecurrenceDatePosition() != null || target.getRecurrencePosition() != 0) && target.getRecurrenceID() == 0) { //Create Exception
            if (target.getStartDate() != null && target.getEndDate() != null) {
                return;
            }

            CalendarDataObject clone = source.clone();
            if (target.containsRecurrencePosition()) {
                clone.setRecurrencePosition(target.getRecurrencePosition());
            }
            if (target.containsRecurrenceDatePosition()) {
                clone.setRecurrenceDatePosition(target.getRecurrenceDatePosition());
            }

            recColl.setRecurrencePositionOrDateInDAO(clone, true);

            RecurringResultsInterface rss = recColl.calculateRecurringIgnoringExceptions(source, 0, 0, clone.getRecurrencePosition());
            if (rss == null) {
                throw OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_RECURRING_POSITION.create(clone.getRecurrencePosition());
            }
            RecurringResultInterface rs = rss.getRecurringResult(0);
            if (target.getStartDate() == null) {
                target.setStartDate(new Date(rs.getStart()));
            }
            if (target.getEndDate() == null) {
                target.setEndDate(new Date(rs.getEnd()));
            }
        } else {
            boolean success = false;
            if (source.getRecurrenceType() != CalendarDataObject.NO_RECURRENCE && target.containsRecurrenceType() && target.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE) { // Series -> Single
                RecurringResultsInterface rss = recColl.calculateFirstRecurring(source);
                if (rss != null && rss.size() > 0) {
                    RecurringResultInterface rs = rss.getRecurringResult(0);
                    if (rs != null) {
                        target.setStartDate(new Date(rs.getStart()));
                        target.setEndDate(new Date(rs.getEnd()));
                        success = true;
                    }
                }
            }
            if (!success) {
                if (!target.containsStartDate()) {
                    target.setStartDate(source.getStartDate());
                }
                if (!target.containsEndDate()) {
                    target.setEndDate(source.getEndDate());
                }
            }
        }
    }

    public CalendarDataObject fillFieldsForConflictQuery(final CalendarDataObject cdao, final CalendarDataObject edao, final boolean action) throws OXException {
        if (!action && !cdao.containsStartDate() && !cdao.containsEndDate() && !cdao.containsParticipants() && !cdao.containsRecurrenceType()) {
            return cdao;
        }

        final CalendarDataObject clone = cdao.clone();
        setStartAndEndDate(clone, edao);
        if (!clone.containsEndDate()) {
            clone.setEndDate(edao.getEndDate());
        }
        if (!clone.containsObjectID() || clone.getObjectID() == 0) {
            clone.setObjectID(edao.getObjectID());
        }
        if (clone.getUsers() == null) {
            clone.setUsers(edao.getUsers());
        }
        if (cdao.containsParticipants() && cdao.getParticipants() != null) {
            // cdao contains participants information; just ensure containsResources is set correctly
            if (!cdao.containsResources()) {
                // Ensure containsResources is set properly
                final Participant[] participants = cdao.getParticipants();
                for (int i = 0; i < participants.length; i++) {
                    if (participants[i].getType() == Participant.RESOURCE) {
                        clone.setContainsResources(true);
                        break;
                    }
                }
            }
        } else {
            // fill participants information from edao
            // TODO: Take care if edao contains Ressources and remove and new ones !!! We have to merge this!
            clone.setParticipants(edao.getParticipants());
            clone.setContainsResources(edao.containsResources());
            if (!clone.containsParticipants()) {
                clone.setParticipants(edao.getParticipants());
            }
        }
        //        if (!cdao.containsParticipants() && !cdao.containsResources() && edao.containsResources()) {
        //            // TODO: Take care if edao contains Ressources and remove and new ones !!! We have to merge this!
        //            clone.setParticipants(edao.getParticipants());
        //            clone.setContainsResources(edao.containsResources());
        //            if (!clone.containsParticipants()) {
        //                clone.setParticipants(edao.getParticipants());
        //            }
        //        }
        if (edao.getRecurrenceType() != CalendarObject.NONE) {
            if (cdao.containsRecurrenceDatePosition()) {
                clone.setRecurrenceDatePosition(cdao.getRecurrenceDatePosition());
            } else if (cdao.containsRecurrencePosition()) {
                clone.setRecurrencePosition(cdao.getRecurrencePosition());
            } else if (!cdao.containsRecurrenceType()) {
                clone.setRecurrence(edao.getRecurrence());
            } else {
                recColl.fillDAO(cdao);
                clone.setRecurrence(cdao.getRecurrence());
            }
            if (edao.containsChangeExceptions()) {
                clone.setChangeExceptions(edao.getChangeException());
            }
            if (edao.containsDeleteExceptions()) {
                clone.setDeleteExceptions(edao.getDeleteException());
            }
        }

        if (!checkForConflictRelevantUpdate(cdao, edao)) {
            clone.removeStartDate();
            clone.removeEndDate();
            clone.removeRecurrenceType();
            clone.removeParticipants();
            clone.removeShownAs();
        }

        return clone;
    }

    /**
     * Checks, if the two objects differ in fields, which are relevant for raising new conflicts.
     * If the new object does not contain a field, it is not changed.
     *
     * @param cdao new Object
     * @param edao new Object
     * @return true, if one or more relevant fields changed, false otherwise
     */
    boolean checkForConflictRelevantUpdate(final CalendarDataObject cdao, final CalendarDataObject edao) {
        if (cdao.containsStartDate() && check(cdao.getStartDate(), edao.getStartDate())) {
            return true;
        }
        if (cdao.containsEndDate() && check(cdao.getEndDate(), edao.getEndDate())) {
            return true;
        }
        if (cdao.containsRecurrenceType() && check(Integer.valueOf(cdao.getRecurrenceType()), Integer.valueOf(edao.getRecurrenceType()))) {
            return true;
        }
        if (cdao.containsParticipants() && checkParticipants(cdao.getParticipants(), edao.getParticipants())) {
            return true;
        }
        if (cdao.containsShownAs() && check(Integer.valueOf(cdao.getShownAs()), Integer.valueOf(edao.getShownAs()))) {
            return true;
        }
        return false;
    }

    @Override
    public void detectFolderMoveAction(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        if (cdao.getFolderMove()) { // TODO: Recurring apointments are not allowed to move, this must be checked !!
            if (FolderObject.SHARED == cdao.getFolderType()) {
                return;
            }
            if (edao.getFolderType() == cdao.getFolderType()) {
                if (FolderObject.PRIVATE == edao.getFolderType()) {
                    // Simple: Just change the uid's private folder id
                    cdao.setFolderMoveAction(CalendarOperation.PRIVATE_CURRENT_PARTICIPANT_ONLY);
                }  // Simple: Just update the overall fid, no separate action needed
            } else {
                if (FolderObject.PRIVATE == edao.getFolderType() && FolderObject.PUBLIC == cdao.getFolderType()) {
                    // Move from private to public
                    cdao.setFolderMoveAction(CalendarOperation.PUBLIC_ALL_PARTICIPANTS);
                } else if (FolderObject.PUBLIC == edao.getFolderType() && FolderObject.PRIVATE == cdao.getFolderType()) {
                    // Move from public to private
                    cdao.setParentFolderID(0);
                    cdao.setFolderMoveAction(CalendarOperation.PRIVATE_ALL_PARTICIPANTS);
                } else if (edao.getFolderType() == FolderObject.SHARED && cdao.getFolderType() == FolderObject.PRIVATE) {
                    //cdao.setParentFolderID(0);
                    cdao.setFolderMoveAction(CalendarOperation.PRIVATE_ALL_PARTICIPANTS);
                } else {
                    throw OXCalendarExceptionCodes.MOVE_NOT_SUPPORTED.create(I(edao.getFolderType()), I(cdao.getFolderType()));
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkUserParticipantObject(com.openexchange.groupware.container.UserParticipant, int)
     */
    @Override
    public void checkUserParticipantObject(final UserParticipant up, final int folder_type) throws OXException {
        if (up.getIdentifier() < 1) {
            throw OXCalendarExceptionCodes.INTERNAL_USER_PARTICIPANT_CHECK_1.create(Integer.valueOf(up.getIdentifier()), Integer.valueOf(folder_type));
        } else if ((folder_type == FolderObject.PRIVATE || folder_type == FolderObject.SHARED) && up.getPersonalFolderId() < 1) {
            throw OXCalendarExceptionCodes.INTERNAL_USER_PARTICIPANT_CHECK_2.create(Integer.valueOf(up.getIdentifier()));
        } else if (folder_type == FolderObject.PUBLIC && up.getPersonalFolderId() > 0) {
            throw OXCalendarExceptionCodes.INTERNAL_USER_PARTICIPANT_CHECK_3.create(Integer.valueOf(up.getIdentifier()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#detectTimeChange(com.openexchange.calendar.CalendarDataObject, com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public boolean detectTimeChange(final CalendarDataObject cdao, final CalendarDataObject edao) throws OXException {
        LOG.debug("detectTimeChange start");
        if (recurrenceTypeChanged(cdao, edao)) {
            return true;
        }
        if (isSeries(edao)) {
            if (seriesChangeStart(cdao, edao) || seriesChangeEnd(cdao, edao)) {
                return true;
            }
        } else {
            if (singleChangeStart(cdao, edao) || singleChangeEnd(cdao, edao)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSeries(CalendarDataObject edao) {
        return edao.getRecurrenceType() != CalendarDataObject.NO_RECURRENCE;
    }

    private boolean seriesChangeStart(CalendarDataObject cdao, CalendarDataObject edao) throws OXException {
        CalendarDataObject clone = cdao.clone();
        if (clone.getStartDate() == null) {
            return false;
        }
        addMissingRecurrenceInformation(clone, edao);
        RecurringResultInterface firstOccurrenceNew = calculateFirstRecurring(clone).getRecurringResult(0);
        RecurringResultInterface firstOccurrenceOld = calculateFirstRecurring(edao).getRecurringResult(0);
        if (firstOccurrenceNew.getStart() != firstOccurrenceOld.getStart()) {
            LOG.debug(cdao.getObjectID() + ": Start changed (" + firstOccurrenceNew.getStart() + ")->(" + firstOccurrenceOld.getStart() + ")");
            return true;
        }
        return false;
    }

    private boolean seriesChangeEnd(CalendarDataObject cdao, CalendarDataObject edao) throws OXException {
        CalendarDataObject clone = cdao.clone();
        edao.setStartDate(new Date(edao.getStartDate().getTime()));
        edao.setEndDate(new Date(edao.getEndDate().getTime()));
        if (clone.getEndDate() == null) {
            return false;
        }
        addMissingRecurrenceInformation(clone, edao);
        RecurringResultsInterface resultsNew = calculateRecurringIgnoringExceptions(clone, 0, 0, 0);
        RecurringResultInterface lastOccurrenceNew = resultsNew.getRecurringResult(resultsNew.size() - 1);
        RecurringResultsInterface resultsOld = calculateRecurringIgnoringExceptions(edao, 0, 0, 0);
        RecurringResultInterface lastOccurrenceOld = resultsOld.getRecurringResult(resultsOld.size() - 1);
        if (lastOccurrenceNew.getEnd() != lastOccurrenceOld.getEnd()) {
            LOG.debug(cdao.getObjectID() + ": End changed (" + new Date(lastOccurrenceNew.getEnd()) + ")->(" + new Date(lastOccurrenceOld.getEnd()) + ")");
            return true;
        }
        return false;
    }
    
    private static final int[] RECURRENCE_FIELDS = new int[] {
        CalendarObject.DAYS,
        CalendarObject.DAY_IN_MONTH,
        CalendarObject.INTERVAL,
        CalendarObject.MONTH,
        CalendarObject.RECURRENCE_TYPE
    };

    private void addMissingRecurrenceInformation(CalendarDataObject cdao, CalendarDataObject edao) {
        cdao.setRecurringStart(edao.getRecurringStart());
        if (edao.getTimezone() != null) {
            cdao.setTimezone(edao.getTimezone()); // Use original TimeZone information for calculation purposes.
        }
        for (int field : RECURRENCE_FIELDS) {
            if (!cdao.contains(field) && edao.contains(field)) {
                cdao.set(field, edao.get(field));
            }
        }
        if (!cdao.containsOccurrence() && edao.containsOccurrence()) {
            cdao.setOccurrence(edao.getOccurrence());
        }
        if (!cdao.containsUntil() && edao.containsUntil()) {
            cdao.setUntil(edao.getUntil());
        }
        if (!cdao.containsFullTime() && edao.containsFullTime()) {
            cdao.setFullTime(edao.getFullTime());
        }
    }

    private boolean singleChangeStart(CalendarDataObject cdao, CalendarDataObject edao) {
        if (cdao.getStartDate() == null) {
            return false;
        }
        return cdao.getStartDate().getTime() != edao.getStartDate().getTime();
    }

    private boolean singleChangeEnd(CalendarDataObject cdao, CalendarDataObject edao) {
        if (cdao.getEndDate() == null) {
            return false;
        }
        return cdao.getEndDate().getTime() != edao.getEndDate().getTime();
    }
    
    private boolean recurrenceTypeChanged(CalendarDataObject cdao, CalendarDataObject edao) {
        if (!cdao.containsRecurrenceType()) {
            return false;
        }
        if (isExceptionOfSeries(cdao, edao)) {
            return false;
        }
        if (cdao.getRecurrenceType() != edao.getRecurrenceType()) {
            LOG.debug(cdao.getObjectID() + ": Type changed (" + cdao.getRecurrenceType() + ")->(" + edao.getRecurrenceType() + ")");
            return true;
        }
        for (int field : RECURRENCE_FIELDS) {
            if (cdao.contains(field)) {
                if (cdao.get(field) == null && edao.get(field) != null) {
                    LOG.debug(cdao.getObjectID() + ": " + field + " changed (" + cdao.get(field) + ")->(" + edao.get(field) + ")");
                    return true;
                }
                if (cdao.get(field) == null && edao.get(field) == null) {
                    continue;
                }
                if (!cdao.get(field).equals(edao.get(field))) {
                    LOG.debug(cdao.getObjectID() + ": " + field + " changed (" + cdao.get(field) + ")->(" + edao.get(field) + ")");
                    return true;
                }
            }
        }
        if (untilChanged(cdao, edao)) {
            LOG.debug(cdao.getObjectID() + ": Until changed (" + cdao.getUntil() + ", " + cdao.containsUntil() + ")->(" + edao.getUntil() + ", " + edao.containsUntil() + ")");
            return true;
        }
        if (cdao.containsOccurrence() && cdao.getOccurrence() != edao.getOccurrence()) {
            LOG.debug(cdao.getObjectID() + ": Occurrence changed (" + cdao.getOccurrence() + ")->(" + edao.getOccurrence() + ")");
            return true;
        }
        return false;
    }

    /**
     * Checks, if the until value has changed. Weird mechanism, because CalendarDataObject.getUntil() returns the implicit value even if no until is set.
     * @param cdao
     * @param edao
     * @return
     */
    private boolean untilChanged(CalendarDataObject cdao, CalendarDataObject edao) {
        if (cdao.containsUntil()) {
            if (cdao.getUntil() == null) {
                if (edao.containsUntil()) {
                    if (edao.getUntil() != null) {
                        return true;
                    }
                }
            } else {
                if (edao.containsUntil()) {
                    if (edao.getUntil() == null) {
                        return true;
                    } else {
                        if (!cdao.getUntil().equals(edao.getUntil())) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isExceptionOfSeries(CalendarDataObject exception, CalendarDataObject series) {
        if (exception.getRecurrenceDatePosition() == null && exception.getRecurrencePosition() == 0) {
            return false;
        }
        return exception.getRecurrenceID() == series.getObjectID();
    }

    private final int[] FIELDS_ALL = {
        CalendarDataObject.OBJECT_ID, CalendarDataObject.CREATED_BY, CalendarDataObject.CREATION_DATE, CalendarDataObject.LAST_MODIFIED,
        CalendarDataObject.MODIFIED_BY, CalendarDataObject.FOLDER_ID, CalendarDataObject.PRIVATE_FLAG, CalendarDataObject.CATEGORIES,
        CalendarDataObject.TITLE, CalendarDataObject.LOCATION, CalendarDataObject.START_DATE, CalendarDataObject.END_DATE,
        CalendarDataObject.NOTE, CalendarDataObject.RECURRENCE_TYPE, CalendarDataObject.PARTICIPANTS, CalendarDataObject.USERS,
        CalendarDataObject.SHOWN_AS, CalendarDataObject.FULL_TIME, CalendarDataObject.COLOR_LABEL,
        CalendarDataObject.NUMBER_OF_ATTACHMENTS, CalendarDataObject.CHANGE_EXCEPTIONS, CalendarDataObject.DELETE_EXCEPTIONS,
        CalendarDataObject.RECURRENCE_ID, CalendarDataObject.RECURRENCE_POSITION, CalendarDataObject.RECURRENCE_CALCULATOR,
        CalendarDataObject.TIMEZONE };

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getAppointmentByID(int, com.openexchange.session.Session)
     */
    @Override
    public CalendarDataObject getAppointmentByID(final int id, final Session session) throws OXException {
        final CalendarSqlImp calendarsqlimp = new CalendarMySQL();
        final int contextId = session.getContextId();
        Connection readcon = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        boolean closeResources = true;
        try {
            readcon = Database.get(contextId, false);
            {
                final StringBuilder sb = new StringBuilder((FIELDS_ALL.length << 3) + 128);
                sb.append(StringCollection.getSelect(FIELDS_ALL, CalendarSql.DATES_TABLE_NAME)).append(" AS pd ");
                sb.append("WHERE cid = ? AND intfield01 = ?");
                prep = calendarsqlimp.getPreparedStatement(readcon, sb.toString());
            }
            prep.setInt(1, contextId);
            prep.setInt(2, id);
            rs = calendarsqlimp.getResultSet(prep);
            /*
             * Use CalendarOperation to load the calendar object
             */
            final Context ctx = ContextStorage.getStorageContext(session);
            final CalendarOperation co = new CalendarOperation();
            co.setResultSet(rs, prep, FIELDS_ALL, calendarsqlimp, readcon, 0, 0, session, ctx);
            final SearchIterator<CalendarDataObject> it = new CachedCalendarIterator(co, ctx, session.getUserId());
            closeResources = false;
            try {
                if (it.hasNext()) {
                    final CalendarDataObject retval = it.next();
                    if (it.hasNext()) {
                        /*
                         * Could not be uniquely determined.
                         */
                        return null;
                    }
                    return retval;
                }
            } finally {
                /*
                 * Implicitly closes SQL resources and connection
                 */
                it.close();
            }
            return null;
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle, new Object[0]);
        } finally {
            if (closeResources) {
                closeResultSet(rs);
                closePreparedStatement(prep);
                if (readcon != null) {
                    Database.back(contextId, false, readcon);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getChangeExceptionByDate(int, int, java.util.Date, int[], com.openexchange.session.Session)
     */
    @Override
    public CalendarDataObject getChangeExceptionByDate(final int folderId, final int recurrenceId, final Date exDate, final int[] fields, final Session session) throws OXException {
        if (null == fields || fields.length == 0) {
            return null;
        }
        final CalendarSqlImp calendarsqlimp = new CalendarMySQL();
        final int contextId = session.getContextId();
        Connection readcon = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        boolean closeResources = true;
        try {
            readcon = Database.get(contextId, false);
            final int[] nfields = checkAndAlterCols(fields);
            {
                final StringBuilder sb = new StringBuilder((nfields.length << 3) + 128);
                sb.append(StringCollection.getSelect(nfields, CalendarSql.DATES_TABLE_NAME)).append(" AS pd ");
                sb.append("WHERE cid = ? AND intfield02 = ? AND intfield01 != intfield02 AND field08 = ?");
                prep = calendarsqlimp.getPreparedStatement(readcon, sb.toString());
            }
            prep.setInt(1, contextId);
            prep.setInt(2, recurrenceId);
            prep.setString(3, Long.toString(exDate.getTime()));
            rs = calendarsqlimp.getResultSet(prep);
            /*
             * Use CalendarOperation to load the calendar object
             */
            final Context ctx = ContextStorage.getStorageContext(session);
            final CalendarOperation co = new CalendarOperation();
            co.setRequestedFolder(folderId);
            co.setResultSet(rs, prep, nfields, calendarsqlimp, readcon, 0, 0, session, ctx);
            final SearchIterator<CalendarDataObject> it = new CachedCalendarIterator(co, ctx, session.getUserId());
            closeResources = false;
            try {
                if (it.hasNext()) {
                    final CalendarDataObject retval = it.next();
                    if (it.hasNext()) {
                        /*
                         * Could not be uniquely determined.
                         */
                        return null;
                    }
                    return retval;
                }
            } finally {
                /*
                 * Implicitly closes SQL resources and connection
                 */
                it.close();
            }
            return null;
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle, new Object[0]);
        } finally {
            if (closeResources) {
                closeResultSet(rs);
                closePreparedStatement(prep);
                if (readcon != null) {
                    Database.back(contextId, false, readcon);
                }
            }
        }
    }

    private final int[] FIELDS_START_DATE = { CalendarDataObject.START_DATE };

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getChangeExceptionDatesByRecurrence(int, com.openexchange.session.Session)
     */
    @Override
    public long[] getChangeExceptionDatesByRecurrence(final int recurrenceId, final Session session) throws OXException {
        final CalendarDataObject[] ces = getChangeExceptionsByRecurrence(recurrenceId, FIELDS_START_DATE, session);
        final long[] dates = new long[ces.length];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = normalizeLong(ces[i].getStartDate().getTime());
        }
        return dates;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getChangeExceptionsByRecurrence(int, int[], com.openexchange.session.Session)
     */
    @Override
    public CalendarDataObject[] getChangeExceptionsByRecurrence(final int recurrenceId, final int[] fields, final Session session) throws OXException {
        if (null == fields || fields.length == 0) {
            return null;
        }
        final CalendarSqlImp calendarsqlimp = new CalendarMySQL();
        final int contextId = session.getContextId();
        Connection readcon = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        boolean closeResources = true;
        try {
            readcon = Database.get(contextId, false);
            final int[] nfields = checkAndAlterCols(fields);
            {
                final StringBuilder sb = new StringBuilder((nfields.length << 3) + 128);
                sb.append(StringCollection.getSelect(nfields, CalendarSql.DATES_TABLE_NAME)).append(" AS pd ");
                sb.append("WHERE cid = ? AND intfield02 = ? AND intfield01 != intfield02");
                prep = calendarsqlimp.getPreparedStatement(readcon, sb.toString());
            }
            prep.setInt(1, contextId);
            prep.setInt(2, recurrenceId);
            rs = calendarsqlimp.getResultSet(prep);
            /*
             * Use CalendarOperation to load the calendar object
             */
            final Context ctx = ContextStorage.getStorageContext(session);
            final CalendarOperation co = new CalendarOperation();
            co.setResultSet(rs, prep, nfields, calendarsqlimp, readcon, 0, 0, session, ctx);
            User user = Tools.getUser(session, ctx);
            UserConfiguration userConfig = Tools.getUserConfiguration(ctx, session.getUserId());
            CalendarFolderObject visibleFolders = recColl.getAllVisibleAndReadableFolderObject(user.getId(), user.getGroups(), ctx, userConfig, readcon);
            final SearchIterator<CalendarDataObject> it = new CachedCalendarIterator(visibleFolders, co, ctx, session.getUserId());
            final List<CalendarDataObject> retval = new ArrayList<CalendarDataObject>();
            closeResources = false;
            try {
                while (it.hasNext()) {
                    retval.add(it.next());
                }
            } finally {
                /*
                 * Implicitly closes SQL resources and connection
                 */
                it.close();
            }
            return retval.toArray(new CalendarDataObject[retval.size()]);
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle, new Object[0]);
        } finally {
            if (closeResources) {
                closeResultSet(rs);
                closePreparedStatement(prep);
                if (readcon != null) {
                    Database.back(contextId, false, readcon);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getAppointmentsByID(int, int[], int[], com.openexchange.session.Session)
     */
    @Override
    public Appointment[] getAppointmentsByID(final int folderId, final int[] ids, final int[] fields, final Session session) throws OXException {
        if (null == ids || ids.length == 0) {
            return null;
        }
        if (null == fields || fields.length == 0) {
            return null;
        }
        final CalendarSqlImp calendarsqlimp = new CalendarMySQL();
        final int contextId = session.getContextId();
        Connection readcon = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        boolean closeResources = true;
        try {
            readcon = Database.get(contextId, false);
            final int[] nfields = checkAndAlterCols(fields);
            {
                final StringBuilder sb = new StringBuilder((nfields.length << 3) + 128);
                sb.append(StringCollection.getSelect(nfields, CalendarSql.DATES_TABLE_NAME)).append(" AS pd ");
                sb.append("WHERE cid = ? AND intfield01 IN (").append(ids[0]);
                for (int i = 1; i < ids.length; i++) {
                    sb.append(',').append(ids[1]);
                }
                sb.append(')');
                prep = calendarsqlimp.getPreparedStatement(readcon, sb.toString());
            }
            prep.setInt(1, contextId);
            rs = calendarsqlimp.getResultSet(prep);
            /*
             * Use CalendarOperation to load the calendar objects
             */
            final Context ctx = ContextStorage.getStorageContext(session);
            final CalendarOperation co = new CalendarOperation();
            co.setRequestedFolder(folderId);
            co.setResultSet(rs, prep, nfields, calendarsqlimp, readcon, 0, 0, session, ctx);
            final SearchIterator<CalendarDataObject> it = new CachedCalendarIterator(co, ctx, session.getUserId());
            closeResources = false;
            final Map<Integer, CalendarDataObject> m = new HashMap<Integer, CalendarDataObject>(ids.length);
            try {
                while (it.hasNext()) {
                    final CalendarDataObject cur = it.next();
                    m.put(Integer.valueOf(cur.getObjectID()), cur);
                }
            } finally {
                /*
                 * Implicitly closes SQL resources and connection
                 */
                it.close();
            }
            final Appointment[] retval = new CalendarDataObject[ids.length];
            for (int i = 0; i < ids.length; i++) {
                retval[i] = m.get(Integer.valueOf(ids[i]));
            }
            return retval;
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle, new Object[0]);
        } finally {
            if (closeResources) {
                closeResultSet(rs);
                closePreparedStatement(prep);
                if (readcon != null) {
                    Database.back(contextId, false, readcon);
                }
            }
        }
    }

    private final String SQL_SELECT_FID = "SELECT fid FROM prg_dates WHERE intfield01 = ? AND cid = ?";

    private final String SQL_SELECT_FID2 = "SELECT pfid FROM prg_dates_members WHERE object_id = ? AND cid = ? AND member_uid = ?";

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#resolveFolderIDForUser(int, int, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public int resolveFolderIDForUser(final int oid, final int uid, final Context c) throws OXException {
        int ret = -1;
        final CalendarSqlImp calendarsqlimp = new CalendarMySQL();
        Connection readcon = null;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try {
            readcon = DBPool.pickup(c);
            prep = calendarsqlimp.getPreparedStatement(readcon, SQL_SELECT_FID);
            prep.setInt(1, oid);
            prep.setInt(2, c.getContextId());
            rs = calendarsqlimp.getResultSet(prep);
            if (rs.next()) {
                final int tmp = rs.getInt(1);
                if (!rs.wasNull() && tmp > 0) {
                    return tmp;
                }
            }
            closeResultSet(rs);
            closePreparedStatement(prep);
            prep = calendarsqlimp.getPreparedStatement(readcon, SQL_SELECT_FID2);
            prep.setInt(1, oid);
            prep.setInt(2, c.getContextId());
            prep.setInt(3, uid);
            rs = calendarsqlimp.getResultSet(prep);
            if (rs.next()) {
                ret = rs.getInt(1);
                if (rs.wasNull() || ret == 0) {
                    ret = -1;
                }
            }
        } catch (final SQLException sqle) {
            throw OXCalendarExceptionCodes.CALENDAR_SQL_ERROR.create(sqle, new Object[0]);
        } finally {
            closePreparedStatement(prep);
            closeResultSet(rs);
            if (readcon != null) {
                DBPool.push(c, readcon);
            }
        }
        return ret;
    }

    @Override
    public void fillEventInformation(final CalendarDataObject cdao, final CalendarDataObject edao, UserParticipant up_event[], final Set<UserParticipant> new_userparticipants, final Set<UserParticipant> deleted_userparticipants, final Set<UserParticipant> modified_userparticipants, Participant p_event[], final Set<Participant> new_participants, final Set<Participant> deleted_participants, final Participant[] modified_participants) {
        final Participants pu = new Participants();
        final Participants p = new Participants();
        final UserParticipant oup[] = edao.getUsers();
        final Participant op[] = edao.getParticipants();
        if (oup != null && oup.length > 0) {
            for (int a = 0; a < oup.length; a++) {
                pu.add(oup[a]);
            }
        }
        if (op != null && op.length > 0) {
            for (int a = 0; a < op.length; a++) {
                p.add(op[a]);
            }
        }
        for (int a = 0; a < up_event.length; a++) {
            pu.add(up_event[a]);
        }
        for (int a = 0; a < p_event.length; a++) {
            p.add(p_event[a]);
        }
        if (new_userparticipants != null && new_userparticipants.size() > 0) {
            for (UserParticipant up : new_userparticipants) {
                pu.add(up);
            }
        }
        if (new_participants != null && new_participants.size() > 0) {
            for (Participant np : new_participants) {
                p.add(np);
            }
        }
        up_event = pu.getUsers();
        if (deleted_userparticipants != null && deleted_userparticipants.size() > 0) {
            Arrays.sort(up_event);
            for (UserParticipant dup : deleted_userparticipants) {
                final int x = Arrays.binarySearch(up_event, dup);
                if (x > -1) {
                    final UserParticipant temp[] = new UserParticipant[up_event.length - 1];
                    System.arraycopy(up_event, 0, temp, 0, x);
                    System.arraycopy(up_event, x + 1, temp, x, ((up_event.length - 1) - x));
                    up_event = temp;
                }
            }
        }

        // Apply changes
        if (modified_userparticipants != null && modified_userparticipants.size() > 0) {
            for (final UserParticipant participant : modified_userparticipants) {
                if (participant.getType() == Participant.USER) {
                    for (int i = 0; i < up_event.length; i++) {
                        if (up_event[i].getIdentifier() == participant.getIdentifier()) {
                            up_event[i] = participant;
                        }
                    }
                }
            }
        }

        p_event = p.getList();
        if (deleted_participants != null && deleted_participants.size() > 0) {
            Arrays.sort(p_event);
            for (Participant dp : deleted_participants) {
                final int x = Arrays.binarySearch(p_event, dp);
                if (x > -1) {
                    final Participant temp[] = new Participant[p_event.length - 1];
                    System.arraycopy(p_event, 0, temp, 0, x);
                    System.arraycopy(p_event, x + 1, temp, x, ((p_event.length - 1) - x));
                    p_event = temp;
                }
            }
        }
        cdao.setUsers(up_event);
        cdao.setParticipants(p_event);
        if (!cdao.containsTitle()) {
            cdao.setTitle(edao.getTitle());
        }
        if (!cdao.containsStartDate()) {
            cdao.setStartDate(edao.getStartDate());
        }
        if (!cdao.containsEndDate()) {
            cdao.setEndDate(edao.getEndDate());
        }
        if (!cdao.containsLocation()) {
            cdao.setLocation(edao.getLocation());
        }
        if (!cdao.containsShownAs()) {
            cdao.setShownAs(edao.getShownAs());
        }
        if (!cdao.containsNote()) {
            cdao.setNote(edao.getNote());
        }
        if (!cdao.containsCreatedBy()) {
            cdao.setCreatedBy(edao.getCreatedBy());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getDAOFromList(java.util.List, int)
     */
    @Override
    public CalendarDataObject getDAOFromList(final List<CalendarDataObject> list, final int oid) {
        for (int a = 0; a < list.size(); a++) {
            final CalendarDataObject cdao = list.get(a);
            if (cdao.getObjectID() == oid) {
                return cdao;
            }
        }
        return null;
    }

    private final Set<Integer> IGNORE_FIELDS = new HashSet<Integer>(Arrays.asList(Integer
        .valueOf(Appointment.ALARM), Integer.valueOf(Appointment.LAST_MODIFIED), Integer
        .valueOf(Appointment.MODIFIED_BY), Integer.valueOf(0)));

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkForSoloReminderUpdate(com.openexchange.calendar.CalendarDataObject, int[], com.openexchange.calendar.MBoolean)
     */
    @Override
    public boolean checkForSoloReminderUpdate(final CalendarDataObject cdao, final int[] ucols, final MBoolean cup) {
        if (cup.getMBoolean()) {
            return false;
        } else if (CalendarConfig.getSoloReminderTriggerEvent()) {
            for (int i = 0; i < ucols.length; i++) {
                if (!IGNORE_FIELDS.contains(Integer.valueOf(ucols[i]))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkAndRemovePastReminders(com.openexchange.calendar.CalendarDataObject, com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public void checkAndRemovePastReminders(final CalendarDataObject cdao, final CalendarDataObject edao) {
        if (CalendarConfig.getCheckAndRemovePastReminders() && cdao.containsAlarm() && cdao.getAlarm() >= 0) {
            long reminder = 0;
            if (cdao.containsStartDateAndIsNotNull()) {
                reminder = cdao.getStartDate().getTime();
            } else {
                reminder = edao.getStartDate().getTime();
            }
            if (checkMillisInThePast(reminder - (cdao.getAlarm() * 60000))) {
                cdao.removeAlarm();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getUserTimeUTCDate(java.util.Date, java.lang.String)
     */
    @Override
    public long getUserTimeUTCDate(final Date date, final String timezone) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone(timezone));
        c.setTime(date);
        Calendar target = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        target.set(Calendar.YEAR, c.get(Calendar.YEAR));
        target.set(Calendar.MONTH, c.get(Calendar.MONTH));
        target.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        //System.out.println("--------> " + date + " to " + new Date(target.getTimeInMillis()));
        return target.getTimeInMillis();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkIfArrayKeyExistInArray(java.lang.Object[], java.lang.Object[])
     */
    @Override
    public boolean checkIfArrayKeyExistInArray(final Object a[], final Object b[]) {
        if (a != null && b != null) {
            Arrays.sort(b);
            for (int x = 0; x < a.length; x++) {
                if (Arrays.binarySearch(b, a[x]) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkIfDateOccursInRecurrence(java.util.Date, com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public boolean checkIfDateOccursInRecurrence(final Date date, final CalendarDataObject recurringAppointment) throws OXException {
        if (date == null) {
            /*
             * No dates given
             */
            return true;
        }
        final long rangeStart = date.getTime() - Constants.MILLI_WEEK;
        final long rangeEnd = date.getTime() + Constants.MILLI_WEEK;
        final RecurringResultsInterface rresults = calculateRecurring(
            recurringAppointment,
            rangeStart,
            rangeEnd,
            0,
            MAX_OCCURRENCESE,
            true);
        return (rresults.getPositionByLong(date.getTime()) != -1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkIfDatesOccurInRecurrence(java.util.Date[], com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public boolean checkIfDatesOccurInRecurrence(final Date[] dates, final CalendarDataObject recurringAppointment) throws OXException {
        if (dates == null || dates.length == 0) {
            /*
             * No dates given
             */
            return true;
        }

        // Generate appropriate range
        final Date[] sorted = new Date[dates.length];
        System.arraycopy(dates, 0, sorted, 0, dates.length);
        Arrays.sort(sorted);
        final long rangeStart = sorted[0].getTime() - Constants.MILLI_WEEK;
        final long rangeEnd = sorted[sorted.length - 1].getTime() + Constants.MILLI_WEEK;

        final RecurringResultsInterface rresults = calculateRecurring(
            recurringAppointment,
            rangeStart,
            rangeEnd,
            0,
            MAX_OCCURRENCESE,
            true);
        boolean result = true;
        for (int i = 0; i < dates.length && result; i++) {
            result = (rresults.getPositionByLong(dates[i].getTime()) != -1);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getDatesPositions(java.util.Date[], com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public int[] getDatesPositions(final Date[] dates, final CalendarDataObject recurringAppointment) throws OXException {
        if (dates == null || dates.length == 0) {
            /*
             * No dates given
             */
            return new int[0];
        }

        // Generate appropriate range
        final Date[] sorted = new Date[dates.length];
        System.arraycopy(dates, 0, sorted, 0, dates.length);
        Arrays.sort(sorted);
        final long rangeStart = sorted[0].getTime() - Constants.MILLI_WEEK;
        final long rangeEnd = sorted[sorted.length - 1].getTime() + Constants.MILLI_WEEK;

        final RecurringResultsInterface rresults = calculateRecurring(
            recurringAppointment,
            rangeStart,
            rangeEnd,
            0,
            MAX_OCCURRENCESE,
            true);

        final int[] retval = new int[dates.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = rresults.getPositionByLong(dates[i].getTime());
        }
        return retval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#mergeExceptionDates(java.util.Date[], java.util.Date[])
     */
    @Override
    public Date[] mergeExceptionDates(final Date[] ddates, final Date[] cdates) {
        final Set<Date> set;
        {
            int initialCapacity = 0;
            if (ddates != null) {
                initialCapacity += ddates.length;
            }
            if (cdates != null) {
                initialCapacity += cdates.length;
            }
            if (initialCapacity == 0) {
                return new Date[0];
            }
            set = new HashSet<Date>(initialCapacity);
        }
        if (ddates != null) {
            set.addAll(Arrays.asList(ddates));
        }
        if (cdates != null) {
            set.addAll(Arrays.asList(cdates));
        }
        final Date[] merged = set.toArray(new Date[set.size()]);
        Arrays.sort(merged);
        return merged;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#checkForInvalidCharacters(com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public void checkForInvalidCharacters(final CalendarDataObject cdao) throws OXException {
        String error = null;
        if (cdao.containsTitle() && cdao.getTitle() != null) {
            error = Check.containsInvalidChars(cdao.getTitle());
            if (error != null) {
                throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Title", error);
            }
        }
        if (cdao.containsLocation() && cdao.getLocation() != null) {
            error = Check.containsInvalidChars(cdao.getLocation());
            if (error != null) {
                throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Location", error);
            }
        }
        if (cdao.containsNote() && cdao.getNote() != null) {
            error = Check.containsInvalidChars(cdao.getNote());
            if (error != null) {
                throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Note", error);
            }
        }
        if (cdao.containsCategories() && cdao.getCategories() != null) {
            error = Check.containsInvalidChars(cdao.getCategories());
            if (error != null) {
                throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Tags", error);
            }
        }
        if (cdao.containsUserParticipants() && cdao.getUsers() != null) {
            final UserParticipant up[] = cdao.getUsers();
            for (int a = 0; a < up.length; a++) {
                error = Check.containsInvalidChars(up[a].getDisplayName());
                if (error != null) {
                    throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Display Name", error);
                }
                error = Check.containsInvalidChars(up[a].getConfirmMessage());
                if (error != null) {
                    throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Confirm Message", error);
                }
            }
        }

        if (cdao.containsParticipants() && cdao.getParticipants() != null) {
            for (final Participant p : cdao.getParticipants()) {
                error = Check.containsInvalidChars(p.getDisplayName());
                if (error != null) {
                    throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Display Name", error);
                }
                error = Check.containsInvalidChars(p.getEmailAddress());
                if (error != null) {
                    throw OXCalendarExceptionCodes.INVALID_CHARACTER.create("Email Address", error);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#getString(com.openexchange.calendar.CalendarDataObject, int)
     */
    @Override
    public String getString(final CalendarDataObject cdao, final int fieldID) {
        switch (fieldID) {
            case CalendarObject.TITLE:
                return cdao.getTitle();
            case Appointment.LOCATION:
                return cdao.getLocation();
            case CalendarObject.NOTE:
                return cdao.getNote();
            case CommonObject.CATEGORIES:
                return cdao.getCategories();
            case Appointment.TIMEZONE:
                return cdao.getTimezoneFallbackUTC();
            case CalendarObject.DELETE_EXCEPTIONS:
                return cdao.getDelExceptions();
            case CalendarObject.CHANGE_EXCEPTIONS:
                return cdao.getExceptions();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.calendar.CalendarCommonCollectionInterface#recoverForInvalidPattern(com.openexchange.calendar.CalendarDataObject)
     */
    @Override
    public void recoverForInvalidPattern(final CalendarDataObject cdao) {
        removeRecurringType(cdao);
    }

    // From Tools:

    /**
     * Formats specified date's time millis into a date string.<br>
     * e.g.: <code>&quot;Jan 13, 2009&quot;</code>
     *
     * @param timeMillis The date's time millis to format
     * @return The date string.
     */
    @Override
    public String getUTCDateFormat(final long timeMillis) {
        return Tools.getUTCDateFormat(timeMillis);
    }

    /**
     * Formats specified date into a date string.<br>
     * e.g.: <code>&quot;Jan 13, 2009&quot;</code>
     *
     * @param date The date to format
     * @return The date string.
     */
    @Override
    public String getUTCDateFormat(final Date date) {
        return Tools.getUTCDateFormat(date);
    }

    @Override
    public Context getContext(final Session so) throws OXException {
        return Tools.getContext(so);
    }

    @Override
    public User getUser(final Session so, final Context ctx) throws OXException {
        return Tools.getUser(so, ctx);
    }

    @Override
    public UserConfiguration getUserConfiguration(final Context ctx, final int userId) throws OXException {
        return UserConfigurationStorage.getInstance().getUserConfiguration(userId, ctx);
    }

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param ID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a
     *            custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     */
    @Override
    public TimeZone getTimeZone(final String ID) {
        return Tools.getTimeZone(ID);
    }

    /**
     * Gets the appointment's title associated with given object ID in given context.
     *
     * @param objectId The object ID
     * @param ctx The context
     * @return The appointment's title or <code>null</code>
     * @throws OXException If determining appointment's title fails
     */
    @Override
    public String getAppointmentTitle(final int objectId, final Context ctx) throws OXException {
        return Tools.getAppointmentTitle(objectId, ctx);
    }

    /**
     * Gets the appointment's folder associated with given object ID in given context.
     *
     * @param objectId The object ID
     * @param userId The session user
     * @param ctx The context
     * @return The appointment's folder associated with given object ID in given context.
     * @throws OXException If determining appointment's folder fails
     */
    @Override
    public int getAppointmentFolder(final int objectId, final int userId, final Context ctx) throws OXException {
        return Tools.getAppointmentFolder(objectId, userId, ctx);
    }
}
