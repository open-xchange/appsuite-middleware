package com.openexchange.ajax.mail.addresscollector;

import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.SetResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;

public class ConfigurationTest extends AbstractAJAXSession {

    public ConfigurationTest(final String name) {
        super(name);
    }

    public void testEnableAttribute() throws Throwable {
        final AJAXClient client = getClient();
        SetRequest setRequest = new SetRequest(Tree.ContactCollectEnabled, true);
        SetResponse setResponse = client.execute(setRequest);
        assertFalse(setResponse.hasError());
        
        GetRequest getRequest = new GetRequest(Tree.ContactCollectEnabled);
        GetResponse getResponse = client.execute(getRequest);
        assertTrue(getResponse.getBoolean());
        
        setRequest = new SetRequest(Tree.ContactCollectEnabled, false);
        setResponse = client.execute(setRequest);
        assertFalse(setResponse.hasError());
        
        getRequest = new GetRequest(Tree.ContactCollectEnabled);
        getResponse = client.execute(getRequest);
        assertFalse(getResponse.getBoolean());
    }
    
    public void testFolderId() throws Throwable {
        final AJAXClient client = getClient();
        SetRequest setRequest = new SetRequest(Tree.ContactCollectFolder, 100);
        SetResponse setResponse = client.execute(setRequest);
        assertFalse(setResponse.hasError());
        
        GetRequest getRequest = new GetRequest(Tree.ContactCollectFolder);
        GetResponse getResponse = client.execute(getRequest);
        assertEquals(100, getResponse.getInteger());
        
        setRequest = new SetRequest(Tree.ContactCollectFolder, 123);
        setResponse = client.execute(setRequest);
        assertFalse(setResponse.hasError());
        
        getRequest = new GetRequest(Tree.ContactCollectFolder);
        getResponse = client.execute(getRequest);
        assertEquals(123, getResponse.getInteger());
    }
}
