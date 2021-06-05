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

package com.openexchange.ajax.mail.actions;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Header;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Strings;

/**
 * {@link ThreadedAllRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class ThreadedAllRequest implements AJAXRequest<ConversationResponse> {

    public static final String PATH = "/ajax/mail";

    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_CATEGORY_ID = "categoryid";
    private static final String PARAMETER_INCLUDE_SENT = "includeSent";
    private static final String ACTION_THREADED_ALL = "threadedAll";

    private final int[] columns;
    private final String categoryId;
    private final String folder;
    private final int sort;
    private final Order order;
    private final boolean includeSent;
    private final boolean failOnError;
    private int rightHandLimit;
    private int leftHandLimit;

    /**
     * Initializes a new {@link ThreadedAllRequest}.
     */
    public ThreadedAllRequest(String folder, int[] columns, int sort, Order order, boolean failOnError, boolean includeSent, String categoryId) {
        super();
        this.folder = folder;
        this.categoryId = categoryId;
        this.columns = columns;
        this.sort = sort;
        this.order = order;
        this.includeSent = includeSent;
        this.failOnError = failOnError;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return PATH;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> list = new LinkedList<>();
        list.add(new Parameter(PARAMETER_ACTION, ACTION_THREADED_ALL));

        if (Strings.isNotEmpty(categoryId)) {
            list.add(new Parameter(PARAMETER_CATEGORY_ID, categoryId));
        }

        list.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folder));
        if (columns != null) {
            list.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        }

        if (null != order) {
            list.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
            list.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        }
        if (validateLimit()) {
            list.add(new Parameter(AJAXServlet.LEFT_HAND_LIMIT, leftHandLimit));
            list.add(new Parameter(AJAXServlet.RIGHT_HAND_LIMIT, rightHandLimit));
        }

        list.add(new Parameter(PARAMETER_INCLUDE_SENT, includeSent));

        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    /**
     * Sets the leftHandLimit
     *
     * @param leftHandLimit the leftHandLimit to set
     */
    public void setLeftHandLimit(final int leftHandLimit) {
        this.leftHandLimit = leftHandLimit;
    }

    /**
     * Sets the rightHandLimit
     *
     * @param rightHandLimit the rightHandLimit to set
     */
    public void setRightHandLimit(final int rightHandLimit) {
        this.rightHandLimit = rightHandLimit;
    }

    private final boolean validateLimit() {
        if ((rightHandLimit != -1 || leftHandLimit != -1)) {
            if (rightHandLimit < leftHandLimit) {
                throw new IllegalArgumentException("right-hand index is less than left-hand index");
            }
            return true;
        }
        return false;
    }

    @Override
    public AbstractAJAXParser<? extends ConversationResponse> getParser() {
        return new ConversationParser(failOnError);
    }
}
