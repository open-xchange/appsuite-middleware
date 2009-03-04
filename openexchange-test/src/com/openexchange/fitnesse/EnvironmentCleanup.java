package com.openexchange.fitnesse;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * {@link EnvironmentCleanup} - a small wrapper to call the clean-up process
 * on the FitNesse environment.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class EnvironmentCleanup {
    public List doTable(List<List<String>> table){
        FitnesseEnvironment.purgeInstance();
        return new LinkedList();
    }
}
