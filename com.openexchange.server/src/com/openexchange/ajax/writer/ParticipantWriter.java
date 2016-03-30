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

package com.openexchange.ajax.writer;

import static com.openexchange.ajax.writer.DataWriter.writeParameter;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.writer.DataWriter.FieldWriter;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.session.Session;

/**
 * {@link ParticipantWriter}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ParticipantWriter {

    public ParticipantWriter() {
        super();
    }

    public void write(ConfirmableParticipant participant, JSONObject json, Session session) throws JSONException {
        if (participant instanceof ExternalUserParticipant) {
            write((ExternalUserParticipant) participant, json, session);
        }
    }

    public void write(ExternalUserParticipant participant, JSONObject json, Session session) throws JSONException {
        for (FieldWriter<Participant> writer : EXTERNAL_WRITERS) {
            writer.write(participant, null, json, session);
        }
    }

    protected static final FieldWriter<Participant> TYPE_WRITER = new FieldWriter<Participant>() {
        @Override
        public void write(Participant obj, TimeZone timeZone, JSONArray json, Session session) {
            throw new UnsupportedOperationException("JSON array writing is not supported for participants.");
        }
        @Override
        public void write(Participant obj, TimeZone timeZone, JSONObject json, Session session) throws JSONException {
            writeParameter(ParticipantsFields.TYPE, obj.getType(), json, obj.getType() > 0);
        }
    };

    protected static final FieldWriter<ExternalUserParticipant> MAIL_WRITER = new FieldWriter<ExternalUserParticipant>() {
        @Override
        public void write(ExternalUserParticipant obj, TimeZone timeZone, JSONArray json, Session session) {
            throw new UnsupportedOperationException("JSON array writing is not supported for participants.");
        }
        @Override
        public void write(ExternalUserParticipant obj, TimeZone timeZone, JSONObject json, Session session) throws JSONException {
            writeParameter(ParticipantsFields.MAIL, obj.getEmailAddress(), json);
        }
    };

    protected static final FieldWriter<Participant> DISPLAY_NAME_WRITER = new FieldWriter<Participant>() {
        @Override
        public void write(Participant obj, TimeZone timeZone, JSONArray json, Session session) {
            throw new UnsupportedOperationException("JSON array writing is not supported for participants.");
        }
        @Override
        public void write(Participant obj, TimeZone timeZone, JSONObject json, Session session) throws JSONException {
            writeParameter(ParticipantsFields.DISPLAY_NAME, obj.getDisplayName(), json);
        }
    };

    protected static final FieldWriter<ConfirmableParticipant> STATUS_WRITER = new FieldWriter<ConfirmableParticipant>() {
        @Override
        public void write(ConfirmableParticipant obj, TimeZone timeZone, JSONArray json, Session session) {
            throw new UnsupportedOperationException("JSON array writing is not supported for participants.");
        }
        @Override
        public void write(ConfirmableParticipant obj, TimeZone timeZone, JSONObject json, Session session) throws JSONException {
            writeParameter(ParticipantsFields.STATUS, obj.getStatus().getId(), json, obj.containsStatus());
        }
    };

    protected static final FieldWriter<ConfirmableParticipant> MESSAGE_WRITER = new FieldWriter<ConfirmableParticipant>() {
        @Override
        public void write(ConfirmableParticipant obj, TimeZone timeZone, JSONArray json, Session session) {
            throw new UnsupportedOperationException("JSON array writing is not supported for participants.");
        }
        @Override
        public void write(ConfirmableParticipant obj, TimeZone timeZone, JSONObject json, Session session) throws JSONException {
            writeParameter(ParticipantsFields.MESSAGE, obj.getMessage(), json, obj.containsMessage());
        }
    };

    @SuppressWarnings("unchecked")
    private static final FieldWriter<Participant>[] EXTERNAL_WRITERS = (FieldWriter<Participant>[]) new FieldWriter<?>[] {
        TYPE_WRITER, MAIL_WRITER, DISPLAY_NAME_WRITER, STATUS_WRITER, MESSAGE_WRITER };
}
