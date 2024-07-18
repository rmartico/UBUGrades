package es.ubu.lsi.ubumonitor.model.log;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import es.ubu.lsi.ubumonitor.model.EnrolledUser;
import es.ubu.lsi.ubumonitor.model.LogLine;

public class FirstGroupBy<E extends Serializable, T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;
	private Map<EnrolledUser, Map<E, Map<T, List<LogLine>>>> counts;
	private Map<E, Map<T, DescriptiveStatistics>> statistics;
	private GroupByAbstract<T> groupByAbstract;
	private static final List<LogLine> EMPTY_LIST = Collections.emptyList();

	public FirstGroupBy(GroupByAbstract<T> groupBy, List<LogLine> logLines, Predicate<LogLine> filter,
			Function<LogLine, E> getEFunction, Function<LogLine, T> getTFunction) {
		this.groupByAbstract = groupBy;
		setCounts(logLines, filter, getEFunction, getTFunction);
	}

	public void setCounts(List<LogLine> logLines, Predicate<LogLine> filter, Function<LogLine, E> getEFunction,
			Function<LogLine, T> getTFunction) {

		counts = logLines.stream()
				.filter(filter)
				.collect(Collectors.groupingBy(LogLine::getUser,
						Collectors.groupingBy(getEFunction, Collectors.groupingBy(getTFunction, Collectors.toList()))));
	}

	public Set<EnrolledUser> getUsers() {
		return counts.keySet();
	}

	/**
	 * Genera las estadisticas para un usuario
	 * 
	 * @param enrolledUsers usuarios que se quiere generar las estadisticas
	 * @param elements      primer listado de agrupamiento
	 * @param groupByRange  rango de un tipo de agrupacion
	 */
	public void generateStatistics(List<EnrolledUser> enrolledUsers, List<E> elements, List<T> groupByRange) {
		statistics = new HashMap<>();
		if (enrolledUsers.isEmpty() || elements.isEmpty() || groupByRange.isEmpty()) {
			return;
		}

		// el metodo computeIfAbsent devuelve el valor de la key y si no existe la key
		// se crea y devuelve el valor nuevo.
		for (EnrolledUser user : enrolledUsers) {

			Map<E, Map<T, List<LogLine>>> userCounts = counts.computeIfAbsent(user, k -> new HashMap<>());
			for (E element : elements) {

				Map<T, List<LogLine>> userComponentsCounts = userCounts.computeIfAbsent(element, k -> new HashMap<>());
				Map<T, DescriptiveStatistics> statisticsMap = statistics.computeIfAbsent(element, k -> new HashMap<>());

				for (T groupBy : groupByRange) {

					List<LogLine> count = userComponentsCounts.computeIfAbsent(groupBy, k -> EMPTY_LIST);
					DescriptiveStatistics descriptiveStatistics = statisticsMap.computeIfAbsent(groupBy,
							k -> new DescriptiveStatistics());
					descriptiveStatistics.addValue(count.size());
				}
			}
		}

	}

	/**
	 * Devuelve las medias de los componentes de un listado de usuarios, componentes
	 * y fecha de inicio y de fin
	 * 
	 * @param enrolledUsers lista de usuarios
	 * @param components    lista de componentes
	 * @param start         fecha de inicio
	 * @param end           fecha de fin
	 * @return medias de componentes
	 */
	public Map<E, List<Double>> getMeans(List<EnrolledUser> enrolledUsers, List<E> elements, LocalDate start,
			LocalDate end) {
		List<T> range = groupByAbstract.getRange(start, end);
		generateStatistics(enrolledUsers, elements, range);

		Map<E, List<Double>> results = new HashMap<>();
		for (E element : elements) {
			List<Double> means = new ArrayList<>();
			Map<T, DescriptiveStatistics> statisticsMap = statistics.computeIfAbsent(element, k -> new HashMap<>());
			for (T typeTime : range) {
				DescriptiveStatistics descriptiveStatistics = statisticsMap.computeIfAbsent(typeTime,
						k -> new DescriptiveStatistics());
				means.add(descriptiveStatistics.getMean());
			}
			results.put(element, means);

		}

		return results;
	}

	/**
	 * Devuelve los contadores de acceso a los registros de unos usuarios y
	 * componentes
	 * 
	 * @param users      usuarios
	 * @param components componentes
	 * @param start      inicio
	 * @param end        fin
	 * @return mapa multinivel
	 */
	public Map<EnrolledUser, Map<E, List<Integer>>> getUsersCounts(List<EnrolledUser> users, List<E> elements,
			LocalDate start, LocalDate end) {

		List<T> groupByRange = groupByAbstract.getRange(start, end);

		Map<EnrolledUser, Map<E, List<Integer>>> result = new HashMap<>();

		for (EnrolledUser user : users) {
			Map<E, List<Integer>> elementsCount = result.computeIfAbsent(user, k -> new HashMap<>());
			Map<E, Map<T, List<LogLine>>> userCounts = counts.computeIfAbsent(user, k -> new HashMap<>());

			for (E element : elements) {
				List<Integer> userComponentCounts = elementsCount.computeIfAbsent(element, k -> new ArrayList<>());
				Map<T, List<LogLine>> countsMap = userCounts.computeIfAbsent(element, k -> new HashMap<>());

				for (T groupBy : groupByRange) {
					List<LogLine> count = countsMap.computeIfAbsent(groupBy, k -> EMPTY_LIST);
					userComponentCounts.add(count.size());
				}
			}
		}
		return result;
	}

	/**
	 * El maximo de los componentes de los usuarios
	 * 
	 * @param enrolledUsers usuarios que se quiere buscar el maximo
	 * @param components    componentes
	 * @param start         fecha de inicio
	 * @param end           fecha de fin
	 * @return maximo encontrado
	 */
	public long getMaxElement(List<EnrolledUser> enrolledUsers, List<E> elements, LocalDate start, LocalDate end) {

		if (elements.isEmpty()) {
			return 1L;
		}

		List<T> range = groupByAbstract.getRange(start, end);

		Map<EnrolledUser, Map<T, Integer>> sumComponentsMap = new HashMap<>();

		for (EnrolledUser enrolledUser : enrolledUsers) {
			Map<E, Map<T, List<LogLine>>> elementsMap = counts.computeIfAbsent(enrolledUser, k -> new HashMap<>());
			Map<T, Integer> sumComponents = sumComponentsMap.computeIfAbsent(enrolledUser, k -> new HashMap<>());
			for (E element : elements) {
				Map<T, List<LogLine>> groupByMap = elementsMap.computeIfAbsent(element, k -> new HashMap<>());
				for (T groupBy : range) {
					sumComponents.merge(groupBy, groupByMap.getOrDefault(groupBy, EMPTY_LIST)
							.size(), Integer::sum);
				}

			}
		}

		return getMax(sumComponentsMap);

	}

	public long getCumulativeMax(List<EnrolledUser> enrolledUsers, List<E> elements, LocalDate start, LocalDate end) {
		if (elements.isEmpty()) {
			return 1L;
		}

		List<T> range = groupByAbstract.getRange(start, end);
		long max = 0;

		for (EnrolledUser enrolledUser : enrolledUsers) {
			long cum = 0;
			Map<E, Map<T, List<LogLine>>> elementsMap = counts.computeIfAbsent(enrolledUser, k -> new HashMap<>());
			for (E element : elements) {
				Map<T, List<LogLine>> groupByMap = elementsMap.computeIfAbsent(element, k -> new HashMap<>());
				for (T groupBy : range) {
					cum += groupByMap.getOrDefault(groupBy, EMPTY_LIST)
							.size();
				}

			}
			if (cum > max) {
				max = cum;
			}
		}
		return max;
	}

	public long getMeanDifferenceMax(List<EnrolledUser> enrolledUsers, List<E> logTypes, LocalDate start,
			LocalDate end) {
		if (logTypes.isEmpty()) {
			return 1L;
		}

		Map<E, List<Double>> means = getMeans(enrolledUsers, logTypes, start, end);
		List<T> range = groupByAbstract.getRange(start, end);

		List<Double> cumMeans = new ArrayList<>();
		double result = 0;
		for (int j = 0; j < range.size(); j++) {

			for (E typeLog : logTypes) {
				List<Double> times = means.get(typeLog);
				result += times.get(j);
			}
			cumMeans.add(result);

		}

		double max = 0;

		for (EnrolledUser enrolledUser : enrolledUsers) {

			Map<E, Map<T, List<LogLine>>> elementsMap = counts.computeIfAbsent(enrolledUser, k -> new HashMap<>());
			long cum = 0;
			for (int i = 0; i < cumMeans.size(); i++) {

				for (E logType : logTypes) {
					Map<T, List<LogLine>> groupByMap = elementsMap.computeIfAbsent(logType, k -> new HashMap<>());

					cum += groupByMap.getOrDefault(range.get(i), EMPTY_LIST)
							.size();

				}
				if (Math.abs(Math.ceil(cum - cumMeans.get(i))) > max) {
					max = Math.abs(Math.ceil(cum - cumMeans.get(i)));
				}
			}

		}

		return (long) max;
	}

	/**
	 * Busca el máximo mapa de usuario por cada tipo de tiempo y el contador.
	 * 
	 * @param sumMap mapa de usuario por cada tipo de tiempo y el contador
	 * @return el valor maximo encontrado
	 */
	private long getMax(Map<EnrolledUser, Map<T, Integer>> sumMap) {
		long max = 1L;

		for (Map<T, Integer> sumComponents : sumMap.values()) {
			for (long values : sumComponents.values()) {
				if (values > max) {
					max = values;
				}
			}
		}
		return max;
	}

	public Map<EnrolledUser, Map<E, List<LogLine>>> getUserLogs(List<EnrolledUser> enrolledUsers, List<E> elements,
			LocalDate start, LocalDate end) {
		Map<EnrolledUser, Map<E, List<LogLine>>> map = new HashMap<>();
		List<T> times = groupByAbstract.getRange(start, end);
		for (EnrolledUser enrolledUser : enrolledUsers) {
			Map<E, List<LogLine>> enrolledUserMap = new HashMap<>();
			map.put(enrolledUser, enrolledUserMap);
			Map<E, Map<T, List<LogLine>>> userLogs = counts.computeIfAbsent(enrolledUser, k -> new HashMap<>());
			for (E logType : elements) {
				List<LogLine> logLines = new ArrayList<>();
				enrolledUserMap.put(logType, logLines);
				Map<T, List<LogLine>> logs = userLogs.computeIfAbsent(logType, k -> new HashMap<>());
				for (T time : times) {
					List<LogLine> listLogLine = logs.computeIfAbsent(time, k -> EMPTY_LIST);
					logLines.addAll(listLogLine);
				}
			}
		}
		return map;
	}

	public Map<EnrolledUser, Integer> getUserTotalLogs(List<EnrolledUser> enrolledUsers, List<E> elements,
			LocalDate start, LocalDate end) {
		Map<EnrolledUser, Integer> map = new HashMap<>();
		List<T> times = groupByAbstract.getRange(start, end);
		for (EnrolledUser enrolledUser : enrolledUsers) {
			int userTotal = 0;
			Map<E, Map<T, List<LogLine>>> userMap = counts.getOrDefault(enrolledUser, Collections.emptyMap());
			for (E element : elements) {
				Map<T, List<LogLine>> elementMap = userMap.getOrDefault(element, Collections.emptyMap());
				for (T time : times) {
					userTotal += elementMap.getOrDefault(time, EMPTY_LIST)
							.size();
				}
			}
			map.put(enrolledUser, userTotal);
		}
		return map;
	}

	public Map<EnrolledUser, Map<E, Integer>> getUserLogsGroupedByLogElement(List<EnrolledUser> users, List<E> elements,
			LocalDate start, LocalDate end) {
		List<T> groupByRange = groupByAbstract.getRange(start, end);

		Map<EnrolledUser, Map<E, Integer>> result = new HashMap<>();

		for (EnrolledUser user : users) {
			Map<E, Integer> elementsCount = result.computeIfAbsent(user, k -> new HashMap<>());
			Map<E, Map<T, List<LogLine>>> userCounts = counts.computeIfAbsent(user, k -> new HashMap<>());

			for (E element : elements) {
				
				Map<T, List<LogLine>> countsMap = userCounts.computeIfAbsent(element, k -> new HashMap<>());
				int count = 0;
				for (T groupBy : groupByRange) {
					count+=countsMap.computeIfAbsent(groupBy, k->EMPTY_LIST).size();
				}
				elementsCount.put(element, count);
			}
		}
		return result;
	}
}
