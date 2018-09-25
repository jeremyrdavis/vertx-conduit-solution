package io.vertx.conduit;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> future) {

    Router router = Router.router(vertx);
    router.route("/").handler(this::indexHandler);

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(8080, result ->{
          if (result.succeeded()) {
            future.complete();
          }else{
            future.fail(result.cause());
          }
        });
  }

  private void indexHandler(RoutingContext context) {
    HttpServerResponse response = context.response();
    response.putHeader("Content-Type", "text/html").end("Hello, CodeOne!");

  }

}
