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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonAllRequest;
import com.openexchange.contacts.json.actions.IDBasedContactAction;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.tools.arrays.Arrays;

/**
 * Contains the data for an contact all request.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ben.pahne@open-xchange.org">Ben Pahne</a>
 */
public class AllRequest extends CommonAllRequest {

    public static final int[] GUI_COLUMNS = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID };

    public static final int GUI_SORT = Contact.SUR_NAME;

    public static final Order GUI_ORDER = Order.ASCENDING;

    private String collation;

    /**
     * Default constructor.
     */
    public AllRequest(final int folderId, final int[] columns) {
        super(AbstractContactRequest.URL, folderId, addGUIColumns(columns), 0, null, true);
    }

    public AllRequest(final int folderId, final int[] columns, final int orderBy, final Order order, final String collation) {
        super(AbstractContactRequest.URL, folderId, addGUIColumns(columns), orderBy, order, true);
        this.collation = collation;
    }

    public AllRequest(final int folderId, final String alias) {
        super(AbstractContactRequest.URL, folderId, alias, 0, null, true);
    }

    public AllRequest(final int folderId, final String alias, final int orderBy, final Order order, final String collation) {
        super(AbstractContactRequest.URL, folderId, alias, orderBy, order, true);
        this.collation = collation;
    }

    @Override
    public Parameter[] getParameters() {
        final Parameter[] params = super.getParameters();
        return Arrays.add(params, new Parameter(AJAXServlet.PARAMETER_COLLATION, collation));
    }

    private static int[] addGUIColumns(final int[] columns) {
        final List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < columns.length; i++) {
            list.add(Integer.valueOf(columns[i]));
        }
        // Move GUI_COLUMNS to end unless already in there.
        for (int i = 0; i < GUI_COLUMNS.length; i++) {
            final Integer column = Integer.valueOf(GUI_COLUMNS[i]);
            if (!list.contains(column)) {
                list.add(column);
            }
        }
        final int[] retval = new int[list.size()];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = list.get(i).intValue();
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllParser getParser() {
        if (getColumns() != null) {
            return new AllParser(isFailOnError(), getColumns());
        }
        if (getAlias().equals("all")) {
            return new AllParser(isFailOnError(), IDBasedContactAction.COLUMNS_ALIAS_ALL);
        }
        if (getAlias().equals("list")) {
            return new AllParser(isFailOnError(), IDBasedContactAction.COLUMNS_ALIAS_LIST);
        }
        return null;
    }
}
