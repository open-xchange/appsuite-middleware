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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEvent.Type;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.json.DefaultLongPollingListener;
import com.openexchange.exception.OXException;

/**
 * {@link CometListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CometListener extends DefaultLongPollingListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CometListener.class);

    private final CometContext<DriveEvent> cometContext;
    private final ReentrantLock lock;

    private DriveEvent event;
    private DriveCometHandler cometHandler;

    /**
     * Initializes a new {@link CometListener}.
     *
     * @param session The session
     * @param cometContext The comet context
     * @param rootFolderIDs The root folder IDs to listen for changes in
     * @param mode The subscription mode
     */
    public CometListener(DriveSession session, CometContext<DriveEvent> cometContext, List<String> rootFolderIDs, SubscriptionMode mode) {
        super(session, rootFolderIDs, mode);
        this.cometContext = cometContext;
        this.lock = new ReentrantLock();
    }

    @Override
    public AJAXRequestResult await(long timeout) throws OXException {
        lock.lock();
        try {
            if (null == this.event) {
                /*
                 * wait for event inside comet handler
                 */
                LOG.debug("Registering new comet handler for {} ...", driveSession);
                cometHandler = new DriveCometHandler(driveSession, rootFolderIDs, mode);
                cometContext.addCometHandler(cometHandler);
                /*
                 * return placeholder result for now
                 */
                AJAXRequestResult noResultYet = new AJAXRequestResult();
                noResultYet.setType(ResultType.DIRECT);
                return noResultYet;
            }
            /*
             * consume available event directly
             */
            LOG.debug("Stored event available for {}, no need to wait.", driveSession);
            AJAXRequestResult result = createResult(this.event);
            this.event = null;
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onEvent(DriveEvent event) {
        if (false == isInteresting(event)) {
            LOG.debug("Skipping uninteresting event: {}", event);
            return;
        }
        lock.lock();
        try {
            if (null != cometHandler) {
                /*
                 * notify comet handler
                 */
                try {
                    cometHandler.getCometContext().notify(event, Type.NOTIFY, cometHandler);
                } catch (IOException e) {
                    LOG.warn("", e);
                }
                cometHandler = null;
            } else {
                /*
                 * no comet handler registered yet, store event for later consumption
                 */
                this.event = event;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "CometListener [driveSession=" + driveSession + ", rootFolderIDs=" + rootFolderIDs + ", mode=" + mode + "]";
    }

}