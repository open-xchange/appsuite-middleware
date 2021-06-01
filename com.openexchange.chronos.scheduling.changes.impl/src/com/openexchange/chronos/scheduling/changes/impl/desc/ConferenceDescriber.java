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

import java.util.Collections;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.ArgumentType;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.impl.ChangeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.SentenceImpl;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.groupware.tools.mappings.common.CollectionUpdate;
import com.openexchange.java.Strings;

/**
 * {@link ConferenceDescriber}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class ConferenceDescriber implements ChangeDescriber {

    /**
     * Initializes a new {@link ConferenceDescriber}.
     */
    public ConferenceDescriber() {
        super();
    }

    @Override
    @NonNull
    public EventField[] getFields() {
        return new EventField[] { EventField.CONFERENCES };
    }

    @Override
    public Description describe(EventUpdate eventUpdate) {
        CollectionUpdate<Conference, ConferenceField> conferenceUpdates = eventUpdate.getConferenceUpdates();
        if (null == conferenceUpdates || conferenceUpdates.isEmpty()) {
            return null;
        }
        int added = conferenceUpdates.getAddedItems().size();
        int updated = conferenceUpdates.getUpdatedItems().size();
        int removed = conferenceUpdates.getRemovedItems().size();
        int newSize = null == eventUpdate.getUpdate().getConferences() ? 0 : eventUpdate.getUpdate().getConferences().size();
        SentenceImpl sentence;
        if (0 == added && 0 == updated && 0 < removed && eventUpdate.getOriginal().getConferences().size() == removed) {
            /*
             * all removed
             */
            sentence = new SentenceImpl(Messages.HAS_REMOVED_CONFERENCE);
        } else if (1 == newSize && 1 == added && 1 >= removed && 0 == updated || 1 == updated) {
            /*
             * single new/updated
             */
            Conference conference = eventUpdate.getUpdate().getConferences().get(0);
            if (Strings.isNotEmpty(conference.getLabel())) {
                sentence = new SentenceImpl(Messages.HAS_CHANGED_CONFERENCE_URI_WITH_LABEL)
                    .add(conference.getLabel(), ArgumentType.UPDATED).add(conference.getUri(), ArgumentType.UPDATED);
            } else {
                sentence = new SentenceImpl(Messages.HAS_CHANGED_CONFERENCE_URI).add(conference.getUri(), ArgumentType.UPDATED);
            }            
        } else {
            /*
             * genric change, otherwise
             */
            sentence = new SentenceImpl(Messages.HAS_CHANGED_CONFERENCE);
        }
        return new DefaultDescription(Collections.singletonList(sentence), EventField.CONFERENCES);
    }

}
