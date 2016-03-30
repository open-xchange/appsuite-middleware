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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.FormElement.Widget;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;

/**
 * {@link CybernekoOXMFFormParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CybernekoOXMFFormParser implements OXMFFormParser {

    @Override
    public OXMFForm parse(String html) {
        return parse(new StringReader(html));
    }

    @Override
    public OXMFForm parse(Reader html) {
        OXMFForm form = new OXMFForm();
        DOMParser parser = new DOMParser();
        try {

            parser.parse(new InputSource(html));
            Document document = parser.getDocument();

            extractMetaInfo(form, document);

            NodeList candidates = document.getElementsByTagName("form");
            Element formElement = pickOXForm(candidates);
            if(formElement == null) {
                return form;
            }
            form.setAction(formElement.getAttribute("action"));
            NodeList labels = formElement.getElementsByTagName("label");
            Map<String, String> displayNames = new HashMap<String, String>();

            for(int i = 0, length = labels.getLength(); i < length; i++) {
                Element labelElement = (Element) labels.item(i);
                displayNames.put(labelElement.getAttribute("for"), labelElement.getTextContent());
            }


            NodeList inputFields = formElement.getElementsByTagName("input");

            for(int i = 0, length = inputFields.getLength(); i < length; i++) {
                Element inputElement = (Element) inputFields.item(i);
                FormElement element = new FormElement();
                Widget widget = FormElement.Widget.chooseFromHTMLElement(inputElement);
                if(widget == null) {
                    continue;
                }
                element.setWidget(widget);
                element.setName(inputElement.getAttribute("name"));

                String id = inputElement.getAttribute("id");
                if(id != null) {
                    element.setDisplayName(displayNames.get(inputElement.getAttribute("id")));
                }
                if(element.getDisplayName() == null) {
                    element.setDisplayName(element.getName());
                }
                form.add(element);
                String klass = inputElement.getAttribute("class");
                if(klass != null && klass.contains("ox_displayName")) {
                    form.setDisplayNameField(element);
                }
            }



        } catch (SAXException e) {
            OXMFSubscriptionErrorMessage.ParseException.create(e, e.getMessage());
        } catch (IOException e) {
            OXMFSubscriptionErrorMessage.IOException.create(e, e.getMessage());
        }

        return form;
    }

    private Element pickOXForm(NodeList candidates) {
        for(int i = 0, length = candidates.getLength(); i < length; i++) {
            Element formElement = (Element) candidates.item(i);
            if(formElement.getAttribute("class").contains("ox_form")) {
                return formElement;
            }
        }
        return null;
    }

    private void extractMetaInfo(OXMFForm form, Document document) {
        NodeList metaTags = document.getElementsByTagName("meta");

        for(int i = 0, length = metaTags.getLength(); i < length; i++) {
            Element element = (Element) metaTags.item(i);
            String key = element.getAttribute("name");
            String value = element.getAttribute("value");
            if(key != null && value != null) {
                form.putMetaInfo(key, value);
            }
        }
    }
}
