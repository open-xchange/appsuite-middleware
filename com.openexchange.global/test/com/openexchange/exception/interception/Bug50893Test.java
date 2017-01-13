/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
