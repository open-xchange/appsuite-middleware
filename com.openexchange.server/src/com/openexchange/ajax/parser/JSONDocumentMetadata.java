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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.URLHelper;

public class JSONDocumentMetadata implements DocumentMetadata {

    private static final long serialVersionUID = -5016635593135118691L;

    private static final URLHelper helper = new URLHelper();


    private final JSONObject jsonObject;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JSONDocumentMetadata.class);
    private static final String DEFAULT_MIMETYPE = "application/octet-stream";
    //private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(JSONDocumentMetadata.class);

    public JSONDocumentMetadata(){
        this.jsonObject = new JSONObject();
    }

    public JSONDocumentMetadata(final String json) throws JSONException {
        this.jsonObject = new JSONObject(json);

        //Test parsing of complex objects
        if(jsonObject.has(Metadata.URL_LITERAL.getName())) {
            String url = jsonObject.getString(Metadata.URL_LITERAL.getName());
            if(!"".equals(url.trim())) {
                url = helper.process(url);
                jsonObject.put(Metadata.URL_LITERAL.getName(),url);
            }
        }
    }

    @Override
    public String getProperty(final String key) {
        if(Metadata.get(key) == null) {
            return jsonObject.optString(key);
        }
        return null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return null;
    }

    @Override
    public Date getLastModified() {
        if(!jsonObject.has(Metadata.LAST_MODIFIED_LITERAL.getName())) {
            return null;
        }
        return new Date(jsonObject.optLong(Metadata.LAST_MODIFIED_LITERAL.getName()));
    }

    @Override
    public void setLastModified(final Date now) {
        try {
            jsonObject.put(Metadata.LAST_MODIFIED_LITERAL.getName(), now.getTime());
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public Date getCreationDate() {
        if(!jsonObject.has(Metadata.CREATION_DATE_LITERAL.getName())) {
            return null;
        }
        return new Date(jsonObject.optLong(Metadata.CREATION_DATE_LITERAL.getName()));
    }

    @Override
    public void setCreationDate(final Date creationDate) {
        try {
            jsonObject.put(Metadata.CREATION_DATE_LITERAL.getName(), creationDate.getTime());
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public int getModifiedBy() {
        if(!jsonObject.has(Metadata.MODIFIED_BY_LITERAL.getName())) {
            return -1;
        }
        return jsonObject.optInt(Metadata.MODIFIED_BY_LITERAL.getName());

    }

    @Override
    public void setModifiedBy(final int lastEditor) {
        try {
            jsonObject.put(Metadata.MODIFIED_BY_LITERAL.getName(), lastEditor);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public long getFolderId() {
        if(!jsonObject.has(Metadata.FOLDER_ID_LITERAL.getName())) {
            return -1;
        }
        return jsonObject.optLong(Metadata.FOLDER_ID_LITERAL.getName());
    }

    @Override
    public void setFolderId(final long folderId) {
        try {
            jsonObject.put(Metadata.FOLDER_ID_LITERAL.getName(), folderId);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getTitle() {
        if(!jsonObject.has(Metadata.TITLE_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.TITLE_LITERAL.getName());
    }

    @Override
    public void setTitle(final String title) {
        try {
            jsonObject.put(Metadata.TITLE_LITERAL.getName(), title);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public int getVersion() {
        if(!jsonObject.has(Metadata.VERSION_LITERAL.getName())) {
            return 0;
        }
        return jsonObject.optInt(Metadata.VERSION_LITERAL.getName());
    }

    @Override
    public void setVersion(final int version) {
        try {
            jsonObject.put(Metadata.VERSION_LITERAL.getName(), version);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getContent() {
        return getDescription();
    }

    @Override
    public long getFileSize() {
        if(!jsonObject.has(Metadata.FILE_SIZE_LITERAL.getName())) {
            return -1;
        }
        return jsonObject.optLong(Metadata.FILE_SIZE_LITERAL.getName());
    }

    @Override
    public void setFileSize(final long length) {
        try {
            jsonObject.put(Metadata.FILE_SIZE_LITERAL.getName(), length);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getFileMIMEType() {
        if(!jsonObject.has(Metadata.FILE_MIMETYPE_LITERAL.getName())) {
            return DEFAULT_MIMETYPE;
        }
        return jsonObject.optString(Metadata.FILE_MIMETYPE_LITERAL.getName());
    }

    @Override
    public void setFileMIMEType(final String type) {
        try {
            jsonObject.put(Metadata.FILE_MIMETYPE_LITERAL.getName(), type);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getFileName() {
        if(!jsonObject.has(Metadata.FILENAME_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.FILENAME_LITERAL.getName());
    }

    @Override
    public void setFileName(final String fileName) {
        try {
            jsonObject.put(Metadata.FILENAME_LITERAL.getName(), fileName);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public int getId() {
        if(!jsonObject.has(Metadata.ID_LITERAL.getName())) {
            return InfostoreFacade.NEW;
        }
        return jsonObject.optInt(Metadata.ID_LITERAL.getName());
    }

    @Override
    public void setId(final int id) {
        try {
            jsonObject.put(Metadata.ID_LITERAL.getName(), id);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public Map<String, Object> getMeta() {
        final JSONObject jMeta = jsonObject.optJSONObject(Metadata.META_LITERAL.getName());
        if (null == jMeta) {
            return null;
        }
        return jMeta.asMap();
    }

    @Override
    public void setMeta(final Map<String, Object> properties) {
        if (null == properties || properties.isEmpty()) {
            jsonObject.remove(Metadata.META_LITERAL.getName());
        } else {
            try {
                jsonObject.put(Metadata.META_LITERAL.getName(), new JSONObject(properties));
            } catch (final JSONException e) {
                LOG.error("",e);
            }
        }
    }

    @Override
    public int getCreatedBy() {
        if(!jsonObject.has(Metadata.CREATED_BY_LITERAL.getName())) {
            return -1;
        }
        return jsonObject.optInt(Metadata.CREATED_BY_LITERAL.getName());
    }

    @Override
    public void setCreatedBy(final int creator) {
        try {
            jsonObject.put(Metadata.CREATED_BY_LITERAL.getName(), creator);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getDescription() {
        if(!jsonObject.has(Metadata.DESCRIPTION_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.DESCRIPTION_LITERAL.getName());
    }

    @Override
    public void setDescription(final String description) {
        try {
            jsonObject.put(Metadata.DESCRIPTION_LITERAL.getName(), description);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getURL() {
        if(!jsonObject.has(Metadata.URL_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.URL_LITERAL.getName());
    }

    @Override
    public void setURL(final String url) {
        try {
            jsonObject.put(Metadata.URL_LITERAL.getName(), url);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public long getSequenceNumber() {
        if(getLastModified()==null) {
            return 0;
        }
        return getLastModified().getTime();
    }

    @Override
    public String getCategories() {
        if(!jsonObject.has(Metadata.CATEGORIES_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.CATEGORIES_LITERAL.getName());
    }

    @Override
    public void setCategories(final String categories) {
        try {
            jsonObject.put(Metadata.CATEGORIES_LITERAL.getName(), categories);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public Date getLockedUntil() {
        if(!jsonObject.has(Metadata.LOCKED_UNTIL_LITERAL.getName())) {
            return null;
        }
        return new Date(jsonObject.optLong(Metadata.LOCKED_UNTIL_LITERAL.getName()));
    }

    @Override
    public void setLockedUntil(final Date lockedUntil) {
        try {
            jsonObject.put(Metadata.LOCKED_UNTIL_LITERAL.getName(), lockedUntil);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getFileMD5Sum() {
        if(!jsonObject.has(Metadata.FILE_MD5SUM_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.FILE_MD5SUM_LITERAL.getName());
    }

    @Override
    public void setFileMD5Sum(final String sum) {
        try {
            jsonObject.put(Metadata.FILE_MD5SUM_LITERAL.getName(), sum);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public int getColorLabel() {
        return jsonObject.optInt(Metadata.COLOR_LABEL_LITERAL.getName());
    }

    @Override
    public void setColorLabel(final int color) {
        try {
            jsonObject.put(Metadata.COLOR_LABEL_LITERAL.getName(), color);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public boolean isCurrentVersion() {
        return jsonObject.optBoolean(Metadata.CURRENT_VERSION_LITERAL.getName());
    }

    @Override
    public void setIsCurrentVersion(final boolean bool) {
        try {
            jsonObject.put(Metadata.CURRENT_VERSION_LITERAL.getName(), bool);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getVersionComment() {
        if(!jsonObject.has(Metadata.VERSION_COMMENT_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.VERSION_COMMENT_LITERAL.getName());
    }

    @Override
    public void setVersionComment(final String string) {
        try {
            jsonObject.put(Metadata.VERSION_COMMENT_LITERAL.getName(), string);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String toString(){
        return jsonObject.toString();
    }

    public String toJSONString(){
        return jsonObject.toString();
    }

    @Override
    public String getFilestoreLocation() {
        if(!jsonObject.has(Metadata.FILESTORE_LOCATION_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.FILESTORE_LOCATION_LITERAL.getName());
    }



    @Override
    public void setFilestoreLocation(final String string) {
        try {
            jsonObject.put(Metadata.FILESTORE_LOCATION_LITERAL.getName(), string);
        } catch (final JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public void setNumberOfVersions(final int numberOfVersions) {
        try {
            jsonObject.put(Metadata.NUMBER_OF_VERSIONS_LITERAL.getName(), numberOfVersions);
        } catch (final JSONException e) {
            LOG.error("", e);
        }
    }

    @Override
    public int getNumberOfVersions() {
        if(jsonObject.has(Metadata.NUMBER_OF_VERSIONS_LITERAL.getName())) {
            return jsonObject.optInt(Metadata.NUMBER_OF_VERSIONS_LITERAL.getName());
        }
        return -1;
    }

    @Override
    public List<ObjectPermission> getObjectPermissions() {
        if (jsonObject.has(Metadata.OBJECT_PERMISSIONS_LITERAL.getName())) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(Metadata.OBJECT_PERMISSIONS_LITERAL.getName());
                if (null != jsonArray) {
                    List<ObjectPermission> objectPermissions = new ArrayList<ObjectPermission>(jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonPermission = jsonArray.getJSONObject(i);
                        int entity = jsonPermission.getInt("entity");
                        boolean group = jsonPermission.getBoolean("group");
                        int permissions = jsonPermission.getInt("bits");
                        objectPermissions.add(new ObjectPermission(entity, group, permissions));
                    }
                }
            } catch (JSONException e) {
                LOG.error("", e);
            }
        }
        return null;
    }

    @Override
    public void setObjectPermissions(List<ObjectPermission> objectPermissions) {
        try {
            if (null == objectPermissions) {
                jsonObject.put(Metadata.OBJECT_PERMISSIONS_LITERAL.getName(), JSONObject.NULL);
            } else {
                JSONArray jsonArray = new JSONArray(objectPermissions.size());
                for (int i = 0; i < objectPermissions.size(); i++) {
                    ObjectPermission objectPermission = objectPermissions.get(i);
                    JSONObject jsonPermission = new JSONObject(3);
                    jsonPermission.put("entity", objectPermission.getEntity());
                    jsonPermission.put("group", objectPermission.isGroup());
                    jsonPermission.put("bits", objectPermission.getPermissions());

                }
                jsonObject.put(Metadata.OBJECT_PERMISSIONS_LITERAL.getName(), jsonArray);
            }
        } catch (JSONException e) {
            LOG.error("", e);
        }
    }

    @Override
    public boolean isShareable() {
        return jsonObject.optBoolean(Metadata.OBJECT_PERMISSIONS_LITERAL.getName());
    }

    @Override
    public void setShareable(boolean shareable) {
        if (shareable) {
            try {
                jsonObject.put(Metadata.SHAREABLE_LITERAL.getName(), shareable);
            } catch (JSONException e) {
                LOG.error("", e);
            }
        } else {
            jsonObject.remove(Metadata.SHAREABLE_LITERAL.getName());
        }
    }

    @Override
    public int getOriginalId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOriginalId(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getOriginalFolderId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOriginalFolderId(long id) {
        throw new UnsupportedOperationException();
    }

}
