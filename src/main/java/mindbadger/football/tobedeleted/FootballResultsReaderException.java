package mindbadger.football.tobedeleted;

public class FootballResultsReaderException extends RuntimeException {
	private static final long serialVersionUID = 6763661656254528431L;

	public FootballResultsReaderException(Exception e) {
		super(e);
	}

	public FootballResultsReaderException(String message) {
		super(message);
	}
}
