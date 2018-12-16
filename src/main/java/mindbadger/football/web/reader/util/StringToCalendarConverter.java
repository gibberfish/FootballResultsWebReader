package mindbadger.football.web.reader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StringToCalendarConverter {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
	
	public static Calendar convertDateStringToCalendar(String dateString) {
		if (dateString == null || "".equals(dateString)) {
			return null;
		}
		
		Calendar cal = Calendar.getInstance();
		
		try {
			cal.setTime(sdf.parse(dateString));
			return cal;
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public static String convertCalendarToDateString (Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		
		return sdf.format(calendar.getTime());
	}
}
