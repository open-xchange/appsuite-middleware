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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.xmpp.packet;

import org.joox.JOOX;
import org.joox.Match;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.Payload;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link XMPPMessage}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class XMPPMessage extends XMPPStanza {

    public static enum Type {
        normal, chat, groupchat, headline, error;
    }

    private JID from;

    private Type type;

    private String subject;

    private Payload payload;

    private String thread;

    public XMPPMessage(Type type, ServerSession session) {
        super(session);
        this.type = type;
    }

    public XMPPMessage(Match xml, ServerSession session) {
        super(session);
        parseXml(xml);
    }

    public JID getFrom() {
        return from;
    }

    public void setFrom(JID from) {
        this.from = from;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    @Override
    public String toXML(ServerSession session) throws OXException {
        Match document = JOOX.$("message").attr("from", from.toString()).attr("type", type.toString());

        addAttributesAndElements(document);

        if (subject != null) {
            document.append(JOOX.$("subject", subject));
        }
        if (thread != null) {
            document.append(JOOX.$("thread", thread));
        }
        if (payload != null) {
            Payload p = payload.to("xmpp", session);
            String d = (String) p.getData();
            Match j = JOOX.$(d);
            document.append(j);
        }

        return document.toString();
    }

    private void parseXml(Match xml) {
        setFrom(new JID(xml.attr("from")));
        setTo(new JID(xml.attr("to")));
        setType(Type.valueOf(xml.attr("type")));

        Match subject = xml.find("subject");
        if (subject != null) {
            setSubject(subject.content());
        }

        Match body = xml.find("body");
        if (body != null) {
            setPayload(new Payload(body.toString(), "xmpp"));
        }
    }

}
