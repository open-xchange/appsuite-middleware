package com.openexchange.tools.versit.valuedefinitions.rfc2445;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.ReaderScanner;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.ValueDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.ListValueDefinition;

/**
 * 
 * {@link TextValueDefinitionTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.0
 */
public class ListValueDefinitionTest {

    private ValueDefinition listValueDefinition;

    @Before
    public void setUp() throws Exception {
        this.listValueDefinition = new ListValueDefinition(new Character(';'), new TextValueDefinition());
    }

    @Test
    public void testCreateValue_adressLineProvided_returnLine() throws IOException {
        String text = ";;c/o BESSY\r\nAlbert-Einstein-Strasse 15;Berlin;;12489;Deutschland";

        List<String> createValue = (List<String>) this.listValueDefinition.createValue(new StringScanner(new ReaderScanner(new InputStreamReader(new ByteArrayInputStream(text.getBytes()))), text), new Property("ADR"));

        String join = StringUtils.join(createValue, ';');

        Assert.assertEquals(text, join);
    }

    @Test
    public void testCreateValue_adressWithEscaeRProvided_returnLineWithEscapedR() throws IOException {
        String text = ";;c/o BESSY\r\nAlbert-Einstein-Strasse 15;Berlin;;12489;Deutschland";

        List<String> createValue = (List<String>) this.listValueDefinition.createValue(new StringScanner(new ReaderScanner(new InputStreamReader(new ByteArrayInputStream(text.getBytes()))), text), new Property("ADR"));

        String join = StringUtils.join(createValue, ';');

        Assert.assertTrue(join.contains("\r"));
    }

    @Test (expected=VersitException.class)
    public void testCreateValue_invalidEscapeSequence_throwException() throws IOException {
        String text = ";;c/o BESSY\\z\nAlbert-Einstein-Strasse 15;Berlin;;12489;Deutschland";
        this.listValueDefinition.createValue(new StringScanner(new ReaderScanner(new InputStreamReader(new ByteArrayInputStream(text.getBytes()))), text), new Property("ADR"));
    }
}
