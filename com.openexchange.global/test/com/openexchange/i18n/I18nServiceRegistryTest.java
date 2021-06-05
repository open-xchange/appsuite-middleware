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

package com.openexchange.i18n;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.i18n.internal.I18nServiceRegistryImpl;

/**
 * {@link I18nServiceRegistryTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class I18nServiceRegistryTest {

    @Test
    public void testMapping() {

        I18nService en_US = PowerMockito.mock(I18nService.class);
        PowerMockito.when(en_US.getLocale()).thenReturn(new Locale("en", "US"));
        I18nService es_ES = PowerMockito.mock(I18nService.class);
        PowerMockito.when(es_ES.getLocale()).thenReturn(new Locale("es", "ES"));
        I18nService es_MX = PowerMockito.mock(I18nService.class);
        PowerMockito.when(es_MX.getLocale()).thenReturn(new Locale("es", "MX"));

        I18nServiceRegistryImpl.getInstance().addI18nService(en_US);
        I18nServiceRegistryImpl.getInstance().addI18nService(es_ES);
        I18nServiceRegistryImpl.getInstance().addI18nService(es_MX);

        I18nService bestFittingI18nService = I18nServiceRegistryImpl.getInstance().getBestFittingI18nService(new Locale("es", "US"));
        Assert.assertEquals(es_MX, bestFittingI18nService);
    }

}
