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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.groupware.update;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.update.tasks.ClearOrphanedInfostoreDocuments;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.update.ForeignKeyOld;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ClearOrphanedInfostoreDocumentsTest extends UpdateTest {

    private final List<String> paths = new ArrayList<String>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Connection con = getProvider().getWriteConnection(ctx);
        List<ForeignKeyOld> foreignKeys = ForeignKeyOld.getForeignKeys(con, "infostore_document");
        for (ForeignKeyOld foreignKey : foreignKeys) {
            try {
                foreignKey.drop(con);
            } catch (SQLException x) {
                // IGNORE
            }
        }

        createOrphanedInfostoreDocumentEntry(100000, 1);
        createOrphanedInfostoreDocumentEntry(100000, 2);
        createOrphanedInfostoreDocumentEntry(100000, 3);

    }

    @Override
    public void tearDown() throws SQLException, OXException, OXException, OXException, OXException {
        exec("DELETE FROM infostore_document WHERE infostore_id = ?", 100000);
        exec("DELETE FROM del_infostore_document WHERE infostore_id = ?", 100001);

        FileStorage fs  = QuotaFileStorage.getInstance(
                FilestoreStorage.createURI(ctx), ctx);
        for (String path : paths) {
            fs.deleteFile(path);
        }
        Connection con = getProvider().getWriteConnection(ctx);
        List<ForeignKeyOld> foreignKeys = ForeignKeyOld.getForeignKeys(con, "infostore_document");
        for (ForeignKeyOld foreignKey : foreignKeys) {
            try {
                foreignKey.drop(con);
            } catch (SQLException x) {
                // IGNORE
            }
        }

        foreignKeys = ForeignKeyOld.getForeignKeys(con, "del_infostore_document");
        for (ForeignKeyOld foreignKey : foreignKeys) {
            try {
                foreignKey.drop(con);
            } catch (SQLException x) {
                // IGNORE
            }
        }
    }

    public void testShouldClearLeftoverDocuments() throws OXException, SQLException {
        assertNoResults("SELECT * FROM infostore WHERE id = 100000");
        new ClearOrphanedInfostoreDocuments().perform(schema, existing_ctx_id);

        assertNoResults("SELECT * FROM infostore_document WHERE infostore_id = 100000");
        assertNotInFilestorage(paths);
    }

    public void testShouldBeRunnableTwice() throws OXException {
        new ClearOrphanedInfostoreDocuments().perform(schema, existing_ctx_id);
        new ClearOrphanedInfostoreDocuments().perform(schema, existing_ctx_id);
    }

    public void testShouldCreateIndexOnInfostoreDocument() throws OXException, SQLException {
        new ClearOrphanedInfostoreDocuments().perform(schema, existing_ctx_id);
        ForeignKeyOld fk = new ForeignKeyOld("infostore_document", "infostore_id", "infostore", "id");
        List<ForeignKeyOld> keys = ForeignKeyOld.getForeignKeys(getProvider().getWriteConnection(ctx), "infostore_document");
        assertTrue(keys.contains(fk));
    }

    private void createOrphanedInfostoreDocumentEntry(int id, int version) throws OXException, OXException, UnsupportedEncodingException, SQLException, OXException {
        FileStorage fs  = QuotaFileStorage.getInstance(
                FilestoreStorage.createURI(ctx), ctx);

        String path = fs.saveNewFile(new ByteArrayInputStream("Hallo Welt".getBytes(com.openexchange.java.Charsets.UTF_8)));
        paths.add(path);

        exec("INSERT INTO infostore_document (cid, infostore_id, version_number, file_store_location, creating_date, last_modified, created_by) VALUES (?,?,?,?, 0,0, ?)", ctx.getContextId(), id, version, path, user_id);
    }

}
