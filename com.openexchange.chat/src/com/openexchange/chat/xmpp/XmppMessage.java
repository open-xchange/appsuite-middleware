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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.chat.xmpp;

import java.util.Date;
import java.util.List;
import org.jivesoftware.smack.packet.Message.Subject;
import org.jivesoftware.smack.packet.XMPPError;
import com.openexchange.chat.ChatAttachment;
import com.openexchange.chat.ChatMember;
import com.openexchange.chat.Message;


/**
 * {@link XmppMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class XmppMessage implements Message {

    private final org.jivesoftware.smack.packet.Message xmppMessage;

    /**
     * Initializes a new {@link XmppMessage}.
     */
    public XmppMessage() {
        super();
        xmppMessage = new org.jivesoftware.smack.packet.Message();
    }

    @Override
    public String getPacketID() {
        return xmppMessage.getPacketID();
    }

    @Override
    public ChatMember getTo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChatMember getFrom() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getTimeStamp() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Type getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSubject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ChatAttachment> getAttachments() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param type
     * @see org.jivesoftware.smack.packet.Message#setType(org.jivesoftware.smack.packet.Message.Type)
     */
    public void setType(final org.jivesoftware.smack.packet.Message.Type type) {
        xmppMessage.setType(type);
    }

    /**
     * @param packetID
     * @see org.jivesoftware.smack.packet.Packet#setPacketID(java.lang.String)
     */
    public void setPacketID(final String packetID) {
        xmppMessage.setPacketID(packetID);
    }

    /**
     * @param to
     * @see org.jivesoftware.smack.packet.Packet#setTo(java.lang.String)
     */
    public void setTo(final String to) {
        xmppMessage.setTo(to);
    }

    /**
     * @param subject
     * @see org.jivesoftware.smack.packet.Message#setSubject(java.lang.String)
     */
    public void setSubject(final String subject) {
        xmppMessage.setSubject(subject);
    }

    /**
     * @param from
     * @see org.jivesoftware.smack.packet.Packet#setFrom(java.lang.String)
     */
    public void setFrom(final String from) {
        xmppMessage.setFrom(from);
    }

    /**
     * @param language
     * @param subject
     * @return
     * @see org.jivesoftware.smack.packet.Message#addSubject(java.lang.String, java.lang.String)
     */
    public Subject addSubject(final String language, final String subject) {
        return xmppMessage.addSubject(language, subject);
    }

    /**
     * @param error
     * @see org.jivesoftware.smack.packet.Packet#setError(org.jivesoftware.smack.packet.XMPPError)
     */
    public void setError(final XMPPError error) {
        xmppMessage.setError(error);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return xmppMessage.toString();
    }

    /**
     * @param body
     * @see org.jivesoftware.smack.packet.Message#setBody(java.lang.String)
     */
    public void setBody(final String body) {
        xmppMessage.setBody(body);
    }

    /**
     * @param thread
     * @see org.jivesoftware.smack.packet.Message#setThread(java.lang.String)
     */
    public void setThread(final String thread) {
        xmppMessage.setThread(thread);
    }

    /**
     * @return
     * @see org.jivesoftware.smack.packet.Message#toXML()
     */
    public String toXML() {
        return xmppMessage.toXML();
    }

    
}
