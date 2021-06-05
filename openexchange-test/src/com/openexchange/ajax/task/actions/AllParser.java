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

import java.util.Iterator;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractColumnsParser;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.ListIDInt;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.tasks.Task;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AllParser extends AbstractColumnsParser<CommonAllResponse> {

    /**
     * Default constructor.
     */
    public AllParser(final boolean failOnError, final int[] columns) {
        super(failOnError, columns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CommonAllResponse instantiateResponse(final Response response) {
        return new CommonAllResponse(response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CommonAllResponse createResponse(final Response response) throws JSONException {
        final CommonAllResponse retval = super.createResponse(response);
        final Iterator<Object[]> iter = retval.iterator();
        final ListIDs list = new ListIDs();
        final int folderPos = retval.getColumnPos(Task.FOLDER_ID);
        final int identifierPos = retval.getColumnPos(Task.OBJECT_ID);
        while (iter.hasNext()) {
            final Object[] row = iter.next();
            list.add(new ListIDInt(((Integer) row[folderPos]).intValue(), ((Integer) row[identifierPos]).intValue()));
        }
        retval.setListIDs(list);
        return retval;
    }
}
