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

package com.openexchange.file.storage.limit.type.impl;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.exceptions.FileLimitExceptionCodes;
import com.openexchange.file.storage.limit.type.TypeLimitChecker;
import com.openexchange.java.ConcurrentList;
import com.openexchange.java.Strings;

/**
 * {@link TypeLimitCheckerRegistry}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class TypeLimitCheckerRegistry {

    private static final TypeLimitCheckerRegistry SINGLETON = new TypeLimitCheckerRegistry();

    public static TypeLimitCheckerRegistry getInstance() {
        return SINGLETON;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<String, List<TypeLimitChecker>> typeLimitCheckers = new ConcurrentHashMap<>();

    private TypeLimitCheckerRegistry() {
        // prevent instantiation
    }

    public synchronized void register(TypeLimitChecker... checkersToRegister) {
        for (TypeLimitChecker typeLimitChecker : checkersToRegister) {
            List<TypeLimitChecker> list = typeLimitCheckers.get(Strings.asciiLowerCase(typeLimitChecker.getType()));
            if (list == null) {
                list = new ConcurrentList<TypeLimitChecker>();
                typeLimitCheckers.put(Strings.asciiLowerCase(typeLimitChecker.getType()), list);
            }
            list.add(typeLimitChecker);
        }
    }

    public synchronized void unregister(TypeLimitChecker... checkersToUnregister) {
        for (TypeLimitChecker checkerToUnregister : checkersToUnregister) {
            List<TypeLimitChecker> availableCheckersForType = typeLimitCheckers.get(Strings.asciiLowerCase(checkerToUnregister.getType()));

            if (null != availableCheckersForType) {
                for (Iterator<TypeLimitChecker> it = availableCheckersForType.iterator(); it.hasNext();) {
                    TypeLimitChecker checker = it.next();
                    if (checker.equals(checkerToUnregister)) {
                        it.remove();
                    }
                }
                if (availableCheckersForType.isEmpty()) {
                    typeLimitCheckers.remove(Strings.asciiLowerCase(checkerToUnregister.getType()));
                }
            }
        }
    }

    public List<TypeLimitChecker> get(String type) throws OXException {
        List<TypeLimitChecker> list = typeLimitCheckers.get(Strings.asciiLowerCase(type));
        if (list != null && !list.isEmpty()) {
            return list;
        }
        throw FileLimitExceptionCodes.TYPE_NOT_AVAILABLE.create(type);
    }
}
