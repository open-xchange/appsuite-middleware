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
package com.openexchange.admin.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShellExecutor {

    public class ArrayOutput {

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            String ret = "";
            ret = "Stdout:\n";
            for(final String line : stdOutput) {
                ret += line+"\n";
            }
            ret += "Stderr:\n";
            for(final String line : errOutput) {
                ret += line+"\n";
            }
            return ret;
        }

        public final ArrayList<String> errOutput = new ArrayList<String>();

        public final ArrayList<String> stdOutput = new ArrayList<String>();

        public int exitstatus;
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
        final BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        final BufferedReader errbuf = new BufferedReader(new InputStreamReader(err));
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
