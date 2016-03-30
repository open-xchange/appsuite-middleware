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

package com.openexchange.publish.json.osgi;

import com.openexchange.ajax.osgi.AbstractSessionServletActivator;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.json.PublicationMultipleHandlerFactory;
import com.openexchange.publish.json.PublicationServlet;
import com.openexchange.publish.json.PublicationTargetMultipleHandlerFactory;
import com.openexchange.publish.json.PublicationTargetServlet;
import com.openexchange.publish.json.types.EntityMap;

public class ServletActivator extends AbstractSessionServletActivator {

    private static final String TARGET_ALIAS_APPENDIX = "publicationTargets";
    private static final String PUB_ALIAS_APPENDIX = "publications";

    /**
     * Initializes a new {@link ServletActivator}.
     */
    public ServletActivator() {
        super();
    }

    @Override
    protected Class<?>[] getAdditionalNeededServices() {
        return new Class<?>[] { PublicationTargetDiscoveryService.class, ConfigurationService.class, DispatcherPrefixService.class };
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected void startBundle() throws Exception {
        register();
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();
    }

    private void register() {
        final PublicationTargetDiscoveryService discovery = getService(PublicationTargetDiscoveryService.class);
        if(discovery == null) {
            return;
        }

        final ConfigurationService config = getService(ConfigurationService.class);
        if(config == null){
        	return;
        }

        final PublicationMultipleHandlerFactory publicationHandlerFactory = new PublicationMultipleHandlerFactory(discovery, new EntityMap(), config);
        final PublicationTargetMultipleHandlerFactory publicationTargetHandlerFactory = new PublicationTargetMultipleHandlerFactory(discovery);

        registerService(MultipleHandlerFactoryService.class, publicationHandlerFactory, null);
        registerService(MultipleHandlerFactoryService.class, publicationTargetHandlerFactory, null);

        PublicationServlet.setFactory(publicationHandlerFactory);
        PublicationTargetServlet.setFactory(publicationTargetHandlerFactory);

        final String prefix = getService(DispatcherPrefixService.class).getPrefix();
        registerSessionServlet(prefix + TARGET_ALIAS_APPENDIX, new PublicationTargetServlet());
        registerSessionServlet(prefix + PUB_ALIAS_APPENDIX, new PublicationServlet());
    }

    private void unregister() {
        PublicationServlet.setFactory(null);
        PublicationTargetServlet.setFactory(null);

        cleanUp();
    }

}
