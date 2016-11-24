/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.java.Streams;

/**
 * {@link DriveCometHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveCometHandler extends DefaultCometHandler<DriveEvent> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveCometHandler.class);
    private static final List<DriveAction<? extends DriveVersion>> EMPTY_ACTIONS = Collections.emptyList();

    private final DriveSession session;
    private final List<String> rootFolderIDs;
    private final AtomicLong initializationTime;

    /**
     * Initializes a new {@link DriveCometHandler}.
     *
     * @param session The session
     * @param rootFolderIDs The root folder IDs to listen for changes in
     */
    public DriveCometHandler(DriveSession session, List<String> rootFolderIDs) {
        super();
        this.session = session;
        this.rootFolderIDs = rootFolderIDs;
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
                        List<DriveAction<? extends DriveVersion>> actions = driveEvent.getActions(rootFolderIDs);
                        if (null != actions && 0 < actions.size()) {
                            write(actions);
                        }
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

