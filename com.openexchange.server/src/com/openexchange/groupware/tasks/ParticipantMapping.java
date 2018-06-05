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
