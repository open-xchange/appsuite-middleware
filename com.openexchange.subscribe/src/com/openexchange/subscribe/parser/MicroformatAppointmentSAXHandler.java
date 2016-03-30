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

package com.openexchange.subscribe.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import com.openexchange.groupware.calendar.CalendarDataObject;

/**
 *
 * {@link MicroformatAppointmentSAXHandler}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class MicroformatAppointmentSAXHandler extends AbstractMicroformatSAXHandler<CalendarDataObject>{

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss z"); //iso 8601, this one should be locale-independend
    private final List<Exception> exceptions = new LinkedList<Exception>();

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        String className = atts.getValue("class");
        if(null == className ){
            return;
        }
        if( className.equals("ox-appointment")){
            newObjectEncountered();
        }
        else if( className.equals("title")){
            currentlyReading = "title";
        }
        else if( className.equals("note")){
            currentlyReading = "note";
        }
        else if( className.equals("location")){
            currentlyReading = "location";
        }
        else if( className.equals("startDate")){
            currentlyReading = "startDate";
        }
        else if( className.equals("endDate")){
            currentlyReading = "endDate";
        }
        else {
            currentlyReading = null;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if( currentlyReading == null){
            return;
        }
        if( currentlyReading.equals("ox-appointment")){
            newObjectEncountered();
        }
        else if( currentlyReading.equals("title")){
            currentObject.setTitle( stringArray(ch, start, start+length) );
        }
        else if( currentlyReading.equals("note")){
            currentObject.setNote( stringArray(ch, start, start+length) );
        }
        else if( currentlyReading.equals("location")){
            currentObject.setLocation( stringArray(ch, start, start+length) );
        }
        else if( currentlyReading.equals("startDate")){
            synchronized (dateFormat) {
                try {
                    currentObject.setStartDate(dateFormat.parse(stringArray(ch, start, start + length)));
                } catch (ParseException e) {
                    exceptions.add(e);
                }
            }
        }
        else if( currentlyReading.equals("endDate")){
            synchronized (dateFormat) {
                try {
                    currentObject.setEndDate(dateFormat.parse(stringArray(ch, start, start + length)));
                } catch (ParseException e) {
                    exceptions.add(e);
                }
            }
        }
    }

    @Override
    public CalendarDataObject instantiate() {
        return new CalendarDataObject();
    }
}
