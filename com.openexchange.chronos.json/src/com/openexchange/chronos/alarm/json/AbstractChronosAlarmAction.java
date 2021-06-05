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

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.json.action.ChronosAction;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AbstractChronosAlarmAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public abstract class AbstractChronosAlarmAction extends ChronosAction {


    /**
     * Initializes a new {@link AbstractChronosAlarmAction}.
     * @param services
     */
    protected AbstractChronosAlarmAction(ServiceLookup services) {
        super(services);
    }

    public Object parseAlarmParameter(AJAXRequestData request, String parameter, boolean required) throws OXException {

        String value = request.getParameter(parameter);
        if (Strings.isEmpty(value)) {
            if (false == required) {
                return null;
            }
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameter);
        }
        try {
            switch (parameter) {
                case AlarmParameters.PARAMETER_ALARM_ID:
                    return Integer.valueOf(value);
                case AlarmParameters.PARAMETER_SNOOZE_DURATION:
                    return Long.valueOf(value);
                default:
                    if (false == required) {
                        return null;
                    }
                    throw AjaxExceptionCodes.INVALID_PARAMETER.create(parameter);
            }
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, parameter, value);
        }
    }
}
