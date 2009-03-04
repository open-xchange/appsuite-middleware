package com.openexchange.fitnesse.tasks;

import java.util.List;
import com.openexchange.ajax.kata.tasks.TaskVerificationStep;
import com.openexchange.fitnesse.AbstractTableTable;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * {@link VerifyTask} - a wrapper to use TaskVerifyStep via FitNesse
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class VerifyTask extends AbstractTableTable {
    
    @Override
    public List doTable() throws Exception {
        final String fixtureName = data.getFixtureName();
        
        Task task = createTask(fixtureName, data);
        
        TaskVerificationStep taskStep = new TaskVerificationStep( task, data.getFixtureName() );
        taskStep.setIdentitySource( environment.getSymbol( fixtureName ) );
        taskStep.perform(environment.getClientForUser1());
        
        environment.registerStep(taskStep);
        
        return createReturnValues("pass");    
    }

}
