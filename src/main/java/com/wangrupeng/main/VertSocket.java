package com.wangrupeng.main;

import com.google.gson.Gson;
import com.wangrupeng.config.Config;
import com.wangrupeng.util.FileHelper;
import com.wangrupeng.util.FileLogger;
import com.wangrupeng.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

import java.io.File;

/**
 * 使用Vert web服务框架，底层使用的websocket技术。它会开放一个监听端口，每当前端建立一个连接之后，它会将实时监听的redis消息转发到前端.
 * @author <a href="http://datacoder.top">王汝鹏</a>
 * @version 1.0
 */
public class VertSocket extends AbstractVerticle {

  private static FileLogger fileLogger;
  private static String TAG = "VertSocket";
  private static String configPath = "/home/hadoop/wrp/redis-listener/vert_redis.json";

  /**
   * 主程序入口.
   * @param args 若args数组大小为1则代表配置文件的地址；若为零，则选择默认的地址读取配置文件
   */
  public static void main(String[] args) {
    if (args.length > 0) {
      configPath = args[0];

    } else {
      fileLogger = new FileLogger("/home/hadoop/wrp/redis-listener/redis-log/VertSocket.log");
    }
    Runner.runExample(VertSocket.class);
  }

  @Override
  public void start() throws Exception {
    String txt = FileHelper.readString(configPath);
    Gson gson = new Gson();
    Config clientConfig = gson.fromJson(txt, Config.class);
    System.out.println("Config file content : " + txt);
    fileLogger = new FileLogger(clientConfig.logDir + "Vertsocket.log");
    int port = clientConfig.vert_port;
    String channel = clientConfig.channel;
    RedisOptions config = new RedisOptions()
        .setHost(clientConfig.redis_host)
        .setPort(clientConfig.redis_port)
            /*.setAuth(clientConfig.redis_author)*/;

    Router router = Router.router(vertx);

    BridgeOptions options = new BridgeOptions()
        .addOutboundPermitted(new PermittedOptions().setAddress("news-feed"));
    router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options, event -> {
      if (event.type() == BridgeEventType.SOCKET_CREATED) {
        System.out.println("A socket was created");
        fileLogger.log(TAG, "A socket was created");
      }
      // This signals that it's ok to process the event
      event.complete(true);
    }));

    // Serve the static resources
    router.route().handler(StaticHandler.create());
    vertx.createHttpServer().requestHandler(router::accept).listen(port);
    vertx.eventBus().<JsonObject>consumer("io.vertx.redis." + channel, received -> {
      JsonObject value = received.body().getJsonObject("value");
      System.out.println(value);
      fileLogger.log(TAG, "Receive message : " + value);
      vertx.eventBus().publish("news-feed", value.getValue("message"));
    });

    //redis client subscribe a channel
    RedisClient redisClient = RedisClient.create(vertx, config);

    redisClient.subscribe(channel, res -> {
      if (res.succeeded()) {
        System.out.println(
            "Subscribe channel : " + channel + ", address is http://" + clientConfig.redis_host
                + ":" + port + "/eventbus");
        fileLogger.log(TAG,
            "Subscribe channel : " + channel + ", address is http://" + clientConfig.redis_host
                + ":" + port + "/eventbus");
        fileLogger.log(TAG, res.result().toString());
      } else {
        fileLogger.log(TAG, "Redis channel " + channel + " failed!");
      }
      //System.out.println(res.result());

    });
  }
}
