package mindbadger.football.web.reader;

import java.io.IOException;
import java.util.List;

public interface TeamFixturesPageParser {
    List<ParsedFixture> getFixturesFromTeamFixturesPage (Integer seasonNumber, String sourceTeamId, List<String> divisionsToInclude)
            throws IOException;
}
