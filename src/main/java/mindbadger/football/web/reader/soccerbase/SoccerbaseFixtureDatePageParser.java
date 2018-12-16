package mindbadger.football.web.reader.soccerbase;

import mindbadger.football.web.reader.FixtureDatePageParser;
import mindbadger.football.web.reader.ParsedFixture;
import mindbadger.football.web.reader.util.StringToCalendarConverter;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class SoccerbaseFixtureDatePageParser implements FixtureDatePageParser {
    private static Logger LOG = Logger.getLogger(SoccerbaseFixtureDatePageParser.class);

    @Autowired
    @Value("${date.page.url}")
    private String url;
    @Autowired
    @Value("${dialect}")
    private String dialect;

    @Override
    public List<ParsedFixture> getFixturesFromDatePage(Calendar date, List<String> divisionsToInclude) {
        LOG.info("[[ getFixturesFromDatePage for date " + StringToCalendarConverter.convertCalendarToDateString(date) +
                " and divisions: " + divisionsToInclude + "]]");

        String url = this.url.replace("{fixtureDate}",
                StringToCalendarConverter.convertCalendarToDateString(date));

        List<ParsedFixture> parsedFixtures = new ArrayList<>();
        String currentDivisionId = null;
        String currentDivisionName = null;
        boolean ignoreDivision = true;

        try {
            Document pageDocument = Jsoup.connect(url).get();

            Elements tableRows = parseTableRowsFromPage(pageDocument);

            for (Element tableRow : tableRows) {
                Elements divisionNodes = tableRow.select("[href^='/tournaments/tournament.sd?comp_id=']");

                if (divisionNodes.size() == 1) {
                    Element divisionNode = divisionNodes.get(0);

                    String[] split = divisionNode.attr("href").split("comp_id=");
                    if (divisionsToInclude.contains(split[1])) {
                        currentDivisionId = split[1];
                        currentDivisionName = divisionNode.text();
                        ignoreDivision = false;
                    } else {
                        ignoreDivision = true;
                    }
                }

                if (!ignoreDivision && "match".equals(tableRow.className())) {
                    LOG.info("... got match for " + currentDivisionId);

                    ParsedFixture newParsedFixture = new ParsedFixture();
                    newParsedFixture.setDivisionId(currentDivisionId);
                    newParsedFixture.setDivisionName(currentDivisionName);

                    parseDateFromFixture (tableRow, newParsedFixture);
                    parseHomeTeamFromFixture (tableRow, newParsedFixture);
                    parseAwayTeamFromFixture (tableRow, newParsedFixture);
                    parseScoreFromFixture (tableRow, newParsedFixture);

                    parsedFixtures.add(newParsedFixture);
                }
            }
        } catch (IOException e) {
            //TODO Handle Read time-out - return 408
            e.printStackTrace();
        }

        return parsedFixtures;
    }

    private void parseScoreFromFixture(Element fixtureElement, ParsedFixture parsedFixture) {
        Elements scoreElements = fixtureElement.select(".score");
        Elements scoreElementAnchorEms = scoreElements.get(0).select("a > em");

        if (scoreElementAnchorEms.size() == 2) {
            parsedFixture.setHomeGoals(Integer.parseInt(scoreElementAnchorEms.get(0).text()));
            parsedFixture.setAwayGoals(Integer.parseInt(scoreElementAnchorEms.get(1).text()));
        }
    }

    private void parseHomeTeamFromFixture(Element fixtureElement, ParsedFixture parsedFixture) {
        Elements homeTeamElements = fixtureElement.select(".homeTeam");
        Elements homeTeamElementAnchor = homeTeamElements.get(0).select("a[href]");
        String[] split = homeTeamElementAnchor.get(0).attr("href").split("team_id=");

        parsedFixture.setHomeTeamId(split[1]);
        parsedFixture.setHomeTeamName(homeTeamElementAnchor.text());
    }

    private void parseAwayTeamFromFixture(Element fixtureElement, ParsedFixture parsedFixture) {
        Elements awayTeamElements = fixtureElement.select(".awayTeam");
        Elements awayTeamElementAnchor = awayTeamElements.get(0).select("a[href]");
        String[] split = awayTeamElementAnchor.get(0).attr("href").split("team_id=");

        parsedFixture.setAwayTeamId(split[1]);
        parsedFixture.setAwayTeamName(awayTeamElementAnchor.text());
    }

    private void parseDateFromFixture (Element fixtureElement, ParsedFixture parsedFixture) {
        Elements dateElements = fixtureElement.select(".dateTime");
        Elements dateElementAnchor = dateElements.get(0).select("a[href]");
        String[] split = dateElementAnchor.get(0).attr("href").split("date=");

        Calendar fixtureDate = StringToCalendarConverter.convertDateStringToCalendar(split[1]);
        parsedFixture.setFixtureDate(fixtureDate);
        parsedFixture.setSeasonId(getSeasonFromFixturedate(fixtureDate));
    }

    private Elements parseTableRowsFromPage (Document pageDocument) {
        return pageDocument.select("#tgc > tbody > tr");
    }

    private Integer getSeasonFromFixturedate(Calendar fixtureDate) {
        if (fixtureDate.get(Calendar.MONTH) < 7) {
            return (fixtureDate.get(Calendar.YEAR) -1);
        } else {
            return fixtureDate.get(Calendar.YEAR);
        }
    }
}
