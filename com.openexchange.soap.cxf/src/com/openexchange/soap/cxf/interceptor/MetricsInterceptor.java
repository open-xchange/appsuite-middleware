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

package com.openexchange.soap.cxf.interceptor;

import java.time.Duration;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;


/**
 * {@link MetricsInterceptor}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class MetricsInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final String UNKNOWN = "UNKNOWN";
    private static final String STATUS_OK = "OK";
    private static final String STATUS_FAULT_PREFIX = "FAULT_";
    private static final String REQUEST_START_TIME = "com.openexchange.soap.requestStartTime";


    /**
     * Initializes a new {@link MetricsInterceptor}.
     * @param phase The phase during which this interceptor is called
     */
    public MetricsInterceptor(String phase) {
        super(phase);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if (MessageUtils.isOutbound(message)) {
            if (MessageUtils.isFault(message)) {
                recordFault(message);
            } else {
                recordSuccess(message);
            }
        } else {
            rememberStartTime(message);
        }
    }

    /**
     * Records a failed request that is responded to with a SOAP Fault
     */
    private void recordFault(Message message) {
        recordRequest(message, getFaultStatus(message));
    }

    /**
     * Records a successful request that is responded to with a message body
     */
    private void recordSuccess(Message message) {
        recordRequest(message, STATUS_OK);
    }

    /**
     * Records a completed request with given status
     *
     * @param message
     * @param status
     */
    private void recordRequest(Message message, String status) {
        Sample startTime = getStartTime(message);
        if (startTime == null) {
            return;
        }

        String service = getService(message);
        String operation = getOperation(message);
        startTime.stop(getTimer(service, operation, status));
    }

    /**
     * Stores a current date within the request message
     */
    private void rememberStartTime(Message message) {
        message.putIfAbsent(REQUEST_START_TIME, Timer.start());
    }

    /**
     * Gets a previously recorded start time from a request or response message.
     * In case of a response message, the date is obtained via {@code message.getExchange().getInMessage()}.
     *
     * @param message
     * @return The {@link Sample} or <code>null</code>
     */
    private Sample getStartTime(Message message) {
        Message inMessage = message;
        if (MessageUtils.isOutbound(message)) {
            Exchange exchange = message.getExchange();
            if (exchange != null) {
                inMessage = exchange.getInMessage();
            }
        }

        if (inMessage == null) {
            return null;
        }

        return (Sample) inMessage.get(REQUEST_START_TIME);
    }

    /**
     * Gets the invoked service name
     *
     * @param message
     * @return The service or {@value #UNKNOWN}
     */
    private String getService(Message message) {
        MessageInfo messageInfo = getMessageInfo(message);
        if (messageInfo == null) {
            return UNKNOWN;
        }

        OperationInfo operation = messageInfo.getOperation();
        if (operation == null) {
            return UNKNOWN;
        }

        InterfaceInfo iface = operation.getInterface();
        if (iface == null) {
            return UNKNOWN;
        }

        ServiceInfo service = iface.getService();
        if (service == null) {
            return UNKNOWN;
        }

        return service.getName().getLocalPart();
    }

    /**
     * Gets the invoked operation name
     *
     * @param message
     * @return The operation or {@value #UNKNOWN}
     */
    private String getOperation(Message message) {
        MessageInfo messageInfo = getMessageInfo(message);
        if (messageInfo == null) {
            return UNKNOWN;
        }

        OperationInfo operation = messageInfo.getOperation();
        if (operation == null) {
            return UNKNOWN;
        }

        return operation.getName().getLocalPart();
    }

    /**
     * Gets the {@link MessageInfo} from a given {@link Message}.
     * In case of a response message, the date is obtained via {@code message.getExchange().getInMessage()}.
     *
     * @param message
     * @return The instance or <code>null</code>
     */
    private MessageInfo getMessageInfo(Message message) {
        if (message == null) {
            return null;
        }

        Message inMessage = message;
        if (MessageUtils.isOutbound(message)) {
            Exchange exchange = message.getExchange();
            if (exchange != null) {
                inMessage = exchange.getInMessage();
            }

            if (inMessage == null) {
                inMessage = message;
            }
        }

        return inMessage.get(MessageInfo.class);
    }

    /**
     * Obtains the status tag value from a SOAP Fault response message
     */
    private String getFaultStatus(Message message) {
        Exception exception = message.getContent(Exception.class);
        if (exception instanceof Fault) {
            return STATUS_FAULT_PREFIX + ((Fault) exception).getFaultCode().getLocalPart().toUpperCase();
        }

        return STATUS_FAULT_PREFIX + UNKNOWN;
    }

    /**
     * Gets the timer for the given service and operation
     *
     * @param service The requested service
     * @param operation The requested operation
     * @return The timer
     */
    private Timer getTimer(String service, String operation, String status) {
        // @formatter:off
        return Timer.builder("appsuite.soapapi.requests")
            .description("Records the timing of the soap calls.")
            .serviceLevelObjectives(
                Duration.ofMillis(50),
                Duration.ofMillis(100),
                Duration.ofMillis(150),
                Duration.ofMillis(200),
                Duration.ofMillis(250),
                Duration.ofMillis(300),
                Duration.ofMillis(400),
                Duration.ofMillis(500),
                Duration.ofMillis(750),
                Duration.ofSeconds(1),
                Duration.ofSeconds(2),
                Duration.ofSeconds(5),
                Duration.ofSeconds(10),
                Duration.ofSeconds(30),
                Duration.ofMinutes(1))
            .tags("service", service, "operation", operation, "status", status)
            .register(Metrics.globalRegistry);
        // @formatter:on
    }

}
