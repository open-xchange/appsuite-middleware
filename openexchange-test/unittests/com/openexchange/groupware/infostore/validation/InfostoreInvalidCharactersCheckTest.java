
package com.openexchange.groupware.infostore.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;

public class InfostoreInvalidCharactersCheckTest {

    public static class TestValidator extends InvalidCharactersValidator {

        List<String> strings = new ArrayList<String>();

        @Override
        public String check(final String s) {
            strings.add(s);
            return s;
        }

    }

    @Test
    public void testSwitcher() {
        final DocumentMetadata metadata = new DocumentMetadataImpl();
        metadata.setCategories("categories");
        metadata.setDescription("description");
        metadata.setFileMD5Sum("sum");
        metadata.setFileMIMEType("mimetype");
        metadata.setFileName("name");
        metadata.setTitle("title");
        metadata.setURL("url");
        metadata.setVersionComment("comment");

        final DocumentMetadataValidation validation = new TestValidator().validate(null, metadata, null, null);

        assertFalse(validation.isValid());

        final Metadata[] fields = new Metadata[] { Metadata.CATEGORIES_LITERAL, Metadata.DESCRIPTION_LITERAL, Metadata.FILE_MD5SUM_LITERAL, Metadata.FILE_MIMETYPE_LITERAL, Metadata.FILENAME_LITERAL, Metadata.TITLE_LITERAL, Metadata.URL_LITERAL, Metadata.VERSION_COMMENT_LITERAL };

        final GetSwitch get = new GetSwitch(metadata);
        for (final Metadata m : fields) {
            assertTrue(validation.hasErrors(m));
            assertEquals(m.doSwitch(get), validation.getError(m));
        }

    }
}
