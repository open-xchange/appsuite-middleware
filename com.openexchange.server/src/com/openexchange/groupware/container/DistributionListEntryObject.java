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

package com.openexchange.groupware.container;

import java.io.Serializable;
import javax.mail.internet.AddressException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * DistributionListObject
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public class DistributionListEntryObject implements Serializable {

    private static final long serialVersionUID = 3878123169840216186L;

    public static final int INDEPENDENT = 0;

    public static final int EMAILFIELD1 = 1;

    public static final int EMAILFIELD2 = 2;

    public static final int EMAILFIELD3 = 3;

    public static final int INSERT = 1;

    public static final int UPDATE = 2;

    public static final int DELETE = 3;

    private int entry_id;

    private String emailaddress;

    private String displayname;

    private String lastname;

    private String firstname;

    private int emailfield;

    private int folderid;

    private String sortName;

    private String contactUid;

    private boolean b_entry_id;

    private boolean b_emailaddress;

    private boolean b_displayname;

    private boolean b_firstname;

    private boolean b_lastname;

    private boolean b_emailfield;

    private boolean b_folderid;

    private boolean b_sortName;
    private boolean b_contactUid;

    /**
     * Initializes a new {@link DistributionListEntryObject}
     */
    public DistributionListEntryObject() {
        super();
    }

    /**
     * Initializes a new {@link DistributionListEntryObject}
     *
     * @param displayname The display name
     * @param emailaddress The email address
     * @param emailfield The email field
     * @throws OXException If specified email address is invalid
     */
    public DistributionListEntryObject(final String displayname, final String emailaddress, final int emailfield) throws OXException {
        this();
        setDisplayname(displayname);
        setEmailaddress(emailaddress);
        setEmailfield(emailfield);
    }

    // GET METHODS
    public int getEntryID() {
        return entry_id;
    }

    public int getFolderID() {
        return folderid;
    }

    public String getDisplayname() {
        return displayname;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public int getEmailfield() {
        return emailfield;
    }

    public String getLastname() {
        return lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    /**
     * Gets the sortName
     *
     * @return The sortName
     */
    public String getSortName() {
        return sortName;
    }

    /**
     * Gets the contactUid
     *
     * @return The contactUid
     */
    public String getContactUid() {
        return this.contactUid;
    }

    // SET METHODS
    public void setEntryID(final int entry_id) {
        this.entry_id = entry_id;
        b_entry_id = true;
    }

    public void setFolderID(final int folderid) {
        this.folderid = folderid;
        b_folderid = true;
    }

    public void setDisplayname(final String displayname) {
        this.displayname = displayname;
        b_displayname = true;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
        b_lastname = true;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
        b_firstname = true;
    }

    /**
     * Sets the sortName
     *
     * @param sortName The sortName to set
     */
    public void setSortName(String sortName) {
        this.sortName = sortName;
        this.b_sortName = true;
    }

    /**
     * Sets the contactUid
     *
     * @param contactUid The contactUid to set
     */
    public void setContactUid(String contactUid) {
        this.contactUid = contactUid;
        this.b_contactUid = true;
    }

    /**
     * Sets the distribution list entry's email address
     *
     * @param emailaddress The email address to set
     * @throws OXException If specified email address is invalid
     */
    public void setEmailaddress(final String emailaddress) throws OXException {
        this.setEmailaddress(emailaddress, null != emailaddress && 0 < emailaddress.length());
    }

    /**
     * Sets the distribution list entry's email address
     *
     * @param emailaddress The email address to set
     * @param verifyAddress <code>true</code>, if the address should be verified, <code>false</code>, otherwise
     * @throws OXException If specified email address is invalid and verification is enabled
     */
    public void setEmailaddress(String emailaddress, boolean verifyAddress) throws OXException {
        if (verifyAddress) {
            /*
             * Verify email address with JavaMail's InternetAddress class
             */
            try {
                new QuotedInternetAddress(emailaddress);
            } catch (AddressException e) {
                throw ContactExceptionCodes.INVALID_EMAIL.create(e, emailaddress);
            }
        }
        this.emailaddress = emailaddress;
        b_emailaddress = true;
    }

    public void setEmailfield(final int emailfield) {
        this.emailfield = emailfield;
        b_emailfield = true;
    }

    // REMOVE METHODS
    public void removeEntryID() {
        entry_id = 0;
        b_entry_id = false;
    }

    public void removeDisplayname() {
        displayname = null;
        b_displayname = false;
    }

    public void removeFirstname() {
        firstname = null;
        b_firstname = false;
    }

    public void removeLastname() {
        lastname = null;
        b_lastname = false;
    }

    public void removeEmailaddress() {
        emailaddress = null;
        b_emailaddress = false;
    }

    public void removeEmailfield() {
        emailfield = 0;
        b_emailfield = false;
    }

    public void removeFolderld() {
        folderid = 0;
        b_folderid = false;
    }

    /**
     * Removes the sort name
     */
    public void removeSortName() {
        sortName = null;
        b_sortName = false;
    }

    /**
     * Removes the contactUid
     */
    public void removeContactUid() {
        contactUid = null;
        b_contactUid = false;
    }

    // CONTAINS METHODS
    public boolean containsEntryID() {
        return b_entry_id;
    }

    public boolean containsDisplayname() {
        return b_displayname;
    }

    public boolean containsLastname() {
        return b_lastname;
    }

    public boolean containsFistname() {
        return b_firstname;
    }

    public boolean containsEmailaddress() {
        return b_emailaddress;
    }

    public boolean containsEmailfield() {
        return b_emailfield;
    }

    public boolean containsFolderld() {
        return b_folderid;
    }

    /**
     * Whether the {@link DistributionListEntryObject} contains a sort name or not
     * 
     * @return true if it contains a sort name, false otherwise
     */
    public boolean containsSortName() {
        return b_sortName;
    }

    public boolean containsContactUid() {
        return b_contactUid;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + emailaddress.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DistributionListEntryObject)) {
            return false;
        }
        DistributionListEntryObject distributionlistentry = (DistributionListEntryObject) o;
        if (emailaddress == null) {
            if (distributionlistentry.emailaddress == null) {
                return true;
            }
        } else if (emailaddress.equals(distributionlistentry.emailaddress)) {
            return true;
        }
        return false;
    }

    public boolean searchDlistObject(final DistributionListEntryObject dleo) {
        if (dleo.getEntryID() > 0) { // this one is not an independent
            if ((getEntryID() == dleo.getEntryID()) && (getEmailfield() == dleo.getEmailfield())) {
                return true;
            }
        }
        return false;
    }

    public boolean compareDlistObject(final DistributionListEntryObject dleo) {
        boolean eq = true; // with true, this dlistentry does not requiere any changes, with false it needs to get updated

        if (getDisplayname() != null && dleo.getDisplayname() != null) {
            if (!getDisplayname().equals(dleo.getDisplayname())) {
                eq = false;
            }
        } else if ((getDisplayname() == null && dleo.getDisplayname() != null) || (getDisplayname() != null && dleo.getDisplayname() == null)) {
            eq = false;
        }
        if (getEmailaddress() != null && dleo.getEmailaddress() != null) {
            if (!getEmailaddress().equals(dleo.getEmailaddress())) {
                eq = false;
            }
        } else if ((getEmailaddress() == null && dleo.getEmailaddress() != null) || (getEmailaddress() != null && dleo.getEmailaddress() == null)) {
            eq = false;
        }
        if (getFirstname() != null && dleo.getFirstname() != null) {
            if (!getFirstname().equals(dleo.getFirstname())) {
                eq = false;
            }
        } else if ((getFirstname() == null && dleo.getFirstname() != null) || (getFirstname() != null && dleo.getFirstname() == null)) {
            eq = false;
        }
        if (getLastname() != null && dleo.getLastname() != null) {
            if (!getLastname().equals(dleo.getLastname())) {
                eq = false;
            }
        } else if ((getLastname() == null && dleo.getLastname() != null) || (getLastname() != null && dleo.getLastname() == null)) {
            eq = false;
        }
        if (getSortName() != null && dleo.getSortName() != null) {
            if (!getSortName().equals(dleo.getSortName())) {
                eq = false;
            }
        } else if ((getSortName() == null && dleo.getSortName() != null) || (getSortName() != null && dleo.getSortName() == null)) {
            eq = false;
        }
        if (getContactUid() != null && dleo.getContactUid() != null) {
            if (!getContactUid().equals(dleo.getContactUid())) {
                eq = false;
            }
        } else if ((getContactUid() == null && dleo.getContactUid() != null) || (getContactUid() != null && dleo.getContactUid() == null)) {
            eq = false;
        }

        return eq;
    }

    public void reset() {
        entry_id = 0;
        displayname = null;
        lastname = null;
        firstname = null;
        emailaddress = null;
        emailfield = 0;
        folderid = 0;
        sortName = null;
        contactUid = null;

        b_entry_id = false;
        b_displayname = false;
        b_lastname = false;
        b_firstname = false;
        b_emailaddress = false;
        b_emailfield = false;
        b_folderid = false;
        b_sortName = false;
        b_contactUid = false;
    }

    @Override
    public String toString() {
        return "DistributionListEntryObject [emailaddress=" + emailaddress + ", displayname=" + displayname + "]";
    }
}
