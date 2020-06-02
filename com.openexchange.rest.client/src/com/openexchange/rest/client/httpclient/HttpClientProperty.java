
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

    /**
     * The property for socket read timeout in milliseconds.
     */
    SOCKET_READ_TIMEOUT_MILLIS("readTimeout", I(30000), (config, in) -> config.setSocketReadTimeout(i(in))),
    /**
     * The property for connect timeout in milliseconds.
     */
    CONNECT_TIMEOUT_MILLIS("connectTimeout", I(30000), (config, in) -> config.setConnectTimeout(i(in))),
    /**
     * The property specifying the timeout in milliseconds when waiting for a connection from connection pool.
     */
    CONNECTION_REQUEST_TIMEOUT_MILLIS("connectionRequestTimeout", I(30000), (config, in) -> config.setConnectionRequestTimeout(i(in))),
    /**
     * The property for keep-alive duration.
     */
    KEEP_ALIVE_DURATION_SECS("keepAlive.duration", I(20), (config, in) -> config.setKeepAliveDuration(i(in))),
    /**
     * The property for keep-alive monitor interval.
     */
    KEEP_ALIVE_MONITOR_INTERVAL_SECS("keepAlive.monitorInterval", I(5), (config, in) -> config.setKeepAliveMonitorInterval(i(in))),
    /**
     * The property for maximum number of connections managed in connection pool.
     * <p>
     * If there is more than one host, this setting should be configured so that:<br>
     * <pre>
     *   connectionsPerRoute < totalConnections <= n * connectionsPerRoute.
     * </pre>
     */
    MAX_TOTAL_CONNECTIONS("totalConnections", I(20), (config, in) -> config.setMaxTotalConnections(i(in))),
    /**
     * The property for maximum number of connections per route/host.
     * <p>
     * If there is more than one host, this setting should be configured so that:<br>
     * <pre>
     *   connectionsPerRoute < totalConnections <= n * connectionsPerRoute.
     * </pre>
     */
    MAX_CONNECTIONS_PER_ROUTE("connectionsPerRoute", I(10), (config, in) -> config.setMaxConnectionsPerRoute(i(in))),
    /**
     * The property for socket buffer size in bytes.
     */
    DEFAULT_SOCKET_BUFFER_SIZE("socketBufferSize", I(8192), (config, in) -> config.setSocketBufferSize(i(in))),
    ;

    /** The wild-card name for the properties */
    public final static String SERVICE_IDENTIFIER = "serviceIdentifier";

    private final String fqn;
    private final String defaultName;
    private final Integer value;
    private final transient BiConsumer<HttpBasicConfig, Integer> setter;

    /**
     * Initializes a new {@link HttpClientProperty}.
     */
    private HttpClientProperty(String propNameAppendix, Integer value, BiConsumer<HttpBasicConfig, Integer> setter) {
        StringBuilder sb = new StringBuilder("com.openenexchange.httpclient.");
        int reslen = sb.length();
        this.fqn = sb.append('[').append(SERVICE_IDENTIFIER).append("].").append(propNameAppendix).toString();
        sb.setLength(reslen);
        this.defaultName = sb.append(propNameAppendix).toString();
        this.value = value;
        this.setter = setter;
    }

    @Override
    public Object getDefaultValue() {
        return value;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    /**
     * Gets this instance of a {@link Property} without the placeholder
     * as defined per {@link #SERVICE_IDENTIFIER}
     *
     * @return The property aka. the default property
     */
    public Property getProperty() {
        return DefaultProperty.valueOf(defaultName, value);
    }

    /**
     * Sets the given value to associated field in the given configuration.
     *
     * @param config The configuration to set the value in
     * @param integer The value to set. If <code>null</code>, the default value is set.
     */
    public void setInConfig(HttpBasicConfig config, Integer integer) {
        setter.accept(config, null == integer ? value : integer);
    }
}
