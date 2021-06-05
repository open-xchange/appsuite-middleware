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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.exception.OXException;

/**
 * {@link OXFolderDependentUtil}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class OXFolderDependentUtil {

    /**
     * Initializes a new {@link OXFolderDependentUtil}.
     */
    private OXFolderDependentUtil() {
        // prevent initialization
    }

    private static final String SQL_HAS_SUBSCRIPTION = "SELECT 1 FROM subscriptions WHERE cid=? AND folder_id=?;";

    /**
     * Checks if the given folder has a subscription or not.
     *
     * @param con The connection to use
     * @return true if it has a subscription, false otherwise
     * @throws OXException
     */
    public static boolean hasSubscription(Connection con, int cid, String fid) throws OXException {
        try (PreparedStatement stmt = con.prepareStatement(SQL_HAS_SUBSCRIPTION)) {
            int index = 1;
            stmt.setInt(index++, cid);
            stmt.setString(index++, fid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

}
