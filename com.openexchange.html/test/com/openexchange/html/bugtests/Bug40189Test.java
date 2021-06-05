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

package com.openexchange.html.bugtests;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug40189Test}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class Bug40189Test extends AbstractSanitizing {
     @Test
     public void testInsecureHref() throws Exception {

        String content = "<a href=3D\"http://neon-response.hmmh.de/argon/jsp/DoubleOptNLZG=" +
            ".jsp?DOINLZGID=3D1SBPO-WgpKBxr3e3AzwHxpL8UI1qj72ypu-bJfhmKYs&SecureAdressKe=" +
            "y=3Dej7wSI1OsAQcy3wVWJAF%2Bg%3D%3D&amp;SendRedirect=3Dhttp%3A%2F%2Femp-onli=" +
            "ne.co.uk%2Fnew_registered__%2F%3Fcrm_id%3D%3C%25SecureAdressKey%25%3E\" targ=" +
            "et=3D\"_blank\"><strong>clicking here</strong></a>";

        String test = getHtmlService().sanitize(content, null, true, null, null);

        assertTrue("Unexpected return value", test.contains("http://neon-response.hmmh.de/argon/jsp/"));
    }
}
