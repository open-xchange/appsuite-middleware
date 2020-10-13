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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.impl;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link TimeZoneMapping}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.5
 */
public class TimeZoneMapping {

    // @formatter:off
    /**
     * Windows to Olson timezone mappings
     */
    static final Map<String, String> WINDOWS2OLSON = ImmutableMap.<String, String> builder()
        .put("Saint Pierre Standard Time", "America/Miquelon")
        .put("Greenwich Standard Time", "Atlantic/Reykjavik")
        .put("Tasmania Standard Time", "Australia/Hobart")
        .put("Magallanes Standard Time", "America/Punta_Arenas")
        .put("Central European Standard Time", "Europe/Warsaw")
        .put("Azores Standard Time", "Atlantic/Azores")
        .put("Arabic Standard Time", "Asia/Baghdad")
        .put("Samoa Standard Time", "Pacific/Apia")
        .put("SA Western Standard Time", "America/La_Paz")
        .put("Bahia Standard Time", "America/Bahia")
        .put("Pakistan Standard Time", "Asia/Karachi")
        .put("Libya Standard Time", "Africa/Tripoli")
        .put("Yakutsk Standard Time", "Asia/Yakutsk")
        .put("Sakhalin Standard Time", "Asia/Sakhalin")
        .put("Norfolk Standard Time", "Pacific/Norfolk")
        .put("Afghanistan Standard Time", "Asia/Kabul")
        .put("Fiji Standard Time", "Pacific/Fiji")
        .put("Central Brazilian Standard Time", "America/Cuiaba")
        .put("Cuba Standard Time", "America/Havana")
        .put("Aleutian Standard Time", "America/Adak")
        .put("Pacific SA Standard Time", "America/Santiago")
        .put("Egypt Standard Time", "Africa/Cairo")
        .put("Arab Standard Time", "Asia/Riyadh")
        .put("Taipei Standard Time", "Asia/Taipei")
        .put("UTC-02", "Etc/GMT+2")
        .put("West Bank Standard Time", "Asia/Hebron")
        .put("Sao Tome Standard Time", "Africa/Sao_Tome")
        .put("Alaskan Standard Time", "America/Anchorage")
        .put("Omsk Standard Time", "Asia/Omsk")
        .put("Eastern Standard Time", "America/New_York")
        .put("Myanmar Standard Time", "Asia/Rangoon")
        .put("Syria Standard Time", "Asia/Damascus")
        .put("Russian Standard Time", "Europe/Moscow")
        .put("Mountain Standard Time (Mexico)", "America/Chihuahua")
        .put("Magadan Standard Time", "Asia/Magadan")
        .put("Iran Standard Time", "Asia/Tehran")
        .put("Marquesas Standard Time", "Pacific/Marquesas")
        .put("Azerbaijan Standard Time", "Asia/Baku")
        .put("E. South America Standard Time", "America/Sao_Paulo")
        .put("Turks And Caicos Standard Time", "America/Grand_Turk")
        .put("UTC-09", "Etc/GMT+9")
        .put("Russia Time Zone 3", "Europe/Samara")
        .put("UTC-08", "Etc/GMT+8")
        .put("E. Africa Standard Time", "Africa/Nairobi")
        .put("Nepal Standard Time", "Asia/Katmandu")
        .put("UTC+12", "Etc/GMT-12")
        .put("Turkey Standard Time", "Europe/Istanbul")
        .put("China Standard Time", "Asia/Shanghai")
        .put("UTC+13", "Etc/GMT-13")
        .put("Mountain Standard Time", "America/Denver")
        .put("West Pacific Standard Time", "Pacific/Port_Moresby")
        .put("AUS Central Standard Time", "Australia/Darwin")
        .put("Newfoundland Standard Time", "America/St_Johns")
        .put("N. Central Asia Standard Time", "Asia/Novosibirsk")
        .put("SA Eastern Standard Time", "America/Cayenne")
        .put("Singapore Standard Time", "Asia/Singapore")
        .put("Vladivostok Standard Time", "Asia/Vladivostok")
        .put("Haiti Standard Time", "America/Port-au-Prince")
        .put("North Asia East Standard Time", "Asia/Irkutsk")
        .put("Sudan Standard Time", "Africa/Khartoum")
        .put("Jordan Standard Time", "Asia/Amman")
        .put("Bangladesh Standard Time", "Asia/Dhaka")
        .put("Qyzylorda Standard Time", "Asia/Qyzylorda")
        .put("Venezuela Standard Time", "America/Caracas")
        .put("Cen. Australia Standard Time", "Australia/Adelaide")
        .put("W. Australia Standard Time", "Australia/Perth")
        .put("Mauritius Standard Time", "Indian/Mauritius")
        .put("Central Standard Time", "America/Chicago")
        .put("Tomsk Standard Time", "Asia/Tomsk")
        .put("Arabian Standard Time", "Asia/Dubai")
        .put("North Korea Standard Time", "Asia/Pyongyang")
        .put("AUS Eastern Standard Time", "Australia/Sydney")
        .put("Namibia Standard Time", "Africa/Windhoek")
        .put("UTC", "Etc/GMT")
        .put("North Asia Standard Time", "Asia/Krasnoyarsk")
        .put("Central America Standard Time", "America/Guatemala")
        .put("Kaliningrad Standard Time", "Europe/Kaliningrad")
        .put("Aus Central W. Standard Time", "Australia/Eucla")
        .put("New Zealand Standard Time", "Pacific/Auckland")
        .put("Volgograd Standard Time", "Europe/Volgograd")
        .put("SA Pacific Standard Time", "America/Bogota")
        .put("Chatham Islands Standard Time", "Pacific/Chatham")
        .put("Cape Verde Standard Time", "Atlantic/Cape_Verde")
        .put("Pacific Standard Time", "America/Los_Angeles")
        .put("US Eastern Standard Time", "America/Indianapolis")
        .put("W. Mongolia Standard Time", "Asia/Hovd")
        .put("Caucasus Standard Time", "Asia/Yerevan")
        .put("Ulaanbaatar Standard Time", "Asia/Ulaanbaatar")
        .put("India Standard Time", "Asia/Calcutta")
        .put("Easter Island Standard Time", "Pacific/Easter")
        .put("E. Europe Standard Time", "Europe/Chisinau")
        .put("W. Central Africa Standard Time", "Africa/Lagos")
        .put("W. Europe Standard Time", "Europe/Berlin")
        .put("Sri Lanka Standard Time", "Asia/Colombo")
        .put("Korea Standard Time", "Asia/Seoul")
        .put("Saratov Standard Time", "Europe/Saratov")
        .put("Tonga Standard Time", "Pacific/Tongatapu")
        .put("Tokyo Standard Time", "Asia/Tokyo")
        .put("Tocantins Standard Time", "America/Araguaina")
        .put("Israel Standard Time", "Asia/Jerusalem")
        .put("Central Standard Time (Mexico)", "America/Mexico_City")
        .put("Bougainville Standard Time", "Pacific/Bougainville")
        .put("Central Asia Standard Time", "Asia/Almaty")
        .put("UTC-11", "Etc/GMT+11")
        .put("US Mountain Standard Time", "America/Phoenix")
        .put("Ekaterinburg Standard Time", "Asia/Yekaterinburg")
        .put("Eastern Standard Time (Mexico)", "America/Cancun")
        .put("Georgian Standard Time", "Asia/Tbilisi")
        .put("Argentina Standard Time", "America/Buenos_Aires")
        .put("Line Islands Standard Time", "Pacific/Kiritimati")
        .put("Hawaiian Standard Time", "Pacific/Honolulu")
        .put("Central Europe Standard Time", "Europe/Budapest")
        .put("GMT Standard Time", "Europe/London")
        .put("West Asia Standard Time", "Asia/Tashkent")
        .put("FLE Standard Time", "Europe/Kiev")
        .put("Canada Central Standard Time", "America/Regina")
        .put("Montevideo Standard Time", "America/Montevideo")
        .put("Central Pacific Standard Time", "Pacific/Guadalcanal")
        .put("Lord Howe Standard Time", "Australia/Lord_Howe")
        .put("South Africa Standard Time", "Africa/Johannesburg")
        .put("Atlantic Standard Time", "America/Halifax")
        .put("Astrakhan Standard Time", "Europe/Astrakhan")
        .put("Paraguay Standard Time", "America/Asuncion")
        .put("Romance Standard Time", "Europe/Paris")
        .put("Greenland Standard Time", "America/Godthab")
        .put("E. Australia Standard Time", "Australia/Brisbane")
        .put("Russia Time Zone 11", "Asia/Kamchatka")
        .put("GTB Standard Time", "Europe/Bucharest")
        .put("Russia Time Zone 10", "Asia/Srednekolymsk")
        .put("Belarus Standard Time", "Europe/Minsk")
        .put("Altai Standard Time", "Asia/Barnaul")
        .put("Morocco Standard Time", "Africa/Casablanca")
        .put("SE Asia Standard Time", "Asia/Bangkok")
        .put("Dateline Standard Time", "Etc/GMT+12")
        .put("Transbaikal Standard Time", "Asia/Chita")
        .put("Middle East Standard Time", "Asia/Beirut")
        .put("Pacific Standard Time (Mexico)", "America/Tijuana")
    .build();

    /**
     * Exchange to Olson timezone mappings
     * Copied from https://github.com/sabre-io/vobject/blob/284f8072bc678387510db663d4c2b148b02f7476/lib/timezonedata/exchangezones.php
     */
    static final Map<String, String> EXCHANGE2OLSON = ImmutableMap.<String, String> builder()
        .put("Universal Coordinated Time", "UTC")
        .put("Casablanca, Monrovia", "Africa/Casablanca")
        .put("Greenwich Mean Time: Dublin, Edinburgh, Lisbon, London", "Europe/Lisbon")
        .put("Greenwich Mean Time; Dublin, Edinburgh, London", "Europe/London")
        .put("Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna", "Europe/Berlin")
        .put("Belgrade, Pozsony, Budapest, Ljubljana, Prague", "Europe/Prague")
        .put("Brussels, Copenhagen, Madrid, Paris", "Europe/Paris")
        .put("Paris, Madrid, Brussels, Copenhagen", "Europe/Paris")
        .put("Prague, Central Europe", "Europe/Prague")
        .put("Sarajevo, Skopje, Sofija, Vilnius, Warsaw, Zagreb", "Europe/Sarajevo")
        .put("West Central Africa", "Africa/Luanda") // This was a best guess
        .put("Athens, Istanbul, Minsk", "Europe/Athens")
        .put("Bucharest", "Europe/Bucharest")
        .put("Cairo", "Africa/Cairo")
        .put("Harare, Pretoria", "Africa/Harare")
        .put("Helsinki, Riga, Tallinn", "Europe/Helsinki")
        .put("Israel, Jerusalem Standard Time", "Asia/Jerusalem")
        .put("Baghdad", "Asia/Baghdad")
        .put("Arab, Kuwait, Riyadh", "Asia/Kuwait")
        .put("Moscow, St. Petersburg, Volgograd", "Europe/Moscow")
        .put("East Africa, Nairobi", "Africa/Nairobi")
        .put("Tehran", "Asia/Tehran")
        .put("Abu Dhabi, Muscat", "Asia/Muscat") // Best guess
        .put("Baku, Tbilisi, Yerevan", "Asia/Baku")
        .put("Kabul", "Asia/Kabul")
        .put("Ekaterinburg", "Asia/Yekaterinburg")
        .put("Islamabad, Karachi, Tashkent", "Asia/Karachi")
        .put("Kolkata, Chennai, Mumbai, New Delhi, India Standard Time", "Asia/Calcutta")
        .put("Kathmandu, Nepal", "Asia/Kathmandu")
        .put("Almaty, Novosibirsk, North Central Asia", "Asia/Almaty")
        .put("Astana, Dhaka", "Asia/Dhaka")
        .put("Sri Jayawardenepura, Sri Lanka", "Asia/Colombo")
        .put("Rangoon", "Asia/Rangoon")
        .put("Bangkok, Hanoi, Jakarta", "Asia/Bangkok")
        .put("Krasnoyarsk", "Asia/Krasnoyarsk")
        .put("Beijing, Chongqing, Hong Kong SAR, Urumqi", "Asia/Shanghai")
        .put("Irkutsk, Ulaan Bataar", "Asia/Irkutsk")
        .put("Kuala Lumpur, Singapore", "Asia/Singapore")
        .put("Perth, Western Australia", "Australia/Perth")
        .put("Taipei", "Asia/Taipei")
        .put("Osaka, Sapporo, Tokyo", "Asia/Tokyo")
        .put("Seoul, Korea Standard time", "Asia/Seoul")
        .put("Yakutsk", "Asia/Yakutsk")
        .put("Adelaide, Central Australia", "Australia/Adelaide")
        .put("Darwin", "Australia/Darwin")
        .put("Brisbane, East Australia", "Australia/Brisbane")
        .put("Canberra, Melbourne, Sydney, Hobart (year 2000 only)", "Australia/Sydney")
        .put("Guam, Port Moresby", "Pacific/Guam")
        .put("Hobart, Tasmania", "Australia/Hobart")
        .put("Vladivostok", "Asia/Vladivostok")
        .put("Magadan, Solomon Is., New Caledonia", "Asia/Magadan")
        .put("Auckland, Wellington", "Pacific/Auckland")
        .put("Fiji Islands, Kamchatka, Marshall Is.", "Pacific/Fiji")
        .put("Nuku'alofa, Tonga", "Pacific/Tongatapu")
        .put("Azores", "Atlantic/Azores")
        .put("Cape Verde Is.", "Atlantic/Cape_Verde")
        .put("Mid-Atlantic", "America/Noronha")
        .put("Brasilia", "America/Sao_Paulo") // Best guess
        .put("Buenos Aires", "America/Argentina/Buenos_Aires")
        .put("Greenland", "America/Godthab")
        .put("Newfoundland", "America/St_Johns")
        .put("Atlantic Time (Canada)", "America/Halifax")
        .put("Atlantic Time", "America/Halifax")
        .put("Caracas, La Paz", "America/Caracas")
        .put("Santiago", "America/Santiago")
        .put("Bogota, Lima, Quito", "America/Bogota")
        .put("Eastern Time (US & Canada)", "America/New_York")
        .put("Eastern Time", "America/New_York")
        .put("Indiana (East)", "America/Indiana/Indianapolis")
        .put("Indiana", "America/Indiana/Indianapolis")
        .put("Central America", "America/Guatemala")
        .put("Central Time (US & Canada)", "America/Chicago")
        .put("Central Time", "America/Chicago")
        .put("Mexico City, Tegucigalpa", "America/Mexico_City")
        .put("Saskatchewan", "America/Edmonton")
        .put("Arizona", "America/Phoenix")
        .put("Mountain Time (US & Canada)", "America/Denver") // Best guess
        .put("Mountain Time", "America/Denver") // Best guess
        .put("Pacific Time (US & Canada)", "America/Los_Angeles") // Best guess
        .put("Pacific Time", "America/Los_Angeles") // Best guess
        .put("Pacific Time (US & Canada); Tijuana", "America/Los_Angeles") // Best guess
        .put("Pacific Time; Tijuana", "America/Los_Angeles") // Best guess
        .put("Alaska", "America/Anchorage")
        .put("Hawaii", "Pacific/Honolulu")
        .put("Midway Island, Samoa", "Pacific/Midway")
        .put("Eniwetok, Kwajalein, Dateline Time", "Pacific/Kwajalein")
    .build();
    // @formatter:on

    /**
     * 
     * Gets the olson timezone identifier for a given timezone identifier, checking all available mappings.
     *
     * @param timezoneId An arbitrary timezone identifier
     * @return an olson timezone identifier
     */
    static String get(String timezoneId) {
        String retval = null;
        retval = WINDOWS2OLSON.get(timezoneId);
        if (retval != null) {
            return retval;
        }
        retval = EXCHANGE2OLSON.get(timezoneId);
        return retval;
    }
}
