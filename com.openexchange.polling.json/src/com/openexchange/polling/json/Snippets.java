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
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.polling.Poll;
import com.openexchange.polling.Question;
import com.openexchange.tools.servlet.AjaxException;


/**
 * {@link Snippets}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Snippets {

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
    
}
