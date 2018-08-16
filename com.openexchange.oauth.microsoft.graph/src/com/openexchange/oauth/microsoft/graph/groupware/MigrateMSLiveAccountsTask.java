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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.oauth.microsoft.graph.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.oauth.KnownApi;

/**
 * {@link MigrateMSLiveAccountsTask} - Migrates i.e. renames the serviceId and displayName of
 * the old MSLive accounts to the new Microsoft Graph serviceId and displayName.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MigrateMSLiveAccountsTask implements UpdateTaskV2 {

    private static final String RENAME_SOURCE_ID = "UPDATE oauthAccounts SET serviceId=? WHERE serviceId=?";
    private static final String DEPRECATED_SERVICE_ID = "com.openexchange.oauth.msliveconnect";

    private static final String RENAME_DISPLAY_NAME = "UPDATE oauthAccounts SET displayName=? WHERE displayName=?";
    private static final String DEPRECATED_DISPLAY_NAME = "My MS Live account";

    /**
     * Initialises a new {@link MigrateMSLiveAccountsTask}.
     */
    public MigrateMSLiveAccountsTask() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(RENAME_SOURCE_ID)) {
            stmt.setString(1, KnownApi.MICROSOFT_GRAPH.getServiceId());
            stmt.setString(2, DEPRECATED_SERVICE_ID);
            stmt.executeUpdate();
        } catch (final SQLException x) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(x, x.getMessage());
        }
        try (PreparedStatement stmt = connection.prepareStatement(RENAME_DISPLAY_NAME)) {
            stmt.setString(1, "My " + KnownApi.MICROSOFT_GRAPH.getDisplayName() + " account");
            stmt.setString(2, DEPRECATED_DISPLAY_NAME);
            stmt.executeUpdate();
        } catch (final SQLException x) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(x, x.getMessage());
        }
        // Also rename accounts with numerical index, e.g. 'My MS Live account (1)' ?
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#getDependencies()
     */
    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#getAttributes()
     */
    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING);
    }

}
