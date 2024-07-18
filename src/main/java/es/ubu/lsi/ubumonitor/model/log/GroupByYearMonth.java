package es.ubu.lsi.ubumonitor.model.log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import es.ubu.lsi.ubumonitor.model.LogLine;

/**
 * Agrupa los logs por mes y año
 * 
 * @author Yi Peng Ji
 *
 */
public class GroupByYearMonth extends GroupByAbstract<YearMonth> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor para agrupar la lineas de log en funcion de los usuarios.
	 * 
	 * @param logLines las lineas de log
	 */
	public GroupByYearMonth(List<LogLine> logLines) {
		super(logLines);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<YearMonth> getRange(LocalDate start, LocalDate end) {
		List<YearMonth> list = new ArrayList<>();

		for (YearMonth yearMonthStart = YearMonth.from(start), yearMonthEnd = YearMonth.from(end); !yearMonthStart
				.isAfter(yearMonthEnd); yearMonthStart = yearMonthStart.plusMonths(1)) {

			list.add(yearMonthStart);

		}
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<LogLine, YearMonth> getGroupByFunction() {
		return LogLine::getYearMonth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Function<YearMonth, String> getStringFormatFunction() {
		// return yearMonth -> yearMonth.getMonth().getDisplayName(TextStyle.FULL,
		// Locale.getDefault());
		return YearMonth::toString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeTimes getTypeTime() {
		return TypeTimes.YEAR_MONTH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean useDatePicker() {
		return true;
	}

	@Override
	public Map<YearMonth, List<LocalDateTime>> getRangeLocalDateTime(LocalDate start, LocalDate end) {
		Map<YearMonth, List<LocalDateTime>> map = new HashMap<>();
		

		for (YearMonth yearMonthStart = YearMonth.from(start), yearMonthEnd = YearMonth.from(end); !yearMonthStart
				.isAfter(yearMonthEnd); yearMonthStart = yearMonthStart.plusMonths(1)) {
			for (LocalDate day = yearMonthStart.atDay(1); !day
					.isAfter(yearMonthStart.atEndOfMonth()); day = day.plusDays(1)) {
				if (!start.isAfter(day) && !end.isBefore(day)) {
					map.computeIfAbsent(yearMonthStart, k -> new ArrayList<>())
							.add(day.atStartOfDay());
				}

			}

		}
		return map;
	}

	@Override
	public LocalDate getStartLocalDate(LocalDate start) {
		return YearMonth.from(start).atDay(1);
	}

	@Override
	public LocalDate getEndLocalDate(LocalDate end) {
		return getEndLocalDate(YearMonth.from(end));
	}

	@Override
	public LocalDate getEndLocalDate(YearMonth end) {
		return end.atEndOfMonth();
	}
}
