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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import com.openexchange.logback.extensions.appenders.AbstractRemoteAppender;
import com.openexchange.logback.extensions.appenders.CommonAppenderProperty;
import com.openexchange.logback.extensions.appenders.kafka.KafkaAppender;
import com.openexchange.logback.extensions.appenders.kafka.KafkaAppenderMBean;

/**
 * {@link KafkaAppenderMBeanRegisterer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class KafkaAppenderMBeanRegisterer extends AbstractRemoteAppenderMBeanRegisterer {

    private static final String APPENDER_NAME = "Kafka";

    /**
     * Initialises a new {@link KafkaAppenderMBeanRegisterer}.
     */
    public KafkaAppenderMBeanRegisterer(BundleContext context) {
        super(context, APPENDER_NAME);
    }

    @Override
    String getEnabledPropertyName() {
        return CommonAppenderProperty.mbeanEnabled.getPropertyName("kafka");
    }

    @Override
    AbstractRemoteAppender<?> getRemoteAppender() {
        return KafkaAppender.getInstance();
    }

    @Override
    ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName(KafkaAppenderMBean.DOMAIN, KafkaAppenderMBean.KEY, KafkaAppenderMBean.VALUE);
    }
}
