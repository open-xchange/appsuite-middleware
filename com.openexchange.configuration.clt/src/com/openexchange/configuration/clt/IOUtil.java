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

package com.openexchange.configuration.clt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link IOUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public final class IOUtil {

    /**
     * Determines and returns the input stream
     * 
     * @param stdin <code>true</code> to use {@link System#in}; if <code>false</code> then a new {@link FileInputStream}
     *            will be initialised
     * @param filename The filename for the new {@link FileInputStream}
     * @return The {@link InputStream}
     */
    public static final InputStream determineInput(boolean stdin, String filename) {
        if (stdin) {
            return System.in;
        }

        File input = new File(filename);
        if (!input.exists() || !input.isFile() || !input.canRead()) {
            System.err.println("Can not open input file: \"" + input.getAbsolutePath() + "\".");
            return null;
        }
        try {
            return new FileInputStream(input);
        } catch (IOException e) {
            System.err.println("Can not read input file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Determines and returns the output stream
     * 
     * @param stdout <code>true</code> to use {@link System#out}; if <code>false</code> then a new {@link FileOutputStream}
     *            will be initialised
     * @param filename The file's name for the {@link FileOutputStream}
     * @return The new {@link OutputStream}
     */
    public static final OutputStream determineOutput(boolean stdout, String filename) {
        if (stdout) {
            return System.out;
        }
        File output = new File(filename);
        try {
            if (!output.createNewFile() && !output.canWrite()) {
                System.err.println("Can not write to output file: \"" + output.getAbsolutePath() + "\".");
                return null;
            }
            return new FileOutputStream(output);
        } catch (IOException e) {
            System.err.println("Can not write output file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
