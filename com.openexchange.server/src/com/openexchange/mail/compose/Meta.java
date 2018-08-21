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
    public static enum MetaType {
        /**
         * A new mail is compiled.
         */
        NEW("new"),
        /**
         * A reply to an existent mail is compiled.
         */
        REPLY("reply"),
        /**
         * A reply-all to an existent mail is compiled.
         */
        REPLY_ALL("replyall"),
        /**
         * A forward for an existent mail is compiled.
         */
        FORWARD_INLINE("forward-inline"),
        /**
         * A forward for an existent mail is compiled.
         */
        FORWARD_ATTACHMENT("forward-attachment"),
        /**
         * A continuation of an existent draft (aka "edit draft") is compiled.
         */
        EDIT("edit"),
        /**
         * A copy of an existent draft template is compiled. No reference is suppose to be kept.
         */
        COPY("copy"),
        /**
         * A resend/bounce of an existent mail is compiled.
         */
        RESEND("resend");

        private final String id;

        private MetaType(String id) {
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

        private static final Map<String, MetaType> MAP;
        static {
            MetaType[] types = MetaType.values();
            ImmutableMap.Builder<String, MetaType> m = ImmutableMap.builderWithExpectedSize(types.length);
            for (MetaType type : types) {
                m.put(type.getId(), type);
            }
            MAP = m.build();
        }

        /**
         * Gets the type for specified type identifier.
         *
         * @param type The type identifier
         * @return The type associated with the given identifier or <code>null</code> if there is no such type
         */
        public static MetaType typeFor(String type) {
            if (null == type) {
                return null;
            }

            return MAP.get(Strings.asciiLowerCase(type));
        }

        /**
         * Gets the meta-specific type for specified type.
         *
         * @param type The type
         * @return The mapped type or <code>null</code> if there is no mappable type
         */
        public static MetaType typeFor(Type type) {
            if (null == type) {
                return null;
            }

            switch (type) {
                case COPY:
                    return MetaType.COPY;
                case EDIT:
                    return MetaType.EDIT;
                case FORWARD:
                    return MetaType.FORWARD_INLINE;
                case NEW:
                    return MetaType.NEW;
                case REPLY:
                    return MetaType.REPLY;
                case REPLY_ALL:
                    return MetaType.REPLY_ALL;
                case RESEND:
                    return MetaType.RESEND;
                default:
                    break;
            }

            return null;
        }
    }

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
