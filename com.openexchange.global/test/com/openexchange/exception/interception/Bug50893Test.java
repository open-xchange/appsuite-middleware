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

package com.openexchange.exception.interception;

import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.exception.interception.internal.OXExceptionInterceptorRegistration;
import com.openexchange.sessiond.SessionExceptionCodes;
import junit.framework.TestCase;

/**
 * {@link Bug50893Test}
 *
 * Reflected content for /api/account
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.8.4
 */
public class Bug50893Test extends TestCase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
        OXExceptionInterceptorRegistration.initInstance();
    }

    @Test
    public void testSanitizeMaliciousSessionParamter() {
        OXException e = SessionExceptionCodes.SESSION_EXPIRED.create(
            "=========%0a%0d========================%0a%0d======================However.it.has.been.moved.to.our.new.website.at.WWW.TTACKER.COM=====================%0a%0d");
        assertNotNull(e.getMessage());
        assertFalse(e.getMessage(), e.getMessage().contains("TTACKER"));
    }

    @Test
    public void testDontSanitizeRegularSessionParamter() {
        OXException e = SessionExceptionCodes.SESSION_EXPIRED.create("5d52add5f0924a2280a30bc491538fdb");
        assertNotNull(e.getMessage());
        assertTrue(e.getMessage(), e.getMessage().contains("5d52add5f0924a2280a30bc491538fdb"));
    }

}
