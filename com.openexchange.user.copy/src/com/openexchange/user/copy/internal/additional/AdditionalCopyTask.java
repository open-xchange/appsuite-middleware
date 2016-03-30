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

package com.openexchange.user.copy.internal.additional;

import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.calendar.CalendarCopyTask;
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
            CalendarCopyTask.class.getName(),
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
            throw UserCopyExceptionCodes.USER_CONTACT_MISSING.create(srcUsr.getId(), srcCtxId);
        }

        final Integer dstContact = contactMapping.getDestination(srcContact);
        if (dstContact == null) {
            throw UserCopyExceptionCodes.USER_CONTACT_MISSING.create(srcUsr.getId(), dstCtxId);
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
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#done(java.util.Map, boolean)
     */
    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
    }

}
