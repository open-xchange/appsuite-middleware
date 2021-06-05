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

package com.openexchange.groupware.settings.tree.modules.mail;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.impl.AbstractMailFuncs;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link UnifiedInboxEnablement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UnifiedInboxEnablement implements PreferencesItemService {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UnifiedInboxEnablement.class);

    /**
     * Default constructor.
     */
    public UnifiedInboxEnablement() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "unifiedinbox" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new AbstractMailFuncs() {

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }

            @Override
            protected Boolean isSet(final UserSettingMail settings) {
                if (false == MailProviderRegistry.isUnifiedMailAvailable()) {
                    return Boolean.FALSE;
                }

                final UnifiedInboxManagement management;
                try {
                    management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class, true);
                } catch (OXException e) {
                    LOG.warn("", e);
                    return Boolean.FALSE;
                }
                try {
                    return Boolean.valueOf(management.getUnifiedINBOXAccountID(settings.getUserId(), settings.getCid()) >= 0);
                } catch (OXException e) {
                    LOG.error("", e);
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void setValue(final UserSettingMail settings, final String value) {
                boolean enable = Boolean.parseBoolean(value);

                if (false == MailProviderRegistry.isUnifiedMailAvailable()) {
                    LOG.warn("{} of Unified Mail for user {} in context {} aborted: {}", enable ? "Enabling" : "Disabling", I(settings.getUserId()), I(settings.getCid()), "Not available");
                    return;
                }

                UnifiedInboxManagement management;
                try {
                    management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class, true);
                } catch (OXException e) {
                    LOG.warn("{} of Unified Mail for user {} in context {} aborted: {}", enable ? "Enabling" : "Disabling", I(settings.getUserId()), I(settings.getCid()), e.getMessage(), e);
                    return;
                }
                try {
                    final int userId = settings.getUserId();
                    final int cid = settings.getCid();
                    if (enable) {
                        if (management.getUnifiedINBOXAccountID(userId, cid) < 0) {
                            management.createUnifiedINBOX(userId, cid);
                        }
                    } else {
                        management.deleteUnifiedINBOX(userId, cid);
                    }
                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
        };
    }

}
