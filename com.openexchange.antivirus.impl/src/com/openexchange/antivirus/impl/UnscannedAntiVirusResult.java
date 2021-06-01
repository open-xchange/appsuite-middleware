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

package com.openexchange.antivirus.impl;

import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.exception.OXException;

/**
 * {@link UnscannedAntiVirusResult} -  A special <code>AntiVirusResult</code>, which signals <code>false</code> for {@link #isStreamScanned()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class UnscannedAntiVirusResult implements AntiVirusResult {

    private static final long serialVersionUID = 8788805314916885254L;

    private final AntiVirusResult delegate;

    /**
     * Initializes a new {@link UnscannedAntiVirusResult}.
     */
    public UnscannedAntiVirusResult(AntiVirusResult delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public String getAntiVirusServiceId() {
        return delegate.getAntiVirusServiceId();
    }

    @Override
    public String getISTag() {
        return delegate.getISTag();
    }

    @Override
    public String getThreatName() {
        return delegate.getThreatName();
    }

    @Override
    public Boolean isInfected() {
        return delegate.isInfected();
    }

    @Override
    public OXException getError() {
        return delegate.getError();
    }

    @Override
    public long getScanTimestamp() {
        return delegate.getScanTimestamp();
    }

    @Override
    public boolean isStreamScanned() {
        return false;
    }

}
