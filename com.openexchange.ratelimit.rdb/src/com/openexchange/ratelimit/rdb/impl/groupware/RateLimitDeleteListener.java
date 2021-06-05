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

package com.openexchange.ratelimit.rdb.impl.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;

/**
 *
 * {@link RateLimitDeleteListener}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class RateLimitDeleteListener implements DeleteListener {

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        int type = event.getType();
        if (type == DeleteEvent.TYPE_CONTEXT) {
            int ctxId = event.getId();
            try (PreparedStatement stmt = writeCon.prepareStatement("DELETE FROM ratelimit WHERE cid=?")) {
                stmt.setInt(1, ctxId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            }
        } else if (type == DeleteEvent.TYPE_USER) {
            int userId = event.getId();
            int ctxId = event.getContext().getContextId();
            try (PreparedStatement stmt = writeCon.prepareStatement("DELETE FROM ratelimit WHERE cid=? AND userId=?")) {
                stmt.setInt(1, ctxId);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            }
        }
    }

}
