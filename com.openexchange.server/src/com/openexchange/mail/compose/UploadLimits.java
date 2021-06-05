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

package com.openexchange.mail.compose;

import org.slf4j.LoggerFactory;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.utils.InfostoreConfigUtils;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link UploadLimits}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.5
 */
public class UploadLimits {

    private final long perAttachmentLimit;
    private final long perRequestLimit;

    /**
     * Initializes a new {@link UploadLimits}.
     *
     * @param perAttachmentLimit The max. size a single uploaded attachment must not exceed
     * @param perRequestLimit The max. size all single uploaded attachments in one request must not exceed
     */
    public UploadLimits(long perAttachmentLimit, long perRequestLimit) {
        super();
        this.perAttachmentLimit = perAttachmentLimit;
        this.perRequestLimit = perRequestLimit;
    }

    /**
     * Gets the max. size a single uploaded attachment must not exceed.
     *
     * @return The limit or {@code -1} if unlimited
     */
    public long getPerAttachmentLimit() {
        return perAttachmentLimit;
    }

    /**
     * Gets the max. size all single uploaded attachments in one request must not exceed.
     *
     * @return The limit or {@code -1} if unlimited
     */
    public long getPerRequestLimit() {
        return perRequestLimit;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Upload target type
     */
    public static enum Type {
        MAIL,
        DRIVE
    }

    /**
     * Gets a proper initialized {@link UploadLimits} instance for given session and upload target type.
     *
     * @param type The type
     * @param session The session
     * @return The {@link UploadLimits}
     * @throws OXException If reading configuration fails
     */
    public static UploadLimits get(Type type, Session session) throws OXException {
        if (type == null) {
            throw OXException.general("Missing type");
        }
        switch (type) {
            case DRIVE:
                return driveUploadLimits();
            case MAIL:
                return mailUploadLimitations(session);
        }
        throw OXException.general("Unknown type: " + type);
    }

    private static UploadLimits driveUploadLimits() {
        long maxUploadSize = InfostoreConfigUtils.determineRelevantUploadSize();
        if (maxUploadSize <= 0) {
            maxUploadSize = -1L;
        }

        return new UploadLimits(-1, maxUploadSize);
    }

    private static UploadLimits mailUploadLimitations(Session session) throws OXException {
        long maxSize;
        long maxFileSize;

        UserSettingMail usm = ServerSessionAdapter.valueOf(session).getUserSettingMail();
        maxFileSize = usm.getUploadQuotaPerFile();
        if (maxFileSize <= 0) {
            maxFileSize = -1L;
        }

        maxSize = usm.getUploadQuota();
        if (maxSize <= 0) {
            if (maxSize == 0) {
                maxSize = -1L;
            } else {
                LoggerFactory.getLogger(UploadLimits.class).debug("Upload quota is less than zero. Using global server property \"MAX_UPLOAD_SIZE\" instead.");
                long globalQuota;
                try {
                    globalQuota = ServerConfig.getLong(Property.MAX_UPLOAD_SIZE).longValue();
                } catch (OXException e) {
                    LoggerFactory.getLogger(UploadLimits.class).error("", e);
                    globalQuota = 0L;
                }
                maxSize = globalQuota <= 0 ? -1L : globalQuota;
            }
        }

        return new UploadLimits(maxFileSize, maxSize);
    }

}
