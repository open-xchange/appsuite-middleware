package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.test.TestException;


public class Bug15229Test extends AbstractVCardImportTest {
    
    public Bug15229Test(String name) throws Exception {
        super(name);
    }
    
    /**
     * Test escaped colons in URLs
     */
    public void testColons() throws TestException, SAXException, JSONException, Exception {
        InputStream is = new FileInputStream("testData/Bug15229.vcf");
        byte[] buf = new byte[1024];
        is.read(buf);
        List<String> folders = new ArrayList<String>(); 
        

         ImportResult[] importResult = importVCard(
                getWebConversation(),
                new ByteArrayInputStream(buf),
                contactFolderId,
                timeZone,
                emailaddress,
                getHostName(),
                getSessionId());

        assertNull("Parsing Exception", importResult[0].getException());
        
    }

}
