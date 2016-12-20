
package com.openexchange.ajax.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.openexchange.ajax.InfostoreAJAXTest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.TestInit;

public class VersionsTest extends InfostoreAJAXTest {

    public VersionsTest() {
        super();
    }

    @Test
    public void testVersions() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        String id = clean.get(0);
        com.openexchange.file.storage.File org = itm.getAction(id);
        org.setVersionComment("Comment 1");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        org.setVersionComment("Comment 2");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        org.setVersionComment("Comment 3");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        com.openexchange.file.storage.File file = itm.getAction(id);
        assertEquals(3,  file.getNumberOfVersions());
    }

    // Bug 13627
    @Test
    public void testVersionSorting() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        String id = clean.get(0);
        com.openexchange.file.storage.File org = itm.getAction(id);
        org.setVersionComment("Comment 1");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        org.setVersionComment("Comment 2");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        org.setVersionComment("Comment 3");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        com.openexchange.file.storage.File reloaded = itm.getAction(id);
        List<com.openexchange.file.storage.File> versions = itm.versions(id, new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION }, Metadata.VERSION, Order.DESCENDING);
        assertFalse(itm.getLastResponse().hasError());

        assureVersions(new Integer[] { 3, 2, 1 }, itm.getLastResponse(), 3);
    }

    @Test
    public void testUniqueVersions() throws Exception {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));

        String id = clean.get(0);
        com.openexchange.file.storage.File org = itm.getAction(id);
        org.setVersionComment("Comment 1");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        org.setVersionComment("Comment 2");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        org.setVersionComment("Comment 3");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        List<com.openexchange.file.storage.File> versions = itm.versions(id, new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION }, Metadata.VERSION, Order.DESCENDING);
        assertFalse(itm.getLastResponse().hasError());

        assureVersions(new Integer[] { 1, 2, 3 }, itm.getLastResponse(), 3);

//      TODO MS re-add
//        final int[] nd = detach(getWebConversation(), getHostName(), sessionId, itm.getLastResponse().getTimestamp().getTime(), clean.get(0), new int[] { 3 });
//        assertEquals(0, nd.length);

        org.setVersionComment("Comment 3");
        itm.updateAction(org, upload, new com.openexchange.file.storage.File.Field[] {com.openexchange.file.storage.File.Field.VERSION_COMMENT}, new Date(Long.MAX_VALUE));
        assertFalse(itm.getLastResponse().hasError());

        versions = itm.versions(id, new int[] { Metadata.VERSION, Metadata.CURRENT_VERSION }, Metadata.VERSION, Order.DESCENDING);
        assertFalse(itm.getLastResponse().hasError());

        assureVersions(new Integer[] { 1, 2, 4 }, itm.getLastResponse(), 4);

    }

    @Test
    public void testLastModifiedUTC() throws JSONException, IOException, SAXException, OXException {
        final File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        
        com.openexchange.file.storage.File toUpdate = itm.getAction(clean.get(0));
        toUpdate.setVersionComment("Comment 1");
        itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.VERSION_COMMENT }, new Date(Long.MAX_VALUE));

        toUpdate.setVersionComment("Comment 2");
        itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.VERSION_COMMENT }, new Date(Long.MAX_VALUE));

        toUpdate.setVersionComment("Comment 3");
        itm.updateAction(toUpdate, new com.openexchange.file.storage.File.Field[] { com.openexchange.file.storage.File.Field.VERSION_COMMENT }, new Date(Long.MAX_VALUE));

        com.openexchange.file.storage.File fileWithVersions = itm.getAction(clean.get(0));
        final int size = fileWithVersions.getNumberOfVersions();
        assertTrue(size > 0);
    }


    public static final void assureVersions(final Integer[] ids, final AbstractAJAXResponse abstractAJAXResponse, final Integer current) throws JSONException{
        final Set<Integer> versions = new HashSet<Integer>(Arrays.asList(ids));
        final JSONArray arrayOfarrays = (JSONArray) abstractAJAXResponse.getData();

        int numberOfVersions = versions.size();
        for(int i = 0; i < arrayOfarrays.length(); i++) {
            final JSONArray comp = arrayOfarrays.getJSONArray(i);
            assertTrue("Didn't expect "+comp.getInt(0), versions.remove(comp.getInt(0)));
            if(current != null && comp.getInt(0) != current) {
                assertFalse(comp.getBoolean(1));
            } else if(current != null){
                assertTrue(comp.getBoolean(1));
            }
        }
        assertEquals(numberOfVersions, arrayOfarrays.length());
        assertTrue(versions.isEmpty());
    }
}
