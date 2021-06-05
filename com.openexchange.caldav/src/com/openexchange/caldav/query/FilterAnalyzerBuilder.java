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
import java.util.Stack;


/**
 * A {@link FilterAnalyzerBuilder} is a builder for a {@link FilterAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FilterAnalyzerBuilder {
    private final List<AnalyzerElement> analyzers = new ArrayList<AnalyzerElement>();

    private AnalyzerElement top;
    private final Stack<AnalyzerElement> stack = new Stack<AnalyzerElement>();


    public FilterAnalyzerBuilder compFilter(String name) {
        CompAnalyzer compAnalyzer = new CompAnalyzer(name);
        if (top != null) {
            top.addChildAnalyzer(compAnalyzer);
            stack.push(top);
        }
        top = compAnalyzer;
        return this;
    }

    public FilterAnalyzerBuilder timeRange() {
        TimeRangeAnalyzer analyzer = new TimeRangeAnalyzer();
        if (top != null) {
            top.addChildAnalyzer(analyzer);
            stack.push(top);
        }
        top = analyzer;
        return this;
    }

    public FilterAnalyzerBuilder capture() {
        if (top != null) {
            top.setCapturing(true);
        }
        return this;
    }

    public FilterAnalyzerBuilder end() {
        if (stack.isEmpty()) {
            analyzers.add(top);
            top = null;
        } else {
            top = stack.pop();
        }
        return this;
    }

    public FilterAnalyzer build() {
        return new FilterAnalyzer(analyzers);
    }

}
