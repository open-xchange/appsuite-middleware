/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
