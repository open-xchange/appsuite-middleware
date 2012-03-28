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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.eav;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.exception.OXException;

/**
 * {@link EAVTypeTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EAVTypeTest extends EAVUnitTest {

    /*
     * #isCompatible
     */

    public void testInvalidDate() {
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR, 23);

        final long notMidnightUTC = calendar.getTimeInMillis();

        try {
            EAVType.DATE.checkCoercible(EAVType.NUMBER, notMidnightUTC);
            fail("Validate non date value");
        } catch (final OXException x) {
            assertTrue(EAVErrorMessage.ILLEGAL_VALUE.equals(x));
        }
    }

    public void testEAVTypeMismatch() {
        for (final EAVType type1 : EAVType.values()) {
            for (final EAVType type2 : EAVType.values()) {
                if (type1 == type2) {
                    assertCoercible(type1, type2);
                } else if (type1 == EAVType.NULL) {
                    assertCoercible(type1, type2);
                } else {
                    switch (type1) {
                    case NUMBER:
                        switch (type2) {
                        case DATE:
                        case TIME:
                            assertCoercible(type1, type2);
                            break;
                        default:
                            assertNotCoercible(type1, type2);
                        }
                        break;
                    case STRING:
                        if (type2 == EAVType.BINARY) {
                            assertCoercible(type1, type2);
                        } else {
                            assertNotCoercible(type1, type2);
                        }
                        break;
                    default:
                        assertNotCoercible(type1, type2);
                    }
                }

            }
        }
    }

    private void assertNotCoercible(final EAVType t1, final EAVType t2) {
        assertFalse("Should not be able to coerce from " + t1.name() + " to " + t2.name(), t2.isCoercibleFrom(t1));
    }

    private void assertCoercible(final EAVType t1, final EAVType t2) {
        assertTrue("Should be able to coerce from " + t1.name() + " to " + t2.name(), t2.isCoercibleFrom(t1));
    }

}
