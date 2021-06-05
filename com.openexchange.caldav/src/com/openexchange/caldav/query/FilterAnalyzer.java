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

package com.openexchange.caldav.query;

import java.util.ArrayList;
import java.util.List;


/**
 * A {@link FilterAnalyzer} is a sort of regular expression for a calendar query. It finds out whether a given filter matches
 * the structure of this builder and optionally extracts certain attributes of the query.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FilterAnalyzer {

    public static final FilterAnalyzer VEVENT_RANGE_QUERY_ANALYZER =
        new FilterAnalyzerBuilder().compFilter("VCALENDAR").compFilter("VEVENT").timeRange().capture().end().end().end().build();

    public static final FilterAnalyzer VTODO_RANGE_QUERY_ANALYZER =
        new FilterAnalyzerBuilder().compFilter("VCALENDAR").compFilter("VTODO").timeRange().capture().end().end().end().build();

    private final List<AnalyzerElement> analyzers;

    public FilterAnalyzer(List<AnalyzerElement> analyzers) {
        super();
        this.analyzers = analyzers;
    }

    public boolean match(Filter filter, List<Object> arguments) {
        List<AnalyzerElement> copy = new ArrayList<AnalyzerElement>(analyzers);
        for (Filter childFilter : filter.getFilters()) {
            boolean matchedOnce = false;
            for (AnalyzerElement analyzer : new ArrayList<AnalyzerElement>(copy)) {
                List<Object> extracted = new ArrayList<Object>(5);
                if (analyzer.matches(childFilter, extracted)) {
                    arguments.addAll(extracted);
                    matchedOnce = true;
                    copy.remove(analyzer);
                }
            }
            if (!matchedOnce) {
                return false;
            }
        }
        return true;
    }

}
