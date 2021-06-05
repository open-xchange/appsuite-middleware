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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.database.Databases.startTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddMediaFieldsForInfostoreDocumentTableV3} - Extends infostore document tables by "meta" JSON BLOB.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddMediaFieldsForInfostoreDocumentTableV3 extends UpdateTaskAdapter {

    private final AddMediaFieldsForInfostoreDocumentTable delegate;

    /**
     * Initializes a new {@link AddMediaFieldsForInfostoreDocumentTableV3}.
     */
    public AddMediaFieldsForInfostoreDocumentTableV3() {
        super();
        delegate = new AddMediaFieldsForInfostoreDocumentTable();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (Tools.columnExists(con, "infostore_document", "camera_make")) {
                return;
            }

            startTransaction(con);
            rollback = 1;

            // Drop old contents
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement("UPDATE infostore_document SET capture_date=NULL, geolocation=NULL, width=NULL, height=NULL, camera_model=NULL, camera_iso_speed=NULL, camera_aperture=NULL, camera_exposure_time=NULL, camera_focal_length=NULL, media_meta=NULL, media_status=NULL");
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
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

        // Invoke delegate
        delegate.perform(params);
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddMediaFieldsForInfostoreDocumentTableV2.class.getName() };
    }
}
