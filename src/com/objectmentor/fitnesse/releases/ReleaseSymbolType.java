package com.objectmentor.fitnesse.releases;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.wikitext.parser.*;

/**
 * <pre>
 * !release 20050301 .FrontPage.FitNesseDevelopment.FitNesseRelease20050301
 * !release 20050405
 * </pre>
 */
public class ReleaseSymbolType extends SymbolType implements Rule, Translation {

  enum OptionType {
    RELEASE,
    HREF,
  }

  public ReleaseSymbolType() {
    super("ReleaseSymbolType");

    wikiMatcher(new Matcher().startLineOrCell().string("!release"));

    wikiRule(this);
    htmlTranslation(this);
  }

  @Override
  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    OptionType nextOption = OptionType.RELEASE;

    Symbol body = parser.parseToEnd(SymbolType.Newline);

    for (Symbol option: body.getChildren()) {
      if (option.isType(SymbolType.Whitespace)) {
        continue;
      } else {
        current.putProperty(nextOption.name(), option.getContent());
        nextOption = OptionType.HREF;
      }
    }

    return new Maybe<Symbol>(current);
  }

  @Override
  public String toTarget(Translator translator, Symbol symbol) {
    Release release;
    try {
      release = new Release(symbol.getProperty(OptionType.RELEASE.name()));
    } catch (Exception e) {
      return "<strong>Malformed release: " + e.getMessage() + "</strong>";
    }

    String href = symbol.getProperty(OptionType.HREF.name());

    TagGroup html = new TagGroup();
    addTitle(html, release, href);
    if (release.exists()) {
      addFilesHtml(html, release);
      release.saveInfo();
    } else
      html.add("The release directory could not be found.");
    return html.html();
  }

  private void addTitle(TagGroup html, Release release, String href)  {
    if (release.isCorrupted()) {
      html.add(new HtmlTag("h3", "release " + release.getName() + " is Corrupted"));
    } else {
      HtmlTag title = new HtmlTag("h3", "release ");
      if (href != null)
        title.add(HtmlUtil.makeLink(href, release.getName()));
      else
        title.add(release.getName());
      html.add(title);
    }
  }

  private void addFilesHtml(TagGroup html, Release release)  {
    if (release.fileCount() == 0)
      html.add("There are no files in this release.");
    else {
      StringBuilder table = new StringBuilder(512);
      table.append("<table>");
      table.append("<thead><tr><th>File</th><th>Size</th><th># of Downloads</th></thead>");
      table.append("<tbody>");
      for (ReleaseFile releaseFile : release.getFiles()) {
        addFileRow(table, release, releaseFile);
      }
      table.append("</tbody></table>");
      html.add(table.toString());
    }
  }

  private void addFileRow(StringBuilder table, Release release, ReleaseFile releaseFile)
  {
    String href = "/" + releaseFile.getFilename() +
            "?responder=releaseDownload&release=" + release.getName();
    table.append(String.format(
            "<tr><td><strong><a href=\"%s\">%s</a></strong></td><td>%s</td><td>%s</td></tr>",
            href, releaseFile.getFilename(), releaseFile.getSize(), releaseFile.getDownloads()));
  }

}
