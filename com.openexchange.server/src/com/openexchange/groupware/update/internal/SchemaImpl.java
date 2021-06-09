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

package com.openexchange.groupware.update.internal;

import com.openexchange.groupware.update.Schema;

/**
 * This class is a data container for the update information of a database schema.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class SchemaImpl implements Schema {

    private static final long serialVersionUID = 8722328691217424068L;

    private boolean locked;
    private String server;
    private String schema;
    private int poolId;

    /**
     * Initializes a new {@link SchemaImpl}.
     */
    protected SchemaImpl() {
        super();
    }

    /**
     * Initializes a new {@link SchemaImpl}.
     *
     * @param locked <code>true</code> if locked; otherwise <code>false</code>
     */
    public SchemaImpl(boolean locked) {
        super();
        this.locked = locked;
    }

    /**
     * Initializes a new {@link SchemaImpl}.
     *
     * @param schema The schema to copy from
     */
    public SchemaImpl(Schema schema) {
        super();
        this.locked = schema.isLocked();
        this.server = schema.getServer();
        this.schema = schema.getSchema();
        this.poolId = schema.getPoolId();
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    void setBlockingUpdatesRunning(final boolean locked) {
        this.locked = locked;
    }

    @Override
    public String getServer() {
        return server;
    }

    /**
     * Sets the server name.
     *
     * @param server The server name
     */
    public void setServer(final String server) {
        this.server = server;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    public void setSchema(final String schema) {
        this.schema = schema;
    }

    @Override
    public int getPoolId() {
        return poolId;
    }

    /**
     * Sets the identifier of the database pool
     *
     * @param poolId The pool identifier to set
     */
    public void setPoolId(int poolId) {
        this.poolId = poolId;
    }

}
