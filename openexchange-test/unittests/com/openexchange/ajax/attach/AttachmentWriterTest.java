
package com.openexchange.ajax.attach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.junit.Test;
import com.openexchange.ajax.writer.AttachmentWriter;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

public class AttachmentWriterTest {

    private static final List<AttachmentMetadata> DUMMY_VALUES = new ArrayList<AttachmentMetadata>();
    static {

        AttachmentMetadata m = new AttachmentImpl();

        m.setAttachedId(1);
        m.setCreatedBy(2);
        m.setCreationDate(new Date(230023));
        m.setFileMIMEType("text/plain");
        m.setFilename("test1.txt");
        m.setFilesize(12345);
        m.setFolderId(3);
        m.setId(10);
        m.setModuleId(23);
        m.setRtfFlag(true);

        DUMMY_VALUES.add(m);

        m = new AttachmentImpl(m);
        m.setId(11);
        m.setFilename("test2.txt");

        DUMMY_VALUES.add(m);

        m = new AttachmentImpl(m);
        m.setId(12);
        m.setFilename("test3.txt");
    }

    @Test
    public void testWriteList() throws Exception {
        final StringWriter result = new StringWriter();
        final AttachmentWriter writer = new AttachmentWriter(new JSONWriter(new PrintWriter(result)));

        writer.writeAttachments(new SearchIteratorAdapter(DUMMY_VALUES.iterator()), new AttachmentField[] { AttachmentField.ID_LITERAL, AttachmentField.FILENAME_LITERAL, AttachmentField.CREATION_DATE_LITERAL }, TimeZone.getTimeZone("utc"));

        final JSONArray arrayOfarrays = new JSONArray(result.toString());

        for (int i = 0; i < arrayOfarrays.length(); i++) {
            final JSONArray array = arrayOfarrays.getJSONArray(i);
            assertEquals(10 + i, array.getInt(0));
            assertEquals("test" + (i + 1) + ".txt", array.getString(1));
            assertEquals(230023, array.getLong(2));
        }
    }

    @Test
    public void testWriteObject() throws Exception {
        final StringWriter result = new StringWriter();
        final AttachmentWriter writer = new AttachmentWriter(new JSONWriter(new PrintWriter(result)));

        writer.write(DUMMY_VALUES.get(0), TimeZone.getTimeZone("utc"));

        final JSONObject object = new JSONObject(result.toString());

        assertEquals(1, object.getInt(AttachmentField.ATTACHED_ID_LITERAL.getName()));
        assertEquals(2, object.getInt(AttachmentField.CREATED_BY_LITERAL.getName()));
        assertEquals(3, object.getInt(AttachmentField.FOLDER_ID_LITERAL.getName()));
        assertEquals(10, object.getInt(AttachmentField.ID_LITERAL.getName()));
        assertEquals(23, object.getInt(AttachmentField.MODULE_ID_LITERAL.getName()));
        assertEquals(12345, object.getInt(AttachmentField.FILE_SIZE_LITERAL.getName()));
        assertEquals(230023, object.getInt(AttachmentField.CREATION_DATE_LITERAL.toString()));
        assertEquals("text/plain", object.getString(AttachmentField.FILE_MIMETYPE_LITERAL.getName()));
        assertEquals("test1.txt", object.getString(AttachmentField.FILENAME_LITERAL.getName()));
        assertTrue(object.getBoolean(AttachmentField.RTF_FLAG_LITERAL.getName()));
    }

    @Test
    public void testTimeZone() throws Exception {
        final StringWriter result = new StringWriter();
        final AttachmentWriter writer = new AttachmentWriter(new JSONWriter(new PrintWriter(result)));
        writer.write(DUMMY_VALUES.get(0), TimeZone.getTimeZone("Europe/Berlin"));

        final JSONObject object = new JSONObject(result.toString());

        assertEquals(3830023, object.getInt(AttachmentField.CREATION_DATE_LITERAL.toString()));

    }
}
