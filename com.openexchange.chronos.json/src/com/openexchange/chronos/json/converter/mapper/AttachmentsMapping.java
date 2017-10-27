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

package com.openexchange.chronos.json.converter.mapper;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.exception.OXException;
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
    protected Attachment deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
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
