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

package com.openexchange.subscribe.database;

import java.sql.Connection;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.SubscriptionStorage;
import com.openexchange.subscribe.sql.SubscriptionSQLStorage;


/**
 * {@link SubscriptionUserDeleteListener}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class SubscriptionUserDeleteListener implements DeleteListener {


    private GenericConfigurationStorageService storageService;
    private SubscriptionSourceDiscoveryService discoveryService;

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (event.getType() != DeleteEvent.TYPE_USER) {
            return;
        }
        getStorage(writeCon).deleteAllSubscriptionsForUser(event.getId(), event.getContext());
    }

    protected SubscriptionStorage getStorage(Connection writeCon) {
        return new SubscriptionSQLStorage(new SimpleDBProvider(writeCon, writeCon), DBTransactionPolicy.NO_TRANSACTIONS, storageService, discoveryService);
    }
    public void setStorageService(GenericConfigurationStorageService storageService) {
        this.storageService = storageService;
    }

    public void setDiscoveryService(SubscriptionSourceDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

}
