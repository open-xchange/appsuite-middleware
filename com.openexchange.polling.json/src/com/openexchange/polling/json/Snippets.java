package com.openexchange.polling.json;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.exception.OXException;
import com.openexchange.polling.Poll;
import com.openexchange.polling.Question;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

public class Snippets {
	
	public void feature12345() {
		//...
	}
	
	public void a1() {
		int a = 12;
	}
	
	
	
	// The meat of this class. Here be dragons ;)
    private JSONObject convert(final Poll poll) throws OXException {
        try {
            final JSONObject object = new JSONObject();

            object.put("title", poll.getTitle());
            object.put("id", poll.getId());

            final List<Question> questions = poll.getQuestions();
            final JSONArray qArray = new JSONArray();
            for (final Question question : questions) {
                final JSONObject q = new JSONObject();
                q.put("question", question.getQuestion());

                final List<String> answerOptions = question.getAnswerOptions();
                final int[] answerCount = question.getAnswerCount();

                final JSONArray optArr = new JSONArray();
                final JSONArray countArr = new JSONArray();

                for (int i = 0; i < answerCount.length; i++) {
                    final String answerOption = answerOptions.get(i);
                    final int count = answerCount[i];

                    optArr.put(answerOption);
                    countArr.put(count);
                }

                q.put("options", optArr);
                q.put("count", countArr);

                qArray.put(q);
            }

            object.put("questions", qArray);
            return object;
        } catch (final JSONException x) {
            throw AjaxExceptionCodes.JSONError.create(x.getMessage());
        }
    }
    
	public void a2() {
		
	}

}
