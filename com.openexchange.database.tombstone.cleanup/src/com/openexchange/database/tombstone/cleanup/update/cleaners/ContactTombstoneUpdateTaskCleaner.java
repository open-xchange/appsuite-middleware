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

package com.openexchange.database.tombstone.cleanup.update.cleaners;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import com.openexchange.database.tombstone.cleanup.cleaners.ContactTombstoneCleaner;

/**
 * {@link ContactTombstoneUpdateTaskCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class ContactTombstoneUpdateTaskCleaner extends ContactTombstoneCleaner {

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        // Removes entries from both tables where the relation matches
        String deleteEntriesWithConstraints = "DELETE FROM del_contacts, del_contacts_image USING del_contacts INNER JOIN del_contacts_image ON del_contacts.cid = del_contacts_image.cid AND del_contacts.intfield01 = del_contacts_image.intfield01 WHERE del_contacts.changing_date < ?";
        delete(connection, timestamp, deleteEntriesWithConstraints);

        try (Statement createStatement = connection.createStatement()) {
            createStatement.addBatch("CREATE TABLE del_contacts_new LIKE del_contacts;");
            createStatement.addBatch("ALTER TABLE del_contacts_new ENGINE = InnoDB;");
            createStatement.addBatch("INSERT INTO del_contacts_new SELECT * FROM del_contacts WHERE changing_date >= " + timestamp + ";");
            createStatement.addBatch("RENAME TABLE del_contacts TO del_contacts_old, del_contacts_new TO del_contacts;");
            createStatement.addBatch("DROP TABLE del_contacts_old");
            createStatement.executeBatch();
        }

        return Collections.emptyMap();
    }
}
