/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are private by
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

package com.openexchange.ajax.mail.actions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
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
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> list = new LinkedList<>();
        list.add(new Parameter(PARAMETER_ACTION, ACTION_THREADED_ALL));

        if (!Strings.isEmpty(categoryId)) {
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
    public Object getBody() throws IOException, JSONException {
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
