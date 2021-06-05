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

package com.openexchange.config.cascade.user;

import java.util.Collections;
import java.util.List;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.java.ConvertUtils;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link BasicPropertyImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class BasicPropertyImpl implements BasicProperty {

    private static final String DYNAMIC_ATTR_PREFIX = UserConfigProvider.DYNAMIC_ATTR_PREFIX;

    private final int contextId;
    private final int userId;
    private final String property;
    private final ServiceLookup services;
    private volatile String value;

    /**
     * Initializes a new {@link BasicPropertyImplementation}.
     *
     * @throws OXException If initialization fails
     */
    BasicPropertyImpl(final String property, final int userId, final int contextId, final ServiceLookup services) throws OXException {
        super();
        // Preload value
        Context context = services.getServiceSafe(ContextService.class).getContext(contextId, UpdateBehavior.DENY_UPDATE);
        User user = services.getServiceSafe(UserService.class).getUser(userId, context);
        value = ConvertUtils.loadConvert(user.getAttributes().get(new StringBuilder(DYNAMIC_ATTR_PREFIX).append(property).toString()));
        // Assign rest
        this.contextId = contextId;
        this.userId = userId;
        this.property = property;
        this.services = services;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public String get(final String metadataName) throws OXException {
        return null;
    }

    @Override
    public boolean isDefined() throws OXException {
        return get() != null;
    }

    @Override
    public void set(String value) throws OXException {
        ContextService contextService = services.getService(ContextService.class);
        UserService userService = services.getService(UserService.class);

        String valueToStore = ConvertUtils.saveConvert(value, false, true);
        Context context = contextService.getContext(contextId);
        userService.setAttribute(new StringBuilder(DYNAMIC_ATTR_PREFIX).append(property).toString(), valueToStore, userId, context);

        this.value = value;
    }

    @Override
    public void set(final String metadataName, final String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, ConfigViewScope.USER.getScopeName());
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        return Collections.emptyList();
    }
}