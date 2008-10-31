/**
 * 
 */
package com.openexchange.ajax.session.actions;

import com.openexchange.ajax.AJAXServlet;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RedirectRequest extends AbstractRequest<RedirectResponse> {

    private final String jvmRoute;

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
