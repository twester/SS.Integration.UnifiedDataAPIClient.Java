package ss.udapi.sdk.examples.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ParticipantTest.class, RestItemTest.class, RestLinkTest.class,
    ServiceRequestTest.class, StreamEchoTest.class, SummaryTest.class,
    TagTest.class })
public class AllTests
{

}
