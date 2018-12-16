package mindbadger.football.tobedeleted;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebPageReader {
	Logger logger = Logger.getLogger(WebPageReader.class);
	
	@Autowired
	@Value("${http.agent}")
	private String httpAgent;
	
	public List<String> readWebPage(String pURL) throws FileNotFoundException, IOException {
		
		if (httpAgent == null) throw new RuntimeException("Must set httpAgent");
		
		logger.info("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");
		logger.info("About to load web page: " + pURL + "...");
		logger.info("   using httpAgent: " + httpAgent + "...");
		logger.info("+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");
		
		ArrayList<String> results = new ArrayList <String>();
		BufferedReader in = null;
		InputStreamReader webReader = null;

		try {
			URL url = new URL(pURL);
			
			HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
			httpcon.addRequestProperty("User-Agent", httpAgent);
			InputStream openStream = httpcon.getInputStream();
			
			webReader = new InputStreamReader(openStream);
			
			in = new BufferedReader(webReader);

			String line;
			while ((line = in.readLine()) != null) {
				results.add(line);
			}
		} finally {
			// Force the close of the file...
			if (webReader != null)
				try {
					webReader.close();
				} catch (IOException e) {
					;
				}
		}
		logger.debug("   Number of lines read from page: " + results.size());
		return results;
	}

	public String getHttpAgent() {
		return httpAgent;
	}

	public void setHttpAgent(String httpAgent) {
		this.httpAgent = httpAgent;
	}

//	public static void main(String[] args) throws FileNotFoundException, IOException {
//		WebPageReader reader = new WebPageReader();
//		reader.setHttpAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
//		//List<String> output = reader.readWebPage("http://www.soccerbase.com/teams/team.sd?team_id=2802&teamTabs=results");
//		List<String> output = reader.readWebPage("http://www.soccerbase.com/matches/results.sd?date=1998-12-26");
//		for (String line : output) {
//			System.out.println(line);
//		}
//	}
	
}
