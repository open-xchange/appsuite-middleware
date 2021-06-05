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

import static com.openexchange.java.Autoboxing.L;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.json.DefaultLongPollingListener;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link BlockingListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BlockingListener extends DefaultLongPollingListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BlockingListener.class);

    private final ReentrantLock lock;
    private final Condition hasEvent;

    private DriveEvent event;

    /**
     * Initializes a new {@link BlockingListener}.
     *
     * @param session The session
     * @param rootFolderIDs The root folder IDs to listen for changes in
     * @param mode The subscription mode
     */
    public BlockingListener(DriveSession session, List<String> rootFolderIDs, SubscriptionMode mode) {
        super(session, rootFolderIDs, mode);
        this.lock = new ReentrantLock();
        this.hasEvent = this.lock.newCondition();
    }

    @Override
    public AJAXRequestResult await(long timeout) throws OXException {
        DriveEvent data = null;
        lock.lock();
        try {
            if (null == this.event) {
                LOG.debug("Awaiting events for max. {}ms...", L(timeout));
                hasEvent.await(timeout, TimeUnit.MILLISECONDS);
            } else {
                LOG.debug("Stored event available, no need to wait.");
            }
            data = this.event;
            this.event = null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            lock.unlock();
        }
        if (null == data) {
            LOG.debug("No event available.");
        } else {
            LOG.debug("Available event: {}", data);
        }
        return createResult(data);
    }

    @Override
    public void onEvent(DriveEvent event) {
        if (false == isInteresting(event)) {
            LOG.debug("Skipping uninteresting event: {}", event);
            return;
        }
        lock.lock();
        try {
            this.event = event;
            this.hasEvent.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "BlockingListener [driveSession=" + driveSession + ", rootFolderIDs=" + rootFolderIDs + "]";
    }

}
