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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.realtime.payload.converter;

import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link PayloadTreeConverter} - Walks over a PayloadTree and converts PayloadElements contained in PayloadTreeNodes from the current
 * to the desired representation. Conversion happens when PayloadTrees of incoming Requests or outgoing Responses have to be changed so
 * that client/server can handle them. Users can register a preferred format for an element path and the framework will convert incoming stanzas payloads
 * to that format. The common usage is to register two {@link SimplePayloadConverter}s, one to turn a regular Java Type (your own class that models the payloa data) into a JSON representation
 * and one to turn JSON back into the custom type. Then you can register a preferred format for the framework by getting the PayloadTreeConverter out of the OSGi registry and declaring a preferred format:
 * 
 * getService(PayloadTreeConverter.class).declarePreferredFormat(new ElementPath("myNamespace", "myElementName"), InternalClass.class.getName()); 
 * 
 * which would turn a payload:
 * { element: "myElementName", namespace: "myNamespace", data: ...} into an InternalClass instance.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public interface PayloadTreeConverter {

    /**
     * Transform an incoming PayloadTree.
     *
     * @param payloadTree The incoming PayloadTree to process
     * @return
     * @throws OXException When transformation fails
     */
    public PayloadTree incoming(PayloadTree payloadTree) throws OXException;

    /**
     * Transform an outgoing PayloadTree.
     *
     * @param payloadTree The PayloadTree to process
     * @throws OXException When transformation fails
     */
    public PayloadTree outgoing(PayloadTree payloadTree, String format) throws OXException;
    
    /**
     * Declare a preferred format for a given element path
     */
    public void declarePreferredFormat(ElementPath path, String format);
    
    /**
     * Transform an incoming PayloadTreeNode.
     *
     * @param node The incoming PayloadTreeNode to process
     * @return
     * @throws OXException When transformation fails
     */
    public PayloadTreeNode incoming(PayloadTreeNode node) throws OXException;

    /**
     * Transform an outgoing PayloadTreeNode.
     *
     * @param node The PayloadTreeNode to process
     * @throws OXException When transformation fails
     */
    public PayloadTreeNode outgoing(PayloadTreeNode node, String format) throws OXException;
    

}
