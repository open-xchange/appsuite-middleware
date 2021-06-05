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

package com.openexchange.groupware.tasks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;

/**
 * {@link ParticipantMapping}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ParticipantMapping {

    private ParticipantMapping() {
        // prevent instantiation
    }

    public static final Mapper<? extends Object>[] EXTERNAL_MAPPERS = new Mapper<?>[] { 
        
        new Mapper<Participant[]>() {

        @Override
        public int getId() {
            return CalendarObject.PARTICIPANTS;
        }

        @Override
        public boolean isSet(final Task task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getDBColumnName() {
            return ParticipantsFields.MAIL;
        }

        @Override
        public String getDisplayName() {
            return ParticipantsFields.MAIL;
        }

        @Override
        public void toDB(final PreparedStatement stmt, final int pos, final Task task) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void fromDB(final ResultSet result, final int pos, final Task task) throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(final Task task1, final Task task2) {
            return Mapping.equals(task1.getParticipants(), task2.getParticipants());
        }

        @Override
        public Participant[] get(Task task) {
            return task.getParticipants();
        }

        @Override
        public void set(Task task, Participant[] value) {
            task.setParticipants(value);
        }
    }

    };

}
