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
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.exceptions.AntiVirusServiceExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link AntiVirusResultEvaluatorServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class AntiVirusResultEvaluatorServiceImpl implements AntiVirusResultEvaluatorService {

    /**
     * Initialises a new {@link AntiVirusResultEvaluatorServiceImpl}.
     */
    public AntiVirusResultEvaluatorServiceImpl() {
        super();
    }

    @Override
    public void evaluate(AntiVirusResult result, String filename) throws OXException {
        if (result == null) {
            throw AntiVirusServiceExceptionCodes.UNEXPECTED_ERROR.create("The anti-virus result was 'null'.");
        }
        if (null != result.getError()) {
            throw AntiVirusServiceExceptionCodes.UNEXPECTED_ERROR.create(result.getError(), "Error while scanning for viruses.");
        }
        Boolean isInfected = result.isInfected();
        if (isInfected == null) {
            throw AntiVirusServiceExceptionCodes.UNEXPECTED_ERROR.create("No scan was performed.");
        }
        if (isInfected.booleanValue()) {
            throw AntiVirusServiceExceptionCodes.FILE_INFECTED.create(filename, result.getThreatName());
        }
    }
}
