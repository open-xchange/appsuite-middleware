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

package com.openexchange.datatypes.genericonf.storage.osgi.tools;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link WhiteboardGenericConfigurationStorageService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class WhiteboardGenericConfigurationStorageService implements GenericConfigurationStorageService {

    private final ServiceTracker<?,?> tracker;

    public WhiteboardGenericConfigurationStorageService(BundleContext context) {
        this.tracker = new ServiceTracker<>(context, GenericConfigurationStorageService.class.getName(), null);
        tracker.open();
    }

    public void close() {
        this.tracker.close();
    }

    public GenericConfigurationStorageService getDelegate() {
        return (GenericConfigurationStorageService) tracker.getService();
    }

    @Override
    public void delete(Context ctx, int id) throws OXException {
        getDelegate().delete(ctx, id);
    }

    @Override
    public void delete(Connection con, Context ctx, int id) throws OXException {
        getDelegate().delete(con, ctx, id);
    }

    @Override
    public void delete(Connection writeConnection, Context ctx) throws OXException {
        getDelegate().delete(writeConnection, ctx);
    }

    @Override
    public void fill(Context ctx, int id, Map<String, Object> content) throws OXException {
        getDelegate().fill(ctx, id, content);
    }

    @Override
    public void fill(Connection con, Context ctx, int id, Map<String, Object> content) throws OXException {
        getDelegate().fill(con, ctx, id, content);
    }

    @Override
    public int save(Connection con, Context ctx, Map<String, Object> content) throws OXException {
        return getDelegate().save(con, ctx, content);
    }

    @Override
    public int save(Context ctx, Map<String, Object> content) throws OXException {
        return getDelegate().save(ctx, content);
    }

    @Override
    public void update(Connection con, Context ctx, int id, Map<String, Object> content) throws OXException {
        getDelegate().update(con, ctx, id, content);
    }

    @Override
    public void update(Context ctx, int id, Map<String, Object> content) throws OXException {
        getDelegate().update(ctx, id, content);
    }

    @Override
    public List<Integer> search(Context ctx, Map<String, Object> query) throws OXException {
        return getDelegate().search(ctx, query);
    }

    @Override
    public List<Integer> search(Connection con, Context ctx, Map<String, Object> query) throws OXException {
        return getDelegate().search(con, ctx, query);
    }


}
