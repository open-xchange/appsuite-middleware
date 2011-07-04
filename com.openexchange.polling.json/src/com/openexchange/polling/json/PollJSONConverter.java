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

package com.openexchange.polling.json;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.polling.Poll;
import com.openexchange.polling.Question;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link PollJSONConverter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
// The PollJSONConverter tells the framework it knows how to turn a poll into a JSONObject
// Note that it can also handle Lists of Polls. The format specification is essentially a contract between the actions and a converter. Everything an action spits out in a certain format, the corresponding
// Converter has to know how to handle. As a convention, converters for older style modules should know how to write single objects and search iterators, more modern modules will usually work with lists.
public class PollJSONConverter implements ResultConverter {

    public void convert(AJAXRequestData request, AJAXRequestResult result, ServerSession session, Converter converter) throws AbstractOXException {
    	Object resultObject = result.getResultObject();
    	
    	if (resultObject instanceof Poll) {
        	Poll poll = (Poll) result.getResultObject();
            
            JSONObject object = convert(poll);
            
            result.setResultObject(object, "json");
    	} else if (resultObject instanceof List) {
    		List<Poll> polls = (List<Poll>) resultObject;
    		JSONArray arr = new JSONArray();
    		for (Poll poll : polls) {
				arr.put(convert(poll));
			}
    		
    		result.setResultObject(arr, "json");
    	}
    }

    // The input format we accept. This converter accepts everything labelled poll
    public String getInputFormat() {
        return "poll";
    }

    // And it spits out json
    public String getOutputFormat() {
        return "json";
    }

    // And we consider it a pretty good conversion
    public Quality getQuality() {
        return Quality.GOOD;
    }
    
    // The meat of this class. Here be dragons ;)
    private JSONObject convert(Poll poll) throws AbstractOXException {
        try {
            JSONObject object = new JSONObject();

            object.put("title", poll.getTitle());
            object.put("id", poll.getId());

            List<Question> questions = poll.getQuestions();
            JSONArray qArray = new JSONArray();
            for (Question question : questions) {
                JSONObject q = new JSONObject();
                q.put("question", question.getQuestion());

                List<String> answerOptions = question.getAnswerOptions();
                int[] answerCount = question.getAnswerCount();

                JSONArray optArr = new JSONArray();
                JSONArray countArr = new JSONArray();

                for (int i = 0; i < answerCount.length; i++) {
                    String answerOption = answerOptions.get(i);
                    int count = answerCount[i];

                    optArr.put(answerOption);
                    countArr.put(count);
                }

                q.put("options", optArr);
                q.put("count", countArr);

                qArray.put(q);
            }

            object.put("questions", qArray);
            return object;
        } catch (JSONException x) {
            throw new AjaxException(AjaxException.Code.JSONError, x.getMessage());
        }
    }
    
    private void test() {
    	
    }

}
