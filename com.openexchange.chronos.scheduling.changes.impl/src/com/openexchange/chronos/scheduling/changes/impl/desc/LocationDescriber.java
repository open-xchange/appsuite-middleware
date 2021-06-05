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

package com.openexchange.chronos.scheduling.changes.impl.desc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.impl.ChangeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.java.Strings;

/**
 * {@link LocationDescriber}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class LocationDescriber implements ChangeDescriber {

    /**
     * Initializes a new {@link LocationDescriber}.
     */
    public LocationDescriber() {
        super();
    }

    @Override
    @NonNull
    public EventField[] getFields() {
        return new EventField[] { EventField.GEO, EventField.LOCATION };
    }

    @Override
    public Description describe(EventUpdate eventUpdate) {
        boolean changedLocation = eventUpdate.getUpdatedFields().contains(EventField.LOCATION);
        boolean changedGeo = eventUpdate.getUpdatedFields().contains(EventField.GEO);
        if (false == changedLocation && false == changedGeo) {
            return null;
        }

        /*
         * Create data for the message, e.g. "Berlin, 52.520008, 13.404954"
         */
        StringBuilder sb = new StringBuilder();
        if (changedLocation && Strings.isNotEmpty(eventUpdate.getUpdate().getLocation())) {
            sb.append(eventUpdate.getUpdate().getLocation());
        }
        if (changedGeo) {
            String description = describeGeo(eventUpdate);
            if (Strings.isNotEmpty(description)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(description);
            }
        }

        /*
         * Check that we described a change. If not, Geo or location has been removed.
         */
        SentenceImpl sentence;
        if (sb.length() > 0) {
            sentence = new SentenceImpl(Messages.HAS_CHANGED_LOCATION).add(sb.toString(), ArgumentType.UPDATED);
        } else {
            sentence = new SentenceImpl(Messages.HAS_REMOVED_LOCATION);
        }
        return new DefaultDescription(Collections.singletonList(sentence), getEventFields(changedGeo, changedLocation));
    }

    private static String describeGeo(EventUpdate eventUpdate) {
        double[] coordinates = eventUpdate.getUpdate().getGeo();
        if (null != coordinates && coordinates.length == 2) {
            return Double.toString(coordinates[0]) + ", " + Double.toString(coordinates[1]);
        }
        return null;
    }

    private List<EventField> getEventFields(boolean changedGeo, boolean changedLocation) {
        ArrayList<EventField> fields = new ArrayList<EventField>(2);
        if (changedGeo) {
            fields.add(EventField.GEO);
        }
        if (changedLocation) {
            fields.add(EventField.LOCATION);
        }
        return fields;
    }

}
