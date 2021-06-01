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

package com.openexchange.chronos.ical.ical4j.mapping.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ical4j.extensions.Conference;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapping;
import net.fortuna.ical4j.extensions.outlook.AllDayEvent;
import net.fortuna.ical4j.extensions.outlook.BusyStatus;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * {@link EventMappings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventMappings {

	/**
	 * Holds a collection of all known event mappings.
	 */
	public static List<ICalMapping<VEvent, Event>> ALL = Collections.<ICalMapping<VEvent, Event>>unmodifiableList(Arrays.asList(
        new AttachmentMapping(),
		new AttendeeMapping(),
        new CategoriesMapping(),
        new ClassMapping(),
		new CreatedMapping(),
		new DescriptionMapping(),
        new DtEndMapping(),
		new DtStampMapping(),
		new DtStartMapping(),
        new DurationMapping(),
        new ExDateMapping(),
        new RDateMapping(),
        new GeoMapping(),
		new LastModifiedMapping(),
		new LocationMapping(),
		new OrganizerMapping(),
        new RecurrenceIdMapping(),
        new RelatedToMapping(),
        new RRuleMapping(),
		new SequenceMapping(),
		new StatusMapping(),
		new SummaryMapping(),
		new TranspMapping(),
        new UidMapping(),
        new UrlMapping(),
        new XMicrosoftAllDayEventMapping(),
        new XMicrosoftBusyStatusMapping(),
        new ConferencesMapping(),
        new ExtendedPropertiesMapping(
            Property.ATTACH, Property.ATTENDEE, Property.CATEGORIES, Property.CLASS, Property.CREATED, Property.DESCRIPTION,
            Property.DTEND, Property.DTSTAMP, Property.DTSTART, Property.DURATION, Property.EXDATE, Property.GEO, Property.LAST_MODIFIED,
            Property.LOCATION, Property.ORGANIZER, Property.RDATE, Property.RECURRENCE_ID, Property.RELATED_TO, Property.RRULE,
            Property.SEQUENCE, Property.STATUS, Property.SUMMARY, Property.TRANSP, Property.UID, Property.URL, AllDayEvent.PROPERTY_NAME,
            BusyStatus.PROPERTY_NAME, Conference.PROPERTY_NAME)
	));

    /**
     * Initializes a new {@link EventMappings}.
     */
	private EventMappings() {
		super();
	}

}
