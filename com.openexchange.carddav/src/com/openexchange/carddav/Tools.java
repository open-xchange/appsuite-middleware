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

package com.openexchange.carddav;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.dav.DAVOAuthScope;
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

    private static final Logger LOG = LoggerFactory.getLogger(Tools.class);

    /**
     * The OAuth scope token for CardDAV
     */
    public static final String OAUTH_SCOPE = DAVOAuthScope.CARDDAV.getScope();

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

    /**
     * Calculates a combined hash code for the supplied collection of folders, based on each folder's identifier as well as the user's
     * <i>own</i> permissions on it.
     *
     * @param folders The folders to get the hash code for
     * @return The hash code
     */
    public static String getFoldersHash(List<UserizedFolder> folders) {
        if (null == folders || folders.isEmpty()) {
            return null;
        }
        final int prime = 31;
        int result = 1;
        for (UserizedFolder folder : folders) {
            result = prime * result + ((null == folder.getID()) ? 0 : folder.getID().hashCode());
            result = prime * result + ((null == folder.getOwnPermission()) ? 0 : folder.getOwnPermission().hashCode());
        }

        if(LOG.isDebugEnabled()) {
            StringBuilder b = new StringBuilder("Generated folder hash '");
            b.append(Integer.toHexString(result)).append("' from: ");
            // @formatter:off
            folders.forEach((f) -> {
                b.append("{ name: '")
                 .append(f.getName())
                 .append("', id: ")
                 .append(f.getID())
                 .append(", perms: ")
                 .append(f.getOwnPermission().hashCode())
                 .append(" }, ");
            });
            // @formatter:on
            LOG.debug(b.toString());
        }
        return Integer.toHexString(result);
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
		} catch (NumberFormatException e) {
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

