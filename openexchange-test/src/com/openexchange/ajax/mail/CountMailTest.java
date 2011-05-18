package com.openexchange.ajax.mail;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.folder.json.FolderField;
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

    public CountMailTest(final String name) throws ConfigurationException, AjaxException, IOException, SAXException, JSONException {
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


    protected int count(final String folder) throws AjaxException, IOException, JSONException {
        final JSONObject data =
            (JSONObject) client.execute(
                new com.openexchange.ajax.folder.actions.GetRequest(API.OX_NEW, folder, new int[] { FolderField.TOTAL.getColumn() })).getData();
        return data.getInt(FolderField.TOTAL.getName());
    }

}
