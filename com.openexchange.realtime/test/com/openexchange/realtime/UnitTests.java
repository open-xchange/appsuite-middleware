
package com.openexchange.realtime;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.realtime.packet.IDComponentsParserTest;
import com.openexchange.realtime.packet.IDTest;
import com.openexchange.realtime.packet.PresenceTest;
import com.openexchange.realtime.payload.FluidPayloadTreeNodeBuilderTest;
import com.openexchange.realtime.payload.PayloadTreeNodeTest;
import com.openexchange.realtime.payload.PayloadTreeTest;
import com.openexchange.realtime.util.ElementPathTest;
import com.openexchange.realtime.util.StanzaSequenceGateTest;

@RunWith(Suite.class)
@SuiteClasses({
    ElementPathTest.class,
    StanzaSequenceGateTest.class,
    PresenceTest.class,
    IDTest.class,
    IDComponentsParserTest.class,
    FluidPayloadTreeNodeBuilderTest.class,
    PayloadTreeNodeTest.class,
    PayloadTreeTest.class
})
public class UnitTests {

}
