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

package com.openexchange.resource.storage;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.resource.Resource;

/**
 * {@link UsecountAwareResourceStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public interface UsecountAwareResourceStorage extends ResourceStorage {

    /**
     * Similar to {@link #searchResources(String, Context)} but sorts the resources by usecount
     *
     * @param pattern The pattern to search for
     * @param context The context
     * @param userId The user id to sort for
     * @return The usecount sorted resources
     * @throws OXException
     */
    public abstract Resource[] searchResources(final String pattern, final Context context, int userId) throws OXException;

    /**
     * Similar to {@link #searchResourcesByMail(String, Context)} but sorts the resources by usecount
     *
     * @param pattern The pattern to search for
     * @param context The context
     * @param userId The user id to sort for
     * @return The usecount sorted resources
     * @throws OXException
     */
    public abstract Resource[] searchResourcesByMail(final String pattern, final Context context, int userId) throws OXException;

}
