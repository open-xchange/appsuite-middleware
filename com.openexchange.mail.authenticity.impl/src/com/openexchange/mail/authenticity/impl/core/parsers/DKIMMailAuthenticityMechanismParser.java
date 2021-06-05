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

package com.openexchange.mail.authenticity.impl.core.parsers;

import java.util.Map;
import com.openexchange.mail.authenticity.impl.core.MailAuthenticityFragmentPhrases;
import com.openexchange.mail.authenticity.mechanism.AuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.DefaultMailAuthenticityMechanism;
import com.openexchange.mail.authenticity.mechanism.MailAuthenticityMechanismResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMAuthMechResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMProperty;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResult;
import com.openexchange.mail.authenticity.mechanism.dkim.DKIMResultHeader;

/**
 * {@link DKIMMailAuthenticityMechanismParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DKIMMailAuthenticityMechanismParser extends AbstractMailAuthenticityMechanismParser {

    /**
     * Initialises a new {@link DKIMMailAuthenticityMechanismParser}.
     */
    public DKIMMailAuthenticityMechanismParser() {
        super(DefaultMailAuthenticityMechanism.DKIM, DKIMResultHeader.HEADER_I, DKIMResultHeader.HEADER_D);
    }

    @Override
    protected AuthenticityMechanismResult parseMechanismResult(String value) {
        try {
            DKIMResult dkimResult = DKIMResult.dkimResultFor(value);
            return dkimResult == null ? DKIMResult.FAIL : dkimResult;
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
            return DKIMResult.FAIL;
        }
    }

    @Override
    protected MailAuthenticityMechanismResult createResult(String domain, AuthenticityMechanismResult mechResult, String mechanismResult, boolean domainMatch, Map<String, String> attributes) {
        DKIMAuthMechResult result = new DKIMAuthMechResult(domain, (DKIMResult) mechResult);
        result.setDomainMatch(domainMatch);
        result.addProperty(DKIMProperty.SIGNING_DOMAIN, result.getDomain());
        result.setReason(compileReasonPhrase(mechResult, MailAuthenticityFragmentPhrases.WITH_DOMAIN, result.getDomain()));
        return result;
    }
}
