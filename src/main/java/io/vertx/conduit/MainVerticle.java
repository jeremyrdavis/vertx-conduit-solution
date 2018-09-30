package io.vertx.conduit;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {

  private JDBCAuth authProvider;

  private JDBCClient jdbcClient;

  @Override
  public void start(Future<Void> future) {

    jdbcClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", "jdbc:hsqldb:file:db/conduit")
      .put("driver_class", "org.hsqldb.jdbcDriver")
      .put("max_pool_size", 30));

    authProvider = JDBCAuth.create(vertx, jdbcClient);
    authProvider.setAuthenticationQuery("SELECT PASSWORD, PASSWORD_SALT FROM USER WHERE EMAIL = ?");

    Router baseRouter = Router.router(vertx);
    baseRouter.route("/").handler(this::indexHandler);

    Router apiRouter = Router.router(vertx);
    apiRouter.route("/*").handler(BodyHandler.create());
    apiRouter.post("/users/login").handler(this::loginHandler);

    baseRouter.mountSubRouter("/api", apiRouter);

    vertx.createHttpServer()
        .requestHandler(baseRouter::accept)
        .listen(8080, result ->{
          if (result.succeeded()) {
            future.complete();
          }else{
            future.fail(result.cause());
          }
        });
  }

  private void loginHandler(RoutingContext context){
    JsonObject user = context.getBodyAsJson().getJsonObject("user");
    user.put("username", "placeholder");

    JsonObject authInfo = new JsonObject()
      .put("username", user.getString("email"))
      .put("password", user.getString("password"));
    System.out.println(user);

    HttpServerResponse response = context.response();
    authProvider.authenticate(authInfo, (AsyncResult<User> ar) -> {
      if (ar.succeeded()) {
        System.out.println("Authentication succeeded");
        jdbcClient.getConnection(ar2 ->{
          if (ar2.succeeded()) {
            System.out.println("Connection succeeded");
            SQLConnection connection = ar2.result();
            connection.queryWithParams("SELECT * FROM USER WHERE EMAIL = ?", new JsonArray().add(user.getString("email")), fetch ->{
              if (fetch.succeeded()) {
                JsonObject res = new JsonObject();
                ResultSet resultSet = fetch.result();
                System.out.println("Query returned " + resultSet.getNumRows() + " results");
                if (resultSet.getNumRows() == 0) {
                  res.put("found", false);
                  response.setStatusCode(200)
                    .putHeader("Content-Type", "text/html")
                    .putHeader("Content-Length", String.valueOf("Not Found".length()))
                    .end(fetch.cause().toString());
                } else {
                  res.put("found", true);
                  JsonArray row = resultSet.getResults().get(0);
                  JsonObject retVal = new JsonObject();
                  retVal.put("username", row.getString(1));
                  retVal.put("email", row.getString(2));
                  retVal.put("bio", row.getString(3));
                  retVal.put("image", row.getString(4));
                  res.put("user", retVal);
                  response.setStatusCode(200)
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .putHeader("Content-Length", String.valueOf(res.toString().length()))
                    .end(res.encode());
                }
              } else{
                response.setStatusCode(200)
                  .putHeader("Content-Type", "text/html")
                  .end(fetch.cause().toString());
              }
            });
          }else{
            response.setStatusCode(200)
              .putHeader("Content-Type", "text/html")
              .end(ar2.cause().toString());
          }
        });

      }else{
        response.setStatusCode(200)
          .putHeader("Content-Type", "text/html")
          .end("Authentication Failed: " + ar.cause());
      }
    });

  }

/*
  private void loginHandler(RoutingContext context) {
    JsonObject user = context.getBodyAsJson().getJsonObject("user");
    user.put("username", "placeholder");

    JsonObject authInfo = new JsonObject()
      .put("username", user.getString("email"))
      .put("password", user.getString("password"));

    System.out.println("loginHandler: " + user);

    HttpServerResponse response = context.response();
    String failureMessage = "Authentication Failed";

    authProvider.authenticate(authInfo, ar -> {
      if (ar.succeeded()) {

        System.out.println("Authentication succeeded");

        JsonObject returnValue = new JsonObject();

        jdbcClient.getConnection(ar2 -> {
          if (ar2.succeeded()) {

            System.out.println("Connection established");

            SQLConnection connection = ar2.result();
            connection.queryWithParams("SELECT * FROM USER WHERE EMAIL = ?", new JsonArray().add(user.getString("email")), fetch ->{
              if (fetch.succeeded()) {

                System.out.println("User retrieved");

                ResultSet resultSet = fetch.result();
                if (resultSet.getNumRows() >= 1) {

                  JsonArray row = resultSet.getResults().get(0);
                  returnValue.put("username", row.getString(1));
                  returnValue.put("email", row.getString(2));
                  returnValue.put("bio", row.getString(3));
                  returnValue.put("image", row.getString(4));

                  System.out.println("returnValue: " + returnValue);

                  response.setStatusCode(200)
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .putHeader("Content-Length", String.valueOf(returnValue.toString().length()))
                    .end(returnValue.encode());
                }else{
                  response.setStatusCode(500)
                    .putHeader("Content-Type", "text/html")
                    .end("No rows retrieved " + fetch.cause());
                }
              }else{
                response.setStatusCode(500)
                  .putHeader("Content-Type", "text/html")
                  .end("Query failed " + fetch.cause());
              }
            });

          }
        });
      }else{
        response.setStatusCode(200)
          .putHeader("Content-Type", "text/html")
          .end("Authentication Failed: " + ar.cause());
      }
    });
  }
*/
  private void indexHandler(RoutingContext context) {
    HttpServerResponse response = context.response();
    response.putHeader("Content-Type", "text/html").end("Hello, CodeOne!");
  }

}
