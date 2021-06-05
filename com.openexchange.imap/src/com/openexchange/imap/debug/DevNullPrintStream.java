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

package com.openexchange.imap.debug;

import java.io.PrintStream;

/**
 * {@link DevNullPrintStream} - A print stream simply swallowing bytes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class DevNullPrintStream extends PrintStream {

    private static final DevNullPrintStream INSTANCE = new DevNullPrintStream();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DevNullPrintStream getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DevNullPrintStream}.
     */
    private DevNullPrintStream() {
        super(System.out); // hopefully nothing will actually reach stdout
    }

    @Override
    public void close() {
        // Nothing
    }

    @Override
    public void flush() {
        // Nothing
    }

    @Override
    public void print(boolean b) {
        // Nothing
    }

    @Override
    public void print(char c) {
        // Nothing
    }

    @Override
    public void print(char[] s) {
        // Nothing
    }

    @Override
    public void print(double d) {
        // Nothing
    }

    @Override
    public void print(float f) {
        // Nothing
    }

    @Override
    public void print(int i) {
        // Nothing
    }

    @Override
    public void print(long l) {
        // Nothing
    }

    @Override
    public void print(Object obj) {
        // Nothing
    }

    @Override
    public void print(String s) {
        // Nothing
    }

    @Override
    public void println() {
        // Nothing
    }

    @Override
    public void println(boolean x) {
        // Nothing
    }

    @Override
    public void println(char x) {
        // Nothing
    }

    @Override
    public void println(char[] x) {
        // Nothing
    }

    @Override
    public void println(double x) {
        // Nothing
    }

    @Override
    public void println(float x) {
        // Nothing
    }

    @Override
    public void println(int x) {
        // Nothing
    }

    @Override
    public void println(long x) {
        // Nothing
    }

    @Override
    public void println(Object x) {
        // Nothing
    }

    @Override
    public void println(String x) {
        // Nothing
    }

    @Override
    public void write(byte[] aBuf, int off, int len) {
        // Nothing
    }

    @Override
    public void write(int b) {
        // Nothing
    }

}
