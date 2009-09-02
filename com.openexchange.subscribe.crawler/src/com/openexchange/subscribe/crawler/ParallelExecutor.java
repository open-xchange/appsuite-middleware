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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.timer.TimerService;


/**
 * {@link ParallelExecutor}
 * This executes several requests for HtmlPages containing contact info in parallel and unites their results.
 * This saves time normally lost waiting for web requests. 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ParallelExecutor{
    
    private ArrayList<Contact> results;
    
    public ParallelExecutor(){
        results = new ArrayList<Contact>();
    }
    
    public List<List<HtmlAnchor>> splitIntoTasks(List<HtmlAnchor> list){
        List<List<HtmlAnchor>> sublists = new ArrayList<List<HtmlAnchor>>();
        List<HtmlAnchor> sublist = new ArrayList<HtmlAnchor>();
        int index = 1;
        for (HtmlAnchor anchor : list){
            sublist.add(anchor);
            
            if (index % 10 == 1){
                sublists.add(sublist);
            }
            
            // create a new sublist for every 10 Links
            if (index % 10 == 0){
                
                sublist = new ArrayList<HtmlAnchor>();
            }
                
            index ++;
        }
        //System.out.println("***** No. of tasks : "+sublists.size());
        return sublists;
    }
    
    public ArrayList<Contact> execute(List<Callable<ArrayList<Contact>>> callables) throws SubscriptionException{
        final CompletionService<ArrayList<Contact>> completionService = new ExecutorCompletionService<ArrayList<Contact>>(
            ServerServiceRegistry.getInstance().getService(TimerService.class).getExecutor());
        
        for (Callable callable : callables) {
            completionService.submit(callable);
        }
        /*
         * Wait for completion
         */
        final int maxRunningMillis = 60000;
        try {
                for (int i = 0; i < callables.size(); i++) {
                    final Future<ArrayList<Contact>> f = completionService.poll(maxRunningMillis, TimeUnit.MILLISECONDS);
                    if (null != f) {
                        results.addAll((ArrayList<Contact>)f.get());
                    }
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SubscriptionException(null, null, null);
            } catch (final ExecutionException e) {
                final Throwable t = e.getCause();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new IllegalStateException("Not unchecked", t);
                }
            }
            return results;
    }
}
