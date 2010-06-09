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
        String bug = "BEGIN:VCARD\n" + 
            "\n" + 
            "VERSION:3.0\n" + 
            "\n" + 
            "N:Lšfflad;Klaus;(piraten);;\n" + 
            "\n" + 
            "FN:Klaus (piraten) Lšfflad\n" + 
            "\n" + 
            "EMAIL;type=INTERNET;type=HOME;type=pref:klaus@der-kapitaen.de\n" + 
            "\n" + 
            "TEL;type=CELL;type=pref:+49 151 22632571\n" + 
            "\n" + 
            "item1.URL;type=pref:http\\://wiki.piratenpartei.de/Benutzer\\:Magister_Navis\n" + 
            "\n" + 
            "item1.X-ABLabel:Piraten\n" + 
            "\n" + 
            "END:VCARD\n" + 
             "\n";
        
        InputStream is = new ByteArrayInputStream(bug.getBytes());
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
