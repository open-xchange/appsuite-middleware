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

package com.openexchange.ipcheck.countrycode.osgi;

import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.ipcheck.spi.IPChecker;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.ipcheck.countrycode.CountryCodeIpChecker;
import com.openexchange.ipcheck.countrycode.mbean.IPCheckMBean;
import com.openexchange.ipcheck.countrycode.mbean.IPCheckMBeanImpl;
import com.openexchange.ipcheck.countrycode.mbean.IPCheckMetricCollector;
import com.openexchange.management.ManagementService;
import com.openexchange.metrics.MetricService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;

/**
 * {@link CountryCodeIpCheckerActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class CountryCodeIpCheckerActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(CountryCodeIpCheckerActivator.class);
    private IPCheckMBeanImpl metricsMBean;

    /**
     * Initializes a new {@link CountryCodeIpCheckerActivator}.
     */
    public CountryCodeIpCheckerActivator() {
        super();
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { GeoLocationService.class, ManagementService.class, TimerService.class, MetricService.class, SessiondService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        CountryCodeIpChecker service = new CountryCodeIpChecker(this, new IPCheckMetricCollector(getServiceSafe(MetricService.class)));
        registerService(IPChecker.class, service);

        ObjectName objectName = new ObjectName(IPCheckMBean.NAME);

        metricsMBean = new IPCheckMBeanImpl(this, service);
        ManagementService managementService = getServiceSafe(ManagementService.class);
        managementService.registerMBean(objectName, metricsMBean);
        LOG.info("Bundle successfully started: {}", context.getBundle().getSymbolicName());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.osgi.HousekeepingActivator#stopBundle()
     */
    @Override
    protected void stopBundle() throws Exception {
        unregisterService(IPChecker.class);

        if (metricsMBean != null) {
            metricsMBean.stop();
        }
        ObjectName objectName = new ObjectName(IPCheckMBean.NAME);
        ManagementService managementService = getServiceSafe(ManagementService.class);
        managementService.unregisterMBean(objectName);
        super.stopBundle();
        LOG.info("Bundle successfully stopped: {}", context.getBundle().getSymbolicName());
    }
}
