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

package com.openexchange.subscribe.crawler.internal;

import java.util.Calendar;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.subscribe.helpers.HTTPToolkit;

/**
 * {@link Mappings}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Mappings {

    /**
     * Generates a {@link Contact contact} from specified map containing crawled contact information.
     *
     * @param map The map
     * @return The generated contact
     * @throws ConverterException If conversion fails
     */
    public static Contact translateMapToContact(final Map<String, String> map) throws OXException {

        final Contact contact = new Contact();

        {
            final String fn = map.get("first_name");
            if (!isEmpty(fn)) {
                contact.setGivenName(fn);
                contact.setDisplayName(fn);
            }

            final String ln = map.get("last_name");
            if (!isEmpty(ln)) {
                contact.setSurName(ln);
                contact.setDisplayName(ln);
            }

            if (!isEmpty(fn) && !isEmpty(ln)) {
                contact.setDisplayName(fn + " " + ln);
            }
        }

        String tmp = map.get("display_name");
        if (!isEmpty(tmp)) {
            contact.setDisplayName(tmp);
        }

        tmp = map.get("middle_name");
        if (!isEmpty(tmp)) {
            contact.setMiddleName(tmp);
        }

        tmp = map.get("title");
        if (!isEmpty(tmp)) {
            contact.setTitle(tmp);
        }

        tmp = map.get("street_home");
        if (!isEmpty(tmp)) {
            contact.setStreetHome(tmp);
        }

        tmp = map.get("postal_code_home");
        if (!isEmpty(tmp)) {
            contact.setPostalCodeHome(tmp);
        }

        tmp = map.get("city_home");
        if (!isEmpty(tmp)) {
            contact.setCityHome(tmp);
        }

        tmp = map.get("state_home");
        if (!isEmpty(tmp)) {
            contact.setStateHome(tmp);
        }

        tmp = map.get("country_home");
        if (!isEmpty(tmp)) {
            contact.setCountryHome(tmp);
        }

        tmp = map.get("street_business");
        if (!isEmpty(tmp)) {
            contact.setStreetBusiness(tmp);
        }

        tmp = map.get("postal_code_business");
        if (!isEmpty(tmp)) {
            contact.setPostalCodeBusiness(tmp);
        }

        tmp = map.get("city_business");
        if (!isEmpty(tmp)) {
            contact.setCityBusiness(tmp);
        }

        tmp = map.get("state_business");
        if (!isEmpty(tmp)) {
            contact.setStateBusiness(tmp);
        }

        tmp = map.get("country_business");
        if (!isEmpty(tmp)) {
            contact.setCountryBusiness(tmp);
        }

        tmp = map.get("street_other");
        if (!isEmpty(tmp)) {
            contact.setStreetOther(tmp);
        }

        tmp = map.get("postal_code_other");
        if (!isEmpty(tmp)) {
            contact.setPostalCodeOther(tmp);
        }

        tmp = map.get("city_other");
        if (!isEmpty(tmp)) {
            contact.setCityOther(tmp);
        }

        tmp = map.get("state_other");
        if (!isEmpty(tmp)) {
            contact.setStateOther(tmp);
        }

        tmp = map.get("country_other");
        if (!isEmpty(tmp)) {
            contact.setCountryOther(tmp);
        }

        tmp = map.get("email1");
        if (!isEmpty(tmp)) {
            contact.setEmail1(tmp);
        }

        tmp = map.get("email2");
        if (!isEmpty(tmp)) {
            contact.setEmail2(tmp);
        }

        tmp = map.get("email3");
        if (!isEmpty(tmp)) {
            contact.setEmail3(tmp);
        }

        tmp = map.get("telephone_home1");
        if (!isEmpty(tmp)) {
            contact.setTelephoneHome1(tmp);
        }

        tmp = map.get("telephone_business1");
        if (!isEmpty(tmp)) {
            contact.setTelephoneBusiness1(tmp);
        }

        tmp = map.get("cellular_telephone1");
        if (!isEmpty(tmp)) {
            contact.setCellularTelephone1(tmp);
        }

        tmp = map.get("cellular_telephone2");
        if (!isEmpty(tmp)) {
            contact.setCellularTelephone2(tmp);
        }

        tmp = map.get("fax_home");
        if (!isEmpty(tmp)) {
            contact.setFaxHome(tmp);
        }

        tmp = map.get("fax_business");
        if (!isEmpty(tmp)) {
            contact.setFaxBusiness(tmp);
        }

        tmp = map.get("company");
        if (!isEmpty(tmp)) {
            contact.setCompany(tmp);
        }

        tmp = map.get("position");
        if (!isEmpty(tmp)) {
            contact.setPosition(tmp);
        }

        tmp = map.get("employee_type");
        if (!isEmpty(tmp)) {
            contact.setEmployeeType(tmp);
        }

        tmp = map.get("department");
        if (!isEmpty(tmp)) {
            contact.setDepartment(tmp);
        }

        tmp = map.get("note");
        if (!isEmpty(tmp)) {
            contact.setNote(tmp);
        }
        // a special kind of note containing the address of the contact (used if the address is only available as one String)

        tmp = map.get("address_note");
        if (!isEmpty(tmp)) {
            final String htmlString = tmp;
            final String noHTMLString = htmlString.replaceAll("<br[ \t]*/?[ \t]*>", "\n").replaceAll("<.*?>", "");
            contact.setNote(noHTMLString);
        }

        tmp = map.get("profession");
        if (!isEmpty(tmp)) {
            contact.setProfession(tmp);
        }

        tmp = map.get("url");
        if (!isEmpty(tmp)) {
            contact.setURL(map.get("url"));
        }

        tmp = map.get("instant_messenger1");
        if (!isEmpty(tmp)) {
            final String tmp2 = map.get("instant_messenger1_type");
            if (null == tmp2) {
                contact.setInstantMessenger1(tmp);
            } else {
                contact.setInstantMessenger1(tmp + " (" + tmp2 + ")");
            }
        }

        tmp = map.get("instant_messenger2");
        if (!isEmpty(tmp)) {
            final String tmp2 = map.get("instant_messenger2_type");
            if (null == tmp2) {
                contact.setInstantMessenger1(tmp);
            } else {
                contact.setInstantMessenger1(tmp + " (" + tmp2 + ")");
            }
        }
        // handle birthdays

        tmp = map.get("birthday_month_real");
        if (!isEmpty(tmp)) {
            map.put("birthday_month", Integer.toString(Integer.parseInt(tmp) - 1));
        }

        tmp = map.get("birthday_month_string");
        if (!isEmpty(tmp)) {
            final String month = tmp;
            if (month.matches("(Januar|January|janvier|enero)")) {
                map.put("birthday_month", "0");
            }
            if (month.matches("(Februar|February|f\u00e9vrier|febrero)")) {
                map.put("birthday_month", "1");
            }
            if (month.matches("(M\u00e4rz|March|mars|marzo)")) {
                map.put("birthday_month", "2");
            }
            if (month.matches("(April|April|avril|abril)")) {
                map.put("birthday_month", "3");
            }
            if (month.matches("(Mai|May|mai|mayo)")) {
                map.put("birthday_month", "4");
            }
            if (month.matches("(Juni|June|juin|junio)")) {
                map.put("birthday_month", "5");
            }
            if (month.matches("(Juli|July|juillet|julio)")) {
                map.put("birthday_month", "6");
            }
            if (month.matches("(August|August|ao\u00fbt|agosto)")) {
                map.put("birthday_month", "7");
            }
            if (month.matches("(September|September|septembre|septiembre)")) {
                map.put("birthday_month", "8");
            }
            if (month.matches("(Oktober|October|octobre|octubre)")) {
                map.put("birthday_month", "9");
            }
            if (month.matches("(November|November|novembre|noviembre)")) {
                map.put("birthday_month", "10");
            }
            if (month.matches("(Dezember|December|d\u00e9cembre|diciembre)")) {
                map.put("birthday_month", "11");
            }
        }

        final String sDay = map.get("birthday_day");
        final String sMonth = map.get("birthday_month");
        if (null != sDay && null != sMonth) {
            final Calendar cal = Calendar.getInstance();
            final int date = Integer.parseInt(sDay);
            final int month = Integer.parseInt(sMonth);
            int year = 2009;
            if (null != map.get("birthday_year")) {
                year = Integer.parseInt(map.get("birthday_year"));
            }
            cal.set(year, month, date, 1, 1, 1);
            contact.setBirthday(cal.getTime());
        }

        // add the image from a url to the contact
        tmp = map.get("image");
        if (!isEmpty(tmp)) {
            HTTPToolkit.loadImageFromURL(contact, tmp);
        }

        return contact;
    }

    public static CalendarDataObject translateMapToCalendarDataObject(final Map<String, String> map){

        final CalendarDataObject oxEvent = new CalendarDataObject();

        {
            final String title = map.get("title");
            if (!isEmpty(title)) {
                oxEvent.setTitle(title);
            }
        }
        {
            final String note = map.get("note");
            if (!isEmpty(note)) {
                oxEvent.setNote(note);
            }
        }
        {
            final String tz = map.get("timezone");
            if (!isEmpty(tz)) {
                oxEvent.setTimezone(tz);
            }
        }
        final String[] dateTypes = new String[]{"start_date_","end_date_"};
        Calendar cal = null;
        for (final String dateType : dateTypes){
            cal = Calendar.getInstance();
            if (map.containsKey(dateType+"day") && map.containsKey(dateType+"month") && map.containsKey(dateType+"year") && map.containsKey(dateType+"hour") && map.containsKey(dateType+"minute")){
                final int year = Integer.parseInt(map.get("year"));
                final int month = Integer.parseInt(map.get("month"));
                final int date = Integer.parseInt("day");
                final int hour = Integer.parseInt(map.get("hour"));
                final int minute = Integer.parseInt(map.get("minute"));
                cal.set(year, month, date, hour, minute);
                if (dateType.equals("start_date_")){
                    oxEvent.setStartDate(cal.getTime());
                } else if (dateType.equals("end_date_")){
                    oxEvent.setEndDate(cal.getTime());
                }
            }
        }
        return oxEvent;
    }

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        boolean isSNull = true;
        for (int i = 0; (isWhitespace || isSNull) && i < len; i++) {
            final char c = string.charAt(i);
            isWhitespace = Strings.isWhitespace(c);
            if (isSNull) {
                switch (i) {
                case 0:
                    isSNull = ('n' == c || 'N' == c);
                    break;
                case 1:
                    isSNull = ('u' == c || 'U' == c);
                    break;
                case 2:
                    isSNull = ('l' == c || 'L' == c);
                    break;
                case 3:
                    isSNull = ('l' == c || 'L' == c);
                    break;
                default:
                    isSNull = false;
                    break;
                }
            }
        }
        return isWhitespace || isSNull;
    }

}
