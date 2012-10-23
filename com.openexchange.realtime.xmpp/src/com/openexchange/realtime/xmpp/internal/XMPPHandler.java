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

package com.openexchange.realtime.xmpp.internal;

import java.util.HashMap;
import java.util.Map;
import org.joox.JOOX;
import org.joox.Match;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.xmpp.XMPPExtension;
import com.openexchange.realtime.xmpp.packet.XMPPMessage;
import com.openexchange.realtime.xmpp.packet.XMPPStanza;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link XMPPHandler}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class XMPPHandler {

    private final Map<String, XMPPComponent> components = new HashMap<String, XMPPComponent>();

    public void handle(XMPPContainer container) throws OXException {
        Match match = JOOX.$(container.getXml());
        String namespace = match.attrs("xmlns").get(0);
        String service = null;
        if (namespace != null) {
            service = namespace;
        } else if (match.tag().equals("message")) {
            service = "chat";
        } else if (match.tag().equals("presence")) {
            service = "presence";
        }

        ServerSession session = container.getSession();
        XMPPStanza xmpp = XMPPStanza.getStanza(match, session);
        String resource = getComponentResource(session, xmpp);
        XMPPComponent xmppComponent = components.get(resource);
        if (xmppComponent == null) {
            // TODO: handle
        }

        XMPPExtension xmppExtension = xmppComponent.getExtension(service);
        if (xmppExtension == null) {
            // TODO: handle
        }

        xmppExtension.handleIncoming(xmpp, session);
    }

    public void addComponent(XMPPComponent component) {
        this.components.put(component.getResource(), component);
    }

    public void removeComponent(String resource) {
        this.components.remove(resource);
    }
    
    public XMPPComponent getComponent(String resource) {
        return this.components.get(resource);
    }

    private String getComponentResource(ServerSession session, XMPPStanza xmpp) {
        String[] split = xmpp.getTo().getDomain().split(session.getContext().getName());
        return split.length == 0 ? "" : split[0];
    }
}
