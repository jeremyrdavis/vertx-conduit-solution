package io.vertx.conduit;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("Base Tests")
@ExtendWith(VertxExtension.class)
class MainVerticleTest {

  @Test
  @DisplayName("Server Started Test")
  @Timeout(5000)
  void testServerStart(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.create(vertx);

    Checkpoint deploymentCheckpoint = testContext.checkpoint();
    Checkpoint requestCheckpoint = testContext.checkpoint();

    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
      deploymentCheckpoint.flag();

//      System.out.println("Verticle deployed");

      webClient.get(8080, "localhost", "/")
        .as(BodyCodec.string())
        .send(testContext.succeeding(resp -> {

//          System.out.println("Response received");

          testContext.verify(() -> {
            assertEquals(200, resp.statusCode());
            assertEquals("Hello, CodeOne!", resp.body());
            requestCheckpoint.flag();
          });
        }));
    }));
  }


}
