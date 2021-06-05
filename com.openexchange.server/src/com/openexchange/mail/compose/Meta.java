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

package com.openexchange.mail.compose;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.java.ISO8601Utils;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailPath;

/**
 * {@link Meta} - Provides meta information for a message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class Meta {

    /** The type for the meta in formation */
    public static class MetaType {

        private final String id;

        /**
         * Initializes a new {@link MetaType}.
         *
         * @param id The identifier
         * @throws IllegalArgumentException If identifier is <code>null</code> or empty
         */
        private MetaType(String id) {
            super();
            if (Strings.isEmpty(id)) {
                throw new IllegalArgumentException("Identifier must not be null or empty");
            }
            this.id = id;
        }

        /**
         * Gets the type identifier.
         *
         * @return The type identifier
         */
        public String getId() {
            return id;
        }


        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MetaType)) {
                return false;
            }
            MetaType other = (MetaType) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return id;
        }

        /**
         * A new mail is compiled.
         */
        public static final MetaType NEW = new MetaType("new");

        /**
         * A reply to an existent mail is compiled.
         */
        public static final MetaType REPLY = new MetaType("reply");

        /**
         * A reply-all to an existent mail is compiled.
         */
        public static final MetaType REPLY_ALL = new MetaType("replyall");

        /**
         * A forward for an existent mail is compiled.
         */
        public static final MetaType FORWARD_INLINE = new MetaType("forward-inline");

        /**
         * A forward for an existent mail is compiled.
         */
        public static final MetaType FORWARD_ATTACHMENT = new MetaType("forward-attachment");

        /**
         * A continuation of an existent draft (aka "edit draft") is compiled.
         */
        public static final MetaType EDIT = new MetaType("edit");

        /**
         * A copy of an existent draft template is compiled. No reference is suppose to be kept.
         */
        public static final MetaType COPY = new MetaType("copy");

        /**
         * A resend/bounce of an existent mail is compiled.
         */
        public static final MetaType RESEND = new MetaType("resend");

        /**
         * A new SMS message is compiled.
         */
        public static final MetaType SMS = new MetaType("sms");

        /**
         * A new FAX message is compiled.
         */
        public static final MetaType FAX = new MetaType("fax");

        private static final Map<String, MetaType> META_TYPES;

        static {
            ImmutableMap.Builder<String, MetaType> m2 = ImmutableMap.builderWithExpectedSize(10);
            m2.put(MetaType.COPY.getId(), MetaType.COPY);
            m2.put(MetaType.EDIT.getId(), MetaType.EDIT);
            m2.put(MetaType.FAX.getId(), MetaType.FAX);
            m2.put(MetaType.FORWARD_ATTACHMENT.getId(), MetaType.FORWARD_ATTACHMENT);
            m2.put(MetaType.FORWARD_INLINE.getId(), MetaType.FORWARD_INLINE);
            m2.put(MetaType.NEW.getId(), MetaType.NEW);
            m2.put(MetaType.REPLY.getId(), MetaType.REPLY);
            m2.put(MetaType.REPLY_ALL.getId(), MetaType.REPLY_ALL);
            m2.put(MetaType.RESEND.getId(), MetaType.RESEND);
            m2.put(MetaType.SMS.getId(), MetaType.SMS);
            META_TYPES = m2.build();
        }

        /**
         * Gets the meta type for specified type identifier.
         *
         * @param type The meta type identifier
         * @return The meta type associated with the given identifier
         */
        public static MetaType typeFor(String type) {
            if (null == type) {
                return null;
            }

            MetaType t = META_TYPES.get(Strings.asciiLowerCase(type));
            return t == null ? new MetaType(type) : t;
        }

        /**
         * Gets the meta-specific type for specified type.
         *
         * @param type The type
         * @return The mapped type
         */
        public static MetaType metaTypeFor(Type type) {
            if (null == type) {
                return null;
            }

            switch (type.getId()) {
                case "copy":
                    return MetaType.COPY;
                case "edit":
                    return MetaType.EDIT;
                case "forward":
                    return MetaType.FORWARD_INLINE;
                case "new":
                    return MetaType.NEW;
                case "reply":
                    return MetaType.REPLY;
                case "replyall":
                    return MetaType.REPLY_ALL;
                case "resend":
                    return MetaType.RESEND;
                case "sms":
                    return MetaType.SMS;
                case "fax":
                    return MetaType.FAX;
                default:
                    break;
            }

            return new MetaType(type.getId());
        }
    } // End of class MetaType

    /**
     * Creates a new builder.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>Meta</code> */
    public static class Builder {

        private MetaType type;
        private Date date;
        private MailPath replyFor;
        private List<MailPath> forwardsFor;
        private MailPath editFor;

        /**
         * Initializes a new {@link Meta.Builder}.
         */
        public Builder() {
            super();
            type = MetaType.NEW;
        }

        public Builder withType(MetaType type) {
            this.type = type;
            return this;
        }

        public Builder withDate(Date date) {
            this.date = date;
            return this;
        }

        public Builder withReplyFor(MailPath replyFor) {
            this.replyFor = replyFor;
            return this;
        }

        public Builder withForwardsFor(List<MailPath> forwardsFor) {
            this.forwardsFor = forwardsFor;
            return this;
        }

        public Builder withEditFor(MailPath editFor) {
            this.editFor = editFor;
            return this;
        }

        public Builder applyFromDraft(Meta metaFromDraft) {
            if (null == metaFromDraft) {
                return this;
            }

            type = metaFromDraft.getType();
            replyFor = metaFromDraft.getReplyFor();
            forwardsFor = metaFromDraft.getForwardsFor();
            date = metaFromDraft.getDate();
            return this;
        }

        public Meta build() {
            return new Meta(type, date, replyFor, forwardsFor, editFor);
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /** The constant meta information for a new message */
    public static final Meta META_NEW = new Meta(MetaType.NEW, null, null, null, null);

    /** The constant meta information for a new SMS message */
    public static final Meta META_SMS = new Meta(MetaType.SMS, null, null, null, null);

    /** The constant meta information for a new FAX message */
    public static final Meta META_FAX = new Meta(MetaType.FAX, null, null, null, null);

    private final MetaType type;
    private final Date date;
    private final MailPath replyFor;
    private final List<MailPath> forwardsFor;
    private final MailPath editFor;

    Meta(MetaType type, Date date, MailPath replyFor, List<MailPath> forwardsFor, MailPath editFor) {
        super();
        this.type = type;
        this.date = date;
        this.replyFor = replyFor;
        this.forwardsFor = forwardsFor;
        this.editFor = editFor;
    }

    /**
     * Gets the type.
     *
     * @return The type
     */
    public MetaType getType() {
        return type;
    }

    /**
     * Gets the date of the referenced message (in case of a reply/forward).
     *
     * @return The date or <code>null</code>
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the reference to the message, for which a reply is being composed.
     *
     * @return The reference to the message, which is being replied
     */
    public MailPath getReplyFor() {
        return replyFor;
    }

    /**
     * Gets the references to the messages, for which a forward is being composed.
     *
     * @return The references to the messages, which are being forwarded
     */
    public List<MailPath> getForwardsFor() {
        return forwardsFor;
    }

    /**
     * Gets the reference to the draft message, which is being edited.
     *
     * @return The reference to the draft message, which is being edited
     */
    public MailPath getEditFor() {
        return editFor;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((editFor == null) ? 0 : editFor.hashCode());
        result = prime * result + ((replyFor == null) ? 0 : replyFor.hashCode());
        result = prime * result + ((forwardsFor == null) ? 0 : forwardsFor.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Meta)) {
            return false;
        }
        Meta other = (Meta) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (editFor == null) {
            if (other.editFor != null) {
                return false;
            }
        } else if (!editFor.equals(other.editFor)) {
            return false;
        }
        if (replyFor == null) {
            if (other.replyFor != null) {
                return false;
            }
        } else if (!replyFor.equals(other.replyFor)) {
            return false;
        }
        if (forwardsFor == null) {
            if (other.forwardsFor != null) {
                return false;
            }
        } else if (!forwardsFor.equals(other.forwardsFor)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        if (type != null) {
            sb.append("type=").append(type);
        }
        if (date != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("date=").append(ISO8601Utils.format(date, false));
        }
        if (replyFor != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("replyFor=").append(replyFor);
        }
        if (forwardsFor != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("forwardsFor=").append(forwardsFor);
        }
        if (editFor != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("editFor=").append(editFor);
        }
        sb.append(']');
        return sb.toString();
    }

}
