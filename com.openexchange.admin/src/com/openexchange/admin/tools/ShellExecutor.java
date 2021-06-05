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
package com.openexchange.admin.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import com.openexchange.java.Charsets;

public class ShellExecutor {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ShellExecutor.class);

    public static class ArrayOutput {

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder("Stdout:\n");
            for(final String line : stdOutput) {
                ret.append(line).append("\n");
            }
            ret.append("Stderr:\n");
            for(final String line : errOutput) {
                ret.append(line).append("\n");
            }
            return ret.toString();
        }

        public final ArrayList<String> errOutput = new ArrayList<String>();

        public final ArrayList<String> stdOutput = new ArrayList<String>();

        public int exitstatus;
    }

    /**
     * Tests for availability of specified program.
     *
     * @param desiredProgram The program to test
     * @return <code>true</code> if available; otherwise <code>false</code>
     */
    public static boolean testForProgramInPath(String desiredProgram) {
        ProcessBuilder pb = new ProcessBuilder(isWindows() ? "where" : "which", desiredProgram);
        try {
            Process proc = pb.start();
            int errCode = proc.waitFor();
            if (errCode != 0) {
                return false;
            }

            Path foundProgram = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                foundProgram = Paths.get(reader.readLine());
            }
            LOGGER.debug("{} has been found at : {}", desiredProgram, foundProgram);
            return true;
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Something went wrong while searching for {}", desiredProgram, e);
            return false;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ShellExecutor}.
     */
    public ShellExecutor() {
        super();
    }

    public final ArrayOutput executeprocargs(final String[] args, final String[] env) throws IOException, InterruptedException {
        final Process proc = Runtime.getRuntime().exec(args, env);
        final ArrayOutput retval = getoutputs(proc);
        retval.exitstatus = proc.waitFor();
        return retval;
    }

    public final ArrayOutput executeprocargs(final String[] args) throws IOException, InterruptedException {
        return executeprocargs(args, null);
    }

    private final ArrayOutput getoutputs(final Process proc) throws IOException {
        final InputStream err = proc.getErrorStream();
        final InputStream is = proc.getInputStream();
        final BufferedReader buf = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
        final BufferedReader errbuf = new BufferedReader(new InputStreamReader(err, Charsets.UTF_8));
        String readBuffer = null;
        String errreadBuffer = null;
        final ArrayOutput retval = new ArrayOutput();

        while (null != (errreadBuffer = errbuf.readLine())) {
            retval.errOutput.add(errreadBuffer);
        }
        while (null != (readBuffer = buf.readLine())) {
            retval.stdOutput.add(readBuffer);
        }
        return retval;
    }

}
