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

package com.openexchange.ajax.share.actions;

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link ShareLink}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareLink {

    private final String shareURL;
    private final int entity;
    private final String password;
    private final Date expiry;
    private final boolean isNew;

    /**
     * Initializes a new {@link ShareLink}.
     *
     * @param json The JSON object to parse
     * @param timeZone The timezone to use
     */
    public ShareLink(JSONObject json, TimeZone timeZone) throws JSONException {
        super();
        shareURL = json.optString("url", null);
        entity = json.getInt("entity");
        password = json.optString("password", null);
        isNew = json.optBoolean("is_new", false);
        if (json.hasAndNotNull("expiry_date")) {
            long date = json.getLong("expiry_date");
            if (null != timeZone) {
                date -= timeZone.getOffset(date);
            }
            expiry = new Date(date);
        } else {
            expiry = null;
        }
    }

    public boolean isNew() {
        return isNew;
    }

    public String getShareURL() {
        return shareURL;
    }

    public int getEntity() {
        return entity;
    }

    public String getPassword() {
        return password;
    }

    public Date getExpiry() {
        return expiry;
    }

}
