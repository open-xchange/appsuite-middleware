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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.exception.OXException;


/**
 * {@link EAVTypeCoercionTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class EAVTypeCoercionTest extends EAVUnitTest {

    private final EAVTypeCoercion typeCoercion = new EAVTypeCoercion(EAVTypeCoercion.Mode.INCOMING);

    /*
     * Coercion
     */
    public void testCoerceNumberToDate() throws OXException {
        final EAVTypeMetadataNode dateMetadata = TYPE("testDate", EAVType.DATE);

        final Object expected = 1245715200000l;

        final Object actual = typeCoercion.coerce(EAVType.NUMBER, expected, dateMetadata);

        assertEquals(expected, actual);
    }

    public void testCoerceNumberToTimeWithTimezone() throws OXException {
        final TimeZone tz = TimeZone.getTimeZone("Pacific/Rarotonga");
        final long nowUTC = new Date().getTime();
        final long nowRarotonga = nowUTC + tz.getOffset(nowUTC);

        final EAVTypeMetadataNode timeMetadata = TYPE("testTime", EAVType.TIME, M("timezone", "Pacific/Rarotonga"));

        final long actual = (Long) typeCoercion.coerce(EAVType.NUMBER, nowRarotonga, timeMetadata);

        assertEquals(nowUTC, actual);
    }

    public void testCoerceNumberToTimeWithDefaultTimezone() throws OXException {
        final TimeZone tz = TimeZone.getTimeZone("Pacific/Rarotonga");
        final long nowUTC = new Date().getTime();
        final long nowRarotonga = nowUTC + tz.getOffset(nowUTC);

        final EAVTypeMetadataNode timeMetadata = TYPE("testTime", EAVType.TIME);

        final long actual = (Long) typeCoercion.coerce(EAVType.NUMBER, nowRarotonga, timeMetadata, tz);

        assertEquals(nowUTC, actual);
    }

    public void testCoerceNumberToTimeWithoutTimezoneAssumingUTC() throws OXException {
        final long nowUTC = new Date().getTime();

        final EAVTypeMetadataNode timeMetadata = TYPE("testTime", EAVType.TIME);

        final long actual = (Long) typeCoercion.coerce(EAVType.NUMBER, nowUTC, timeMetadata);

        assertEquals(nowUTC, actual);
    }

    public void testCoerceStringToBinary() throws IOException, OXException {
        final Base64 base64 = new Base64();
        final byte[] bytes = "Hello World".getBytes("UTF-8");
        final String encoded = new String(base64.encode(bytes), "ASCII");

        final EAVTypeMetadataNode binaryMetadata = TYPE("testBinary", EAVType.BINARY);

        final InputStream data = (InputStream) typeCoercion.coerce(EAVType.STRING, encoded, binaryMetadata);

        final byte[] buffer = new byte[bytes.length];
        final int read = data.read(buffer);

        assertEquals(read, buffer.length);
        assertEquals(-1, data.read());
        assertEquals("Hello World", new String(buffer, "UTF-8"));
   }

    public void testLeaveSameTypesAlone() throws OXException {

        final EAVTypeMetadataNode stringMetadata = TYPE("testString", EAVType.STRING);
        final EAVTypeMetadataNode booleanMetadata = TYPE("testBool", EAVType.BOOLEAN);
        final EAVTypeMetadataNode numberMetadata = TYPE("testNumber", EAVType.NUMBER);


        assertUnchanged(EAVType.STRING, "Hello", stringMetadata);
        assertUnchanged(EAVType.BOOLEAN, true, booleanMetadata);
        assertUnchanged(EAVType.NUMBER, 12, numberMetadata);

    }


    public void testInvalidDate() {
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR, 23);

        final long notMidnightUTC = calendar.getTimeInMillis();
        final EAVTypeMetadataNode dateMetadata = TYPE("testDate", EAVType.DATE);

        try {
            typeCoercion.coerce(EAVType.NUMBER, notMidnightUTC, dateMetadata);
            fail("Type coercion accepted illegal date");
        } catch (final OXException x) {
            assertTrue(EAVErrorMessage.ILLEGAL_VALUE.equals(x));
        }

    }

    // Note: For more about the validity of type coercions see the EAVTypeTest
    public void testImplicitTypeMismatch() {
        final EAVTypeMetadataNode stringMetadata = TYPE("testString", EAVType.STRING);
        try {
            typeCoercion.coerce(EAVType.NUMBER, 12, stringMetadata);
            fail("Could coerce number to string");
        } catch (final OXException x) {
            assertTrue(EAVErrorMessage.INCOMPATIBLE_TYPES.equals(x));
        }
    }


    protected void assertUnchanged(final EAVType type, final Object datum, final EAVTypeMetadataNode metadata) throws OXException {
        assertEquals(datum, typeCoercion.coerce(type, datum, metadata));
    }

    public void testCoerceEAVNodeTree() throws UnsupportedEncodingException {
        final TimeZone tz = TimeZone.getTimeZone("Pacific/Rarotonga");
        final long nowUTC = new Date().getTime();
        final long nowRarotonga = nowUTC + tz.getOffset(nowUTC);

        final Base64 base64 = new Base64();
        final byte[] bytes = "Hello World".getBytes("UTF-8");
        final String encoded = new String(base64.encode(bytes), "ASCII");


        final EAVNode tree = N("com.openexchange.test",
                            N("date", EAVType.NUMBER, 1245715200000l),
                            N("time", EAVType.NUMBER, nowRarotonga),
                            N("binary", encoded),
                            N("multiples",
                                N("date", EAVType.NUMBER, 1245715200000l,1245715200000l,1245715200000l),
                                N("time", EAVType.NUMBER, nowRarotonga, nowRarotonga, nowRarotonga),
                                N("binary", encoded, encoded, encoded)
                            )
                        );

        final EAVNode expected = N("com.openexchange.test",
                                N("date", EAVType.DATE, 1245715200000l),
                                N("time", EAVType.TIME, nowUTC),
                                N("binary", bytes),
                                N("multiples",
                                    N("date", EAVType.DATE, 1245715200000l,1245715200000l,1245715200000l),
                                    N("time", EAVType.TIME, nowUTC, nowUTC, nowUTC),
                                    N("binary", bytes, bytes, bytes)
                                )
                            );



        final EAVTypeMetadataNode metadata = TYPE("com.openexchange.test",
                                             TYPE("date", EAVType.DATE),
                                             TYPE("time", EAVType.TIME, M("timezone", tz.getID())),
                                             TYPE("binary", EAVType.BINARY),
                                             TYPE("multiples",
                                                 TYPE("date", EAVType.DATE, EAVContainerType.MULTISET),
                                                 TYPE("time", EAVType.TIME, EAVContainerType.MULTISET, M("timezone", tz.getID())),
                                                 TYPE("binary", EAVType.BINARY, EAVContainerType.MULTISET)
                                             )
                                        );

        final EAVNodeTypeCoercionVisitor visitor = new EAVNodeTypeCoercionVisitor(metadata, null, EAVTypeCoercion.Mode.INCOMING);
        tree.visit(visitor);

        assertNoError(visitor);
        assertEquals(expected, tree);
    }

    public void testCoercingSingleToMultipleFails() {
        final EAVNode tree = N("com.openexchange.test",
                            N("attribute", EAVType.NUMBER, 1245715200000l)
                        );

        final EAVTypeMetadataNode metadata = TYPE("com.openexchange.test",
                                           TYPE("attribute", EAVType.DATE, EAVContainerType.MULTISET)
                                       );

        final EAVNodeTypeCoercionVisitor visitor = new EAVNodeTypeCoercionVisitor(metadata, null, EAVTypeCoercion.Mode.INCOMING);
        tree.visit(visitor);

        assertError(visitor, EAVErrorMessage.WRONG_TYPES);
     }


    public void testCoerceEAVSetTransformationTree() throws UnsupportedEncodingException {
        final TimeZone tz = TimeZone.getTimeZone("Pacific/Rarotonga");
        final long nowUTC = new Date().getTime();
        final long nowRarotonga = nowUTC + tz.getOffset(nowUTC);

        final Base64 base64 = new Base64();
        final byte[] bytes = "Hello World".getBytes("UTF-8");
        final String encoded = new String(base64.encode(bytes), "ASCII");


        final EAVSetTransformation transformation = TRANS("com.openexchange.test",
                                                    TRANS("date", ADD(1245715200000l), REMOVE(1245715200000l)),
                                                    TRANS("time", ADD(nowRarotonga), REMOVE(nowRarotonga)),
                                                    TRANS("binary", ADD(encoded), REMOVE(encoded))
                                              );

        final EAVTypeMetadataNode metadata = TYPE("com.openexchange.test",
                                            TYPE("date", EAVType.DATE, EAVContainerType.MULTISET),
                                            TYPE("time", EAVType.TIME, EAVContainerType.MULTISET, M("timezone", tz.getID())),
                                            TYPE("binary", EAVType.BINARY, EAVContainerType.MULTISET)

                                       );

        final EAVSetTransformationTypeCoercionVisitor visitor = new EAVSetTransformationTypeCoercionVisitor(metadata, null,EAVTypeCoercion.Mode.INCOMING);
        transformation.visit(visitor);

        assertNoError(visitor);

        assertTransformation(transformation.getChildByName("date"), EAVType.DATE, ADD(1245715200000l), REMOVE(1245715200000l));
        assertTransformation(transformation.getChildByName("time"), EAVType.TIME, ADD(nowUTC), REMOVE(nowUTC));
        assertTransformation(transformation.getChildByName("binary"), EAVType.BINARY, ADD(bytes), REMOVE(bytes));
    }


    public void testCoercingSingleToMultipleInTransformationTreeFails() {
        final EAVSetTransformation transformation = TRANS("com.openexchange.test",
                                                    TRANS("date", ADD(1245715200000l), REMOVE(1245715200000l))
                                              );

        final EAVTypeMetadataNode metadata = TYPE("com.openexchange.test",
                                            TYPE("date", EAVType.DATE, EAVContainerType.SINGLE)
                                       );

        final EAVSetTransformationTypeCoercionVisitor visitor = new EAVSetTransformationTypeCoercionVisitor(metadata, null, EAVTypeCoercion.Mode.INCOMING);
        transformation.visit(visitor);

        assertError(visitor, EAVErrorMessage.WRONG_TYPES);
    }

    private void assertNoError(final AbstractEAVExceptionHolder holder) {
        assertNull(holder.getException());
    }

    private void assertError(final AbstractEAVExceptionHolder holder, final EAVErrorMessage message) {
        assertNotNull(holder.getException());
        assertEquals(message.getNumber(), holder.getException().getCode());
    }

}
