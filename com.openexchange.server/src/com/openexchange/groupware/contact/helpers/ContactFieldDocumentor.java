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

package com.openexchange.groupware.contact.helpers;

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactFieldDocumentor {

    private static final Pattern PATTERN = Pattern.compile("([a-zA-Z0-9]+)_(\\w)([a-zA-Z0-9]+)");

    /**
     * Entry point
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        try (Formatter formatter = new Formatter()) {
            ContactField[] fields = ContactField.values();
            Arrays.sort(fields, (o1, o2) -> o1.getNumber() - o2.getNumber());
            System.out.println(formatter.format("%3s %38s %38s%n", "#", "Ajax name", "OXMF name"));
            for (ContactField field : fields) {
                System.out.println(formatter.format("%3s %38s %38s%n", I(field.getNumber()), field.getAjaxName(), oxmf(field.getAjaxName())));

            }
        }
    }

    private static String oxmf(String ajaxName) {
        Matcher matcher = PATTERN.matcher(ajaxName);
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        while (matcher.find()) {
            found = true;
            sb.append(matcher.group(1));
            sb.append(matcher.group(2).toUpperCase());
            sb.append(matcher.group(3));
        }
        if (!found) {
            return ajaxName;
        }
        return sb.toString();
    }

}
