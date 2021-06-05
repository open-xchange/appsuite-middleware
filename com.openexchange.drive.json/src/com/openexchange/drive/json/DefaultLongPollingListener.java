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

package com.openexchange.drive.json;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.subscribe.SubscriptionMode;
import com.openexchange.drive.json.json.JsonDriveAction;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link DefaultLongPollingListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultLongPollingListener implements LongPollingListener {

    protected final DriveSession driveSession;
    protected final List<String> rootFolderIDs;
    protected final SubscriptionMode mode;

    /**
     * Initializes a new {@link DefaultLongPollingListener}.
     *
     * @param driveSession The drive session
     * @param rootFolderIDs The root folder IDs to listen for changes in
     * @param mode The subscription mode
     */
    protected DefaultLongPollingListener(DriveSession driveSession, List<String> rootFolderIDs, SubscriptionMode mode) {
        super();
        this.driveSession = driveSession;
        this.rootFolderIDs = rootFolderIDs;
        this.mode = mode;
    }

    @Override
    public DriveSession getSession() {
        return driveSession;
    }

    @Override
    public boolean matches(String tokenRef) {
        String token = extractToken(driveSession);
        return null == tokenRef ? null == token : tokenRef.equals(token) || tokenRef.equals(getMD5(token));
    }

    @Override
    public List<String> getRootFolderIDs() {
        return rootFolderIDs;
    }

    @Override
    public SubscriptionMode getSubscriptionMode() {
        return mode;
    }

    /**
     * Creates an AJAX request result containing the sync actions for the client based on the supplied drive event.
     *
     * @param event The drive event to generate the result for
     * @return The result
     */
    protected AJAXRequestResult createResult(DriveEvent event) throws OXException {
        /*
         * create and return resulting actions if available
         */
        List<DriveAction<? extends DriveVersion>> actions = null != event ? event.getActions(rootFolderIDs, SubscriptionMode.SEPARATE.equals(mode)) :
            Collections.<DriveAction<? extends DriveVersion>>emptyList();
        try {
            return new AJAXRequestResult(JsonDriveAction.serialize(actions, Locale.US), "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets a value indicating whether the supplied event is of interest for the underlying listener or not.
     *
     * @param event The event
     * @return <code>true</code> if it's interesting, <code>false</code>, otherwise
     */
    protected boolean isInteresting(DriveEvent event) {
        if (null != event && null != event.getFolderIDs() && null != rootFolderIDs) {
            for (String rootFolderID : rootFolderIDs) {
                if (event.getFolderIDs().contains(rootFolderID)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String extractToken(DriveSession driveSession) {
        if (null != driveSession && null != driveSession.getServerSession()) {
            return (String) driveSession.getServerSession().getParameter(DriveSession.PARAMETER_PUSH_TOKEN);
        }
        return null;
    }

    private static String getMD5(String string) {
        if (Strings.isEmpty(string)) {
            return string;
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(string.getBytes(Charsets.UTF_8));
            return new BigInteger(1, digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            return null; // ignore
        }
    }

}
