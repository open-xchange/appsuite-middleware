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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.chat.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.chat.ChatAttachment;
import com.openexchange.chat.Message;

/**
 * {@link MessageImpl} - The basic {@link Message message} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessageImpl extends PacketImpl implements Message {

    private Type type;

    private String subject;

    private String text;

    private final List<ChatAttachment> attachments;

    /**
     * Initializes a new {@link MessageImpl}.
     */
    public MessageImpl() {
        super();
        attachments = new CopyOnWriteArrayList<ChatAttachment>();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public List<ChatAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    /**
     * Removes specified attachment.
     *
     * @param attachment The attachment
     */
    public void removeAttachments(final ChatAttachment attachment) {
        if (attachment == null) {
            return;
        }
        attachments.remove(attachment);
    }

    /**
     * Adds specified attachment.
     *
     * @param attachment The attachment
     */
    public void addAttachments(final ChatAttachment attachment) {
        if (attachment == null) {
            return;
        }
        attachments.add(attachment);
    }

    /**
     * Adds specified attachments.
     *
     * @param attachments The attachments
     */
    public void addAttachments(final Collection<ChatAttachment> attachments) {
        if (attachments == null) {
            return;
        }
        this.attachments.addAll(attachments);
    }

    /**
     * Sets the type
     *
     * @param type The type to set
     */
    public void setType(final Type type) {
        this.type = type;
    }

    /**
     * Sets the subject
     *
     * @param subject The subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * Sets the text
     *
     * @param text The text to set
     */
    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("MessageImpl {");
        final String delim = ", ";
        if (type != null) {
            builder.append("type=").append(type).append(delim);
        }
        if (subject != null) {
            builder.append("subject=").append(subject).append(delim);
        }
        if (text != null) {
            builder.append("text=").append(text).append(delim);
        }
        if (attachments != null) {
            builder.append("attachments=").append(attachments).append(delim);
        }
        if (packetId != null) {
            builder.append("packetId=").append(packetId).append(delim);
        }
        if (from != null) {
            builder.append("from=").append(from).append(delim);
        }
        if (timeStamp != null) {
            builder.append("timeStamp=").append(timeStamp);
        }
        builder.append('}');
        return builder.toString();
    }

}
