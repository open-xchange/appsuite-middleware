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

package com.openexchange.find.facet;

import java.util.LinkedList;
import java.util.List;


/**
 * {@link FilterBuilder}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class FilterBuilder {

    private String id;

    private String displayName;

    private final List<String> fields;

    private final List<String> queries;

    public FilterBuilder() {
        super();
        id = null;
        displayName = null;
        fields = new LinkedList<String>();
        queries = new LinkedList<String>();
    }

    public FilterBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public FilterBuilder withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public FilterBuilder addField(String field) {
        fields.add(field);
        return this;
    }

    public FilterBuilder addQuery(String query) {
        queries.add(query);
        return this;
    }

    public Filter build() {
        return new Filter(id, displayName, fields, queries);
    }

}
