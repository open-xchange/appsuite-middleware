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

package com.openexchange.groupware.upload.quotachecker;

import org.apache.commons.lang.Validate;
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

        Validate.notNull(settings, "UserSettingMail cannot be null!");

        long quota = settings.getUploadQuota();
        if (quota >= 0) {
            uploadQuota = quota;
        } else {
            LOG.debug("Upload quota is less than zero. Using global server property \"MAX_UPLOAD_SIZE\" instead.");
            Long globalQuota;
            try {
                globalQuota = ServerConfig.getLong(Property.MAX_UPLOAD_SIZE);
            } catch (final OXException e) {
                LOG.error("", e);
                globalQuota = 0L;
            }
            uploadQuota = globalQuota;
        }

        long quotaPerFile = settings.getUploadQuotaPerFile();
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