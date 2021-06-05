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

package com.openexchange.groupware.tasks;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.User;


/**
 * {@link FindTask} is used to execute find queries from the 'find' api.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class FindTask extends Search {
    
    /**
     * Initializes a new {@link FindTask}.
     * @param ctx
     * @param user
     * @param permissionBits
     * @param search
     * @param orderBy
     * @param order
     * @param columns
     */
    public FindTask(Context ctx, User user, UserPermissionBits permissionBits, TaskSearchObject search, int orderBy, Order order, int[] columns) {
        super(ctx, user, permissionBits, search, orderBy, order, columns);
    }
    
    /**
     * Execute the 'find' query
     * 
     * @return
     * @throws OXException 
     */
    @Override
    public SearchIterator<Task> perform() throws OXException {
        checkConditions();
        prepareFolder();
        return TaskSearch.getInstance().find(ctx, user.getId(), search, columns, orderBy, order, all, own, shared);
    }
}
