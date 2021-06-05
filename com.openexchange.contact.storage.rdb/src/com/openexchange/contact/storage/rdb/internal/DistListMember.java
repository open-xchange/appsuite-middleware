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
        if (dleo.containsContactUid()) {
            member.setContactUid(dleo.getContactUid());
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
