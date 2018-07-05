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

package com.openexchange.chronos.common;

import static com.openexchange.chronos.common.DataHandlers.ALARM2JSON;
import static com.openexchange.chronos.common.DataHandlers.AVAILABLE2JSON;
import static com.openexchange.chronos.common.DataHandlers.JSON2ALARM;
import static com.openexchange.chronos.common.DataHandlers.JSON2AVAILABLE;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Available;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;

/**
 * {@link UserConfigWrapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UserConfigWrapper {

    private static final String KEY_DEFAULT_ALARM_DATE = "defaultAlarmDate";
    private static final String KEY_DEFAULT_ALARM_DATE_TIME = "defaultAlarmDateTime";
    private static final String KEY_AVAILABILITY = "availability";

    private final ConversionService conversionService;
    private final JSONObject userConfig;

    /**
     * Initializes a new {@link UserConfigWrapper}.
     *
     * @param conversionService A reference to the conversion service
     * @param userConfig The underlying configuration
     */
    public UserConfigWrapper(ConversionService conversionService, JSONObject userConfig) {
        super();
        this.conversionService = conversionService;
        this.userConfig = userConfig;
    }

    /**
     * Gets the default alarms to be applied to events whose start-date is of type <i>date</i> from the underlying user configuration.
     *
     * @return The default alarms, or <code>null</code> if not defined
     */
    public List<Alarm> getDefaultAlarmDate() throws OXException {
        return deserializeAlarms(userConfig.optJSONArray(KEY_DEFAULT_ALARM_DATE));
    }

    /**
     * Sets the default alarms to be applied to events whose start-date is of type <i>date</i> in the underlying user configuration.
     *
     * @param alarms The alarms to set, or <code>null</code> to remove the value
     * @return <code>true</code> if the underlying configuration was modified, <code>false</code>, otherwise
     */
    public boolean setDefaultAlarmDate(List<Alarm> alarms) throws OXException {
        if (null == alarms) {
            return null != userConfig.remove(KEY_DEFAULT_ALARM_DATE);
        }
        JSONArray value = serializeAlarms(alarms);
        JSONArray oldValue = userConfig.optJSONArray(KEY_DEFAULT_ALARM_DATE);
        userConfig.putSafe(KEY_DEFAULT_ALARM_DATE, value);
        return false == Objects.equals(value, oldValue);
    }

    /**
     * Gets the default alarms to be applied to events whose start-date is of type <i>date-time</i> from the underlying user configuration.
     *
     * @return The default alarms, or <code>null</code> if not defined
     */
    public List<Alarm> getDefaultAlarmDateTime() throws OXException {
        return deserializeAlarms(userConfig.optJSONArray("defaultAlarmDateTime"));
    }

    /**
     * Sets the default alarm to be applied to events whose start-date is of type <i>date-time</i> in the underlying user configuration.
     *
     * @param alarm The alarm to set, or <code>null</code> to remove the value
     * @return <code>true</code> if the underlying configuration was modified, <code>false</code>, otherwise
     */
    public boolean setDefaultAlarmDateTime(List<Alarm> alarms) throws OXException {
        if (null == alarms) {
            return null != userConfig.remove(KEY_DEFAULT_ALARM_DATE_TIME);
        }
        JSONArray value = serializeAlarms(alarms);
        JSONArray oldValue = userConfig.optJSONArray(KEY_DEFAULT_ALARM_DATE_TIME);
        userConfig.putSafe(KEY_DEFAULT_ALARM_DATE_TIME, value);
        return false == Objects.equals(value, oldValue);
    }

    /**
     * Gets the defined availability (in form of one or more available definitions) from the underlying user configuration.
     *
     * @return The availability, or <code>null</code> if not defined
     */
    public Available[] getAvailability() throws OXException {
        return deserializeAvailability(userConfig.optJSONArray(KEY_AVAILABILITY));
    }

    /**
     * Sets the availability (in form of one or more available definitions) in the underlying user configuration.
     *
     * @param availability The availability to set, or <code>null</code> to remove the value
     * @return <code>true</code> if the underlying configuration was modified, <code>false</code>, otherwise
     */
    public boolean setAvailability(Available[] availability) throws OXException {
        if (null == availability) {
            return null != userConfig.remove(KEY_AVAILABILITY);
        }
        JSONArray value = serializeAvailability(availability);
        JSONArray oldValue = userConfig.optJSONArray(KEY_AVAILABILITY);
        userConfig.putSafe(KEY_AVAILABILITY, value);
        return false == Objects.equals(value, oldValue);
    }

    private List<Alarm> deserializeAlarms(JSONArray jsonArray) throws OXException {
        if (null == jsonArray) {
            return null;
        }
        List<Alarm> alarms = new ArrayList<Alarm>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            alarms.add(deserializeAlarm(jsonArray.optJSONObject(i)));
        }
        return alarms;
    }

    private JSONArray serializeAlarms(List<Alarm> alarms) throws OXException {
        if (null == alarms) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(alarms.size());
        for (Alarm alarm : alarms) {
            jsonArray.put(serializeAlarm(alarm));
        }
        return jsonArray;
    }

    private JSONObject serializeAlarm(Alarm alarm) throws OXException {
        if (null == alarm) {
            return null;
        }
        DataHandler dataHandler = conversionService.getDataHandler(ALARM2JSON);
        ConversionResult result = dataHandler.processData(new SimpleData<Alarm>(alarm), new DataArguments(), null);
        return (JSONObject) result.getData();
    }

    private Alarm deserializeAlarm(JSONObject jsonObject) throws OXException {
        if (null == jsonObject) {
            return null;
        }
        DataHandler dataHandler = conversionService.getDataHandler(JSON2ALARM);
        ConversionResult result = dataHandler.processData(new SimpleData<JSONObject>(jsonObject), new DataArguments(), null);
        return (Alarm) result.getData();
    }

    private JSONArray serializeAvailability(Available[] availability) throws OXException {
        if (null == availability) {
            return null;
        }
        DataHandler dataHandler = conversionService.getDataHandler(AVAILABLE2JSON);
        ConversionResult result = dataHandler.processData(new SimpleData<Available[]>(availability), new DataArguments(), null);
        return (JSONArray) result.getData();
    }

    private Available[] deserializeAvailability(JSONArray jsonArray) throws OXException {
        if (null == jsonArray) {
            return null;
        }
        DataHandler dataHandler = conversionService.getDataHandler(JSON2AVAILABLE);
        ConversionResult result = dataHandler.processData(new SimpleData<JSONArray>(jsonArray), new DataArguments(), null);
        return (Available[]) result.getData();
    }

}
