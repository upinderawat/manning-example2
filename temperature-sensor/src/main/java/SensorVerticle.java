import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

public class SensorVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(SensorVerticle.class);
  private static final int httpPort = Integer.parseInt(System.getenv().getOrDefault("HTTP_PORT", "8080"));

  private final String uuid = UUID.randomUUID().toString();
  private double temperature = 21.0;
  private final Random random = new Random();

  @Override
  public void start(Promise<Void> startPromise){
    vertx.setPeriodic(2000, this::updateTemperature);

    Router router = Router.router(vertx);
    router.get("/data").handler(this::getData);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(httpPort)
      .onSuccess(ok -> {
        logger.info("http server running: http://localhost:{}", httpPort);
        startPromise.complete();
      })
      .onFailure(startPromise::fail);
  }

  private void getData(RoutingContext routingContext) {
    logger.info("Processing HTTP request from {}", routingContext.request().remoteAddress());
    JsonObject payload = new JsonObject()
      .put("uuid", uuid)
      .put("temperature", temperature)
      .put("timestamp", System.currentTimeMillis());

    routingContext.response()
      .putHeader("Content-Type", "application/json")
      .setStatusCode(200)
      .end(payload.encode());
  }

  private void updateTemperature(Long aLong) {
    temperature = temperature + (random.nextGaussian()/ 2.0d);
    logger.info("Temperature updated: "+ temperature);
  }
}
