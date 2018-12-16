package mindbadger.football.web.reader.api;

import mindbadger.football.web.reader.FixtureDatePageParser;
import mindbadger.football.web.reader.ParsedFixture;
import mindbadger.football.web.reader.TeamFixturesPageParser;
import mindbadger.football.web.reader.util.StringToCalendarConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public @ResponseBody List<ParsedFixture> getFixturesForDate (@RequestParam(value="date") String date,
                                                                 @RequestParam(value="trackedDiv") String[] trackedDivisions) {
        List<String> trackedDivisionList = Arrays.asList(trackedDivisions);
        return fixtureDatePageParser.getFixturesFromDatePage
                (StringToCalendarConverter.convertDateStringToCalendar(date),
                trackedDivisionList);
    }

    @GetMapping(value = "/getFixturesForTeam", produces = "application/json")
    public @ResponseBody List<ParsedFixture> getFixturesForTeam (@RequestParam(value="ssnNum") Integer ssnNum,
                                                                 @RequestParam(value="teamId") String teamId,
                                                                 @RequestParam(value="trackedDiv") String[] trackedDivisions) {
        List<String> trackedDivisionList = Arrays.asList(trackedDivisions);
        return teamFixturesPageParser.getFixturesFromTeamFixturesPage(ssnNum, teamId, trackedDivisionList);
    }
}
