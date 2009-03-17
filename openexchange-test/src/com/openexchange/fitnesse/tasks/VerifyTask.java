package com.openexchange.fitnesse.tasks;

import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.tasks.TaskVerificationStep;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * {@link VerifyTask} - a wrapper to use TaskVerifyStep via FitNesse
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class VerifyTask extends AbstractTaskFixture {

    @Override
    protected Step createStep(Task task, String fixtureName, String expectedError) {
        return new TaskVerificationStep(task, data.getFixtureName());
    }
    
   

}
