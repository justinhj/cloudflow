package com.example;

//tag::imports[]
import cloudflow.streamlets.StreamletExecution;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.*;
import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.scalatestplus.junit.JUnitSuite;

import scala.concurrent.duration.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import cloudflow.flink.testkit.*;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import cloudflow.flink.*;
import scala.concurrent.Await;
//end::imports[]

//tag::test[]
// 1. Extend from the abstract class JUnitSuite
public class FlinkProcessorTest extends JUnitSuite {

  @Test
  public void shouldProcessDataWhenItIsRun() throws TimeoutException, InterruptedException {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    // 2. Instantiate the testkit class FlinkTestkit
    FlinkTestkit testkit = new FlinkTestkit() {};

    // 3. Create the FlinkStreamlet to test
    FlinkProcessor streamlet = new FlinkProcessor();

    // 4. Prepare data to be pushed into inlet ports
    List<Integer> range = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());
    List<Data> data = range.stream().map((Integer i) -> new Data(i, "name" + i.toString())).collect(Collectors.toList());

    // 5. Setup inlet taps that tap the inlet ports of the streamlet
    FlinkInletTap<Data> in = testkit.<Data>getInletAsTap(streamlet.in,
      env.<Data>addSource(
        FlinkSource.<Data>collectionSourceFunction(data),
        TypeInformation.<Data>of(Data.class)
      ),
      Data.class
    );

    // 6. Setup outlet taps for outlet ports
    FlinkOutletTap<Data> out = testkit.getOutletAsTap(streamlet.out, Data.class);

    Config config = ConfigFactory.parseString("cloudflow.streamlets.testFlinkStreamlet.test = 1");
    TestFlinkStreamletContext ctx = new TestFlinkStreamletContext(
            "testFlinkStreamlet",
            new org.apache.flink.streaming.api.scala.StreamExecutionEnvironment(env),
            scala.collection.JavaConversions.asScalaBuffer(Collections.singletonList(in)),
            scala.collection.JavaConversions.asScalaBuffer(Collections.singletonList(out)),
            config
    );
    streamlet.setContext(ctx);
    StreamletExecution se = streamlet.run(ctx);
    Await.ready(se.ready(), Duration.apply(20, TimeUnit.SECONDS));

    // 7. Run the streamlet using the `run` method that the testkit offers
    //testkit.run(streamlet, Collections.singletonList(in), Collections.singletonList(out), env);

    // 8. Write assertions to ensure that the expected results match the actual ones
    assertTrue(TestFlinkStreamletContext.result().contains((new Data(2 ,"name2").toString())));
    assertEquals(TestFlinkStreamletContext.result().size(), 5);
  }
}
//end::test[]
