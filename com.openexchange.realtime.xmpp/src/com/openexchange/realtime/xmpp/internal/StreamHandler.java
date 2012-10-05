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

import static org.joox.JOOX.$;
import java.util.UUID;
import org.joox.Match;
import com.openexchange.realtime.xmpp.internal.XMPPChatDelivery.State;

/**
 * {@link StreamHandler}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class StreamHandler {

    private UUID streamId;

    private static Match REQUIRED = $("required");

    private static Match OPTIONAL = $("optional");

    private static Match PLAIN = $("mechanism", "PLAIN");

    private static Match MECHANISMS = $("mechanisms").attr("xmlns", "urn:ietf:params:xml:ns:xmpp-sasl").append(PLAIN).append(REQUIRED);

    private static Match BIND = $("bind").attr("xmlns", "urn:ietf:params:xml:ns:xmpp-bind").append(REQUIRED);

    private static Match SESSION = $("session").attr("xmlns", "urn:ietf:params:xml:ns:xmpp-session");

    /**
     * Initializes a new {@link StreamHandler}.
     * 
     * @param streamId
     */
    public StreamHandler(UUID streamId) {
        super();
        this.streamId = streamId;
    }

    public String createClientResponseStream(String domain) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<stream:stream from=\"" + domain + "\" id=\"" + streamId.toString() + "\" version=\"1.0\" xmlns=\"jabber.client\" xmlns:stream=\"http://etherx.jabber.org/streams\">");
        String retval = sb.toString();
        return retval;
    }

    public String getStreamFeatures(State state) {
        Match match = $("stream:features");

        if (state == State.init) {
            match.append(MECHANISMS);
        } else if (state == State.postLogin) {
            match.append(BIND).append(SESSION);
        }

        return match.toString();
    }

}
