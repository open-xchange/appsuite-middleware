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

package com.openexchange.contact.vcard.storage.impl;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.vcard.storage.VCardStorageFactory;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultVCardStorageFactory}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DefaultVCardStorageFactory implements VCardStorageFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultVCardStorageFactory.class);

    private final VCardStorageService vCardStorageService;

    public DefaultVCardStorageFactory(VCardStorageService vCardStorageService) {
        this.vCardStorageService = vCardStorageService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VCardStorageService getVCardStorageService(ConfigViewFactory configViewFactory, int contextId) {
        try {
            for (String capability : this.vCardStorageService.neededCapabilities()) {
                ConfigProperty<Boolean> filestoreCapability = configViewFactory.getView(-1, contextId).property(capability, boolean.class);
                if (!filestoreCapability.get().booleanValue()) {
                    LOG.info("Needed capability '{}' not available for context id {}. Unable to handle VCard for storage.", capability, I(contextId));
                    return null;
                }
            }
        } catch (OXException oxException) {
            LOG.warn("Unable to return requested VCardStorageService implementation.", oxException);
            return null;
        }
        return vCardStorageService;
    }
}
