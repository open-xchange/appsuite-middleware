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

package com.openexchange.contact.storage.rdb.sql;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.java.Autoboxing.I;
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
            logger.info("Corrected number of images in prg_contacts, {} rows affected.", I(updated));
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
