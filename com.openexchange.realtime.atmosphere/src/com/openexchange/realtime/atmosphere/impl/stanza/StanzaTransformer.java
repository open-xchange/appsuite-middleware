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

package com.openexchange.realtime.atmosphere.impl.stanza;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.StanzaSender;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.realtime.atmosphere.payload.PayloadTransformerLibrary;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.Payload;
import com.openexchange.realtime.payload.PayloadTransformer;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link StanzaTransformer} - Transforms a Stanza "from" one representation "to" another by iterating over all the Payloads found
 * in the Stanza, and recursively applying the the proper PayloadTransformers. 
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class StanzaTransformer<T> {

    private final AtmosphereServiceRegistry services = AtmosphereServiceRegistry.getInstance();
    private final PayloadTransformerLibrary transformers = StranzaTransformerLibrary.getInstance();
    
    /**
     * Handle an incoming {@link Stanza}.
     * <p>
     * Channel handlers can decide to delegate the processing of stanzas to the proper {@OXRTHandler} when they can't be
     * handled internally. The TransformingStanzaHandler's concern is to process and validate it so that the stanza can be handled by the
     * MessageDispatcher.
     * </p>
     * 
     * @param stanza The incoming stanza to process
     * @param session The currently active session
     * @throws OXException When transformation of the Stanza fails
     */
    public T incoming(Stanza stanza, ServerSession session) throws OXException {
        List<Payload> payloads = stanza.getPayloads();
        for (Payload payload : payloads) {
            //get Handler for element from namespace and transform element
            ElementPath elementPath = new ElementPath(payload.getNamespace(), payload.getElementName());
            PayloadTransformer transformer = transformers.getHandlerFor(elementPath);
            if (transformer == null) {
                throw OXException.general("No transformer for " + elementPath);
            }
            Payload result = transformer.incoming(payload, session);
            //TODO: create new Stanza prefilled with copied basics and add payloads?
        }
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * Handle an outgoing {@link Stanza}.
     * <p>
     * Calling <code>Channel.send()</code> delegates the processing of the stanza to this method.
     * </p>
     * 
     * @param stanza the stanza to process
     * @param session the currently active session
     * @param sender the StanzaSender to use for finally sending the processed Stanza
     * @throws OXException
     */
    public T outgoing(Stanza stanza, ServerSession session, StanzaSender sender) throws OXException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}
