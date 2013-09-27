/**
 *
 */
package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginResponse extends AbstractAJAXResponse {

    private String sessionId;

    private String random;

    private String jvmRoute;

    private String password;

    /**
     * @param response
     */
    public LoginResponse(final Response response) {
        super(response);
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @return the random
     */
    public String getRandom() {
        return random;
    }

    /**
     * @param random the random to set
     */
    public void setRandom(final String random) {
        this.random = random;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the jvmRoute
     */
    public String getJvmRoute() {
        return jvmRoute;
    }

    /**
     * @param jvmRoute the jvmRoute to set
     */
    public void setJvmRoute(final String jvmRoute) {
        this.jvmRoute = jvmRoute;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }

}
