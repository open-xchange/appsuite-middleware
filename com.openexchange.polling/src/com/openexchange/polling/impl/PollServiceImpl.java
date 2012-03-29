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

package com.openexchange.polling.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.openexchange.polling.Answer;
import com.openexchange.polling.Poll;
import com.openexchange.polling.PollService;
import com.openexchange.polling.Question;


/**
 * {@link PollServiceImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PollServiceImpl implements PollService {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    private final Map<Integer, Poll> polls = new ConcurrentHashMap<Integer, Poll>();

    @Override
    public void createPoll(Poll poll, int cid) {
        int newID = ID_COUNTER.incrementAndGet();

        poll.setId(newID);

        polls.put(newID, poll);
    }

    @Override
    public void deletePoll(int id, int cid) {
        polls.remove(id);
    }

    @Override
    public Poll getPoll(int id, int cid) {
        return polls.get(id);
    }

    @Override
    public List<Poll> getPolls(int cid) {
        return new ArrayList<Poll>(polls.values());
    }

    @Override
    public void saveAnswers(int id, int cid, Answer answer) {
        Poll poll = polls.get(id);
        if (poll == null) {
            return;
        }

        List<Question> questions = poll.getQuestions();
        List<Integer> answers = answer.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            questions.get(i).incAnswerCount(answers.get(i));
        }
    }

    @Override
    public void updatePoll(Poll poll, int cid) {
        polls.put(poll.getId(), poll);
    }

}
