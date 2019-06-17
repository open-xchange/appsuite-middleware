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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
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

        public Builder applyFromDraft(Meta meta) {
            if (null == meta) {
                return this;
            }

            type = meta.getType();
            replyFor = meta.getReplyFor();
            forwardsFor = meta.getForwardsFor();
            date = meta.getDate();
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
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("[");
        if (type != null) {
            builder2.append("type=").append(type).append(", ");
        }
        if (date != null) {
            builder2.append("date=").append(date).append(", ");
        }
        if (replyFor != null) {
            builder2.append("replyFor=").append(replyFor).append(", ");
        }
        if (forwardsFor != null) {
            builder2.append("forwardsFor=").append(forwardsFor).append(", ");
        }
        if (editFor != null) {
            builder2.append("editFor=").append(editFor);
        }
        builder2.append("]");
        return builder2.toString();
    }

}
