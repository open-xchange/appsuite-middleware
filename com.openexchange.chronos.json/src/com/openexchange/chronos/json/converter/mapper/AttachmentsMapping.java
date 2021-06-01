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

package com.openexchange.chronos.json.converter.mapper;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.groupware.tools.mappings.json.ListItemMapping;
import com.openexchange.session.Session;

/**
 *
 * {@link AttachmentsMapping}>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public abstract class AttachmentsMapping<O> extends ListItemMapping<Attachment, O, JSONObject> {

    /**
     * Initializes a new {@link AttachmentsMapping}.
     *
     * @param ajaxName The mapped ajax name
     * @param columnID The mapped column identifier
     */
    public AttachmentsMapping(String ajaxName, Integer columnID) {
        super(ajaxName, columnID);
    }

    @Override
    protected Attachment deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException {
        JSONObject jsonObject = array.getJSONObject(index);
        return deserialize(jsonObject, timeZone);
    }

    @Override
    public Object serialize(O from, TimeZone timeZone, Session session) throws JSONException {
        List<Attachment> value = get(from);
        if (null == value) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(value.size());
        for (Attachment attachment : value) {

            jsonArray.put(serialize(attachment, timeZone));
        }
        return jsonArray;
    }

    @Override
    public Attachment deserialize(JSONObject from, TimeZone timeZone) throws JSONException {
        Attachment attachment = new Attachment();

        if (from.has(ChronosJsonFields.Attachment.FORMAT_TYPE)) {
            attachment.setFormatType(from.getString(ChronosJsonFields.Attachment.FORMAT_TYPE));
        }

        if (from.has(ChronosJsonFields.Attachment.FILENAME)) {
            attachment.setFilename(from.getString(ChronosJsonFields.Attachment.FILENAME));
        }

        if (from.has(ChronosJsonFields.Attachment.SIZE)) {
            attachment.setSize(from.getLong(ChronosJsonFields.Attachment.SIZE));
        }
        if (from.has(ChronosJsonFields.Attachment.CREATED)) {
            long date = from.getLong(ChronosJsonFields.Attachment.CREATED);
            if (null != timeZone) {
                date -= timeZone.getOffset(date);
            }
            attachment.setCreated(new Date(date));
        }
        /**
         * The attachment is either a uri or a managed id.
         */
        if (from.has(ChronosJsonFields.Attachment.URI)) {
            attachment.setUri(from.getString(ChronosJsonFields.Attachment.URI));
            return attachment;
        }

        if (from.has(ChronosJsonFields.Attachment.MANAGED_ID)) {
            attachment.setManagedId(from.getInt(ChronosJsonFields.Attachment.MANAGED_ID));
            return attachment;
        }
        throw new JSONException("Missing required field. At least one of [" + ChronosJsonFields.Attachment.MANAGED_ID + "," + ChronosJsonFields.Attachment.URI + "] must be present.");
    }

    @Override
    public JSONObject serialize(Attachment attachment, TimeZone timeZone) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (null != attachment.getFilename()) {
            jsonObject.put(ChronosJsonFields.Attachment.FILENAME, attachment.getFilename());
        }
        if (null != attachment.getFormatType()) {
            jsonObject.put(ChronosJsonFields.Attachment.FORMAT_TYPE, attachment.getFormatType());
        }
        if (0 < attachment.getSize()) {
            jsonObject.put(ChronosJsonFields.Attachment.SIZE, attachment.getSize());
        }
        if (null != attachment.getCreated()) {
            long date = attachment.getCreated().getTime();
            if (null != timeZone) {
                date += timeZone.getOffset(date);
            }
            jsonObject.put(ChronosJsonFields.Attachment.CREATED, date);
        }
        if (0 < attachment.getManagedId()) {
            jsonObject.put(ChronosJsonFields.Attachment.MANAGED_ID, attachment.getManagedId());
        }
        if (null != attachment.getUri()) {
            jsonObject.put(ChronosJsonFields.Attachment.URI, attachment.getUri());
        }

        return jsonObject;
    }

}
