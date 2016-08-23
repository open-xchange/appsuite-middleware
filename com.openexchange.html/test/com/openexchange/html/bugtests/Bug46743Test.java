package com.openexchange.html.bugtests;

import static org.junit.Assert.assertEquals;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.StringUtil;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

public class Bug46743Test extends AbstractSanitizing {
    
    @Test
    public void testInsuficentParanthesesAtEndOfComment() {
        String content = "<style type=\"text/css\">\n"+
    "<!--\n"+   
        "@media screen and (max-width: 440px) {\n"+
            "@media only screen and (max-device-width: 440px) and (-webkit-min-device-pixel-ratio: 1) {\n"+
                ".recoMobile {\n"+
                   "width: 50% !important;\n"+
                "}\n"+
            "}\n"+
            "-->\n"+
    "</style>";
        String test = getHtmlService().sanitize(content, null, true, null, null);
        assertEquals(true, StringUtils.countMatches(test, "{") == StringUtils.countMatches(test, "}"));
        assertEquals(true, test.endsWith("--> \n</style>"));
    }

}
