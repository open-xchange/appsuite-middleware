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

import java.util.Map;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;


/**
 * {@link PublicationParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationParserTest extends TestCase {

    private JSONObject object;
    private SimPublicationTargetDiscoveryService discovery;
    private PublicationTarget target;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        target = new PublicationTarget();
        target.setId("com.openexchange.publish.test");

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("siteName", "Site Name")).add(FormElement.checkbox("protected", "Protected"));
        target.setFormDescription(form);


        discovery = new SimPublicationTargetDiscoveryService();
        discovery.addTarget(target);

        object = new JSONObject();

        object.put("id", 12);

        JSONObject entity = new JSONObject();
        entity.put("id", 23);
        entity.put("folder", 42);

        object.put("entity", entity);
        object.put("entityModule", "oranges");
        object.put("target", "com.openexchange.publish.test");


        JSONObject config = new JSONObject();
        config.put("siteName", "publication");
        config.put("protected", true);

        object.put("com.openexchange.publish.test", config);

        object.put("enabled", false);

    }

    public void testParse() throws JSONException, OXException, OXException {
        PublicationParser publicationParser = new PublicationParser(discovery);
        Publication publication = publicationParser.parse(object);

        assertEquals("id was wrong", 12, publication.getId());
        assertEquals("entityId was wrong", "42", publication.getEntityId());
        assertEquals("entityModule was wrong", "oranges", publication.getModule());

        assertNotNull("target was null", publication.getTarget());
        assertEquals("wrong target", target, publication.getTarget());

        Map<String, Object> config = publication.getConfiguration();

        assertNotNull("config was null", config);
        assertEquals("siteName was wrong", "publication", config.get("siteName"));

        assertEquals("enabled was wrong", true, publication.containsEnabled());
        assertEquals("enabled was wrong", false, publication.isEnabled());

    }
}
