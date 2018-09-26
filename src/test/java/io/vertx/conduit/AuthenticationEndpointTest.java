package io.vertx.conduit;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Authentication Endpoint Tests")
@ExtendWith(VertxExtension.class)
public class AuthenticationEndpointTest {

  @Test
  @DisplayName("Successful Authentication Test")
  void testSuccessfulAuthentication(Vertx vertx, VertxTestContext testContext) throws InterruptedException {

    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> {
      WebClient webClient = WebClient.create(vertx);
      webClient.post(8080, "localhost", "/api/users/login")
        .sendJsonObject(new JsonObject()
          .put("user", new JsonObject()
            .put("email", "jake@jake.jake")
            .put("password", "jakejake")
          ), response -> testContext.verify(() -> {
          JsonObject user = response.result().bodyAsJsonObject().getJsonObject("user");
          assertEquals(200, response.result().statusCode());
          assertEquals("jake@jake.jake", user.getString("email"));
          assertEquals("jakejake", user.getString("password"));
          assertEquals("jwt.token.here", user.getString("token"));
          assertEquals("jake", user.getString("username"));
          assertEquals("I work at statefarm", user.getString("bio"));
          assertEquals("", user.getString("image"));
          testContext.completeNow();
        }));
    }));
  }

}
