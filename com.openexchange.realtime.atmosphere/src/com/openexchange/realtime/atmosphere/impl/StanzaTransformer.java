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

package com.openexchange.realtime.atmosphere.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.StanzaFilter;
import com.openexchange.realtime.atmosphere.AtmosphereExceptionCode;
import com.openexchange.realtime.atmosphere.osgi.ExtensionRegistry;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadElement;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.payload.transformer.PayloadElementTransformer;
import com.openexchange.realtime.util.ElementPath;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link StanzaTransformer} - Transforms a Stanza "from" one representation "to" another by visiting all the PayloadTrees found in the
 * Stanza, and recursively applying the proper PayloadTransformers.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */

public class StanzaTransformer implements StanzaFilter {

    private final ExtensionRegistry transformers = ExtensionRegistry.getInstance();

    /**
     * Transform an incoming {@link Stanza} by transforming every PayloadTree of the Stanza.
     * 
     * @param stanza The incoming stanza to process
     * @param session The currently active session
     * @throws OXException When transformation of the Stanza fails
     */
    @Override
    public void incoming(Stanza stanza, ServerSession session) throws OXException {
        List<PayloadTree> payloadTrees = new ArrayList<PayloadTree>(stanza.getPayloads());
        for (PayloadTree tree : payloadTrees) {
            // TODO: Use ThreadService to transform trees in parallel
            PayloadTreeNode root = tree.getRoot();
            if (root != null) {
                incoming(root, session);
            }
        }
    }

    private void incoming(PayloadTreeNode node, ServerSession session) throws OXException {
        PayloadElementTransformer transformer = getPayloadTransformer(node.getElementPath());
        node.setPayloadElement(transformer.incoming(node.getPayloadElement(), session));
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
    @Override
    public void outgoing(Stanza stanza, ServerSession session) throws OXException {
        Collection<PayloadTree> payloadTrees = stanza.getPayloads();
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

    /**
     * Get a PayloadElementTransformer suitable for a given ElementPath. We maintain two mappings: ElementPath ->
     * PayloadElementTransformer.class and PayloadElementTransformer.class -> Transformer. So when we want to transform a PayloadElement whe
     * have to lookup the class we use to represent it by its ElementPath and then get the Transformer suitable for that class. This way we
     * don't have to register multiple instances of the same PayloadElementTransformer for different ElementPaths.
     * 
     * @param elementPath The ElementPath of the PayloadElement we want to transform
     * @return A PayloadElementTransformer suitable for that PayloadElement
     * @throws OXException If no suitable PayloadElementTransformer could be found
     */
    private PayloadElementTransformer getPayloadTransformer(ElementPath elementPath) throws OXException {
        PayloadElementTransformer transformer = transformers.getTransformerFor(elementPath);
        if (transformer == null) {
            throw AtmosphereExceptionCode.MISSING_TRANSFORMER_FOR_PAYLOADELEMENT.create(elementPath);
        }
        return transformer;
    }

}
