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

/**
 * {@link ProgressMonitor} - A simple command line progress monitoring bar
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class ProgressMonitor {

    private final int width;
    private final String label;

    /**
     * Initialises a new {@link ProgressMonitor}.
     */
    public ProgressMonitor(int width, String label) {
        super();
        this.width = width;
        this.label = label;
    }

    /**
     * Updates the progress bar
     *
     * @param updateLabel the update label
     * @param progressPercentage The progress percentage ranging from 0 to 1
     */
    public void update(String updateLabel, double progressPercentage) {
        System.out.print("\r " + label + " " + String.format("%.2f", Double.valueOf(progressPercentage * 100)) + "% [");
        int i = 0;
        for (; i <= (int) (progressPercentage * width) - 1; i++) {
            System.out.print("=");
        }
        System.out.print(">");
        for (; i < width; i++) {
            System.out.print(" ");
        }
        System.out.print("]  " + updateLabel);
    }
}
