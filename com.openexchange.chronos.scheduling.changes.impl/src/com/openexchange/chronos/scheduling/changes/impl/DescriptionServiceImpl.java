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

package com.openexchange.chronos.scheduling.changes.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.scheduling.changes.Description;
import com.openexchange.chronos.scheduling.changes.DescriptionService;
import com.openexchange.chronos.scheduling.changes.impl.desc.AttachmentDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.AttendeeDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.ConferenceDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.DescriptionDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.LocationDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.OrganizerDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.RRuleDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.ReschedulingDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.SplitDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.SummaryDescriber;
import com.openexchange.chronos.scheduling.changes.impl.desc.TransparencyDescriber;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DescriptionServiceImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class DescriptionServiceImpl implements DescriptionService {

    private final List<ChangeDescriber> describers;

    /**
     * Initializes a new {@link DescriptionServiceImpl}.
     *
     * @param services A service lookup reference
     */
    public DescriptionServiceImpl(ServiceLookup services) {
        super();
        this.describers = initDescribers(services);
    }

    @Override
    public List<Description> describe(EventUpdate eventUpdate, EventField... ignorees) {
        List<Description> descriptions = new LinkedList<>();
        for (ChangeDescriber describer : describers) {
            if ((null == ignorees || false == contains(describer.getFields(), ignorees)) && eventUpdate.containsAnyChangeOf(describer.getFields())) {
                Description description = describer.describe(eventUpdate);
                if (null != description) {
                    descriptions.add(description);
                }
            }
        }
        return descriptions;
    }

    @Override
    public List<Description> describeOnly(EventUpdate eventUpdate, EventField... toDescribe) {
        if (toDescribe == null || eventUpdate == null) {
            return Collections.emptyList();
        }
        List<Description> descriptions = new LinkedList<>();
        for (ChangeDescriber describer : describers) {
            if (contains(describer.getFields(), toDescribe) && eventUpdate.containsAnyChangeOf(describer.getFields())) {
                Description description = describer.describe(eventUpdate);
                if (null != description) {
                    descriptions.add(description);
                }
            }
        }
        return descriptions;
    }

    private static boolean contains(EventField[] describedFields, EventField... fieldsToMatch) {
        for (EventField f : describedFields) {
            for (EventField i : fieldsToMatch) {
                if (f.equals(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<ChangeDescriber> initDescribers(ServiceLookup services) {
        //@formatter:off
        return ImmutableList.<ChangeDescriber> of(
            new SplitDescriber(),
            new RRuleDescriber(services),
            new ReschedulingDescriber(),
            new OrganizerDescriber(),
            new SummaryDescriber(),
            new LocationDescriber(),
            new ConferenceDescriber(),
            new DescriptionDescriber(),
            new TransparencyDescriber(),
            new AttachmentDescriber(),
            new AttendeeDescriber()
        );
        //@formatter:on
    }

}
