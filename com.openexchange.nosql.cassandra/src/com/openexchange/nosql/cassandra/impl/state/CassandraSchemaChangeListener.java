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

package com.openexchange.nosql.cassandra.impl.state;

import com.datastax.driver.core.AggregateMetadata;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.FunctionMetadata;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.MaterializedViewMetadata;
import com.datastax.driver.core.SchemaChangeListener;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.UserType;

/**
 * {@link CassandraSchemaChangeListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CassandraSchemaChangeListener implements SchemaChangeListener {

    /**
     * Initialises a new {@link CassandraSchemaChangeListener}.
     */
    public CassandraSchemaChangeListener() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onKeyspaceAdded(com.datastax.driver.core.KeyspaceMetadata)
     */
    @Override
    public void onKeyspaceAdded(KeyspaceMetadata keyspace) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onKeyspaceRemoved(com.datastax.driver.core.KeyspaceMetadata)
     */
    @Override
    public void onKeyspaceRemoved(KeyspaceMetadata keyspace) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onKeyspaceChanged(com.datastax.driver.core.KeyspaceMetadata, com.datastax.driver.core.KeyspaceMetadata)
     */
    @Override
    public void onKeyspaceChanged(KeyspaceMetadata current, KeyspaceMetadata previous) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onTableAdded(com.datastax.driver.core.TableMetadata)
     */
    @Override
    public void onTableAdded(TableMetadata table) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onTableRemoved(com.datastax.driver.core.TableMetadata)
     */
    @Override
    public void onTableRemoved(TableMetadata table) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onTableChanged(com.datastax.driver.core.TableMetadata, com.datastax.driver.core.TableMetadata)
     */
    @Override
    public void onTableChanged(TableMetadata current, TableMetadata previous) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onUserTypeAdded(com.datastax.driver.core.UserType)
     */
    @Override
    public void onUserTypeAdded(UserType type) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onUserTypeRemoved(com.datastax.driver.core.UserType)
     */
    @Override
    public void onUserTypeRemoved(UserType type) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onUserTypeChanged(com.datastax.driver.core.UserType, com.datastax.driver.core.UserType)
     */
    @Override
    public void onUserTypeChanged(UserType current, UserType previous) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onFunctionAdded(com.datastax.driver.core.FunctionMetadata)
     */
    @Override
    public void onFunctionAdded(FunctionMetadata function) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onFunctionRemoved(com.datastax.driver.core.FunctionMetadata)
     */
    @Override
    public void onFunctionRemoved(FunctionMetadata function) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onFunctionChanged(com.datastax.driver.core.FunctionMetadata, com.datastax.driver.core.FunctionMetadata)
     */
    @Override
    public void onFunctionChanged(FunctionMetadata current, FunctionMetadata previous) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onAggregateAdded(com.datastax.driver.core.AggregateMetadata)
     */
    @Override
    public void onAggregateAdded(AggregateMetadata aggregate) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onAggregateRemoved(com.datastax.driver.core.AggregateMetadata)
     */
    @Override
    public void onAggregateRemoved(AggregateMetadata aggregate) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onAggregateChanged(com.datastax.driver.core.AggregateMetadata, com.datastax.driver.core.AggregateMetadata)
     */
    @Override
    public void onAggregateChanged(AggregateMetadata current, AggregateMetadata previous) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onMaterializedViewAdded(com.datastax.driver.core.MaterializedViewMetadata)
     */
    @Override
    public void onMaterializedViewAdded(MaterializedViewMetadata view) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onMaterializedViewRemoved(com.datastax.driver.core.MaterializedViewMetadata)
     */
    @Override
    public void onMaterializedViewRemoved(MaterializedViewMetadata view) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onMaterializedViewChanged(com.datastax.driver.core.MaterializedViewMetadata, com.datastax.driver.core.MaterializedViewMetadata)
     */
    @Override
    public void onMaterializedViewChanged(MaterializedViewMetadata current, MaterializedViewMetadata previous) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onRegister(com.datastax.driver.core.Cluster)
     */
    @Override
    public void onRegister(Cluster cluster) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.datastax.driver.core.SchemaChangeListener#onUnregister(com.datastax.driver.core.Cluster)
     */
    @Override
    public void onUnregister(Cluster cluster) {
        // TODO Auto-generated method stub

    }

}
