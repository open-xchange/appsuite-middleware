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

package com.openexchange.contact.vcard.storage;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;

/**
 * Factory to provide the registered {@link VCardStorageService} if all requirements to ensure a smooth usage are met.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface VCardStorageFactory {

    /**
     * Returns the registered {@link VCardStorageService} if all requirements (like needed capabilities) are met.
     *
     * @param configViewFactory The {@link ConfigViewFactory}
     * @param contextId The context identifier
     * @return registered {@link VCardStorageService} if all requirements are met. Otherwise <code>null</code>
     * @throws OXException
     */
    VCardStorageService getVCardStorageService(ConfigViewFactory configViewFactory, int contextId);
}
