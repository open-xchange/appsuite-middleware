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
 * An {@link AnalyzerElement} matches a given filter
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AnalyzerElement {

    private final List<AnalyzerElement> children = new ArrayList<AnalyzerElement>();
    protected boolean capturing;

    protected abstract boolean applyAndExtract(Filter filter, List<Object> extracted);
    protected abstract boolean apply(Filter filter);

    public void addChildAnalyzer(AnalyzerElement analyzer) {
        children.add(analyzer);
    }

    public boolean matches(Filter filter, List<Object> extracted) {
        boolean match = false;
        if (capturing) {
            match = applyAndExtract(filter, extracted);
        } else {
            match = apply(filter);
        }
        if (!match) {
            return false;
        }

        List<AnalyzerElement> copy = new ArrayList<AnalyzerElement>(children);
        for (Filter childFilter : filter.getFilters()) {
            boolean matchedOnce = false;
            for (AnalyzerElement analyzer : new ArrayList<AnalyzerElement>(copy)) {
                if (analyzer.matches(childFilter, extracted)) {
                    matchedOnce = true;
                    copy.remove(analyzer);
                }
            }
            if (!matchedOnce) {
                return false;
            }
        }
        return match;
    }

    public void setCapturing(boolean b) {
        capturing = b;
    }

}
