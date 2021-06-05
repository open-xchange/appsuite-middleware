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

package com.openexchange.resource.internal;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.storage.ResourceStorage;
import com.openexchange.resource.storage.UsecountAwareResourceStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 * {@link ResourceServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceServiceImpl implements ResourceService {

    /**
     * The permission path to access create, update, and delete methods
     */
    static final String PATH = "com.openexchange.resource.managerequest";

    @Override
    public void create(final User user, final Context ctx, final Resource resource) throws OXException {
        new ResourceCreate(user, ctx, resource).perform();
    }

    @Override
    public void update(final User user, final Context ctx, final Resource resource, final Date clientLastModified) throws OXException {
        new ResourceUpdate(user, ctx, resource, clientLastModified).perform();
    }

    @Override
    public void delete(final User user, final Context ctx, final Resource resource, final Date clientLastModified) throws OXException {
        new ResourceDelete(user, ctx, resource, clientLastModified).perform();
    }

    @Override
    public Resource getResource(final int resourceId, final Context context) throws OXException {
        return ServerServiceRegistry.getServize(ResourceStorage.class, true).getResource(resourceId, context);
    }

    @Override
    public Resource[] listModified(final Date modifiedSince, final Context context) throws OXException {
        return ServerServiceRegistry.getServize(ResourceStorage.class, true).listModified(modifiedSince, context);
    }

    @Override
    public Resource[] listDeleted(final Date modifiedSince, final Context context) throws OXException {
        return ServerServiceRegistry.getServize(ResourceStorage.class, true).listDeleted(modifiedSince, context);
    }

    @Override
    public Resource[] searchResources(final String pattern, final Context context) throws OXException {
        return ServerServiceRegistry.getServize(ResourceStorage.class, true).searchResources(pattern, context);
    }

    @Override
    public Resource[] searchResourcesByMail(final String pattern, final Context context) throws OXException {
        return ServerServiceRegistry.getServize(ResourceStorage.class, true).searchResourcesByMail(pattern, context);
    }

    @Override
    public Resource[] searchResources(Session session, String pattern) throws OXException {
        ResourceStorage servize = ServerServiceRegistry.getServize(ResourceStorage.class, true);
        if (servize instanceof UsecountAwareResourceStorage) {
            return ((UsecountAwareResourceStorage) servize).searchResources(pattern, ServerSessionAdapter.valueOf(session).getContext(), session.getUserId());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource[] searchResourcesByMail(Session session, String pattern) throws OXException {
        ResourceStorage servize = ServerServiceRegistry.getServize(ResourceStorage.class, true);
        if (servize instanceof UsecountAwareResourceStorage) {
            return ((UsecountAwareResourceStorage) servize).searchResourcesByMail(pattern, ServerSessionAdapter.valueOf(session).getContext(), session.getUserId());
        }
        throw new UnsupportedOperationException();
    }

}
