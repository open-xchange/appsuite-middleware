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

package com.openexchange.preview.thirdwing;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.thirdwing.common.UpdateMessages;
import com.openexchange.exception.OXException;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.tools.regex.MatcherReplacer;

/**
 * {@link TransformationObservationTask}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class TransformationObservationTask extends AbstractTask<String> implements Observer {

    private static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern FILENAME_PATTERN = Pattern.compile(
        "src=['\"]?([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)['\"]?",
        Pattern.CASE_INSENSITIVE);

    private StreamProvider streamProvider;

    private Session session;
    
    private AtomicBoolean done;
    
    private String content;
    
    private OXException exception;
    

    public TransformationObservationTask(StreamProvider streamProvider, Session session) {
        super();
        this.streamProvider = streamProvider;
        this.session = session;
        done = new AtomicBoolean(false);
    }
    
    @Override
    public String call() {
        while (!done.get());
        
        return content;
    }
    
    public OXException getException() {
        return exception;
    }

    @Override
    public void update(Observable o, Object obj) {
        UpdateMessages message = (UpdateMessages) obj;
        if (message.getKey().equals(UpdateMessages.HTML_TRANSFORMATION_FINISHED)) {
            try {
                String content = streamProvider.getDocumentContent();
                this.content = replaceLinks(content);                
            } catch (OXException e) {
                exception = e;
            } finally {
                done.compareAndSet(false, true);
            }
        } else if (message.getKey().equals(UpdateMessages.HTML_TRANSFORMATION_FAILED)) {
            Exception e = (Exception) message.getData();
            exception = PreviewExceptionCodes.ERROR.create(e);
            done.compareAndSet(false, true);
        }        
    }

    private String replaceLinks(String content) throws OXException {
        String retval = content;
        Matcher imgMatcher = IMG_PATTERN.matcher(retval);
        MatcherReplacer imgReplacer = new MatcherReplacer(imgMatcher, retval);
        StringBuilder sb = new StringBuilder(retval.length());
        if (imgMatcher.find()) {
            final StringBuilder strBuffer = new StringBuilder(256);
            final MatcherReplacer mr = new MatcherReplacer();
            final StringBuilder linkBuilder = new StringBuilder(256);
            do {
                String imgTag = imgMatcher.group();
                strBuffer.setLength(0);
                final Matcher m = FILENAME_PATTERN.matcher(imgTag);
                mr.resetTo(m, imgTag);
                if (m.find()) {
                    final String fileName = m.group(1);
                    /*
                     * Compose corresponding image data
                     */
                    String imageURL = streamProvider.getLinkForFile(fileName, session);
                    if (imageURL == null) {
                        imageURL = fileName;
                    }

                    linkBuilder.setLength(0);
                    linkBuilder.append("src=").append('"').append(imageURL).append('"');
                    mr.appendLiteralReplacement(strBuffer, linkBuilder.toString());
                    
                    imgReplacer.appendLiteralReplacement(sb, strBuffer.toString());
                    strBuffer.setLength(0);
                }
                mr.appendTail(strBuffer);
            } while (imgMatcher.find());
        }
        imgReplacer.appendTail(sb);
        retval = sb.toString();

        return retval;
    }

}
