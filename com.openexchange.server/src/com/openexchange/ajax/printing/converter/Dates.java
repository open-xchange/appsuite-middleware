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

package com.openexchange.ajax.printing.converter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * {@link Dates}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Dates {
    
    private final Locale locale;
    
    public Dates(Locale locale) {
        this.locale = locale;
    }
    
    public String format(Date d, String format) {
        int style = 0;
        if (format.equalsIgnoreCase("short")) {
            style = SimpleDateFormat.SHORT;
        } else if (format.equals("medium")) {
            style = SimpleDateFormat.MEDIUM;
        } else if (format.equals("long")) {
            style = SimpleDateFormat.LONG;
        } else if (format.equals("full")) {
            style = SimpleDateFormat.FULL;
        } else if (format.equals("default")) {
            style = SimpleDateFormat.DEFAULT;
        } else {
            return new SimpleDateFormat(format, locale).format(d);
        }
        
        
        return SimpleDateFormat.getDateInstance(style, locale).format(d);
    }
    
    public String format(long d, String format) {
        return format(new Date(d), format);
    }
    
    
}
