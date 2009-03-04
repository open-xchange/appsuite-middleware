package com.openexchange.fitnesse.tasks;

import java.util.List;
import com.openexchange.ajax.kata.tasks.TaskUpdateStep;
import com.openexchange.fitnesse.AbstractTableTable;
import com.openexchange.fitnesse.wrappers.FitnesseResult;
import com.openexchange.groupware.tasks.Task;

/**
 * 
 * {@link UpdateTask} - a wrapper to use TaskUpdateStep via FitNesse
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class UpdateTask extends AbstractTableTable {
    
    @Override
    public List doTable() throws Exception {
        final String fixtureName = data.getFixtureName();
        Task task = createTask(fixtureName, data);
        
        TaskUpdateStep taskStep = new TaskUpdateStep( task, data.getFixtureName(), null );
        taskStep.setIdentitySource( environment.getSymbol( fixtureName ) );
        taskStep.perform( environment.getClientForUser1() );
        
        environment.registerStep(taskStep);
        
        return (new FitnesseResult(data, FitnesseResult.PASS)).toResult();
    }

}
