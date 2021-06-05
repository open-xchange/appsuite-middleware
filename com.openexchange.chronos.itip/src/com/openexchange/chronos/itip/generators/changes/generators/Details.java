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

package com.openexchange.chronos.itip.generators.changes.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.itip.generators.Sentence;
import com.openexchange.chronos.itip.generators.changes.ChangeDescriptionGenerator;
import com.openexchange.chronos.itip.tools.ITipEventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link Details}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Details implements ChangeDescriptionGenerator {

    private static final EventField[] FIELDS = { EventField.SUMMARY, EventField.LOCATION, EventField.DESCRIPTION };

    private static final Map<EventField, String> MESSAGE_MAP = new HashMap<EventField, String>(4, 0.9f);
    
    static {
        MESSAGE_MAP.put(EventField.SUMMARY, Messages.HAS_CHANGED_TITLE);
        MESSAGE_MAP.put(EventField.LOCATION, Messages.HAS_CHANGED_LOCATION);
        MESSAGE_MAP.put(EventField.DESCRIPTION, Messages.HAS_CHANGED_NOTE);
        //put(TIMEZONE, Messages.HAS_CHANGED_TIMEZONE);
    }

    @Override
    public List<Sentence> getDescriptions(Context ctx, Event original, Event updated, ITipEventUpdate diff, Locale locale, TimeZone timezone) throws OXException {
        List<Sentence> changes = new ArrayList<Sentence>();
        add(EventField.SUMMARY, diff, changes, true);
        add(EventField.LOCATION, diff, changes, true);
        add(EventField.DESCRIPTION, diff, changes, false);
        //add(TIMEZONE, diff, changes, true);

        return changes;
    }

    private void add(EventField field, ITipEventUpdate diff, List<Sentence> changes, boolean includeNewValue) throws OXException {
        if (!diff.getUpdatedFields().contains(field)) {
            return;
        }

        String message = MESSAGE_MAP.get(field);

        Sentence changeDescription = new Sentence(message);
        if (includeNewValue) {
            changeDescription.add(EventMapper.getInstance().get(field).get(diff.getUpdate()), ArgumentType.UPDATED);
        }
        changes.add(changeDescription);
    }

    @Override
    public EventField[] getFields() {
        return FIELDS;
    }

}
