/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.html.bugtests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;

/**
 * {@link Bug27335Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug27335Test extends AbstractSanitizing {
     @Test
     public void testFormatURL1() {
        String content = "blah http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275 blah";

        String test = getHtmlService().formatURLs(content, "aaa");

        final String regex = "<a[^>]+href=\"([^\"]+)\"[^>]+>(.*?)</a>";
        final Matcher m = Pattern.compile(regex).matcher(test);

        Assert.assertTrue("Anchor not found", m.find());

        String group1 = m.group(1);
        String group2 = m.group(2);
        Assert.assertNotNull(group1);
        Assert.assertNotNull(group2);

        Assert.assertEquals("Unexpected URL", "http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275", group1);
        Assert.assertEquals("Unexpected URL", "http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275", group2);
    }

     @Test
     public void testFormatURL2() {
        String content = "blah (http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275) blah";

        String test = getHtmlService().formatURLs(content, "aaa");

        final String regex = "<a[^>]+href=\"([^\"]+)\"[^>]+>(.*?)</a>";
        final Matcher m = Pattern.compile(regex).matcher(test);

        Assert.assertTrue("Anchor not found", m.find());

        String group1 = m.group(1);
        String group2 = m.group(2);
        Assert.assertNotNull(group1);
        Assert.assertNotNull(group2);

        Assert.assertEquals("Unexpected URL", "http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275", group1);
        Assert.assertEquals("Unexpected URL", "http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275", group2);
    }

     @Test
     public void testFormatURL3() {
        String content = "blah [http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275] blah";

        String test = getHtmlService().formatURLs(content, "aaa");

        final String regex = "<a[^>]+href=\"([^\"]+)\"[^>]+>(.*?)</a>";
        final Matcher m = Pattern.compile(regex).matcher(test);

        Assert.assertTrue("Anchor not found", m.find());

        String group1 = m.group(1);
        String group2 = m.group(2);
        Assert.assertNotNull(group1);
        Assert.assertNotNull(group2);

        Assert.assertEquals("Unexpected URL", "http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275", group1);
        Assert.assertEquals("Unexpected URL", "http://www.ox.io/blub/blab.php?foo=bar&[showUid]=1275", group2);
    }

     @Test
     public void testFormatURL4() {
        String content = "echo \"Re: http://support.open-xchange.com/~karl/hidden/itil/data/Page_1_1_1.htm (change Management)\"";

        String test = getHtmlService().formatURLs(content, "aaa");

        final String regex = "<a[^>]+href=\"([^\"]+)\"[^>]+>(.*?)</a>";
        final Matcher m = Pattern.compile(regex).matcher(test);

        Assert.assertTrue("Anchor not found", m.find());

        String group1 = m.group(1);
        String group2 = m.group(2);
        Assert.assertNotNull(group1);
        Assert.assertNotNull(group2);

        Assert.assertEquals("Unexpected URL", "http://support.open-xchange.com/~karl/hidden/itil/data/Page_1_1_1.htm", group1);
        Assert.assertEquals("Unexpected URL", "http://support.open-xchange.com/~karl/hidden/itil/data/Page_1_1_1.htm", group2);
    }
}
