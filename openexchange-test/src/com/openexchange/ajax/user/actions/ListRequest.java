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

package com.openexchange.ajax.user.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;

/**
 * {@link ListRequest}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ListRequest extends AbstractUserRequest<ListResponse> {

    private final int[] userIds;

    private final int[] columns;

    private final Map<String, Set<String>> attributeParameters;

    public ListRequest(final int[] userIds, final int[] columns) {
        super();
        this.userIds = userIds;
        this.columns = columns;
        attributeParameters = new HashMap<String, Set<String>>(4);
    }

    /**
     * Adds an attribute parameter; e.g prefix="com.custom.tpl",name="address" would be attribute "com.custom.tpl/address".
     *
     * @param prefix The prefix
     * @param name The name
     */
    public void putAttributeParameter(final String prefix, final String name) {
        Set<String> set = attributeParameters.get(prefix);
        if (null == set) {
            set = new HashSet<String>(4);
            attributeParameters.put(prefix, set);
        }
        set.add(name);
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONArray json = new JSONArray();
        for (final int userId : userIds) {
            json.put(userId);
        }
        return json;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> l = new ArrayList<Parameter>(6);
        l.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST));
        l.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (!attributeParameters.isEmpty()) {
            for (final Entry<String, Set<String>> entry : attributeParameters.entrySet()) {
                l.add(new Parameter(entry.getKey(), toCSV(entry.getValue())));
            }
        }
        return l.toArray(new Parameter[l.size()]);
    }

    private static String toCSV(final Collection<String> c) {
        final Iterator<String> iterator = c.iterator();
        if (iterator.hasNext()) {
            final StringBuilder sb = new StringBuilder(32);
            sb.append(iterator.next());
            while (iterator.hasNext()) {
                sb.append(',').append(iterator.next());
            }
            return sb.toString();
        }
        return "";
    }

    @Override
    public ListParser getParser() {
        return new ListParser(true, columns);
    }
}
