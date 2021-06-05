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

package com.openexchange.data.conversion.ical.ical4j.internal;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.resource.Resource;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface ResourceResolver {

    /**
     * Loads the resource with the given unique identifier.
     * @param resourceId unique resource identifier.
     * @param ctx Context.
     * @return the loaded resource.
     * @throws OXException if loading the resource fails.
     * @throws OXException if the resource service is not available.
     */
    Resource load(final int resourceId, final Context ctx) throws OXException;

    /**
     * Find the resources with the given names.
     * @param names display names of resources.
     * @param ctx Context.
     * @return list of found resources.
     */
    List<Resource> find(final List<String> names, final Context ctx) throws OXException;

}
