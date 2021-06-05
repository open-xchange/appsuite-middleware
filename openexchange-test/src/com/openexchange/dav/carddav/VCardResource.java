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
    private final String vCardString;

    public VCardResource(final String vCardString, final String href, final String eTag) throws IOException, VCardParseException {
        super();
        this.vCardString = vCardString;
        this.vCard = PARSER.parse(vCardString);
        this.href = href;
        this.eTag = eTag;
    }

    public String getVCardString() {
        return vCardString;
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
