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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ContactTestData;
import com.openexchange.importexport.formats.Format;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;
import com.openexchange.webdav.xml.FolderTest;

/**
 * Test of the ImporterExporter servlet. This class serves as library for all
 * derived tests.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public abstract class AbstractImportExportServletTest extends AbstractAJAXSession {

    //private SessionObject sessObj;
    public String FOLDER_NAME = "csv-contact-roundtrip-ajax-test";

    public String IMPORTED_CSV = ContactTestData.IMPORT_MULTIPLE;
    public String EXPORT_SERVLET = "export";
    public String IMPORT_SERVLET = "import";

    /* @formatter:off */
    public String IMPORT_VCARD =
          "BEGIN:VCARD\n"
        + "VERSION:3.0\n"
        + "PRODID:-//Open-Xchange//7.8.0-Rev0//EN\n"
        + "FN:Prinz\\, Tobias\n"
        + "N:Prinz;Tobias;;;\n"
        + "NICKNAME:Tierlieb\n"
        + "BDAY:19810501\n"
        + "ADR;TYPE=work:;;;Meinerzhagen;NRW;58540;DE\n"
        + "TEL;TYPE=home,voice:+49 2358 7192\n"
        + "EMAIL:tobias.prinz@open-xchange.com\n"
        + "ORG:- deactivated -\n"
        + "REV:20061204T160750.018Z\n"
        + "URL:www.tobias-prinz.de\n"
        + "UID:80@ox6.netline.de\n"
        + "X-SHOESIZE:9.5\n"
        + "END:VCARD\n";

    public String IMPORT_VCARD_2 =
          "BEGIN:VCARD\n"
        + "VERSION:3.0\n"
        + "PRODID:-//Open-Xchange//7.8.0-Rev0//EN\n"
        + "FN:Mustermann\\, Max\n"
        + "N:Mustermann;Max;;;\n"
        + "BDAY:19660730\n"
        + "ADR;TYPE=work:;;;Musterstadt;NRW;12345;DE\n"
        + "TEL;TYPE=home,voice:+491234567890\n"
        + "EMAIL:max.mustermann@example.invalid\n"
        + "REV:20061204T160750.018Z\n"
        + "URL:www.example.invalid\n"
        + "X-SHOESIZE:6.0\n"
        + "END:VCARD\n";
    
    public Map<String, String> VCARD_ELEMENTS = new HashMap<String, String>(){{
        //put("PRODID", "-//Open-Xchange//7.8.0-Rev0//EN");
        put("FN", "Prinz\\, Tobias");
        put("N", "Prinz;Tobias;;;");
        put("BDAY", "1981-05-01");
        put("ADR;TYPE=work", ";;;Meinerzhagen;NRW;58540;DE");
        put("TEL;TYPE=home,voice", "+49 2358 7192");
        put("EMAIL", "tobias.prinz@open-xchange.com");
        put("X-SHOESIZE", "9.5");
    }};
    
    public Map<String, String> VCARD_ELEMENTS_2 = new HashMap<String, String>(){{
        //put("PRODID", "-//Open-Xchange//7.8.0-Rev0//EN");
        put("FN", "Mustermann\\, Max");
        put("N", "Mustermann;Max;;;");
        put("BDAY", "1966-07-30");
        put("ADR;TYPE=work", ";;;Musterstadt;NRW;12345;DE");
        put("TEL;TYPE=home,voice", "+491234567890");
        put("EMAIL", "max.mustermann@example.invalid");
        put("X-SHOESIZE", "6.0");
    }};

    public String getUrl(final String servlet, final int folderId, final Format format) throws IOException, JSONException, OXException {
        final StringBuilder bob = new StringBuilder("http://");
        bob.append(getClient().getHostname());
        bob.append("/ajax/");
        bob.append(servlet);
        bob.append("?session=");
        bob.append(getSession().getId());
        addParam(bob, AJAXServlet.PARAMETER_FOLDERID, folderId);
        addParam(bob, AJAXServlet.PARAMETER_ACTION, format.getConstantName());
        return bob.toString();
    }

    public String getCSVColumnUrl(final String servlet, final int folderId, final Format format) throws IOException, OXException, JSONException {
        final StringBuilder bob = new StringBuilder(getUrl(servlet, folderId, format));

        addParam(bob, AJAXServlet.PARAMETER_COLUMNS, ContactField.GIVEN_NAME.getNumber() + "," + ContactField.EMAIL1.getNumber() + "," + ContactField.DISPLAY_NAME.getNumber());
        return bob.toString();
    }

    protected void addParam(final StringBuilder bob, final String param, final String value) {
        bob.append('&');
        bob.append(param);
        bob.append('=');
        bob.append(value);
    }

    protected void addParam(final StringBuilder bob, final String param, final int value) {
        addParam(bob, param, Integer.toString(value));
    }

    protected int createFolder(final String title, final int folderObjectModuleID) throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName(title);
        folderObj.setParentFolderID(FolderObject.PRIVATE);
        folderObj.setModule(folderObjectModuleID);
        folderObj.setType(FolderObject.PRIVATE);

        final OCLPermission[] permission = new OCLPermission[] { FolderTestManager.createPermission(getClient().getValues().getPrivateAppointmentFolder(), false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),};
        folderObj.setPermissionsAsArray(permission);
        
        return ftm.insertFolderOnServer(folderObj).getObjectID();
    }

    protected void removeFolder(final int folderId) throws OXException, Exception {
        if (folderId == -1) {
            return;
        }
        FolderTest.deleteFolder(getClient().getSession().getConversation(), new int[] { folderId }, getClient().getHostname(), testUser.getLogin(), testUser.getPassword());
    }

    public static void assertEquals(final String message, final List l1, final List l2) {
        if (l1.size() != l2.size()) {
            fail(message);
        }
        final Set s = new HashSet(l1);
        for (final Object o : l2) {
            assertTrue(message, s.remove(o));
        }
    }
    
    public static JSONObject extractFromCallback(final String html) throws JSONException {
        return new JSONObject(AbstractUploadParser.extractFromCallback(html));
    }

}
