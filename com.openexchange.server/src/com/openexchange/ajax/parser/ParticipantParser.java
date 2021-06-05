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

package com.openexchange.ajax.parser;

import static com.openexchange.java.Autoboxing.I;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ParticipantsFields;
import com.openexchange.ajax.parser.CalendarParser.FieldParser;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.AbstractConfirmableParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * {@link ParticipantParser}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ParticipantParser {

    public ParticipantParser() {
        super();
    }

    public ConfirmableParticipant parseConfirmation(boolean parseAll, JSONObject json) throws JSONException {
        JSONParticipant parsed = new JSONParticipant();
        parseField(parseAll, parsed, null, json);
        return parsed;
    }

    protected void parseField(boolean parseAll, JSONParticipant obj, TimeZone tz, JSONObject json) throws JSONException {
        for (FieldParser<Participant> parser : PARSERS) {
            parser.parse(parseAll, obj, tz, json);
        }
    }

    private static final FieldParser<JSONParticipant> TYPE_PARSER = new FieldParser<JSONParticipant>() {
        @Override
        public void parse(boolean parseAll, JSONParticipant obj, TimeZone timeZone, JSONObject json) throws JSONException {
            if (json.has(ParticipantsFields.TYPE)) {
                obj.setType(json.getInt(ParticipantsFields.TYPE));
            }
        }
    };
    private static final FieldParser<JSONParticipant> MAIL_PARSER = new FieldParser<JSONParticipant>() {
        @Override
        public void parse(boolean parseAll, JSONParticipant obj, TimeZone timeZone, JSONObject json) {
            if (json.has(ParticipantsFields.MAIL)) {
                obj.setEmailAddress(json.optString(ParticipantsFields.MAIL));
            }
        }
    };
    private static final FieldParser<JSONParticipant> DISPLAY_NAME_PARSER = new FieldParser<JSONParticipant>() {
        @Override
        public void parse(boolean parseAll, JSONParticipant obj, TimeZone timeZone, JSONObject json) {
            if (json.has(ParticipantsFields.DISPLAY_NAME)) {
                obj.setDisplayName(json.optString(ParticipantsFields.DISPLAY_NAME));
            }
        }
    };
    private static final FieldParser<ConfirmableParticipant> STATUS_PARSER = new FieldParser<ConfirmableParticipant>() {
        @Override
        public void parse(boolean parseAll, ConfirmableParticipant obj, TimeZone timeZone, JSONObject json) {
            if (json.has(ParticipantsFields.STATUS)) {
                obj.setStatus(ConfirmStatus.byId(json.optInt(ParticipantsFields.STATUS)));
            } else if (json.has(ParticipantsFields.CONFIRMATION)) {
                obj.setStatus(ConfirmStatus.byId(json.optInt(ParticipantsFields.CONFIRMATION)));
            }
        }
    };
    private static final FieldParser<ConfirmableParticipant> MESSAGE_PARSER = new FieldParser<ConfirmableParticipant>() {
        @Override
        public void parse(boolean parseAll, ConfirmableParticipant obj, TimeZone timeZone, JSONObject json) {
            if (json.has(ParticipantsFields.CONFIRM_MESSAGE)) {
                obj.setMessage(json.optString(ParticipantsFields.CONFIRM_MESSAGE));
            } else if (json.has(ParticipantsFields.MESSAGE)) {
                obj.setMessage(json.optString(ParticipantsFields.MESSAGE));
            }
        }
    };

    @SuppressWarnings("unchecked")
    private static final FieldParser<Participant>[] PARSERS = new FieldParser[] {
        TYPE_PARSER, MAIL_PARSER, DISPLAY_NAME_PARSER, STATUS_PARSER, MESSAGE_PARSER };

    private static final class JSONParticipant extends AbstractConfirmableParticipant implements Comparable<Participant> {

        private static final long serialVersionUID = -3063859164091177034L;

        private int type;
        private int identifier;
        private String emailAddress;
        private String displayName;
        private boolean ignoreNotification;

        JSONParticipant() {
            super();
        }

        private JSONParticipant(JSONParticipant copy) {
            super(copy);
            type = copy.getType();
            identifier = copy.getIdentifier();
            emailAddress = copy.getEmailAddress();
            displayName = copy.getDisplayName();
            ignoreNotification = copy.isIgnoreNotification();
        }

        @Override
        public ConfirmableParticipant getClone() {
            return new JSONParticipant(this);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getEmailAddress() {
            return emailAddress == null ? null : emailAddress.toLowerCase();
        }

        @Override
        public int getIdentifier() {
            return identifier;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public boolean isIgnoreNotification() {
            return ignoreNotification;
        }

        @Override
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @Deprecated
        @Override
        public void setIdentifier(int id) {
            this.identifier = id;
        }

        @Override
        public void setIgnoreNotification(boolean ignoreNotification) {
            this.ignoreNotification = ignoreNotification;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress == null ? null : emailAddress.toLowerCase();
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public JSONParticipant clone() throws CloneNotSupportedException {
            JSONParticipant retval = (JSONParticipant) super.clone();

            retval.displayName = this.displayName;
            retval.emailAddress = this.emailAddress;
            retval.identifier = this.identifier;
            retval.ignoreNotification = this.ignoreNotification;
            retval.type = this.type;

            return retval;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
            result = prime * result + type;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            JSONParticipant other = (JSONParticipant) obj;
            if (emailAddress == null) {
                if (other.emailAddress != null) {
                    return false;
                }
            } else if (!emailAddress.equals(other.emailAddress)) {
                return false;
            }
            if (type != other.type) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(Participant part) {
            final int retval;
            if (EXTERNAL_USER == part.getType()) {
                if (null == getEmailAddress()) {
                    if (null == part.getEmailAddress()) {
                        retval = 0;
                    } else {
                        retval = -1;
                    }
                } else {
                    if (null == part.getEmailAddress()) {
                        retval = 1;
                    } else {
                        retval = getEmailAddress().compareTo(part.getEmailAddress());
                    }
                }
            } else {
                retval = I(EXTERNAL_USER).compareTo(I(part.getType()));
            }
            return retval;
        }
    }
}
