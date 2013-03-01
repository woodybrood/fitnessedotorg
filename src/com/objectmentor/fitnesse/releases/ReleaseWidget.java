package com.objectmentor.fitnesse.releases;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import util.Maybe;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTableListingBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.html.TagGroup;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.Rule;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.Matcher;
import fitnesse.wikitext.parser.Translation;
import fitnesse.wikitext.parser.Translator;
import fitnesse.wikitext.widgets.ParentWidget;

public class ReleaseWidget extends SymbolType implements Rule, Translation{
  public static final String REGEXP = "^!release [^\r\n]*";
  private static final Pattern pattern = Pattern.compile(
    "^!release (-r\\s)?(\\S*)\\s?(\\S+)?"
  );
  private String releaseName;
  private Release release;
  public boolean reverseOrder;
  public String href;

  public ReleaseWidget() throws Exception {
    super("Release");
    wikiMatcher(new Matcher().string("!pi"));
    wikiRule(this);
    htmlTranslation(this);
  }
  
  @Override
  public Maybe<Symbol> parse(Symbol arg0, Parser arg1) {
	  
	
  	// TODO Auto-generated method stub
  	return null;
  }

  public String toTarget(Translator translator, Symbol symbol) {
    TagGroup html = new TagGroup();
    addTitle(html);
    if (release.exists()) {
      addFilesHtml(html);
      release.saveInfo();
    } else
      html.add("The release directory could not be found.");
    return html.html();
  }

  private void addTitle(TagGroup html)  {
    if (release.isCorrupted()) {
      html.add(new HtmlTag("h3", "release " + releaseName + " is Corrupted"));
    } else {
      HtmlTag title = new HtmlTag("h3", "release ");
      if (href != null)
        title.add(HtmlUtil.makeLink(href, releaseName));
      else
        title.add(releaseName);
      html.add(title);
    }
  }

  private void addFilesHtml(TagGroup html)  {
    if (release.fileCount() == 0)
      html.add("There are no files in this release.");
    else {
      HtmlTableListingBuilder list = new HtmlTableListingBuilder();
      addTitleRow(list);
      List files = release.getFiles();
      if (reverseOrder)
        Collections.reverse(files);
      for (Iterator iterator = files.iterator(); iterator.hasNext();) {
        ReleaseFile releaseFile = (ReleaseFile) iterator.next();
        addFileRow(releaseFile, list);
      }
      html.add(list.getTable());
    }
  }

  private void addFileRow(ReleaseFile file, HtmlTableListingBuilder list)
    {
    HtmlElement[] rowElements = new HtmlElement[3];
    String href = "/" + file.getFilename() +
      "?responder=releaseDownload&release=" + releaseName;
    HtmlElement link = HtmlUtil.makeLink(href, new HtmlTag("b",
      file.getFilename()
    )
    );
    rowElements[0] = link;
    rowElements[1] = new RawHtml(file.getSize());
    rowElements[2] = new RawHtml(file.getDownloads());
    list.addRow(rowElements);
  }

  private void addTitleRow(HtmlTableListingBuilder list)  {
    HtmlElement[] rowElements = new HtmlElement[3];
    rowElements[0] = HtmlUtil.makeSpanTag("caps", "File");
    rowElements[1] = HtmlUtil.makeSpanTag("caps", "Size");
    rowElements[2] = HtmlUtil.makeSpanTag("caps", "# of Downloads");
    list.addRow(rowElements);
  }

  public String getReleaseName() {
    return releaseName;
  }


}
