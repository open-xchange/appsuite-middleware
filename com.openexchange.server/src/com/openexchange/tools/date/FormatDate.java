/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
