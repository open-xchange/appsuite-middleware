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

package com.openexchange.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.framework.ProvisioningSetup;
import com.openexchange.exception.OXException;

/**
 * Test suite for all AJAX interface tests. All suites considered within this definition will be executed sequentially.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    com.openexchange.test.MainInterfaceTests.class,
    com.openexchange.test.RESTTests.class,
    com.openexchange.test.InterfaceSmtpMockTests.class,
})
public final class InterfaceTests {

    @BeforeClass
    public static void beforeClass() {
        try {
            ProvisioningSetup.init();
        } catch (OXException e) {
            e.printStackTrace();
        }
    }
    
    @AfterClass
    public static void afterClass() {
        ProvisioningSetup.down();
    }
}
