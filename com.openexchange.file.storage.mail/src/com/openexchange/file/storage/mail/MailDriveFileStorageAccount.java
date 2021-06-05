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

package com.openexchange.file.storage.mail;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.session.Session;

/**
 * {@link MailDriveFileStorageAccount} - The special account for Mail Drive.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveFileStorageAccount implements FileStorageAccount {

    private static final long serialVersionUID = 8675168697186715107L;

    private static final String ACCOUNT_ID = MailDriveConstants.ACCOUNT_ID;
    private static final String ACCOUNT_DISPLAY_NAME = MailDriveStrings.NAME_ATTACHMENTS_ALL;

    // --------------------------------------------------------------------------------------------------------------

    private final MailDriveFileStorageService service;
    private final Locale locale;

    /**
     * Initializes a new {@link FileStorageAccountImplementation}.
     */
    MailDriveFileStorageAccount(MailDriveFileStorageService service, Session session) {
        super();
        this.service = service;
        locale = getLocaleFor(session);
    }

    private Locale getLocaleFor(Session session) {
        try {
            return MailDriveFolder.getSessionUserLocale(session);
        } catch (OXException e) {
            Logger logger = org.slf4j.LoggerFactory.getLogger(MailDriveFileStorageAccount.class);
            logger.error("Error getting locale for session. Using en_US as fall-back", e);
            return Locale.US;
        }
    }

    @Override
    public String getId() {
        return ACCOUNT_ID;
    }

    @Override
    public FileStorageService getFileStorageService() {
        return service;
    }

    @Override
    public String getDisplayName() {
        return StringHelper.valueOf(locale).getString(ACCOUNT_DISPLAY_NAME);
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return Collections.emptyMap();
    }

    @Override
    public JSONObject getMetadata() {
        return new JSONObject();
    }
}