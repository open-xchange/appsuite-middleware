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

package com.openexchange.regional.impl.test;

import static com.openexchange.java.Autoboxing.c;
import static org.junit.Assert.assertEquals;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.impl.service.RegionalSettingsImpl;
import com.openexchange.regional.impl.service.RegionalSettingsServiceImpl;

/**
 * {@link SeparatorTest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class SeparatorTest {

    /**
     * Initialises a new {@link SeparatorTest}.
     */
    public SeparatorTest() {
        super();
    }

    /**
     * Tests the grouping separators
     */
    @Test
    public void testGroupingSeparators() throws Exception {
        RegionalSettings settings = RegionalSettingsImpl.newBuilder().withNumberFormat("1.234,56").build();
        DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols(Locale.US);
        unusualSymbols.setDecimalSeparator(c(Whitebox.invokeMethod(RegionalSettingsServiceImpl.class, "getDecimalSeparator", settings)));
        unusualSymbols.setGroupingSeparator(c(Whitebox.invokeMethod(RegionalSettingsServiceImpl.class, "getGroupingSeparator", settings)));

        String format = "#,###.###";
        DecimalFormat decimalFormat = new DecimalFormat(format, unusualSymbols);
        assertEquals("1.234,56", decimalFormat.format(1234.56));
        assertEquals("123.456,78", decimalFormat.format(123456.78));
        assertEquals("1.234.567.891.234,568", decimalFormat.format(1234567891234.56789));
        assertEquals("1.234.567.891.234,567", decimalFormat.format(1234567891234.567189));
    }
}
