package io.vertx.conduit;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            ), testContext.succeeding(resp -> {
              testContext.verify(() -> {
                assertEquals(200, resp.statusCode(), "status code should be 200");
                JsonObject user = resp.bodyAsJsonObject().getJsonObject("user");
                assertNotNull(user);
                assertEquals("jake@jake.jake", user.getString("email"), "Email should be 'jake@jakejake'");
                assertEquals("jake", user.getString("username"), "username should not be 'jake");
                testContext.completeNow();
              });
          }));
//        .send(testContext.succeeding(resp -> {
//          testContext.verify(() -> {
//            assertEquals(200, resp.statusCode(), "status code should be 200");
//          });
//        }));
    }));

/*
      webClient.post(8080, "localhost", "/api/users/login")
        .sendJsonObject(new JsonObject()
          .put("user", new JsonObject()
            .put("email", "jake@jake.jake")
            .put("password", "jakejake")
          ), response -> {

          if (response.succeeded()) {
            JsonObject user = response.result().bodyAsJsonObject().getJsonObject("user");
            System.out.println("Response: " + user);
            assertEquals(200, response.result().statusCode());
            assertNotNull(user.getString("email"), "email should not be null");
            assertEquals("jake@jake.jake", user.getString("email"));
            assertNotNull(user.getString("password"), "password should not be null");
            assertEquals("jakejake", user.getString("password"));
            assertNotNull( user.getString("token"), "JWT Token should not be null");
            assertEquals("jwt.token.here", user.getString("token"));
            assertEquals("jake", user.getString("username"));
            assertEquals(user.getString("username"), "username should not be null");
            assertEquals("I work at statefarm", user.getString("bio"));
            assertEquals("", user.getString("image"));
            assertNotNull( user.getString("image"), "image should not be null");
            testContext.completeNow();
          }
        });
    }));
*/
  }

}
