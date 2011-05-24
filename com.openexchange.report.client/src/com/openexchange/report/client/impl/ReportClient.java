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

package com.openexchange.report.client.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import org.json.JSONException;
import com.openexchange.admin.console.AbstractJMXTools;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.report.client.container.ClientLoginCount;
import com.openexchange.report.client.container.ContextDetail;
import com.openexchange.report.client.container.Total;
import com.openexchange.report.client.transport.TransportHandler;
import com.openexchange.tools.CSVWriter;
import com.openexchange.tools.console.TableWriter;
import com.openexchange.tools.console.TableWriter.ColumnFormat;
import com.openexchange.tools.console.TableWriter.ColumnFormat.Align;

public class ReportClient extends AbstractJMXTools {

    private static final char OPT_SEND_ONLY_SHORT = 's';

    private static final String OPT_SEND_ONLY_LONG = "sendonly";

    private static final char OPT_DISPLAY_ONLY_SHORT = 'd';

    private static final String OPT_DISPLAY_ONLY_LONG = "displayonly";

    private static final char OPT_CSV_SHORT = 'c';

    private static final String OPT_CSV_LONG = "csv";

    private CLIOption displayonly = null;

    private CLIOption sendonly = null;

    private CLIOption csv = null;

    public static void main(final String args[]) {
        System.out.println("Starting the Open-Xchange report client. Note that the report generation may take a little while.");

        final AbstractJMXTools t = new ReportClient();
        t.start(args, "report");
    }

    @Override
    protected void furtherOptionsHandling(final AdminParser parser, HashMap<String, String[]> env) throws InterruptedException, IOException, MalformedURLException, InstanceNotFoundException, AttributeNotFoundException, IntrospectionException, MBeanException, ReflectionException {
        try {
            final MBeanServerConnection initConnection = initConnection(false, env);
            List<Total> totals = ObjectHandler.getTotalObjects(initConnection);
            List<ContextDetail> contextDetails = ObjectHandler.getDetailObjects(initConnection);
            String[] versions = VersionHandler.getServerVersion();
            ClientLoginCount clc = ObjectHandler.getClientLoginCount(initConnection);

            if ((null != parser.getOptionValue(this.sendonly) &&
                    (null != parser.getOptionValue(this.displayonly)))) {
                System.err.println("More than one of the stat options given. Using the default one one only (display and send)");
                new TransportHandler().sendReport(totals, contextDetails, versions, clc);
                print(totals, contextDetails, versions, parser, clc);
            } else {
                int count = 0;
                if (null != parser.getOptionValue(this.sendonly)) {
                    new TransportHandler().sendReport(totals, contextDetails, versions, clc);
                    count++;
                }
                if (null != parser.getOptionValue(this.displayonly)) {
                    if (0 == count) {
                        print(totals, contextDetails, versions, parser, clc);
                    }
                    count++;
                }
                if (0 == count) {
                    System.err.println("No option selected. Using the default one one (display and send)");
                    new TransportHandler().sendReport(totals, contextDetails, versions, clc);
                    print(totals, contextDetails, versions, parser, clc);
                }
            }
        } catch (MalformedObjectNameException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (NullPointerException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (JSONException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (InvalidAttributeValueException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (Exception e) {
            printServerException(e, parser);
            sysexit(1);
        }
    }

    @Override
    public void setFurtherOptions(AdminParser parser) {
        this.sendonly = setShortLongOpt(parser,
                OPT_SEND_ONLY_SHORT,
                OPT_SEND_ONLY_LONG,
                "Send report without displaying it (Disables default)",
                false,
                NeededQuadState.notneeded);
        this.displayonly = setShortLongOpt(parser, OPT_DISPLAY_ONLY_SHORT,
                OPT_DISPLAY_ONLY_LONG,
                "Display report without sending it (Disables default)",
                false,
                NeededQuadState.notneeded);
        this.csv = setShortLongOpt(parser, OPT_CSV_SHORT,
                OPT_CSV_LONG,
                "Show output as CSV",
                false,
                NeededQuadState.notneeded);
    }

    private void print(List<Total> totals, List<ContextDetail> contextDetails, String[] versions, final AdminParser parser, ClientLoginCount clc) {
        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createVersionList(versions)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createVersionList(versions)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createTotalList(totals)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] { new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT) }, ObjectHandler.createTotalList(totals)).write();
        }

        System.out.println("");

        if (null != parser.getOptionValue(this.csv)) {
            new CSVWriter(System.out, ObjectHandler.createDetailList(contextDetails)).write();
        } else {
            new TableWriter(System.out, new ColumnFormat[] {
                new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT), new ColumnFormat(Align.LEFT),
                new ColumnFormat(Align.LEFT) }, ObjectHandler.createDetailList(contextDetails)).write();
        }
    }

}
