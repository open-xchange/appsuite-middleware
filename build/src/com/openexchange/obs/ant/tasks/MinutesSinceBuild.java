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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.obs.ant.tasks;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This ant task can calculate the seconds since the last build.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class MinutesSinceBuild extends Task {

    private String timestamp;

    private String lastbuildtimestamp;

    private String property;

    private String format;

    private String lapping;

    public MinutesSinceBuild() {
        super();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastbuildtimestamp() {
        return lastbuildtimestamp;
    }

    public void setLastbuildtimestamp(final String lastbuildtimestamp) {
        this.lastbuildtimestamp = lastbuildtimestamp;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(final String property) {
        this.property = property;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLapping() {
        return lapping;
    }

    public void setLapping(String lapping) {
        this.lapping = lapping;
    }

    private static final int DEFAULT_LAPPING = 5;

    private final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public void execute() throws BuildException {
        if (null == getProperty()) {
            throw new BuildException("Property name for returning calculated seconds must be defined.");
        }
        if (null == getTimestamp()) {
            throw new BuildException("Property for current build timestamp must be defined.");
        }
        if (null == getLastbuildtimestamp()) {
            throw new BuildException("Property for last successful build timestamp must be defined.");
        }
        if (null == getFormat()) {
            throw new BuildException("Property for the used format must be defined.");
        }
        final Date now;
        final Date last;
        if ("datetime".equals(getFormat())) {
            try {
                now = df.parse(getTimestamp());
                last = df.parse(getLastbuildtimestamp());
            } catch (final ParseException e) {
                throw new BuildException("Problem while parsing build timestamps.", e);
            }
        } else
        if ("utclong".equals(getFormat())) {
            now = new Date(Long.parseLong(getTimestamp()));
            last = new Date(Long.parseLong(getLastbuildtimestamp()));
        } else {
            throw new BuildException("Unknown format " + getFormat() + "."); 
        }
        final int lappingInt;
        if (null == getLapping()) {
            lappingInt = DEFAULT_LAPPING;
        } else {
            try {
                lappingInt = Integer.parseInt(getLapping());
            } catch (NumberFormatException e) {
                throw new BuildException("Can not parse lapping minutes from \"" + getLapping() + "\".");
            }
        }
        getProject().setNewProperty(getProperty(), String.valueOf((now.getTime() - last.getTime())/60000 + lappingInt));
    }
}
