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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.spamfilter;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 * {@link SpamFilterUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class SpamFilterUserAttributeChangers extends AbstractAttributeChangers {

    /**
     * Initialises a new {@link SpamFilterUserAttributeChangers}.
     */
    public SpamFilterUserAttributeChangers() {
        super();
    }

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        Boolean spam_filter_enabled = userData.getGui_spam_filter_enabled();
        if (null == spam_filter_enabled) {
            return EMPTY_SET;
        }

        OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        Context ctx = new Context(I(contextId));
        if (spam_filter_enabled.booleanValue()) {
            tool.setUserSettingMailBit(ctx, userData, UserSettingMail.INT_SPAM_ENABLED, connection);
            return Collections.singleton("spam filter enabled");
        }
        tool.unsetUserSettingMailBit(ctx, userData, UserSettingMail.INT_SPAM_ENABLED, connection);
        return Collections.singleton("spam filter disabled");
    }
}
