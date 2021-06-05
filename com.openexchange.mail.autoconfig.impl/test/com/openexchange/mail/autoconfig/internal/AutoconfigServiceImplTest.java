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

package com.openexchange.mail.autoconfig.internal;

import static org.junit.Assert.assertEquals;
import javax.mail.internet.AddressException;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link AutoconfigServiceImplTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class AutoconfigServiceImplTest {

    private AutoconfigServiceImpl service;

    private final String umlautAddress = "franz@xn--drobst-wxa.de";

    private final String address = "info@schalke04.de";

    @Before
    public void setUp() {
        service = new AutoconfigServiceImpl(null);
    }

     @Test
     public void testGetDomain_containsUmlauts_returnsStillNonIDN() throws AddressException {
        QuotedInternetAddress quoatedAddress = new QuotedInternetAddress(umlautAddress);
        String domain = service.getDomain(quoatedAddress);

        assertEquals(umlautAddress.substring(umlautAddress.indexOf('@') + 1), domain);
    }

     @Test
     public void testGetDomain_containsNoUmlauts_returnAsItIs() throws AddressException {
        QuotedInternetAddress quoatedAddress = new QuotedInternetAddress(address);
        String domain = service.getDomain(quoatedAddress);

        assertEquals(address.substring(address.indexOf('@') + 1), domain);
    }
}
