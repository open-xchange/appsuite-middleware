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

package com.openexchange.report.internal;

import java.util.Collection;
import java.util.Set;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import com.openexchange.context.ContextService;
import com.openexchange.user.UserService;


/**
 * {@link ContextTabularData}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ContextTabularData implements TabularData {

    private final ContextService contextService;

    private final UserService userService;

    /**
     * Initializes a new {@link ContextTabularData}.
     * @param userService
     * @param contextService
     */
    public ContextTabularData(ContextService contextService, UserService userService) {
        super();
        this.contextService = contextService;
        this.userService = userService;
    }

    @Override
    public Object[] calculateIndex(CompositeData value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object[] key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(CompositeData value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompositeData get(Object[] key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TabularType getTabularType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(CompositeData value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(CompositeData[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompositeData remove(Object[] key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection values() {
        // Nothing to do
        return null;
    }

}
