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

package com.openexchange.folderstorage.calendar;

import static com.openexchange.java.Autoboxing.L;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarAccountField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAccountField extends FolderField {

    /** The column identifier of the field as used in the HTTP API */
    private static final int COLUMN_ID = 3202;

    /** The column name of the field as used in the HTTP API */
    private static final String COLUMN_NAME = "com.openexchange.calendar.account";

    private static final long serialVersionUID = -8172813977801568827L;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarAccountField.class);
    private static final CalendarAccountField INSTANCE = new CalendarAccountField();

    /**
     * Gets the extended properties field instance.
     *
     * @return The instance
     */
    public static CalendarAccountField getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link CalendarAccountField}.
     */
    private CalendarAccountField() {
        super(COLUMN_ID, COLUMN_NAME, null);
    }

    @Override
    public FolderProperty parse(Object value) {
        if (null != value) {
            try {
                return new FolderProperty(getName(), readAccount((JSONObject) value));
            } catch (Exception e) {
                LOG.warn("Error parsing extended calendar properties from \"{}\": {}", value, e.getMessage(), e);
            }
        }
        return new FolderProperty(getName(), null);
    }

    @Override
    public Object write(FolderProperty property, ServerSession session) {
        if (null != property) {
            try {
                return writeAccount((CalendarAccount) property.getValue());
            } catch (Exception e) {
                LOG.warn("Error writing calendar account \"{}\": {}", property.getValue(), e.getMessage(), e);
            }
        }
        return getDefaultValue();
    }

    private static CalendarAccount readAccount(JSONObject jsonObject) throws JSONException {
        return new DefaultCalendarAccount(
            jsonObject.optString("provider", null),
            jsonObject.optInt("id"),
            0,
            null,
            jsonObject.optJSONObject("config"),
            jsonObject.has("timestamp") ? new Date(jsonObject.getLong("timestamp")) : null
        );
    }

    private static JSONObject writeAccount(CalendarAccount account) throws JSONException {
        return new JSONObject()
            .put("id", account.getAccountId())
            .put("provider", account.getProviderId())
            .putOpt("timestamp", null != account.getLastModified() ? L(account.getLastModified().getTime()) : null)
            .putOpt("config", account.getUserConfiguration())
        ;
    }

}
