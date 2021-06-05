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

package com.openexchange.database.provider;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

public class ReuseReadConProvider implements DBProvider {

    private final DBProvider provider;

    private Connection readCon;

    private int refCount;

    public ReuseReadConProvider(final DBProvider provider) {
        this.provider = provider;
    }

    @Override
    public Connection getReadConnection(final Context ctx) throws OXException {
        if (readCon != null) {
            refCount++;
            return readCon;
        }
        readCon = provider.getReadConnection(ctx);
        refCount++;
        return readCon;
    }

    @Override
    public Connection getWriteConnection(final Context ctx) throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseReadConnection(final Context ctx, final Connection con) {
        if (con == null) {
            return;
        }
        refCount--;
        if (refCount == 0) {
            provider.releaseReadConnection(ctx, con);
            readCon = null;
        }
    }

    @Override
    public void releaseWriteConnection(final Context ctx, final Connection con) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseWriteConnectionAfterReading(final Context ctx, final Connection con) {
        throw new UnsupportedOperationException();
    }

}
