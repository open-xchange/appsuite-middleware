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

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ical4j.ICal4JEmitter;
import com.openexchange.data.conversion.ical.ical4j.ICal4JParser;
import com.openexchange.data.conversion.ical.ical4j.internal.OXResourceResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.OXUserResolver;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.CreatedBy;
import com.openexchange.data.conversion.ical.ical4j.internal.calendar.Participants;
import com.openexchange.group.GroupService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.resource.ResourceService;
import com.openexchange.user.UserService;

/**
 * Publishes the iCal4j parser and emitter services.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Activator extends HousekeepingActivator {

    private static final String ICAL_UPDATE_TIMEZONES = "com.openexchange.ical.updateTimezones";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ConfigurationService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final OXUserResolver userResolver = new OXUserResolver();
        track(UserService.class, new UserServiceTrackerCustomizer(context, userResolver));
        Participants.userResolver = userResolver;
        CreatedBy.userResolver = userResolver;

        Reloadable reloadable = new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                String updateTimezones = configService.getProperty(ICAL_UPDATE_TIMEZONES, "true");
                System.setProperty("net.fortuna.ical4j.timezone.update.enabled", updateTimezones);
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties(ICAL_UPDATE_TIMEZONES);
            }
        };
        ConfigurationService configurationService = getService(ConfigurationService.class);
        reloadable.reloadConfiguration(configurationService);
        registerService(Reloadable.class, reloadable);

        final OXResourceResolver resourceResolver = new OXResourceResolver();
        track(ResourceService.class, new ResourceServiceTrackerCustomizer(context, resourceResolver));
        Participants.resourceResolver = resourceResolver;

        track(GroupService.class, new GroupServiceTracker(context));
        openTrackers();

        ICal4JParser parser = new ICal4JParser();
        parser.setLimit(configurationService.getIntProperty("com.openexchange.import.ical.limit", -1));
		registerService(ICalParser.class, parser, null);
        registerService(ICalEmitter.class, new ICal4JEmitter(), null);
    }
}
