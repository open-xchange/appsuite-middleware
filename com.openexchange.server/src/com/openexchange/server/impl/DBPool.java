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

package com.openexchange.server.impl;

import java.sql.Connection;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * DBPool
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class DBPool  {

    public static final int getServerId() throws OXException {
        return Database.getServerId();
    }

    public static final Connection pickup() throws OXException {
        return Database.get(false);
    }

    public static final Connection pickup(final Context context) throws OXException {
        return Database.get(context, false);
    }

    public static final Connection pickupWriteable() throws OXException {
        return Database.get(true);
    }

    public static final Connection pickupWriteable(final Context context) throws OXException {
        return Database.get(context, true);
    }

    public static final boolean push(final Connection con) {
        if (null != con) {
            Database.back(false, con);
        }
        return true;
    }

    public static final boolean push(final Context context, final Connection con) {
        if (null != con) {
            Database.back(context, false, con);
        }
        return true;
    }

    public static final boolean pushWrite(final Connection con) {
        if (null != con) {
            Database.back(true, con);
        }
        return true;
    }

    public static final boolean pushWrite(final Context context, final Connection con) {
        if (null != con) {
            Database.back(context, true, con);
        }
        return true;
    }

    public static final void pushWriteAfterReading(Context ctx, Connection con) {
        if (null != con) {
            Database.backAfterReading(ctx, con);
        }
    }

    public static final void closeReaderSilent(final Connection con) {
        if (null != con) {
            Database.back(false, con);
        }
    }

    public static final void closeReaderSilent(final Context context, final Connection con) {
        if (null != con) {
            Database.back(context, false, con);
        }
    }

    public static final void closeWriterSilent(final Connection con) {
        if (null != con) {
            Database.back(true, con);
        }
    }

    public static final void closeWriterSilent(final Context context, final Connection con) {
        if (null != con) {
            Database.back(context, true, con);
        }
    }

    public static final void closeWriterAfterReading(final Context context, final Connection con) {
        if (null != con) {
            Database.backAfterReading(context, con);
        }
    }
}
