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

package com.openexchange.subscribe.json;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.json.FormContentWriter;
import com.openexchange.datatypes.genericonf.json.ValueWriterSwitch;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.Subscription;

/**
 * {@link SubscriptionJSONWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionJSONWriter {

    public static final int CLASS_ID = 2;

    private static final ValueWriterSwitch valueWrite = new ValueWriterSwitch();

    private static final String ID = "id";
    private static final String FOLDER = "folder";
    private static final String ERRORS = "errors";
    private static final String DISPLAYNAME = "displayName";
    private static final String SOURCE = "source";
    private static final String ENABLED = "enabled";
    private static final String CREATED = "created";
    private static final String LAST_UPDATED = "lastUpdated";

    public JSONObject write(final Subscription subscription, final DynamicFormDescription form, final String urlPrefix, TimeZone tz) throws JSONException {
        final JSONObject object = new JSONObject();
        object.put(ID, subscription.getId());
        object.put(FOLDER, subscription.getFolderId());
        object.put(ENABLED, subscription.isEnabled());
        object.put(DISPLAYNAME, subscription.getDisplayName());
        object.put(SOURCE, subscription.getSource().getId());
        object.put(LAST_UPDATED, subscription.getLastUpdate() + (tz != null ? tz.getOffset(subscription.getLastUpdate()) : 0L));
        object.put(CREATED, subscription.getCreated() + (tz != null ? tz.getOffset(subscription.getCreated()) : 0L));
        object.put(ERRORS, subscription.getConfiguration().containsKey(ERRORS) ? subscription.getConfiguration().get(ERRORS) : Boolean.valueOf(false));

        writeConfiguration(object, subscription.getSource().getId(), subscription.getConfiguration(), form, urlPrefix);
        return object;
    }

    private void writeConfiguration(final JSONObject object, final String id, final Map<String, Object> configuration, final DynamicFormDescription form, final String urlPrefix) throws JSONException {
        final JSONObject config = FormContentWriter.write(form, configuration, urlPrefix);
        object.put(id, config);
    }

    public JSONArray writeArray(final Subscription subscription, final String[] basicCols, final Map<String, String[]> specialCols, final List<String> specialsList, final DynamicFormDescription form, TimeZone tz) throws OXException {
        final JSONArray array = new JSONArray();
        writeBasicCols(array, subscription, basicCols, tz);
        for (final String identifier : specialsList) {
            writeSpecialCols(array, subscription, specialCols.get(identifier), identifier, form);
        }
        return array;
    }

    private void writeSpecialCols(final JSONArray array, final Subscription subscription, final String[] strings, final String externalId, final DynamicFormDescription form) {
        if (strings == null) {
            return;
        }
        final boolean writeNulls = !subscription.getSource().getId().equals(externalId);
        final Map<String, Object> configuration = subscription.getConfiguration();
        for (final String col : strings) {
            if (writeNulls) {
                array.put(JSONObject.NULL);
            } else {
                Object value = configuration.get(col);
                final FormElement field = form.getField(col);
                value = field.doSwitch(valueWrite, value);
                array.put(value);
            }
        }
    }

    private void writeBasicCols(final JSONArray array, final Subscription subscription, final String[] basicCols, TimeZone tz) throws OXException {
        for (final String basicCol : basicCols) {
            switch (basicCol) {
                case ID:
                    array.put(subscription.getId());
                    break;
                case FOLDER:
                    array.put(subscription.getFolderId());
                    break;
                case SOURCE:
                    array.put(subscription.getSource().getId());
                    break;
                case DISPLAYNAME:
                    array.put(subscription.getDisplayName());
                    break;
                case ENABLED:
                    array.put(subscription.isEnabled());
                    break;
                case LAST_UPDATED:
                    array.put(subscription.getLastUpdate() + tz.getOffset(subscription.getLastUpdate()));
                    break;
                case CREATED:
                    array.put(subscription.getCreated() + tz.getOffset(subscription.getCreated()));
                    break;
                default:
                    throw SubscriptionJSONErrorMessages.UNKNOWN_COLUMN.create(basicCol);
            }
        }
    }

}
