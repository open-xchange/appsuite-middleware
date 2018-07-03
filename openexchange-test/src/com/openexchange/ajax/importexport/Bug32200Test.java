
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.CSVExportRequest;
import com.openexchange.ajax.importexport.actions.CSVExportResponse;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.ajax.importexport.actions.CSVImportResponse;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug32200Test}
 *
 * csv-contact-import of categories does not work
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug32200Test extends AbstractManagedContactTest {

    /**
     * Initializes a new {@link Bug32200Test}.
     *
     * @param name The test name
     */
    public Bug32200Test() {
        super();
    }

    @Test
    public void testImportCategories() throws Exception {
        String categories = "Wichtig,Firma,Neu";
        String csv = "\"Sur name\",\"Given name\",\"Email 1\",\"Categories\"\n" + "\"Walter\",\"Otto\",\"otto.walter@example.com\",\"" + categories + "\"\n";
        CSVImportRequest request = new CSVImportRequest(folderID, new ByteArrayInputStream(csv.getBytes()), false);
        CSVImportResponse response = getClient().execute(request);
        assertFalse("response has error", response.hasError());
        JSONArray data = (JSONArray) response.getData();
        assertNotNull("got no data", data);
        assertEquals(1, data.length());
        Contact contact = cotm.getAction(folderID, data.getJSONObject(0).getInt("id"));
        assertNotNull("imported contact not found", contact);
        assertNotNull("no categories imported", contact.getCategories());
        assertEquals("wrong categories imported", categories, contact.getCategories());
    }

    @Test
    public void testExportCategories() throws Exception {
        String categories = "Unwichtig,Privat,Alt";
        Contact contact = generateContact(getClass().getName());
        contact.setCategories(categories);
        cotm.newAction(contact);
        CSVExportResponse csvExportResponse = getClient().execute(new CSVExportRequest(folderID));
        String csv = String.valueOf(csvExportResponse.getData());
        assertNotNull("no data exported", csv);
        assertTrue("categories not exported", csv.contains(categories));
    }

}
