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

package com.openexchange.drive.json.internal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.DriveQuota;
import com.openexchange.file.storage.Quota;

/**
 * {@link DriveJSONUtils}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class DriveJSONUtils {

    /**
     * Serializes given {@link DriveQuota} object into {@link JSONArray}
     * 
     * @param driveQuota The quota to serialize
     * @throws JSONException
     */
    public static JSONArray serializeQuota(DriveQuota driveQuota) throws JSONException {
        if (driveQuota == null) {
            return JSONArray.EMPTY_ARRAY;
        }
        JSONArray jsonArray = new JSONArray(2);
        Quota[] quota = driveQuota.getQuota();
        if (null != quota && quota.length > 0) {
            for (Quota q : quota) {
                if (Quota.UNLIMITED != q.getLimit()) {
                    JSONObject jsonQuota = new JSONObject();
                    jsonQuota.put("limit", q.getLimit());
                    jsonQuota.put("use", q.getUsage());
                    jsonQuota.put("type", String.valueOf(q.getType()).toLowerCase());
                    jsonArray.put(jsonQuota);
                }
            }
        }
        return jsonArray;
    }
}
