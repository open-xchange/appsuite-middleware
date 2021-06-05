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

package com.openexchange.antivirus.exceptions;

/**
 * {@link AntiVirusServiceExceptionMessages}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
final class AntiVirusServiceExceptionMessages {

    // The remote Anti-Virus service is unavailable at the moment. There is nothing we can do about it. Please try again later.
    public static final String REMOTE_SERVICE_UNAVAILABLE_MSG = "The remote Anti-Virus service is unavailable at the moment. There is nothing we can do about it. Please try again later.";
    // An internal server error occurred on the remote Anti-Virus's server side. There is nothing we can do about it.
    public static final String REMOTE_INTERNAL_SERVER_ERROR_MSG = "An internal server error occurred on the remote Anti-Virus' server side. There is nothing we can do about it.";
    // A remote server error occurred on the remote Anti-Virus's server side. There is nothing we can do about it.
    public static final String REMOTE_SERVER_ERROR_MSG = "A remote server error occurred on the remote Anti-Virus's server side. There is nothing we can do about it.";
    // It seems that the remote Anti-Virus service is experiencing some connectivity issues. There is nothing we can do about it. Please try again later.
    public static final String CANNOT_STABLISH_CONNECTION = "It seems that the remote Anti-Virus service is experiencing some connectivity issues. There is nothing we can do about it. Please try again later.";
    // The file '%1$s' you are trying to scan does not exist.
    public static final String FILE_NOT_EXISTS = "The file '%1$s' you are trying to scan does not exist.";
    // The file you are trying to scan exceeds the maximum allowed file size of %1$s.
    public static final String FILE_TOO_BIG = "The file you are trying to scan exceeds the maximum allowed file size of %1$s MB.";
    // The file '%1$s' you are trying to download seems to be infected with '%2$s.
    public static final String FILE_INFECTED = "The file '%1$s' you are trying to download seems to be infected with '%2$s'.";
    // We were unable to scan your file for viruses.
    public static final String UNABLE_TO_SCAN = "We were unable to scan your file for viruses.";
    // The Anti-Virus service is unavailable at the moment. Please try again later.
    public static final String ANTI_VIRUS_SERVICE_UNAVAILABLE = "The Anti-Virus service is unavailable at the moment. Please try again later.";
}
