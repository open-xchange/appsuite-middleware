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

package com.openexchange.groupware.infostore.utils;

import java.util.Date;
import com.openexchange.groupware.infostore.DocumentMetadata;

public class GetSwitch implements MetadataSwitcher {

	private final DocumentMetadata metadata;

	public GetSwitch(final DocumentMetadata metadata){
		this.metadata = metadata;
	}

	@Override
	public Object meta() {
	    return metadata.getMeta();
	}

	@Override
    public Object lastModified() {
		return metadata.getLastModified();
	}

	@Override
    public Object creationDate() {
		return metadata.getCreationDate();
	}

	@Override
    public Object modifiedBy() {
		return Integer.valueOf(metadata.getModifiedBy());
	}

	@Override
    public Object folderId() {
		return Long.valueOf(metadata.getFolderId());
	}

	@Override
    public Object title() {
		return metadata.getTitle();
	}

	@Override
    public Object version() {
		return Integer.valueOf(metadata.getVersion());
	}

	@Override
    public Object content() {
		return metadata.getContent();
	}

	@Override
    public Object id() {
		return Integer.valueOf(metadata.getId());
	}

	@Override
    public Object fileSize() {
		return Long.valueOf(metadata.getFileSize());
	}

	@Override
    public Object description() {
		return metadata.getDescription();
	}

	@Override
    public Object url() {
		return metadata.getURL();
	}

	@Override
    public Object createdBy() {
		return Integer.valueOf(metadata.getCreatedBy());
	}

	@Override
    public Object fileName() {
		return metadata.getFileName();
	}

	@Override
    public Object fileMIMEType() {
		return metadata.getFileMIMEType();
	}

	@Override
    public Object sequenceNumber() {
		return Long.valueOf(metadata.getSequenceNumber());
	}

	@Override
    public Object categories() {
		return metadata.getCategories();
	}

	@Override
    public Object lockedUntil() {
		return metadata.getLockedUntil();
	}

	@Override
    public Object fileMD5Sum() {
		return metadata.getFileMD5Sum();
	}

	@Override
    public Object versionComment() {
		return metadata.getVersionComment();
	}

	@Override
    public Object currentVersion() {
		return Boolean.valueOf(metadata.isCurrentVersion());
	}

	@Override
    public Object colorLabel() {
		return Integer.valueOf(metadata.getColorLabel());
	}

	@Override
    public Object filestoreLocation() {
		return metadata.getFilestoreLocation();
	}

    @Override
    public Object lastModifiedUTC() {
        return metadata.getLastModified();
    }

    @Override
    public Object numberOfVersions() {
        return Integer.valueOf(metadata.getNumberOfVersions());
    }

    @Override
    public Object objectPermissions() {
        return metadata.getObjectPermissions();
    }

    @Override
    public Object shareable() {
        return Boolean.valueOf(metadata.isShareable());
    }

    @Override
    public Object origin() {
        return metadata.getOriginFolderPath();
    }

    @Override
    public Object captureDate() {
        return metadata.getCaptureDate();
    }

    @Override
    public Object geolocation() {
        return metadata.getGeoLocation();
    }

    @Override
    public Object width() {
        return metadata.getWidth();
    }

    @Override
    public Object height() {
        return metadata.getHeight();
    }

    @Override
    public Object cameraMake() {
        return metadata.getCameraMake();
    }

    @Override
    public Object cameraModel() {
        return metadata.getCameraModel();
    }

    @Override
    public Object cameraIsoSpeed() {
        return metadata.getCameraIsoSpeed();
    }

    @Override
    public Object cameraAperture() {
        return metadata.getCameraAperture();
    }

    @Override
    public Object cameraExposureTime() {
        return metadata.getCameraExposureTime();
    }

    @Override
    public Object cameraFocalLength() {
        return metadata.getCameraFocalLength();
    }

    @Override
    public Object mediaMeta() {
        return metadata.getMediaMeta();
    }

    @Override
    public Object mediaStatus() {
        return metadata.getMediaStatus();
    }

    @Override
    public Object mediaDate() {
        Date d = metadata.getCaptureDate();
        return null == d ? metadata.getLastModified() : d;
    }

}
