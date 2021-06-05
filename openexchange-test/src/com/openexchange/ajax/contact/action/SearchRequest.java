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

package com.openexchange.ajax.contact.action;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class SearchRequest extends AbstractContactRequest<SearchResponse> {

    private final JSONObject body = new JSONObject();

    private final SearchParser searchParser;

    private final List<AJAXRequest.Parameter> params = new ArrayList<AJAXRequest.Parameter>();

    public SearchRequest(final String pattern, final int inFolder, final int[] columns, final boolean failOnError) {
        this(pattern, inFolder, columns, -1, null, failOnError);
    }

    public SearchRequest(final String pattern, final boolean firstLetterOnly, final int inFolder, final int[] columns, final int orderBy, final String orderDir, final boolean failOnError) {
        this(pattern, firstLetterOnly, inFolder, columns, orderBy, orderDir, null, failOnError);
    }

    public SearchRequest(final String pattern, final boolean firstLetterOnly, final int inFolder, final int[] columns, final int orderBy, final String orderDir, final String collation, final boolean failOnError) {
        searchParser = new SearchParser(failOnError, columns);

        param(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
        param(AJAXServlet.PARAMETER_COLUMNS, join(columns));
        if (orderBy != -1) {
            param(AJAXServlet.PARAMETER_SORT, String.valueOf(orderBy));
        }
        if (orderDir != null) {
            param(AJAXServlet.PARAMETER_ORDER, orderDir);
        }
        if (collation != null) {
            param(AJAXServlet.PARAMETER_COLLATION, collation);
        }
        try {
            if (firstLetterOnly) {
                body.put("startletter", true);
                body.put(AJAXServlet.PARAMETER_SEARCHPATTERN, pattern);
                body.put(AJAXServlet.PARAMETER_FOLDERID, inFolder);
            }

            if (inFolder != -1) {
                body.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
            }
            if (pattern != null) {
                body.put(SearchFields.PATTERN, pattern);
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e); // Shouldn't happen
        }
    }

    public SearchRequest(final String pattern, final int inFolder, final int[] columns, final int orderBy, final String orderDir, final boolean failOnError) {
        this(pattern, false, inFolder, columns, orderBy, orderDir, failOnError);
    }

    public SearchRequest(final ContactSearchObject cso, final int[] columns, boolean failOnError) {
        this(cso, columns, -1, null, failOnError);
    }

    public SearchRequest(ContactSearchObject cso, int[] columns, int orderBy, Order order) {
        this(cso, columns, orderBy, order, true);
    }

    public SearchRequest(ContactSearchObject cso, int[] columns, int orderBy, Order order, boolean failOnError) {
        this(cso, columns, orderBy, order, null, failOnError);
    }

    public SearchRequest(ContactSearchObject cso, int[] columns, int orderBy, Order order, String collation, boolean failOnError) {
        searchParser = new SearchParser(failOnError, columns);

        param(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
        param(AJAXServlet.PARAMETER_COLUMNS, join(columns));
        if (orderBy != -1) {
            param(AJAXServlet.PARAMETER_SORT, Integer.toString(orderBy));
        }
        if (null != order) {
            param(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order));
        }
        if (null != collation) {
            param(AJAXServlet.PARAMETER_COLLATION, collation);
        }
        try {
            body.put(ContactFields.LAST_NAME, cso.getSurname());
            body.put(ContactFields.FIRST_NAME, cso.getGivenName());
            body.put(ContactFields.DISPLAY_NAME, cso.getDisplayName());
            body.put(ContactFields.YOMI_COMPANY, cso.getYomiCompany());
            body.put(ContactFields.YOMI_FIRST_NAME, cso.getYomiFirstName());
            body.put(ContactFields.YOMI_LAST_NAME, cso.getYomiLastName());
            body.put(ContactFields.EMAIL1, cso.getEmail1());
            body.put(ContactFields.EMAIL2, cso.getEmail2());
            body.put(ContactFields.EMAIL3, cso.getEmail3());
            body.put(ContactFields.DEPARTMENT, cso.getDepartment());
            // TODO add missing fields

            if (cso.isEmailAutoComplete()) {
                body.put("emailAutoComplete", "true");
                // parameter.setParameter("emailAutoComplete","true");
            }
            if (cso.isOrSearch()) {
                body.put("orSearch", "true");
            }
            if (cso.isExactMatch()) {
                body.put("exactMatch", "true");
            }

            @SuppressWarnings("deprecation") int singleFolderId = cso.getFolder();
            if (singleFolderId != -1) {
                body.put(AJAXServlet.PARAMETER_INFOLDER, singleFolderId);
            }
            if (cso.getPattern() != null) {
                body.put(SearchFields.PATTERN, cso.getPattern());
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e); // Shouldn't happen
        }

    }

    private void param(final String key, final String value) {
        if (value != null) {
            params.add(new AJAXRequest.Parameter(key, value));
        }
    }

    @Override
    public AJAXRequest.Method getMethod() {
        return AJAXRequest.Method.PUT;
    }

    @Override
    public AJAXRequest.Parameter[] getParameters() {
        return params.toArray(new AJAXRequest.Parameter[params.size()]);
    }

    @Override
    public AbstractAJAXParser<SearchResponse> getParser() {
        return searchParser;
    }

    @Override
    public Object getBody() {
        return body;
    }

    private String join(final int[] values) {
        final StringBuilder b = new StringBuilder();
        for (final int v : values) {
            b.append(v).append(", ");
        }
        b.setLength(b.length() - 2);
        return b.toString();
    }
}
