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
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
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
