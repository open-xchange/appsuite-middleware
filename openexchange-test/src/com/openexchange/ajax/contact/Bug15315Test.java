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

package com.openexchange.ajax.contact;

import static org.junit.Assert.fail;
import java.util.Random;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.GetRequest;
import com.openexchange.ajax.contact.action.GetResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;

/**
 * {@link Bug15315Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug15315Test extends AbstractAJAXSession {

    private static final int RANGE = 100;
    private TimeZone tz;

    public Bug15315Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        tz = getClient().getValues().getTimeZone();
    }

    @Test
    public void testLoadContactsWithRandomFolder() throws Throwable {
        final Random rand = new Random(System.currentTimeMillis());
        for (int i = 1; i <= RANGE; i++) {
            GetRequest request = new GetRequest(rand.nextInt(), i, tz, false);
            GetResponse response = getClient().execute(request);
            if (!response.hasError()) {
                fail("Contacts can be read without respect to the folder identifier.");
            }
        }
    }
}
