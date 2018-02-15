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

package com.openexchange.ipcheck.countrycode.mbean;

import java.util.Set;
import com.openexchange.metrics.AbstractMetricCollector;
import com.openexchange.metrics.MetricMetadata;
import com.openexchange.metrics.MetricType;

/**
 * {@link IPCheckMetricCollector}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class IPCheckMetricCollector extends AbstractMetricCollector {

    private static final String COMPONENT_NAME = "ipcheck";

    /**
     * Initialises a new {@link IPCheckMetricCollector}.
     * 
     * @param componentName
     */
    public IPCheckMetricCollector() {
        super(COMPONENT_NAME);
        Set<MetricMetadata> metricMetadata = getMetricMetadata();
        metricMetadata.add(new MetricMetadata(MetricType.METER, "meter.totalIPChanges"));
        metricMetadata.add(new MetricMetadata(MetricType.METER, "meter.acceptedIPChanges"));
        metricMetadata.add(new MetricMetadata(MetricType.METER, "meter.deniedIPChanges"));
        metricMetadata.add(new MetricMetadata(MetricType.METER, "meter.acceptedPrivateIP"));
        metricMetadata.add(new MetricMetadata(MetricType.METER, "meter.acceptedWhiteListed"));
        metricMetadata.add(new MetricMetadata(MetricType.METER, "meter.acceptedEligibleIPChanges"));
        metricMetadata.add(new MetricMetadata(MetricType.METER, "meter.deniedException"));
        metricMetadata.add(new MetricMetadata(MetricType.METER, "meter.deniedCountryChanged"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.MetricCollector#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        // TODO check from config
        return true;
    }
}
