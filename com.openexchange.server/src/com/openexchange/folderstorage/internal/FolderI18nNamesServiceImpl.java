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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.folderstorage.FolderI18nNamesService;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.i18n.I18nService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link FolderI18nNamesServiceImpl} - Provides the localized folder names for specified folder modules.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderI18nNamesServiceImpl implements FolderI18nNamesService {

    private static final FolderI18nNamesServiceImpl INSTANCE = new FolderI18nNamesServiceImpl();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static FolderI18nNamesServiceImpl getInstance() {
        return INSTANCE;
    }

    private final Map<String, String> i18nNames;

    private final Map<Locale, I18nService> services;

    private final Set<String> identifiers;

    /**
     * Initializes a new {@link FolderI18nNamesServiceImpl}.
     */
    private FolderI18nNamesServiceImpl() {
        super();
        services = new ConcurrentHashMap<Locale, I18nService>();
        identifiers = new HashSet<String>(32);
        i18nNames = new ConcurrentHashMap<String, String>(512);
        initIdentifiers();
    }

    private void initIdentifiers() {
        // Mail strings
        identifiers.add(MailStrings.INBOX);
        identifiers.add(MailStrings.SPAM);
        identifiers.add(MailStrings.DRAFTS);
        identifiers.add(MailStrings.TRASH);
        identifiers.add(MailStrings.SENT);
        identifiers.add(MailStrings.SENT_ALT);
        identifiers.add(MailStrings.CONFIRMED_HAM);
        identifiers.add(MailStrings.CONFIRMED_HAM_ALT);
        identifiers.add(MailStrings.CONFIRMED_SPAM);
        identifiers.add(MailStrings.CONFIRMED_SPAM_ALT);
        // Folder strings
        identifiers.add(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME);
        identifiers.add(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME);
        identifiers.add(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME);
        identifiers.add(FolderStrings.DEFAULT_TASK_FOLDER_NAME);
        identifiers.add(FolderStrings.DEFAULT_EMAIL_ATTACHMENTS_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_GLOBAL_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_INFOSTORE_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_LDAP_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_OX_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_OX_PROJECT_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_PRIVATE_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_PUBLIC_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_SHARED_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_USER_INFOSTORE_FOLDER_NAME);
        identifiers.add(FolderStrings.VIRTUAL_LIST_CALENDAR_FOLDER_NAME);
        identifiers.add(FolderStrings.VIRTUAL_LIST_CONTACT_FOLDER_NAME);
        identifiers.add(FolderStrings.VIRTUAL_LIST_INFOSTORE_FOLDER_NAME);
        identifiers.add(FolderStrings.VIRTUAL_LIST_TASK_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_FILES_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_USER_FILES_FOLDER_NAME);
        identifiers.add(FolderStrings.SYSTEM_PUBLIC_FILES_FOLDER_NAME);
        identifiers.add(FolderStrings.VIRTUAL_LIST_FILES_FOLDER_NAME);
    }

    /**
     * Adds specified service.
     *
     * @param service The service to add
     */
    public void addService(final I18nService service) {
        services.put(service.getLocale(), service);
        i18nNames.clear();
    }

    /**
     * Removes specified service.
     *
     * @param service The service to remove
     */
    public void removeService(final I18nService service) {
        services.remove(service.getLocale());
        i18nNames.clear();
    }

    @Override
    public Set<String> getI18nNamesFor(final int... modules) {
        // TODO: Generate names with respect to specified modules
        if (i18nNames.isEmpty()) {
            synchronized (services) {
                if (i18nNames.isEmpty()) {
                    // Insert as-is for default locale
                    for (final String identifier : identifiers) {
                        i18nNames.put(identifier, identifier);
                    }
                    // Insert translated names
                    for (final I18nService service : services.values()) {
                        for (final String identifier : identifiers) {
                            final String translated = service.getLocalized(identifier);
                            i18nNames.put(translated, translated);
                        }
                    }
                    // Insert user-defined names considered as reserved folder identifiers
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null != service) {
                        handleText(service.getText("folder-reserved-names"));
                    }
                }
            }
        }
        return Collections.unmodifiableSet(i18nNames.keySet());
    }

    private void handleText(final String text) {
        if (null == text) {
            return;
        }
        for (final String line : text.split("\r?\n")) {
            if (!isEmpty(line) && '#' != line.charAt(0)) {
                for (final String value : line.split(" *, *")) {
                    processValue(value);
                }
            }
        }
    }

    private void processValue(final String value) {
        if (isEmpty(value)) {
            return;
        }
        String val = value.trim();
        if ('#' == val.charAt(0)) { // Comment line
            return;
        }
        final int mlen = val.length() - 1;
        if ('"' == val.charAt(0) && '"' == val.charAt(mlen)) {
            if (0 == mlen) {
                return;
            }
            val = val.substring(1, mlen);
            if (isEmpty(val)) {
                return;
            }
        }
        i18nNames.put(val, val);
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
