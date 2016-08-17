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

package com.openexchange.logging.osgi;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyListener;
import com.openexchange.logging.filter.ExceptionCategoryFilter;
import com.openexchange.logging.filter.RankingAwareTurboFilterList;
import com.openexchange.logging.mbean.IncludeStackTraceServiceImpl;


/**
 * {@link ExceptionCategoryFilterRegistererTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ExceptionCategoryFilterRegistererTest {

    @Test
    public void testCorrectFilterHandlingOnConfigServiceAppearance() throws Exception {
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

        Mockito.doReturn("ERROR").when(configMock).getProperty(Mockito.matches("com.openexchange.log.suppressedCategories"), Mockito.matches("USER_INPUT"), (PropertyListener) Mockito.any());
        ecfr.addingService(refMock);
        Assert.assertEquals(1, turboFilterList.size());
        Assert.assertEquals(ExceptionCategoryFilter.getCategories(), "ERROR");

        Mockito.doReturn("TRY_AGAIN").when(configMock).getProperty(Mockito.matches("com.openexchange.log.suppressedCategories"), Mockito.matches("USER_INPUT"), (PropertyListener) Mockito.any());
        ecfr.addingService(refMock);
        Assert.assertEquals(1, turboFilterList.size());
        Assert.assertEquals(ExceptionCategoryFilter.getCategories(), "TRY_AGAIN");
    }

}
