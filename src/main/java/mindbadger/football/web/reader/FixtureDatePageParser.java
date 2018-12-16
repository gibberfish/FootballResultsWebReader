package mindbadger.football.web.reader;

import java.util.Calendar;
import java.util.List;

public interface FixtureDatePageParser {
    List<ParsedFixture> getFixturesFromDatePage (Calendar date, List<String> divisionsToInclude);
}
