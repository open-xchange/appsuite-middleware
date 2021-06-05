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

package com.openexchange.subscribe.json.osgi;

import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link I18nActivator}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class I18nActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link I18nActivator}.
     */
    public I18nActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        Services.setServiceLookup(this);
    }
    
    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }
    
    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { I18nServiceRegistry.class };
    }
}
