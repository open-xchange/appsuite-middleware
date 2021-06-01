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

package com.openexchange.switchboard.zoom;

import static com.openexchange.chronos.common.CalendarUtils.optExtendedParameterValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.Event;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public class Utils {

    private static final String TYPE_PARAMETER = "X-OX-TYPE";
    private static final String ID_PARAMETER = "X-OX-ID";
    private static final String PROVIDER_ZOOM = "zoom";

    static List<Conference> getZoomConferences(Event event) {
        List<Conference> conferences = event.getConferences();
        if (conferences == null || conferences.isEmpty()) {
            return Collections.emptyList();
        }

        List<Conference> retval = new ArrayList<>();
        for (Conference conference : event.getConferences()) {
            if (PROVIDER_ZOOM.equals(optExtendedParameterValue(conference.getExtendedParameters(), TYPE_PARAMETER))) {
                retval.add(conference);
            }
        }

        return retval;
    }

    /**
     * 
     * Checks, if the two given conferences match according to their zoom id
     *
     * @param conf1 The first conference
     * @param conf2 The second conference
     * @return true if the zoom ids match, false otherwise
     */
    static boolean matches(Conference conf1, Conference conf2) {
        if (conf1 == null || conf2 == null) {
            return false;
        }

        String param1 = optExtendedParameterValue(conf1.getExtendedParameters(), ID_PARAMETER);
        return null != param1 && param1.equals(optExtendedParameterValue(conf2.getExtendedParameters(), ID_PARAMETER));
    }

}
