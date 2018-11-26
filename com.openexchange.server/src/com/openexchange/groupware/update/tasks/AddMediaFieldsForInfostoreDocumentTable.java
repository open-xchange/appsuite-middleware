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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddMediaFieldsForInfostoreDocumentTable} - Extends infostore document tables by "meta" JSON BLOB.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddMediaFieldsForInfostoreDocumentTable extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AddMediaFieldsForInfostoreDocumentTable}.
     */
    public AddMediaFieldsForInfostoreDocumentTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            startTransaction(con);
            rollback = 1;

            if (!Tools.columnExists(con, "infostore_document", "capture_date")) {
                Tools.addColumns(con, "infostore_document", new Column("capture_date", "int8 DEFAULT NULL"));
            }

            if (!Tools.columnExists(con, "infostore_document", "geolocation")) {
                Tools.addColumns(con, "infostore_document", new Column("geolocation", "POINT DEFAULT NULL"));
            }

            if (!Tools.columnExists(con, "infostore_document", "width")) {
                Tools.addColumns(con, "infostore_document", new Column("width", "int8 UNSIGNED DEFAULT NULL"));
            }

            if (!Tools.columnExists(con, "infostore_document", "height")) {
                Tools.addColumns(con, "infostore_document", new Column("height", "int8 UNSIGNED DEFAULT NULL"));
            }

            if (!Tools.columnExists(con, "infostore_document", "camera_model")) {
                Tools.addColumns(con, "infostore_document", new Column("camera_model", "VARCHAR(128) DEFAULT NULL"));
            }

            if (!Tools.columnExists(con, "infostore_document", "iso_speed")) {
                Tools.addColumns(con, "infostore_document", new Column("iso_speed", "int8 UNSIGNED DEFAULT NULL"));
            }

            if (!Tools.columnExists(con, "infostore_document", "media_meta")) {
                Tools.addColumns(con, "infostore_document", new Column("media_meta", "MEDIUMBLOB DEFAULT NULL"));
            }

            if (!Tools.columnExists(con, "infostore_document", "media_status")) {
                Tools.addColumns(con, "infostore_document", new Column("media_status", "VARCHAR(16) DEFAULT NULL"));
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(con);
                }
                autocommit(con);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddMetaForInfostoreDocumentTable.class.getName() };
    }
}
