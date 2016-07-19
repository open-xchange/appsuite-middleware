package com.openexchange.html.bugtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

public class Bug46864Test extends AbstractSanitizing{

    @Test
    public void testNoTagsInImageAttributes() {
        String htmlContent = "<html><head><base href=\"https://uwd1.unitedwayepledge.org/epledge/\"></head><body><img width=\"1\" height=\"1\" src=\"comm/AndarRead.jsp?U=<HexMailUsage>&A=2F3E6D476A366D2B40657E3E&OA=2F3E6D476A366D2B40657E3E&UA=724B372C6E392F602A447E3E\"></body></html>";
        String actual = getHtmlService().checkBaseTag(htmlContent, false);
        String expected = "<html><head></head><body><img width=\"1\" height=\"1\" src=\"https://uwd1.unitedwayepledge.org/epledge/comm/AndarRead.jsp?U=&lt;HexMailUsage&gt;&A=2F3E6D476A366D2B40657E3E&OA=2F3E6D476A366D2B40657E3E&UA=724B372C6E392F602A447E3E\"\"></body></html>";
        assertFalse("The parsed HTML still contains a tag inside the image attribute", actual.contains("<HexMailUsage>"));
        assertEquals("Unexpected output", expected, actual);
    }
}
