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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.WorkingLevel;

/**
 * {@link DeleteOXMFSubscriptionTask} - Deletes leftovers of OXMF aka. microformats from the subscriptions table
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public class DeleteOXMFSubscriptionTask implements UpdateTaskV2 {

    /**
     * The MF contacts source id from MicroformatSubscribeService
     */
    private static final String CONTACTS_SOURCE_ID = "com.openexchange.subscribe.microformats.contacts.http";

    /**
     * The infostore a.k.a. files a.k.a. drive source id from MicroformatSubscribeService
     */
    private static final String INFOSTORE_SOURCE_ID = "com.openexchange.subscribe.microformats.infostore.http";

    private static final String DELETE_STATEMENT = "DELETE FROM subscriptions WHERE source_id=?";

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteOXMFSubscriptionTask.class);

    /**
     * Initializes a new {@link DeleteOXMFSubscriptionTask}.
     * 
     */
    public DeleteOXMFSubscriptionTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        boolean rollback = false;

        LOGGER.info("Start deleting OXMF subscriptions from \"subscriptions\" table.");
        try {
            startTransaction(con);
            rollback = true;

            deleteSource(con, CONTACTS_SOURCE_ID);
            deleteSource(con, INFOSTORE_SOURCE_ID);

            con.commit();
            rollback = false;
            LOGGER.info("{} has been finished successful. OXMF related data has been dropped from \"subscriptions\" table.", this.getClass().getName());
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
        }

    }

    private void deleteSource(Connection con, String sourceId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(DELETE_STATEMENT);
            stmt.setString(1, sourceId);

            int i = stmt.executeUpdate();
            LOGGER.info("{} rows has been removed for the source {} from \"subscriptions\" table.", Integer.valueOf(i), sourceId);
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { DropPublicationTablesTask.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND, WorkingLevel.SCHEMA);
    }

}
