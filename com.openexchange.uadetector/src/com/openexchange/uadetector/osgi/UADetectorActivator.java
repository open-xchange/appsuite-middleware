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

package com.openexchange.uadetector.osgi;

import org.slf4j.LoggerFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.uadetector.UserAgentParser;
import com.openexchange.uadetector.internal.CachingUserAgentParser;

/**
 * {@link UADetectorActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UADetectorActivator extends HousekeepingActivator {

    private CachingUserAgentParser parser;

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    /**
     * Initializes a new {@link UADetectorActivator}.
     */
    public UADetectorActivator() {
        super();
    }

    @Override
    public synchronized void startBundle() throws Exception {
        LoggerFactory.getLogger(UADetectorActivator.class).info("starting bundle: \"com.openexchange.uadetector\"");
        CachingUserAgentParser parser = new CachingUserAgentParser(false);
        this.parser = parser;
        registerService(UserAgentParser.class, parser);
    }

    @Override
    public synchronized void stopBundle() throws Exception {
        LoggerFactory.getLogger(UADetectorActivator.class).info("stopping bundle: \"com.openexchange.uadetector\"");
        CachingUserAgentParser parser = this.parser;
        if (null != parser) {
            this.parser = null;
            parser.shutdown();
        }
        super.stopBundle();
    }

}
