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

package com.openexchange.eav.json.parse.metadata.type;

import org.json.JSONObject;
import com.openexchange.eav.EAVContainerType;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.EAVUnitTest;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.eav.json.parse.JSONParserInterface;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class TypeMetadataTest extends EAVUnitTest {
    
    private JSONObject json, badJson;
    
    private EAVTypeMetadataNode node;

    public void setUp() throws Exception {
        super.setUp();
        
        json = new JSONObject()
            .put("com.shoefactory.crm", new JSONObject()
                .put("shoeSize", new JSONObject()
                    .put("t", "Number"))
                .put("favoriteModel", new JSONObject()
                    .put("t", "String"))
                .put("married", new JSONObject()
                    .put("t", "Boolean"))
                .put("birthday", new JSONObject()
                    .put("t", "Date"))
                .put("joinedDate", new JSONObject()
                    .put("t", "Time")
                    .put("timezone", "Europe/Berlin"))
                .put("favoriteColors", new JSONObject()
                    .put("t", "String")
                    .put("containerType", "set"))
                .put("creditCardInformation", new JSONObject()
                    .put("type", new JSONObject()
                        .put("t", "String"))
                    .put("number", new JSONObject()
                        .put("t", "String")))
                .put("t", new JSONObject()
                    .put("t", "String")
                    .put("containerType", "multiset")
                    .put("option123", "optionValue123")));
        
        badJson = new JSONObject()
            .put("com.shoefactory.crm", new JSONObject()
                .put("creditCardInformation", new JSONObject()
                    .put("evilOption", "evilOptionValue")
                    .put("type", new JSONObject()
                        .put("t", "String"))
                    .put("number", new JSONObject()
                        .put("t", "String"))));
        
        node = createMetadataNode("types", null, null, null, null,
                    createMetadataNode("com.shoefactory.crm", null, null, null, null,
                    createMetadataNode("shoeSize", EAVType.NUMBER, null, null, null),
                    createMetadataNode("favoriteModel", EAVType.STRING, null, null, null),
                    createMetadataNode("married", EAVType.BOOLEAN, null, null, null),
                    createMetadataNode("birthday", EAVType.DATE, null, null, null),
                    createMetadataNode("joinedDate", EAVType.TIME, null, "timezone", "Europe/Berlin"),
                    createMetadataNode("favoriteColors", EAVType.STRING, EAVContainerType.SET, null, null),
                    createMetadataNode("creditCardInformation", null, null, null, null,
                        createMetadataNode("type", EAVType.STRING, null, null, null),
                        createMetadataNode("number", EAVType.STRING, null, null, null)),
                    createMetadataNode("t", EAVType.STRING, EAVContainerType.MULTISET, "option123", "optionValue123")));
    }
    
    public void testParsing() throws Exception {
        JSONParserInterface<EAVTypeMetadataNode> parser = new JSONTypeMetadataParser("types", json);
        EAVTypeMetadataNode result = parser.getNode();
        assertEquals(node, result);
    }
    
    public void testBad() throws Exception {
        JSONParserInterface<EAVTypeMetadataNode> parser = new JSONTypeMetadataParser("types", badJson);
        try {
            parser.getNode();
            fail("Exception expected");
        } catch (EAVJsonException e) {
            assertEquals("Wrong exception", EAVJsonExceptionMessage.InvalidTreeStructure.getDetailNumber(), e.getDetailNumber());
        }
    }

}
