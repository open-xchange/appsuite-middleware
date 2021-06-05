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

package com.openexchange.groupware.userconfiguration;

import static org.junit.Assert.fail;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.internal.PermissionConfigurationCheckerImpl;

/**
 * {@link PermissionConfigurationCheckerTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public class PermissionConfigurationCheckerTest {

    private PermissionConfigurationChecker checker;

    /**
     * Initializes a new {@link PermissionConfigurationCheckerTest}.
     */
    public PermissionConfigurationCheckerTest() {
        super();
        checker = new PermissionConfigurationCheckerImpl();
    }

    @Test
    public void testChecker() {

        Map<String, String> map = new HashMap<>();
        map.put("com.openexchange.capability.infostore", "true");
        map.put("com.openexchange.capability.contacts", "true");
        try {
            checker.checkAttributes(map);
            fail();
        } catch (@SuppressWarnings("unused") OXException e) {
            // expected
        }

        map = new HashMap<>();
        map.put("com.openexchange.capability.infostore", null);
        try {
            // Should always work
            checker.checkAttributes(map);
        } catch (@SuppressWarnings("unused") OXException e) {
            fail();
        }

        try {
            checker.checkCapabilities(Collections.singleton("infostore"));
            fail();
        } catch (@SuppressWarnings("unused") OXException e) {
            // expected
        }
    }

}
