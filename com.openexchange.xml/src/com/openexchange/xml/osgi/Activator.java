/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.xml.osgi;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.xml.jdom.JDOMParser;
import com.openexchange.xml.jdom.impl.JDOMParserImpl;
import com.openexchange.xml.spring.SpringParser;
import com.openexchange.xml.spring.impl.DefaultSpringParser;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class Activator extends HousekeepingActivator {

    @Override
    public void startBundle() throws Exception {
        registerService(JDOMParser.class, new JDOMParserImpl(), null);
        registerService(SpringParser.class, new DefaultSpringParser(), null);
        // new javax.xml.stream.internal.Activator().start(bundleContext);
    }

    @Override
    public void stopBundle() throws Exception {
        // new javax.xml.stream.internal.Activator().stop(bundleContext);
        unregisterServices();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        // Nothing to do
        return null;
    }
}
