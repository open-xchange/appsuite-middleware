/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.publish.json;

import static com.openexchange.json.JSONAssertion.assertValidates;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.json.JSONAssertion;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;

/**
 * {@link PublicationWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationWriterTest extends TestCase {
    private Publication publication;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        PublicationTarget target = new PublicationTarget();
        target.setId("com.openexchange.publish.test");

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("siteName", "Site Name")).add(FormElement.checkbox("protected", "Protected"));
        target.setFormDescription(form);

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("siteName", "publication");
        config.put("protected", true);

        publication = new Publication();
        publication.setId(23);
        publication.setEntityId("12");
        publication.setModule("oranges");
        publication.setTarget(target);
        publication.setConfiguration(config);
        publication.setDisplayName("myName");
        publication.setEnabled(true);
    }

    public void testWriteObject() throws JSONException, OXException {
        PublicationWriter writer = new PublicationWriter();
        JSONObject object = writer.write(publication, null, TimeZone.getTimeZone("utc"));

        JSONAssertion assertion = new JSONAssertion()
                .hasKey("id").withValue(23)
                .hasKey("entity").withValueObject()
                    .hasKey("folder").withValue("12")
                    .objectEnds()
                .hasKey("enabled").withValue(true)
                .hasKey("entityModule").withValue("oranges")
                .hasKey("target").withValue("com.openexchange.publish.test")
                .hasKey("displayName").withValue("myName")
                .hasKey("com.openexchange.publish.test").withValueObject()
                    .hasKey("siteName").withValue("publication")
                    .hasKey("protected").withValue(true)
                    .objectEnds()
                .objectEnds();


       assertValidates(assertion, object);
    }

    public void testWriteArray() throws JSONException, OXException {
        Map<String, String[]> specialCols = new HashMap<String, String[]>();
        String[] basicCols = new String[] { "id", "target", "displayName", "enabled" };
        specialCols.put("com.openexchange.publish.test", new String[] { "siteName" });

        JSONArray array = new PublicationWriter().writeArray(
            publication,
            basicCols,
            specialCols,
            Arrays.asList("com.openexchange.publish.test"), publication.getTarget().getFormDescription(), TimeZone.getTimeZone("utc"));

        JSONAssertion assertion = new JSONAssertion().isArray().withValues(23, "com.openexchange.publish.test", "myName", true, "publication");

        assertValidates(assertion, array);

    }

    public void testThrowsExceptionOnUnknownColumn() {
        try {
            new PublicationWriter().writeArray(publication, new String[]{"id", "unknownColumn"}, new HashMap<String, String[]>(), Arrays.asList("com.openexchange.publish.test"), publication.getTarget().getFormDescription(), TimeZone.getTimeZone("utc"));
            fail("Should have failed");
        } catch (OXException e) {

        } catch (JSONException e) {

        }
    }
}
