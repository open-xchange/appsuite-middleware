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
import com.openexchange.tools.session.ServerSession;

/**
 * {@link XMPPPresence}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class XMPPPresence extends XMPPStanza {

    public static enum Type {
        none, error, probe, subscribe, subscribed, unavailable, unsubscribe, unsubscribed;
    }

    public static enum Show {
        available, away, chat, dnd, xa;
    }

    private Type type;

    private Show show = Show.available;

    private String status;

    private int priority = -129;

    public XMPPPresence(Type type, ServerSession session) {
        super(session);
        this.type = type;
    }

    public XMPPPresence(ServerSession session) {
        this(Type.none, session);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority < -128 || priority > 127) {
            throw new IllegalArgumentException("Priority must be between -128 and 127.");
        }
        this.priority = priority;
    }

    @Override
    public String toXML(ServerSession session) {
        Match document = JOOX.$("presence");

        addAttributesAndElements(document);

        if (type != null) {
            document.attr("type", getType().toString());
        }
        if (show != null) {
            document.append(JOOX.$("show", show.toString()));
        }
        if (status != null) {
            document.append(JOOX.$("status", status));
        }
        if (priority != -129) {
            document.append(JOOX.$("priority", Integer.toString(priority)));
        }

        return document.toString();
    }

}
