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

package com.openexchange.config.cascade.user;

import java.util.Collections;
import java.util.List;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
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
        User user = services.getService(UserService.class).getUser(userId, services.getService(ContextService.class).getContext(contextId));
        value = user.getAttributes().get(new StringBuilder(DYNAMIC_ATTR_PREFIX).append(property).toString());
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
    public void set(final String value) throws OXException {
        final Context context = services.getService(ContextService.class).getContext(contextId);
        services.getService(UserService.class).setAttribute(new StringBuilder(DYNAMIC_ATTR_PREFIX).append(property).toString(), value, userId, context);
        this.value = value;
    }

    @Override
    public void set(final String metadataName, final String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, "user");
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        return Collections.emptyList();
    }
}