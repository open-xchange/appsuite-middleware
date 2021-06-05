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

package com.openexchange.user.copy.internal.connection;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.user.copy.ObjectMapping;

/**
 * Provides a connection and puts the connection for the destination context into transaction.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ConnectionHolder implements ObjectMapping<Connection> {

    private final Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
    private final Map<Connection, Integer> backMap = new HashMap<Connection, Integer>();
    private final Map<Integer, Connection> connections = new HashMap<Integer, Connection>();

    public ConnectionHolder() {
        super();
    }

    @Override
    public Connection getSource(final int id) {
        return connections.get(I(id));
    }

    @Override
    public Connection getDestination(final Connection source) {
        final Integer srcCtxId = backMap.get(source);
        if (null == srcCtxId) {
            return null;
        }
        final Integer dstCtxId = mapping.get(srcCtxId);
        if (null == dstCtxId) {
            return null;
        }
        return connections.get(dstCtxId);
    }

    @Override
    public Set<Integer> getSourceKeys() {
        final Set<Integer> keySet = new HashSet<Integer>(mapping.keySet());

        return keySet;
    }

    public void addMapping(final int srcCtxId, final Connection srcCon, final int dstCtxId, final Connection dstCon) {
        connections.put(I(srcCtxId), srcCon);
        connections.put(I(dstCtxId), dstCon);
        backMap.put(srcCon, I(srcCtxId));
        backMap.put(dstCon, I(dstCtxId));
        mapping.put(I(srcCtxId), I(dstCtxId));
    }
}
