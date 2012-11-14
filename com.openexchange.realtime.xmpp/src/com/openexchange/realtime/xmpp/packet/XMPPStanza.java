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

import org.joox.Match;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link XMPPStanza}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class XMPPStanza {

    protected JID to;

    private String id;

    private ServerSession session;

    protected XMPPStanza(ServerSession session) {
        this.session = session;
    }

    public JID getTo() {
        return to;
    }

    public void setTo(JID to) {
        this.to = to;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ServerSession getSession() {
        return session;
    }

    public void setSession(ServerSession session) {
        this.session = session;
    }

    protected void addAttributesAndElements(Match document) {
        document.attr("to", to.toString());

        if (id != null) {
            document.attr("id", id);
        }
    }

    public static XMPPStanza getStanza(Match xml, ServerSession session) {
        String id = xml.id().trim().toLowerCase();

        if (id.equals("message")) {
            return new XMPPMessage(xml, session);
        } else if (id.equals("presence")) {
            return new XMPPPresence(session);
        } else if (id.equals("iq")) {
            return new XMPPIq(xml, session);
        }

        return null;
    }

    public abstract String toXML(ServerSession session) throws OXException;

}
