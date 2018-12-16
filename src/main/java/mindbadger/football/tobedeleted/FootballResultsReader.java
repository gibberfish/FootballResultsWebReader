package mindbadger.football.tobedeleted;

import mindbadger.football.web.reader.ParsedFixture;

import java.util.Calendar;
import java.util.List;

public interface FootballResultsReader {
	public List<ParsedFixture> readFixturesForSeason(int season, List<String> includedDivisions);

	public List<ParsedFixture> readFixturesForDate(Calendar date);

	public List<ParsedFixture> readFixturesForTeamInSeason(int season, String teamId);
}
