/**
 * 
 */
package com.openexchange.ajax.session;

import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXResponse;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class LoginResponse extends AJAXResponse {

    private String sessionId;

    private String random;

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
    public void setRandom(String random) {
        this.random = random;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
