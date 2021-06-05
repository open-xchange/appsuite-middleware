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
