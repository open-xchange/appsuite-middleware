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

package com.openexchange.logging.osgi;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.logging.filter.ExceptionCategoryFilter;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;
import com.openexchange.logging.internal.IncludeStackTraceServiceImpl;


/**
 * {@link ExceptionCategoryFilterRegistererTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ExceptionCategoryFilterRegistererTest {

     @Test
     public void testCorrectFilterHandlingOnConfigServiceAppearance() {
        /*
         * When ConfigurationService appears, ExceptionCategoryFilter shall be registered.
         * When ConfigurationService appears again, the old filter shall be removed and registered again.
         */
        BundleContext bundleContext = Mockito.mock(BundleContext.class);

        RankingAwareTurboFilterList turboFilterList = new RankingAwareTurboFilterList();

        ExceptionCategoryFilterRegisterer ecfr = new ExceptionCategoryFilterRegisterer(bundleContext, turboFilterList, new IncludeStackTraceServiceImpl());
        Assert.assertEquals(0, turboFilterList.size());

        ConfigurationService configMock = Mockito.mock(ConfigurationService.class);
        ServiceReference<ConfigurationService> refMock = Mockito.mock(ServiceReference.class);
        Mockito.doReturn(configMock).when(bundleContext).getService(refMock);

        Mockito.doReturn("ERROR").when(configMock).getProperty(Mockito.matches("com.openexchange.log.suppressedCategories"), Mockito.matches("USER_INPUT"));
        ecfr.addingService(refMock);
        Assert.assertEquals(1, turboFilterList.size());
        Assert.assertEquals(ExceptionCategoryFilter.getCategoriesAsString(), "ERROR");

        Mockito.doReturn("TRY_AGAIN").when(configMock).getProperty(Mockito.matches("com.openexchange.log.suppressedCategories"), Mockito.matches("USER_INPUT"));
        ecfr.addingService(refMock);
        Assert.assertEquals(1, turboFilterList.size());
        Assert.assertEquals(ExceptionCategoryFilter.getCategoriesAsString(), "TRY_AGAIN");
    }

}
