package es.ubu.lsi.ubumonitor.model;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Session {
	private List<LogLine> logLines;

	public Session(LogLine start) {
		logLines = new ArrayList<>();
		logLines.add(start);
	}

	public void add(LogLine next) {
		logLines.add(next);
	}

	public long getDiffMinutes() {
		ZonedDateTime start = logLines.get(0)
				.getTime();
		ZonedDateTime end = logLines.get(logLines.size() - 1)
				.getTime();

		return Math.max(start.until(end, ChronoUnit.MINUTES), 1);
	}

	public ZonedDateTime getFirstLogTime() {
		return logLines.get(0)
				.getTime();
	}

	public ZonedDateTime getLastLogTime() {
		return logLines.get(logLines.size() - 1)
				.getTime();

	}

}
