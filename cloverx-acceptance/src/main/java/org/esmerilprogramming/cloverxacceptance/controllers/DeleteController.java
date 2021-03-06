package org.esmerilprogramming.cloverxacceptance.controllers;

import io.undertow.io.Sender;
import org.esmerilprogramming.cloverx.annotation.Controller;
import org.esmerilprogramming.cloverx.annotation.path.Delete;
import org.esmerilprogramming.cloverx.annotation.path.Get;
import org.esmerilprogramming.cloverx.annotation.path.Post;
import org.esmerilprogramming.cloverx.annotation.path.Put;
import org.esmerilprogramming.cloverx.http.CloverXRequest;

/**
 * Created by efraimgentil<efraimgentil@gmail.com> on 14/03/15.
 */
@Controller
public class DeleteController {

  @Get(template = "index.ftl")
  public void index(CloverXRequest request){

  }

  @Delete
  public void delete(Integer id, CloverXRequest request){
    Sender sender = request.getExchange().getResponseSender();
    sender.send("DELETE - delete/delete - id:" + id);
  }


}
