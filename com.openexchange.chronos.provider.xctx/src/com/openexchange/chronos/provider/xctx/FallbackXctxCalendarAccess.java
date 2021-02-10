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

package com.openexchange.chronos.provider.xctx;

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.DefaultCalendarPermission;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.groupware.DefaultGroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.FallbackGroupwareCalendarAccess;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link FallbackXctxCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class FallbackXctxCalendarAccess extends FallbackGroupwareCalendarAccess {

    private final ServiceLookup services;
    private final OXException error;
    private final Session localSession;
    private final CalendarParameters parameters;

    /**
     * @param services A service lookup reference
     * @param account The underlying calendar account
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param parameters Additional calendar parameters
     * @param error The error to include in the folders
     */
    public FallbackXctxCalendarAccess(ServiceLookup services, CalendarAccount account, Session localSession, CalendarParameters parameters, OXException error) {
        super(account);
        this.services = services;
        this.error = error;
        this.parameters = parameters;
        this.localSession = localSession;
    }

    @Override
    public List<GroupwareCalendarFolder> getVisibleFolders(GroupwareFolderType type) throws OXException {
        List<GroupwareCalendarFolder> calendarFolders = new ArrayList<GroupwareCalendarFolder>();
        for (DefaultGroupwareCalendarFolder calendarFolder : getConfigHelper().getRememberedCalendars()) {
            if (type.equals(calendarFolder.getType())) {
                calendarFolders.add(asFallback(calendarFolder));
            }
        }
        return calendarFolders;
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        if (null != folder.getName() || null != folder.getPermissions()) {
            throw unsupportedOperation();
        }
        /*
         * update folder's configuration in underlying account's internal config
         */
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
        boolean updated = false;
        if (null != folder.isSubscribed()) {
            updated = updated || new AccountConfigHelper(internalConfig).setSubscribed(folderId, folder.isSubscribed());
        }
        if (null != folder.getExtendedProperties()) {
            updated = updated || new AccountConfigHelper(internalConfig).setColor(folderId, folder.getExtendedProperties().get(COLOR_LITERAL));
        }
        if (updated) {
            JSONObject userConfig = null != account.getUserConfiguration() ? account.getUserConfiguration() : new JSONObject();
            userConfig.putSafe("internalConfig", internalConfig);
            services.getService(CalendarAccountService.class).updateAccount(localSession, account.getAccountId(), userConfig, clientTimestamp, parameters);
        }
        return folderId;
    }

    @Override
    public CalendarFolder getFolder(String folderId) throws OXException {
        DefaultGroupwareCalendarFolder calendarFolder = getConfigHelper().getRememberedCalendar(folderId);
        if (null == calendarFolder) {
            throw CalendarExceptionCodes.FOLDER_NOT_FOUND.create(folderId);
        }
        return asFallback(calendarFolder);
    }

    private DefaultGroupwareCalendarFolder asFallback(DefaultGroupwareCalendarFolder rememberedFolder) {
        /*
         * insert a system permission for the user to ensure folder is considered as visible for the local session user throughout the stack
         */
        CalendarPermission ownSystemPermission = new DefaultCalendarPermission(String.valueOf(account.getUserId()), account.getUserId(), null,
            CalendarPermission.READ_FOLDER, CalendarPermission.READ_OWN_OBJECTS, CalendarPermission.NO_PERMISSIONS, CalendarPermission.NO_PERMISSIONS,
            false, false, Permissions.createPermissionBits(CalendarPermission.READ_FOLDER, CalendarPermission.READ_OWN_OBJECTS, CalendarPermission.NO_PERMISSIONS, CalendarPermission.NO_PERMISSIONS, false));
        rememberedFolder.setPermissions(Collections.singletonList(ownSystemPermission));
        /*
         * indicate the error within the folder
         */
        rememberedFolder.setAccountError(error);
        return rememberedFolder;
    }

    private AccountConfigHelper getConfigHelper() {
        JSONObject internalConfig = null != account.getInternalConfiguration() ? new JSONObject(account.getInternalConfiguration()) : new JSONObject();
        return new AccountConfigHelper(internalConfig);
    }

}
