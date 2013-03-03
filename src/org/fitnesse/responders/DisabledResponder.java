package org.fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.responders.BasicResponder;
import fitnesse.responders.templateUtilities.HtmlPage;

public class DisabledResponder extends BasicResponder {

  public Response makeResponse(FitNesseContext context, Request request) {
    String content = prepareResponseDocument(context).html();
    return responseWith(content);
  }

  private HtmlPage prepareResponseDocument(FitNesseContext context) {
    HtmlPage responseDocument = context.pageFactory.newPage();
    HtmlUtil.addTitles(responseDocument, "Default Responder");
    responseDocument.setMainTemplate("disabled");
    return responseDocument;
  }

}