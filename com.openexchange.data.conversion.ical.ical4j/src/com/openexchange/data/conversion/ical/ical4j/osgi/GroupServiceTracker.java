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

package com.openexchange.data.conversion.ical.ical4j.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.group.GroupService;

/**
 * {@link GroupServiceTracker}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class GroupServiceTracker implements ServiceTrackerCustomizer<GroupService, GroupService> {

    private final BundleContext context;

    /**
     * Initializes a new {@link GroupServiceTracker}.
     *
     * @param context The bundle context
     */
    public GroupServiceTracker(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public GroupService addingService(ServiceReference<GroupService> reference) {
        GroupService service = context.getService(reference);
        Participants.setGroupService(service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<GroupService> reference, GroupService service) {
        // no
    }

    @Override
    public void removedService(ServiceReference<GroupService> reference, GroupService service) {
        Participants.setGroupService(null);
        context.ungetService(reference);
    }

}
