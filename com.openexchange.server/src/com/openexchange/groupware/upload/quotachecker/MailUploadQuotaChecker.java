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

package com.openexchange.groupware.upload.quotachecker;

import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.upload.impl.UploadQuotaChecker;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;

/**
 * {@link MailUploadQuotaChecker} - Quota checker for mail module. <br/>
 * <br/>
 * Checks the value set for com.openexchange.mail.usersetting.UserSettingMail.uploadQuota and
 * com.openexchange.mail.usersetting.UserSettingMail.uploadQuotaPerFile (based on DB entries) and set the value that will be used for mail
 * attachments file size. In some cases a fallback to server.properties file is used.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailUploadQuotaChecker extends UploadQuotaChecker {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailUploadQuotaChecker.class);

    private final long uploadQuota;

    private final long uploadQuotaPerFile;

    /**
     * Initializes a new {@link MailUploadQuotaChecker}.
     *
     * @param usm The settings
     */
    public MailUploadQuotaChecker(final UserSettingMail settings) {
        super();

        long quota = null == settings ? -1 : settings.getUploadQuota();
        if (quota >= 0) {
            uploadQuota = 0 == quota ? -1 : quota;
        } else {
            LOG.debug("Upload quota is less than zero. Using global server property \"MAX_UPLOAD_SIZE\" instead.");
            Long globalQuota;
            try {
                globalQuota = ServerConfig.getLong(Property.MAX_UPLOAD_SIZE);
            } catch (OXException e) {
                LOG.error("", e);
                globalQuota = Long.valueOf(0);
            }
            uploadQuota = globalQuota.longValue();
        }

        long quotaPerFile = null == settings ? -1 : settings.getUploadQuotaPerFile();
        uploadQuotaPerFile = quotaPerFile > 0 ? quotaPerFile : -1;
    }

    /**
     * Initializes a new {@link MailUploadQuotaChecker}.
     *
     * @param session The session
     * @param ctx The context
     */
    public MailUploadQuotaChecker(final Session session, final Context ctx) {
        this(UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx));
    }

    @Override
    public long getFileQuotaMax() {
        return uploadQuotaPerFile;
    }

    @Override
    public long getQuotaMax() {
        return uploadQuota;
    }
}
