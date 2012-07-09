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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.preview.thirdwing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import net.thirdwing.common.IContentIterator;
import net.thirdwing.common.IConversionJob;
import net.thirdwing.common.UpdateMessages;
import net.thirdwing.exception.XHTMLConversionException;
import net.thirdwing.io.IOUnit;

import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link TransformationObservationTask}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class TransformationObservationTask implements Observer {

    /*
     * TODO:
     * We have to recognize the following patterns:
     * url(filename.ext)
     * href="/filename.html"
     * rel="filename.html"
     */

    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern URL_PATTERN = Pattern.compile("url\\([^\\)]*\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HREF_PATTERN = Pattern.compile("href=\"[^#\"]*\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern REL_PATTERN = Pattern.compile("rel=\"[^\"]*\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    private static final Pattern IMG_FILENAME_PATTERN = Pattern.compile("src=['\"]?([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)['\"]?", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_FILENAME_PATTERN = Pattern.compile("url\\(([0-9a-z&&[^.\\s>\"\\)]]+\\.[0-9a-z&&[^.\\s>\"\\)]]+)\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HREF_FILENAME_PATTERN = Pattern.compile("href=['\"]?([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)['\"]?", Pattern.CASE_INSENSITIVE);
    private static final Pattern REL_FILENAME_PATTERN = Pattern.compile("rel=['\"]?([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)['\"]?", Pattern.CASE_INSENSITIVE);

    private final StreamProvider streamProvider;

    private final Session session;

    private final AtomicBoolean done;
    private final AtomicBoolean hasMore;
    
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition signal = lock.newCondition();
    private final AtomicBoolean pageRendered;
    
    
    private final List<String> content;

    private OXException exception;
	private final int pages;
	private final IConversionJob transformer;
	private final File file;
	private IContentIterator contentIterator;


    public TransformationObservationTask(final StreamProvider streamProvider, final Session session, int pages, IConversionJob transformer, File file) {
        super();
        this.streamProvider = streamProvider;
        this.session = session;
        done = new AtomicBoolean(false);
        hasMore = new AtomicBoolean(true);
        pageRendered = new AtomicBoolean(true);
        
        content = new ArrayList<String>();
        this.pages = pages;
        this.transformer = transformer;
        this.file = file;
    }

    public List<String> call() {
    	
    	FileInputStream fis = null;
    	try {
        	transformer.addObserver(this);
        	IOUnit unit = new IOUnit((fis = new FileInputStream(file)));
        	unit.setStreamProvider(streamProvider);
        	contentIterator = transformer.transformDocument(unit, 80, true); 
        	while (contentIterator.hasNext() && !done.get()) {
        		try {
        			contentIterator.writeNextContent();
        		} finally {
        		}
				// Wait for Event
				
				
            }
    	} catch (FileNotFoundException e) {
			exception = PreviewExceptionCodes.ERROR.create();
		} catch (XHTMLConversionException e) {
			exception = PreviewExceptionCodes.ERROR.create();
		} finally {
		    if (contentIterator != null) {
		        contentIterator.releaseData();
		    }
    		Streams.close(fis);
    	}

    	System.out.println(content);
        return content;
    }

    public OXException getException() {
        return exception;
    }

    @Override
    public void update(final Observable o, final Object obj) {
        final UpdateMessages message = (UpdateMessages) obj;
        final String key = message.getKey();
        //System.out.println("UpdateMessage.getKey(): " + key);
        boolean htmlFinished = key.equals(UpdateMessages.HTML_TRANSFORMATION_FINISHED);
		boolean pageFinished = key.equals(UpdateMessages.PAGE_TRANSFORMATION_FINISHED);
		if (htmlFinished || pageFinished) {
            try {
                //Thread.sleep(2000);
            	String page = streamProvider.getDocumentContent();
            	//System.out.println(page);
            	if (htmlFinished) {
    				//this.content.set(this.content.size() - 1, page); // *shrugs* Recovery. For some reason the page before the last is reported twice
                    //this.content.add(page);
            	} else {
    				this.content.add(page);
            	}
            } catch (final OXException e) {
                exception = e;

            } finally {
            	if ((pages != -1 && this.content.size() >= pages) || htmlFinished) {
                    done.compareAndSet(false, true);
            	}
            	if (htmlFinished) {
            		hasMore.set(false);
            	}
            }
        } else if (key.equals(UpdateMessages.HTML_TRANSFORMATION_FAILED)) {
            final Exception e = (Exception) message.getData();
            exception = PreviewExceptionCodes.ERROR.create(e);
            done.compareAndSet(false, true);
        } else if (key.equals(UpdateMessages.PREVIEW_IMAGE_CREATION_STARTED) || message.getKey().equals(UpdateMessages.PREVIEW_IMAGE_CREATION_FAILED)) {
        }
    }
    
    public boolean hasMoreContent() {
    	return hasMore.get();
    }
}
