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

package com.openexchange.drive.json.comet.internal;

import java.util.List;
import org.glassfish.grizzly.comet.CometContext;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.json.LongPollingListener;
import com.openexchange.drive.json.LongPollingListenerFactory;


/**
 * {@link CometListenerFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CometListenerFactory implements LongPollingListenerFactory {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CometListenerFactory.class);
    private static final int PRIORITY = 10;

    private final CometContext<DriveEvent> cometContext;

    /**
     * Initializes a new {@link CometListenerFactory}.
     */
    public CometListenerFactory(CometContext<DriveEvent> cometContext) {
        super();
        this.cometContext = cometContext;
        LOG.info("CometListenerFactory initialized, using comet context @ topic \"{}\".", cometContext.getTopic());
    }

    @Override
    public LongPollingListener create(DriveSession session, List<String> rootFolderIDs, SubscriptionMode mode) {
        return new CometListener(session, cometContext, rootFolderIDs, mode);
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

}
