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

package com.openexchange.ajax.framework;

import java.util.Set;
import com.openexchange.ajax.container.Response;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CommonUpdatesResponse extends AbstractColumnsResponse {

    private Set<Integer> newOrModifiedIds;

    private Set<Integer> deletedIds;

    public CommonUpdatesResponse(final Response response) {
        super(response);
    }

    @Override
    void setArray(final Object[][] array) {
        super.setArray(array);
    }

    /**
     * Get a collection of object ids that were modified(new or updated) during the request.
     * 
     * @return a collection of object ids that were modified during the request.
     */
    public Set<Integer> getNewOrModifiedIds() {
        return newOrModifiedIds;
    }

    /**
     * Sets the newOrModifiedIds
     * 
     * @param newOrModifiedIds The newOrModifiedIds to set
     */
    public void setNewOrModifiedIds(Set<Integer> newOrModifiedIds) {
        this.newOrModifiedIds = newOrModifiedIds;
    }

    /**
     * Get a collection of object ids that were deleted during the request.
     * 
     * @return a collection of object ids that were deleted during the request.
     */
    public Set<Integer> getDeletedIds() {
        return deletedIds;
    }

    /**
     * Sets the deletedIds
     * 
     * @param deletedIds The deletedIds to set
     */
    public void setDeletedIds(Set<Integer> deletedIds) {
        this.deletedIds = deletedIds;
    }

}
