package com.openexchange.ajax.mail;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.mail.actions.CountRequest;
import com.openexchange.ajax.mail.actions.CountResponse;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.tools.servlet.AjaxException;

/**
 * 
 * {@link CountMailTest} - tests the CountRequest 
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class CountMailTest extends AbstractMailTest {

    protected String folder;

    public CountMailTest(String name) throws ConfigurationException, AjaxException, IOException, SAXException, JSONException {
        super(name);
        this.client = new AJAXClient(User.User2);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        folder = getSentFolder();
    }

    @Override
    public void tearDown() throws Exception {
        clearFolder(folder);
        super.tearDown();
    }
    
    public void testCounting() throws Exception {
        clearFolder(folder);
        assertEquals("Should be empty", 0, count(folder) );

        for(int number = 1; number < 10; number++){
            sendMail(generateMail());
            assertEquals("Does not contain the expected number of elements in folder "+folder, number, count(folder) );    
        }
        
        clearFolder(folder);
        assertEquals("Should be empty again", 0, count(folder) );
    }


    protected int count(String folder) throws AjaxException, IOException, SAXException, JSONException {
        CountResponse response = client.execute( new CountRequest(folder) );
        return response.getCount();
    }

}
