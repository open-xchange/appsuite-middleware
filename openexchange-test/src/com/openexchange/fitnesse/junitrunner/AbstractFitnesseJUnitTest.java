
package com.openexchange.fitnesse.junitrunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import com.neuri.trinidad.JUnitHelper;
import com.neuri.trinidad.TestEngine;
import com.neuri.trinidad.TestRunner;
import com.neuri.trinidad.fitnesserunner.FitNesseRepository;
import com.neuri.trinidad.fitnesserunner.FitTestEngine;
import com.neuri.trinidad.fitnesserunner.SlimTestEngine;
import junit.framework.TestCase;

/**
 * 
 * {@link AbstractFitnesseJUnitTest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public abstract class AbstractFitnesseJUnitTest extends TestCase {

    protected Properties settings = new Properties();

    protected JUnitHelper helper;

    public AbstractFitnesseJUnitTest(String name) {
        super();
        try {
            settings.load(new FileInputStream("/Users/development/workspace/openexchange-test/conf/fitnesse.properties"));

            helper = new JUnitHelper(new TestRunner(
                initRepository(settings.getProperty("fitnesseWiki")),
                initEngine(settings.getProperty("engine")),
                initOutputPath(settings.getProperty("htmlOutputDir"))));

        } catch (IOException e) {
            fail("Instantiation failed: " + e.getMessage());
        }
    }

    protected TestEngine initEngine(String value) {
        if (value.equalsIgnoreCase("slim"))
            return new SlimTestEngine();
        return new FitTestEngine();
    }

    protected String initOutputPath(String value) {
        return new File(value).getAbsolutePath();
    }

    protected FitNesseRepository initRepository(String value) throws IOException {
        return new FitNesseRepository(value);
    }

}
