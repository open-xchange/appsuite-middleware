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

package com.openexchange.metrics.dropwizard.jmx.beans;

import javax.management.NotCompliantMBeanException;
import com.openexchange.metrics.MetricDescriptor;
import com.openexchange.metrics.dropwizard.types.DropwizardGauge;
import com.openexchange.metrics.jmx.beans.AbstractMetricMBean;
import com.openexchange.metrics.jmx.beans.GaugeMBean;
import com.openexchange.metrics.types.Gauge;

/**
 * {@link GaugeMBeanImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GaugeMBeanImpl extends AbstractMetricMBean implements GaugeMBean {

    private final DropwizardGauge gauge;

    /**
     * Initialises a new {@link GaugeMBeanImpl}.
     * 
     * @param gauge The {@link Gauge} metric
     * @param metricDescriptor The {@link MetricDescriptor}
     * @throws NotCompliantMBeanException
     */
    public GaugeMBeanImpl(DropwizardGauge gauge, MetricDescriptor metricDescriptor) throws NotCompliantMBeanException {
        super(GaugeMBean.class, metricDescriptor);
        this.gauge = gauge;
    }

    @Override
    public Object getValue() {
        return gauge.getValue();
    }

}
