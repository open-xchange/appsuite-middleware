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

package com.openexchange.carddav;

import java.util.Date;
import java.util.List;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.webdav.protocol.WebdavPath;


/**
 * {@link Tools}
 *
 * Provides some utility functions.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Tools {

    /**
     * The OAuth scope token for CardDAV
     */
    public static final String OAUTH_SCOPE = "carddav";

    /**
     * Extracts the UID part from the supplied {@link WebdavPath}, i.e. the
     * path's name without the <code>.vcf</code> extension.
     *
     * @param path the path
     * @return the name
     */
    public static String extractUID(WebdavPath path) {
    	if (null == path) {
    		throw new IllegalArgumentException("path");
    	}
    	return extractUID(path.name());
    }

    /**
     * Extracts the UID part from the supplied resource name, i.e. the
     * resource name without the <code>.vcf</code> extension.
     *
     * @param name the name
     * @return the UID
     */
    public static String extractUID(String name) {
    	if (null != name && 4 < name.length() && name.toLowerCase().endsWith(".vcf")) {
    		return name.substring(0, name.length() - 4);
    	}
    	return name;
    }

    public static Date getLatestModified(final Date lastModified1, final Date lastModified2) {
    	return lastModified1.after(lastModified2) ? lastModified1 : lastModified2;
    }

    public static Date getLatestModified(final Date lastModified, final Contact contact) {
    	return getLatestModified(lastModified, contact.getLastModified());
    }

    public static Date getLatestModified(Date lastModified, UserizedFolder folder) {
    	return getLatestModified(lastModified, folder.getLastModifiedUTC());
    }

    public static boolean isImageProblem(final OXException e) {
    	return ContactExceptionCodes.IMAGE_BROKEN.equals(e) ||
    			ContactExceptionCodes.IMAGE_DOWNSCALE_FAILED.equals(e) ||
    			ContactExceptionCodes.IMAGE_SCALE_PROBLEM.equals(e) ||
    			ContactExceptionCodes.IMAGE_TOO_LARGE.equals(e) ||
    			ContactExceptionCodes.NOT_VALID_IMAGE.equals(e)
    	;
    }

    public static boolean isDataTruncation(final OXException e) {
        return ContactExceptionCodes.DATA_TRUNCATION.equals(e);
    }

    public static boolean isIncorrectString(OXException e) {
        return ContactExceptionCodes.INCORRECT_STRING.equals(e);
    }

	/**
	 * Parses a numerical identifier from a string, wrapping a possible
	 * NumberFormatException into an OXException.
	 *
	 * @param id the id string
	 * @return the parsed identifier
	 * @throws OXException
	 */
	public static int parse(final String id) throws OXException {
		try {
			return Integer.parseInt(id);
		} catch (final NumberFormatException e) {
			throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, id);
		}
	}

	public static SearchTerm<?> getSearchTerm(String uid, List<String> folderIDs) {
		CompositeSearchTerm uidsTerm = new CompositeSearchTerm(CompositeOperation.OR);
		uidsTerm.addSearchTerm(getSingleSearchTerm(ContactField.UID, uid));
		uidsTerm.addSearchTerm(getSingleSearchTerm(ContactField.USERFIELD19, uid));
		if (null == folderIDs || 0 == folderIDs.size()) {
			return uidsTerm;
		} else {
			CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
			andTerm.addSearchTerm(uidsTerm);
			if (1 == folderIDs.size()) {
				andTerm.addSearchTerm(getSingleSearchTerm(ContactField.FOLDER_ID, folderIDs.get(0)));
			} else {
				CompositeSearchTerm foldersTerm = new CompositeSearchTerm(CompositeOperation.OR);
				for (String folderID : folderIDs) {
					foldersTerm.addSearchTerm(getSingleSearchTerm(ContactField.FOLDER_ID, folderID));
				}
				andTerm.addSearchTerm(foldersTerm);
			}
			return andTerm;
		}
	}

	private static SingleSearchTerm getSingleSearchTerm(ContactField field, String value) {
		SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
		term.addOperand(new ContactFieldOperand(field));
		term.addOperand(new ConstantOperand<String>(value));
		return term;
	}

}

