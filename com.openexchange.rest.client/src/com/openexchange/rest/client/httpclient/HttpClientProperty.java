
package com.openexchange.rest.client.httpclient;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.function.BiConsumer;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.Property;

/**
 * {@link HttpClientProperty} - Properties for different services that requires a HTTP client
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public enum HttpClientProperty implements Property {

    SOCKET_READ_TIMEOUT_MILLIS("readTimeout", I(30000), (config, in) -> config.setSocketReadTimeout(i(in))),
    CONNTECTION_TIMEOUT_MILLIS("connectTimeout", I(30000), (config, in) -> config.setConnectionTimeout(i(in))),
    CONNECTION_REQUEST_TIMEOUT_MILLIS("connectionRequestTimeout", I(30000), (config, in) -> config.setConnectionRequestTimeout(i(in))),
    KEEP_ALIVE_DURATION_SECS("keepAlive.duration", I(20), (config, in) -> config.setKeepAliveDuration(i(in))),
    KEEP_ALIVE_MONITOR_INTERVAL_SECS("keepAlive.monitorInterval", I(5), (config, in) -> config.setKeepAliveMonitorInterval(i(in))),
    MAX_TOTAL_CONNECTIONS("totalConnections", I(20), (config, in) -> config.setMaxTotalConnections(i(in))),
    MAX_CONNECTIONS_PER_ROUTE("connectionsPerRoute", I(10), (config, in) -> config.setMaxConnectionsPerRoute(i(in))),
    DEFAULT_SOCKET_BUFFER_SIZE("socketBufferSize", I(8192), (config, in) -> config.setSocketBufferSize(i(in)));

    /** The wildcard name for the properties */
    public final static String SERVICE_IDENTIFIER = "serviceIdentifer";
    public final static String PREFIX = "com.openenexchange.httpclient.";

    private final String name;
    private final Integer value;
    private final BiConsumer<HttpBasicConfig, Integer> setter;

    /**
     * Initializes a new {@link HttpClientProperty}.
     */
    private HttpClientProperty(String name, Integer value, BiConsumer<HttpBasicConfig, Integer> setter) {
        this.name = name;
        this.value = value;
        this.setter = setter;
    }

    @Override
    public Object getDefaultValue() {
        return value;
    }

    @Override
    public String getFQPropertyName() {
        //@formatter:off
        return new StringBuilder(PREFIX)
            .append('[')
            .append(SERVICE_IDENTIFIER)
            .append(']')
            .append('.')
            .append(name)
            .toString();
        //@formatter:on
    }

    /**
     * Gets this instance of a {@link Property} without the placeholder
     * as defined per {@link #SERVICE_IDENTIFIER}
     *
     * @return The property aka the default property
     */
    public Property getProperty() {
        return DefaultProperty.valueOf(PREFIX + name, value);
    }

    /**
     * Sets the given value to associated field in the given config
     *
     * @param config The config to set the value in
     * @param integer The value to set. If <code>null</code>, the default value is set.
     */
    public void setInConfig(HttpBasicConfig config, Integer integer) {
        setter.accept(config, null == integer ? value : integer);
    }
}
