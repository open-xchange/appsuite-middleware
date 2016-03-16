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

package com.openexchange.subscribe.microformats.objectparser;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.microformats.hCard.HCardParser.HCardVisitor;

/**
 * A visitor for the HCardParser. This one extends the typical one by reading OX-specific attributes, identified by the prefix defined in
 * OXMF_PREFIX ("ox_" in the following example). There are two ways to define an additional OX element: <code>
 * <span class="ox_element">myValue</span>
 * </code> or: <code>
 * <span class="ox_element">some unimportant stuff<span class="value">myValue</span></span>
 * </code> If both are
 * used, the second form takes precedence.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class OXMFVisitor extends HCardVisitor {

    private String attributeName = null;

    private Tag endTagForAttribute = null;

    private Tag endTagForOxmfElement = null;

    private boolean readingElementValueNow = false;

    private boolean readingSeparateValueNow = false;

    private boolean readingHCardNow = false;;

    private Map<String, String> oxmfElement;

    private final List<Map<String, String>> oxmfElements = new LinkedList<Map<String, String>>();

    public static final String OXMF_PREFIX = "ox_";

    public OXMFVisitor(int toParse, URI defaultBase) {
        super(toParse, defaultBase);
    }

    public List<Map<String, String>> getOXMFElements() {
        return oxmfElements;
    }

    @Override
    public void visitTag(Tag tag) {
        super.visitTag(tag);
        String hClass = tag.getAttribute("class");

        if (hClass == null) {
            return;
        }

        if (hClass.equalsIgnoreCase("vcard")) {
            readingHCardNow = true;
            endTagForOxmfElement = tag.getEndTag();
            oxmfElement = new HashMap<String, String>();
        }

        if (hClass.startsWith(OXMF_PREFIX)) {
            attributeName = hClass;
            endTagForAttribute = tag.getEndTag();
            readingElementValueNow = true;
        }

        if (hClass.equalsIgnoreCase("value") && attributeName != null) {
            readingSeparateValueNow = true;
        }
    }

    @Override
    public void visitEndTag(Tag tag) {
        super.visitEndTag(tag);

        if (tag.equals(endTagForOxmfElement)) {
            oxmfElements.add(oxmfElement);
            endTagForOxmfElement = null;
            readingHCardNow = false;
        }

        if (tag.equals(endTagForAttribute)) {
            attributeName = null;
            endTagForAttribute = null;
        }

        readingSeparateValueNow = false;
        readingElementValueNow = false;
    }

    @Override
    public void visitStringNode(Text string) {
        super.visitStringNode(string);

        if (!readingHCardNow) {
            return;
        }

        if (!readingSeparateValueNow && !readingElementValueNow) {
            return;
        }

        if (attributeName == null) {
            throw new IllegalStateException("Reading an ox value without an ox element?");
        }

        oxmfElement.put(attributeName, string.getText());
    }

}
