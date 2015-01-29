package org.esmerilprogramming.cloverx.server;


import io.undertow.Undertow;
import org.esmerilprogramming.cloverx.server.handlers.PreBuildHandler;
import org.esmerilprogramming.cloverx.server.handlers.PreBuildHandlerImpl;
import org.jboss.logging.Logger;

import javax.servlet.ServletException;
import java.io.IOException;

import static io.undertow.Handlers.path;

public final class CloverX {

  private static final Logger LOGGER = Logger.getLogger(CloverX.class);

  private Undertow server;
  
  public CloverX( CloverXConfiguration configuration ){
    try{
      start(configuration);
    }catch(RuntimeException e){
      LOGGER.error("Error on startup");
      LOGGER.error( e.getMessage() );
      e.printStackTrace();
    }
  }

  public CloverX() {
    this( new ConfigurationBuilder().defaultConfiguration() );
  }

  private void start( CloverXConfiguration configuration ) throws RuntimeException {
    LOGGER.info("ignition...");
    try {
      server = buildServer( configuration );
    } catch (ServletException | IOException e) {
      e.printStackTrace();
    }
    server.start();
    LOGGER.info("Enjoy it! http://" + configuration.getHost()
        + ":" + configuration.getPort()
        + "/" + configuration.getAppContext() );
  }
  
  public void stop(){
    server.stop();
  }

  private Undertow buildServer( CloverXConfiguration configuration ) throws ServletException, IOException {
    PreBuildHandler preBuildHandler = new PreBuildHandlerImpl();
    preBuildHandler.prepareBuild(configuration);
    return Undertow.builder()
        .addHttpListener( configuration.getPort() ,  configuration.getHost() )
        .setHandler(
            path()
            .addPrefixPath("/" + configuration.getAppContext() , preBuildHandler.createAppHandlers() )
            .addPrefixPath("/" + configuration.getStaticRootPath() , new ResourceHandlerMounter()
            .mount()))
        .build();
  }
  
  public Undertow getServer() {
    return server;
  }

  public static void main(String[] args) {
    new CloverX(new ConfigurationBuilder()
      .withPackageToScan("org.esmerilprogramming.cloverx.management").shouldRunManagement(true)
      .withHost("0.0.0.0")
      .withPort(8080)
      .build());
  }

}