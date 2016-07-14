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

package com.openexchange.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.sql.SQLException;
import org.junit.Test;

/**
 * {@link DatabasesTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class DatabasesTest {

    private SQLException mysqlException;

    @Test
    public void testIsPrimaryKeyConflictInMySQL_mySQL50Exception_returnTrue() {
        this.mysqlException = new SQLException("Duplicate entry '57234' for key 1", "23000", 1586);

        boolean primaryKeyConflictInMySQL = Databases.isPrimaryKeyConflictInMySQL(this.mysqlException);
        
        assertTrue(primaryKeyConflictInMySQL);
    }
    
    @Test
    public void testIsPrimaryKeyConflictInMySQL_mySQL50Exception_returnFalse() {
        this.mysqlException = new SQLException("Duplicate entry '57234' for key 1", "23000", 1777);

        boolean primaryKeyConflictInMySQL = Databases.isPrimaryKeyConflictInMySQL(this.mysqlException);
        
        assertFalse(primaryKeyConflictInMySQL);
    }

    @Test
    public void testIsPrimaryKeyConflictInMySQL_mySQL5xException_returnTrue() {
        this.mysqlException = new SQLException("Duplicate entry '57234' for key 'PRIMARY'", "23000", 1062);

        boolean primaryKeyConflictInMySQL = Databases.isPrimaryKeyConflictInMySQL(this.mysqlException);
        
        assertTrue(primaryKeyConflictInMySQL);
    }
    
    @Test
    public void testIsPrimaryKeyConflictInMySQL_mySQL5xExceptionNoPrimary_returnFalse() {
        this.mysqlException = new SQLException("Duplicate entry '57234' for key 'NOT_PRIMARY'", "23000", 1062);

        boolean primaryKeyConflictInMySQL = Databases.isPrimaryKeyConflictInMySQL(this.mysqlException);
        
        assertFalse(primaryKeyConflictInMySQL);
    }

    @Test
    public void testIsKeyConflictInMySQL_mySQL5xExceptionNoPrimary_returnFalse() {
        this.mysqlException = new SQLException("Duplicate entry '57234' for key 'NOT_PRIMARY'", "23000", 1062);

        boolean primaryKeyConflictInMySQL = Databases.isKeyConflictInMySQL(this.mysqlException, "PRIMARY");
        
        assertFalse(primaryKeyConflictInMySQL);
    }
    
    @Test
    public void testIsKeyConflictInMySQL_mySQL5xException_returnTrue() {
        this.mysqlException = new SQLException("Duplicate entry '57234' for key 'PRIMARY'", "23000", 1062);

        boolean primaryKeyConflictInMySQL = Databases.isKeyConflictInMySQL(this.mysqlException, "PRIMARY");
        
        assertTrue(primaryKeyConflictInMySQL);
    }
    
    @Test
    public void testIsKeyConflictInMySQL_mySQL5xExceptionWithNoPrimary_returnTrue() {
        this.mysqlException = new SQLException("Duplicate entry '57234' for key 'NOT_PRIMARY'", "23000", 1062);

        boolean primaryKeyConflictInMySQL = Databases.isKeyConflictInMySQL(this.mysqlException, "NOT_PRIMARY");
        
        assertTrue(primaryKeyConflictInMySQL);
    }
}
