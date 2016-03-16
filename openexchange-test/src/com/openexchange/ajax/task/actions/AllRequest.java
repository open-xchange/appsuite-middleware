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

package com.openexchange.ajax.task.actions;

import com.openexchange.ajax.framework.AbstractAllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tasks.json.actions.TaskAction;

/**
 * Contains the data for an task all request.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AllRequest extends AbstractAllRequest<CommonAllResponse> {

    public static final int GUI_SORT = Task.END_DATE;

    public static final Order GUI_ORDER = Order.DESCENDING;

    /**
     * Default constructor.
     */
    public AllRequest(final int folderId, final int[] columns, final int sort, final Order order) {
        super(AbstractTaskRequest.TASKS_URL, folderId, AbstractTaskRequest.addGUIColumns(columns), sort, order, true);
    }

    public AllRequest(final int folderId, final String alias, final int sort, final Order order) {
        super(AbstractTaskRequest.TASKS_URL, folderId, alias, sort, order, true);
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
                return new AllParser(isFailOnError(), TaskAction.COLUMNS_ALL_ALIAS);
            }
            if (getAlias().equals("list")) {
                return  new AllParser(isFailOnError(), TaskAction.COLUMNS_LIST_ALIAS);
            }
        }
        return null;
    }
}
