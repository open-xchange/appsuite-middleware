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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEvent.Type;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.json.DefaultLongPollingListener;
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

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
     */
    public CometListener(DriveSession session, CometContext<DriveEvent> cometContext) {
        super(session);
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
                cometHandler = new DriveCometHandler(driveSession);
                cometContext.addCometHandler(cometHandler);
                /*
                 * return placeholder result for now
                 */
                AJAXRequestResult noResultYet = new AJAXRequestResult();
                noResultYet.setType(ResultType.DIRECT);
                return noResultYet;
            } else {
                /*
                 * consume available event directly
                 */
                LOG.debug("Stored event available for {}, no need to wait.", driveSession);
                AJAXRequestResult result = createResult(this.event);
                this.event = null;
                return result;
            }
        } finally {
            lock.unlock();
        }
    }

    protected AJAXRequestResult createResult(DriveEvent event) throws OXException {
        /*
         * create and return resulting actions if available
         */
        List<DriveAction<? extends DriveVersion>> actions = null != event ? event.getActions(getSession()) :
            new ArrayList<DriveAction<? extends DriveVersion>>(0);
        try {
            return new AJAXRequestResult(JsonDriveAction.serialize(actions, Locale.US), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
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

    private boolean isInteresting(DriveEvent event) {
        return null != event && null != event.getFolderIDs() && event.getFolderIDs().contains(driveSession.getRootFolderID());
    }

    @Override
    public String toString() {
        return "CometListener [session=" + driveSession + "]";
    }

}