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

package com.openexchange.importexport.formats.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * This is a library with little helpers needed when preparing
 * the parsing of a CSV file.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public final class CSVLibrary {

    private CSVLibrary() {
        super();
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CSVLibrary.class);
    public static final char CELL_DELIMITER = ',';
    public static final char ROW_DELIMITER = '\n';

    /**
     * Maps {@link ContactField}s to their readable titles. Taken over from previous mapping
     * <code>com.openexchange.groupware.contact.Contacts.mapping</code>.
     */
    private static final EnumMap<ContactField, String> READABLE_TITLES;
    static {
        EnumMap<ContactField, String> readableTitles = new EnumMap<ContactField, String>(ContactField.class);
        readableTitles.put(ContactField.OBJECT_ID, "Object id");
        readableTitles.put(ContactField.CREATED_BY, "Created by");
        readableTitles.put(ContactField.MODIFIED_BY, "Modified by");
        readableTitles.put(ContactField.CREATION_DATE, "Creation date");
        readableTitles.put(ContactField.LAST_MODIFIED, "Changing date");
        readableTitles.put(ContactField.FOLDER_ID, "Folder id");
        readableTitles.put(ContactField.CATEGORIES, "Categories");
        readableTitles.put(ContactField.UID, "UID");
        readableTitles.put(ContactField.DISPLAY_NAME, "Display name");
        readableTitles.put(ContactField.GIVEN_NAME, "Given name");
        readableTitles.put(ContactField.SUR_NAME, "Sur name");
        readableTitles.put(ContactField.MIDDLE_NAME, "Middle name");
        readableTitles.put(ContactField.SUFFIX, "Suffix");
        readableTitles.put(ContactField.TITLE, "Title");
        readableTitles.put(ContactField.STREET_HOME, "Street home");
        readableTitles.put(ContactField.POSTAL_CODE_HOME, "Postal code home");
        readableTitles.put(ContactField.CITY_HOME, "City home");
        readableTitles.put(ContactField.STATE_HOME, "State home");
        readableTitles.put(ContactField.COUNTRY_HOME, "Country home");
        readableTitles.put(ContactField.BIRTHDAY, "Birthday");
        readableTitles.put(ContactField.MARITAL_STATUS, "Marital status");
        readableTitles.put(ContactField.NUMBER_OF_CHILDREN, "Number of children");
        readableTitles.put(ContactField.PROFESSION, "Profession");
        readableTitles.put(ContactField.NICKNAME, "Nickname");
        readableTitles.put(ContactField.SPOUSE_NAME, "Spouse name");
        readableTitles.put(ContactField.ANNIVERSARY, "Anniversary");
        readableTitles.put(ContactField.NOTE, "Note");
        readableTitles.put(ContactField.DEPARTMENT, "Department");
        readableTitles.put(ContactField.POSITION, "Position");
        readableTitles.put(ContactField.EMPLOYEE_TYPE, "Employee type");
        readableTitles.put(ContactField.ROOM_NUMBER, "Room number");
        readableTitles.put(ContactField.STREET_BUSINESS, "Street business");
        readableTitles.put(ContactField.POSTAL_CODE_BUSINESS, "Postal code business");
        readableTitles.put(ContactField.CITY_BUSINESS, "City business");
        readableTitles.put(ContactField.STATE_BUSINESS, "State business");
        readableTitles.put(ContactField.COUNTRY_BUSINESS, "Country business");
        readableTitles.put(ContactField.NUMBER_OF_EMPLOYEE, "Employee ID");
        readableTitles.put(ContactField.SALES_VOLUME, "Sales volume");
        readableTitles.put(ContactField.TAX_ID, "Tax id");
        readableTitles.put(ContactField.COMMERCIAL_REGISTER, "Commercial register");
        readableTitles.put(ContactField.BRANCHES, "Branches");
        readableTitles.put(ContactField.BUSINESS_CATEGORY, "Business category");
        readableTitles.put(ContactField.INFO, "Info");
        readableTitles.put(ContactField.MANAGER_NAME, "Manager's name");
        readableTitles.put(ContactField.ASSISTANT_NAME, "Assistant's name");
        readableTitles.put(ContactField.STREET_OTHER, "Street other");
        readableTitles.put(ContactField.CITY_OTHER, "City other");
        readableTitles.put(ContactField.POSTAL_CODE_OTHER, "Postal code other");
        readableTitles.put(ContactField.COUNTRY_OTHER, "Country other");
        readableTitles.put(ContactField.TELEPHONE_BUSINESS1, "Telephone business 1");
        readableTitles.put(ContactField.TELEPHONE_BUSINESS2, "Telephone business 2");
        readableTitles.put(ContactField.FAX_BUSINESS, "FAX business");
        readableTitles.put(ContactField.TELEPHONE_CALLBACK, "Telephone callback");
        readableTitles.put(ContactField.TELEPHONE_CAR, "Telephone car");
        readableTitles.put(ContactField.TELEPHONE_COMPANY, "Telephone company");
        readableTitles.put(ContactField.TELEPHONE_HOME1, "Telephone home 1");
        readableTitles.put(ContactField.TELEPHONE_HOME2, "Telephone home 2");
        readableTitles.put(ContactField.FAX_HOME, "FAX home");
        readableTitles.put(ContactField.CELLULAR_TELEPHONE1, "Cellular telephone 1");
        readableTitles.put(ContactField.CELLULAR_TELEPHONE2, "Cellular telephone 2");
        readableTitles.put(ContactField.TELEPHONE_OTHER, "Telephone other");
        readableTitles.put(ContactField.FAX_OTHER, "FAX other");
        readableTitles.put(ContactField.EMAIL1, "Email 1");
        readableTitles.put(ContactField.EMAIL2, "Email 2");
        readableTitles.put(ContactField.EMAIL3, "Email 3");
        readableTitles.put(ContactField.URL, "URL");
        readableTitles.put(ContactField.TELEPHONE_ISDN, "Telephone ISDN");
        readableTitles.put(ContactField.TELEPHONE_PAGER, "Telephone pager");
        readableTitles.put(ContactField.TELEPHONE_PRIMARY, "Telephone primary");
        readableTitles.put(ContactField.TELEPHONE_RADIO, "Telephone radio");
        readableTitles.put(ContactField.TELEPHONE_TELEX, "Telephone telex");
        readableTitles.put(ContactField.TELEPHONE_TTYTDD, "Telephone TTY/TDD");
        readableTitles.put(ContactField.INSTANT_MESSENGER1, "Instantmessenger 1");
        readableTitles.put(ContactField.INSTANT_MESSENGER2, "Instantmessenger 2");
        readableTitles.put(ContactField.TELEPHONE_IP, "Telephone IP");
        readableTitles.put(ContactField.TELEPHONE_ASSISTANT, "Telephone assistant");
        readableTitles.put(ContactField.COMPANY, "Company");
        readableTitles.put(ContactField.USERFIELD01, "Dynamic Field 1");
        readableTitles.put(ContactField.USERFIELD02, "Dynamic Field 2");
        readableTitles.put(ContactField.USERFIELD03, "Dynamic Field 3");
        readableTitles.put(ContactField.USERFIELD04, "Dynamic Field 4");
        readableTitles.put(ContactField.USERFIELD05, "Dynamic Field 5");
        readableTitles.put(ContactField.USERFIELD06, "Dynamic Field 6");
        readableTitles.put(ContactField.USERFIELD07, "Dynamic Field 7");
        readableTitles.put(ContactField.USERFIELD08, "Dynamic Field 8");
        readableTitles.put(ContactField.USERFIELD09, "Dynamic Field 9");
        readableTitles.put(ContactField.USERFIELD10, "Dynamic Field 10");
        readableTitles.put(ContactField.USERFIELD11, "Dynamic Field 11");
        readableTitles.put(ContactField.USERFIELD12, "Dynamic Field 12");
        readableTitles.put(ContactField.USERFIELD13, "Dynamic Field 13");
        readableTitles.put(ContactField.USERFIELD14, "Dynamic Field 14");
        readableTitles.put(ContactField.USERFIELD15, "Dynamic Field 15");
        readableTitles.put(ContactField.USERFIELD16, "Dynamic Field 16");
        readableTitles.put(ContactField.USERFIELD17, "Dynamic Field 17");
        readableTitles.put(ContactField.USERFIELD18, "Dynamic Field 18");
        readableTitles.put(ContactField.USERFIELD19, "Dynamic Field 19");
        readableTitles.put(ContactField.USERFIELD20, "Dynamic Field 20");
        readableTitles.put(ContactField.DISTRIBUTIONLIST, "Distribution list");
        readableTitles.put(ContactField.CONTEXTID, "Context id");
        readableTitles.put(ContactField.NUMBER_OF_DISTRIBUTIONLIST, "Number of distributionlists");
        readableTitles.put(ContactField.STATE_OTHER, "State other");
        readableTitles.put(ContactField.DEFAULT_ADDRESS, "Default address");
        readableTitles.put(ContactField.YOMI_FIRST_NAME, "Yomi First Name");
        readableTitles.put(ContactField.YOMI_LAST_NAME, "Yomi Last Name");
        readableTitles.put(ContactField.YOMI_COMPANY, "Yomi Company");
        readableTitles.put(ContactField.HOME_ADDRESS, "Home Address");
        readableTitles.put(ContactField.BUSINESS_ADDRESS, "Business Address");
        readableTitles.put(ContactField.OTHER_ADDRESS, "Other Address");
        READABLE_TITLES = readableTitles;
    }

    /**
     * Translates the folder number for a certain user to a FolderObject
     *
     * @param sessObj The user's session
     * @param folder The folder, usually a number, but for mails it really might be a string
     * @return
     * @throws OXException - if could not be loaded
     */
    public static FolderObject getFolderObject(final ServerSession sessObj, final String folder) throws OXException {
        final int folderId = getFolderId(folder);
        FolderObject fo = null;
        try {
            fo = new OXFolderAccess(sessObj.getContext()).getFolderObject(folderId);
        } catch (final OXException e) {
            throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(e, folder);
        }
        return fo;
    }

    /**
     * ...because OX throws OXExceptions not NumberFormatExceptions
     *
     * @param folderString
     * @return
     * @throws OXException
     */
    public static int getFolderId(final String folderString) throws OXException {
        try{
            return Integer.parseInt(folderString);
        } catch (final NumberFormatException e) {
            throw ImportExportExceptionCodes.NUMBER_FAILED.create(e, folderString);
        }
    }

     /**
      * Translates a list of contact fields to readable titles.
      *
      * @param fields The fields to convert
      * @return The readable titles as list
      */
    public static List<String> convertToList(ContactField[] fields) {
        final List<String> l = new LinkedList<String>();
        for(ContactField field : fields) {
            l.add(READABLE_TITLES.get(field));
        }
        return l;
    }

    public static String transformInputStreamToString(final InputStream is, final String encoding, final boolean close) throws OXException{
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(is, encoding);
            final StringBuilder bob = new StringBuilder(8192);
            boolean isUTF8 = encoding.equalsIgnoreCase("UTF-8");
            boolean firstPartSpecialTreatment = isUTF8;
            final char[] buf = new char[512];
            int length = -1;
            while ((length = isr.read(buf)) > 0) {
            	if(firstPartSpecialTreatment){
            		firstPartSpecialTreatment = false;
            		int offset = lengthOfBOM(buf);
            		bob.append(buf, offset, length - offset);
            	} else {
            		bob.append(buf, 0, length);
            	}
            }
            return bob.toString();
        } catch (final UnsupportedEncodingException e) {
            LOG.error("", e);
            throw ImportExportExceptionCodes.UTF8_ENCODE_FAILED.create(e);
        } catch (final IOException e) {
            if ("Bad file descriptor".equals(e.getMessage())) {
                // Stream is already closed
                throw ImportExportExceptionCodes.IOEXCEPTION_RETRY.create(e);
            }
            throw ImportExportExceptionCodes.IOEXCEPTION.create(e);
        } finally {
            if (close) {
                Streams.close(isr);
            }
        }
    }

    /**
     * Reads one or more lines from the supplied input stream.
     *
     * @param inputStream The input stream to read from
     * @param charset The charset to use
     * @param close <code>true</code> to close the input stream after reading, <code>false</code>, otherwise
     * @param maxLines The maximum number of lines to read, or <code>-1</code> to read all available lines
     * @return The read data
     */
    public static String readLines(InputStream inputStream, Charset charset, boolean close, int maxLines) throws OXException{
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, charset);
            StringBuilder stringBuilder = new StringBuilder(8192);
            /*
             * special handling for UTF-8 byte order mark (taken over from previous implementation)
             */
            if (Charsets.UTF_8_NAME.equalsIgnoreCase(charset.name())) {
                char[] buf = new char[8];
                int length = -1;
                if ((length = inputStreamReader.read(buf)) > 0) {
                    int offset = lengthOfBOM(buf);
                    stringBuilder.append(buf, offset, length - offset);
                }
            }
            /*
             * read line by line
             */
            int lines = 0;
            BufferedReader reader = new BufferedReader(inputStreamReader);
            for (String line = reader.readLine(); null != line; line = reader.readLine()) {
                stringBuilder.append(line).append('\n');
                if (0 < maxLines && ++lines >= maxLines) {
                    break;
                }
            }
            return stringBuilder.toString();
        } catch (UnsupportedEncodingException e) {
            LOG.error("", e);
            throw ImportExportExceptionCodes.UTF8_ENCODE_FAILED.create(e);
        } catch (IOException e) {
            if ("Bad file descriptor".equals(e.getMessage())) {
                // Stream is already closed
                throw ImportExportExceptionCodes.IOEXCEPTION_RETRY.create(e);
            }
            throw ImportExportExceptionCodes.IOEXCEPTION.create(e);
        } finally {
            if (close) {
                Streams.close(inputStreamReader);
            }
        }
    }

	private static int lengthOfBOM(char[] buf) {
		int length = buf.length;

		if(length > 3) {
            if(Character.getNumericValue(buf[0]) < 0 && Character.getNumericValue(buf[1]) < 0 && Character.getNumericValue(buf[2]) < 0 && Character.getNumericValue(buf[3]) < 0){
				if(Character.getType(buf[0]) == 15 && Character.getType(buf[1]) == 15 && Character.getType(buf[2]) == 28 && Character.getType(buf[3]) == 28) {
                    return 4;
                }
				if(Character.getType(buf[0]) == 28 && Character.getType(buf[1]) == 28 && Character.getType(buf[2]) == 15 && Character.getType(buf[3]) == 15) {
                    return 4;
                }
			}
        }
		if(length > 1) {
            if(Character.getNumericValue(buf[0]) < 0 && Character.getNumericValue(buf[1]) < 0) {
                if(Character.getType(buf[0]) == 28 && Character.getType(buf[1]) == 28) {
                    return 2;
                }
            }
        }
		if(length > 0) {
            if(Character.getNumericValue(buf[0]) < 0) {
                if(Character.getType(buf[0]) == 16) {
                    return 1;
                }
            }
        }

		return 0;
	}
}
