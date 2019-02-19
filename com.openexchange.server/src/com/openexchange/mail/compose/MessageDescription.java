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

import java.util.List;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.Message.Priority;

/**
 * {@link MessageDescription}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class MessageDescription {

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

    private boolean bFrom;
    private boolean bSender;
    private boolean bTo;
    private boolean bCc;
    private boolean bBcc;
    private boolean bSubject;
    private boolean bContent;
    private boolean bContentType;
    private boolean bRequestReadReceipt;
    private boolean bSharedAttachmentsInfo;
    private boolean bAttachments;
    private boolean bMeta;
    private boolean bSecurity;
    private boolean bPriority;
    private boolean bContentEncrypted;

    /**
     * Initializes a new {@link MessageDescription}.
     */
    public MessageDescription() {
        super();
        priority = Priority.NORMAL;
        sharedAttachmentsInfo = SharedAttachmentsInfo.DISABLED;
        security = Security.DISABLED;
        meta = Meta.META_NEW;
    }

    public Address getFrom() {
        return from;
    }

    public MessageDescription setFrom(Address from) {
        this.from = from;
        bFrom = true;
        return this;
    }

    public boolean containsFrom() {
        return bFrom;
    }

    public void removeFrom() {
        from = null;
        bFrom = false;
    }

    public Address getSender() {
        return sender;
    }

    public MessageDescription setSender(Address sender) {
        this.sender = sender;
        bSender = true;
        return this;
    }

    public boolean containsSender() {
        return bSender;
    }

    public void removeSender() {
        sender = null;
        bSender = false;
    }

    public List<Address> getTo() {
        return to;
    }

    public MessageDescription setTo(List<Address> to) {
        this.to = to;
        bTo = true;
        return this;
    }

    public boolean containsTo() {
        return bTo;
    }

    public void removeTo() {
        to = null;
        bTo = false;
    }

    public List<Address> getCc() {
        return cc;
    }

    public MessageDescription setCc(List<Address> cc) {
        this.cc = cc;
        bCc = true;
        return this;
    }

    public boolean containsCc() {
        return bCc;
    }

    public void removeCc() {
        cc = null;
        bCc = false;
    }

    public List<Address> getBcc() {
        return bcc;
    }

    public MessageDescription setBcc(List<Address> bcc) {
        this.bcc = bcc;
        bBcc = true;
        return this;
    }

    public boolean containsBcc() {
        return bBcc;
    }

    public void removeBcc() {
        bcc = null;
        bBcc = false;
    }

    public String getSubject() {
        return subject;
    }

    public MessageDescription setSubject(String subject) {
        this.subject = subject;
        bSubject = true;
        return this;
    }

    public boolean containsSubject() {
        return bSubject;
    }

    public void removeSubject() {
        subject = null;
        bSubject = false;
    }

    public String getContent() {
        return content;
    }

    public MessageDescription setContent(String content) {
        this.content = content;
        bContent = true;
        return this;
    }

    public boolean containsContent() {
        return bContent;
    }

    public void removeContent() {
        content = null;
        bContent = false;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public MessageDescription setContentType(ContentType contentType) {
        this.contentType = contentType;
        bContentType = true;
        return this;
    }

    public boolean containsContentType() {
        return bContentType;
    }

    public void removeContentType() {
        contentType = null;
        bContentType = false;
    }

    public boolean isContentEncrypted() {
        return contentEncrypted;
    }

    public MessageDescription setContentEncrypted(boolean contentEncrypted) {
        this.contentEncrypted = contentEncrypted;
        bContentEncrypted = true;
        return this;
    }

    public boolean containsContentEncrypted() {
        return bContentEncrypted;
    }

    public void removeContentEncrypted() {
        contentEncrypted = false;
        bContentEncrypted = false;
    }

    public boolean isRequestReadReceipt() {
        return requestReadReceipt;
    }

    public MessageDescription setRequestReadReceipt(boolean requestReadReceipt) {
        this.requestReadReceipt = requestReadReceipt;
        bRequestReadReceipt = true;
        return this;
    }

    public boolean containsRequestReadReceipt() {
        return bRequestReadReceipt;
    }

    public void removeRequestReadReceipt() {
        requestReadReceipt = false;
        bRequestReadReceipt = false;
    }

    public SharedAttachmentsInfo getSharedAttachmentsInfo() {
        return sharedAttachmentsInfo;
    }

    public MessageDescription setsharedAttachmentsInfo(SharedAttachmentsInfo sharedAttachmentsInfo) {
        this.sharedAttachmentsInfo = sharedAttachmentsInfo;
        bSharedAttachmentsInfo = true;
        return this;
    }

    public boolean containsSharedAttachmentsInfo() {
        return bSharedAttachmentsInfo;
    }

    public void removeSharedAttachmentsInfo() {
        sharedAttachmentsInfo = SharedAttachmentsInfo.DISABLED;
        bSharedAttachmentsInfo = false;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public MessageDescription setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        bAttachments = true;
        return this;
    }

    public boolean containsAttachments() {
        return bAttachments;
    }

    public void removeAttachments() {
        attachments = null;
        bAttachments = false;
    }

    public Meta getMeta() {
        return meta;
    }

    public MessageDescription setMeta(Meta meta) {
        this.meta = meta;
        bMeta = true;
        return this;
    }

    public boolean containsMeta() {
        return bMeta;
    }

    public void removeMeta() {
        meta = null;
        bMeta = false;
    }

    public Security getSecurity() {
        return security;
    }

    public MessageDescription setSecurity(Security security) {
        this.security = security;
        bSecurity = true;
        return this;
    }

    public boolean containsSecurity() {
        return bSecurity;
    }

    public void removeSecurity() {
        security = null;
        bSecurity = false;
    }

    public Priority getPriority() {
        return priority;
    }

    public MessageDescription setPriority(Priority priority) {
        this.priority = priority;
        bPriority = true;
        return this;
    }

    public boolean containsPriority() {
        return bPriority;
    }

    public void removePriority() {
        priority = null;
        bPriority = false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (from != null) {
            builder.append("from=").append(from).append(", ");
        }
        if (sender != null) {
            builder.append("sender=").append(sender).append(", ");
        }
        if (to != null) {
            builder.append("to=").append(to).append(", ");
        }
        if (cc != null) {
            builder.append("cc=").append(cc).append(", ");
        }
        if (bcc != null) {
            builder.append("bcc=").append(bcc).append(", ");
        }
        if (subject != null) {
            builder.append("subject=").append(subject).append(", ");
        }
        if (content != null) {
            builder.append("content=").append(content).append(", ");
        }
        if (contentType != null) {
            builder.append("contentType=").append(contentType).append(", ");
        }
        builder.append("requestReadReceipt=").append(requestReadReceipt).append(", ");
        if (sharedAttachmentsInfo != null) {
            builder.append("sharedAttachmentsInfo=").append(sharedAttachmentsInfo).append(", ");
        }
        if (attachments != null) {
            builder.append("attachments=").append(attachments).append(", ");
        }
        if (meta != null) {
            builder.append("meta=").append(meta).append(", ");
        }
        if (security != null) {
            builder.append("security=").append(security).append(", ");
        }
        if (priority != null) {
            builder.append("priority=").append(priority);
        }
        builder.append("]");
        return builder.toString();
    }

}
