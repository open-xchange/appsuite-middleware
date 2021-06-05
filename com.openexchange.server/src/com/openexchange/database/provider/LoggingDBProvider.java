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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;


/**
 * A {@link DBProvider} that logs every method call on the trace log level.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class LoggingDBProvider implements DBProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingDBProvider.class);

    private static final String PREFIX = "!!!DBPROVIDER!!! ";

    private final DBProvider delegate;

    public LoggingDBProvider(DBProvider delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public Connection getReadConnection(Context ctx) throws OXException {
        LOG.trace("{}Getting read connection", PREFIX);
        return delegate.getReadConnection(ctx);
    }

    @Override
    public void releaseReadConnection(Context ctx, Connection con) {
        LOG.trace("{}Releasing read connection", PREFIX);
        delegate.releaseReadConnection(ctx, con);
    }

    @Override
    public Connection getWriteConnection(Context ctx) throws OXException {
        LOG.trace("{}Getting write connection", PREFIX);
        return delegate.getWriteConnection(ctx);
    }

    @Override
    public void releaseWriteConnection(Context ctx, Connection con) {
        LOG.trace("{}Releasing write connection", PREFIX);
        delegate.releaseWriteConnection(ctx, con);
    }

    @Override
    public void releaseWriteConnectionAfterReading(Context ctx, Connection con) {
        LOG.trace("{}Releasing write connection", PREFIX);
        delegate.releaseWriteConnectionAfterReading(ctx, con);
    }

}
