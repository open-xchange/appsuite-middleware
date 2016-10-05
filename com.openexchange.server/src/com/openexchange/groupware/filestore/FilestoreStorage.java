/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
