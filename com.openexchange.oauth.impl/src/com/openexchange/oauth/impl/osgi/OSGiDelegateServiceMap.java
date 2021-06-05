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

package com.openexchange.oauth.impl.osgi;

import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;

/**
 * {@link OSGiDelegateServiceMap}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiDelegateServiceMap {

    private final Map<Class<?>, AbstractOSGiDelegateService<?>> map;

    /**
     * Initializes a new {@link OSGiDelegateServiceMap}.
     */
    public OSGiDelegateServiceMap() {
        super();
        map = new HashMap<Class<?>, AbstractOSGiDelegateService<?>>(4);
    }

    public void clear() {
        for (final AbstractOSGiDelegateService<?> delegateService : map.values()) {
            delegateService.stop();
        }
        map.clear();
    }

    public void startAll(final BundleContext context) {
        for (final AbstractOSGiDelegateService<?> delegateService : map.values()) {
            delegateService.start(context);
        }
    }

    public boolean containsKey(final Class<?> serviceClass) {
        return map.containsKey(serviceClass);
    }

    public <S> S get(final Class<S> serviceClass) {
        return serviceClass.cast(map.get(serviceClass));
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public <C, S> void put(final Class<C> key, final AbstractOSGiDelegateService<S> delegateService) {
        map.put(key, delegateService);
    }

    public <S> S remove(final Class<S> serviceClass) {
        return serviceClass.cast(map.remove(serviceClass));
    }

    public int size() {
        return map.size();
    }

}
