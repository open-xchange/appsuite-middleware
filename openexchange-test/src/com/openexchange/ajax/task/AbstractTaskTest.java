/**
 * 
 */
package com.openexchange.ajax.task;

import java.util.TimeZone;

import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession.AJAXSession;
import com.openexchange.configuration.AJAXConfig;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AbstractTaskTest extends AbstractAJAXSession {

    /**
     * Private task folder identifier of the user.
     */
    private int privateTaskFolder;

    /**
     * Time zone of the user.
     */
    private TimeZone timeZone;

    /**
     * @param name
     */
    public AbstractTaskTest(String name) {
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
        timeZone = ConfigTools.getTimeZone(session.getConversation(), AJAXConfig
            .getProperty(AJAXConfig.Property.HOSTNAME), session.getId());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        timeZone = null;
        privateTaskFolder = 0;
        super.tearDown();
    }

    /**
     * @return the private task folder of the user.
     */
    protected int getPrivateTaskFolder() {
        return privateTaskFolder;
    }

    /**
     * @return the time zone of the user.
     */
    protected TimeZone getTimeZone() {
        return timeZone;
    }
}
