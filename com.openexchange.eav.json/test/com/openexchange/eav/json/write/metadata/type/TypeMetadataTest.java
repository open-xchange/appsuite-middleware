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

package com.openexchange.eav.json.write.metadata.type;

import org.json.JSONObject;
import com.openexchange.eav.EAVContainerType;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.EAVUnitTest;
import com.openexchange.eav.json.write.JSONWriterInterface;
import com.openexchange.json.JSONAssertion;
import static com.openexchange.json.JSONAssertion.assertValidates;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class TypeMetadataTest extends EAVUnitTest {

    private EAVTypeMetadataNode node;


    public void setUp() throws Exception {
        super.setUp();
        
        node = createMetadataNode("types", null, null, null, null,
                    createMetadataNode("com.shoefactory.crm", null, null, null, null,
                        createMetadataNode("shoeSize", EAVType.NUMBER, EAVContainerType.SINGLE, null, null),
                        createMetadataNode("favoriteModel", EAVType.STRING, EAVContainerType.SINGLE, null, null),
                        createMetadataNode("married", EAVType.BOOLEAN, EAVContainerType.SINGLE, null, null),
                        createMetadataNode("birthday", EAVType.DATE, EAVContainerType.SINGLE, null, null),
                        createMetadataNode("joinedDate", EAVType.TIME, EAVContainerType.SINGLE, "timezone", "Europe/Berlin"),
                        createMetadataNode("favoriteColors", EAVType.STRING, EAVContainerType.SET, null, null),
                        createMetadataNode("creditCardInformation", null, null, null, null,
                            createMetadataNode("type", EAVType.STRING, EAVContainerType.SINGLE, null, null),
                            createMetadataNode("number", EAVType.STRING, EAVContainerType.SINGLE, null, null)
                        ),
                        createMetadataNode("t", EAVType.STRING, EAVContainerType.MULTISET, "option123", "optionValue123")
                    )
                );
    }
    
    public void testWriter() throws Exception {
        JSONWriterInterface writer = new JSONTypeMetadataWriter(node);
        JSONObject json = writer.getJson();
        
        JSONAssertion assertion = new JSONAssertion()
            .isObject()
            .hasKey("types").withValueObject()
                .isObject()
                .hasKey("com.shoefactory.crm").withValueObject()
                    .isObject()
                    .hasKey("shoeSize").withValueObject()
                        .isObject()
                        .hasKey("t").withValue("number")
                        .hasKey("containerType").withValue("single")
                    .objectEnds()
                    .hasKey("favoriteModel").withValueObject()
                        .isObject()
                        .hasKey("t").withValue("string")
                        .hasKey("containerType").withValue("single")
                    .objectEnds()
                    .hasKey("married").withValueObject()
                        .isObject()
                        .hasKey("t").withValue("boolean")
                        .hasKey("containerType").withValue("single")
                    .objectEnds()
                    .hasKey("birthday").withValueObject()
                        .isObject()
                        .hasKey("t").withValue("date")
                        .hasKey("containerType").withValue("single")
                    .objectEnds()
                    .hasKey("joinedDate").withValueObject()
                        .isObject()
                        .hasKey("t").withValue("time")
                        .hasKey("containerType").withValue("single")
                        .hasKey("timezone").withValue("Europe/Berlin")
                    .objectEnds()
                    .hasKey("favoriteColors").withValueObject()
                        .isObject()
                        .hasKey("t").withValue("string")
                        .hasKey("containerType").withValue("set")
                    .objectEnds()
                    .hasKey("creditCardInformation").withValueObject()
                        .isObject()
                        .hasKey("type").withValueObject()
                            .isObject()
                            .hasKey("t").withValue("string")
                            .hasKey("containerType").withValue("single")
                        .objectEnds()
                        .hasKey("number").withValueObject()
                            .isObject()
                            .hasKey("t").withValue("string")
                            .hasKey("containerType").withValue("single")
                        .objectEnds()
                    .objectEnds()
                    .hasKey("t").withValueObject()
                        .isObject()
                        .hasKey("t").withValue("string")
                        .hasKey("containerType").withValue("multiset")
                        .hasKey("option123").withValue("optionValue123")
                    .objectEnds()
                .objectEnds()
            .objectEnds();
        
        assertValidates(assertion, json);
    }
}
