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

package com.openexchange.chronos.provider.internal.config;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.UserConfigWrapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.conversion.ConversionService;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
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
     * Checks the user configuration as passed by the client prior applying it.
     *
     * @param session The session
     * @param userConfig The user configuration to check for validity
     */
    public void checkUserConfig(ServerSession session, JSONObject userConfig) throws OXException {
        if (null == userConfig) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("null");
        }
        UserConfigWrapper configWrapper = new UserConfigWrapper(requireService(ConversionService.class, services), userConfig);
        try {
            /*
             * check default alarms
             */
            List<Alarm> defaultAlarmDate = configWrapper.getDefaultAlarmDate();
            if (null != defaultAlarmDate) {
                Check.alarmsAreValid(defaultAlarmDate);
                Check.haveReleativeTriggers(defaultAlarmDate);
            }
            List<Alarm> defaultAlarmDateTime = configWrapper.getDefaultAlarmDateTime();
            if (null != defaultAlarmDateTime) {
                Check.alarmsAreValid(defaultAlarmDateTime);
                Check.haveReleativeTriggers(defaultAlarmDateTime);
            }
            /*
             * check availability
             */
            Available[] availability = configWrapper.getAvailability();
            if (null != availability) {
                Check.availabilityIsValid(requireService(RecurrenceService.class, services), availability);
            }
        } catch (OXException e) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create(e, String.valueOf(userConfig));
        }
    }

    /**
     * Takes over all known <i>legacy</i> calendar settings into a user configuration object.
     *
     * @param session The session
     * @param userConfig The user configuration to apply the legacy settings in
     */
    public void applyLegacyConfig(ServerSession session, JSONObject userConfig) throws OXException {
        /*
         * migrate default alarm from legacy reminder minutes setting
         */
        Alarm defaultAlarmDateTime = optLegacyDefaultAlarm(session);
        if (null != defaultAlarmDateTime) {
            new UserConfigWrapper(requireService(ConversionService.class, services), userConfig).setDefaultAlarmDateTime(Collections.singletonList(defaultAlarmDateTime));
        }
    }

    private Alarm optLegacyDefaultAlarm(ServerSession session) {
        JSlob jsLob = optJSlob(session, "io.ox/calendar");
        if (null != jsLob && jsLob.getJsonObject().hasAndNotNull("defaultReminder")) {
            try {
                int reminderMinutes = jsLob.getJsonObject().getInt("defaultReminder");
                if (-1 == reminderMinutes) {
                    return null;
                }
                String duration = AlarmUtils.getDuration(true, 0, 0, 0, reminderMinutes, 0);
                Alarm alarm = new Alarm(new Trigger(duration), AlarmAction.DISPLAY);
                alarm.setDescription("Reminder");
                return alarm;
            } catch (JSONException e) {
                LOG.warn("Error converting default alarm from JSlob for user {} in context {}", I(session.getUserId()), I(session.getContextId()), e);
            }
        }
        return null;
    }

    private JSlob optJSlob(ServerSession session, String id) {
        try {
            JSlobService jslobService = services.getOptionalService(JSlobService.class);
            if (null == jslobService) {
                throw ServiceExceptionCode.absentService(JSlobService.class);
            }
            JSlobStorage jsLobStorage = services.getOptionalService(JSlobStorage.class);
            if (null == jsLobStorage) {
                throw ServiceExceptionCode.absentService(JSlobStorage.class);
            }
            return jsLobStorage.opt(new JSlobId(jslobService.getIdentifier(), id, session.getUserId(), session.getContextId()));
        } catch (OXException e) {
            LOG.warn("Error getting JSlob {} for user {} in context {}", id, I(session.getUserId()), I(session.getContextId()), e);
        }
        return null;
    }

}
