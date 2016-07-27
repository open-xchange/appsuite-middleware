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

package com.openexchange.file.storage.mail;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
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
}