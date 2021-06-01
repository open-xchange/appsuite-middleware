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
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEvent;
import org.glassfish.grizzly.comet.DefaultCometHandler;
import org.glassfish.grizzly.http.server.Response;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.java.Streams;

/**
 * {@link DriveCometHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@SuppressWarnings("rawtypes")
public class DriveCometHandler extends DefaultCometHandler<DriveEvent> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveCometHandler.class);
    private static final List<DriveAction<? extends DriveVersion>> EMPTY_ACTIONS = Collections.emptyList();

    private final DriveSession session;
    private final List<String> rootFolderIDs;
    private final SubscriptionMode mode;
    private final AtomicLong initializationTime;

    /**
     * Initializes a new {@link DriveCometHandler}.
     *
     * @param session The session
     * @param rootFolderIDs The root folder IDs to listen for changes in
     * @param mode The subscription mode
     */
    public DriveCometHandler(DriveSession session, List<String> rootFolderIDs, SubscriptionMode mode) {
        super();
        this.session = session;
        this.rootFolderIDs = rootFolderIDs;
        this.mode = mode;
        initializationTime = new AtomicLong();
    }

    /**
     * Receive CometEvent notification when Grizzly is about to suspend the connection. This method is always invoked during the
     * processing of CometContext.addCometHandler operations.
     */
    @Override
    public void onInitialize(CometEvent event) throws IOException {
        initializationTime.set(System.currentTimeMillis());
        LOG.trace("{}: initialized.", this);
    }

    @Override
    public void onEvent(CometEvent event) throws IOException {
        try {
            LOG.debug("{}: Got EVENT after {}ms: {} [{}]", this, String.valueOf(System.currentTimeMillis() - initializationTime.get()), event.getType(), event.attachment());
            /*
             * create and return resulting actions if available
             */
            if (CometEvent.Type.NOTIFY == event.getType()) {
                CometEvent<DriveEvent> driveCometEvent = event;
                DriveEvent driveEvent = driveCometEvent.attachment();
                if (null != driveEvent && driveEvent.getContextID() == session.getServerSession().getContextId()) {
                    Set<String> folderIDs = driveEvent.getFolderIDs();
                    if (null != folderIDs && null != rootFolderIDs) {
                        write(driveEvent.getActions(rootFolderIDs, SubscriptionMode.SEPARATE.equals(mode)));
                    }
                }
            }
        } finally {
            resume(event.getCometContext());
        }
    }

    /**
     * Receive CometEvent notification when the response is resumed by a CometHandler or by the CometContext.
     */
    @Override
    public void onTerminate(CometEvent event) throws IOException {
        try {
            LOG.trace("{}: Got TERMINATE after {}ms.", this, String.valueOf(System.currentTimeMillis() - initializationTime.get()));
        } finally {
            resume(event.getCometContext());
        }
    }

    /**
     * Receive CometEvent notification when the underlying tcp communication is resumed by Grizzly. This happens when the
     * CometContext.setExpirationDelay expires or when the remote client close the connection.
     */
    @Override
    public void onInterrupt(CometEvent event) throws IOException {
        try {
            LOG.trace("{}: Got INTERRUPT after {}ms.", this, String.valueOf(System.currentTimeMillis() - initializationTime.get()));
            CometContext cometContext = event.getCometContext();
            if (null != cometContext && cometContext.isActive(this)) {
                LOG.debug("{}: Comet context still active, returning empty drive actions.", this);
                write(EMPTY_ACTIONS);
            }
        } finally {
            resume(event.getCometContext());
        }
    }

    /**
     * Serializes the supplied actions to the underlying response stream.
     *
     * @param actions The actions to write
     */
    private void write(List<DriveAction<? extends DriveVersion>> actions) {
        Writer responseWriter = null != getResponse() ? getResponse().getWriter() : null;
        if (null == responseWriter) {
            LOG.warn("{}: Unable to access response writer, unable to write JSON result", this);
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", JsonDriveAction.serialize(actions, session.getLocale()));
            jsonObject.write(responseWriter);
        } catch (JSONException e) {
            LOG.error("Error writing json response", e);
        }
    }

    /**
     * Resumes the comet context for this request and removes this handler from the active handler list. Conveniently, the response
     * writer is closed implicitly.
     *
     * @param cometContext The comet context
     */
    private void resume(CometContext cometContext) throws IOException {
        /*
         * ensure the response writer gets closed
         */
        Response response = getResponse();
        if (null != response) {
            Writer writer = response.getWriter();
            if (null != writer) {
                Streams.close(writer);
            }
        }
        /*
         * resume comet request
         */
        if (null == cometContext) {
            LOG.warn("{}: Unable to access comet context, unable to resume request.", this);
            return;
        }
        if (cometContext.resumeCometHandler(this)) {
            LOG.debug("{}: Resumed successfully.", this);
        }
    }

    @Override
    public String toString() {
        return "[" + Thread.currentThread().getId() + "] DriveCometHandler [" +
            session.getServerSession().getLogin() + ':' + session.getRootFolderID() + ']';
    }

}

