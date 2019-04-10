
package com.openexchange.ajax.mail.addresscollector;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AbstractAJAXSession;

public class ConfigurationTest extends AbstractAJAXSession {

    public ConfigurationTest() {
        super();
    }

    @Test
    public void testEnableAttribute() throws Throwable {
        SetRequest setRequest = new SetRequest(Tree.ContactCollectEnabled, Boolean.TRUE);
        SetResponse setResponse = getClient().execute(setRequest);
        assertFalse(setResponse.hasError());

        GetRequest getRequest = new GetRequest(Tree.ContactCollectEnabled);
        GetResponse getResponse = getClient().execute(getRequest);
        assertTrue(getResponse.getBoolean());

        setRequest = new SetRequest(Tree.ContactCollectEnabled, Boolean.FALSE);
        setResponse = getClient().execute(setRequest);
        assertFalse(setResponse.hasError());

        getRequest = new GetRequest(Tree.ContactCollectEnabled);
        getResponse = getClient().execute(getRequest);
        assertFalse(getResponse.getBoolean());
    }

    @Test
    public void testFolderId() throws Throwable {
        SetRequest setRequest = new SetRequest(Tree.ContactCollectFolder, I(100));
        SetResponse setResponse = getClient().execute(setRequest);
        assertFalse(setResponse.hasError());

        GetRequest getRequest = new GetRequest(Tree.ContactCollectFolder);
        GetResponse getResponse = getClient().execute(getRequest);
        assertEquals(100, getResponse.getInteger());

        setRequest = new SetRequest(Tree.ContactCollectFolder, I(123));
        setResponse = getClient().execute(setRequest);
        assertFalse(setResponse.hasError());

        getRequest = new GetRequest(Tree.ContactCollectFolder);
        getResponse = getClient().execute(getRequest);
        assertEquals(123, getResponse.getInteger());
    }
}
