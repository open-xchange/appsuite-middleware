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

package com.openexchange.ajax.oauth.provider.protocol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.openexchange.java.util.UUIDs;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractRequest<T extends AbstractRequest<T>> {

    protected String scheme = "https";
    protected String hostname;
    protected int port;
    protected String clientId;
    protected String redirectURI;
    protected String scope;
    protected String state = UUIDs.getUnformattedStringFromRandom();
    protected String responseType = "code";
    protected String sessionId;
    protected final Map<String, String> overrideParams = new HashMap<>();
    protected final Map<String, String> headers = new HashMap<>();

    public T setScheme(String scheme) {
        this.scheme = scheme;
        return (T) this;
    }

    public T setHostname(String hostname) {
        int portIndex = hostname.indexOf(':');
        if (portIndex > 0) {
            this.hostname = hostname.substring(0, portIndex);
            this.port = Integer.parseInt(hostname.substring(portIndex + 1));
        } else {
            this.hostname = hostname;
        }
        return (T) this;
    }

    public T setPort(int port) {
        this.port = port;
        return (T) this;
    }

    public T setClientId(String clientId) {
        this.clientId = clientId;
        return (T) this;
    }

    public T setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
        return (T) this;
    }

    public T setScope(String scope) {
        this.scope = scope;
        return (T) this;
    }

    public T setState(String state) {
        this.state = state;
        return (T) this;
    }

    public T setResponseType(String responseType) {
        this.responseType = responseType;
        return (T) this;
    }

    public T setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return (T) this;
    }

    /**
     * Sets a parameter. Parameters set via this method take precedence over named parameters
     * set via other <code>set[ParamName]</code> methods. If the passed value is <code>null</code>,
     * the parameter will be omitted.
     *
     * @param value The parameter value
     * @param name The parameter name
     */
    public T setParameter(String name, String value) {
        overrideParams.put(name, value);
        return (T) this;
    }

    /**
     * Sets an HTTP header. If the passed value is <code>null</code>,
     * the header will be omitted.
     *
     * @param name The name
     * @param value The value
     */
    public T setHeader(String name, String value) {
        headers.put(name, value);
        return (T) this;
    }

    List<NameValuePair> prepareParams() {
        return prepareParams(null);
    }

    List<NameValuePair> prepareParams(Map<String, String> requestSpecific) {
        Map<String, String> formParams = new HashMap<>();
        if (clientId != null) {
            formParams.put("client_id", clientId);
        }
        if (redirectURI != null) {
            formParams.put("redirect_uri", redirectURI);
        }
        if (scope != null) {
            formParams.put("scope", scope);
        }
        if (state != null) {
            formParams.put("state", state);
        }
        if (responseType != null) {
            formParams.put("response_type", responseType);
        }
        if (sessionId != null) {
            formParams.put("session", sessionId);
        }

        if (requestSpecific != null) {
            for (String param : requestSpecific.keySet()) {
                formParams.put(param, requestSpecific.get(param));
            }
        }

        for (Entry<String, String> param : overrideParams.entrySet()) {
            String name = param.getKey();
            String value = param.getValue();
            if (value == null) {
                formParams.remove(name);
            } else {
                formParams.put(name, value);
            }
        }

        LinkedList<NameValuePair> loginFormParams = new LinkedList<>();
        for (Entry<String, String> param : formParams.entrySet()) {
            loginFormParams.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return loginFormParams;
    }

}
