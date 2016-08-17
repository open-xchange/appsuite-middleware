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

package com.openexchange.rest.services.database.migrations;

import static com.openexchange.database.DatabaseMocking.connection;
import static com.openexchange.database.DatabaseMocking.verifyConnection;
import static com.openexchange.database.DatabaseMocking.whenConnection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.rest.services.database.migrations.DBVersionChecker;
import com.openexchange.rest.services.database.migrations.VersionChecker;

/**
 * {@link VersionCheckerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class VersionCheckerTest {

    private VersionChecker checker = null;

    @Before
    public void setup() {
        checker = new DBVersionChecker();
    }

    @Test
    public void versionMatch() throws OXException {
        Connection con = connection();
        whenConnection(con).isQueried("SELECT version FROM serviceSchemaVersion WHERE module = ?").withParameter("com.openexchange.mySystem")
            .thenReturnColumns("version").withRow("abcdef");

        assertNull(checker.isUpToDate(new Object(), con, "com.openexchange.mySystem", "abcdef"));
    }

    @Test
    public void versionMismatch() throws OXException {
        Connection con = connection();
        whenConnection(con).isQueried("SELECT version FROM serviceSchemaVersion WHERE module = ?").withParameter("com.openexchange.mySystem")
            .thenReturnColumns("version").withRow("abcdef");

        String oldVersion = checker.isUpToDate(new Object(), con, "com.openexchange.mySystem", "ghijkl");

        assertEquals("abcdef", oldVersion);
    }

    @Test
    public void unknownModule() throws OXException {
        Connection con = connection();
        whenConnection(con).isQueried("SELECT version FROM serviceSchemaVersion WHERE module = ?").withParameter("com.openexchange.mySystem").thenReturnColumns("version");
        assertNotNull(checker.isUpToDate(new Object(), con, "com.openexchange.mySystem", "ghijkl"));
    }

    @Test
    public void upgradeVersion() throws OXException {
        Connection con = connection();

        whenConnection(con).isQueried("UPDATE serviceSchemaVersion SET version = ? WHERE module = ? AND version = ?")
            .withParameter("ghijkl").andParameter("com.openexchange.mySystem").andParameter("abcdef").thenReturnModifiedRows(1);
        assertNull(checker.updateVersion(con, "com.openexchange.mySystem", "abcdef", "ghijkl"));
    }

    @Test
    public void upgradeVersionFails() throws OXException {
        Connection con = connection();

        whenConnection(con).isQueried("UPDATE serviceSchemaVersion SET version = ? WHERE module = ? AND version = ?")
            .withParameter("ghijkl").andParameter("com.openexchange.mySystem").andParameter("abcdef").thenReturnModifiedRows(0);
        whenConnection(con).isQueried("SELECT version FROM serviceSchemaVersion WHERE module = ?").thenReturnColumns("ghijkl")
            .withParameter("com.openexchange.mySystem")
            .thenReturnColumns("version").withRow("ghijkl");

        assertEquals("ghijkl", checker.updateVersion(con, "com.openexchange.mySystem", "abcdef", "ghijkl"));
    }

    @Test
    public void upgradeVersionOfUnknownModule() throws OXException {
        Connection con = connection();
        whenConnection(con).isQueried("INSERT IGNORE INTO serviceSchemaVersion (module, version) VALUES (?, ?)")
            .withParameter("com.openexchange.mySystem").andParameter("ghijkl").thenReturnModifiedRows(1);

        assertEquals(null, checker.updateVersion(con, "com.openexchange.mySystem", "", "ghijkl"));
    }

    @Test
    public void failWhenVersionShouldBeKnownButIsnt() throws OXException {
        Connection con = connection();

        whenConnection(con).isQueried("UPDATE serviceSchemaVersion SET version = ? WHERE module = ? AND version = ?")
            .withParameter("ghijkl").andParameter("com.openexchange.mySystem").andParameter("abcdef").thenReturnModifiedRows(0);
        whenConnection(con).isQueried("SELECT version FROM serviceSchemaVersion WHERE module = ?").thenReturnColumns("ghijkl")
            .withParameter("com.openexchange.mySystem")
            .thenReturnColumns("version").andNoRows();

        try {
            checker.updateVersion(con, "com.openexchange.mySystem", "abcdef", "ghijkl");
            fail("Should have exited with exception");
        } catch (OXException x) {

        }
    }

    @Test
    public void cacheVersion() throws OXException {
        Connection con = connection();
        whenConnection(con).isQueried("SELECT version FROM serviceSchemaVersion WHERE module = ?").withParameter("com.openexchange.mySystem")
            .thenReturnColumns("version").withRow("abcdef");

        Object id = new Object();

        checker.isUpToDate(id, con, "com.openexchange.mySystem", "abcdef");

        con = connection();

        checker.isUpToDate(id, con, "com.openexchange.mySystem", "abcdef");

        verifyNoMoreInteractions(con);
    }

    @Test
    public void versionMismatchForcesReadthru() throws OXException {
        Connection con = connection();
        whenConnection(con).isQueried("SELECT version FROM serviceSchemaVersion WHERE module = ?").withParameter("com.openexchange.mySystem")
            .thenReturnColumns("version").withRow("abcdef");

        Object id = new Object();

        checker.isUpToDate(id, con, "com.openexchange.mySystem", "abcdef");

        con = connection();
        whenConnection(con).isQueried("SELECT version FROM serviceSchemaVersion WHERE module = ?").withParameter("com.openexchange.mySystem")
            .thenReturnColumns("version").withRow("ghijkl");

        assertNull(checker.isUpToDate(id, con, "com.openexchange.mySystem", "ghijkl"));

    }

    @Test
    public void migrationMutexLock() throws OXException {
        Connection con = connection();
        long now = System.currentTimeMillis();
        long expires = now + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);
        // DELETE STALE LOCKS
        whenConnection(con).isQueried("DELETE FROM serviceSchemaMigrationLock WHERE module = ? AND expires <= ?").withParameter("com.openexchange.myModule").andParameter(now).thenReturnModifiedRows(0);

        // SELECT LOCK, NO LOCK FOUND
        whenConnection(con).isQueried("SELECT 1 FROM serviceSchemaMigrationLock WHERE module = ?").withParameter("com.openexchange.myModule").thenReturnColumns("1").andNoRows();

        // CREATE LOCK
        whenConnection(con).isQueried("INSERT IGNORE INTO serviceSchemaMigrationLock (module, expires) VALUES (?, ?)")
            .withParameter("com.openexchange.myModule")
            .withParameter(expires)
            .thenReturnModifiedRows(1);

        assertTrue(checker.lock(con, "com.openexchange.myModule", now, expires));
    }

    @Test
    public void migrationMutexLockAlreadyLocked() throws OXException {
        Connection con = connection();
        long now = System.currentTimeMillis();
        long expires = now + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);
        // DELETE STALE LOCKS
        whenConnection(con).isQueried("DELETE FROM serviceSchemaMigrationLock WHERE module = ? AND expires <= ?").withParameter("com.openexchange.myModule").andParameter(now).thenReturnModifiedRows(0);

        // SELECT LOCK, LOCK FOUND
        whenConnection(con).isQueried("SELECT 1 FROM serviceSchemaMigrationLock WHERE module = ?").withParameter("com.openexchange.myModule").thenReturnColumns("1").andRow(1);

        assertFalse(checker.lock(con, "com.openexchange.myModule", now, expires));
    }

    @Test
    public void migrationMutextRaceCondition() throws OXException {
        Connection con = connection();
        long now = System.currentTimeMillis();
        long expires = now + TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);
        // DELETE STALE LOCKS
        whenConnection(con).isQueried("DELETE FROM serviceSchemaMigrationLock WHERE module = ? AND expires <= ?").withParameter("com.openexchange.myModule").andParameter(now).thenReturnModifiedRows(0);

        // SELECT LOCK, NO LOCK FOUND
        whenConnection(con).isQueried("SELECT 1 FROM serviceSchemaMigrationLock WHERE module = ?").withParameter("com.openexchange.myModule").thenReturnColumns("1").andNoRows();

        // CREATE LOCK
        whenConnection(con).isQueried("INSERT IGNORE INTO serviceSchemaMigrationLock (module, expires) VALUES (?, ?)")
            .withParameter("com.openexchange.myModule")
            .withParameter(expires)
            .thenReturnModifiedRows(0);

        assertFalse(checker.lock(con, "com.openexchange.myModule", now, expires));
    }

    @Test
    public void migrationMutexUnlock() throws OXException {
        Connection con = connection();

        checker.unlock(con, "com.openexchange.myModule");

        verifyConnection(con).receivedQuery("DELETE FROM serviceSchemaMigrationLock WHERE module = ?").withParameter("com.openexchange.myModule");
    }

    @Test
    public void migrationMutexKeepAlive() throws OXException {
        Connection con = connection();

        whenConnection(con).isQueried("UPDATE serviceSchemaMigrationLock SET expires = ? WHERE module = ?")
            .withParameter(12l)
            .withParameter("com.openexchange.myModule")
            .thenReturnModifiedRows(1);

        assertTrue(checker.touchLock(con, "com.openexchange.myModule", 12));
    }

    @Test
    public void migrationMutexKeepAliveIsGone() throws OXException {
        Connection con = connection();

        whenConnection(con).isQueried("UPDATE serviceSchemaMigrationLock SET expires = ? WHERE module = ?")
            .withParameter(12l)
            .withParameter("com.openexchange.myModule")
            .thenReturnModifiedRows(0);

        assertFalse(checker.touchLock(con, "com.openexchange.myModule", 12));

    }

}
