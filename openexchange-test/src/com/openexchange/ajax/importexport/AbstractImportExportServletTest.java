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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import com.openexchange.importexport.Format;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.common.groupware.importexport.ContactTestData;

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

    @SuppressWarnings("serial")
    public Map<String, String> VCARD_ELEMENTS = new HashMap<String, String>(){{
        //put("PRODID", "-//Open-Xchange//7.8.0-Rev0//EN");
        put("FN", "Prinz\\, Tobias");
        put("N", "Prinz;Tobias");
        put("BDAY", "1981-05-01");
        put("ADR;TYPE=work", ";;;Meinerzhagen;NRW;58540;DE");
        put("TEL;TYPE=home,voice", "+49 2358 7192");
        put("EMAIL", "tobias.prinz@open-xchange.com");
        put("X-SHOESIZE", "9.5");
    }};

    @SuppressWarnings("serial")
    public Map<String, String> VCARD_ELEMENTS_2 = new HashMap<String, String>(){{
        //put("PRODID", "-//Open-Xchange//7.8.0-Rev0//EN");
        put("FN", "Mustermann\\, Max");
        put("N", "Mustermann;Max");
        put("BDAY", "1966-07-30");
        put("ADR;TYPE=work", ";;;Musterstadt;NRW;12345;DE");
        put("TEL;TYPE=home,voice", "+491234567890");
        put("EMAIL", "max.mustermann@example.invalid");
        put("X-SHOESIZE", "6.0");
    }};

    public String getUrl(final String servlet, final int folderId, final Format format) {
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

    @SuppressWarnings("deprecation")
    public String getCSVColumnUrl(final String servlet, final int folderId, final Format format) {
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

        final OCLPermission[] permission = new OCLPermission[] { FolderTestManager.createPermission(ftm.getClient().getValues().getPrivateAppointmentFolder(), false, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION)};
        folderObj.setPermissionsAsArray(permission);

        return ftm.insertFolderOnServer(folderObj).getObjectID();
    }

    protected void removeFolder(final int folderId) throws OXException, Exception {
        if (folderId == -1) {
            return;
        }
        final Calendar cal = GregorianCalendar.getInstance();
        FolderTestManager ftm = new FolderTestManager(getClient());
        ftm.deleteFolderOnServer(folderId, new Date(cal.getTimeInMillis()));
    }

    public static void assertEquals(final String message, final List<?> l1, final List<?> l2) {
        if (l1.size() != l2.size()) {
            fail(message);
        }
        final Set<?> s = new HashSet<>(l1);
        for (final Object o : l2) {
            assertTrue(message, s.remove(o));
        }
    }

    public static JSONObject extractFromCallback(final String html) throws JSONException {
        return new JSONObject(AbstractUploadParser.extractFromCallback(html));
    }

}
