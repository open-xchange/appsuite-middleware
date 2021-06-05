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

import static org.junit.Assert.assertEquals;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.LimitFile;
import com.openexchange.file.storage.limit.type.TypeLimitChecker;
import com.openexchange.session.Session;
import com.openexchange.test.mock.MockUtils;

/**
 * {@link TypeLimitCheckerRegistryTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class TypeLimitCheckerRegistryTest {

    private static final String TYPE = "type";

    private TypeLimitChecker checker1 = new TypeLimitChecker() {

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public List<OXException> check(Session session, String folderId, List<LimitFile> files) throws OXException {
            return Collections.emptyList();
        }
    };
    private TypeLimitChecker checker2 = new TypeLimitChecker() {

        @Override
        public String getType() {
            return TYPE;
        }

        @Override
        public List<OXException> check(Session session, String folderId, List<LimitFile> files) throws OXException {
            return Collections.emptyList();
        }
    };

    @Before
    public void setUp() {
        MockUtils.injectValueIntoPrivateField(TypeLimitCheckerRegistry.getInstance(), "typeLimitCheckers", new ConcurrentHashMap<>());
    }

    @Test
    public void testUnregister() throws OXException {
        TypeLimitCheckerRegistry.getInstance().register(checker2, checker1);
        List<TypeLimitChecker> actual = TypeLimitCheckerRegistry.getInstance().get(TYPE);
        assertEquals(2, actual.size());

        TypeLimitCheckerRegistry.getInstance().unregister(checker1);

        actual = TypeLimitCheckerRegistry.getInstance().get(TYPE);
        assertEquals(1, actual.size());
        assertEquals(checker2, actual.get(0));
    }

    @Test
    public void testUnregisterTwice_onlyRemoveOnce() throws OXException {
        TypeLimitCheckerRegistry.getInstance().register(checker2, checker1);
        List<TypeLimitChecker> actual = TypeLimitCheckerRegistry.getInstance().get(TYPE);
        assertEquals(2, actual.size());

        TypeLimitCheckerRegistry.getInstance().unregister(checker1, checker1);

        actual = TypeLimitCheckerRegistry.getInstance().get(TYPE);
        assertEquals(1, actual.size());
        assertEquals(checker2, actual.get(0));
    }

    @Test(expected = OXException.class)
    public void testUnregisterAll() throws OXException {
        TypeLimitCheckerRegistry.getInstance().register(checker2, checker1);
        List<TypeLimitChecker> actual = TypeLimitCheckerRegistry.getInstance().get(TYPE);
        assertEquals(2, actual.size());

        TypeLimitCheckerRegistry.getInstance().unregister(checker1, checker2);

        actual = TypeLimitCheckerRegistry.getInstance().get(TYPE);
    }
}
