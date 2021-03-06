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
import com.openexchange.groupware.contexts.Context;

/**
 * The most simple database connection provider.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SimpleDBProvider implements DBProvider {

    private final Connection readCon;

    private final Connection writeCon;

    public SimpleDBProvider(final Connection readCon, final Connection writeCon) {
        super();
        this.readCon = readCon;
        this.writeCon = writeCon;
    }

    @Override
    public Connection getReadConnection(final Context ctx) {
        return readCon;
    }

    @Override
    public Connection getWriteConnection(final Context ctx) {
        return writeCon;
    }

    @Override
    public void releaseReadConnection(final Context ctx, final Connection con) {
        // Nothing to release.
    }

    @Override
    public void releaseWriteConnection(final Context ctx, final Connection con) {
        // Nothing to release.
    }

    @Override
    public void releaseWriteConnectionAfterReading(final Context ctx, final Connection con) {
        // Nothing to release.
    }
}
