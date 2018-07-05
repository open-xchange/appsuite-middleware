
package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.contact.AbstractManagedContactTest;
import com.openexchange.ajax.importexport.actions.CSVExportRequest;
import com.openexchange.ajax.importexport.actions.CSVExportResponse;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug32200Test}
 *
 * csv-contact-import of categories does not work
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug32200Test extends AbstractManagedContactTest {

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
