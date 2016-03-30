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

package com.openexchange.subscribe.microformats.parser;

import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.datatypes.genericonf.FormElement;


/**
 * {@link OXMFFormParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class OXMFFormParserTest extends TestCase {
    private static final String FORM_PAGE =
        "<html>\n"+
        "    <head>\n"+
        "        <meta name=\"ox_key\" value=\"ox_value\"></meta>\n"+
        "    </head>\n"+
        "    <body>\n"+
        "        <form class=\"ox_form\" action=\"http://someserver.com/someURL\">\n"+
        "            <label for=\"inputField1\">The First Input Field</label>\n"+
        "            <input type=\"text\" name=\"inputField\" id=\"inputField1\" class=\"ox_displayName\"></input>\n"+
        "            \n"+
        "            <label for=\"inputField2\">The other Input Field</label>\n"+
        "            <input type=\"text\" name=\"otherInputField\" id=\"inputField2\"></input>\n"+
        "            \n"+
        "            <label for=\"passwordField1\">The Password Field</label>\n"+
        "            <input type=\"password\" name=\"passwordField\" id=\"passwordField1\"></input>\n"+
        "            <input type=\"checkbox\" name=\"choice\" id=\"checkbox1\"></input><label for=\"checkbox1\">A checkbox</label>\n"+
        "        </form>\n"+
        "    </body>\n"+
        "</html>\n";


        public void testParseMetadata() {

            OXMFForm form = new CybernekoOXMFFormParser().parse(FORM_PAGE);
            assertNotNull(form);

            Map<String, String> metaInfo = form.getMetaInfo();
            assertNotNull(metaInfo);

            assertTrue(metaInfo.containsKey("ox_key"));
            assertEquals("ox_value", metaInfo.get("ox_key"));

        }

        public void testParseForm() {
            OXMFForm form = new CybernekoOXMFFormParser().parse(FORM_PAGE);
            assertNotNull(form);

            List<FormElement> formElements = form.getFormElements();
            assertNotNull(formElements);

            assertEquals(4, formElements.size());

            FormElement input1 = formElements.get(0);
            FormElement input2 = formElements.get(1);
            FormElement password = formElements.get(2);
            FormElement checkbox = formElements.get(3);

            assertEquals(FormElement.Widget.INPUT, input1.getWidget());
            assertEquals("inputField", input1.getName());
            assertEquals("The First Input Field", input1.getDisplayName());
            assertEquals(input1, form.getDisplayNameField());

            assertEquals(FormElement.Widget.INPUT, input2.getWidget());
            assertEquals("otherInputField", input2.getName());
            assertEquals("The other Input Field", input2.getDisplayName());

            assertEquals(FormElement.Widget.PASSWORD, password.getWidget());
            assertEquals("passwordField", password.getName());
            assertEquals("The Password Field", password.getDisplayName());


            assertEquals(FormElement.Widget.CHECKBOX, checkbox.getWidget());
            assertEquals("choice", checkbox.getName());
            assertEquals("A checkbox", checkbox.getDisplayName());

            assertEquals("http://someserver.com/someURL", form.getAction());



        }
}
