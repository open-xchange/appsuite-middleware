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

package com.openexchange.eav.json.write;


import static com.openexchange.json.JSONAssertion.assertValidates;
import org.json.JSONObject;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVUnitTest;
import com.openexchange.eav.json.write.JSONWriter;
import com.openexchange.eav.json.write.JSONWriterInterface;
import com.openexchange.json.JSONAssertion;
import com.openexchange.tools.encoding.Base64;


public class WriterTest extends EAVUnitTest {
    
    private EAVNode singleString,
                    multiString,
                    singleNumberInt,
                    singleNumberFloat,
                    multiNumberInt,
                    multiNumberFloat,
                    singleBoolean,
                    multiBoolean,
                    singleDate,
                    multiDate,
                    singleTime,
                    multiTime,
                    singleBinary,
                    multiBinary,
                    complex;

    public void setUp() throws Exception {
        super.setUp();
        
        singleString = N("com.openexchange.test",
            N("exampleString", "Hallo")
        );
        
        multiString = N("com.openexchange.test",
            N("exampleStrings", "Hello", "World","what's", "up")
        );
        
        singleNumberInt = N("com.openexchange.test",
            N("exampleNumberInt", 10)
        );
        
        singleNumberFloat = N("com.openexchange.test",
            N("exampleNumberFloat", 10.1)
        );
        
        multiNumberInt = N("com.openexchange.test",
            N("exampleNumbersInt", 11, 12, 13)
        );
        
        multiNumberFloat = N("com.openexchange.test",
            N("exampleNumbersFloat", 10.1, 10.2, 10.3)
        );
        
        singleBoolean = N("com.openexchange.test",
            N("exampleBoolean", true)
        );
        
        multiBoolean = N("com.openexchange.test",
            N("exampleBooleans", true, false, true, false, false)
        );
        
        singleDate = N("com.openexchange.test",
            N("exampleDate", EAVType.DATE, 1230814800000L)
        );
        
        multiDate = N("com.openexchange.test",
            N("exampleDates", EAVType.DATE, 1230814800000L, 1233493200000L)
        );
        
        singleTime = N("com.openexchange.test",
            N("exampleTime", EAVType.TIME, 1230814800000L)
        );
        
        multiTime = N("com.openexchange.test",
            N("exampleTimes", EAVType.TIME, 1230814800000L, 1233493200000L)
        );
        
        byte[] bytes = "Hello World".getBytes("UTF-8");
        singleBinary = N("com.openexchange.test",
            N("exampleBinary", bytes)
        );
        
        multiBinary = N("com.openexchange.test",
            N("exampleBinaries", bytes, bytes)
        );
        
        complex = N("com.openexchange.test", 
            N("exampleString", "Hallo"),
            N("exampleBoolean", true),
            N("exampleNumber", 12),
            N("exampleFloat", 12.1),
            N("exampleDate", EAVType.DATE, 12),
            N("exampleTime", EAVType.TIME, 12),
            N("multiples", 
                N("strings", "Hello", "World","what's", "up"),
                N("bools", true, true, false, true, false, false, false, true),
                N("numbers", 12,13,14,15),
                N("dates", 12,13,14,15,16),
                N("times", 12,13,14,15,16)
            )
        );
    }
    
    public void testSingle() throws Exception {
        JSONWriterInterface writer = new JSONWriter(singleBoolean);
        JSONObject json = writer.getJson();
        JSONAssertion assertion = prepareSingleAssertion("exampleBoolean", true);
        assertValidates(assertion, json);
        
        writer = new JSONWriter(singleDate);
        json = writer.getJson();
        assertion = prepareSingleAssertion("exampleDate", 1230814800000L);
        assertValidates(assertion, json);
        
        writer = new JSONWriter(singleNumberFloat);
        json = writer.getJson();
        assertion = prepareSingleAssertion("exampleNumberFloat", 10.1);
        assertValidates(assertion, json);
        
        writer = new JSONWriter(singleNumberInt);
        json = writer.getJson();
        assertion = prepareSingleAssertion("exampleNumberInt", 10);
        assertValidates(assertion, json);
        
        writer = new JSONWriter(singleString);
        json = writer.getJson();
        assertion = prepareSingleAssertion("exampleString", "Hallo");
        assertValidates(assertion, json);
        
        writer = new JSONWriter(singleTime);
        json = writer.getJson();
        assertion = prepareSingleAssertion("exampleTime", 1230814800000L);
        assertValidates(assertion, json);
        
        writer = new JSONWriter(singleBinary);
        json = writer.getJson();
        assertion = prepareSingleAssertion("exampleBinary", Base64.encode("Hello World".getBytes("UTF-8")));
        assertValidates(assertion, json);
        
        
    }
    
    public void testMultiple() throws Exception {
        JSONWriterInterface writer = new JSONWriter(multiBoolean);
        JSONObject json = writer.getJson();
        JSONAssertion assertion = prepareMultipleAssertion("exampleBooleans", true, false, true, false, false);
        assertValidates(assertion, json);

        writer = new JSONWriter(multiDate);
        json = writer.getJson();
        assertion = prepareMultipleAssertion("exampleDates", 1230814800000L, 1233493200000L);
        assertValidates(assertion, json);

        writer = new JSONWriter(multiNumberFloat);
        json = writer.getJson();
        assertion = prepareMultipleAssertion("exampleNumbersFloat", 10.1, 10.2, 10.3);
        assertValidates(assertion, json);

        writer = new JSONWriter(multiNumberInt);
        json = writer.getJson();
        assertion = prepareMultipleAssertion("exampleNumbersInt", 11, 12, 13);
        assertValidates(assertion, json);

        writer = new JSONWriter(multiString);
        json = writer.getJson();
        assertion = prepareMultipleAssertion("exampleStrings", "Hello", "World","what's", "up");
        assertValidates(assertion, json);

        writer = new JSONWriter(multiTime);
        json = writer.getJson();
        assertion = prepareMultipleAssertion("exampleTimes", 1230814800000L, 1233493200000L);
        assertValidates(assertion, json);
        
        writer = new JSONWriter(multiBinary);
        json = writer.getJson();
        String encoded = Base64.encode("Hello World".getBytes("UTF-8"));
        assertion = prepareMultipleAssertion("exampleBinaries", encoded, encoded);
        assertValidates(assertion, json);
    }
    
    public void testComplex() throws Exception {
        JSONWriterInterface writer = new JSONWriter(complex);
        JSONObject json = writer.getJson();
        JSONAssertion assertion = new JSONAssertion()
            .isObject()
            .hasKey("com.openexchange.test").withValueObject()
                .isObject()
                .hasKey("exampleString").withValue("Hallo")
                .hasKey("exampleBoolean").withValue(true)
                .hasKey("exampleNumber").withValue(12)
                .hasKey("exampleFloat").withValue(12.1)
                .hasKey("exampleDate").withValue(12)
                .hasKey("exampleTime").withValue(12)
                .hasKey("multiples").withValueObject()
                    .hasKey("strings").withValueArray().withValues("Hello", "World", "what's", "up").inAnyOrder()
                    .hasKey("bools").withValueArray().withValues(true, true, false, true, false, false, false, true).inAnyOrder()
                    .hasKey("numbers").withValueArray().withValues(12, 13, 14, 15).inAnyOrder()
                    .hasKey("dates").withValueArray().withValues(12, 13, 14, 15, 16).inAnyOrder()
                    .hasKey("times").withValueArray().withValues(12, 13, 14, 15, 16).inAnyOrder()
                .objectEnds()
            .objectEnds()
        .objectEnds();
        assertValidates(assertion, json);
    }
    
    private JSONAssertion prepareSingleAssertion(String key, Object value) {
        return new JSONAssertion()
            .isObject()
            .hasKey("com.openexchange.test").withValueObject()
                .isObject()
                .hasKey(key).withValue(value)
            .objectEnds()
        .objectEnds();
    }
    
    private JSONAssertion prepareMultipleAssertion(String key, Object... values) {
        return new JSONAssertion()
            .isObject()
            .hasKey("com.openexchange.test").withValueObject()
                .isObject()
                .hasKey(key).withValueArray().withValues(values).inAnyOrder()
            .objectEnds()
        .objectEnds();
    }
}
