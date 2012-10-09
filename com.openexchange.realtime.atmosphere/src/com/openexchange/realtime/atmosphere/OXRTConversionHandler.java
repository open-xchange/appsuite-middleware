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

package com.openexchange.realtime.atmosphere;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.StanzaSender;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.Payload;
import com.openexchange.realtime.payload.PayloadTransformer;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link OXRTConversionHandler} - Handles Conversion of Stanzas for a given namespace by telling the Stanza payload the format it should
 * convert itslef into, getting the MessageDispatcher and delegating the further processing of the Stanza.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class OXRTConversionHandler implements PayloadTransformer {

    public static ServiceLookup services;

    private final String format;
    private final ElementPath elementPath;

    /**
     * Initializes a new {@link OXRTConversionHandler}.
     * 
     * @param elementPath the path to an element in a namespace this OXRTConversionHandler can handle
     * @param format the format of POJOs that incoming Stanzas should be converted to
     */
    public OXRTConversionHandler(ElementPath elementPath, String format) {
        this.elementPath = elementPath;
        this.format = format;
    }

    @Override
    public ElementPath getElementPath() {
        return elementPath; 
    }

    @Override
    public void incoming(Stanza stanza, ServerSession session) throws OXException {
        Payload payload = stanza.getPayload();
        if(payload != null) {
            stanza.setPayload(payload.to(format, session));
        }
        send(stanza, session);
    }

    @Override
    public void outgoing(Stanza stanza, ServerSession session, StanzaSender sender) throws OXException {
        stanza.setPayload(stanza.getPayload().to("json", session));
        sender.send(stanza);
    }

    /**
     * Send the Stanza by getting the MessageDispatcher service and letting it handle the further processing of the Stanza.
     * 
     * @param stanza the stanza to send
     * @param session the associated ServerSession
     * @throws OXException when sending the Stanza fails
     */
    protected void send(Stanza stanza, ServerSession session) throws OXException {
        services.getService(MessageDispatcher.class).send(stanza, session);
    }

}
