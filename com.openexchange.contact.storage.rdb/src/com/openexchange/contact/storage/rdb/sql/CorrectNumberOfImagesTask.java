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

package com.openexchange.contact.storage.rdb.sql;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link CorrectNumberOfImagesTask}
 *
 * Sets the number of images (intfield04) column in prg_contacts to <code>NULL</code> for entries without corresponding data in prg_contacts_image.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CorrectNumberOfImagesTask extends UpdateTaskAdapter {

    public CorrectNumberOfImagesTask() {
		super();
	}

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connnection = params.getConnection();
        int rollback = 0;
        PreparedStatement statement = null;
        try {
            connnection.setAutoCommit(false);
            rollback = 1;

            statement = connnection.prepareStatement("UPDATE prg_contacts SET intfield04 = NULL WHERE intfield04 > 0 AND NOT EXISTS (SELECT intfield01 FROM prg_contacts_image WHERE prg_contacts.intfield01 = prg_contacts_image.intfield01 AND prg_contacts.cid = prg_contacts_image.cid)");
            int updated = statement.executeUpdate();

            connnection.commit();
            rollback = 2;

            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CorrectNumberOfImagesTask.class);
            logger.info("Corrected number of images in prg_contacts, {} rows affected.", updated);
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement);
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(connnection);
                }
                autocommit(connnection);
            }
        }
    }

}
