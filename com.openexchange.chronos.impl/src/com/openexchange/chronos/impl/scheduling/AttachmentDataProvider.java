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

package com.openexchange.chronos.impl.scheduling;

import java.io.InputStream;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AttachmentDataProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class AttachmentDataProvider {

    private final ServiceLookup services;
    private final int contextId;

    /**
     * Initializes a new {@link AttachmentDataProvider}.
     * 
     * @param services A service lookup reference
     * @param contextId The context identifier
     */
    public AttachmentDataProvider(ServiceLookup services, int contextId) {
        super();
        this.services = services;
        this.contextId = contextId;
    }

    public InputStream getAttachmentData(int managedId) throws OXException {
        return new OSGiCalendarStorageOperation<InputStream>(services, contextId, Utils.ACCOUNT_ID) {

            @Override
            protected InputStream call(CalendarStorage storage) throws OXException {
                return storage.getAttachmentStorage().loadAttachmentData(managedId);
            }
        }.executeQuery();
    }

}
