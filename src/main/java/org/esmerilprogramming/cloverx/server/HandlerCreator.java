package org.esmerilprogramming.cloverx.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.esmerilprogramming.cloverx.annotation.Page;
import org.esmerilprogramming.cloverx.http.CloverXRequest;
import org.esmerilprogramming.cloverx.http.CloverXSessionManager;
import org.esmerilprogramming.cloverx.http.HttpResponse;
import org.esmerilprogramming.cloverx.http.Response;
import org.esmerilprogramming.cloverx.http.converter.ParametersConverter;
import org.esmerilprogramming.cloverx.server.injection.CoreClassInjector;
import org.esmerilprogramming.cloverx.server.injection.CoreClassInjectorImpl;
import org.esmerilprogramming.cloverx.server.mounters.ConverterMounter;
import org.esmerilprogramming.cloverx.server.mounters.ConverterMounterImpl;
import org.jboss.logging.Logger;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.SessionAttachmentHandler;

public class HandlerCreator {
  
  private Class<?> pageClass;
  private Method method;
  private List<Method> beforeExecutemethods;
  private Paranamer paranamer;
  
  public HandlerCreator() {
    paranamer = new CachingParanamer(new BytecodeReadingParanamer());
  }
  
  public HandlerCreator forPageClass(Class<?> pageClass){
    this.pageClass = pageClass;
    return this;
  }
  
  public HandlerCreator withPathMethod(Method method){
    this.method = method;
    return this;
  }
  
  public HandlerCreator withExecuteBeforeMethods(List<Method> methods){
    this.beforeExecutemethods = methods;
    return this;
  }
  
  public HttpHandler mount(){
    return createHandlerForPage(pageClass, method , beforeExecutemethods);
  }
  
  private HttpHandler createHandlerForPage(final Class<?> pageClass , final Method method , final List<Method> beforeExecutemethods){
    final ConverterMounter converterMounter = new ConverterMounterImpl();
    final String[] parameterNames = paranamer.lookupParameterNames(method);
    Page methodPagePath = method.getAnnotation(Page.class);
    final String responseTemplate = methodPagePath.responseTemplate();
    
    HttpHandler handler = null;
    try {
      handler = new HttpHandler() {
        private final Logger LOGGER = Logger.getLogger(pageClass);
        
        private final Object newInstance = pageClass.getConstructor().newInstance();
        
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            try {
              Class<?>[] parameterTypes = method.getParameterTypes();
              CloverXRequest request = new CloverXRequest(exchange);

              for (Method method : beforeExecutemethods) {
                method.invoke(newInstance, request);
              }
              request = converterMounter.mountParameterConveters(method, parameterNames, request);
              
              ParametersConverter translator = new ParametersConverter();
              Object[] parameters =  translator.translateAllParameters(parameterNames, parameterTypes, request);
              CoreClassInjector injector = new CoreClassInjectorImpl();
              parameters =  injector.injectCoreInstances(parameterNames , parameters , parameterTypes, request);
              
              method.invoke(newInstance, parameters);

              Response response = request.getResponse();
              if (!Page.NO_TEMPLATE.equals(responseTemplate) && !response.isResponseSend()) {
                 request.respondAsHttp();
                 ((HttpResponse)request.getResponse() ).fowardTo( responseTemplate );  
              }else{
                //TODO should check json or xml response 
                if(!response.isResponseSend()){
                  response.close();
                }
              }
            } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
              e.printStackTrace();
              LOGGER.error(e.getMessage());
            }
        }
      };
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
        | InvocationTargetException e) {
      e.printStackTrace();
    }
    
    CloverXSessionManager sessionManager = CloverXSessionManager.getInstance();
    return new SessionAttachmentHandler( handler , sessionManager.getSessionManager(), sessionManager.getSessionConfig() );
  }
  
}
