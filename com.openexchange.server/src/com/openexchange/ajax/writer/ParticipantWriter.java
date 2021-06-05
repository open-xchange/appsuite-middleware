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
