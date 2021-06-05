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

package com.openexchange.cli;

import com.openexchange.java.Strings;

/**
 * {@link ConsoleSpinner} - A simple console spinner to indicate that work is in progress.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ConsoleSpinner {

    private final Thread animationThread;
    private final String optionalPrefix;
    private final String optionalSuffix;

    /**
     * Initializes a new {@link ConsoleSpinner}.
     */
    public ConsoleSpinner(String optionalPrefix, String optionalSuffix) {
        super();
        this.optionalPrefix = Strings.isEmpty(optionalPrefix) ? "" : optionalPrefix;
        this.optionalSuffix = Strings.isEmpty(optionalSuffix) ? "" : optionalSuffix;
        animationThread = new Thread(new SpinnerAnimation(this.optionalPrefix, this.optionalSuffix), "spinnerThread");
        animationThread.setDaemon(true);
    }

    /**
     * Starts the spinning
     */
    public void start() {
        animationThread.start();
    }

    /**
     * Stops the spinning and sets an optional status.
     */
    public void stop(String status) {
        System.out.print("\r" + optionalPrefix + "[ " + status + " ]" + optionalSuffix + "\n");
        animationThread.interrupt();
    }

    private static class SpinnerAnimation implements Runnable {

        private final String optionalPrefix;
        private final String optionalSuffix;

        /**
         * Initialises a new {@link ConsoleSpinner.SpinnerAnimation}.
         */
        public SpinnerAnimation(String optionalPrefix, String optionalSuffix) {
            super();
            this.optionalPrefix = optionalPrefix;
            this.optionalSuffix = optionalSuffix;
        }

        @Override
        public void run() {
            try {
                int c = 1;
                String s = "";
                while (true) {
                    Thread.sleep(200);
                    System.out.print("\r" + optionalPrefix + s + optionalSuffix);
                    switch (c) {
                        case 1:
                            s = "[ \\ ]";
                            break;
                        case 2:
                            s = "[ | ]";
                            break;
                        case 3:
                            s = "[ / ]";
                            break;
                        default:
                            s = "[ - ]";
                            c = 0;
                    }
                    c++;
                }
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }
}
