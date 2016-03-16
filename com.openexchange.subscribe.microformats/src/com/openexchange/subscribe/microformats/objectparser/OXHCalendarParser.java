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

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;
import com.openexchange.subscribe.microformats.parser.ObjectParser;


/**
 * {@link OXHCalendarParser}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class OXHCalendarParser implements ObjectParser<Appointment>{

    private List<Appointment> calendarData;

    @Override
    public Collection<Appointment> parse(final Reader html) throws OXException {
        final DOMParser parser = new DOMParser();
        reset();
        try {
            parser.parse(new InputSource(html));
            final Document doc = parser.getDocument();
            recurse(doc.getFirstChild());
            System.out.println();
        } catch (final SAXException e) {
            OXMFSubscriptionErrorMessage.ParseException.create(e, e.getMessage());
        } catch (final IOException e) {
            OXMFSubscriptionErrorMessage.IOException.create(e, e.getMessage());
        }
        return calendarData;
    }

    private void recurse(final Node node) {
        if(node == null) {
            return;
        }
        extractInformation(node);

        final NodeList children = node.getChildNodes();

        if(children != null && children.getLength() != 0) {
            for(int i = 0, length =children.getLength(); i < length; i++) {
                recurse(children.item(i));
            }
        }
    }

    private void extractInformation(final Node node) {
        if(node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }
        final Element elem = (Element) node;

        final List<String> classes = getClasses(elem);
        if(classes == null) {
            return;
        }

        final String name = node.getNodeName();

        String value = null;
        if("ABBR".equalsIgnoreCase(name)){
            value = elem.getAttribute("title");
        }

        if(value == null) {
            value = elem.getTextContent();
        }

        storeInformation(elem, value);
    }

    private void storeInformation(final Element elem, final String value){
        final List<String> classes = getClasses(elem);
        for(final String classname: classes){
            if("vevent".equalsIgnoreCase(classname)) {
                calendarData.add(new Appointment());
            }
            if(value == null) {
                continue;
            }
            if("location".equalsIgnoreCase(classname)) {
                last().setLocation(value);
            }
            if("summary".equalsIgnoreCase(classname)) {
                last().setNote(value);
            }
            if("dtstart".equalsIgnoreCase(classname)) {
                last().setStartDate(parseDate(value));
            }
            if("dtend".equalsIgnoreCase(classname)) {
                last().setEndDate(parseDate(value));
            }
        }
    }

    public static Date parseDate(final String data){
        final List<Locale> locales = Arrays.asList(Locale.US, Locale.UK, Locale.CANADA, Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN, Locale.CHINA);
        final int[] styles = new int [] {DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT };
        for(final Locale loc: locales){
            for(final int dateStyle: styles){
                for(final int timeStyle: styles){
                    final DateFormat sdf = DateFormat.getDateTimeInstance(dateStyle, timeStyle, loc);
                    try { return sdf.parse(data);
                        } catch (final ParseException e) {/*Next*/ }
                }
                final DateFormat sdf = DateFormat.getDateInstance(dateStyle, loc);
                try { return sdf.parse(data);
                    } catch (final ParseException e) {/*Next*/ }
                }
        }
        final DateFormat sdf = DateFormat.getInstance();
        try { return sdf.parse(data);
            } catch (final ParseException e) {/*Next*/ }

        return null;
    }


    private Appointment last() {
        if(calendarData == null || calendarData.size() == 0)
         {
            return new Appointment(); //no one cares about this, it will be dropped
        }

        return calendarData.get(calendarData.size() - 1);
    }

    private List<String> getClasses(final Element node) {
            final String[] classes = node.getAttribute("class").split("\\s+");
            final List<String> keys = Arrays.asList(classes);
            if(keys.size() == 0) {
                return null;
            }
            return keys;
    }

    private void reset() {
        calendarData = new LinkedList<Appointment>();
    }

}
