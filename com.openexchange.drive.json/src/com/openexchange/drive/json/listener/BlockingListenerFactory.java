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

package com.openexchange.drive.json.listener;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveProperty;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.json.LongPollingListener;
import com.openexchange.drive.json.LongPollingListenerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BlockingListenerFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BlockingListenerFactory implements LongPollingListenerFactory {

    private final ServiceLookup services;

    public BlockingListenerFactory(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public LongPollingListener create(DriveSession session, List<String> rootFolderIDs, SubscriptionMode mode) throws OXException {
        LeanConfigurationService service = services.getService(LeanConfigurationService.class);
        ServerSession serverSession = session.getServerSession();
        if (service.getBooleanProperty(serverSession.getUserId(), serverSession.getContextId(), DriveProperty.EVENTS_BLOCKING_LONG_POLLING_ENABLED)) {
            return new BlockingListener(session, rootFolderIDs, mode);
        }
        throw DriveExceptionCodes.LONG_POLLING_DISABLED.create(I(serverSession.getUserId()), I(serverSession.getContextId()));
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
