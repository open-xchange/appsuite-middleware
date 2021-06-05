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

package com.openexchange.user.copy.internal.additional;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.user.User;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.chronos.ChronosCopyTask;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.contact.ContactCopyTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.tasks.TaskCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link AdditionalCopyTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AdditionalCopyTask implements CopyUserTaskService {

    private static final String UPDATE_USER =
        "UPDATE " +
            "user " +
        "SET " +
            "contactId = ? " +
        "WHERE " +
            "cid = ? " +
        "AND " +
            "id = ?";


    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName(),
            ChronosCopyTask.class.getName(),
            ContactCopyTask.class.getName(),
            TaskCopyTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return "additional";
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final User srcUsr = copyTools.getSourceUser();
        final Connection dstCon = copyTools.getDestinationConnection();

        final ObjectMapping<Integer> contactMapping = copyTools.checkAndExtractGenericMapping(Contact.class.getName());
        final Integer srcContact = contactMapping.getSource(srcUsr.getContactId());
        if (srcContact == null) {
            throw UserCopyExceptionCodes.USER_CONTACT_MISSING.create(I(srcUsr.getId()), srcCtxId);
        }

        final Integer dstContact = contactMapping.getDestination(srcContact);
        if (dstContact == null) {
            throw UserCopyExceptionCodes.USER_CONTACT_MISSING.create(I(srcUsr.getId()), dstCtxId);
        }

        correctUsersContactId(dstCon, i(dstUsrId), i(dstCtxId), i(dstContact));

        return null;
    }

    private void correctUsersContactId(final Connection con, final int uid, final int cid, final int contact) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(UPDATE_USER);
            stmt.setInt(1, contact);
            stmt.setInt(2, cid);
            stmt.setInt(3, uid);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
