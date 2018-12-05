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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getExceptionDateUpdates;
import static com.openexchange.chronos.common.CalendarUtils.getRecurrenceIds;
import static com.openexchange.chronos.common.CalendarUtils.hasFurtherOccurrences;
import static com.openexchange.chronos.common.CalendarUtils.isAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isClassifiedFor;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isLastUserAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.common.CalendarUtils.optTimeZone;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.chronos.compat.Event2Appointment.asInt;
import static com.openexchange.chronos.impl.AbstractStorageOperation.PARAM_CONNECTION;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i2I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import com.google.common.collect.ImmutableMap;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarStrings;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DataAwareRecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.impl.performer.AttendeeUsageTracker;
import com.openexchange.chronos.impl.session.DefaultEntityResolver;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CalendarConfig;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Utils {

    /** The fixed identifier for an account of the internal calendar provider */
    public static final int ACCOUNT_ID = CalendarAccount.DEFAULT_ACCOUNT.getAccountId();

    /** The fixed identifier for the internal calendar provider */
    public static final String PROVIDER_ID = CalendarAccount.DEFAULT_ACCOUNT.getProviderId();

    /** The event fields that are also available if an event's classification is not {@link Classification#PUBLIC} */
    public static final EventField[] NON_CLASSIFIED_FIELDS = {
        EventField.CLASSIFICATION, EventField.CREATED, EventField.UID, EventField.FILENAME, EventField.CREATED_BY,
        EventField.CALENDAR_USER, EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES, EventField.END_DATE,
        EventField.ID, EventField.TIMESTAMP, EventField.MODIFIED_BY, EventField.FOLDER_ID, EventField.SERIES_ID,
        EventField.RECURRENCE_RULE, EventField.SEQUENCE, EventField.START_DATE, EventField.TRANSP
    };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Utils.class);

    /** Windows to Olson timezone mappings */
    private static final Map<String, String> WINDOWS2OLSON = ImmutableMap.<String, String> builder() // @formatter:off
        .put("Saint Pierre Standard Time", "America/Miquelon")
        .put("Greenwich Standard Time", "Atlantic/Reykjavik")
        .put("Tasmania Standard Time", "Australia/Hobart")
        .put("Magallanes Standard Time", "America/Punta_Arenas")
        .put("Central European Standard Time", "Europe/Warsaw")
        .put("Azores Standard Time", "Atlantic/Azores")
        .put("Arabic Standard Time", "Asia/Baghdad")
        .put("Samoa Standard Time", "Pacific/Apia")
        .put("SA Western Standard Time", "America/La_Paz")
        .put("Bahia Standard Time", "America/Bahia")
        .put("Pakistan Standard Time", "Asia/Karachi")
        .put("Libya Standard Time", "Africa/Tripoli")
        .put("Yakutsk Standard Time", "Asia/Yakutsk")
        .put("Sakhalin Standard Time", "Asia/Sakhalin")
        .put("Norfolk Standard Time", "Pacific/Norfolk")
        .put("Afghanistan Standard Time", "Asia/Kabul")
        .put("Fiji Standard Time", "Pacific/Fiji")
        .put("Central Brazilian Standard Time", "America/Cuiaba")
        .put("Cuba Standard Time", "America/Havana")
        .put("Aleutian Standard Time", "America/Adak")
        .put("Pacific SA Standard Time", "America/Santiago")
        .put("Egypt Standard Time", "Africa/Cairo")
        .put("Arab Standard Time", "Asia/Riyadh")
        .put("Taipei Standard Time", "Asia/Taipei")
        .put("UTC-02", "Etc/GMT+2")
        .put("West Bank Standard Time", "Asia/Hebron")
        .put("Sao Tome Standard Time", "Africa/Sao_Tome")
        .put("Alaskan Standard Time", "America/Anchorage")
        .put("Omsk Standard Time", "Asia/Omsk")
        .put("Eastern Standard Time", "America/New_York")
        .put("Myanmar Standard Time", "Asia/Rangoon")
        .put("Syria Standard Time", "Asia/Damascus")
        .put("Russian Standard Time", "Europe/Moscow")
        .put("Mountain Standard Time (Mexico)", "America/Chihuahua")
        .put("Magadan Standard Time", "Asia/Magadan")
        .put("Iran Standard Time", "Asia/Tehran")
        .put("Marquesas Standard Time", "Pacific/Marquesas")
        .put("Azerbaijan Standard Time", "Asia/Baku")
        .put("E. South America Standard Time", "America/Sao_Paulo")
        .put("Turks And Caicos Standard Time", "America/Grand_Turk")
        .put("UTC-09", "Etc/GMT+9")
        .put("Russia Time Zone 3", "Europe/Samara")
        .put("UTC-08", "Etc/GMT+8")
        .put("E. Africa Standard Time", "Africa/Nairobi")
        .put("Nepal Standard Time", "Asia/Katmandu")
        .put("UTC+12", "Etc/GMT-12")
        .put("Turkey Standard Time", "Europe/Istanbul")
        .put("China Standard Time", "Asia/Shanghai")
        .put("UTC+13", "Etc/GMT-13")
        .put("Mountain Standard Time", "America/Denver")
        .put("West Pacific Standard Time", "Pacific/Port_Moresby")
        .put("AUS Central Standard Time", "Australia/Darwin")
        .put("Newfoundland Standard Time", "America/St_Johns")
        .put("N. Central Asia Standard Time", "Asia/Novosibirsk")
        .put("SA Eastern Standard Time", "America/Cayenne")
        .put("Singapore Standard Time", "Asia/Singapore")
        .put("Vladivostok Standard Time", "Asia/Vladivostok")
        .put("Haiti Standard Time", "America/Port-au-Prince")
        .put("North Asia East Standard Time", "Asia/Irkutsk")
        .put("Sudan Standard Time", "Africa/Khartoum")
        .put("Jordan Standard Time", "Asia/Amman")
        .put("Bangladesh Standard Time", "Asia/Dhaka")
        .put("Venezuela Standard Time", "America/Caracas")
        .put("Cen. Australia Standard Time", "Australia/Adelaide")
        .put("W. Australia Standard Time", "Australia/Perth")
        .put("Mauritius Standard Time", "Indian/Mauritius")
        .put("Central Standard Time", "America/Chicago")
        .put("Tomsk Standard Time", "Asia/Tomsk")
        .put("Arabian Standard Time", "Asia/Dubai")
        .put("North Korea Standard Time", "Asia/Pyongyang")
        .put("AUS Eastern Standard Time", "Australia/Sydney")
        .put("Namibia Standard Time", "Africa/Windhoek")
        .put("UTC", "Etc/GMT")
        .put("North Asia Standard Time", "Asia/Krasnoyarsk")
        .put("Central America Standard Time", "America/Guatemala")
        .put("Kaliningrad Standard Time", "Europe/Kaliningrad")
        .put("Aus Central W. Standard Time", "Australia/Eucla")
        .put("New Zealand Standard Time", "Pacific/Auckland")
        .put("SA Pacific Standard Time", "America/Bogota")
        .put("Chatham Islands Standard Time", "Pacific/Chatham")
        .put("Cape Verde Standard Time", "Atlantic/Cape_Verde")
        .put("Pacific Standard Time", "America/Los_Angeles")
        .put("US Eastern Standard Time", "America/Indianapolis")
        .put("W. Mongolia Standard Time", "Asia/Hovd")
        .put("Caucasus Standard Time", "Asia/Yerevan")
        .put("Ulaanbaatar Standard Time", "Asia/Ulaanbaatar")
        .put("India Standard Time", "Asia/Calcutta")
        .put("Easter Island Standard Time", "Pacific/Easter")
        .put("E. Europe Standard Time", "Europe/Chisinau")
        .put("W. Central Africa Standard Time", "Africa/Lagos")
        .put("W. Europe Standard Time", "Europe/Berlin")
        .put("Sri Lanka Standard Time", "Asia/Colombo")
        .put("Korea Standard Time", "Asia/Seoul")
        .put("Saratov Standard Time", "Europe/Saratov")
        .put("Tonga Standard Time", "Pacific/Tongatapu")
        .put("Tokyo Standard Time", "Asia/Tokyo")
        .put("Tocantins Standard Time", "America/Araguaina")
        .put("Israel Standard Time", "Asia/Jerusalem")
        .put("Central Standard Time (Mexico)", "America/Mexico_City")
        .put("Bougainville Standard Time", "Pacific/Bougainville")
        .put("Central Asia Standard Time", "Asia/Almaty")
        .put("UTC-11", "Etc/GMT+11")
        .put("US Mountain Standard Time", "America/Phoenix")
        .put("Ekaterinburg Standard Time", "Asia/Yekaterinburg")
        .put("Eastern Standard Time (Mexico)", "America/Cancun")
        .put("Georgian Standard Time", "Asia/Tbilisi")
        .put("Argentina Standard Time", "America/Buenos_Aires")
        .put("Line Islands Standard Time", "Pacific/Kiritimati")
        .put("Hawaiian Standard Time", "Pacific/Honolulu")
        .put("Central Europe Standard Time", "Europe/Budapest")
        .put("GMT Standard Time", "Europe/London")
        .put("West Asia Standard Time", "Asia/Tashkent")
        .put("FLE Standard Time", "Europe/Kiev")
        .put("Canada Central Standard Time", "America/Regina")
        .put("Montevideo Standard Time", "America/Montevideo")
        .put("Central Pacific Standard Time", "Pacific/Guadalcanal")
        .put("Lord Howe Standard Time", "Australia/Lord_Howe")
        .put("South Africa Standard Time", "Africa/Johannesburg")
        .put("Atlantic Standard Time", "America/Halifax")
        .put("Astrakhan Standard Time", "Europe/Astrakhan")
        .put("Paraguay Standard Time", "America/Asuncion")
        .put("Romance Standard Time", "Europe/Paris")
        .put("Greenland Standard Time", "America/Godthab")
        .put("E. Australia Standard Time", "Australia/Brisbane")
        .put("Russia Time Zone 11", "Asia/Kamchatka")
        .put("GTB Standard Time", "Europe/Bucharest")
        .put("Russia Time Zone 10", "Asia/Srednekolymsk")
        .put("Belarus Standard Time", "Europe/Minsk")
        .put("Altai Standard Time", "Asia/Barnaul")
        .put("Morocco Standard Time", "Africa/Casablanca")
        .put("SE Asia Standard Time", "Asia/Bangkok")
        .put("Dateline Standard Time", "Etc/GMT+12")
        .put("Transbaikal Standard Time", "Asia/Chita")
        .put("Middle East Standard Time", "Asia/Beirut")
        .put("Pacific Standard Time (Mexico)", "America/Tijuana")
    .build(); // @formatter:on

    /**
     * Gets a value indicating whether the current calendar user should be added as default attendee to events implicitly or not,
     * independently of the event being group-scheduled or not, based on the value of {@link CalendarParameters#PARAMETER_DEFAULT_ATTENDEE}
     * in the supplied parameters.
     * <p/>
     * If the <i>legacy</i> storage is in use, the default attendee is enforced statically.
     *
     * @param session The calendar session to evaluate
     * @return <code>true</code> the current calendar user should be added as default attendee to events implicitly, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_DEFAULT_ATTENDEE
     */
    public static boolean isEnforceDefaultAttendee(CalendarSession session) {
        // enabled by default for now (as legacy storage still in use)
        return session.get(CalendarParameters.PARAMETER_DEFAULT_ATTENDEE, Boolean.class, Boolean.TRUE).booleanValue();
    }

    /**
     * Gets a value indicating whether a recurring event series should be resolved to individual occurrences or not, based on the value
     * of {@link CalendarParameters#PARAMETER_EXPAND_OCCURRENCES} in the supplied parameters.
     *
     * @param parameters The calendar parameters to evaluate
     * @return <code>true</code> if individual occurrences should be resolved, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_EXPAND_OCCURRENCES
     */
    public static boolean isResolveOccurrences(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class, Boolean.FALSE).booleanValue();
    }

    /**
     * Gets a value indicating whether (soft) conflicts of internal attendees should be checked during event creation or update or not,
     * based on the value of {@link CalendarParameters#PARAMETER_CHECK_CONFLICTS} in the supplied parameters.
     *
     * @param parameters The calendar parameters to evaluate
     * @return <code>true</code> if (soft) conflicts should be checked, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_CHECK_CONFLICTS
     */
    public static boolean isCheckConflicts(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.class, Boolean.FALSE).booleanValue();
    }
    
    /**
     * Gets a value indicating whether the checks of (external) attendee URIs are disabled or not, considering both the calendar
     * parameters and general configuration.
     *
     * @param session The calendar session to evaluate
     * @return <code>true</code> if the URI checks are disabled, <code>false</code>, otherwise
     * @see CalendarParameters#PARAMETER_SKIP_EXTERNAL_ATTENDEE_URI_CHECKS
     * @see CalendarConfig#isSkipExternalAttendeeURIChecks()
     */
    public static boolean isSkipExternalAttendeeURIChecks(CalendarSession session) {
        return b(session.get(CalendarParameters.PARAMETER_SKIP_EXTERNAL_ATTENDEE_URI_CHECKS, Boolean.class, Boolean.FALSE)) ||
             session.getConfig().isSkipExternalAttendeeURIChecks();
    }

    /**
     * Gets the timezone valid for the supplied calendar session, which is either the (possibly overridden) timezone defined via
     * {@link CalendarParameters#PARAMETER_TIMEZONE}, or as fallback, the session user's default timezone.
     *
     * @param session The calendar session to get the timezone for
     * @return The timezone
     * @see CalendarParameters#PARAMETER_TIMEZONE
     * @see User#getTimeZone()
     */
    public static TimeZone getTimeZone(CalendarSession session) throws OXException {
        TimeZone timeZone = session.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class);
        return null != timeZone ? timeZone : session.getEntityResolver().getTimeZone(session.getUserId());
    }

    /**
     * Extracts the "from" date used for range-queries from the parameter {@link CalendarParameters#PARAMETER_RANGE_START}.
     *
     * @param parameters The calendar parameters to evaluate
     * @return The "from" date, or <code>null</code> if not set
     */
    public static Date getFrom(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    /**
     * Extracts the "until" date used for range-queries from the parameter {@link CalendarParameters#PARAMETER_RANGE_END}.
     *
     * @param parameters The calendar parameters to evaluate
     * @return The "until" date, or <code>null</code> if not set
     */
    public static Date getUntil(CalendarParameters parameters) {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    /**
     * Constructs a search term to match events located in a specific folder. Depending on the folder's type, either a search term for
     * the {@link EventField#FOLDER_ID} and/or for the {@link AttendeeField#FOLDER_ID} is built.
     * <p/>
     * The session user's read permissions in the folder (<i>own</i> vs <i>all</i>) are considered automatically, too, by restricting via
     * {@link EventField#CREATED_BY} if needed.
     *
     * @param session The calendar session
     * @param folder The folder to construct the search term for
     * @return The search term
     */
    public static SearchTerm<?> getFolderIdTerm(CalendarSession session, CalendarFolder folder) {
        SearchTerm<?> searchTerm;
        if (PublicType.getInstance().equals(folder.getType())) {
            /*
             * match the event's common folder identifier
             */
            searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folder.getId());
        } else {
            /*
             * for personal folders, match against the corresponding attendee's folder
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(folder.getCreatedBy())))
                .addSearchTerm(getSearchTerm(AttendeeField.FOLDER_ID, SingleOperation.EQUALS, folder.getId()));
            if (false == isEnforceDefaultAttendee(session)) {
                /*
                 * also match the event's common folder identifier if no default attendee is enforced
                 */
                searchTerm = new CompositeSearchTerm(CompositeOperation.OR)
                    .addSearchTerm(getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folder.getId()))
                    .addSearchTerm(searchTerm);
            }
        }
        if (folder.getOwnPermission().getReadPermission() < Permission.READ_ALL_OBJECTS) {
            /*
             * if only access to "own" objects, restrict to events created by the current session's user
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(searchTerm)
                .addSearchTerm(getSearchTerm(EventField.CREATED_BY, SingleOperation.EQUALS, folder.getSession().getUserId()));
        }
        return searchTerm;
    }

    /**
     * Selects a well-known and valid timezone based on a client-supplied timezone, using different fallbacks if no exactly matching
     * timezone is available.
     *
     * @param session The calendar session
     * @param calendarUserId The identifier of the calendar user
     * @param timeZone The timezone as supplied by the client
     * @param originalTimeZone The original timezone in case of updates, or <code>null</code> if not available
     * @return The selected timezone, or <code>null</code> if passed timezoen reference was <code>null</code>
     */
    public static TimeZone selectTimeZone(CalendarSession session, int calendarUserId, TimeZone timeZone, TimeZone originalTimeZone) throws OXException {
        if (null == timeZone) {
            return null;
        }
        /*
         * try to match by timezone identifier first
         */
        TimeZone matchingTimeZone = optTimeZone(timeZone.getID(), null);
        if (null != matchingTimeZone) {
            return matchingTimeZone;
        }
        /*
         * try and match a known timezone with the same rules (original timezone, calendar user timezone, session user timezone)
         */
        if (null != originalTimeZone && timeZone.hasSameRules(originalTimeZone)) {
            LOG.debug("No matching timezone found for '{}', falling back to original timezone '{}'.", timeZone.getID(), originalTimeZone);
            return originalTimeZone;
        }
        /*
         * use calendar user's / session user's timezone if same rules are effective
         */
        TimeZone calendarUserTimeZone = session.getEntityResolver().getTimeZone(calendarUserId);
        if (timeZone.hasSameRules(calendarUserTimeZone)) {
            LOG.debug("No matching timezone found for '{}', falling back to calendar user's timezone '{}'.", timeZone.getID(), calendarUserTimeZone);
            return calendarUserTimeZone;
        }
        if (session.getUserId() != calendarUserId) {
            TimeZone sessionUserTimeZone = session.getEntityResolver().getTimeZone(session.getUserId());
            if (timeZone.hasSameRules(sessionUserTimeZone)) {
                LOG.debug("No matching timezone found for '{}', falling back to session user's timezone '{}'.", timeZone.getID(), sessionUserTimeZone);
                return sessionUserTimeZone;
            }
        }
        /*
         * select matching olson timezone for a known windows timezone
         */
        TimeZone mappedTimeZone = optTimeZone(WINDOWS2OLSON.get(timeZone.getID()));
        if (null != mappedTimeZone) {
            LOG.debug("No matching timezone found for '{}', falling back to mapped olson timezone '{}'.", timeZone.getID(), mappedTimeZone);
            return mappedTimeZone;
        }
        /*
         * select the timezone with the same rules, and most similar identifier
         */
        List<TimeZone> timeZonesWithSameRules = getWithSameRules(timeZone);
        if (timeZonesWithSameRules.isEmpty()) {
            LOG.warn("No timezone with matching rules found for '{}', falling back to calendar user timezone '{}'.", timeZone.getID(), calendarUserTimeZone);
            return calendarUserTimeZone;
        }
        timeZonesWithSameRules.sort(Comparator.comparingInt(tz -> levenshteinDistance(tz.getID(), timeZone.getID())));
        TimeZone fallbackTimeZone = timeZonesWithSameRules.get(0);
        LOG.warn("No matching timezone found for '{}', falling back to '{}'.", timeZone.getID(), fallbackTimeZone);
        return fallbackTimeZone;
    }

    private static List<TimeZone> getWithSameRules(TimeZone timeZone) {
        List<TimeZone> timeZones = new ArrayList<TimeZone>();
        for (String timeZoneId : TimeZone.getAvailableIDs(timeZone.getRawOffset())) {
            TimeZone candidateTimeZone = optTimeZone(timeZoneId);
            if (timeZone.hasSameRules(candidateTimeZone)) {
                timeZones.add(candidateTimeZone);
            }
        }
        return timeZones;
    }

    /**
     * Measures the distance between two strings, based on the <i>Levenshtein</i> algorithm.
     *
     * @param a The first string
     * @param b The second string
     * @return The result
     * @see <a href="http://rosettacode.org/wiki/Levenshtein_distance#Java">Levenshtein Distance</a>
     */
    private static int levenshteinDistance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    /**
     * <i>Anonymizes</i> an event in case it is not marked as {@link Classification#PUBLIC}, and the session's user is neither creator, nor
     * attendee of the event.
     * <p/>
     * After anonymization, the event will only contain those properties defined in {@link #NON_CLASSIFIED_FIELDS}, as well as the
     * generic summary "Private".
     *
     * @param session The calendar session
     * @param event The event to anonymize
     * @return The potentially anonymized event
     */
    public static Event anonymizeIfNeeded(CalendarSession session, Event event) throws OXException {
        if (false == isClassifiedFor(event, session.getUserId())) {
            return event;
        }
        Event anonymizedEvent = EventMapper.getInstance().copy(event, new Event(), NON_CLASSIFIED_FIELDS);
        anonymizedEvent.setSummary(StringHelper.valueOf(session.getEntityResolver().getLocale(session.getUserId())).getString(CalendarStrings.SUMMARY_PRIVATE));
        return anonymizedEvent;
    }

    /**
     * Adds one or more warnings in the calendar session.
     *
     * @param session The calendar session
     * @param warnings The warnings to add, or <code>null</code> to ignore
     */
    public static void addWarnings(CalendarSession session, Collection<OXException> warnings) {
        if (null != warnings && 0 < warnings.size()) {
            for (OXException warning : warnings) {
                session.addWarning(warning);
            }
        }
    }

    /**
     * Gets a user.
     *
     * @param session The calendar session
     * @param userId The identifier of the user to get
     * @return The user
     */
    public static User getUser(CalendarSession session, int userId) throws OXException {
        return Services.getService(UserService.class).getUser(userId, session.getContextId());
    }

    /**
     * Gets the actual target calendar user for a specific folder. This is either the current session's user for "private" or "public"
     * folders, or the folder owner for "shared" calendar folders.
     *
     * @param session The calendar session
     * @param folder The folder to get the calendar user for
     * @return The calendar user
     */
    public static CalendarUser getCalendarUser(CalendarSession session, CalendarFolder folder) throws OXException {
        int calendarUserId = getCalendarUserId(folder);
        return session.getEntityResolver().applyEntityData(new CalendarUser(), calendarUserId);
    }

    /**
     * Gets the identifier of the actual target calendar user for a specific folder. This is either the current session's user for
     * "private" or "public" folders, or the folder owner for "shared" calendar folders.
     *
     * @param folder The folder to get the calendar user for
     * @return The identifier of the calendar user
     */
    public static int getCalendarUserId(CalendarFolder folder) {
        if (SharedType.getInstance().equals(folder.getType())) {
            return folder.getCreatedBy();
        }
        return folder.getSession().getUserId();
    }

    /**
     * Gets a value indicating whether a specific event is actually present in the supplied folder. Based on the folder type, the
     * event's public folder identifier or the attendee's personal calendar folder is checked, as well as the attendee's <i>hidden</i>
     * marker.
     *
     * @param event The event to check
     * @param folder The folder where the event should appear in
     * @return <code>true</code> if the event <i>is</i> in the folder, <code>false</code>, otherwise
     */
    public static boolean isInFolder(Event event, CalendarFolder folder) {
        if (PublicType.getInstance().equals(folder.getType()) || false == isGroupScheduled(event) && null != event.getFolderId()) {
            return folder.getId().equals(event.getFolderId());
        } else {
            Attendee userAttendee = CalendarUtils.find(event.getAttendees(), folder.getCreatedBy());
            return null != userAttendee && folder.getId().equals(userAttendee.getFolderId()) && false == userAttendee.isHidden();
        }
    }

    /**
     * Gets a value indicating whether an event in a specific folder is visible to the current user or not, either based on the user's
     * permissions in the calendar folder representing the actual view on the event, together with its classification, or based on the
     * user participating in the event as organizer or attendee.
     *
     * @param folder The calendar folder the event is read in
     * @param event The event to check
     * @return <code>true</code> if the event can be read, <code>false</code>, otherwise
     */
    public static boolean isVisible(CalendarFolder folder, Event event) {
        int userId = folder.getSession().getUserId();
        if (Classification.PRIVATE.equals(event.getClassification()) && isClassifiedFor(event, userId)) {
            return false;
        }
        Permission ownPermission = folder.getOwnPermission();
        if (ownPermission.getReadPermission() >= Permission.READ_ALL_OBJECTS) {
            return true;
        }
        if (ownPermission.getReadPermission() == Permission.READ_OWN_OBJECTS && matches(event.getCreatedBy(), userId)) {
            return true;
        }
        if ((PublicType.getInstance().equals(folder.getType()) || PrivateType.getInstance().equals(folder.getType())) &&
            (matches(event.getCalendarUser(), userId) || isAttendee(event, userId) || isOrganizer(event, userId))) {
            return true;
        }
        return false;
    }

    /**
     * Gets a <i>userized</i> calendar folder by its identifier.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to get
     * @return The folder
     */
    public static CalendarFolder getFolder(CalendarSession session, String folderId) throws OXException {
        return getFolder(session, folderId, true);
    }

    /**
     * Gets a <i>userized</i> calendar folder by its identifier.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to get
     * @param failIfNotVisible <code>true</code> to fail if the folder is not visible for the current session's user, <code>false</code>, otherwise
     * @return The folder
     */
    public static CalendarFolder getFolder(CalendarSession session, String folderId, boolean failIfNotVisible) throws OXException {
        FolderObject folder = getEntityResolver(session).getFolder(asInt(folderId), optConnection(session));
        UserPermissionBits permissionBits = ServerSessionAdapter.valueOf(session.getSession()).getUserPermissionBits();
        EffectivePermission permission = folder.getEffectiveUserPermission(session.getUserId(), permissionBits);
        if (failIfNotVisible && false == permission.isFolderVisible()) {
            throw CalendarExceptionCodes.NO_READ_PERMISSION.create(folderId);
        }
        return new CalendarFolder(session.getSession(), folder, permission);
    }

    /**
     * Gets the folders that are actually visible for the current session's user from a list of possible folder identifiers.
     *
     * @param session The calendar session
     * @param folderIds The possible identifiers of the folders to get
     * @return The visible folders, or an empty list if none are visible
     */
    public static List<CalendarFolder> getVisibleFolders(CalendarSession session, Collection<String> folderIds) throws OXException {
        if (null == folderIds || 0 == folderIds.size()) {
            return Collections.emptyList();
        }
        List<CalendarFolder> folders = new ArrayList<CalendarFolder>(folderIds.size());
        DefaultEntityResolver entityResolver = getEntityResolver(session);
        UserPermissionBits permissionBits = ServerSessionAdapter.valueOf(session.getSession()).getUserPermissionBits();
        Connection connection = optConnection(session);
        for (String folderId : folderIds) {
            try {
                FolderObject folder = entityResolver.getFolder(asInt(folderId), connection);
                EffectivePermission permission = folder.getEffectiveUserPermission(session.getUserId(), permissionBits);
                if (permission.isFolderVisible()) {
                    folders.add(new CalendarFolder(session.getSession(), folder, permission));
                }
            } catch (OXException | NumberFormatException e) {
                LOG.warn("Error evaluating if folder {} is visible, skipping.", folderId, e);
                continue;
            }
        }
        return folders;
    }

    /**
     * Maps the corresponding event occurrences from two collections based on their common object- and recurrence identifiers.
     *
     * @param originalOccurrences The original event occurrences
     * @param updatedOccurrences The updated event occurrences
     * @return A list of entries holding each of the matching original and updated event occurrences, with one of them possibly <code>null</code>
     */
    public static List<Entry<Event, Event>> mapEventOccurrences(List<Event> originalOccurrences, List<Event> updatedOccurrences) {
        List<Entry<Event, Event>> mappedEvents = new ArrayList<Entry<Event, Event>>(Math.max(originalOccurrences.size(), updatedOccurrences.size()));
        for (Event originalOccurrence : originalOccurrences) {
            Event updatedOccurrence = find(updatedOccurrences, originalOccurrence.getId(), originalOccurrence.getRecurrenceId());
            mappedEvents.add(new AbstractMap.SimpleEntry<Event, Event>(originalOccurrence, updatedOccurrence));
        }
        for (Event updatedOccurrence : updatedOccurrences) {
            if (null == find(originalOccurrences, updatedOccurrence.getId(), updatedOccurrence.getRecurrenceId())) {
                mappedEvents.add(new AbstractMap.SimpleEntry<Event, Event>(null, updatedOccurrence));
            }
        }
        return mappedEvents;
    }

    /**
     * Get the configured quota and the actual usage of the underlying calendar account.
     *
     * @param session The calendar session
     * @param storage The calendar storage to use
     * @return The quota
     * @throws OXException In case of an error
     */
    public static Quota getQuota(CalendarSession session, CalendarStorage storage) throws OXException {
        /*
         * get configured amount quota limit
         */
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class, true);
        Connection connection = session.get(PARAM_CONNECTION, Connection.class);
        long limit;
        if (null != connection) {
            limit = AmountQuotas.getLimit(session.getSession(), Module.CALENDAR.getName(), configViewFactory, connection);
        } else {
            DatabaseService databaseService = Services.getService(DatabaseService.class, true);
            limit = AmountQuotas.getLimit(session.getSession(), Module.CALENDAR.getName(), configViewFactory, databaseService);
        }
        if (Quota.UNLIMITED == limit) {
            return Quota.UNLIMITED_AMOUNT;
        }
        /*
         * get actual usage & wrap in quota structure appropriately
         */
        long usage = storage.getEventStorage().countEvents();
        return new Quota(QuotaType.AMOUNT, limit, usage);
    }

    /**
     * Applies <i>userized</i> versions of change- and delete-exception dates in the series master event based on the user's actual
     * attendance.
     *
     * @param storage A reference to the calendar storage to use
     * @param seriesMaster The series master event
     * @param forUser The identifier of the user to apply the exception dates for
     * @return The passed event reference, with possibly adjusted exception dates
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.2.6">RFC 6638, section 3.2.6</a>
     */
    public static Event applyExceptionDates(CalendarStorage storage, Event seriesMaster, int forUser) throws OXException {
        if (false == isSeriesMaster(seriesMaster) || false == isGroupScheduled(seriesMaster) || isOrganizer(seriesMaster, forUser) ||
            isLastUserAttendee(seriesMaster.getAttendees(), forUser)) {
            /*
             * "real" delete exceptions for all attendees, take over as-is
             */
            return seriesMaster;
        }
        /*
         * check which change exceptions exist where the user is attending
         */
        SortedSet<RecurrenceId> changeExceptionDates = seriesMaster.getChangeExceptionDates();
        if (null == changeExceptionDates || 0 == changeExceptionDates.size()) {
            return seriesMaster;
        }
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, seriesMaster.getSeriesId()))
            .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
            .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(forUser)))
            .addSearchTerm(new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(getSearchTerm(AttendeeField.HIDDEN, SingleOperation.ISNULL))
                .addSearchTerm(getSearchTerm(AttendeeField.HIDDEN, SingleOperation.NOT_EQUALS, Boolean.TRUE)))
        ;
        EventField[] fields = new EventField[] { EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_ID };
        List<Event> attendedChangeExceptions = storage.getEventStorage().searchEvents(searchTerm, null, fields);
        if (attendedChangeExceptions.size() == changeExceptionDates.size()) {
            return seriesMaster;
        }
        /*
         * apply userized exception dates
         */
        return applyExceptionDates(seriesMaster, getRecurrenceIds(attendedChangeExceptions));
    }

    /**
     * Applies <i>userized</i> versions of change- and delete-exception dates in the series master event based on the user's actual
     * attendance.
     *
     * @param seriesMaster The series master event
     * @param attendedChangeExceptionDates The exception dates the user actually attends
     * @return The passed event reference, with possibly adjusted exception dates
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.2.6">RFC 6638, section 3.2.6</a>
     */
    private static Event applyExceptionDates(Event seriesMaster, SortedSet<RecurrenceId> attendedChangeExceptionDates) throws OXException {
        /*
         * check which change exceptions exist where the user is attending
         */
        SortedSet<RecurrenceId> changeExceptionDates = seriesMaster.getChangeExceptionDates();
        if (null == changeExceptionDates || 0 == changeExceptionDates.size()) {
            return seriesMaster;
        }
        /*
         * apply a 'userized' version of exception dates by moving exception date from change- to delete-exceptions
         */
        SortedSet<RecurrenceId> userizedChangeExceptions = new TreeSet<RecurrenceId>(attendedChangeExceptionDates);
        SortedSet<RecurrenceId> userizedDeleteExceptions = new TreeSet<RecurrenceId>();
        if (null != seriesMaster.getDeleteExceptionDates()) {
            userizedDeleteExceptions.addAll(seriesMaster.getDeleteExceptionDates());
        }
        for (RecurrenceId originalChangeExceptionDate : seriesMaster.getChangeExceptionDates()) {
            if (false == userizedChangeExceptions.contains(originalChangeExceptionDate)) {
                userizedDeleteExceptions.add(originalChangeExceptionDate);
            }
        }
        return new DelegatingEvent(seriesMaster) {

            @Override
            public SortedSet<RecurrenceId> getDeleteExceptionDates() {
                return userizedDeleteExceptions;
            }

            @Override
            public boolean containsDeleteExceptionDates() {
                return true;
            }

            @Override
            public SortedSet<RecurrenceId> getChangeExceptionDates() {
                return userizedChangeExceptions;
            }

            @Override
            public boolean containsChangeExceptionDates() {
                return true;
            }
        };
    }

    /**
     * Replaces a change exception's recurrence identifier to piggyback the recurrence data of the corresponding event series. This aids the
     * <i>legacy</i> storage to calculate the correct recurrence positions properly.
     * 
     * @param changeException The change exception event to edit the recurrence identifier in
     * @param recurrenceData The recurrence data to inject
     * @return The passed change exception event, with an replaced data-aware recurrence identifier
     * @see DataAwareRecurrenceId
     */
    public static Event injectRecurrenceData(Event changeException, RecurrenceData recurrenceData) {
        RecurrenceId recurrenceId = changeException.getRecurrenceId();
        if (null != recurrenceId) {
            changeException.setRecurrenceId(new DataAwareRecurrenceId(recurrenceData, recurrenceId.getValue()));
        }
        return changeException;
    }

    /**
     * Replaces the recurrence identifier in a list of change exceptions to piggyback the recurrence data of the corresponding event
     * series. This aids the <i>legacy</i> storage to calculate the correct recurrence positions properly.
     * 
     * @param changeExceptions The change exception events to edit the recurrence identifier in
     * @param recurrenceData The recurrence data to inject
     * @return The passed change exception events, with an replaced data-aware recurrence identifier
     * @see DataAwareRecurrenceId
     */
    public static List<Event> injectRecurrenceData(List<Event> changeExceptions, RecurrenceData recurrenceData) {
        if (null != changeExceptions) {
            for (Event changeException : changeExceptions) {
                injectRecurrenceData(changeException, recurrenceData);
            }
        }
        return changeExceptions;
    }

    /**
     * Gets a list containing all elements provided by the supplied iterator.
     *
     * @param itrerator The iterator to get the list for
     * @return The list
     */
    public static <T> List<T> asList(Iterator<T> iterator) {
        List<T> list = new ArrayList<T>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Gets a list containing all elements provided by the supplied iterator.
     *
     * @param itrerator The iterator to get the list for
     * @param limit The maximum number of items to include
     * @return The list
     */
    public static <T> List<T> asList(Iterator<T> iterator, int limit) {
        List<T> list = new ArrayList<T>();
        while (iterator.hasNext() && list.size() < limit) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * Gets all calendar folders accessible by the current sesssion's user.
     *
     * @param session The underlying calendar session
     * @return The folders, or an empty list if there are none
     */
    public static List<CalendarFolder> getVisibleFolders(CalendarSession session) throws OXException {
        Connection connection = optConnection(session);
        List<FolderObject> folders = getEntityResolver(session).getVisibleFolders(session.getUserId(), connection);
        UserPermissionBits permissionBits = ServerSessionAdapter.valueOf(session.getSession()).getUserPermissionBits();
        List<CalendarFolder> calendarFolders = new ArrayList<CalendarFolder>(folders.size());
        for (FolderObject folder : folders) {
            EffectivePermission permission;
            try {
                permission = folder.getEffectiveUserPermission(session.getUserId(), permissionBits, connection);
            } catch (SQLException e) {
                LOG.warn("Error getting effective user permission for folder {}; skipping.", I(folder.getObjectID()), e);
                continue;
            }
            if (permission.isFolderVisible()) {
                calendarFolders.add(new CalendarFolder(session.getSession(), folder, permission));
            }
        }
        return calendarFolders;
    }

    /**
     * Calculates a map holding the identifiers of all folders a user is able to access, based on the supplied collection of folder
     * identifiers.
     *
     * @param session The calendar session
     * @param folderIds The identifiers of all folders to determine the users with access for
     * @return The identifiers of the affected folders for each user
     */
    public static Map<Integer, List<String>> getAffectedFoldersPerUser(CalendarSession session, Collection<String> folderIds) throws OXException {
        return getAffectedFoldersPerUser(getEntityResolver(session), folderIds);
    }

    /**
     * Calculates a map holding the identifiers of all folders a user is able to access, based on the supplied collection of folder
     * identifiers.
     *
     * @param contextId The context identifier
     * @param entityResolver The entity resolver, or <code>null</code> if not available
     * @param folderIds The identifiers of all folders to determine the users with access for
     * @return The identifiers of the affected folders for each user
     */
    public static Map<Integer, List<String>> getAffectedFoldersPerUser(int contextId, EntityResolver entityResolver, Collection<String> folderIds) throws OXException {
        DefaultEntityResolver defaultEntityResolver;
        if (null != entityResolver && contextId == entityResolver.getContextID() && DefaultEntityResolver.class.isInstance(entityResolver)) {
            defaultEntityResolver = (DefaultEntityResolver) entityResolver;
        } else {
            defaultEntityResolver = new DefaultEntityResolver(contextId, Services.getServiceLookup());
        }
        return getAffectedFoldersPerUser(defaultEntityResolver, folderIds);
    }

    private static Map<Integer, List<String>> getAffectedFoldersPerUser(DefaultEntityResolver entityResolver, Collection<String> folderIds) {
        Map<Integer, List<String>> affectedFoldersPerUser = new HashMap<Integer, List<String>>();
        for (String folderId : folderIds) {
            try {
                FolderObject folder = entityResolver.getFolder(asInt(folderId));
                for (Integer userId : getAffectedUsers(folder, entityResolver)) {
                    com.openexchange.tools.arrays.Collections.put(affectedFoldersPerUser, userId, folderId);
                }
            } catch (Exception e) {
                LOG.warn("Error collecting affected users for folder {}; skipping.", folderId, e);
            }
        }
        return affectedFoldersPerUser;
    }

    /**
     * Gets a list of the personal folder identifiers representing all internal user attendee's view for the supplied collection of
     * attendees.
     *
     * @param attendees The attendees to collect the folder identifiers for
     * @return The personal folder identifiers of the internal user attendees, or an empty list if there are none
     */
    public static List<String> getPersonalFolderIds(List<Attendee> attendees) {
        List<String> folderIds = new ArrayList<String>();
        for (Attendee attendee : CalendarUtils.filter(attendees, Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
            String folderId = attendee.getFolderId();
            if (Strings.isNotEmpty(folderId)) {
                folderIds.add(folderId);
            }
        }
        return folderIds;
    }

    /**
     * Gets a collection of all user identifiers for whom a specific folder is visible, i.e. a list of user identifiers who'd be affected
     * by a change in this folder.
     *
     * @param folder The folder to get the affected user identifiers for
     * @param entityResolver The entity resolver to use
     * @return The identifiers of the affected folders for each user
     */
    public static Set<Integer> getAffectedUsers(FolderObject folder, EntityResolver entityResolver) {
        List<OCLPermission> permissions = folder.getPermissions();
        if (null == permissions || 0 == permissions.size()) {
            return Collections.emptySet();
        }
        Set<Integer> affectedUsers = new HashSet<Integer>();
        for (OCLPermission permission : permissions) {
            if (permission.isFolderVisible()) {
                if (permission.isGroupPermission()) {
                    try {
                        int[] groupMembers = entityResolver.getGroupMembers(permission.getEntity());
                        affectedUsers.addAll(Arrays.asList(i2I(groupMembers)));
                    } catch (OXException e) {
                        LOG.warn("Error resolving members of group {} for for folder {}; skipping.", I(permission.getEntity()), I(folder.getObjectID()), e);
                    }
                } else {
                    affectedUsers.add(I(permission.getEntity()));
                }
            }
        }
        return affectedUsers;
    }

    /**
     * Gets a collection of all user identifiers for whom a specific folder is visible, i.e. a list of user identifiers who'd be affected
     * by a change in this folder.
     *
     * @param folder The folder to get the affected user identifiers for
     * @param entityResolver The entity resolver to use
     * @return The identifiers of the affected folders for each user
     */
    public static Set<Integer> getAffectedUsers(CalendarFolder folder, EntityResolver entityResolver) {
        Permission[] permissions = folder.getPermissions();
        if (null == permissions || 0 == permissions.length) {
            return Collections.emptySet();
        }
        Set<Integer> affectedUsers = new HashSet<Integer>();
        for (Permission permission : permissions) {
            if (permission.isVisible()) {
                if (permission.isGroup()) {
                    try {
                        int[] groupMembers = entityResolver.getGroupMembers(permission.getEntity());
                        affectedUsers.addAll(Arrays.asList(i2I(groupMembers)));
                    } catch (OXException e) {
                        LOG.warn("Error resolving members of group {} for for folder {}; skipping.", I(permission.getEntity()), folder.getId(), e);
                    }
                } else {
                    affectedUsers.add(I(permission.getEntity()));
                }
            }
        }
        return affectedUsers;
    }

    /**
     * Gets a value indicating whether an event update will cover a different time period than the original event. If this is the case,
     * conflicts may have to be re-checked or if the attendee's participation status should be reseted.
     *
     * @param originalEvent The original event being updated
     * @param updatedEvent The updated event, as passed by the client
     * @return <code>true</code> if the updated event covers a different times period, <code>false</code>, otherwise
     * @see <a href="https://tools.ietf.org/html/rfc6638#section-3.2.8">RFC 6638, section 3.2.8</a>
     */
    public static boolean coversDifferentTimePeriod(Event originalEvent, Event updatedEvent) throws OXException {
        if (false == EventMapper.getInstance().get(EventField.RECURRENCE_RULE).equals(originalEvent, updatedEvent)) {
            /*
             * true if there are 'new' occurrences (caused by a modified or extended rule)
             */
            if (hasFurtherOccurrences(originalEvent.getRecurrenceRule(), updatedEvent.getRecurrenceRule())) {
                return true;
            }
        }
        if (false == EventMapper.getInstance().get(EventField.DELETE_EXCEPTION_DATES).equals(originalEvent, updatedEvent)) {
            /*
             * true if there are 'new' occurrences (caused by the reinstatement of previous delete exceptions)
             */
            SimpleCollectionUpdate<RecurrenceId> exceptionDateUpdates = getExceptionDateUpdates(originalEvent.getDeleteExceptionDates(), updatedEvent.getDeleteExceptionDates());
            if (false == exceptionDateUpdates.getRemovedItems().isEmpty()) {
                return true;
            }
        }
        if (false == EventMapper.getInstance().get(EventField.RECURRENCE_DATES).equals(originalEvent, updatedEvent)) {
            /*
             * true if there are 'new' occurrences (caused by newly introduced recurrence dates)
             */
            SimpleCollectionUpdate<RecurrenceId> exceptionDateUpdates = getExceptionDateUpdates(originalEvent.getRecurrenceDates(), updatedEvent.getRecurrenceDates());
            if (false == exceptionDateUpdates.getAddedItems().isEmpty()) {
                return true;
            }
        }
        if (false == EventMapper.getInstance().get(EventField.START_DATE).equals(originalEvent, updatedEvent)) {
            /*
             * true if updated start is before the original start
             */
            if (updatedEvent.getStartDate().before(originalEvent.getStartDate())) {
                return true;
            }
        }
        if (false == EventMapper.getInstance().get(EventField.END_DATE).equals(originalEvent, updatedEvent)) {
            /*
             * true if updated end is after the original end
             */
            if (updatedEvent.getEndDate().after(originalEvent.getEndDate())) {
                return true;
            }
        }
        /*
         * no different time period, otherwise
         */
        return false;
    }

    /**
     * Gets an iterator for the recurrence set of the supplied series master event, iterating over the occurrences of the event series.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link
     * Event#getChangeExceptionDates()}) are skipped implicitly, so that those occurrences won't be included in the resulting iterator.
     * <p/>
     * Start- and end of the considered range are taken from the corresponding parameters in the supplied session.
     *
     * @param session The calendar session
     * @param masterEvent The recurring event master
     * @return The recurrence iterator
     */
    public static Iterator<Event> resolveOccurrences(CalendarSession session, Event masterEvent) throws OXException {
        return resolveOccurrences(session, masterEvent, getFrom(session), getUntil(session));
    }

    /**
     * Gets an iterator for the recurrence set of the supplied series master event, iterating over the occurrences of the event series.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link
     * Event#getChangeExceptionDates()}) are skipped implicitly, so that those occurrences won't be included in the resulting iterator.
     *
     * @param session The calendar session
     * @param masterEvent The recurring event master
     * @param from The start of the iteration interval, or <code>null</code> to start with the first occurrence
     * @param until The end of the iteration interval, or <code>null</code> to iterate until the last occurrence
     * @return The recurrence iterator
     */
    public static Iterator<Event> resolveOccurrences(CalendarSession session, Event masterEvent, Date from, Date until) throws OXException {
        return session.getRecurrenceService().iterateEventOccurrences(masterEvent, from, until);
    }

    /**
     * Gets an iterator for the recurrence set of the supplied series master event, iterating over the recurrence identifiers of the event.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link
     * Event#getChangeExceptionDates()}) are skipped implicitly, so that those occurrences won't be included in the resulting iterator.
     * <p/>
     * Start- and end of the considered range are taken from the corresponding parameters in the supplied session.
     *
     * @param session The calendar session
     * @param masterEvent The recurring event master
     * @return The recurrence iterator
     */
    public static Iterator<RecurrenceId> getRecurrenceIterator(CalendarSession session, Event masterEvent) throws OXException {
        return getRecurrenceIterator(session, masterEvent, getFrom(session), getUntil(session));
    }

    /**
     * Gets a recurrence iterator for the supplied series master event, iterating over the recurrence identifiers of the event.
     * <p/>
     * Any exception dates (as per {@link Event#getDeleteExceptionDates()}) and overridden instances (as per {@link
     * Event#getChangeExceptionDates()}) are skipped implicitly, so that those occurrences won't be included in the resulting iterator.
     *
     * @param session The calendar session
     * @param masterEvent The recurring event master
     * @param from The start of the iteration interval, or <code>null</code> to start with the first occurrence
     * @param until The end of the iteration interval, or <code>null</code> to iterate until the last occurrence
     * @return The recurrence iterator
     */
    public static Iterator<RecurrenceId> getRecurrenceIterator(CalendarSession session, Event masterEvent, Date from, Date until) throws OXException {
        return session.getRecurrenceService().iterateRecurrenceIds(new DefaultRecurrenceData(masterEvent), from, until);
    }

    /**
     * Tracks newly added attendees from creations and updates found in the supplied calendar result, in case attendee tracking is
     * enabled as per {@link CalendarParameters#PARAMETER_TRACK_ATTENDEE_USAGE}.
     *
     * @param session The calendar session
     * @param event The calendar event to track
     */
    public static void trackAttendeeUsage(CalendarSession session, CalendarEvent event) {
        if (null != event && b(session.get(CalendarParameters.PARAMETER_TRACK_ATTENDEE_USAGE, Boolean.class, Boolean.FALSE))) {
            new AttendeeUsageTracker(session.getEntityResolver()).track(event);
        }
    }

    /**
     * Prepares the organizer for an event, taking over an external organizer if specified.
     *
     * @param session The calendar session
     * @param folder The target calendar folder of the event
     * @param organizerData The organizer as defined by the client, or <code>null</code> to prepare the default organizer for the target folder
     * @return The prepared organizer
     */
    public static Organizer prepareOrganizer(CalendarSession session, CalendarFolder folder, Organizer organizerData) throws OXException {
        CalendarUser calendarUser = getCalendarUser(session, folder);
        Organizer organizer;
        if (null != organizerData) {
            organizer = session.getEntityResolver().prepare(organizerData, CalendarUserType.INDIVIDUAL);
            if (0 < organizer.getEntity()) {
                /*
                 * internal organizer must match the actual calendar user if specified
                 */
                if (organizer.getEntity() != calendarUser.getEntity()) {
                    throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(organizer.getUri(), I(organizer.getEntity()), CalendarUserType.INDIVIDUAL);
                }
            } else {
                /*
                 * take over external organizer as-is
                 */
                return isSkipExternalAttendeeURIChecks(session) ? organizer : Check.requireValidEMail(organizer);
            }
        } else {
            /*
             * prepare a default organizer for calendar user
             */
            organizer = session.getEntityResolver().applyEntityData(new Organizer(), calendarUser.getEntity());
        }
        /*
         * apply "sent-by" property if someone is acting on behalf of the calendar user
         */
        if (calendarUser.getEntity() != session.getUserId()) {
            organizer.setSentBy(session.getEntityResolver().applyEntityData(new CalendarUser(), session.getUserId()));
        }
        return organizer;
    }

    /**
     * Optionally gets a database connection set in a specific calendar session.
     *
     * @param session The calendar session to get the connection from
     * @return The connection, or <code>null</code> if not defined
     */
    public static Connection optConnection(CalendarSession session) {
        return session.get(AbstractStorageOperation.PARAM_CONNECTION, Connection.class, null);
    }

    private static DefaultEntityResolver getEntityResolver(CalendarSession session) throws OXException {
        if (DefaultEntityResolver.class.isInstance(session.getEntityResolver())) {
            return (DefaultEntityResolver) session.getEntityResolver();
        }
        return new DefaultEntityResolver(ServerSessionAdapter.valueOf(session.getSession()), Services.getServiceLookup());
    }

}
