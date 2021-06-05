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

package com.openexchange.drive.impl.actions;

import java.util.List;
import java.util.Map.Entry;
import com.openexchange.drive.Action;
import com.openexchange.drive.DriveFileField;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;

/**
 * {@link AbstractFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractFileAction extends AbstractAction<FileVersion> {

    /**
     * Initializes a new {@link AbstractFileAction}.
     *
     * @param version
     * @param newVersion
     */
    protected AbstractFileAction(FileVersion version, FileVersion newVersion, ThreeWayComparison<FileVersion> comparison) {
        super(version, newVersion, comparison);
    }

    /**
     * Applies file metadata to this action's parameter collection, based on the requested additional file metadata fields found
     * configured in the session.
     *
     * @param file The file generate the metadata for
     * @param session The session
     */
    protected void applyMetadataParameters(File file, SyncSession session) {
        if (null != file) {
            /*
             * add default metadata
             */
            parameters.put(PARAMETER_TOTAL_LENGTH, Long.valueOf(file.getFileSize()));
            if (null != file.getCreated()) {
                parameters.put(PARAMETER_CREATED, Long.valueOf(file.getCreated().getTime()));
            }
            if (null != file.getLastModified()) {
                parameters.put(PARAMETER_MODIFIED, Long.valueOf(file.getLastModified().getTime()));
            }
            /*
             * add additional metadata
             */
            if (false == session.getDriveSession().useDriveMeta() && null != session.getFields()) {
                List<DriveFileField> fields = session.getFields();
                if (fields.contains(DriveFileField.CONTENT_TYPE)) {
                    String contentType = file.getFileMIMEType();
                    if (null != contentType) {
                        parameters.put(PARAMETER_CONTENT_TYPE, contentType);
                    }
                }
                if (fields.contains(DriveFileField.DIRECT_LINK)) {
                    String directLink = session.getLinkGenerator().getFileLink(file);
                    if (null != directLink) {
                        parameters.put(PARAMETER_DIRECT_LINK, directLink);
                    }
                }
                if (fields.contains(DriveFileField.DIRECT_LINK_FRAGMENTS)) {
                    String directLinkFragments = session.getLinkGenerator().getFileLinkFragments(file);
                    if (null != directLinkFragments) {
                        parameters.put(PARAMETER_DIRECT_LINK_FRAGMENTS, directLinkFragments);
                    }
                }
                if (fields.contains(DriveFileField.THUMBNAIL_LINK)) {
                    String thumbnailLink = session.getLinkGenerator().getFileThumbnailLink(file);
                    if (null != thumbnailLink) {
                        parameters.put(PARAMETER_THUMBNAIL_LINK, thumbnailLink);
                    }
                }
                if (fields.contains(DriveFileField.PREVIEW_LINK)) {
                    String previewLink = session.getLinkGenerator().getFilePreviewLink(file);
                    if (null != previewLink) {
                        parameters.put(PARAMETER_PREVIEW_LINK, previewLink);
                    }
                }
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + FileVersion.class.hashCode();
        Action action = getAction();
        result = prime * result + ((null == action) ? 0 : action.hashCode());
        if (null != version) {
            result = prime * result + version.getChecksum().hashCode();
            result = prime * result + version.getName().hashCode();
        }
        if (null != newVersion) {
            result = prime * result + newVersion.getChecksum().hashCode();
            result = prime * result + newVersion.getName().hashCode();
        }
        if (null != parameters) {
            for (Entry<String, Object> parameter : parameters.entrySet()) {
                String key = parameter.getKey();
                Object value = parameter.getValue();
                if (PARAMETER_ERROR.equals(key) && null != value && OXException.class.isInstance(value)) {
                    result = prime * result + (null == key ? 0 : key.hashCode()) ^ (((OXException) value).getErrorCode().hashCode());
                } else {
                    result = prime * result + (null == key ? 0 : key.hashCode()) ^ (null == value ? 0 : value.hashCode());
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractFileAction)) {
            return false;
        }
        AbstractFileAction other = (AbstractFileAction) obj;
        if (newVersion == null) {
            if (other.newVersion != null) {
                return false;
            }
        } else if (!newVersion.equals(other.newVersion)) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (!parameters.equals(other.parameters)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}

