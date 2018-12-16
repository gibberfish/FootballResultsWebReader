package mindbadger.football.web.reader.soccerbase;

import mindbadger.football.web.reader.ParsedFixture;
import mindbadger.football.web.reader.TeamFixturesPageParser;
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
public class SoccerbaseTeamFixturesPageParser implements TeamFixturesPageParser {
    private static Logger LOG = Logger.getLogger(SoccerbaseTeamFixturesPageParser.class);

    @Autowired
    @Value("${team.page.url}")
    private String url;
    @Autowired
    @Value("${dialect}")
    private String dialect;

    @Override
    public List<ParsedFixture> getFixturesFromTeamFixturesPage(Integer seasonNumber, String sourceTeamId, List<String> divisionsToInclude) {
        LOG.info("[[ getFixturesFromTeamFixturesPage for season " + seasonNumber + " and team id " + sourceTeamId + "]]");

        List<ParsedFixture> parsedFixtures = new ArrayList<>();

        Integer soccerbaseSeasonNumber = null;
        if (seasonNumber > 2015) {
            soccerbaseSeasonNumber = seasonNumber - 1867;
        } else {
            soccerbaseSeasonNumber = seasonNumber - 1870;
        }

        String url = this.url.replace("{seasonNum}", soccerbaseSeasonNumber.toString());
        url = url.replace("{teamId}", sourceTeamId);

        try {
            Document pageDocument = Jsoup.connect(url).get();

            Elements tableRows = parseTableRowsFromPage(pageDocument);

            for (Element tableRow : tableRows) {
                ParsedFixture newParsedFixture = new ParsedFixture();
                parseDivisionFromFixture(tableRow, newParsedFixture);

                if (divisionsToInclude.contains(newParsedFixture.getDivisionId())) {
                    newParsedFixture.setSeasonId(seasonNumber);
                    parseDateFromFixture(tableRow, newParsedFixture);
                    parseHomeTeamFromFixture(tableRow, newParsedFixture);
                    parseAwayTeamFromFixture(tableRow, newParsedFixture);
                    parseScoreFromFixture(tableRow, newParsedFixture);

                    parsedFixtures.add(newParsedFixture);
                }
            }

        } catch (IOException e) {
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
        String[] split2 = split[1].split("&");

        parsedFixture.setHomeTeamId(split2[0]);
        parsedFixture.setHomeTeamName(homeTeamElementAnchor.text());
    }

    private void parseAwayTeamFromFixture(Element fixtureElement, ParsedFixture parsedFixture) {
        Elements awayTeamElements = fixtureElement.select(".awayTeam");
        Elements awayTeamElementAnchor = awayTeamElements.get(0).select("a[href]");
        String[] split = awayTeamElementAnchor.get(0).attr("href").split("team_id=");
        String[] split2 = split[1].split("&");

        parsedFixture.setAwayTeamId(split2[0]);
        parsedFixture.setAwayTeamName(awayTeamElementAnchor.text());
    }

    private void parseDivisionFromFixture(Element fixtureElement, ParsedFixture parsedFixture) {
        Elements dateElements = fixtureElement.select(".tournament");
        Elements dateElementAnchor = dateElements.get(0).select("a[href]");
        String[] split = dateElementAnchor.get(0).attr("href").split("comp_id=");
        parsedFixture.setDivisionId(split[1]);
        parsedFixture.setDivisionName(dateElementAnchor.text());
    }

    private Elements parseTableRowsFromPage(Document pageDocument) {
        return pageDocument.select("#tgc > tbody > tr");
    }

    private void parseDateFromFixture (Element fixtureElement, ParsedFixture parsedFixture) {
        Elements dateElements = fixtureElement.select(".dateTime");
        Elements dateElementAnchor = dateElements.get(0).select("a[href]");
        String[] split = dateElementAnchor.get(0).attr("href").split("date=");

        Calendar fixtureDate = StringToCalendarConverter.convertDateStringToCalendar(split[1]);
        parsedFixture.setFixtureDate(fixtureDate);
    }
}
