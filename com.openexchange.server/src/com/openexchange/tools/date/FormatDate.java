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

package com.openexchange.tools.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.openexchange.tools.conf.GlobalConfig;

/**
 * @author Benjamin Otterbach
 * @deprecated this class doesn't work anymore.
 */
@Deprecated
public class FormatDate {

    private final String language;

    private final String country;

    private final String[] originalPatternFormat;

    /**
     * Initial Methode.<BR>
     * Setzt die Standard Formatierung des Datums anhand des Sprach und Laender Codes fest.<BR>
     * <BR>
     * Laender Code = de : Pattern String = dd.MM.yyyy HH:mm:ss<BR>
     * Laender Code = en : Pattern String = <BR>
     * Laender Code = en : Pattern String = <BR>
     * Laender Code = unknown : Pattern String = <BR>
     *
     * @param String Sprach Code
     * @param String Laender Code
     */
    public FormatDate(final String language, final String country) {
        this.language = language;
        this.country = country;

        originalPatternFormat = new String[2];
        originalPatternFormat[0] = GlobalConfig.getDateTimePattern(language);
        originalPatternFormat[1] = GlobalConfig.getDatePattern(language);
    }

    /**
     * Mit dieser Methode kann ein Datum konvertiert werden, wobei das Original Format sowie das Ausgabe Format angegeben werden muss.
     *
     * @param String Das Datum das konvertiert werden soll.
     * @param String Format des Original Datum.
     * @param String Format des gewuenschten Datums.
     * @return String - Formatiertes Datum
     */
    public String formatDate(final String originalDate, String originalPattern, String wantedPattern) throws ParseException {

        if ((originalPattern == null) || (originalPattern.trim().length() <= 0)) {
            originalPattern = "MM.dd.yyyy HH:mm";
        }

        if ((wantedPattern == null) || (wantedPattern.trim().length() <= 0)) {
            wantedPattern = "dd.MM.yyyy HH:mm";
        }

        final Locale l = new Locale(language, country);
        final SimpleDateFormat sdfi = new SimpleDateFormat(originalPattern);
        final SimpleDateFormat sdfo = new SimpleDateFormat(wantedPattern, l);

        return (sdfo.format(sdfi.parse(originalDate)));

    }

    /**
     * Ein Standart Datum wird Postgres gerecht konvertiert.<BR>
     * Rueckgabe Formatierung : "yyyy-dd-MM HH:mm:ss"
     *
     * @param String Das Datum das konvertiert werden soll.
     * @param boolean gibt an ob im Ausgabe Format die Zeitangabe mit enthalten sein soll.
     * @return String - Formatiertes Datum
     */
    public String formatDateForPostgres(final String originalDate, final boolean withTime) throws ParseException {
        int timeCount = 0;
        if (!withTime) {
            timeCount = 1;
        }

        final Locale l = new Locale(language, country);
        final SimpleDateFormat sdfi = new SimpleDateFormat(originalPatternFormat[timeCount]);
        final SimpleDateFormat sdfo = new SimpleDateFormat(GlobalConfig.getDateTimePattern("DATABASE"), l);

        return (sdfo.format(sdfi.parse(originalDate)));
    }

    /**
     * Ein Standart Datum wird Postgres gerecht konvertiert.<BR>
     * Rueckgabe Formatierung : "yyyy-dd-MM HH:mm:ss"
     *
     * @param String Das Datum das konvertiert werden soll.
     * @param boolean gibt an ob im Ausgabe Format die Zeitangabe mit enthalten sein soll.
     * @return String - Formatiertes Datum
     */
    public String formatDateForPostgres(final Date originalDate, final boolean withTime) throws ParseException {
        return formatDateForPostgres(getStringFromDate(originalDate, withTime), withTime);
    }

    /**
     * Wandelt ein Datum in einen String um.
     */
    public String getStringFromDate(final Date d, final boolean time) throws ParseException {
        if (d == null) {
            return null;
        }
        int withTime = 1;
        if (time) {
            withTime = 0;
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        String actDate = cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        actDate = formatDate(actDate, "dd.MM.yyyy HH:mm", originalPatternFormat[withTime]);

        return (actDate);
    }
}
