package mindbadger.football.web.reader;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public interface FixtureDatePageParser {
    List<ParsedFixture> getFixturesFromDatePage (Calendar date, List<String> divisionsToInclude)
            throws IOException;
}
