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

package com.openexchange.ajax.contact.action;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.CommonAllRequest;
import com.openexchange.contacts.json.actions.ContactAction;
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
        } else {
            if (getAlias().equals("all")) {
                return new AllParser(isFailOnError(), ContactAction.COLUMNS_ALIAS_ALL);
            }
            if (getAlias().equals("list")) {
                return new AllParser(isFailOnError(), ContactAction.COLUMNS_ALIAS_LIST);
            }
        }
        return null;
    }
}
