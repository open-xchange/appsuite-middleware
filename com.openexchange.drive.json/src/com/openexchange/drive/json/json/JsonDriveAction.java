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

package com.openexchange.drive.json.json;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.FileVersion;
import com.openexchange.exception.OXException;


/**
 * {@link JsonDriveAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonDriveAction<T extends DriveVersion> implements DriveAction<T> {

    private final Action action;
    private final T version;
    private final T newVersion;
    private final Map<String, Object> parameters;

    /**
     * Initializes a new {@link JsonDriveAction}.
     *
     * @param action The action
     * @param version The (previous) version referenced by the action
     * @param newVersion The (new) version referenced by the action
     * @param parameters The list of action parameters (possible parameter names are defined at {@link DriveAction#PARAMETER_NAMES})
     */
    public JsonDriveAction(Action action, T version, T newVersion, Map<String, Object> parameters) {
        super();
        this.action = action;
        this.version = version;
        this.newVersion = newVersion;
        this.parameters = parameters;
    }

    /**
     * Serializes the supplied drive action to JSON.
     *
     * @param <T> The drive version type, either {@link FileVersion} or {@link DirectoryVersion}
     * @param action The action to serialize
     * @param locale The locale to use during serialization
     * @return The serialized action
     * @throws JSONException
     */
    public static <T extends DriveVersion> JSONObject serialize(DriveAction<T> action, Locale locale) throws JSONException {
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
                    if (DriveAction.PARAMETER_ERROR.equals(entry.getKey()) && OXException.class.isInstance(entry.getValue())) {
                        JSONObject errorObject = new JSONObject();
                        ResponseWriter.addException(errorObject, (OXException)entry.getValue(), locale, false);
                        jsonObject.put("error", errorObject);
                    } else {
                        jsonObject.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        return jsonObject;
    }

    /**
     * Serializes the supplied drive actions to JSON.
     *
     * @param <T> The drive version type, either {@link FileVersion} or {@link DirectoryVersion}
     * @param actions The actions to serialize
     * @param locale The locale to use during serialization
     * @return The serialized actions
     * @throws JSONException
     */
    public static <T extends DriveVersion> JSONArray serializeActions(List<DriveAction<T>> actions, Locale locale) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (DriveAction<? extends DriveVersion> action : actions) {
            jsonArray.put(JsonDriveAction.serialize(action, locale));
        }
        return jsonArray;
    }

    /**
     * Serializes the supplied drive actions to JSON.
     *
     * @param actions The actions to serialize, either of type {@link FileVersion} or {@link DirectoryVersion}
     * @param locale The locale to use during serialization
     * @return The serialized actions
     * @throws JSONException
     */
    public static JSONArray serialize(List<DriveAction<? extends DriveVersion>> actions, Locale locale) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (DriveAction<? extends DriveVersion> action : actions) {
            jsonArray.put(JsonDriveAction.serialize(action, locale));
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
