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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.eav.json.parse;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVUnitTest;
import com.openexchange.eav.json.parse.JSONParser;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ParserTest extends EAVUnitTest {
    
    private JSONObject singleString,
                       singleInteger,
                       singleLong,
                       singleFloat,
                       singleBoolean,
                       multiString,
                       multiInteger,
                       multiLong,
                       multiFloat,
                       multiBoolean,
                       complex;
    
    private EAVNode singleStringEAV,
                    singleIntegerEAV,
                    singleLongEAV,
                    singleFloatEAV,
                    singleBooleanEAV,
                    multiStringEAV,
                    multiIntegerEAV,
                    multiLongEAV,
                    multiFloatEAV,
                    multiBooleanEAV,
                    complexEAV;
    
    public void setUp() throws Exception {
        super.setUp();

        singleString = new JSONObject().put("exampleString", "Hello");
        singleInteger = new JSONObject().put("exampleInteger", 12);
        singleLong = new JSONObject().put("exampleLong", 1230814800000L);
        singleFloat = new JSONObject().put("exampleFloat", 10.1);
        singleBoolean = new JSONObject().put("exampleBoolean", true);

        multiString = new JSONObject().put("exampleStrings", jsonArray("Hello", "World", "what's", "up"));
        multiInteger = new JSONObject().put("exampleIntegers", jsonArray(10, 11, 12, 13));
        multiLong = new JSONObject().put("exampleLongs", jsonArray(1230814800000L, 1230814801000L, 1230814802000L, 1230814803000L));
        multiFloat = new JSONObject().put("exampleFloats", jsonArray(10.1, 10.2, 10.3));
        multiBoolean = new JSONObject().put("exampleBooleans", jsonArray(true, false, true, false, false, true));
        
        complex = new JSONObject()
            .put("exampleString", "Hello")
            .put("exampleInteger", 12)
            .put("exampleStrings", jsonArray("Hello", "World", "what's", "up"))
            .put("exampleFloat", 10.1)
            .put("sub", new JSONObject()
                .put("exampleLong", 1230814800000L)
                .put("exampleBooleans", jsonArray(true, false, true, false, false, true))
            )
            .put("exampleBoolean", true);
        
        singleStringEAV = new EAVNode("exampleString");
        singleStringEAV.setPayload("Hello");
        singleIntegerEAV = new EAVNode("exampleInteger");
        singleIntegerEAV.setPayload(12);
        singleLongEAV = new EAVNode("exampleLong");
        singleLongEAV.setPayload(1230814800000L);
        singleFloatEAV = new EAVNode("exampleFloat");
        singleFloatEAV.setPayload(10.1);
        singleBooleanEAV = new EAVNode("exampleBoolean");
        singleBooleanEAV.setPayload(true);

        multiStringEAV = new EAVNode("exampleStrings");
        multiStringEAV.setPayload("Hello", "World", "what's", "up");
        multiIntegerEAV = new EAVNode("exampleIntegers");
        multiIntegerEAV.setPayload(10, 11, 12, 13);
        multiLongEAV = new EAVNode("exampleLongs");
        multiLongEAV.setPayload(1230814800000L, 1230814801000L, 1230814802000L, 1230814803000L);
        multiFloatEAV = new EAVNode("exampleFloats");
        multiFloatEAV.setPayload(10.1, 10.2, 10.3);
        multiBooleanEAV = new EAVNode("exampleBooleans");
        multiBooleanEAV.setPayload(true, false, true, false, false, true);
        
        complexEAV = eavNode(singleStringEAV,
                             singleIntegerEAV,
                             multiStringEAV,
                             singleFloatEAV,
                             eavNode("sub",
                                     singleLongEAV,
                                     multiBooleanEAV),
                             singleBooleanEAV);
    }
    
    public void testSingle() throws Exception {
        compare(singleBooleanEAV, singleBoolean);
        compare(singleFloatEAV, singleFloat);
        compare(singleIntegerEAV, singleInteger);
        compare(singleLongEAV, singleLong);
        compare(singleStringEAV, singleString);
    }

    public void testMultiple() throws Exception {
        compare(multiBooleanEAV, multiBoolean);
        compare(multiFloatEAV, multiFloat);
        compare(multiIntegerEAV, multiInteger);
        compare(multiLongEAV, multiLong);
        compare(multiStringEAV, multiString);
    }

    public void testComplex() throws Exception {
        JSONParser parser = new JSONParser(complex);
        EAVNode node = parser.getEAVNode();
        assertEquals(complexEAV, node);
    }

    private void compare(EAVNode expected, JSONObject json) throws Exception {
        JSONParser parser = new JSONParser(json);
        EAVNode node = parser.getEAVNode();
        EAVNode wrappedEAV = eavNode(expected);
        assertEquals(wrappedEAV, node);
    }

    private JSONArray jsonArray(Object first, Object... additional) {
        JSONArray retval = new JSONArray();

        retval.put(first);
        for (Object o : additional) {
            retval.put(o);
        }

        return retval;
    }

    private EAVNode eavNode(EAVNode... children) {
        return eavNode(null, children);
    }

    private EAVNode eavNode(String name, EAVNode... children) {
        EAVNode retval;
        if (name != null) {
            retval = new EAVNode(name);
        } else {
            retval = new EAVNode();
        }
        retval.addChildren(children);
        return retval;
    }
}
