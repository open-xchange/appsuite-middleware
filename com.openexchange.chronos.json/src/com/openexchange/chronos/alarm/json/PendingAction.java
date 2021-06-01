/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.alarm.json;

import static com.openexchange.tools.arrays.Collections.unmodifiableSet;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.json.action.ChronosAction;
import com.openexchange.chronos.json.converter.AlarmTriggerConverter;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link PendingAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class PendingAction extends ChronosAction {

    private static final String ACTIONS_PARAM = "actions";
    private static final Set<String> REQUIRED_PARAMETERS = unmodifiableSet(PARAM_RANGE_END);
    private static final Set<String> OPTIONAL_PARAMETERS = unmodifiableSet(PARAM_RANGE_START, ACTIONS_PARAM);

    private static final Set<String> DEFAULT_ACTIONS = unmodifiableSet("DISPLAY", "AUDIO");

    /**
     * Initializes a new {@link PendingAction}.
     * @param services
     */
    protected PendingAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected Set<String> getRequiredParameters() {
        return REQUIRED_PARAMETERS;
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {

        String parameter = requestData.getParameter(ACTIONS_PARAM);
        Set<String> actions = null;
        if (parameter!=null){
            String[] splitByComma = Strings.splitByComma(parameter);
            for(int x=0; x<splitByComma.length; x++){
                splitByComma[x]=splitByComma[x].toUpperCase();
            }
            actions = unmodifiableSet(splitByComma);
        } else {
            actions = DEFAULT_ACTIONS;
        }

        List<AlarmTrigger> alarmTrigger = calendarAccess.getAlarmTriggers(actions);
        return new AJAXRequestResult(alarmTrigger, AlarmTriggerConverter.INPUT_FORMAT);
    }


}
