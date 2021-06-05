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

package com.openexchange.groupware.filestore;

import java.net.URI;
import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.RdbContextStorage;

public abstract class FilestoreStorage {

    private static final FilestoreStorage INSTANCE = new CachingFilestoreStorage(new RdbFilestoreStorage());

    public static FilestoreStorage getInstance(){
        return INSTANCE;
    }

    public abstract Filestore getFilestore(int id) throws OXException;

    public abstract Filestore getFilestore(Connection con, int id) throws OXException;

    /**
     * Gets the associated file store for given URI
     *
     * @param uri The URI to resolve
     * @returnThe file store
     * @throws OXException If file store cannot be resolved
     */
    public abstract Filestore getFilestore(URI uri) throws OXException;

    /**
     * Convenience method for generating the context specific file store location.
     * <pre>
     * &lt;filestore-uri&gt; + "/" + &lt;context-appendix&gt;
     * </pre>
     *
     * @param ctx the location will be generated for this context.
     * @return A ready to use context-specific file store location.
     * @throws OXException If an error occurs generating the URI
     */
    public static URI createURI(final Context ctx) throws OXException {
        final Filestore store = getInstance().getFilestore(ctx.getFilestoreId());
        return FilestoreTools.createLocation(store, ctx);
    }

    /**
     * Convenience method for generating the context specific file store location.
     * <pre>
     * &lt;filestore-uri&gt; + "/" + &lt;context-appendix&gt;
     * </pre>
     *
     * @param con The connection to use
     * @param ctx The associated context
     * @return The context-specific file store location
     * @throws OXException If an error occurs generating the URI
     */
    public static URI createURI(final Connection con, final Context ctx) throws OXException {
        final FilestoreStorage storage = getInstance();
        final Filestore store = storage.getFilestore(con, ctx.getFilestoreId());
        return FilestoreTools.createLocation(store, ctx);
    }

    /**
     * Convenience method for generating the context specific file store location.
     * <pre>
     * &lt;filestore-uri&gt; + "/" + &lt;context-appendix&gt;
     * </pre>
     *
     * @param con The connection
     * @param contextId The context identifier
     * @return The context-specific file store location
     * @throws OXException If an error occurs generating the URI
     */
    public static URI createURI(final Connection con, final int contextId) throws OXException {
        final RdbContextStorage ctxStorage = new RdbContextStorage();
        final Context ctx = ctxStorage.loadContextData(con, contextId);
        final FilestoreStorage storage = getInstance();
        final Filestore store = storage.getFilestore(con, ctx.getFilestoreId());
        return FilestoreTools.createLocation(store, ctx);
    }

}
