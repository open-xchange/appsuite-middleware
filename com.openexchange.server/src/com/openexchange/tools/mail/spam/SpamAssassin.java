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

package com.openexchange.tools.mail.spam;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.mail.Message;
import javax.mail.MessagingException;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * SpamAssassin - Offers methods for spam detection and learning.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SpamAssassin {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamAssassin.class);

    private static final String STR_SPAMASSASSIN = "SpamAssassin result: ";

    // /**
    // * <code>spamassassin -L</code>. "-L" use local tests only
    // */
    // private static final String CMD_SPAMASSASSIN = "spamassassin -L";

    private static final String STR_LINUX = "/usr/bin/";

    private static final String STR_LOCAL_LINUX = "/usr/local/bin/";

    private static final String CMD_SPAMC = "spamc";

    private static final String CMD_SA_LEARN = "sa-learn";

    private static final String PARAM_SPAMC = " -c -L";

    private static final String PARAM_SA_LEARN_SPAM = " --no-sync --spam --single";

    private static final String PARAM_SA_LEARN_HAM = " --no-sync --ham --single";

    private static final Map<String, File> locationMap = new HashMap<String, File>();

    private static final String getCommand(final String command, final String paramList) {
        final File location = getLocation(command);
        if (location == null) {
            return null;
        }
        return new StringBuilder(location.getPath()).append(paramList.charAt(0) == ' ' ? "" : " ").append(paramList).toString();
    }

    private static final File getLocation(final String command) {
        if (locationMap.containsKey(command)) {
            return locationMap.get(command);
        }
        File retval = null;
        try {
            retval = new File(new StringBuilder().append(STR_LINUX).append(command).toString());
            if (retval.exists()) {
                return retval;
            }
            retval = new File(new StringBuilder().append(STR_LOCAL_LINUX).append(command).toString());
            if (retval.exists()) {
                return retval;
            }
            retval = null;
            return null;
        } finally {
            locationMap.put(command, retval);
        }
    }

    private SpamAssassin() {
        super();
    }

    /**
     * Check if message is spam or non-spam
     *
     * @return <code>true</code> if message is treated as spam, <code>false</code> otherwise
     */
    public static final boolean scoreMessage(final Message msg) {
        try {
            /*
             * Initialize process to execute command
             */
            final CommandExecutor cmdExec = new CommandExecutor(getCommand(CMD_SPAMC, PARAM_SPAMC));
            /*
             * Send data
             */
            cmdExec.send(getRawMessageInputStream(msg));
            /*
             * Wait until process terminates. Get its exit code.
             */
            final int exitCode = cmdExec.waitFor();
            /*
             * Read its result
             */
            final String result = cmdExec.getOutputString();
            LOG.info("{}{}", STR_SPAMASSASSIN, result);
            if (result != null && exitCode == 1) {
                /*
                 * Spam found
                 */
                return true;
            }
            return false;
        } catch (final IOException e) {
            LOG.error("", e);
        } catch (final MessagingException e) {
            LOG.error("", e);
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            LOG.error("", e);
        }
        return false;
    }

    /**
     * Adds given message to SpamAssassin's learn rules as <b>spam</b> in a separate thread
     *
     * @param msg - the message
     */
    public static final void trainMessageAsSpam(final Message msg) {
        trainMessage(msg, true);
    }

    /**
     * Adds given message to SpamAssassin's learn rules as <b>ham</b> in a separate thread
     *
     * @param msg - the message
     */
    public static final void trainMessageAsHam(final Message msg) {
        trainMessage(msg, false);
    }

    private static final String STR_SPAM = "Spam ";

    private static final String STR_HAM = "Ham ";

    private static final void trainMessage(final Message msg, final boolean isSpam) {
        try {
            new TrainMessageThread(getRawMessageInputStream(msg), isSpam).start();
        } catch (final IOException e) {
            LOG.error("", e);
        } catch (final MessagingException e) {
            LOG.error("", e);
        }
    }

    private static final InputStream getRawMessageInputStream(final Message msg) throws IOException, MessagingException {
        final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
        msg.writeTo(baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private static class TrainMessageThread extends Thread {

        private static final String NAME = "TrainMessageThread";

        private static final String ERR_PREFIX = "Invocation of " + CMD_SA_LEARN + " failed ";

        private final InputStream msgSrc;

        private final boolean isSpam;

        private final String cmd;

        public TrainMessageThread(final InputStream msgSrc, final boolean isSpam) {
            super(NAME);
            this.isSpam = isSpam;
            cmd = isSpam ? getCommand(CMD_SA_LEARN, PARAM_SA_LEARN_SPAM) : getCommand(CMD_SA_LEARN, PARAM_SA_LEARN_HAM);
            this.msgSrc = msgSrc;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                /*
                 * Initialize process to execute command
                 */
                final CommandExecutor cmdExec = new CommandExecutor(cmd);
                /*
                 * Send data
                 */
                cmdExec.send(msgSrc);
                /*
                 * Wait until process terminates. Get its exit code.
                 */
                cmdExec.waitFor();
                /*
                 * Read its result
                 */
                final String res = cmdExec.getOutputString();
                LOG.info("{}{}{}", STR_SPAMASSASSIN, isSpam ? STR_SPAM : STR_HAM, res);
            } catch (final IOException e) {
                LOG.error(ERR_PREFIX, e);
            } catch (final InterruptedException e) {
                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                Thread.currentThread().interrupt();
                LOG.error(ERR_PREFIX, e);
            }
        }

    }

}
