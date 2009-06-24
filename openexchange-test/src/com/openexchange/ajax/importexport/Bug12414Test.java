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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.importexport;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXException.ProblematicAttribute;
import com.openexchange.groupware.AbstractOXException.Truncated;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.RandomString;
import com.openexchange.tools.servlet.AjaxException;

/**
 * Checks if truncation information is properly handled by importer.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug12414Test extends AbstractAJAXSession {

    AJAXClient client;

    int folderId;

    /**
     * Default constructor.
     * @param name test name.
     */
    public Bug12414Test(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        folderId = client.getValues().getPrivateContactFolder();
    }

    public void testTruncation() throws AjaxException, IOException,
        SAXException, JSONException {
        final VCardImportRequest request = new VCardImportRequest(folderId, 
            new ByteArrayInputStream(vCard.getBytes("UTF-8")), false);
        final VCardImportResponse importR = client.execute(request);
        assertEquals("Missing import response.", 1, importR.size());
        final Response response = importR.get(0);
        assertTrue("To long field not detected.", response.hasError());
        final AbstractOXException e = response.getException();
        assertEquals("Truncated information missing.", 1, e.getProblematics().length);
        ProblematicAttribute problem = e.getProblematics()[0];
        assertTrue("Not a truncated problem.", problem instanceof Truncated);
        Truncated truncated = (Truncated) problem;
        assertEquals("Some other attribute is reported as truncated.",
            Contact.DISPLAY_NAME, truncated.getId());
    }

    public static final String vCard =
        "BEGIN:VCARD\n" +
        "VERSION:2.1\n" +
        "FN:" + RandomString.generateChars(321) + '\n' +
        "END:VCARD\n";
}
