package com.objectmentor.fitnesse.releases;

import static org.junit.Assert.assertTrue;

import fitnesse.wikitext.parser.Symbol;
import org.junit.Test;

public class ReleaseSymbolTypeTest {

  private static ReleaseSymbolType releaseSymbolType = new ReleaseSymbolType();

  @Test
  public void translatesToContent() {
    Symbol symbol = new Symbol(releaseSymbolType);

    symbol.putProperty(ReleaseSymbolType.OptionType.RELEASE.name(), "20121220");

    String html = releaseSymbolType.toTarget(null, symbol);
    assertTrue(html.contains("<h3>release \n" +
            "\t<a href=\"\">20121220</a>\n" +
            "</h3>"));
    assertTrue(html.contains("<a href=\"/fitnesse-standalone.jar?responder=releaseDownload&release=20121220\">"));
  }

}
