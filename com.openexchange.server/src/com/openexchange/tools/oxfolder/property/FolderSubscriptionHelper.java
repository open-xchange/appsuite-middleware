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

package com.openexchange.tools.oxfolder.property;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UsedForSync;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;

/**
 * The {@link FolderSubscriptionHelper} is able to check and set subscription and usedForSync status of pim folders.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class FolderSubscriptionHelper {

    private static final String SUBSCRIBED_KEY = "subscribed";
    private static final String SYNC_KEY = "usedForSync";

    /**
     * A map of supported modules mapped to their property prefix names
     */
    private static final Map<Integer, String> SUBSCRIPTION_AWARE_MODULES = ImmutableMap.<Integer, String> builder()
        .put(I(TaskContentType.getInstance().getModule()), "tsk/")
        .put(I(CalendarContentType.getInstance().getModule()), "cal/")
        .put(I(ContactContentType.getInstance().getModule()), "con/")
    .build();
        
    private final FolderUserPropertyStorage storage;

    /**
     * Initializes a new {@link FolderSubscriptionHelper}.
     *
     * @param storage The underlying {@link FolderUserPropertyStorage}
     */
    public FolderSubscriptionHelper(FolderUserPropertyStorage storage) {
        super();
        this.storage = storage;
    }

    /**
     * Checks if the folder has any subscriptions
     *
     * @param optCon The optional {@link Connection} to use
     * @param ctxId The context id
     * @param userId The user id
     * @param folderId The folder id
     * @param module The module id
     * @return The optional subscription value
     * @throws OXException In case the subscription couldn't be loaded
     */
    public Optional<Boolean> isSubscribed(Optional<Connection> optCon, int ctxId, int userId, int folderId, int module) throws OXException {
        String result;
        if(optCon.isPresent()) {
            result = storage.getFolderProperty(ctxId, folderId, userId, getSubscribeKey(module), optCon.get());
        } else {
            result = storage.getFolderProperty(ctxId, folderId, userId, getSubscribeKey(module));
        }
        return Optional.ofNullable(result == null ? null : Boolean.valueOf(result));
    }

    /**
     * Gets the subscribed key for the given content type
     *
     * @param module the module number
     * @return The key
     */
    private String getSubscribeKey(int module) {
        String prefix = SUBSCRIPTION_AWARE_MODULES.get(I(module));
        if(prefix != null) {
            return prefix + SUBSCRIBED_KEY;
        }
        return SUBSCRIBED_KEY;
    }

    /**
     * Gets the subscribed key for the given content type
     *
     * @param int The module number
     * @return The key
     */
    private String getUsedForSyncKey(int module) {
        String prefix = SUBSCRIPTION_AWARE_MODULES.get(I(module));
        if(prefix != null) {
            return prefix + SYNC_KEY;
        }
        return SYNC_KEY;
    }

    /**
     * Checks if the folder has any subscriptions
     *
     * @param optCon The optional {@link Connection} to use
     * @param ctxId The context id
     * @param userId The user id
     * @param folderId The folder id
     * @param folderModule The folder module
     * @return The optional usedForSync value
     * @throws OXException In case the subscription couldn't be loaded
     */
    public Optional<Boolean> isUsedForSync(Optional<Connection> optCon, int ctxId, int userId, int folderId, int folderModule) throws OXException {
        String result;
        if(optCon.isPresent()) {
            result = storage.getFolderProperty(ctxId, folderId, userId, getUsedForSyncKey(folderModule), optCon.get());
        } else {
            result = storage.getFolderProperty(ctxId, folderId, userId, getUsedForSyncKey(folderModule));
        }
        return Optional.ofNullable(result == null ? null : Boolean.valueOf(result));
    }

    /**
     * Inserts, updates or removes the usedForSync property for the given user and folder
     *
     * @param optCon The optional {@link Connection} to use
     * @param contextId The context id
     * @param userId The user id
     * @param folderId The folder id
     * @param module The module id
     * @param usedForSync Whether the folder is used for sync or not
     * @throws OXException In case the subscription couldn't be set
     */
    public void setUsedForSync(Optional<Connection> optCon, int contextId, int userId, int folderId, int module, UsedForSync usedForSync) throws OXException {
        Map<String, String> data = Collections.singletonMap(getUsedForSyncKey(module), Boolean.valueOf(usedForSync.isUsedForSync()).toString());
        if (optCon.isPresent()) {
            storage.setFolderProperties(contextId, folderId, userId, data, optCon.get());
        } else {
            storage.setFolderProperties(contextId, folderId, userId, data);
        }
    }

    /**
     * Inserts, updates or removes the subscription property for the given user and folder
     *
     * @param optCon The optional {@link Connection} to use
     * @param contextId The context id
     * @param userId The user id
     * @param folderId The folder id
     * @param module The module id
     * @param subscribed Whether the folder is subscribed or not
     * @throws OXException In case the subscription couldn't be set
     */
    public void setSubscribed(Optional<Connection> optCon, int contextId, int userId, int folderId, int module, boolean subscribed) throws OXException {
        Map<String, String> data = Collections.singletonMap(getSubscribeKey(module), Boolean.valueOf(subscribed).toString());
        if (optCon.isPresent()) {
            storage.setFolderProperties(contextId, folderId, userId, data, optCon.get());
        } else {
            storage.setFolderProperties(contextId, folderId, userId, data);
        }
    }

    /**
     * Checks if the given module is subscription aware
     *
     * @param module The module id
     * @return <code>true</code> if the module is subscription aware, <code>false</code> otherwise
     */
    public static boolean isSubscribableModule(final int module) {
        return SUBSCRIPTION_AWARE_MODULES.containsKey(I(module));
    }

}
