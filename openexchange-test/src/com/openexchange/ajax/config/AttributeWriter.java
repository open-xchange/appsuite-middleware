
package com.openexchange.ajax.config;

import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.test.pool.TestUser;

public abstract class AttributeWriter implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BetaWriter.class);

    private Tree param;

    private final TestUser user;

    private boolean run = true;

    private Throwable t;

    public AttributeWriter(Tree param, TestUser user) {
        super();
        this.param = param;
        this.user = user;
    }

    public void stop() {
        run = false;
    }

    public Throwable getThrowable() {
        return t;
    }

    @Override
    public void run() {
        // This does a login which also touches the user attributes for the last login time stamp.
        AJAXClient client;
        try {
            client = new AJAXClient(user);

            while (run) {
                try {
                    // Touches the user attributes a second time.
                    SetResponse response = client.execute(new SetRequest(param, getValue(), false));
                    OXException exception = response.getException();
                    if (exception != null) {
                        throw exception;
                    }
                } catch (Throwable t) {
                    Throwable handled = handleError(t);
                    if (handled != null) {
                        LOG.error(handled.getMessage(), handled);
                        t = handled;
                        break;
                    }
                }
            }

            client.logout();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            t = e;
        }
    }

    protected abstract Object getValue();

    /**
     * Can be overridden for custom exception handling.
     * 
     * @param t The exception
     * @return The exception to stop execution immediately and to preserve it for {@link AttributeWriter#getThrowable()}.
     *         If <code>null</code>, the execution continues.
     */
    protected Throwable handleError(Throwable t) {
        return t;
    }

}
