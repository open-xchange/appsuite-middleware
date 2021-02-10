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
 *    trademarks of the OX Software GmbH. group of companies.
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
