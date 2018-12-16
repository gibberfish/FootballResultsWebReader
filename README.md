# Football Results Web Reader API

## To build
```
mvn clean spring-boot:run
```

## Parse Fixtures for Team in Season
e.g.
```
http://localhost:1990/webreader/getFixturesForTeam?ssnNum=2018&teamId=2049&trackedDiv=1&trackedDiv=2&trackedDiv=3&trackedDiv=4
```
The teamId is the ID of the source system (in our case Soccerbase)
The list of trackedDiv ids determines which tournaments will be included.

## Parse Fixtures for Fixture Date
e.g.
```
http://localhost:1990/webreader/getFixturesForDate?date=2008-12-26&trackedDiv=1&trackedDiv=2&trackedDiv=3&trackedDiv=4
```
The date is in the format YYYY-MM-DD.
The list of trackedDiv ids determines which tournaments will be included.

## Example of a parsed fixture
```
{
    seasonId: 2008,
    fixtureDate: "2008-12-26",
    divisionId: "1",
    divisionName: "Premier League",
    homeTeamId: "2477",
    homeTeamName: "Stoke",
    awayTeamId: "1724",
    awayTeamName: "Man Utd",
    homeGoals: 0,
    awayGoals: 1
}
```
