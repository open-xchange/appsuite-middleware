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

package com.openexchange.ajax.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreFolderPath;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.URLHelper;
import com.openexchange.java.GeoLocation;

/**
 * Parses a <code>DocumentMetadata</code> from its JSON representation
 *
 * @deprecated Only used for testing
 */
@Deprecated
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
        if (jsonObject.has(Metadata.URL_LITERAL.getName())) {
            String url = jsonObject.getString(Metadata.URL_LITERAL.getName());
            if (!"".equals(url.trim())) {
                url = helper.process(url);
                jsonObject.put(Metadata.URL_LITERAL.getName(),url);
            }
        }
    }

    @Override
    public String getProperty(final String key) {
        if (Metadata.get(key) == null) {
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
        if (!jsonObject.has(Metadata.LAST_MODIFIED_LITERAL.getName())) {
            return null;
        }
        return new Date(jsonObject.optLong(Metadata.LAST_MODIFIED_LITERAL.getName()));
    }

    @Override
    public void setLastModified(final Date now) {
        try {
            if (now != null) {
                jsonObject.put(Metadata.LAST_MODIFIED_LITERAL.getName(), now.getTime());
            } else {
                jsonObject.remove(Metadata.LAST_MODIFIED_LITERAL.getName());
            }
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public Date getCreationDate() {
        if (!jsonObject.has(Metadata.CREATION_DATE_LITERAL.getName())) {
            return null;
        }
        return new Date(jsonObject.optLong(Metadata.CREATION_DATE_LITERAL.getName()));
    }

    @Override
    public void setCreationDate(final Date creationDate) {
        try {
            if (creationDate != null) {
                jsonObject.put(Metadata.CREATION_DATE_LITERAL.getName(), creationDate.getTime());
            } else {
                jsonObject.remove(Metadata.CREATION_DATE_LITERAL.getName());
            }
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public int getModifiedBy() {
        if (!jsonObject.has(Metadata.MODIFIED_BY_LITERAL.getName())) {
            return -1;
        }
        return jsonObject.optInt(Metadata.MODIFIED_BY_LITERAL.getName());

    }

    @Override
    public void setModifiedBy(final int lastEditor) {
        try {
            jsonObject.put(Metadata.MODIFIED_BY_LITERAL.getName(), lastEditor);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public long getFolderId() {
        if (!jsonObject.has(Metadata.FOLDER_ID_LITERAL.getName())) {
            return -1;
        }
        return jsonObject.optLong(Metadata.FOLDER_ID_LITERAL.getName());
    }

    @Override
    public void setFolderId(final long folderId) {
        try {
            jsonObject.put(Metadata.FOLDER_ID_LITERAL.getName(), folderId);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getTitle() {
        if (!jsonObject.has(Metadata.TITLE_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.TITLE_LITERAL.getName());
    }

    @Override
    public void setTitle(final String title) {
        try {
            jsonObject.put(Metadata.TITLE_LITERAL.getName(), title);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public int getVersion() {
        if (!jsonObject.has(Metadata.VERSION_LITERAL.getName())) {
            return 0;
        }
        return jsonObject.optInt(Metadata.VERSION_LITERAL.getName());
    }

    @Override
    public void setVersion(final int version) {
        try {
            jsonObject.put(Metadata.VERSION_LITERAL.getName(), version);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getContent() {
        return getDescription();
    }

    @Override
    public long getFileSize() {
        if (!jsonObject.has(Metadata.FILE_SIZE_LITERAL.getName())) {
            return -1;
        }
        return jsonObject.optLong(Metadata.FILE_SIZE_LITERAL.getName());
    }

    @Override
    public void setFileSize(final long length) {
        try {
            jsonObject.put(Metadata.FILE_SIZE_LITERAL.getName(), length);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getFileMIMEType() {
        if (!jsonObject.has(Metadata.FILE_MIMETYPE_LITERAL.getName())) {
            return DEFAULT_MIMETYPE;
        }
        return jsonObject.optString(Metadata.FILE_MIMETYPE_LITERAL.getName());
    }

    @Override
    public void setFileMIMEType(final String type) {
        try {
            jsonObject.put(Metadata.FILE_MIMETYPE_LITERAL.getName(), type);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getFileName() {
        if (!jsonObject.has(Metadata.FILENAME_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.FILENAME_LITERAL.getName());
    }

    @Override
    public void setFileName(final String fileName) {
        try {
            jsonObject.put(Metadata.FILENAME_LITERAL.getName(), fileName);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public int getId() {
        if (!jsonObject.has(Metadata.ID_LITERAL.getName())) {
            return InfostoreFacade.NEW;
        }
        return jsonObject.optInt(Metadata.ID_LITERAL.getName());
    }

    @Override
    public void setId(final int id) {
        try {
            jsonObject.put(Metadata.ID_LITERAL.getName(), id);
        } catch (JSONException e) {
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
            } catch (JSONException e) {
                LOG.error("",e);
            }
        }
    }

    @Override
    public int getCreatedBy() {
        if (!jsonObject.has(Metadata.CREATED_BY_LITERAL.getName())) {
            return -1;
        }
        return jsonObject.optInt(Metadata.CREATED_BY_LITERAL.getName());
    }

    @Override
    public void setCreatedBy(final int creator) {
        try {
            jsonObject.put(Metadata.CREATED_BY_LITERAL.getName(), creator);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getDescription() {
        if (!jsonObject.has(Metadata.DESCRIPTION_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.DESCRIPTION_LITERAL.getName());
    }

    @Override
    public void setDescription(final String description) {
        try {
            jsonObject.put(Metadata.DESCRIPTION_LITERAL.getName(), description);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getURL() {
        if (!jsonObject.has(Metadata.URL_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.URL_LITERAL.getName());
    }

    @Override
    public void setURL(final String url) {
        try {
            jsonObject.put(Metadata.URL_LITERAL.getName(), url);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public long getSequenceNumber() {
        if (getLastModified()==null) {
            return 0;
        }
        return getLastModified().getTime();
    }

    @Override
    public String getCategories() {
        if (!jsonObject.has(Metadata.CATEGORIES_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.CATEGORIES_LITERAL.getName());
    }

    @Override
    public void setCategories(final String categories) {
        try {
            jsonObject.put(Metadata.CATEGORIES_LITERAL.getName(), categories);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public Date getLockedUntil() {
        if (!jsonObject.has(Metadata.LOCKED_UNTIL_LITERAL.getName())) {
            return null;
        }
        return new Date(jsonObject.optLong(Metadata.LOCKED_UNTIL_LITERAL.getName()));
    }

    @Override
    public void setLockedUntil(final Date lockedUntil) {
        try {
            jsonObject.put(Metadata.LOCKED_UNTIL_LITERAL.getName(), lockedUntil);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getFileMD5Sum() {
        if (!jsonObject.has(Metadata.FILE_MD5SUM_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.FILE_MD5SUM_LITERAL.getName());
    }

    @Override
    public void setFileMD5Sum(final String sum) {
        try {
            jsonObject.put(Metadata.FILE_MD5SUM_LITERAL.getName(), sum);
        } catch (JSONException e) {
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
        } catch (JSONException e) {
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
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public String getVersionComment() {
        if (!jsonObject.has(Metadata.VERSION_COMMENT_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.VERSION_COMMENT_LITERAL.getName());
    }

    @Override
    public void setVersionComment(final String string) {
        try {
            jsonObject.put(Metadata.VERSION_COMMENT_LITERAL.getName(), string);
        } catch (JSONException e) {
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
        if (!jsonObject.has(Metadata.FILESTORE_LOCATION_LITERAL.getName())) {
            return null;
        }
        return jsonObject.optString(Metadata.FILESTORE_LOCATION_LITERAL.getName());
    }



    @Override
    public void setFilestoreLocation(final String string) {
        try {
            jsonObject.put(Metadata.FILESTORE_LOCATION_LITERAL.getName(), string);
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public void setNumberOfVersions(final int numberOfVersions) {
        try {
            jsonObject.put(Metadata.NUMBER_OF_VERSIONS_LITERAL.getName(), numberOfVersions);
        } catch (JSONException e) {
            LOG.error("", e);
        }
    }

    @Override
    public int getNumberOfVersions() {
        if (jsonObject.has(Metadata.NUMBER_OF_VERSIONS_LITERAL.getName())) {
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

    @Override
    public void setSequenceNumber(long sequenceNumber) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InfostoreFolderPath getOriginFolderPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOriginFolderPath(InfostoreFolderPath originFolderPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getCaptureDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCaptureDate(Date captureDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeoLocation getGeoLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGeoLocation(GeoLocation geoLocation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWidth(long width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeight(long height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCameraMake() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCameraMake(String cameraMake) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCameraModel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCameraModel(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getCameraIsoSpeed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCameraIsoSpeed(long isoSpeed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Double getCameraAperture() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCameraAperture(double aperture) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Double getCameraExposureTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCameraExposureTime(double exposureTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Double getCameraFocalLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCameraFocalLength(double focalLength) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getMediaMeta() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMediaMeta(Map<String, Object> mediaMeta) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MediaStatus getMediaStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMediaStatus(MediaStatus infostoreMediaStatus) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MediaStatus getMediaStatusForClient(com.openexchange.session.Session session) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityInfo getCreatedFrom() {
        if (!jsonObject.has(Metadata.CREATED_FROM_LITERAL.getName())) {
            return null;
        }
        return EntityInfo.parseJSON(jsonObject.optJSONObject(Metadata.CREATED_FROM_LITERAL.getName()));
    }

    @Override
    public void setCreatedFrom(EntityInfo createdFrom) {
        try {
            if (null != createdFrom) {
                jsonObject.put(Metadata.CREATED_FROM_LITERAL.getName(), createdFrom.toJSON());
            } else {
                jsonObject.remove(Metadata.CREATED_FROM_LITERAL.getName());
            }
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

    @Override
    public EntityInfo getModifiedFrom() {
        if (!jsonObject.has(Metadata.MODIFIED_FROM_LITERAL.getName())) {
            return null;
        }
        return EntityInfo.parseJSON(jsonObject.optJSONObject(Metadata.MODIFIED_FROM_LITERAL.getName()));
    }

    @Override
    public void setModifiedFrom(EntityInfo modifiedFrom) {
        try {
            if (null != modifiedFrom) {
                jsonObject.put(Metadata.MODIFIED_FROM_LITERAL.getName(), modifiedFrom.toJSON());
            } else {
                jsonObject.remove(Metadata.MODIFIED_FROM_LITERAL.getName());
            }
        } catch (JSONException e) {
            LOG.error("",e);
        }
    }

}
