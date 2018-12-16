package mindbadger.football.web.reader;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Calendar;

public class ParsedFixture implements Comparable<ParsedFixture> {
	private Integer seasonId;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Calendar fixtureDate;
	private String divisionId;
	private String divisionName;
	private String homeTeamId;
	private String homeTeamName;
	private String awayTeamId;
	private String awayTeamName;
	private Integer homeGoals;
	private Integer awayGoals;
	public Integer getSeasonId() {
		return seasonId;
	}
	public void setSeasonId(Integer seasonId) {
		this.seasonId = seasonId;
	}
	public Calendar getFixtureDate() {
		return fixtureDate;
	}
	public void setFixtureDate(Calendar fixtureDate) {
		this.fixtureDate = fixtureDate;
	}
	public String getDivisionId() {
		return divisionId;
	}
	public void setDivisionId(String divisionId) {
		this.divisionId = divisionId;
	}
	public String getDivisionName() {
		return divisionName;
	}
	public void setDivisionName(String divisionName) {
		this.divisionName = divisionName;
	}
	public String getHomeTeamId() {
		return homeTeamId;
	}
	public void setHomeTeamId(String homeTeamId) {
		this.homeTeamId = homeTeamId;
	}
	public String getHomeTeamName() {
		return homeTeamName;
	}
	public void setHomeTeamName(String homeTeamName) {
		this.homeTeamName = homeTeamName;
	}
	public String getAwayTeamId() {
		return awayTeamId;
	}
	public void setAwayTeamId(String awayTeamId) {
		this.awayTeamId = awayTeamId;
	}
	public String getAwayTeamName() {
		return awayTeamName;
	}
	public void setAwayTeamName(String awayTeamName) {
		this.awayTeamName = awayTeamName;
	}
	public Integer getHomeGoals() {
		return homeGoals;
	}
	public void setHomeGoals(Integer homeGoals) {
		this.homeGoals = homeGoals;
	}
	public Integer getAwayGoals() {
		return awayGoals;
	}
	public void setAwayGoals(Integer awayGoals) {
		this.awayGoals = awayGoals;
	}
	
	@Override
	public String toString () {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		if (fixtureDate != null) {
			buf.append(String.format("%1$te-%1$tm-%1$tY", fixtureDate));
		} else {
			buf.append("NODATE");
		}
		buf.append(" (");
		buf.append(seasonId);
		buf.append(")][Div=");
		buf.append(divisionName);
		buf.append("(");
		buf.append(divisionId);
		buf.append(")][");
		buf.append(homeTeamName);
		buf.append("(");
		buf.append(homeTeamId);
		buf.append(") ");
		buf.append(homeGoals);
		buf.append("-");
		buf.append(awayGoals);
		buf.append(" ");
		buf.append(awayTeamName);
		buf.append("(");
		buf.append(awayTeamId);
		buf.append(")]");
		return buf.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ParsedFixture) {
			ParsedFixture parsedFixture = (ParsedFixture) obj;
			
			boolean equals = parsedFixture.getSeasonId().equals(this.seasonId);
			equals = equals && ((parsedFixture.getFixtureDate() == null && this.fixtureDate == null) || (parsedFixture != null && parsedFixture.getFixtureDate() != null && parsedFixture.getFixtureDate().equals(this.fixtureDate)));
			equals = equals && parsedFixture.getHomeTeamId().equals(this.homeTeamId);
			equals = equals && parsedFixture.getAwayTeamId().equals(this.awayTeamId);
			equals = equals && parsedFixture.getDivisionId().equals(this.divisionId);
			
			return equals;
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(ParsedFixture o) {
		ParsedFixture compareFixture = (ParsedFixture) o;
		
		// Season
		int compareSeason = this.getSeasonId().compareTo(compareFixture.getSeasonId());
		if (compareSeason != 0) return compareSeason;
		
		// Fixture Date
		String thisObjectDate = null;
		if (this.fixtureDate != null) {
			thisObjectDate = String.format("%1$tY-%1$tm-%1$te", this.fixtureDate);
		}
		
		String compareObjectDate = null;
		if (compareFixture.getFixtureDate() != null) {
			compareObjectDate = String.format("%1$tY-%1$tm-%1$te", compareFixture.getFixtureDate());
		}
		
		if (thisObjectDate == null && compareObjectDate != null) {
			return -1;
		}
		
		if (thisObjectDate != null && compareObjectDate == null) {
			return 1;
		}
		
		int compareDate = 0;
		if (thisObjectDate != null && compareObjectDate != null) {
			compareDate = thisObjectDate.compareTo(compareObjectDate);
		}
		if (compareDate != 0) return compareDate;
		
		// Home Team
		return this.getHomeTeamName().compareTo(compareFixture.getHomeTeamName());	
	}	
}
