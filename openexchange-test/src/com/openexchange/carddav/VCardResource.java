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

package com.openexchange.carddav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.cardme.engine.VCardEngine;
import net.sourceforge.cardme.io.CompatibilityMode;
import net.sourceforge.cardme.io.VCardWriter;
import net.sourceforge.cardme.vcard.VCard;
import net.sourceforge.cardme.vcard.VCardVersion;
import net.sourceforge.cardme.vcard.features.ExtendedFeature;
import net.sourceforge.cardme.vcard.features.UIDFeature;

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
	
	public VCardResource(final String vCardString, final String href, final String eTag) throws IOException {
		this.vCard = PARSER.parse(vCardString);	
		this.href = href;
		this.eTag = eTag;
	}
	
	public String getUID() {
		final UIDFeature uidFeature = this.vCard.getUID();
		return null != uidFeature ? uidFeature.getUID() : null;
	}	

	public List<ExtendedFeature> getExtendedFeatures(final String extensionName) {
		final List<ExtendedFeature> xFeatures = new ArrayList<ExtendedFeature>();
		final Iterator<ExtendedFeature> iter = this.vCard.getExtendedTypes();
		while (iter.hasNext()) {
			final ExtendedFeature extension = iter.next();
			if (extensionName.equals(extension.getExtensionName())) {
				xFeatures.add(extension);
			}
		}
		return xFeatures;				
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
		final List<ExtendedFeature> xFeatures = this.getExtendedFeatures("X-ADDRESSBOOKSERVER-KIND");
		return null != xFeatures && 0 < xFeatures.size() && "group".equals(xFeatures.get(0).getExtensionData());
	}
	
	@Override
    public String toString() {
		WRITER.setVCard(this.vCard);
		return WRITER.buildVCardString();		
	}

}
