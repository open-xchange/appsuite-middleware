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

package com.openexchange.ajax.mail.filter.api.conversion.writer.comparison;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.mail.filter.api.dao.MatchType;

/**
 * {@link ComparisonWriterRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ComparisonWriterRegistry {

    private static Map<MatchType, ComparisonWriter> registry = new HashMap<>(8);

    /**
     * Initialises a new {@link ComparisonWriterRegistry}.
     */
    public ComparisonWriterRegistry() {
        super();
        //registry = new HashMap<>(8);
    }

    /**
     * Adds the specified {@link ComparisonWriter} to the registry
     * 
     * @param matchType The {@link MatchType}
     * @param writer The {@link ComparisonWriter}
     */
    public static void addWriter(MatchType matchType, ComparisonWriter writer) {
        registry.put(matchType, writer);
    }

    /**
     * Retrieves the specified {@link ComparisonWriter} from the registry
     * 
     * @param matchType The {@link MatchType}
     * @return The {@link ComparisonWriter}
     */
    public static ComparisonWriter getWriter(MatchType matchType) {
        return registry.get(matchType);
    }
}
