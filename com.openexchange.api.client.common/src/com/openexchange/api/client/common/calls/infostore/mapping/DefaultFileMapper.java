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

package com.openexchange.api.client.common.calls.infostore.mapping;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.d;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.api.client.common.calls.mapping.EntityInfoMapping;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FolderPath;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.tools.mappings.json.BooleanMapping;
import com.openexchange.groupware.tools.mappings.json.DateMapping;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.DoubleMapping;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.LongMapping;
import com.openexchange.groupware.tools.mappings.json.MapMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;
import com.openexchange.java.GeoLocation;

/**
 * {@link DefaultFileMapper}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class DefaultFileMapper extends DefaultJsonMapper<DefaultFile, File.Field> {

    @Override
    public DefaultFile newInstance() {
        return new DefaultFile();
    }

    @Override
    public Field[] newArray(int size) {
        return new File.Field[size];
    }

    @Override
    protected EnumMap<Field, ? extends JsonMapping<? extends Object, DefaultFile>> createMappings() {
        //@formatter:off
        EnumMap<File.Field, JsonMapping<? extends Object, DefaultFile>> mappings = new
            EnumMap<File.Field, JsonMapping<? extends Object, DefaultFile>>(File.Field.class);
        //@formatter:on

        mappings.put(File.Field.LAST_MODIFIED, new DateMapping<DefaultFile>(File.Field.LAST_MODIFIED.getName(), I(File.Field.LAST_MODIFIED.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getLastModified() != null;
            }

            @Override
            public void set(DefaultFile object, Date value) throws OXException {
                object.setLastModified(value);
            }

            @Override
            public Date get(DefaultFile object) {
                return object.getLastModified();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setLastModified(null);

            }
        });

        mappings.put(File.Field.CREATED, new DateMapping<DefaultFile>(File.Field.CREATED.getName(), I(File.Field.CREATED.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getCreated() != null;
            }

            @Override
            public void set(DefaultFile object, Date value) throws OXException {
                object.setCreated(value);
            }

            @Override
            public Date get(DefaultFile object) {
                return object.getCreated();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setCreated(null);
            }
        });

        mappings.put(File.Field.MODIFIED_BY, new IntegerMapping<DefaultFile>(File.Field.MODIFIED_BY.getName(), I(File.Field.MODIFIED_BY.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getModifiedBy() != 0;
            }

            @Override
            public void set(DefaultFile object, Integer value) throws OXException {
                object.setModifiedBy(value != null ? i(value) : 0);
            }

            @Override
            public Integer get(DefaultFile object) {
                return I(object.getModifiedBy());
            }

            @Override
            public void remove(DefaultFile object) {
                object.setModifiedBy(0);
            }
        });

        mappings.put(File.Field.FOLDER_ID, new StringMapping<DefaultFile>(File.Field.FOLDER_ID.getName(), I(File.Field.FOLDER_ID.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getFolderId() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setFolderId(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getFolderId();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setFolderId(null);
            }
        });

        mappings.put(File.Field.TITLE, new StringMapping<DefaultFile>(File.Field.TITLE.getName(), I(File.Field.TITLE.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getTitle() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setTitle(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getTitle();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setTitle(null);
            }
        });

        mappings.put(File.Field.VERSION, new StringMapping<DefaultFile>(File.Field.VERSION.getName(), I(File.Field.VERSION.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getVersion() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setVersion(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getVersion();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setVersion(null);
            }
        });

        mappings.put(File.Field.ID, new StringMapping<DefaultFile>(File.Field.ID.getName(), I(File.Field.ID.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getId() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setId(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getId();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setId(null);
            }
        });

        mappings.put(File.Field.FILE_SIZE, new LongMapping<DefaultFile>(File.Field.FILE_SIZE.getName(), I(File.Field.FILE_SIZE.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getFileSize() != 0;
            }

            @Override
            public void set(DefaultFile object, Long value) throws OXException {
                if(value != null) {
                    object.setFileSize(l(value));
                }
            }

            @Override
            public Long get(DefaultFile object) {
                return L(object.getFileSize());
            }

            @Override
            public void remove(DefaultFile object) {
                object.setFileSize(0);
            }
        });

        mappings.put(File.Field.DESCRIPTION, new StringMapping<DefaultFile>(File.Field.DESCRIPTION.getName(), I(File.Field.DESCRIPTION.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getDescription() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setDescription(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getDescription();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setDescription(null);
            }
        });

        mappings.put(File.Field.URL, new StringMapping<DefaultFile>(File.Field.URL.getName(), I(File.Field.URL.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getURL() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setURL(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getURL();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setURL(null);
            }
        });

        mappings.put(File.Field.CREATED_BY, new IntegerMapping<DefaultFile>(File.Field.CREATED_BY.getName(), I(File.Field.CREATED_BY.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getCreatedBy() != 0;
            }

            @Override
            public void set(DefaultFile object, Integer value) throws OXException {
                object.setCreatedBy(value != null ? i(value) : 0);
            }

            @Override
            public Integer get(DefaultFile object) {
                return I(object.getCreatedBy());
            }

            @Override
            public void remove(DefaultFile object) {
                object.setCreatedBy(0);
            }
        });

        mappings.put(File.Field.FILENAME, new StringMapping<DefaultFile>(File.Field.FILENAME.getName(), I(File.Field.FILENAME.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getFileName() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setFileName(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getFileName();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setFileName(null);
            }
        });

        mappings.put(File.Field.FILE_MIMETYPE, new StringMapping<DefaultFile>(File.Field.FILE_MIMETYPE.getName(), I(File.Field.FILE_MIMETYPE.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getFileMIMEType() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setFileMIMEType(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getFileMIMEType();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setFileMIMEType(null);
            }
        });

        mappings.put(File.Field.SEQUENCE_NUMBER, new LongMapping<DefaultFile>(File.Field.SEQUENCE_NUMBER.getName(), I(File.Field.SEQUENCE_NUMBER.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getSequenceNumber() != 0;
            }

            @Override
            public void set(DefaultFile object, Long value) throws OXException {
                if(value != null) {
                    object.setSequenceNumber(l(value));
                }
            }

            @Override
            public Long get(DefaultFile object) {
                return L(object.getSequenceNumber());
            }

            @Override
            public void remove(DefaultFile object) {
                object.setSequenceNumber(0);
            }
        });

        mappings.put(File.Field.CATEGORIES, new StringMapping<DefaultFile>(File.Field.CATEGORIES.getName(), I(File.Field.CATEGORIES.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getCategories() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setCategories(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getCategories();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setCategories(null);
            }
        });

        mappings.put(File.Field.LOCKED_UNTIL, new DateMapping<DefaultFile>(File.Field.LOCKED_UNTIL.getName(), I(File.Field.LOCKED_UNTIL.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getLockedUntil() != null;
            }

            @Override
            public void set(DefaultFile object, Date value) throws OXException {
                object.setLockedUntil(value);
            }

            @Override
            public Date get(DefaultFile object) {
                return object.getLockedUntil();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setLockedUntil(null);
            }
        });

        mappings.put(File.Field.FILE_MD5SUM, new StringMapping<DefaultFile>(File.Field.FILE_MD5SUM.getName(), I(File.Field.FILE_MD5SUM.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getFileMD5Sum() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setFileMD5Sum(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getFileMD5Sum();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setFileMD5Sum(null);
            }
        });

        mappings.put(File.Field.VERSION_COMMENT, new StringMapping<DefaultFile>(File.Field.VERSION_COMMENT.getName(), I(File.Field.VERSION_COMMENT.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getVersionComment() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setVersionComment(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getVersionComment();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setVersionComment(null);
            }
        });

        mappings.put(File.Field.CURRENT_VERSION, new BooleanMapping<DefaultFile>(File.Field.CURRENT_VERSION.getName(), I(File.Field.CURRENT_VERSION.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Boolean value) throws OXException {
                if(value != null) {
                    object.setIsCurrentVersion(b(value));
                }
            }

            @Override
            public Boolean get(DefaultFile object) {
                return B(object.isCurrentVersion());
            }

            @Override
            public void remove(DefaultFile object) {
                //no-op
            }
        });

        mappings.put(File.Field.COLOR_LABEL, new IntegerMapping<DefaultFile>(File.Field.COLOR_LABEL.getName(), I(File.Field.COLOR_LABEL.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getColorLabel() != 0;
            }

            @Override
            public void set(DefaultFile object, Integer value) throws OXException {
                object.setColorLabel(value != null ? i(value) : 0);
            }

            @Override
            public Integer get(DefaultFile object) {
                return I(object.getColorLabel());
            }

            @Override
            public void remove(DefaultFile object) {
                object.setColorLabel(0);
            }
        });

        mappings.put(File.Field.LAST_MODIFIED_UTC, new DateMapping<DefaultFile>(File.Field.LAST_MODIFIED_UTC.getName(), I(File.Field.LAST_MODIFIED_UTC.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getLastModified() != null;
            }

            @Override
            public void set(DefaultFile object, Date value) throws OXException {
                object.setLastModified(value);
            }

            @Override
            public Date get(DefaultFile object) {
                return object.getLastModified();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setLastModified(null);

            }
        });

        mappings.put(File.Field.NUMBER_OF_VERSIONS, new IntegerMapping<DefaultFile>(File.Field.NUMBER_OF_VERSIONS.getName(), I(File.Field.NUMBER_OF_VERSIONS.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getNumberOfVersions() != 0;
            }

            @Override
            public void set(DefaultFile object, Integer value) throws OXException {
                object.setNumberOfVersions(value != null ? i(value) : 0);
            }

            @Override
            public Integer get(DefaultFile object) {
                return I(object.getNumberOfVersions());
            }

            @Override
            public void remove(DefaultFile object) {
                object.setNumberOfVersions(0);
            }
        });

        mappings.put(File.Field.META, new MapMapping<DefaultFile>(File.Field.META.getName(), I(File.Field.META.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Map<String, Object> value) throws OXException {
                object.setMeta(value);
            }

            @Override
            public Map<String, Object> get(DefaultFile object) {
                return object.getMeta();
            }

            @Override
            public void remove(DefaultFile object) {
                /* no-op */
            }
        });

        mappings.put(File.Field.OBJECT_PERMISSIONS, new FileStorageObjectPermissionMapping<DefaultFile>(File.Field.OBJECT_PERMISSIONS.getName(), I(File.Field.OBJECT_PERMISSIONS.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getObjectPermissions() != null;
            }

            @Override
            public void set(DefaultFile object, List<FileStorageObjectPermission> value) throws OXException {
                object.setObjectPermissions(value);
            }

            @Override
            public List<FileStorageObjectPermission> get(DefaultFile object) {
                return object.getObjectPermissions();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setObjectPermissions(null);
            }
        });

        mappings.put(File.Field.SHAREABLE, new BooleanMapping<DefaultFile>(File.Field.SHAREABLE.getName(), I(File.Field.SHAREABLE.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Boolean value) throws OXException {
                if(value != null) {
                    object.setShareable(b(value));
                }
            }

            @Override
            public Boolean get(DefaultFile object) {
                return B(object.isShareable());
            }

            @Override
            public void remove(DefaultFile object) {
                /* no-op */
            }
        });

        mappings.put(File.Field.ORIGIN, new StringMapping<DefaultFile>(File.Field.ORIGIN.getName(), I(File.Field.ORIGIN.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getOrigin() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setOrigin(FolderPath.parseFrom(value));
            }

            @Override
            public String get(DefaultFile object) {
                return object.getOrigin() != null ? object.getOrigin().toString() : null;
            }

            @Override
            public void remove(DefaultFile object) {
                object.setOrigin(null);
            }
        });

        mappings.put(File.Field.CAPTURE_DATE, new DateMapping<DefaultFile>(File.Field.CAPTURE_DATE.getName(), I(File.Field.CAPTURE_DATE.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getCaptureDate() != null;
            }

            @Override
            public void set(DefaultFile object, Date value) throws OXException {
                object.setCaptureDate(value);
            }

            @Override
            public Date get(DefaultFile object) {
                return object.getCaptureDate();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setCaptureDate(null);
            }
        });

        mappings.put(File.Field.GEOLOCATION, new GeoLocationMapping<DefaultFile>(File.Field.GEOLOCATION.getName(), I(File.Field.GEOLOCATION.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getGeoLocation() != null;
            }

            @Override
            public void set(DefaultFile object, GeoLocation value) throws OXException {
                object.setGeoLocation(value);
            }

            @Override
            public GeoLocation get(DefaultFile object) {
                return object.getGeoLocation();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setGeoLocation(null);
            }
        });

        mappings.put(File.Field.WIDTH, new LongMapping<DefaultFile>(File.Field.WIDTH.getName(), I(File.Field.WIDTH.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Long value) throws OXException {
                if(value != null) {
                    object.setWidth(l(value));
                }
            }

            @Override
            public Long get(DefaultFile object) {
                return object.getWidth();
            }

            @Override
            public void remove(DefaultFile object) {
                //no-op
            }
        });

        mappings.put(File.Field.HEIGHT, new LongMapping<DefaultFile>(File.Field.HEIGHT.getName(), I(File.Field.HEIGHT.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Long value) throws OXException {
                if(value != null) {
                    object.setHeight(l(value));
                }
            }

            @Override
            public Long get(DefaultFile object) {
                return object.getHeight();
            }

            @Override
            public void remove(DefaultFile object) {
                //no-op
            }
        });

        mappings.put(File.Field.CAMERA_MAKE, new StringMapping<DefaultFile>(File.Field.CAMERA_MAKE.getName(), I(File.Field.CAMERA_MAKE.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getCameraMake() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setCameraMake(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getCameraMake();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setCameraMake(null);
            }
        });

        mappings.put(File.Field.CAMERA_MODEL, new StringMapping<DefaultFile>(File.Field.CAMERA_MODEL.getName(), I(File.Field.CAMERA_MODEL.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getCameraModel() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setCameraModel(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getCameraModel();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setCameraModel(null);
            }
        });

        mappings.put(File.Field.CAMERA_ISO_SPEED, new LongMapping<DefaultFile>(File.Field.CAMERA_ISO_SPEED.getName(), I(File.Field.CAMERA_ISO_SPEED.getNumber())) {

            @Override
            public void deserialize(JSONObject from, DefaultFile to) throws JSONException, OXException {
                String isoSpeed = from.getString(getAjaxName());
                this.set(to, from.isNull(getAjaxName()) ? null : Long.valueOf(isoSpeed));
            }

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Long value) throws OXException {
                if(value != null) {
                    object.setCameraIsoSpeed(l(value));
                }
            }

            @Override
            public Long get(DefaultFile object) {
                return object.getCameraIsoSpeed();
            }

            @Override
            public void remove(DefaultFile object) {
                //no-op
            }
        });

        mappings.put(File.Field.CAMERA_APERTURE, new DoubleMapping<DefaultFile>(File.Field.CAMERA_APERTURE.getName(), I(File.Field.CAMERA_APERTURE.getNumber())) {

            @Override
            public void deserialize(JSONObject from, DefaultFile to) throws JSONException, OXException {
                String aperture = from.getString(getAjaxName());
                if(aperture.startsWith("f/")) {
                    //Remove f-number if present
                    aperture = aperture.substring(2);
                }
                this.set(to, from.isNull(getAjaxName()) ? null : Double.valueOf(aperture));
            }

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Double value) throws OXException {
                if(value != null) {
                    object.setCameraAperture(d(value));
                }
            }

            @Override
            public Double get(DefaultFile object) {
                return object.getCameraAperture();
            }

            @Override
            public void remove(DefaultFile object) {
                //no-op
            }
        });

        mappings.put(File.Field.CAMERA_EXPOSURE_TIME, new DoubleMapping<DefaultFile>(File.Field.CAMERA_EXPOSURE_TIME.getName(), I(File.Field.CAMERA_EXPOSURE_TIME.getNumber())) {

            @Override
            public void deserialize(JSONObject from, DefaultFile to) throws JSONException, OXException {
                String exposureTime = from.getString(getAjaxName());
                exposureTime = exposureTime.replaceAll("\\D*$", "");
                this.set(to, from.isNull(getAjaxName()) ? null : Double.valueOf(exposureTime));
            }

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Double value) throws OXException {
                if(value != null) {
                    object.setCameraExposureTime(d(value));
                }
            }

            @Override
            public Double get(DefaultFile object) {
                return object.getCameraExposureTime();
            }

            @Override
            public void remove(DefaultFile object) {
                //no-op
            }
        });

        mappings.put(File.Field.CAMERA_FOCAL_LENGTH, new DoubleMapping<DefaultFile>(File.Field.CAMERA_FOCAL_LENGTH.getName(), I(File.Field.CAMERA_FOCAL_LENGTH.getNumber())) {

            @Override
            public void deserialize(JSONObject from, DefaultFile to) throws JSONException, OXException {
                String focalLength = from.getString(getAjaxName());
                focalLength = focalLength.replaceAll("\\D*$", "");
                this.set(to, from.isNull(getAjaxName()) ? null : Double.valueOf(focalLength));
            }

            @Override
            public boolean isSet(DefaultFile object) {
                return true;
            }

            @Override
            public void set(DefaultFile object, Double value) throws OXException {
                if(value != null) {
                    object.setCameraFocalLength(d(value));
                }
            }

            @Override
            public Double get(DefaultFile object) {
                return object.getCameraFocalLength();
            }

            @Override
            public void remove(DefaultFile object) {
                //no-op
            }
        });

        mappings.put(File.Field.MEDIA_META, new MapMapping<DefaultFile>(File.Field.MEDIA_META.getName(), I(File.Field.MEDIA_META.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getMediaMeta() != null;
            }

            @Override
            public void set(DefaultFile object, Map<String, Object> value) throws OXException {
                object.setMediaMeta(value);
            }

            @Override
            public Map<String, Object> get(DefaultFile object) {
                return object.getMediaMeta();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setMediaMeta(null);
            }
        });

        mappings.put(File.Field.MEDIA_STATUS, new MediaStatusMapping<DefaultFile>(File.Field.MEDIA_STATUS.getName(), I(File.Field.MEDIA_STATUS.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getMediaStatus() != null;
            }

            @Override
            public void set(DefaultFile object, MediaStatus value) throws OXException {
                object.setMediaStatus(value);
            }

            @Override
            public MediaStatus get(DefaultFile object) {
                return object.getMediaStatus();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setMediaStatus(null);
            }
        });

        mappings.put(File.Field.MEDIA_DATE, new DateMapping<DefaultFile>(File.Field.MEDIA_DATE.getName(), I(File.Field.MEDIA_DATE.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getCaptureDate() != null;
            }

            @Override
            public void set(DefaultFile object, Date value) throws OXException {
                object.setCaptureDate(value);
            }

            @Override
            public Date get(DefaultFile object) {
                return object.getCaptureDate();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setCaptureDate(null);
            }
        });

        mappings.put(File.Field.CREATED_FROM, new EntityInfoMapping<DefaultFile>(File.Field.CREATED_FROM.getName(), I(File.Field.CREATED_FROM.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return null != object.getCreatedFrom();
            }

            @Override
            public void set(DefaultFile object, EntityInfo value) throws OXException {
                object.setCreatedFrom(value);
            }

            @Override
            public EntityInfo get(DefaultFile object) {
                return object.getCreatedFrom();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setCreatedFrom(null);
            }
        });

        mappings.put(File.Field.MODIFIED_FROM, new EntityInfoMapping<DefaultFile>(File.Field.MODIFIED_FROM.getName(), I(File.Field.MODIFIED_FROM.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return null != object.getModifiedFrom();
            }

            @Override
            public void set(DefaultFile object, EntityInfo value) throws OXException {
                object.setModifiedFrom(value);
            }

            @Override
            public EntityInfo get(DefaultFile object) {
                return object.getModifiedFrom();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setModifiedFrom(null);
            }
        });

        mappings.put(File.Field.UNIQUE_ID, new StringMapping<DefaultFile>(File.Field.UNIQUE_ID.getName(), I(File.Field.UNIQUE_ID.getNumber())) {

            @Override
            public boolean isSet(DefaultFile object) {
                return object.getUniqueId() != null;
            }

            @Override
            public void set(DefaultFile object, String value) throws OXException {
                object.setUniqueId(value);
            }

            @Override
            public String get(DefaultFile object) {
                return object.getUniqueId();
            }

            @Override
            public void remove(DefaultFile object) {
                object.setUniqueId(null);
            }
        });

        return mappings;
    }
}
