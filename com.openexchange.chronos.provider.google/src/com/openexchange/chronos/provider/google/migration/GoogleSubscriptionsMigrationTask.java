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

package com.openexchange.chronos.provider.google.migration;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;
import com.openexchange.chronos.provider.google.GoogleCalendarProvider;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.WorkingLevel;
import com.openexchange.oauth.OAuthAccountStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.AdministrativeSubscriptionStorage;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionStorage;
import com.openexchange.tools.oxfolder.property.FolderSubscriptionHelper;
import com.openexchange.user.UserService;

/**
 * {@link GoogleSubscriptionsMigrationTask}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleSubscriptionsMigrationTask extends UpdateTaskAdapter {

    private final static Logger LOG = LoggerFactory.getLogger(GoogleSubscriptionsMigrationTask.class);
    private static final String SOURCE_ID = "com.openexchange.subscribe.google.calendar";

    @Override
    public void perform(PerformParameters params) throws OXException {

        GenericConfigurationStorageService storageService = Services.getService(GenericConfigurationStorageService.class);
        if (storageService == null) {
            throw ServiceExceptionCode.absentService(GenericConfigurationStorageService.class);
        }

        SubscriptionStorage subscriptionStorage = AbstractSubscribeService.STORAGE.get();
        if (subscriptionStorage == null || !(subscriptionStorage instanceof AdministrativeSubscriptionStorage)) {
            throw ServiceExceptionCode.absentService(AdministrativeSubscriptionStorage.class);
        }

        ContextService contextService = Services.getService(ContextService.class);
        if (contextService == null) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }

        OAuthAccountStorage oauthAccountStorage = Services.getService(OAuthAccountStorage.class);
        if (oauthAccountStorage == null) {
            throw ServiceExceptionCode.absentService(OAuthAccountStorage.class);
        }

        Connection writeCon = params.getConnection();
        int[] contextsInSameSchema = params.getContextsInSameSchema();

        for (int ctxId : contextsInSameSchema) {
            Context ctx;
            try {
                ctx = contextService.loadContext(ctxId);
            } catch (OXException e) {
                if ("CTX-0001".equals(e.getErrorCode())) {
                    LOG.error("Unable to load context {}, skipping migration.", I(ctxId), e);
                    continue;
                }
                throw e;
            }
            /*
             * Step 1: Load subscriptions
             */
            final List<Subscription> subscriptions = ((AdministrativeSubscriptionStorage) subscriptionStorage).getSubscriptionsForContext(ctx, SOURCE_ID, writeCon);

            /*
             * Step 2: Create accounts for the existing subscriptions
             */
            Iterator<Subscription> iterator = subscriptions.iterator();
            CalendarStorage calendarStorage = Services.getService(CalendarStorageFactory.class).create(ctx, -1, null);
            while (iterator.hasNext()) {
                Subscription sub = iterator.next();
                boolean inserted = false;
                try {
                    Object oauthAccountId = sub.getConfiguration().get("account");
                    if (oauthAccountId == null) {
                        // Skip bad configured subscriptions
                        iterator.remove();
                        continue;
                    }

                    JSONObject internalConfig = new JSONObject();
                    internalConfig.put(GoogleCalendarConfigField.OAUTH_ID, oauthAccountId);
                    JSONObject userConfig = new JSONObject(internalConfig);
                    internalConfig.put(GoogleCalendarConfigField.OLD_FOLDER, sub.getFolderId());

                    internalConfig.put("name", "Google Calendar");

                    int id = calendarStorage.getAccountStorage().nextId();
                    calendarStorage.getAccountStorage().insertAccount(new DefaultCalendarAccount(GoogleCalendarProvider.PROVIDER_ID, id, sub.getUserId(), internalConfig, userConfig, new Date()));
                    inserted = true;
                    calendarStorage.getAccountStorage().invalidateAccount(sub.getUserId(), -1);
                } catch (JSONException e) {
                    LOG.error("Error during migration of google subscriptions. Subscription with id {} in context {} could not be migrated to a calendar account because of: {}", I(sub.getId()), I(ctxId), e.getMessage());
                    // remove the subscription so it does not get deleted
                    iterator.remove();
                } catch (OXException e) {
                    if (inserted) {
                        LOG.warn("Problem during migration of google subscriptions. Cache could not be invalidated for user with id {}", I(sub.getUserId()));
                    } else {
                        LOG.error("Error during migration of google subscriptions. Subscription with id {} in context {} could not be migrated to a calendar account because of: {}", I(sub.getId()), I(ctxId), e.getMessage());
                        // remove the subscription so it does not get deleted
                        iterator.remove();
                    }
                }
            }

            /*
             * Step 3: Remove old subscriptions and unsubscribe old folder
             */
            GroupService groupService = Services.getService(GroupService.class);
            UserService userService = Services.getService(UserService.class);
            FolderService folderService = Services.getService(FolderService.class);
            FolderSubscriptionHelper subscriptionHelper = Services.getService(FolderSubscriptionHelper.class);
            for (Subscription sub : subscriptions) {
                subscriptionStorage.forgetSubscription(sub);

                // Unsubscribe old folder for all users with permissions
                try {
                    UserizedFolder folder = folderService.getFolder("0", sub.getFolderId(), userService.getUser(sub.getUserId(), ctx), ctx, new FolderServiceDecorator());
                    Set<Integer> userFromPermissions = getUserFromPermissions(folder.getPermissions(), groupService, ctx);
                    int folderId = Integer.parseInt(sub.getFolderId(), 10);
                    for (Integer user : userFromPermissions) {
                        subscriptionHelper.setSubscribed(Optional.of(writeCon), ctxId, user.intValue(), folderId, folder.getContentType().getModule(), false);
                    }
                } catch (OXException e) {
                    if (FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.equals(e)) {
                        // Ignore not visible subscriptions
                        continue;
                    }
                    throw e;
                }

            }
        }
    }

    private Set<Integer> getUserFromPermissions(Permission[] permissions, GroupService groupService, Context ctx) {
        Set<Integer> result = new HashSet<Integer>(permissions.length);
        for (Permission perm : permissions) {
            if (perm.isGroup()) {
                try {
                    Group group = groupService.getGroup(ctx, perm.getEntity());
                    for (int member : group.getMember()) {
                        result.add(I(member));
                    }
                } catch (OXException e) {
                    // Continue
                    LOG.debug("{}", e.getMessage(), e);
                }
            } else {
                result.add(I(perm.getEntity()));
            }
        }
        return result;

    }

    @Override
    public String[] getDependencies() {
        return new String[] {
            "com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask",
            "com.openexchange.groupware.update.tasks.AddSharedParentFolderToFolderPermissionTableUpdateTask",
            "com.openexchange.groupware.update.tasks.AddTypeToFolderPermissionTableUpdateTask",
            "com.openexchange.groupware.update.tasks.AddOriginColumnToInfostoreDocumentTables",
            "com.openexchange.tools.oxfolder.property.sql.CreateFolderUserPropertyTask",
            "com.openexchange.groupware.update.tasks.AddUserSaltColumnTask"
        };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, WorkingLevel.SCHEMA);
    }

}
