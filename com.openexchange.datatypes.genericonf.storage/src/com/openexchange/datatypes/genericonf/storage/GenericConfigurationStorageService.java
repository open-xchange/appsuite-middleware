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

package com.openexchange.datatypes.genericonf.storage;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link GenericConfigurationStorageService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SingletonService
public interface GenericConfigurationStorageService {

    public abstract int save(Context ctx, Map<String, Object> content) throws OXException;

    public abstract int save(Connection con, final Context ctx, final Map<String, Object> content) throws OXException;

    public abstract void fill(Context ctx, int id, Map<String, Object> content) throws OXException;

    public abstract void fill(Connection con, Context ctx, int id, Map<String, Object> content) throws OXException;

    public abstract void update(final Context ctx, final int id, final Map<String, Object> content) throws OXException;

    public abstract void update(Connection con, final Context ctx, final int id, final Map<String, Object> content) throws OXException;

    public abstract void delete(final Context ctx, final int id) throws OXException;

    public abstract void delete(Connection con, final Context ctx, final int id) throws OXException;

    public abstract void delete(Connection writeConnection, Context ctx) throws OXException;

    public abstract List<Integer> search(final Context ctx, final Map<String, Object> query) throws OXException;

    public abstract List<Integer> search(Connection con, final Context ctx, final Map<String, Object> query) throws OXException;

}
