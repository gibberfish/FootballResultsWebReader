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
public class SoccerbaseDatePageParser {
	Logger logger = Logger.getLogger(SoccerbaseDatePageParser.class);
	
	private static final String END_OF_HOME_GOALS_LOCATION = "</em>&nbsp;-&nbsp;<em>";
	private static final String END_OF_AWAY_GOALS_LOCATION = "</em></a>";
	private static final String START_OF_HOME_GOALS_LOCATION = " <a href=\"#\" class=\"vs\" title=\"View Match info\"><em>";
	private static final String START_OF_SCORE_SECTION = " <td class=\"score\">";
	private static final String END_OF_TEAM_NAME_LOCATION = "</a>";
	private static final String START_OF_TEAM_NAME_LOCATION = "team page\">";
	private static final String END_OF_TEAM_ID_LOCATION = "\" title=";
	private static final String START_OF_TEAM_ID_LOCATION = " <a href=\"/teams/team.sd?team_id=";
	private static final String START_OF_AWAY_TEAM_SECTION = " <td class=\"team awayTeam";
	private static final String START_OF_HOME_TEAM_SECTION = " <td class=\"team homeTeam";
	private static final String END_OF_DATE_LOCATION = END_OF_TEAM_ID_LOCATION;
	private static final String START_OF_DATE_LOCATION = " <a href=\"/matches/results.sd?date=";
	private static final String START_OF_DIVISION_NAME_LOCATION = "page\">";
	private static final String END_OF_DIVISION_ID_LOCATION = "\" title";
	private static final String START_OF_DIVISION_ID_LOCATION = " <a href=\"/tournaments/tournament.sd?comp_id=";
	@Autowired
	private WebPageReader webPageReader;
	@Autowired
	private Pauser pauser;

	@Autowired
	@Value("${date.page.url}")
	private String url;
	@Autowired
	@Value("${dialect}")
	private String dialect;
	
	public void setWebPageReader(WebPageReader webPageReader) {
		this.webPageReader = webPageReader;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<ParsedFixture> parseFixturesForDate(String dateString) {
		String url = this.url.replace("{fixtureDate}", dateString);
		
		try {
			List<String> page = webPageReader.readWebPage(url);
			
			pauser.pause();
			
			return parsePage(page);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new FootballResultsReaderException("No page found for " + dateString);
		} catch (IOException e) {
			e.printStackTrace();
			throw new FootballResultsReaderException("Cannot load page for " + dateString);
		}
	}

	protected List<ParsedFixture> parsePage(List<String> page) {
		
		List<ParsedFixture> parsedFixtures = new ArrayList<ParsedFixture> ();
		
		String divisionId = null;
		String divisionName = null;
		String dateString = null;
		boolean lookingForAwayTeam = false;
		String homeTeamId = null;
		String awayTeamId = null;
		String homeTeamName = null;
		String awayTeamName = null;
		Integer homeGoals = null;
		Integer awayGoals = null;
		Calendar fixtureDate = null;
		Integer season = null;
		boolean notInTrackedDivisionSoIgnore = false;
		
		for (String line : page) {
			if (line.startsWith(START_OF_DIVISION_ID_LOCATION)) {
				logger.debug("#### START OF NEW DIVISION");
				
				int divisionIdStartPos = line.indexOf(START_OF_DIVISION_ID_LOCATION) + START_OF_DIVISION_ID_LOCATION.length();
				int divisionIdEndPos = line.indexOf(END_OF_DIVISION_ID_LOCATION,divisionIdStartPos);
				divisionId = line.substring(divisionIdStartPos, divisionIdEndPos);
				
				logger.debug("####.. divisionId = " + divisionId);
				
				int divisionNameStartPos = line.indexOf(START_OF_DIVISION_NAME_LOCATION, divisionIdEndPos) + START_OF_DIVISION_NAME_LOCATION.length();
				int divisionNameEndPos = line.indexOf(END_OF_TEAM_NAME_LOCATION, divisionIdStartPos);
				divisionName = line.substring(divisionNameStartPos, divisionNameEndPos);
				
				logger.debug("####.. divisionName = " + divisionName);
				
				//notInTrackedDivisionSoIgnore = !mapping.getIncludedDivisions(dialect).contains(divisionId);
			}
			
			if (line.startsWith(START_OF_DATE_LOCATION)) {
				logger.debug("#### START OF DATE");
				
				int dateStartPos = line.indexOf(START_OF_DATE_LOCATION) + START_OF_DATE_LOCATION.length();
				int dateEndPos = line.indexOf(END_OF_DATE_LOCATION, dateStartPos);
				dateString = line.substring(dateStartPos, dateEndPos);
				fixtureDate = StringToCalendarConverter.convertDateStringToCalendar(dateString);
				
				logger.debug("####.. fixtureDate = " + fixtureDate);
				
				if (season == null) {
					season = getSeasonFromFixturedate (fixtureDate);
				}
			}
			
			if (line.startsWith(START_OF_HOME_TEAM_SECTION)) {
				logger.debug("#### START OF HOME TEAM");
				lookingForAwayTeam = false;
			}
			
			if (line.startsWith(START_OF_AWAY_TEAM_SECTION)) {
				logger.debug("#### START OF AWAY TEAM");
				lookingForAwayTeam = true;
			}

			if (line.startsWith(START_OF_TEAM_ID_LOCATION)) {
				logger.debug("#### START OF TEAM ID");
				
				int teamIdStartPos = line.indexOf(START_OF_TEAM_ID_LOCATION) + START_OF_TEAM_ID_LOCATION.length();
				int teamIdEndPos = line.indexOf(END_OF_TEAM_ID_LOCATION,teamIdStartPos);
				if (lookingForAwayTeam) {
					awayTeamId = line.substring(teamIdStartPos, teamIdEndPos);
					logger.debug("####.. awayTeamId = " + awayTeamId);
				} else {
					homeTeamId = line.substring(teamIdStartPos, teamIdEndPos);
					logger.debug("####.. homeTeamId = " + homeTeamId);
				}
				
				
				int teamNameStartPos = line.indexOf(START_OF_TEAM_NAME_LOCATION, teamIdEndPos) + START_OF_TEAM_NAME_LOCATION.length();
				int teamNameEndPos = line.indexOf(END_OF_TEAM_NAME_LOCATION, teamIdStartPos);
				if (lookingForAwayTeam) {
					awayTeamName = line.substring(teamNameStartPos, teamNameEndPos);
					logger.debug("####.. awayTeamName = " + awayTeamName);
					
					if (notInTrackedDivisionSoIgnore) {
						logger.debug("Ignoring Fixture, as it is not tracked");
					} else {
						ParsedFixture fixture = createFixture(divisionId, divisionName, homeTeamId, awayTeamId, homeTeamName, awayTeamName, homeGoals, awayGoals, fixtureDate, season);
						parsedFixtures.add(fixture);
						logger.info("Parsed Fixture: " + fixture);						
					}
					
					logger.debug("####.. ADDED NEW FIXTURE");
				} else {
					homeTeamName = line.substring(teamNameStartPos, teamNameEndPos);
					logger.debug("####.. homeTeamName = " + homeTeamName);
				}
			}

			if (line.startsWith(START_OF_SCORE_SECTION)) {
				logger.debug("#### START OF SCORE");
				homeGoals = null;
				awayGoals = null;
			}
			
			if (line.startsWith(START_OF_HOME_GOALS_LOCATION)) {
				int homeGoalsStartPos = line.indexOf(START_OF_HOME_GOALS_LOCATION) + START_OF_HOME_GOALS_LOCATION.length();
				int homeGoalsEndPos = line.indexOf(END_OF_HOME_GOALS_LOCATION,homeGoalsStartPos);
				homeGoals = Integer.parseInt(line.substring(homeGoalsStartPos, homeGoalsEndPos));
				
				logger.debug("####.. homeGoals = " + homeGoals);
				
				int awayGoalsStartPos = homeGoalsEndPos + END_OF_HOME_GOALS_LOCATION.length();
				int awayGoalsEndPos = line.indexOf(END_OF_AWAY_GOALS_LOCATION, awayGoalsStartPos);
				awayGoals = Integer.parseInt(line.substring(awayGoalsStartPos, awayGoalsEndPos));
				
				logger.debug("####.. awayGoals = " + awayGoals);
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
		
		return parsedFixture;
	}

	
	private Integer getSeasonFromFixturedate(Calendar fixtureDate) {
		if (fixtureDate.get(Calendar.MONTH) < 7) {
			return (fixtureDate.get(Calendar.YEAR) -1);
		} else {
			return fixtureDate.get(Calendar.YEAR);
		}
	}

	public void setPauser(Pauser pauser) {
		this.pauser = pauser;
	}

	public String getDialect() {
		return dialect;
	}
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

}
