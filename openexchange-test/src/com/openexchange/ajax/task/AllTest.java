/**
 * 
 */
package com.openexchange.ajax.task;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.config.ConfigTools;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.MultipleRequest;
import com.openexchange.ajax.framework.MultipleResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.groupware.tasks.Task;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class AllTest extends AbstractTaskTest2 {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(AllTest.class);

    private TimeZone timeZone;
    
    /**
     * Default constructor.
     * @param name Name of this test.
     */
    public AllTest(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final AJAXSession session = getSession();
        timeZone = ConfigTools.getTimeZone(session.getConversation(), AJAXConfig
            .getProperty(AJAXConfig.Property.HOSTNAME), session.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        timeZone = null;
        super.tearDown();
    }

    public void testAll() throws Throwable {
        final TaskInsertRequest[] inserts = new TaskInsertRequest[10];
        for (int i = 0; i < inserts.length; i++) {
            final Task task = new Task();
            task.setTitle("Task " + (i + 1));
            task.setParentFolderID(getPrivateTaskFolder());
            inserts[i] = new TaskInsertRequest(task, timeZone);
        }
        final MultipleResponse mInsert = (MultipleResponse) AJAXClient.execute(
            getSession(), new MultipleRequest(inserts));
        for (int i = 0; i < inserts.length; i++) {
            final TaskInsertResponse ins = (TaskInsertResponse) mInsert
                .getResponse(i);
            LOG.info(ins.getId());
        }
        // Get for timestamp
        // List

        final TaskDeleteRequest[] deletes = new TaskDeleteRequest[inserts.length];
        for (int i = 0; i < inserts.length; i++) {
            deletes[i] = new TaskDeleteRequest(getPrivateTaskFolder(),
                ((TaskInsertResponse) mInsert.getResponse(i)).getId(),
                new Date());
        }
        final MultipleResponse mDelete = (MultipleResponse) AJAXClient.execute(
            getSession(), new MultipleRequest(deletes)); 
    }
}
