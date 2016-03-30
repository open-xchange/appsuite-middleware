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

package com.openexchange.ajax.parser;

import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class JSONAttachmentMetadata implements AttachmentMetadata {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JSONAttachmentMetadata.class);

    private final JSONObject json;

    public JSONAttachmentMetadata(JSONObject json) {
        super();
        this.json = new JSONObject(json);
    }

    public JSONAttachmentMetadata(final String jsonString) throws JSONException {
        json = new JSONObject(jsonString);
    }

    public JSONAttachmentMetadata() {
        json = new JSONObject();
    }

    @Override
    public int getCreatedBy() {
        if(json.has(AttachmentField.CREATED_BY_LITERAL.getName())) {
            return json.optInt(AttachmentField.CREATED_BY_LITERAL.getName());
        }
        return -1;
    }

    @Override
    public void setCreatedBy(final int createdBy) {
        try {
            json.put(AttachmentField.CREATED_BY_LITERAL.getName(),createdBy);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public Date getCreationDate() {
        if(!json.has(AttachmentField.CREATION_DATE_LITERAL.getName())) {
            return null;
        }
        return new Date(json.optLong(AttachmentField.CREATION_DATE_LITERAL.getName()));
    }

    @Override
    public void setCreationDate(final Date creationDate) {
        if(creationDate == null && json.has(AttachmentField.CREATION_DATE_LITERAL.getName())) {
            json.remove(AttachmentField.CREATION_DATE_LITERAL.getName());
        } else if(creationDate != null) {
            try {
                json.put(AttachmentField.CREATION_DATE_LITERAL.getName(),creationDate.getTime());
            } catch (final JSONException e) {
                LOG.debug("",e);
            }
        }
    }

    @Override
    public String getFileMIMEType() {
        if(!json.has(AttachmentField.FILE_MIMETYPE_LITERAL.getName())) {
            return null;
        }
        return json.optString(AttachmentField.FILE_MIMETYPE_LITERAL.getName());
    }

    @Override
    public void setFileMIMEType(final String fileMIMEType) {
        if(fileMIMEType == null && json.has(AttachmentField.FILE_MIMETYPE_LITERAL.getName())){
            json.remove(AttachmentField.FILE_MIMETYPE_LITERAL.getName());
            return;
        }
        try {
            json.put(AttachmentField.FILE_MIMETYPE_LITERAL.getName(),fileMIMEType);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public String getFilename() {
        if(!json.has(AttachmentField.FILENAME_LITERAL.getName())) {
            return null;
        }
        return json.optString(AttachmentField.FILENAME_LITERAL.getName());
    }

    @Override
    public void setFilename(final String filename) {
        if(filename == null && json.has(AttachmentField.FILENAME_LITERAL.getName())){
            json.remove(AttachmentField.FILENAME_LITERAL.getName());
            return;
        }
        try {
            json.put(AttachmentField.FILENAME_LITERAL.getName(),filename);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public long getFilesize() {
        if(!json.has(AttachmentField.FILE_SIZE_LITERAL.getName())) {
            return 0;
        }
        return json.optLong(AttachmentField.FILE_SIZE_LITERAL.getName());

    }

    @Override
    public void setFilesize(final long filesize) {
        try {
            json.put(AttachmentField.FILE_SIZE_LITERAL.getName(),filesize);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public int getAttachedId() {
        if(!json.has(AttachmentField.ATTACHED_ID_LITERAL.getName())) {
            return -1;
        }
        return json.optInt(AttachmentField.ATTACHED_ID_LITERAL.getName());

    }

    @Override
    public void setAttachedId(final int objectId) {
        try {
            json.put(AttachmentField.ATTACHED_ID_LITERAL.getName(),objectId);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public boolean getRtfFlag() {
        return json.optBoolean(AttachmentField.RTF_FLAG_LITERAL.getName());
    }

    @Override
    public void setRtfFlag(final boolean rtfFlag) {
        try {
            json.put(AttachmentField.RTF_FLAG_LITERAL.getName(),rtfFlag);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public int getModuleId() {
        if(!json.has(AttachmentField.MODULE_ID_LITERAL.getName())) {
            return -1;
        }
        return json.optInt(AttachmentField.MODULE_ID_LITERAL.getName());

    }

    @Override
    public void setModuleId(final int moduleId) {
        try {
            json.put(AttachmentField.MODULE_ID_LITERAL.getName(),moduleId);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public int getId() {
        if(!json.has(AttachmentField.ID_LITERAL.getName())) {
            return -1;
        }
        return json.optInt(AttachmentField.ID_LITERAL.getName());
    }

    @Override
    public void setId(final int id) {
        try {
            json.put(AttachmentField.ID_LITERAL.getName(),id);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public void setFolderId(final int folderId) {
        try {
            json.put(AttachmentField.FOLDER_ID_LITERAL.getName(),folderId);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public int getFolderId() {
        if(!json.has(AttachmentField.FOLDER_ID_LITERAL.getName())) {
            return -1;
        }
        return json.optInt(AttachmentField.FOLDER_ID_LITERAL.getName());
    }

    @Override
    public String toString(){
        return json.toString();
    }

    public String toJSONString(){
        return json.toString();
    }

    @Override
    public void setComment(final String string) {
        if(null == string) {
            try {
                json.put(AttachmentField.COMMENT_LITERAL.getName(),JSONObject.NULL);
                return;
            } catch (final JSONException e) {
                LOG.debug("",e);
            }
        }
        try {
            json.put(AttachmentField.COMMENT_LITERAL.getName(),string);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

    @Override
    public String getComment() {
        return json.optString(AttachmentField.COMMENT_LITERAL.getName());
    }

    @Override
    public String getFileId() {
        return json.optString(AttachmentField.FILE_ID_LITERAL.getName());
    }

    @Override
    public void setFileId(final String string) {
        if(null == string) {
            try {
                json.put(AttachmentField.COMMENT_LITERAL.getName(),JSONObject.NULL);
                return;
            } catch (final JSONException e) {
                LOG.debug("",e);
            }
        }
        try {
            json.put(AttachmentField.FILE_ID_LITERAL.getName(),string);
        } catch (final JSONException e) {
            LOG.debug("",e);
        }
    }

}
