package com.amazonaws.serverless.proxy.spark.embeddedserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.jetty.websocket.WebSocketHandlerWrapper;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.ssl.SslStores;
import spark.staticfiles.StaticFilesConfiguration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class LambdaEmbeddedServer
        implements EmbeddedServer {

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------

    private Routes applicationRoutes;
    private MatcherFilter sparkFilter;
    private StaticFilesConfiguration staticFilesConfiguration;
    private boolean hasMultipleHandler;
    private Logger log = LoggerFactory.getLogger(LambdaEmbeddedServer.class);


    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    LambdaEmbeddedServer(Routes routes, StaticFilesConfiguration filesConfig, boolean multipleHandlers) {
        applicationRoutes = routes;
        staticFilesConfiguration = filesConfig;
        hasMultipleHandler = multipleHandlers;
    }


    //-------------------------------------------------------------
    // Implementation - EmbeddedServer
    //-------------------------------------------------------------

    @Override
    public int ignite(String host,
                      int port,
                      SslStores sslStores,
                      CountDownLatch countDownLatch,
                      int maxThreads,
                      int minThreads,
                      int threadIdleTimeoutMillis) {
        sparkFilter = new MatcherFilter(applicationRoutes, staticFilesConfiguration, false, hasMultipleHandler);
        sparkFilter.init(null);

        countDownLatch.countDown();

        return 0;
    }


    public void configureWebSockets(Map<String, WebSocketHandlerWrapper> webSocketHandlers,
                                    Optional<Integer> webSocketIdleTimeoutMillis) {
        // Swallowing this exception to prevent Spark from getting stuck
        // throw new UnsupportedOperationException();
        log.info("Spark called configureWebSockets. However, web sockets are not supported");
    }


    @Override
    public void extinguish() {
    }


    //-------------------------------------------------------------
    // Methods - Public
    //-------------------------------------------------------------

    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        //RouteMatch route = applicationRoutes.find(HttpMethod.get(request.requestMethod()), request.contextPath(), "*/*");
        sparkFilter.doFilter(request, response, null);
    }
}
