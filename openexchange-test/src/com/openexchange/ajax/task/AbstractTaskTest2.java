/**
 * 
 */
package com.openexchange.ajax.task;

import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.configuration.AJAXConfig;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AbstractTaskTest2 extends AbstractAJAXSession {

    private int privateTaskFolder;

    /**
     * @param name
     */
    public AbstractTaskTest2(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final AJAXSession session = getSession();
        privateTaskFolder = TaskTools.getPrivateTaskFolder(session
            .getConversation(), AJAXConfig.getProperty(AJAXConfig.Property
            .HOSTNAME), session.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        privateTaskFolder = 0;
        super.tearDown();
    }

    /**
     * @return the privateTaskFolder
     */
    protected int getPrivateTaskFolder() {
        return privateTaskFolder;
    }
}
