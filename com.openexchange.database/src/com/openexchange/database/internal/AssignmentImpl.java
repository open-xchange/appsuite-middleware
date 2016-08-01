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

package com.openexchange.database.internal;

import java.io.Serializable;
import com.openexchange.database.Assignment;

/**
 * Assignment of context and server to read and write databases.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AssignmentImpl implements Serializable, Assignment {

    private static final long serialVersionUID = -3426601066426517436L;

    private final int contextId;
    private final int serverId;

    private final int readPoolId;
    private final int writePoolId;

    private final String schema;

    private volatile Long transaction;

    private final int hash;

    /**
     * Default constructor.
     *
     * @param contextId
     * @param serverId
     * @param readPoolId
     * @param writePoolId
     * @param schema
     */
    AssignmentImpl(int contextId, int serverId, int readPoolId, int writePoolId, String schema) {
        super();
        this.contextId = contextId;
        this.serverId = serverId;
        this.readPoolId = readPoolId;
        this.writePoolId = writePoolId;
        this.schema = schema;

        int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + serverId;
        result = prime * result + readPoolId;
        result = prime * result + writePoolId;
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        this.hash = result;
    }

    public AssignmentImpl(Assignment assign) {
        this(assign.getContextId(), assign.getServerId(), assign.getReadPoolId(), assign.getWritePoolId(), assign.getSchema());
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getServerId() {
        return serverId;
    }

    @Override
    public int getReadPoolId() {
        return readPoolId;
    }

    @Override
    public int getWritePoolId() {
        return writePoolId;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    /**
     * Returns <code>true</code> if the transaction counter has been initialized
     *
     * @return <code>true</code> if the transaction counter has been initialized; otherwise <code>false</code>
     */
    boolean isTransactionInitialized() {
        return null != this.transaction;
    }

    /**
     * Gets the previously assigned transaction counter or <code>0</code> (zero)
     *
     * @return The transaction counter or <code>0</code> (zero)
     */
    long getTransaction() {
        Long transaction = this.transaction;
        return null == transaction ? 0L : transaction.longValue();
    }

    /**
     * Sets the transaction counter and thus marks this assignment having an initialized transaction
     *
     * @param transaction The transaction counter to set
     */
    public void setTransaction(long transaction) {
        this.transaction = Long.valueOf(transaction);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AssignmentImpl)) {
            return false;
        }
        AssignmentImpl other = (AssignmentImpl) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (serverId != other.serverId) {
            return false;
        }
        if (readPoolId != other.readPoolId) {
            return false;
        }
        if (writePoolId != other.writePoolId) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("write_pool_id: " + this.writePoolId + ", ");
        builder.append("read_pool_id: " + this.readPoolId + ", ");
        builder.append("schema name: " + this.schema + ", ");
        builder.append("server_id: " + this.serverId + ", ");
        builder.append("context_id: " + this.contextId);
        return builder.toString();
    }
}
