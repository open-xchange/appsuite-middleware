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

package com.openexchange.rest.services.database.transactions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.openexchange.java.util.UUIDs;


/**
 * A {@link Transaction} represents a running transaction on a database connection and arbitrary metadata that may be needed
 * to work in a transaction.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Transaction {
    
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Transaction.class);
    
    private String id;
    private Connection con;
    private TransactionKeeper txKeeper;
    private long expires;
    
    private Map<String, Object> parameters = new HashMap<String, Object>();

    private boolean migration;
    
    public Transaction(Connection con, TransactionKeeper txKeeper) {
        super();
        this.id = UUIDs.getUnformattedString(UUID.randomUUID());
        this.con = con;
        this.txKeeper = txKeeper;
        this.expires = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES); // Make this configurable?
    }
    
    /**
     * The ID of this transaction
     * @see TransactionKeeper#getTransaction(String)
     */
    public String getID() {
        return id;
    }
    
    /**
     * The connection this transaction is active on.
     */
    public Connection getConnection() {
        return con;
    }
    
    /**
     * Transactions (usually) expire after 2 minutes. This call triggers  a {@link TransactionKeeper#rollback(String)} on this transaction if this transaction expired
     * @param now Usually System.currentTimeMillis()
     */
    public void tick(long now) {
        if (now > expires) {
            try {
                txKeeper.rollback(id);
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * Remember an arbitrary value in this transaction. Useful for storing state information. Can be retrieved with {@link #getParameter(String)}
     */
    public void put(String name, Object value) {
        parameters.put(name, value);
    }
    
    /**
     * Retrieve a value previously stored with {@link #put(String, Object)}
     */
    public Object getParameter(String name) {
        return parameters.get(name);
    }
    
    
    /**
     * Update the connection object. Used in tests.
     */
    public void setConnection(Connection con) {
        this.con = con;
    }
    
    /**
     * Migrations can take significantly longer then regular operations, so extend the grace period for this transaction to 8 hours.
     */
    public void extendLifetimeForMigration() {
        expires = expires + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);
        migration = true;
    }
    
    public void extendLifetime() {
        if (migration) {
            expires = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(8,  TimeUnit.HOURS);
            return;
        }
        expires = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(2,  TimeUnit.MINUTES);
    }

}
