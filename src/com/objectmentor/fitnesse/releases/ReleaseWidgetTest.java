package com.objectmentor.fitnesse.releases;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.test.ParserTestHelper;
import fitnesse.wikitext.test.TestRoot;
import fitnesse.wikitext.test.TestVariableSource;
import util.FileUtil;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

public class ReleaseWidgetTest {
  private WikiPage page;

  @Before
  public void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    page = root.addChildPage("ThePage");
    widgetRoot = new WidgetRoot(page);
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("releases");
  }
  
  

  @Test 
  public void translatesVariables() throws Exception {
      ParserTestHelper.assertTranslatesTo("${x}", new TestVariableSource("x", "y"), "y");
      ParserTestHelper.assertTranslatesTo("${BoBo}", new TestVariableSource("BoBo", "y"), "y");
      assertTranslatesVariable("${x}", "y");
      assertTranslatesVariable("${z}", "<span class=\"meta\">undefined variable: z</span>");
      assertTranslatesVariable("${}", "${}");
  }
  
  private void assertTranslatesVariable(String variable, String expected) throws Exception {
      WikiPage pageOne = new TestRoot().makePage("PageOne", "!define x {y}\n" + variable);
      ParserTestHelper.assertTranslatesTo(pageOne,
        "<span class=\"meta\">variable defined: x=y</span>" + HtmlElement.endl +
          ParserTestHelper.newLineRendered + expected);
  }


  public void testRegexp() throws Exception {
    assertMatchEquals("!release somerelease", "!release somerelease");
  }

  public void testReleaseName() throws Exception {
    ReleaseWidget widget = new ReleaseWidget(new MockWidgetRoot(),
      "!release somerelease"
    );
    assertEquals("somerelease", widget.getReleaseName());
  }

  public void testRenderWithNoDirectory() throws Exception {
    ReleaseWidget widget = new ReleaseWidget(new MockWidgetRoot(),
      "!release xyz"
    );
    String html = widget.render();
    assertSubString("release xyz is Corrupted", html);
    assertSubString("The release directory could not be found.", html);
  }

  public void testRenderWithNoPackages() throws Exception {
    FileUtil.makeDir("releases");
    FileUtil.makeDir("releases/xyz");
    ReleaseWidget widget = new ReleaseWidget(new MockWidgetRoot(),
      "!release xyz"
    );
    String html = widget.render();
    assertSubString("release xyz", html);
    assertSubString("There are no files in this release.", html);
  }

  public void testRenderWithPackages() throws Exception {
    makeSampleRelease();
    ReleaseWidget widget = new ReleaseWidget(widgetRoot, "!release xyz");
    String html = widget.render();
    assertSubString("release xyz", html);
    assertSubString("package1", html);
    assertSubString(
      "<a href=\"/package1?responder=releaseDownload&release=xyz\">", html
    );
    assertSubString("package2", html);
    assertSubString(
      "<a href=\"/package2?responder=releaseDownload&release=xyz\">", html
    );
  }

  public void testReverseOrder() throws Exception {
    makeSampleRelease();
    ReleaseWidget widget = new ReleaseWidget(widgetRoot, "!release -r xyz");
    assertTrue(widget.reverseOrder);
    String html = widget.render();
    int indexOf1 = html.indexOf("package1");
    int indexOf2 = html.indexOf("package2");

    assertTrue(indexOf1 > 0);
    assertTrue(indexOf2 > 0);
    assertTrue(indexOf1 > indexOf2);
  }

  public void testReleaseLink() throws Exception {
    createReleaseInfo();
    ReleaseWidget widget = new ReleaseWidget(widgetRoot, "!release xyz someurl"
    );
    assertEquals("someurl", widget.href);
    String html = widget.render();
    assertSubString("<a href=\"someurl\">xyz</a>", html);
  }

  private void createReleaseInfo() {
    String line1 = "fitnesse20050301.zip\t1062799\t1109816900000\t10\n";
    String line2 = "fitnesse_src20050301.zip\t3362842\t1109817028000\t9\n";
    FileUtil.makeDir("releases");
    FileUtil.makeDir("releases/xyz");
    FileUtil.createFile("releases/xyz/.releaseInfo", line1 + line2);
  }

  public void testMultipleUsers() throws InterruptedException {
    Thread firstThread = createThreadForRunningWidget();
    Thread secondThread = createThreadForRunningWidget();

    firstThread.start();
    secondThread.start();
    Thread.sleep(100);
  }

  private void runWidget() {
    try {
      createReleaseInfo();
      ReleaseWidget widget = new ReleaseWidget(widgetRoot,
        "!release xyz someurl"
      );
      String html = widget.render();
      assertSubString("<a href=\"someurl\">xyz</a>", html);
    }
    catch (Exception e) {
      fail();
    }
  }

  private void makeSampleRelease() {
    FileUtil.makeDir("releases");
    FileUtil.makeDir("releases/xyz");
    FileUtil.createFile("releases/xyz/package1", "package one");
    FileUtil.createFile("releases/xyz/package2", "package two");
  }

  private Thread createThreadForRunningWidget() {
    Thread user = new Thread(
      new Runnable() {
        public void run() {
          try {
            runWidget();
          }
          catch (Exception e) {
            fail();
          }
        }
      }
    );
    return user;
  }

  protected String getRegexp() {
    return ReleaseWidget.REGEXP;
  }
}
