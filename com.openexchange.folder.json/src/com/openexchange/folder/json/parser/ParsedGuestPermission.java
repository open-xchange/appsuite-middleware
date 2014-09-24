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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.folder.json.parser;

import java.util.Date;
import com.openexchange.folderstorage.GuestPermission;
import com.openexchange.share.AuthenticationMode;

/**
 * {@link ParsedGuestPermission}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ParsedGuestPermission extends ParsedPermission implements GuestPermission {

    private static final long serialVersionUID = 7203618073266628866L;

    private String mailAddress;
    private String contactID;
    private String contactFolderID;
    private String displayName;
    private AuthenticationMode authenticationMode;
    private String password;
    private Date expires;

    /**
     * Initializes an empty {@link ParsedGuestPermission}.
     */
    public ParsedGuestPermission() {
        super();
    }

    @Override
    public String getEmailAddress() {
        return mailAddress;
    }


    public void setEmailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    @Override
    public String getContactID() {
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }

    @Override
    public String getContactFolderID() {
        return contactFolderID;
    }

    public void setContactFolderID(String contactFolderID) {
        this.contactFolderID = contactFolderID;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public AuthenticationMode getAuthenticationMode() {
        return authenticationMode;
    }

    /**
     * Sets the authenticationMode
     *
     * @param authenticationMode The authenticationMode to set
     */
    public void setAuthenticationMode(AuthenticationMode authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Date getExpires() {
        return expires;
    }

    /**
     * Sets the expires
     *
     * @param expires The expires to set
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((authenticationMode == null) ? 0 : authenticationMode.hashCode());
        result = prime * result + ((contactFolderID == null) ? 0 : contactFolderID.hashCode());
        result = prime * result + ((contactID == null) ? 0 : contactID.hashCode());
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((mailAddress == null) ? 0 : mailAddress.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((expires == null) ? 0 : expires.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ParsedGuestPermission)) {
            return false;
        }
        ParsedGuestPermission other = (ParsedGuestPermission) obj;
        if (authenticationMode != other.authenticationMode) {
            return false;
        }
        if (contactFolderID == null) {
            if (other.contactFolderID != null) {
                return false;
            }
        } else if (!contactFolderID.equals(other.contactFolderID)) {
            return false;
        }
        if (contactID == null) {
            if (other.contactID != null) {
                return false;
            }
        } else if (!contactID.equals(other.contactID)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (mailAddress == null) {
            if (other.mailAddress != null) {
                return false;
            }
        } else if (!mailAddress.equals(other.mailAddress)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (expires == null) {
            if (other.expires != null) {
                return false;
            }
        } else if (!expires.equals(other.expires)) {
            return false;
        }
        return true;
    }

}
