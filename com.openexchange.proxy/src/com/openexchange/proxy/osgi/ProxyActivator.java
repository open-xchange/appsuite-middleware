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

package com.openexchange.proxy.osgi;

import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link ProxyActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ProxyActivator extends HousekeepingActivator {

	@Override
    public void startBundle() throws Exception {
	    final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyActivator.class);
        try {
            log.info("starting bundle: com.openexchange.proxy");
        } catch (Exception e) {
            log.error("Failed start-up of bundle com.openexchange.proxy", e);
            throw e;
        }
	}

	@Override
    public void stopBundle() throws Exception {
	    final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyActivator.class);
        try {
            log.info("stopping bundle: com.openexchange.proxy");
            super.stopBundle();
        } catch (Exception e) {
            log.error("Failed shut-down of bundle com.openexchange.proxy", e);
            throw e;
        }
	}

    @Override
    protected Class<?>[] getNeededServices() {
        // Nothing to do
        return null;
    }

}
