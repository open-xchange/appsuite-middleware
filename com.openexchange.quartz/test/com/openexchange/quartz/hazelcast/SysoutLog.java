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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.quartz.hazelcast;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * {@link SysoutLog}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SysoutLog implements Logger {

    @Override
    public void debug(String arg0) {
        System.out.println(arg0.toString());
    }

    @Override
    public void debug(String arg0, Throwable arg1) {
        System.out.println(arg0.toString());
        arg1.printStackTrace();
    }

    @Override
    public void error(String arg0) {
        System.out.println(arg0.toString());
    }

    @Override
    public void error(String arg0, Throwable arg1) {
        System.out.println(arg0.toString());
        arg1.printStackTrace();
    }

    @Override
    public void info(String arg0) {
        System.out.println(arg0.toString());
    }

    @Override
    public void info(String arg0, Throwable arg1) {
        System.out.println(arg0.toString());
        arg1.printStackTrace();
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void trace(String arg0) {
        System.out.println(arg0.toString());
    }

    @Override
    public void trace(String arg0, Throwable arg1) {
        System.out.println(arg0.toString());
        arg1.printStackTrace();
    }

    @Override
    public void warn(String arg0) {
        System.out.println(arg0.toString());
    }

    @Override
    public void warn(String arg0, Throwable arg1) {
        System.out.println(arg0.toString());
        arg1.printStackTrace();
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void trace(String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void trace(String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        // TODO Auto-generated method stub

    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        // TODO Auto-generated method stub

    }

    @Override
    public void debug(String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void debug(String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void debug(Marker marker, String msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        // TODO Auto-generated method stub

    }

    @Override
    public void info(String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void info(String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void info(Marker marker, String msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        // TODO Auto-generated method stub

    }

    @Override
    public void warn(String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void warn(String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void warn(Marker marker, String msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void error(Marker marker, String msg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        // TODO Auto-generated method stub

    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        // TODO Auto-generated method stub

    }

}
