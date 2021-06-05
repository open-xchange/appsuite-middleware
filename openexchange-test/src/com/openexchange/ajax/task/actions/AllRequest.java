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
        }
        if (getAlias().equals("all")) {
            return new AllParser(isFailOnError(), TaskAction.COLUMNS_ALL_ALIAS);
        }
        if (getAlias().equals("list")) {
            return new AllParser(isFailOnError(), TaskAction.COLUMNS_LIST_ALIAS);
        }
        return null;
    }
}
