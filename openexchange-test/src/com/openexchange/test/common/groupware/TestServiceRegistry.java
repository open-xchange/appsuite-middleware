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

package com.openexchange.test.common.groupware;

import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link TestServiceRegistry}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class TestServiceRegistry implements ServiceLookup {

    private static final TestServiceRegistry REGISTRY = new TestServiceRegistry();

    private TestServiceRegistry() {
        super();
    }

    public static TestServiceRegistry getInstance() {
        return REGISTRY;
    }

    public void clearRegistry() {
        ServerServiceRegistry.getInstance().clearRegistry();
    }

    public void removeService(final Class<?> clazz) {
        ServerServiceRegistry.getInstance().removeService(clazz);
    }

    public <S extends Object> void addService(final Class<? extends S> clazz, final S service) {
        ServerServiceRegistry.getInstance().addService(clazz, service);
    }

    @Override
    public <S extends Object> S getService(final Class<? extends S> clazz) {
        return ServerServiceRegistry.getInstance().getService(clazz);
    }

    public <S extends Object> S getService(final Class<? extends S> clazz, final boolean failOnError) throws OXException {
        return ServerServiceRegistry.getInstance().getService(clazz, failOnError);
    }

    @Override
    public <S> S getOptionalService(Class<? extends S> clazz) {
        try {
            return ServerServiceRegistry.getInstance().getService(clazz, false);
        } catch (OXException e) {
            return null;
        }
    }
}
