package mindbadger.football.tobedeleted;

import mindbadger.football.web.reader.ParsedFixture;
import mindbadger.football.web.reader.util.StringToCalendarConverter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Component
public class SoccerbaseTeamPageParser {
	Logger logger = Logger.getLogger(SoccerbaseTeamPageParser.class);
	
	private static final String START_OF_MATCH = " <tr class=\"match\"";
	private static final String START_OF_DIVISION_LINE = " <a href=\"/tournaments/tournament.sd?comp_id=";
	private static final String END_OF_DIVISION_ID = "\"";
	private static final String START_OF_DIVISION_NAME = " competition page\">";
	private static final String END_OF_DIVISION_NAME = "</a>";
	private static final String START_OF_FIXTURE_DATE = " <a href=\"/matches/results.sd?date=";
	private static final String END_OF_FIXTURE_DATE = "\" title=\"";
	private static final String START_OF_TEAM_ID = " <a href=\"/teams/team.sd?team_id=";
	private static final String END_OF_TEAM_ID = "&amp;season_id=";
	private static final String START_OF_TEAM_NAME = " team page\">";
	private static final String END_OF_TEAM_NAME = "</a> <span";
	private static final String START_OF_HOME_TEAM_SECTION = " <td class=\"team homeTeam";
	private static final String START_OF_AWAY_TEAM_SECTION = " <td class=\"team awayTeam";
	private static final String START_OF_SCORE_SECTION = " <td class=\"score\">";
	private static final String START_OF_HOME_GOALS_LOCATION = " <a href=\"#\" class=\"vs\" title=\"View Match info\"><em>";
	private static final String END_OF_HOME_GOALS_LOCATION = "</em>&nbsp;-&nbsp;<em>";
	private static final String END_OF_AWAY_GOALS_LOCATION = "</em></a>";

	@Autowired
	private WebPageReader webPageReader;
	@Autowired
	private Pauser pauser;

	@Autowired
	@Value("${team.page.url}")
	private String url;
	
	public List<ParsedFixture> parseFixturesForTeam(Integer seasonNumber, String teamId, boolean retry) {
		Integer soccerbaseSeasonNumber = null;
		if (seasonNumber > 2015) {
			soccerbaseSeasonNumber = seasonNumber - 1867;
		} else {
			soccerbaseSeasonNumber = seasonNumber - 1870;
		}
		
		logger.debug("ABOUT TO LOAD FIXTURES FOR TEAM " + teamId + " IN SEASON " + seasonNumber);
		
		String url = this.url.replace("{seasonNum}", soccerbaseSeasonNumber.toString());
		url = url.replace("{teamId}", teamId);
		
		try {
			List<String> page = webPageReader.readWebPage(url);
			
			pauser.pause ();
			
			return parsePage(page);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new FootballResultsReaderException("No page found for team ID " + teamId);
		} catch (IOException e) {
			e.printStackTrace();
			
			if (retry) {
				// retry once
				return parseFixturesForTeam(seasonNumber, teamId, true);
			} else {
				throw new FootballResultsReaderException("Cannot load page for team ID " + teamId);
			}
		}

	}

	public List<ParsedFixture> parseFixturesForTeam(Integer seasonNumber, String teamId) {
		return parseFixturesForTeam(seasonNumber, teamId, true);
	}

	private List<ParsedFixture> parsePage(List<String> page) {
		List<ParsedFixture> parsedFixtures = new ArrayList<ParsedFixture> ();
		
		String divisionId = null;
		String divisionName = null;
		String homeTeamId = null;
		String awayTeamId = null;
		String homeTeamName = null;
		String awayTeamName = null;
		Integer homeGoals = null;
		Integer awayGoals = null;
		Calendar fixtureDate = null;
		Integer season = null;

		boolean lookingForAwayTeam = false;

		for (String line : page) {
			
			if (line.startsWith(START_OF_MATCH)) {
				divisionId = null;
				divisionName = null;
				homeTeamId = null;
				awayTeamId = null;
				homeTeamName = null;
				awayTeamName = null;
				homeGoals = null;
				awayGoals = null;
				fixtureDate = null;
				season = null;
				
				logger.debug("   Start of match");
			}
			
			if (line.startsWith(START_OF_DIVISION_LINE)) {
				int divisionIdStartPos = line.indexOf(START_OF_DIVISION_LINE) + START_OF_DIVISION_LINE.length();
				int divisionIdEndPos = line.indexOf(END_OF_DIVISION_ID,divisionIdStartPos);
				divisionId = line.substring(divisionIdStartPos, divisionIdEndPos);
				
				int divisionNameStartPos = line.indexOf(START_OF_DIVISION_NAME, divisionIdEndPos) + START_OF_DIVISION_NAME.length();
				int divisionNameEndPos = line.indexOf(END_OF_DIVISION_NAME, divisionIdStartPos);
				divisionName = line.substring(divisionNameStartPos, divisionNameEndPos);
				
				logger.debug("   Got division, id: " + divisionId + ", name: " + divisionName);
			}
			
			if (line.startsWith(START_OF_FIXTURE_DATE)) {
				int fixtureDateStartPos = line.indexOf(START_OF_FIXTURE_DATE) + START_OF_FIXTURE_DATE.length();
				int fixtureDateEndPos = line.indexOf(END_OF_FIXTURE_DATE,fixtureDateStartPos);
				String fixtureDateString = line.substring(fixtureDateStartPos, fixtureDateEndPos);
				
				fixtureDate = StringToCalendarConverter.convertDateStringToCalendar(fixtureDateString);
				
				logger.debug("   Got fixture date: " + fixtureDate);
			}
			
			if (line.startsWith(START_OF_HOME_TEAM_SECTION)) {
				logger.debug("   Start of home team section");
				lookingForAwayTeam = false;
			}
			
			if (line.startsWith(START_OF_AWAY_TEAM_SECTION)) {
				logger.debug("   Start of away team section");
				lookingForAwayTeam = true;
			}

			if (line.startsWith(START_OF_TEAM_ID)) {
				int teamIdStartPos = line.indexOf(START_OF_TEAM_ID) + START_OF_TEAM_ID.length();
				int teamIdEndPos = line.indexOf(END_OF_TEAM_ID,teamIdStartPos);
				if (lookingForAwayTeam) {
					awayTeamId = line.substring(teamIdStartPos, teamIdEndPos);
				} else {
					homeTeamId = line.substring(teamIdStartPos, teamIdEndPos);
				}
				
				int seasonStartPos = line.indexOf(END_OF_TEAM_ID, teamIdEndPos) + END_OF_TEAM_ID.length();
				int seasonEndPos = line.indexOf("&amp;teamTabs=results\" title", seasonStartPos);
				
				season = Integer.parseInt(line.substring(seasonStartPos, seasonEndPos));
				if (season >= 149) {
					season+=1867;
				} else {
					season+=1870;
				}
				
				int teamNameStartPos = line.indexOf(START_OF_TEAM_NAME, seasonEndPos) + START_OF_TEAM_NAME.length();
				int teamNameEndPos = line.indexOf(END_OF_TEAM_NAME, teamNameStartPos);
				if (lookingForAwayTeam) {
					awayTeamName = line.substring(teamNameStartPos, teamNameEndPos);
					logger.debug("   Got Away team: " + awayTeamName);
					
					parsedFixtures.add(createFixture(divisionId, divisionName, homeTeamId, awayTeamId, homeTeamName, awayTeamName, homeGoals, awayGoals, fixtureDate, season));
				} else {
					homeTeamName = line.substring(teamNameStartPos, teamNameEndPos);
					logger.debug("   Got Home team: " + homeTeamName);
				}
			}
			
			if (line.startsWith(START_OF_SCORE_SECTION)) {
				homeGoals = null;
				awayGoals = null;
				logger.debug("   Start of score section");
			}

			if (line.startsWith(START_OF_HOME_GOALS_LOCATION)) {
				int homeGoalsStartPos = line.indexOf(START_OF_HOME_GOALS_LOCATION) + START_OF_HOME_GOALS_LOCATION.length();
				int homeGoalsEndPos = line.indexOf(END_OF_HOME_GOALS_LOCATION,homeGoalsStartPos);
				homeGoals = Integer.parseInt(line.substring(homeGoalsStartPos, homeGoalsEndPos));
				
				int awayGoalsStartPos = homeGoalsEndPos + END_OF_HOME_GOALS_LOCATION.length();
				int awayGoalsEndPos = line.indexOf(END_OF_AWAY_GOALS_LOCATION, awayGoalsStartPos);
				awayGoals = Integer.parseInt(line.substring(awayGoalsStartPos, awayGoalsEndPos));
				
				logger.debug("   Got Score: " + homeGoals + "-" + awayGoals);
			}

		}		
		
		return parsedFixtures;
	}

	private ParsedFixture createFixture(String divisionId, String divisionName, String homeTeamId, String awayTeamId, String homeTeamName, String awayTeamName, Integer homeGoals, Integer awayGoals, Calendar fixtureDate,
			Integer season) {
		ParsedFixture parsedFixture = new ParsedFixture();
		parsedFixture.setSeasonId(season);
		parsedFixture.setFixtureDate(fixtureDate);
		parsedFixture.setDivisionId(divisionId);
		parsedFixture.setDivisionName(divisionName);
		parsedFixture.setHomeTeamId(homeTeamId);
		parsedFixture.setHomeTeamName(homeTeamName);
		parsedFixture.setAwayTeamId(awayTeamId);
		parsedFixture.setAwayTeamName(awayTeamName);
		parsedFixture.setHomeGoals(homeGoals);
		parsedFixture.setAwayGoals(awayGoals);
		
		logger.info("Parsed Fixture: " + parsedFixture);
		
		return parsedFixture;
	}

	public void setWebPageReader(WebPageReader webPageReader) {
		this.webPageReader = webPageReader;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setPauser(Pauser pauser) {
		this.pauser = pauser;
	}
}
