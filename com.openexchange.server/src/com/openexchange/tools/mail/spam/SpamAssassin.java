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
        } catch (IOException e) {
            LOG.error("", e);
        } catch (MessagingException e) {
            LOG.error("", e);
        } catch (InterruptedException e) {
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
        } catch (IOException e) {
            LOG.error("", e);
        } catch (MessagingException e) {
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
            } catch (IOException e) {
                LOG.error(ERR_PREFIX, e);
            } catch (InterruptedException e) {
                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                Thread.currentThread().interrupt();
                LOG.error(ERR_PREFIX, e);
            }
        }

    }

}
