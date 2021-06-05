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

package com.openexchange.mail.usersetting.reloadable;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;

/**
 * {@link ForcedReloadable} that ensures to clear {@link UserSettingMail} cache and guarantees reading the configuration again.<br>
 * <br>
 * As the initial configuration setting 'com.openexchange.spamhandler.enabled' is ConfigCascade-aware and might only have configurations within
 * the contextSets definition we have to force this download as changes in the contextSets cannot be recognized without having a defined file.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class UserSettingMailReloadable implements ForcedReloadable {

    /**
     * Initializes a new {@link UserSettingMailReloadable}.
     */
    public UserSettingMailReloadable() {
        super();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            UserSettingMailStorage.getInstance().clearStorage();
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserSettingMailReloadable.class);
            logger.error("Unable to reload configuration for UserSettingMail.", e);
        }
    }
}
