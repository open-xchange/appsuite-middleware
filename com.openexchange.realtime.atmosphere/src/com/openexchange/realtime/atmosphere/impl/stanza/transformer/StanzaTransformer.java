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

package com.openexchange.realtime.atmosphere.impl.stanza.transformer;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.impl.payload.PayloadTransformerRegistry;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadElementTransformer;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link StanzaTransformer} - Transforms a Stanza "from" one representation "to" another by iterating over all the PayloadTrees found in
 * the Stanza, and recursively applying the the proper PayloadTransformers.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class StanzaTransformer<T> {

    private final PayloadTransformerRegistry transformers = PayloadTransformerRegistry.getInstance();

    /**
     * Transform an incoming {@link Stanza} by transforming every PayloadTree of the Stanza.
     * 
     * @param stanza The incoming stanza to process
     * @param session The currently active session
     * @throws OXException When transformation of the Stanza fails
     */
    public void incoming(Stanza stanza, ServerSession session) throws OXException {
        List<PayloadTree> payloadTrees = stanza.getPayloads();
        for (PayloadTree tree : payloadTrees) {
            PayloadTreeNode root = tree.getRoot();
            if (root != null) {
                incoming(root, session);
            }
        }
    }

    private void incoming(PayloadTreeNode node, ServerSession session) throws OXException {
        PayloadElementTransformer transformer = getPayloadTransformer(node.getElementPath());
        PayloadElement result = transformer.incoming(node.getPayloadElement(), session);
        node.setPayloadElement(result);
        for (PayloadTreeNode child : node.getChildren()) {
            incoming(child, session);
        }
    }

    /**
     * Transform an outgoing {@link Stanza} by transforming every PayloadTree of the Stanza.
     * 
     * @param stanza The outgoing stanza to process
     * @param session The currently active session
     * @throws OXException When transformation of the Stanza fails
     */
    public void outgoing(Stanza stanza, ServerSession session) throws OXException {
        List<PayloadTree> payloadTrees = stanza.getPayloads();
        for (PayloadTree tree : payloadTrees) {
            PayloadTreeNode root = tree.getRoot();
            if (root != null) {
                outgoing(root, session);
            }
        }
    }

    private void outgoing(PayloadTreeNode node, ServerSession session) throws OXException {
        PayloadElementTransformer transformer = getPayloadTransformer(node.getElementPath());
        PayloadElement result = transformer.outgoing(node.getPayloadElement(), session);
        node.setPayloadElement(result);
        for (PayloadTreeNode child : node.getChildren()) {
            outgoing(child, session);
        }
    }

    private PayloadElementTransformer getPayloadTransformer(ElementPath elementPath) throws OXException {
        PayloadElementTransformer transformer = transformers.getHandlerFor(elementPath);
        if (transformer == null) {
            throw OXException.general("No transformer for " + elementPath); // TODO: write proper OXEX
        }
        return transformer;
    }

}
