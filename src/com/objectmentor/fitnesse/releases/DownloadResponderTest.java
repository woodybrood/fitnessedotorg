package com.objectmentor.fitnesse.releases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.FileUtil;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;

import static com.objectmentor.fitnesse.releases.Asserts.assertSubString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DownloadResponderTest {
  private DownloadResponder responder;
  private FitNesseContext context;
  
  @Before
  public void setUp() throws Exception {
    responder = new DownloadResponder();
    context = FitNesseUtil.makeTestContext();
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("releases/xyz");
  }

  @Test
  public void testParameters() throws Exception {
    MockRequest request = new MockRequest();
    request.addInput("release", "abc");
    request.setResource("xyz");

    responder.makeResponse(context, request);

    assertEquals("abc", responder.getReleaseName());
    assertEquals("xyz", responder.getFilename());
  }

  @Test
  public void testDownloading() throws Exception {
    Response response = doSimpleDownload();

    MockResponseSender mockResponseSender = new MockResponseSender();
    mockResponseSender.doSending(response);
    String content = mockResponseSender.sentData();
    assertSubString("package one", content);
  }

  @Test
  public void testDownloadWasRecorded() throws Exception {
    doSimpleDownload();
    Release release = new Release("xyz");
    ReleaseFile releaseFile = release.getFile("file1");
    assertEquals(1, releaseFile.downloads);
  }

  @Test
  public void testMimeType() throws Exception {
    ReleaseTest.prepareReleaseWithFiles();
    FileUtil.createFile("releases/xyz/file3.jar", "jar file");
    FileUtil.createFile("releases/xyz/file4.zip", "zip file");
    MockRequest request = new MockRequest();
    request.addInput("release", "xyz");
    request.setResource("file3.jar");

    Response response = responder.makeResponse(context, request);
    assertEquals("application/x-java-archive", response.getContentType());

    request.setResource("file4.zip");
    response = responder.makeResponse(context, request);
    assertEquals("application/zip", response.getContentType());
  }

  private Response doSimpleDownload() throws Exception {
    ReleaseTest.prepareReleaseWithFiles();
    MockRequest request = new MockRequest();
    request.addInput("release", "xyz");
    request.setResource("file1");

    Response response = responder.makeResponse(context, request);
    return response;
  }

}
