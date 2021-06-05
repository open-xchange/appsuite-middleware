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

package com.openexchange.server;

import java.util.HashMap;
import java.util.Map;
import org.mockito.Mockito;

/**
 * {@link MockingServiceLookup}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */

public class MockingServiceLookup implements ServiceLookup {

    private Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

    public <S> S mock(Class<? extends S> clazz) {
        Object object = services.get(clazz);
        if (object != null) {
            return (S) object;
        }
        S instance = Mockito.mock(clazz);
        services.put(clazz, instance);
        return instance;
    }

    @Override
    public <S> S getService(Class<? extends S> clazz) {
        return mock(clazz);
    }

    @Override
    public <S> S getOptionalService(Class<? extends S> clazz) {
        return (S) services.get(clazz);
    }

}
