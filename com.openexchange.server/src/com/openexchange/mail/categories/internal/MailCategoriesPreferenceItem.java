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

package com.openexchange.mail.categories.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.IValueHandlerExtended;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.user.User;

/**
 * {@link MailCategoriesPreferenceItem}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesPreferenceItem implements PreferencesItemService, ConfigTreeEquivalent {

    /** The service listing */
    final ServiceLookup lookupService;

    /**
     * Initializes a new {@link MailCategoriesPreferenceItem}.
     */
    public MailCategoriesPreferenceItem(ServiceLookup lookupService) {
        super();
        this.lookupService = lookupService;
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "categories" };
    }

    private static final String MAIL_CATEGORIES_CAPABILITY = "mail_categories";

    private static final String FIELD_LIST = "list";
    private static final String FIELD_FEATURE_ENABLED = "enabled";
    private static final String FIELD_FEATURE_FORCED = "forced";
    private static final String FIELD_FEATURE_INITIALIZED = "initialized";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_ACTIVE = "active";
    private static final String FIELD_PERMISSIONS = "permissions";

    private static final String PERMISSION_RENAME = "rename";
    private static final String PERMISSION_DISABLE = "disable";
    private static final String PERMISSION_TRAIN = "train";

    @Override
    public IValueHandler getSharedValue() {
        return new IValueHandlerExtended() {

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return userConfig.hasWebMail() && userConfig.getExtendedPermissions().contains(MAIL_CATEGORIES_CAPABILITY);
            }

            @Override
            public boolean isAvailable(Session session, UserConfiguration userConfig) throws OXException {
                if (false == userConfig.hasWebMail()) {
                    return false;
                }

                CapabilityService service = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
                return service.getCapabilities(session).contains(MAIL_CATEGORIES_CAPABILITY);
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                JSONObject item = new JSONObject(3);
                try {
                    MailCategoriesConfigService service = lookupService.getOptionalService(MailCategoriesConfigService.class);
                    if (service != null) {
                        boolean mailCategoriesEnabled = service.isEnabled(session);
                        boolean mailCategoriesForced = service.isForced(session);
                        if (mailCategoriesForced) {
                            mailCategoriesEnabled = true;
                        }
                        item.put(FIELD_FEATURE_ENABLED, mailCategoriesEnabled);
                        item.put(FIELD_FEATURE_FORCED, mailCategoriesForced);
                        item.put(FIELD_FEATURE_INITIALIZED, service.getInitStatus(session));
                        List<MailCategoryConfig> configs = service.getAllCategories(session, user.getLocale(), false, true);
                        JSONArray categories = new JSONArray();

                        for (MailCategoryConfig config : configs) {
                            JSONObject categoryJSON = new JSONObject(3);
                            categoryJSON.put(FIELD_ID, config.getCategory());
                            categoryJSON.put(FIELD_NAME, config.getName());
                            categoryJSON.put(FIELD_DESCRIPTION, config.getDescription());
                            categoryJSON.put(FIELD_ACTIVE, config.isActive());

                            List<String> mailCategoryPermissions = new ArrayList<>();
                            if (!config.isForced()) {
                                mailCategoryPermissions.add(PERMISSION_DISABLE);
                            }
                            if (!config.isSystemCategory()) {
                                mailCategoryPermissions.add(PERMISSION_RENAME);
                            }

                            mailCategoryPermissions.add(PERMISSION_TRAIN);
                            categoryJSON.put(FIELD_PERMISSIONS, mailCategoryPermissions);

                            categories.put(categoryJSON);
                        }
                        item.put(FIELD_LIST, categories);
                    } else {
                        item.put(FIELD_FEATURE_ENABLED, false);
                    }
                    setting.setSingleValue(item);
                } catch (JSONException e) {
                    throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create();
                }
            }

            @Override
            public boolean isWritable() {
                return true;
            }

            @Override
            public boolean isWritable(Session session) {
                return true;
            }

            @Override
            public void writeValue(Session session, Context ctx, User user, Setting setting) throws OXException {

                CapabilityService capabilityService = lookupService.getService(CapabilityService.class);
                if (capabilityService == null || !capabilityService.getCapabilities(session).contains(MAIL_CATEGORIES_CAPABILITY)) {
                    return;
                }

                JSONObject config = getType(setting.getSingleValue(), JSONObject.class, setting.getSingleValue(), setting.getName());
                try {

                    MailCategoriesConfigService service = lookupService.getOptionalService(MailCategoriesConfigService.class);
                    if (service == null) {
                        return;
                    }

                    boolean featureEnabled = config.getBoolean(FIELD_FEATURE_ENABLED);
                    if (!service.isForced(session)) {
                        boolean isFeatureEnabled = service.isEnabled(session);
                        if (isFeatureEnabled != featureEnabled) {
                            service.enable(session, featureEnabled);
                        }
                    }

                    JSONArray mailCategories = getType(config.get(FIELD_LIST), JSONArray.class, setting.getSingleValue(), setting.getName());
                    List<MailCategoryConfig> newConfigs = new ArrayList<>();
                    for (Object o : mailCategories.asList()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> newConfJSON = getType(o, Map.class, setting.getSingleValue(), setting.getName());
                        String catID = getType(newConfJSON.remove(FIELD_ID), String.class, setting.getSingleValue(), setting.getName());
                        String name = getType(newConfJSON.remove(FIELD_NAME), String.class, setting.getSingleValue(), setting.getName());
                        Boolean enable = getType(newConfJSON.remove(FIELD_ACTIVE), Boolean.class, setting.getSingleValue(), setting.getName());
                        newConfJSON.remove(FIELD_PERMISSIONS);
                        newConfJSON.remove(FIELD_DESCRIPTION);
                        if (!newConfJSON.isEmpty()) {
                            throw SettingExceptionCodes.INVALID_VALUE.create(setting.getSingleValue(), setting.getName());
                        }
                        MailCategoryConfig.Builder builder = new MailCategoryConfig.Builder();
                        MailCategoryConfig mcc = builder.category(catID).enabled(enable.booleanValue()).name(name).build();
                        newConfigs.add(mcc);
                    }
                    if (!newConfigs.isEmpty()) {
                        try {
                            service.updateConfigurations(newConfigs, session, user.getLocale());
                        } catch (OXException e) {
                            throw SettingExceptionCodes.NOT_ALLOWED.create(e, new Object[0]);
                        }
                    }
                } catch (JSONException e) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(setting.getSingleValue(), setting.getName());
                }

            }

            private <T> T getType(Object o, Class<T> clazz, Object exceptionObject, String name) throws OXException {
                if (!(clazz.isInstance(o))) {
                    throw SettingExceptionCodes.INVALID_VALUE.create(exceptionObject, name);
                }
                return clazz.cast(o);
            }

            @Override
            public int getId() {
                return NO_ID;
            }
        };
    }

    @Override
    public String getConfigTreePath() {
        return "modules/mail/categories";
    }

    @Override
    public String getJslobPath() {
        return "io.ox/mail//categories";
    }

}
