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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceExceptionCode;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class OXResourceResolver implements ResourceResolver {

    private ResourceService resourceService;

    /**
     * Default constructor.
     */
    public OXResourceResolver() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource load(final int resourceId, final Context ctx) throws OXException {
        if (null == resourceService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(
                ResourceService.class.getName());
        }
        return resourceService.getResource(resourceId, ctx);
    }

    @Override
    public List<Resource> find(final List<String> resourceNames, final Context ctx)
        throws OXException {
        final List<Resource> retval = new ArrayList<Resource>();
        if (resourceNames.isEmpty()) {
            return retval;
        }
        if (null == resourceService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(
                ResourceService.class.getName());
        }
        for (final String name : resourceNames) {
            final Resource[] resources = resourceService.searchResources(name, ctx);
            if (resources.length == 1) {
                retval.add(resources[0]);
            }
        }
        return retval;
    }

    /**
     * @param service the service to set
     */
    public void setResourceService(final ResourceService resourceService) {
        this.resourceService = resourceService;
    }
}
