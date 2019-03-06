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

package com.openexchange.mail.compose.impl.storage;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.mail.compose.Address;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.Message;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Security;
import com.openexchange.mail.compose.SharedAttachmentsInfo;

/**
 * {@link ImmutableMessage}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class ImmutableMessage implements Message {

    /**
     * Creates a new builder.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>ImmutableMessage</code> */
    public static class Builder {

        private Address from;
        private Address sender;
        private List<Address> to;
        private List<Address> cc;
        private List<Address> bcc;
        private String subject;
        private String content;
        private ContentType contentType;
        private boolean requestReadReceipt;
        private SharedAttachmentsInfo sharedAttachmentsInfo;
        private List<Attachment> attachments;
        private Meta meta;
        private Security security;
        private Priority priority;
        private boolean contentEncrypted;

        Builder() {
            super();
            priority = Priority.NORMAL;
            sharedAttachmentsInfo = SharedAttachmentsInfo.DISABLED;
            security = Security.DISABLED;
            meta = Meta.META_NEW;
            contentEncrypted = false;
        }

        public Builder withContentEncrypted(boolean contentEncrypted) {
            this.contentEncrypted = contentEncrypted;
            return this;
        }

        public Builder withFrom(Address from) {
            this.from = from;
            return this;
        }

        public Builder withSender(Address sender) {
            this.sender = sender;
            return this;
        }

        public Builder withTo(List<Address> to) {
            this.to = to;
            return this;
        }

        public Builder withCc(List<Address> cc) {
            this.cc = cc;
            return this;
        }

        public Builder withBcc(List<Address> bcc) {
            this.bcc = bcc;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withContent(String content) {
            this.content = content;
            return this;
        }

        public Builder withContentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder withRequestReadReceipt(boolean requestReadReceipt) {
            this.requestReadReceipt = requestReadReceipt;
            return this;
        }

        public Builder withSharedAttachmentsInfo(SharedAttachmentsInfo sharedAttachmentsInfo) {
            this.sharedAttachmentsInfo = sharedAttachmentsInfo;
            return this;
        }

        public Builder withAttachments(List<Attachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public Builder withMeta(Meta meta) {
            this.meta = meta;
            return this;
        }

        public Builder withSecurity(Security security) {
            this.security = security;
            return this;
        }

        public Builder withPriority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public Builder fromMessageDescription(MessageDescription md) {
            if (null != md) {
                from = md.getFrom();
                sender = md.getSender();
                to = md.getTo();
                cc = md.getCc();
                bcc = md.getBcc();
                subject = md.getSubject();
                content = md.getContent();
                contentType = md.getContentType();
                requestReadReceipt = md.isRequestReadReceipt();
                sharedAttachmentsInfo = md.getSharedAttachmentsInfo();
                attachments = md.getAttachments();
                meta = md.getMeta();
                security = md.getSecurity();
                priority = md.getPriority();
                contentEncrypted = md.isContentEncrypted();
            }
            return this;
        }

        public Builder fromMessage(Message m) {
            if (null != m) {
                from = m.getFrom();
                sender = m.getSender();
                to = m.getTo();
                cc = m.getCc();
                bcc = m.getBcc();
                subject = m.getSubject();
                content = m.getContent();
                contentType = m.getContentType();
                requestReadReceipt = m.isRequestReadReceipt();
                sharedAttachmentsInfo = m.getSharedAttachments();
                attachments = m.getAttachments();
                meta = m.getMeta();
                security = m.getSecurity();
                priority = m.getPriority();
                contentEncrypted = m.isContentEncrypted();
            }
            return this;
        }

        public ImmutableMessage build() {
            return new ImmutableMessage(from, sender, to, cc, bcc, subject, content, contentType, requestReadReceipt, sharedAttachmentsInfo, attachments, meta, security, priority, contentEncrypted);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Address from;
    private final Address sender;
    private final List<Address> to;
    private final List<Address> cc;
    private final List<Address> bcc;
    private final String subject;
    private final String content;
    private final ContentType contentType;
    private final boolean requestReadReceipt;
    private final SharedAttachmentsInfo sharedAttachmentsInfo;
    private final List<Attachment> attachments;
    private final Meta meta;
    private final Security security;
    private final Priority priority;
    private final boolean contentEncrypted;

    ImmutableMessage(Address from, Address sender, List<Address> to, List<Address> cc, List<Address> bcc, String subject, String content, ContentType contentType, boolean requestReadReceipt, SharedAttachmentsInfo sharedAttachmentsInfo, List<Attachment> attachments, Meta meta, Security security, Priority priority, boolean contentEncrypted) {
        super();
        this.from = from;
        this.sender = sender;
        this.to = immutableListFor(to);
        this.cc = immutableListFor(cc);
        this.bcc = immutableListFor(bcc);
        this.subject = subject;
        this.content = content;
        this.contentType = contentType;
        this.requestReadReceipt = requestReadReceipt;
        this.sharedAttachmentsInfo = sharedAttachmentsInfo;
        this.attachments = immutableListFor(attachments);
        this.meta = meta;
        this.security = security;
        this.priority = priority;
        this.contentEncrypted = contentEncrypted;
    }

    private static <E> List<E> immutableListFor(List<E> list) {
        if (null == list || list.isEmpty()) {
            return Collections.emptyList();
        }

        ImmutableList.Builder<E> il = ImmutableList.builderWithExpectedSize(list.size());
        for (E element : list) {
            if (null != element) {
                il.add(element);
            }
        }
        return il.build();
    }

    @Override
    public Address getFrom() {
        return from;
    }

    @Override
    public Address getSender() {
        return sender;
    }

    @Override
    public List<Address> getTo() {
        return to;
    }

    @Override
    public List<Address> getCc() {
        return cc;
    }

    @Override
    public List<Address> getBcc() {
        return bcc;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public boolean isRequestReadReceipt() {
        return requestReadReceipt;
    }

    @Override
    public SharedAttachmentsInfo getSharedAttachments() {
        return sharedAttachmentsInfo;
    }

    @Override
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @Override
    public Meta getMeta() {
        return meta;
    }

    @Override
    public Security getSecurity() {
        return security;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    @Override
    public boolean isContentEncrypted() {
        return contentEncrypted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (from != null) {
            sb.append("from=").append(from).append(", ");
        }
        if (sender != null) {
            sb.append("sender=").append(sender).append(", ");
        }
        if (to != null) {
            sb.append("to=").append(to).append(", ");
        }
        if (cc != null) {
            sb.append("cc=").append(cc).append(", ");
        }
        if (bcc != null) {
            sb.append("bcc=").append(bcc).append(", ");
        }
        if (subject != null) {
            sb.append("subject=").append(subject).append(", ");
        }
        if (content != null) {
            sb.append("content=").append(content).append(", ");
        }
        if (contentType != null) {
            sb.append("contentType=").append(contentType).append(", ");
        }
        sb.append("requestReadReceipt=").append(requestReadReceipt).append(", ");
        if (sharedAttachmentsInfo != null) {
            sb.append("sharedAttachmentsInfo=").append(sharedAttachmentsInfo).append(", ");
        }
        if (attachments != null) {
            sb.append("attachments=").append(attachments).append(", ");
        }
        if (meta != null) {
            sb.append("meta=").append(meta).append(", ");
        }
        if (security != null) {
            sb.append("security=").append(security).append(", ");
        }
        if (priority != null) {
            sb.append("priority=").append(priority);
        }
        sb.append("]");
        return sb.toString();
    }

}
