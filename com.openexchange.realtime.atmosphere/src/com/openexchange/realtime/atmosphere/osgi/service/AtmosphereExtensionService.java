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

package com.openexchange.realtime.atmosphere.osgi.service;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.stanza.StanzaHandler;
import com.openexchange.realtime.atmosphere.stanza.StanzaInitializer;
import com.openexchange.realtime.payload.transformer.PayloadElementTransformer;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link AtmosphereExtensionService} - Service to register StanzaHandlers and PayloadElementTransformers and StanzaInitializers. This allows us to extend the
 * functionality of the central Atmosphere realtime bundle from within other bundles that concentrate on specific features like Presence,
 * Messaging or others.
 * 
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public interface AtmosphereExtensionService {

    /**
     * Adds specified transformer to the Atmosphere realtime bundle.
     * 
     * @param transformer The transformer to add
     */
    void addPayloadElementTransFormer(PayloadElementTransformer transformer);

    /**
     * Remove specified transformer from the Atmosphere realtime bundle.
     * 
     * @param transformer The transformer to remove
     */
    void removePayloadElementTransformer(PayloadElementTransformer transformer);

    /**
     * Add an ElementPathMapping to the Atmosphere realtime bundle so it knows how to transform a PayloadElement.
     * 
     * @param elementPath The ElementPath of the PayloadElement
     * @param mappingClass The Class used to map the PayloadElement
     * @throws OXException when no transformer for the mapping class can be found
     */
    void addElementPathMapping(ElementPath elementPath, Class<?> mappingClass) throws OXException;

    /**
     * Remove an ElementPathMapping from the Atmosphere realtime bundle.
     * 
     * @param elementPath The ElementPath of the PayloadElement
     */
    void removeElementpathMapping(ElementPath elementPath);

    /**
     * Add a StazaHandler to the Atmosphere realtime bundle so it knows how to handle a Stanza subclass.
     * 
     * @param handler The StanzaHandler to add
     */
    void addStanzaHandler(StanzaHandler handler);

    /**
     * Remove a StazaHandler from the Atmosphere realtime bundle.
     * 
     * @param handler The StanzaHandler to remove
     */
    void removeStanzaHandler(StanzaHandler handler);

}
