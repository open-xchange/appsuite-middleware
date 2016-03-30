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

package com.openexchange.dav.carddav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.CompatibilityMode;
import net.sourceforge.cardme.io.VCardWriter;
import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.vcard.arch.VCardVersion;
import net.sourceforge.cardme.vcard.exceptions.VCardBuildException;
import net.sourceforge.cardme.vcard.exceptions.VCardParseException;
import net.sourceforge.cardme.vcard.features.ExtendedFeature;
import net.sourceforge.cardme.vcard.features.UidFeature;
import net.sourceforge.cardme.vcard.types.ExtendedType;
import net.sourceforge.cardme.vcard.types.FNType;
import net.sourceforge.cardme.vcard.types.NType;

/**
 * {@link VCardResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class VCardResource {

	private static final VCardEngine PARSER = new VCardEngine(CompatibilityMode.MAC_ADDRESS_BOOK);
	private static final VCardWriter WRITER = new VCardWriter(VCardVersion.V3_0, CompatibilityMode.MAC_ADDRESS_BOOK);

	private final String eTag;
	private final String href;
	private final VCard vCard;

	public VCardResource(final String vCardString, final String href, final String eTag) throws IOException, VCardParseException {
		this.vCard = PARSER.parse(vCardString);
		this.href = href;
		this.eTag = eTag;
	}

	public String getUID() {
		final UidFeature uidFeature = this.vCard.getUid();
		return null != uidFeature ? uidFeature.getUid() : null;
	}

    public String getFN() {
        FNType formattedName = this.vCard.getFN();
        return null != formattedName ? formattedName.getFormattedName() : null;
    }

    public String getGivenName() {
        NType n = this.vCard.getN();
        return null != n ? n.getGivenName() : null;
    }

    public String getFamilyName() {
        NType n = this.vCard.getN();
        return null != n ? n.getFamilyName() : null;
    }

	public List<ExtendedType> getExtendedTypes(String extendedName) {
		List<ExtendedType> xTypes = new ArrayList<ExtendedType>();
		List<ExtendedType> extendedTypes = this.vCard.getExtendedTypes();
		if (null != extendedTypes) {
    		for (ExtendedType xType : extendedTypes) {
                if (extendedName.equals(xType.getExtendedName())) {
                    xTypes.add(xType);
                }
    		}
		}
		return xTypes;
	}

	/**
	 * @return the eTag
	 */
	public String getETag() {
		return eTag;
	}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @return the vCard
	 */
	public VCard getVCard() {
		return vCard;
	}

	public boolean isGroup() {
		final List<ExtendedType> xFeatures = this.getExtendedTypes("X-ADDRESSBOOKSERVER-KIND");
		return null != xFeatures && 0 < xFeatures.size() && "group".equals(xFeatures.get(0).getExtendedValue());
	}

	public List<String> getMemberUIDs() {
		List<ExtendedType> members = this.getExtendedTypes("X-ADDRESSBOOKSERVER-MEMBER");
		if (null == members) {
			return null;
		}
		List<String> uids = new ArrayList<String>();
		for (ExtendedType memberType : members) {
			uids.add(memberType.getExtendedValue().substring(9));

		}
		return uids;
	}

	public ExtendedFeature getMemberXFeature(String uid) {
		List<ExtendedType> members = this.getExtendedTypes("X-ADDRESSBOOKSERVER-MEMBER");
		if (null == members) {
			return null;
		}
		for (ExtendedType memberType : members) {
			if (uid.equals(memberType.getExtendedValue().substring(9))) {
				return memberType;
			}
		}
		return null;
	}

	@Override
    public String toString() {
		WRITER.setVCard(this.vCard);
		try {
            return WRITER.buildVCardString();
        } catch (VCardBuildException e) {
            e.printStackTrace();
            return null;
        }
	}

}
