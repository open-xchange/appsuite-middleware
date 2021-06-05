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

package com.openexchange.chronos.ical.ical4j.mapping.available;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.ical.ical4j.mapping.ICalMapping;
import net.fortuna.ical4j.model.component.Available;

/**
 * {@link AvailableMappings}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AvailableMappings {

    /**
     * Holds a collection of all known availability mappings.
     */
    //@formatter:off
    public static List<ICalMapping<Available, com.openexchange.chronos.Available>> ALL = Collections.<ICalMapping<Available, com.openexchange.chronos.Available>> unmodifiableList(Arrays.asList(
        new DtStampMapping(),
        new DtStartMapping(),
        new UidMapping(),
        new DtEndMapping(),
        new DurationMapping(),
        new CreatedMapping(),
        new DescriptionMapping(),
        new LastModifiedMapping(),
        new LocationMapping(),
        new RecurrenceIdMapping(),
        new RRuleMapping(),
        new SummaryMapping()
        //new CategoriesMapping(),
        //new CommentMapping(),
        //new ContactMapping(),
        //new ExDateMapping(),
        //new RDateMapping(),
        //new ExtendedPropertiesMapping()
        ));
    //@formatter:on

    /**
     * Initialises a new {@link AvailableMappings}.
     */
    public AvailableMappings() {
        super();
    }
}
