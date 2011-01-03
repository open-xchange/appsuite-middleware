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

package com.openexchange.eav.json.parse.arrayupdate;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVUnitTest;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.eav.json.parse.JSONParserInterface;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ArrayUpdateTest extends EAVUnitTest {
    
    private JSONObject jsonSimple, jsonComplex, jsonBadKey, jsonBadType;
    
    private EAVSetTransformation nodeSimple, nodeComplex;

    public void setUp() throws Exception {
        super.setUp();
        
        jsonSimple = new JSONObject()
            .put("com.shoefactory.crm", new JSONObject()
                .put("favoriteColors", new JSONObject()
                    .put("add", new JSONArray().put("red").put("blue"))));
        
        jsonComplex = new JSONObject()
            .put("com.shoefactory.crm", new JSONObject()
                .put("favoriteNumbers", new JSONObject()
                    .put("add", new JSONArray().put(1).put(3)))
                .put("subValues", new JSONObject()
                    .put("favoriteMovies", new JSONObject()
                        .put("add", new JSONArray().put("Titanic").put("Spiderman 3"))
                        .put("remove", new JSONArray().put("In Bruges").put("The Fifth Element")))));
        
        jsonBadKey = new JSONObject()
            .put("com.shoefactory.crm", new JSONObject()
                .put("favoriteColors", new JSONObject()
                    .put("replace", new JSONArray().put("red").put("blue"))));
        
        jsonBadType = new JSONObject()
            .put("com.shoefactory.crm", new JSONObject()
                .put("favoriteColors", new JSONObject()
                    .put("add", new JSONArray().put("red").put(2))));
        
        nodeSimple = createSetTransformation("updates", null, null, null,
            createSetTransformation("com.shoefactory.crm", null, null, null,
                createSetTransformation("favoriteColors", EAVType.STRING, new String[]{"red", "blue"}, null)));
        
        nodeComplex = createSetTransformation("updates", null, null, null,
            createSetTransformation("com.shoefactory.crm", null, null, null,
                createSetTransformation("favoriteNumbers", EAVType.NUMBER, new Integer[]{1, 3}, null),
                createSetTransformation("subValues", null, null, null,
                    createSetTransformation("favoriteMovies", EAVType.STRING, new String[]{"Titanic", "Spiderman 3"}, new String[]{"In Bruges", "The Fifth Element"}))));
    }
    
    public void testParsing() throws Exception {
        JSONParserInterface<EAVSetTransformation> parser = new JSONArrayUpdateParser("updates", jsonSimple);
        EAVSetTransformation node = parser.getNode();
        assertEquals(nodeSimple, node);
        
        parser = new JSONArrayUpdateParser("updates", jsonComplex);
        node = parser.getNode();
        assertEquals(nodeComplex, node);
    }
    
    public void testBad() throws Exception {
        JSONParserInterface<EAVSetTransformation> parser = new JSONArrayUpdateParser("updates", jsonBadKey);
        try {
            parser.getNode();
            fail("Exception expected");
        } catch (EAVJsonException e) {
            assertEquals("Wrong exception", EAVJsonExceptionMessage.InvalidTreeStructure.getDetailNumber(), e.getDetailNumber());
        }

        parser = new JSONArrayUpdateParser("updates", jsonBadType);
        try {
            parser.getNode();
            fail("Exception expected");
        } catch (EAVJsonException e) {
            assertEquals("Wrong exception", EAVJsonExceptionMessage.DifferentTypesInArray.getDetailNumber(), e.getDetailNumber());
        }
    }
}
