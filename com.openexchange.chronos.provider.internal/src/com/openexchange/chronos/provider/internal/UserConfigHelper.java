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

package com.openexchange.chronos.provider.internal;

import static com.openexchange.chronos.common.DataHandlers.ALARM2JSON;
import static com.openexchange.chronos.common.DataHandlers.AVAILABLE2JSON;
import static com.openexchange.chronos.common.DataHandlers.JSON2ALARM;
import static com.openexchange.chronos.common.DataHandlers.JSON2AVAILABLE;
import static com.openexchange.chronos.provider.internal.Constants.CONTENT_TYPE;
import static com.openexchange.chronos.provider.internal.Constants.TREE_ID;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobService;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UserConfigHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UserConfigHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserConfigHelper.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link UserConfigHelper}.
     *
     * @param services A service lookup reference
     */
    public UserConfigHelper(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Checks the user configuration as passed by the client during prior applying it.
     *
     * @param session The session
     * @param userConfig The user configuration to check for validity
     */
    public void checkUserConfig(ServerSession session, JSONObject userConfig) throws OXException {
        if (null == userConfig) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("null");
        }
        /*
         * check default alarms
         */
        checkAlarm(session, userConfig.optJSONObject("defaultAlarmDate"));
        checkAlarm(session, userConfig.optJSONObject("defaultAlarmDateTime"));
        /*
         * check availability
         */
        checkAvailability(session, userConfig.optJSONArray("availability"));
    }

    /**
     * Takes over all known <i>legacy</i> calendar settings into a user configuration object.
     *
     * @param session The session
     * @param userConfig The user configuration to apply the legacy settings in
     */
    public void applyLegacyConfig(ServerSession session, JSONObject userConfig) {
        try {
            /*
             * default alarm
             */
            userConfig.putOpt("defaultAlarmDateTime", optDefaultAlarm(session));
            /*
             * default folder id
             */
            userConfig.putOpt("defaultFolderId", optDefaultFolderId(session));
            /*
             * availability
             */
            userConfig.putOpt("availability", optAvailability(session));
            /*
             * notification settings
             */
            UserSettingMail userSettingMail = UserSettingMailStorage.getInstance().getUserSettingMail(session);
            if (userSettingMail != null) {
                userConfig.put("notifyNewModifiedDeleted", userSettingMail.isNotifyAppointments());
                userConfig.put("notifyAcceptedDeclinedAsCreator", userSettingMail.isNotifyAppointmentsConfirmOwner());
                userConfig.put("notifyAcceptedDeclinedAsParticipant", userSettingMail.isNotifyAppointmentsConfirmParticipant());
            }
        } catch (OXException | JSONException e) {
            LOG.warn("Error applying legacy calendar settings for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
        }
    }

    private String optDefaultFolderId(ServerSession session) {
        try {
            UserizedFolder defaultFolder = requireService(FolderService.class, services).getDefaultFolder(session.getUser(), TREE_ID, CONTENT_TYPE, PrivateType.getInstance(), session, null);
            return defaultFolder.getID();
        } catch (OXException e) {
            LOG.warn("Error getting default folder for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
        }
        return null;
    }

    private JSONArray optAvailability(ServerSession session) {
        boolean INSERT_DEFAULT_AVAILABILITY = false;
        if (false == INSERT_DEFAULT_AVAILABILITY) {
            return null;
        }
        //        JSlob jsLob = optJSlob(session, "io.ox/calendar");
        //        if (null != jsLob) {
        //            try {
        //                int startTime = jsLob.getJsonObject().getInt("startTime");
        //                startTime = Math.min(23, Math.max(0, startTime));
        //                int endTime = jsLob.getJsonObject().getInt("endTime");
        //                endTime = Math.min(23, Math.max(0, endTime));
        //                int workweekStart = jsLob.getJsonObject().getInt("workweekStart") + 1;
        //                workweekStart = Math.min(7, Math.max(1, workweekStart));
        //                int numDaysWorkweek = jsLob.getJsonObject().getInt("numDaysWorkweek");
        //                numDaysWorkweek = Math.min(7, Math.max(1, workweekStart));
        //
        //                Available available = new Available();
        //                available.setCreationTimestamp(new Date());
        //                Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, null);
        //                for (int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); workweekStart != dayOfWeek; calendar.add(Calendar.DATE, -1))
        //                    ;
        //                List<WeekdayNum> weekDays = new ArrayList<WeekdayNum>(numDaysWorkweek);
        //                for (int i = 0; i < numDaysWorkweek; i++) {
        //
        ////                    new WeekdayNum(0, Weekday.v)
        ////                    weekDays.add(new wee)
        //                }
        //                RecurrenceRule rule = new RecurrenceRule(Freq.WEEKLY);
        ////                rule.set
        ////
        ////
        ////                DateTime startTime = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), startHour, 0, 0);
        ////                DateTime endTime = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), endHour, 0, 0);
        ////                available.setStartTime(startTime);
        ////                available.setEndTime(endTime);
        ////                available.setUid(UUID.randomUUID().toString());
        ////                available.setRecurrenceRule("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR");
        ////                try {
        ////                    return serializeAvailability(session, Collections.singletonList(available));
        ////                } catch (OXException e) {
        ////                    LOG.warn("Error inserting default availability for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
        ////                }
        ////
        //
        //            } catch (JSONException e) {
        //                LOG.warn("Error converting default alarm from JSlob for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
        //            }
        //        }

        return null;
    }

    private JSONObject optDefaultAlarm(ServerSession session) {
        JSlob jsLob = optJSlob(session, "io.ox/calendar");
        if (null != jsLob) {
            if (jsLob.getJsonObject().hasAndNotNull("defaultReminder")) {
                try {
                    int reminderMinutes = jsLob.getJsonObject().getInt("defaultReminder");
                    if (-1 == reminderMinutes) {
                        return null;
                    }
                    String duration = AlarmUtils.getDuration(true, 0, 0, 0, reminderMinutes, 0);
                    Alarm alarm = new Alarm(new Trigger(duration), AlarmAction.DISPLAY);
                    alarm.setDescription("Reminder");
                    return serializeAlarm(session, alarm);
                } catch (JSONException | OXException e) {
                    LOG.warn("Error converting default alarm from JSlob for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
                }
            }
        }
        return null;
    }

    private JSlob optJSlob(Session session, String id) {
        try {
            JSlobService jslobService = services.getOptionalService(JSlobService.class);
            if (null == jslobService) {
                throw ServiceExceptionCode.absentService(JSlobService.class);
            }
            return jslobService.get(id, session);
        } catch (OXException e) {
            LOG.warn("Error getting JSlob {} for user {} in context {}", id, I(session.getUserId()), I(session.getContextId()), e);
        }
        return null;
    }

    private JSONArray checkAvailability(ServerSession session, JSONArray availabilityJsonArray) throws OXException {
        if (null == availabilityJsonArray || JSONObject.NULL.equals(availabilityJsonArray)) {
            return null;
        }
        ConversionService conversionService = requireService(ConversionService.class, services);
        DataHandler dataHandler = conversionService.getDataHandler(JSON2AVAILABLE);
        ConversionResult result = dataHandler.processData(new SimpleData<JSONArray>(availabilityJsonArray), new DataArguments(), session);
        if (null == result || null == result.getData() || false == List.class.isInstance(result.getData())) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(String.valueOf(availabilityJsonArray));
        }
        List<Available> availability = (List<Available>) result.getData();
        return serializeAvailability(session, availability);
    }

    private JSONObject checkAlarm(ServerSession session, JSONObject alarmJsonObject) throws OXException {
        if (null == alarmJsonObject || JSONObject.NULL.equals(alarmJsonObject)) {
            return null;
        }
        ConversionService conversionService = requireService(ConversionService.class, services);
        DataHandler dataHandler = conversionService.getDataHandler(JSON2ALARM);
        ConversionResult result = dataHandler.processData(new SimpleData<JSONObject>(alarmJsonObject), new DataArguments(), session);
        if (null == result || null == result.getData() || false == Alarm.class.isInstance(result.getData())) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(String.valueOf(alarmJsonObject));
        }
        return serializeAlarm(session, Check.alarmIsValid((Alarm) result.getData()));
    }

    private JSONObject serializeAlarm(ServerSession session, Alarm alarm) throws OXException {
        ConversionService conversionService = requireService(ConversionService.class, services);
        DataHandler dataHandler = conversionService.getDataHandler(ALARM2JSON);
        ConversionResult result = dataHandler.processData(new SimpleData<Alarm>(alarm), new DataArguments(), session);
        return (JSONObject) result.getData();
    }

    private JSONArray serializeAvailability(ServerSession session, List<Available> availability) throws OXException {
        ConversionService conversionService = requireService(ConversionService.class, services);
        DataHandler dataHandler = conversionService.getDataHandler(AVAILABLE2JSON);
        ConversionResult result = dataHandler.processData(new SimpleData<List<Available>>(availability), new DataArguments(), session);
        return (JSONArray) result.getData();
    }

}
