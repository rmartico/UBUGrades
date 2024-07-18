package es.ubu.lsi.ubumonitor.model.log;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.threeten.extra.YearWeek;

import es.ubu.lsi.ubumonitor.model.LogLine;

/**
 * Agrupación de los logs por semana y año.
 * 
 * @author Yi Peng Ji
 *
 */
public class GroupByYearWeek extends GroupByAbstract<YearWeek> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

	/**
	 * Constructor para agrupar la lineas de log en funcion de los usuarios.
	 * 
	 * @param logLines las lineas de log
	 */
	public GroupByYearWeek(List<LogLine> logLines) {
		super(logLines);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<YearWeek> getRange(LocalDate start, LocalDate end) {
		List<YearWeek> list = new ArrayList<>();

		for (YearWeek yearWeekStart = YearWeek.from(start), yearWeekEnd = YearWeek.from(end); !yearWeekStart
				.isAfter(yearWeekEnd); yearWeekStart = yearWeekStart.plusWeeks(1)) {

			list.add(yearWeekStart);

		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<LogLine, YearWeek> getGroupByFunction() {
		return LogLine::getYearWeek;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<YearWeek, String> getStringFormatFunction() {
		return yearWeek -> yearWeek.atDay(WeekFields.of(Locale.getDefault())
				.getFirstDayOfWeek())
				.format(DATE_TIME_FORMATTER);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeTimes getTypeTime() {
		return TypeTimes.YEAR_WEEK;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean useDatePicker() {
		return true;
	}

	@Override
	public Map<YearWeek, List<LocalDateTime>> getRangeLocalDateTime(LocalDate start, LocalDate end) {
		DayOfWeek dayOfWeek = WeekFields.of(Locale.getDefault())
				.getFirstDayOfWeek();
		Map<YearWeek, List<LocalDateTime>> map = new HashMap<>();

		for (YearWeek yearWeekStart = YearWeek.from(start), yearWeekEnd = YearWeek.from(end); !yearWeekStart
				.isAfter(yearWeekEnd); yearWeekStart = yearWeekStart.plusWeeks(1)) {

			for (LocalDate day = yearWeekStart.atDay(dayOfWeek); !day
					.isAfter(yearWeekStart.atDay(dayOfWeek.minus(1))); day = day.plusDays(1)) {
				if (!start.isAfter(day) && !end.isBefore(day)) {
					map.computeIfAbsent(yearWeekStart, k -> new ArrayList<>())
							.add(day.atStartOfDay());
				}
			}

		}
		return map;
	}

	@Override
	public LocalDate getStartLocalDate(LocalDate start) {
		DayOfWeek dayOfWeek = WeekFields.of(Locale.getDefault())
				.getFirstDayOfWeek();
		return YearWeek.from(start)
				.atDay(dayOfWeek);
	}

	@Override
	public LocalDate getEndLocalDate(LocalDate end) {
		return getEndLocalDate(YearWeek.from(end));
	}

	@Override
	public LocalDate getEndLocalDate(YearWeek end) {
		DayOfWeek lastDayOfWeek = WeekFields.of(Locale.getDefault())
				.getFirstDayOfWeek()
				.minus(1);
		return end.atDay(lastDayOfWeek);
	}

}
