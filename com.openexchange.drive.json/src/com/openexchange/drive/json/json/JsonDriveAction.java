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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.drive.json.json;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.FileVersion;


/**
 * {@link JsonDriveAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class JsonDriveAction<T extends DriveVersion> implements DriveAction<T> {

    protected final Action action;
    protected final T version;
    protected final T newVersion;
    protected final Map<String, Object> parameters;

    public JsonDriveAction(Action action, T version, T newVersion, Map<String, Object> parameters) {
        super();
        this.action = action;
        this.version = version;
        this.newVersion = newVersion;
        this.parameters = parameters;
    }

    public static JSONObject serialize(DriveAction<? extends DriveVersion> action) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("action", action.getAction().toString().toLowerCase());
        if (null != action.getVersion()) {
            if (FileVersion.class.isInstance(action.getVersion())) {
                jsonObject.putOpt("version", JsonFileVersion.serialize((FileVersion)action.getVersion()));
            } else if (DirectoryVersion.class.isInstance(action.getVersion())) {
                jsonObject.putOpt("version", JsonDirectoryVersion.serialize((DirectoryVersion)action.getVersion()));
            } else {
                throw new UnsupportedOperationException("Unsupported drive version: " + action.getVersion().getClass());
            }
        }
        if (null != action.getNewVersion()) {
            if (FileVersion.class.isInstance(action.getNewVersion())) {
                jsonObject.putOpt("newVersion", JsonFileVersion.serialize((FileVersion)action.getNewVersion()));
            } else if (DirectoryVersion.class.isInstance(action.getNewVersion())) {
                jsonObject.putOpt("newVersion", JsonDirectoryVersion.serialize((DirectoryVersion)action.getNewVersion()));
            } else {
                throw new UnsupportedOperationException("Unsupported drive version: " + action.getNewVersion().getClass());
            }
        }
        if (null != action.getParameters()) {
            for (Map.Entry<String, Object> entry : action.getParameters().entrySet()) {
                if (DriveAction.PARAMETER_NAMES.contains(entry.getKey())) {
                    jsonObject.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return jsonObject;
    }

    public static JSONArray serialize(List<DriveAction<? extends DriveVersion>> actions) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (DriveAction<? extends DriveVersion> action : actions) {
            jsonArray.put(serialize(action));
        }
        return jsonArray;
    }


    @Override
    public T getVersion() {
        return version;
    }

    @Override
    public T getNewVersion() {
        return newVersion;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public int compareTo(DriveAction<T> other) {
        Action thisAction = this.getAction();
        Action otherAction = null != other ? other.getAction() : null;
        if (null == otherAction) {
            return null == thisAction ? 0 : -1;
        }
        return thisAction.compareTo(otherAction);
    }

    @Override
    public String toString() {
        return getAction() + " [version=" + version + ", newVersion=" + newVersion + ", parameters=" + parameters + "]";
    }

}
