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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;

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
        private Address replyTo;
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
        private Map<String, String> customHeaders;
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

        public Builder withReplyTo(Address replyTo) {
            this.replyTo = replyTo;
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

        public Builder withCustomHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
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
                replyTo = md.getReplyTo();
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
                customHeaders = md.getCustomHeaders();
            }
            return this;
        }

        public Builder fromMessage(Message m) {
            if (null != m) {
                from = m.getFrom();
                sender = m.getSender();
                replyTo = m.getReplyTo();
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
                customHeaders = m.getCustomHeaders();
            }
            return this;
        }

        public ImmutableMessage build() {
            return new ImmutableMessage(from, sender, replyTo, to, cc, bcc, subject, content, contentType, requestReadReceipt, sharedAttachmentsInfo, attachments, meta, security, priority, contentEncrypted, customHeaders);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Address from;
    private final Address sender;
    private final Address replyTo;
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
    private final Map<String, String> customHeaders;
    private final Security security;
    private final Priority priority;
    private final boolean contentEncrypted;

    ImmutableMessage(Address from, Address sender, Address replyTo, List<Address> to, List<Address> cc, List<Address> bcc, String subject, String content, ContentType contentType, boolean requestReadReceipt, SharedAttachmentsInfo sharedAttachmentsInfo, List<Attachment> attachments, Meta meta, Security security, Priority priority, boolean contentEncrypted, Map<String, String> customHeaders) {
        super();
        this.from = from;
        this.sender = sender;
        this.replyTo = replyTo;
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
        this.customHeaders = customHeaders;
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
    public Address getReplyTo() {
        return replyTo;
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
    public Map<String, String> getCustomHeaders() {
        return customHeaders;
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
