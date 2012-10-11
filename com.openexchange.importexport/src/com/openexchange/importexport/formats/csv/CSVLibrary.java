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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
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

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CSVLibrary.class));
    public static final char CELL_DELIMITER = ',';
    public static final char ROW_DELIMITER = '\n';

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
      * Translates a list of column numbers to readable titles.
      * @param cols
      * @return
      */
    public static List<String> convertToList(final int[] cols) {
        final List<String> l = new LinkedList<String>();
        for(final int col : cols){
            l.add( Contacts.mapping[col].getReadableTitle() );
        }
        return l;
    }

    /**
     * ...because Java5, basic data types and Arrays don't mix
     */
    public static Set<Integer> transformIntArrayToSet(final int[] arr) {
        final LinkedHashSet<Integer> s = new LinkedHashSet<Integer>();
        for(final int val : arr){
            s.add(Integer.valueOf(val));
        }
        return s;
    }

    /**
     * ...because Java5, basic data types and Arrays don't mix
     */
    public static int[] transformSetToIntArray(final Set<Integer> s) {
        final int[] ret = new int[s.size()];
        int i = 0;
        for(final Integer val : s){
            ret[i++] = val.intValue();
        }
        return ret;
    }

    public static String transformInputStreamToString(final InputStream is, final String encoding) throws OXException{
    	boolean isUTF8 = encoding.equalsIgnoreCase("UTF-8");

        final InputStreamReader isr;
        try {
            isr = new InputStreamReader(is, encoding);
        } catch (final UnsupportedEncodingException e) {
            LOG.fatal(e);
            throw ImportExportExceptionCodes.UTF8_ENCODE_FAILED.create(e);
        }
        final StringBuilder bob = new StringBuilder();
        boolean firstPartSpecialTreatment = isUTF8;
        try {
            char[] buf = new char[512];
            int length = -1;
            while ((length = isr.read(buf)) != -1) {
            	if(firstPartSpecialTreatment){
            		firstPartSpecialTreatment = false;
            		int offset = lengthOfBOM(buf);
            		bob.append(buf, offset, length);
            	} else {
            		bob.append(buf, 0, length);
            	}
            }
        } catch (final IOException e) {
            throw ImportExportExceptionCodes.IOEXCEPTION.create(e);
        } finally {
            try {
                isr.close();
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return bob.toString();
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
