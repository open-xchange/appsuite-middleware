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

package com.openexchange.realtime.atmosphere.osgi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.realtime.atmosphere.payload.converter.AtmospherePayloadElementConverter;
import com.openexchange.realtime.atmosphere.stanza.StanzaBuilder;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;

/**
 * {@link ExtensionRegistry} 
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ExtensionRegistry extends ServiceRegistry {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ExtensionRegistry.class);

    private static final ExtensionRegistry INSTANCE = new ExtensionRegistry();

    private final ConcurrentHashMap<Class<? extends Stanza>, StanzaBuilder<? extends Stanza>> builders;

    private final Map<ElementPath, AtmospherePayloadElementConverter> elementPathToTransformer;

    /**
     * Encapsulated constructor.
     */
    private ExtensionRegistry() {
        super();
        builders = new ConcurrentHashMap<Class<? extends Stanza>, StanzaBuilder<? extends Stanza>>();
        elementPathToTransformer = new ConcurrentHashMap<ElementPath, AtmospherePayloadElementConverter>();
    }

    /**
     * Get the Registry singleton.
     * 
     * @return the Registry singleton
     */
    public static ExtensionRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the appropriate transformer for the specified Stanz class.
     * 
     * @param stanzaClass The Stanza subclass we want to transform.
     * @return The appropriate transformer or <code>null</code> if none is applicable.
     */
    public AtmospherePayloadElementConverter getTransformerFor(ElementPath elementPath) {
        return elementPathToTransformer.get(elementPath);
    }

    /**
     * Adds specified transformer to this library.
     * 
     * @param transformer The transformer to add
     */
    public void addPayloadElementTransFormer(AtmospherePayloadElementConverter transformer) {
        elementPathToTransformer.put(transformer.getElementPath(), transformer);
    }

    /**
     * Removes specified transformer from this library.
     * 
     * @param transformer The transformer to remove
     */
    public void removePayloadElementTransformer(AtmospherePayloadElementConverter transformer) {
        elementPathToTransformer.remove(transformer.getElementPath());
    }

    /**
     * Get the collected ElementPaths the registered PayloadElementTransformers are able to transform.
     * 
     * @return the collected ElementPaths the registered PayloadElementTransformers are able to transform.
     */
    public Set<ElementPath> getTransformableableElementPaths() {
        return new HashSet<ElementPath>(elementPathToTransformer.keySet());
    }

    @Override
    public void clearRegistry() {
        elementPathToTransformer.clear();
        builders.clear();
    }

}
