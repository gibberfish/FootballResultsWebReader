package mindbadger.football.web.reader.api;

import mindbadger.football.web.reader.FixtureDatePageParser;
import mindbadger.football.web.reader.ParsedFixture;
import mindbadger.football.web.reader.TeamFixturesPageParser;
import mindbadger.football.web.reader.util.StringToCalendarConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("webreader")
public class WebReaderController {

    @Autowired
    private FixtureDatePageParser fixtureDatePageParser;

    @Autowired
    private TeamFixturesPageParser teamFixturesPageParser;

    @GetMapping(value = "/getFixturesForDate", produces = "application/json")
    public ResponseEntity<List<ParsedFixture>> getFixturesForDate (@RequestParam(value="date") String date,
                                                                 @RequestParam(value="trackedDiv") String[] trackedDivisions) {
        List<String> trackedDivisionList = Arrays.asList(trackedDivisions);
        try {
            List<ParsedFixture> parsedFixtures = fixtureDatePageParser.getFixturesFromDatePage
                    (StringToCalendarConverter.convertDateStringToCalendar(date), trackedDivisionList);
            return new ResponseEntity<>(parsedFixtures, HttpStatus.OK);
        } catch (IOException exception) {
            return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @GetMapping(value = "/getFixturesForTeam", produces = "application/json")
    public ResponseEntity<List<ParsedFixture>> getFixturesForTeam (@RequestParam(value="ssnNum") Integer ssnNum,
                                                                 @RequestParam(value="teamId") String teamId,
                                                                 @RequestParam(value="trackedDiv") String[] trackedDivisions) {
        List<String> trackedDivisionList = Arrays.asList(trackedDivisions);
        try {
            List<ParsedFixture> parsedFixtures = teamFixturesPageParser.getFixturesFromTeamFixturesPage(ssnNum, teamId, trackedDivisionList);
            return new ResponseEntity<>(parsedFixtures, HttpStatus.OK);
        } catch (IOException exception) {
            return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
        }
    }
}
