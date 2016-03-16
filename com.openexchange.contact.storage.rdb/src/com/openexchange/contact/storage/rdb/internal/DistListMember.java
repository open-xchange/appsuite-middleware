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

package com.openexchange.contact.storage.rdb.internal;

import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DistributionListEntryObject;


/**
 * {@link DistListMember} -
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DistListMember extends DistributionListEntryObject {

    private static final long serialVersionUID = 3887003030363062903L;

    /**
	 * Creates an array of distribution list members using the supplied data.
	 *
	 * @param distList an array of distribution list entry objects
	 * @param contextID the context ID
	 * @param parentContactID the ID of the corresponding contact
	 * @return the distribution list members
	 * @throws OXException
	 */
	public static DistListMember[] create(final DistributionListEntryObject[] distList, final int contextID, final int parentContactID) throws OXException {
    	if (null != distList) {
    		final DistListMember[] members = new DistListMember[distList.length];
    		for (int i = 0; i < members.length; i++) {
				members[i] = DistListMember.create(distList[i], contextID, parentContactID);
			}
        	return members;
    	}
    	return null;
	}

	public static DistListMember create(final DistributionListEntryObject dleo, final int contextID, final int parentContactID) throws OXException {
		final DistListMember member = new DistListMember();
		member.setParentContactID(parentContactID);
		member.setContextID(contextID);
		if (dleo.containsDisplayname()) {
			member.setDisplayname(dleo.getDisplayname());
		}
		if (dleo.containsEmailaddress()) {
			member.setEmailaddress(dleo.getEmailaddress(), false);
		}
		if (dleo.containsEmailfield()) {
			member.setEmailfield(dleo.getEmailfield());
		}
		if (dleo.containsEntryID()) {
			member.setEntryID(dleo.getEntryID());
		}
		if (dleo.containsFistname()) {
			member.setFirstname(dleo.getFirstname());
		}
		if (dleo.containsFolderld()) {
			member.setFolderID(dleo.getFolderID());
		}
		if (dleo.containsLastname()) {
			member.setLastname(dleo.getLastname());
		}

		return member;
	}

	// ------------------------------------------------------------------------------------------------------------------------

	private int parentContactID;
    private boolean b_parentContactID;

    private int contextID;
    private boolean b_contextID;

    private UUID uuid;
    private boolean b_uuid;

    /**
     * Initializes a new {@link DistListMember}.
     */
    public DistListMember() {
        super();
    }

	/**
	 * @return the parentContactID
	 */
	public int getParentContactID() {
		return parentContactID;
	}

	/**
	 * @param parentContactID the parentContactID to set
	 */
	public void setParentContactID(int parentContactID) {
		this.b_parentContactID = true;
		this.parentContactID = parentContactID;
	}

    public void removeParentContactID() {
        parentContactID = 0;
        b_parentContactID = false;
    }

	/**
	 * @return the contextID
	 */
	public int getContextID() {
		return contextID;
	}

	/**
	 * @param contextID the contextID to set
	 */
	public void setContextID(int contextID) {
		this.b_contextID = true;
		this.contextID = contextID;
	}


    public void removeContextID() {
        contextID = 0;
        b_contextID = false;
    }

	/**
	 * @return the b_contextID
	 */
	public boolean containsContextID() {
		return b_contextID;
	}

	/**
	 * @return the b_parentContactID
	 */
	public boolean containsParentContactID() {
		return b_parentContactID;
	}

	public void setUuid(UUID uuid) {
	    this.b_uuid = true;
	    this.uuid = uuid;
	}

	public void removeUuid() {
	    this.uuid = null;
	    this.b_uuid = false;
	}

	public boolean containsUuid() {
	    return b_uuid;
	}

	public UUID getUuid() {
	    return uuid;
	}

}
