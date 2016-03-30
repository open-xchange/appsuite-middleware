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

package com.openexchange.subscribe.crawler;

import java.util.ArrayList;
import java.util.TimeZone;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.subscribe.crawler.internal.AbstractStep;


/**
 * {@link TasksByICalFileStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class TasksByICalFileStep extends AbstractStep<Task[], Page> {
private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TasksByICalFileStep.class);

    public TasksByICalFileStep(){

    }

    @Override
    public void execute(WebClient webClient) {
        ArrayList<Task> tempTasks = new ArrayList<Task>();
        ArrayList<Task> tasks = new ArrayList<Task>();

        try {
            LOG.debug("This should be an iCal-File : \n{}", input.getWebResponse().getContentAsString());
            String iCalFile = input.getWebResponse().getContentAsString();
            ICalParser iCalParser = workflow.getActivator().getICalParser();

            if (iCalParser != null) {
                tempTasks = (ArrayList<Task>) iCalParser.parseTasks(
                    iCalFile,
                    TimeZone.getDefault(),
                    new ContextImpl(23),
                    new ArrayList<ConversionError>(),
                    new ArrayList<ConversionWarning>());
            } else {
                LOG.error("No iCal-Parser found!");
            }
            tasks.addAll(tempTasks);

        } catch (ConversionError e) {
            LOG.error("", e);
        } catch (FailingHttpStatusCodeException e) {
            LOG.error("", e);
        }

        output = new Task[tasks.size()];
        for (int i = 0; i < tasks.size() && i < output.length; i++) {
            output[i] = tasks.get(i);
        }
        executedSuccessfully = true;
    }
}
