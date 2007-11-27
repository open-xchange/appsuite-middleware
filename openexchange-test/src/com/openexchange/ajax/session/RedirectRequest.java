/**
 * 
 */
package com.openexchange.ajax.session;

import com.openexchange.ajax.AJAXServlet;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RedirectRequest extends AbstractRequest {

    private final String jvmRoute;

    private final String random;

    /**
     * @param random
     */
    public RedirectRequest(final String jvmRoute, final String random) {
        super(new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
                .ACTION_REDIRECT),
            new Parameter("random", random)
        });
        this.jvmRoute = jvmRoute;
        this.random = random;
    }

    /**
     * {@inheritDoc}
     */
    public RedirectResponseParser getParser() {
        return new RedirectResponseParser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServletPath() {
        return super.getServletPath() + ";jsessionid=abc." + jvmRoute;
    }
}
