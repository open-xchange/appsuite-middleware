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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.passwordchange.importer.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.Database;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.passwordchange.PasswordChangeEvent;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ImporterPasswordChange} - Omits security checks like authenticating old password or look-up of appropriate user permission. The
 * password is passed to storage as it is; meaning no encoding or crypting takes place before.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImporterPasswordChange extends PasswordChangeService {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.LogFactory.getLog(ImporterPasswordChange.class);

    /**
     * Initializes a new {@link ImporterPasswordChange}
     */
    public ImporterPasswordChange() {
        super();
    }

    @Override
    protected void allow(final PasswordChangeEvent event) {
        // No permission check here
    }

    @Override
    protected void check(final PasswordChangeEvent event) {
        // Nothing to check here
    }

    @Override
    protected void update(final PasswordChangeEvent event) throws UserException {
        final String encodedPassword = event.getNewPassword();
        /*
         * Update database
         */
        final Connection writeCon;
        try {
            writeCon = Database.get(event.getContext(), true);
        } catch (final DBPoolingException e) {
            throw new UserException(e);
        }
        try {
            writeCon.setAutoCommit(false);
            update(writeCon, encodedPassword, event.getSession().getUserId(), event.getContext().getContextId());
            writeCon.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(writeCon);
            throw new UserException(UserException.Code.SQL_ERROR, e);
        } finally {
            try {
                writeCon.setAutoCommit(true);
            } catch (final SQLException e) {
                LOG.error("Problem setting autocommit to true.", e);
            }
            Database.back(event.getContext(), true, writeCon);
        }
    }

    private static final String SQL_UPDATE = "UPDATE user SET userPassword = ? WHERE cid = ? AND id = ?";

    private void update(final Connection writeCon, final String encodedPassword, final int userId, final int cid) throws SQLException {
        PreparedStatement stmt = null;
        final ResultSet result = null;
        try {
            stmt = writeCon.prepareStatement(SQL_UPDATE);
            int pos = 1;
            stmt.setString(pos++, encodedPassword);
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, userId);
            stmt.executeUpdate();
        } finally {
            DBUtils.closeSQLStuff(result, stmt);
        }
    }
}
