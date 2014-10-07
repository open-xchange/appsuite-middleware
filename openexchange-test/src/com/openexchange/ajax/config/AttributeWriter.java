package com.openexchange.ajax.config;

import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;

public abstract class AttributeWriter implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BetaWriter.class);

    private Tree param;
    private final User user;
    private boolean run = true;

    private Throwable t;


    public AttributeWriter(Tree param, User user) {
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
        try {
            // This does a login which also touches the user attributes for the last login time stamp.
            AJAXClient client = new AJAXClient(user);
            while (run) {
                // Touches the user attributes a second time.
                client.execute(new SetRequest(param, getValue()));
            }
            client.logout();
        } catch (Throwable t2) {
            LOG.error(t2.getMessage(), t2);
            t = t2;
        }
    }
    
    protected abstract Object getValue();

}
