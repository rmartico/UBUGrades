package es.ubu.lsi.ubumonitor.controllers.load;

import java.io.IOException;
import java.io.Reader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ubu.lsi.ubumonitor.controllers.Controller;
import es.ubu.lsi.ubumonitor.model.Component;
import es.ubu.lsi.ubumonitor.model.Event;
import es.ubu.lsi.ubumonitor.model.LogLine;
import es.ubu.lsi.ubumonitor.model.Logs;
import es.ubu.lsi.ubumonitor.model.Origin;
import es.ubu.lsi.ubumonitor.model.log.logtypes.LogTypes;
import es.ubu.lsi.ubumonitor.model.log.logtypes.ReferencesLog;
import es.ubu.lsi.ubumonitor.util.I18n;
import okhttp3.Response;

/**
 * Clase encargada de los logs, con metodos encargados de descargar los logs y
 * parsearlo.
 * 
 * @author Yi Peng Ji
 *
 */
public class LogCreator {
	private static final Logger LOGGER = LoggerFactory.getLogger(LogCreator.class);

	private static final Controller CONTROLLER = Controller.getInstance();

	private static DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("d/MM/yy, kk:mm");
	private static DateTimeFormatter dateTimeFormatter4 = DateTimeFormatter.ofPattern("d/MM/yy, kk:mm:ss");
	private static DateTimeFormatter actualDateTimeFormatter;
	private static final Set<String> NOT_AVAIBLE_COMPONENTS = new TreeSet<>();
	private static final Set<String> NOT_AVAIBLE_EVENTS = new TreeSet<>();
	
	// Recoge los numeros entre comillas simples o dobles
	// Sin escapar: (['"])(?<idQuote>\d+)\1|[^'"](?<idNoQuote>\d+)[^'"]
	private static final Pattern INTEGER_PATTERN = Pattern
			.compile("(['\"])(?<idQuote>-?\\d+)\\1|[^'\"](?<idNoQuote>\\d+)[^'\"]");

	public static final String TIME = "Time";
	public static final String USER_FULL_NAME = "User full name";
	public static final String AFFECTED_USER = "Affected user";
	public static final String EVENT_CONTEXT = "Event context";
	public static final String COMPONENT = "Component";
	public static final String EVENT_NAME = "Event name";
	public static final String DESCRIPTION = "Description";
	public static final String ORIGIN = "Origin";
	public static final String IP_ADRESS = "IP address";

	private static final Set<String> ALL_COLUMNS = new HashSet<>(Arrays.asList(TIME, USER_FULL_NAME, AFFECTED_USER,
			EVENT_CONTEXT, COMPONENT, EVENT_NAME, DESCRIPTION, ORIGIN, IP_ADRESS));

	/**
	 * Cambia la zona horia del formateador de tiempo
	 * 
	 * @param zoneId zona horaria
	 */
	public static void setDateTimeFormatter(ZoneId zoneId) {
		actualDateTimeFormatter = actualDateTimeFormatter.withZone(zoneId);
	}
	
	public static void setDateTimeFormatter(String release) {
		if (release.startsWith("4")) {
			actualDateTimeFormatter = dateTimeFormatter4;
		}else {
			actualDateTimeFormatter = dateTimeFormatter3;
		}
	}

	public static void createLogsMultipleDays(Logs logs) throws IOException {
		ZoneId userZone = CONTROLLER.getUser()
				.getTimezone();
		ZoneId serverZone = CONTROLLER.getUser()
				.getServerTimezone();
		LOGGER.info("Zona horaria del usuario: {}", userZone);
		LOGGER.info("Zona horaria del servidor: {}", serverZone);
		DownloadLogController download = new DownloadLogController(CONTROLLER.getUrlHost()
				.toString(),
				CONTROLLER.getActualCourse()
						.getId(),
				userZone, serverZone);

		setDateTimeFormatter(download.getUserTimeZone());

		ZonedDateTime lastDateTime = logs.getLastZonedDatetime();
		ZonedDateTime now = ZonedDateTime.now()
				.withZoneSameInstant(lastDateTime.getZone());
		LOGGER.info("La fecha del ultimo log antes de actualizar es {}", lastDateTime);
		while (now.isAfter(lastDateTime)) {
			Response response = download.downloadLog(lastDateTime, false);
			parserResponse(logs, response.body()
					.charStream());
			lastDateTime = lastDateTime.plusDays(1);
		}
		if (!NOT_AVAIBLE_COMPONENTS.isEmpty()) {
			LOGGER.warn("Not avaible components: {}", NOT_AVAIBLE_COMPONENTS);
		}
		if (!NOT_AVAIBLE_EVENTS.isEmpty()) {
			LOGGER.warn("Not avaible events: {}", NOT_AVAIBLE_EVENTS);
		}

	}

	public static DownloadLogController download() {
		DownloadLogController download = new DownloadLogController(CONTROLLER.getUrlHost()
				.toString(),
				CONTROLLER.getActualCourse()
						.getId(),
				CONTROLLER.getUser()
						.getTimezone(),
				CONTROLLER.getUser()
						.getServerTimezone());

		setDateTimeFormatter(download.getUserTimeZone());
		return download;
	}

	public static void parserResponse(Logs logs, Reader reader) throws IOException {

		try (CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
			List<LogLine> logList = LogCreator.createLogs(csvParser);
			logs.addAll(logList);
		}
		if (!NOT_AVAIBLE_COMPONENTS.isEmpty()) {
			LOGGER.warn("Not avaible components: {}", NOT_AVAIBLE_COMPONENTS);
		}
		if (!NOT_AVAIBLE_EVENTS.isEmpty()) {
			LOGGER.warn("Not avaible events: {}", NOT_AVAIBLE_EVENTS);
		}

	}

	/**
	 * Crea todos los logs la lista
	 * 
	 * @param allLogs los logs listados en mapas con clave la columna del logline
	 * @return los logs creados
	 */
	private static List<LogLine> createLogs(CSVParser csvParser) {
		LinkedList<LogLine> logs = new LinkedList<>();
		Set<String> headers = csvParser.getHeaderMap()
				.keySet();
		LOGGER.info("Los nombres de las columnas del csv son: {}", headers);
		if (!headers.isEmpty() && headers.stream()
				.noneMatch(ALL_COLUMNS::contains)) {
			throw new IllegalArgumentException(I18n.get("error.logsEnglish"));
		}
		for (CSVRecord csvRecord : csvParser) {
			LogLine logLine = createLog(headers, csvRecord);

			logs.addFirst(logLine);

		}

		return logs;
	}

	/**
	 * Crea un log y añade los atributos de las columnas y a que van asociado
	 * (usuario, course module etc)
	 * 
	 * @param mapLog mapa con clave las columnas de la linea de log
	 * @return logline con atributos
	 */
	public static LogLine createLog(Set<String> headers, CSVRecord csvRecord) {

		LogLine log = new LogLine();
		Component component;
		if (headers.contains(LogCreator.COMPONENT)) {
			component = Component.get(csvRecord.get(LogCreator.COMPONENT));
			if (component == Component.COMPONENT_NOT_AVAILABLE) {
				NOT_AVAIBLE_COMPONENTS.add(csvRecord.get(LogCreator.COMPONENT));
			}
		} else {
			component = Component.COMPONENT_NOT_AVAILABLE;
		}
		log.setComponent(component);

		Event event;
		if (headers.contains(LogCreator.EVENT_NAME)) {
			event = Event.get(csvRecord.get(LogCreator.EVENT_NAME));
			if (event == Event.EVENT_NOT_AVAILABLE) {
				NOT_AVAIBLE_EVENTS.add(csvRecord.get(LogCreator.EVENT_NAME));
			}
		} else {
			event = Event.EVENT_NOT_AVAILABLE;
		}
		log.setEventName(event);

		if (headers.contains(LogCreator.DESCRIPTION)) {
			String description = csvRecord.get(LogCreator.DESCRIPTION);
			List<Integer> ids = getIdsInDescription(description);
			try {
				ReferencesLog referencesLog = LogTypes.getReferenceLog(component, event);
				referencesLog.setLogReferencesAttributes(log, ids);
				if (headers.contains(LogCreator.USER_FULL_NAME) && log.getUser() != null && log.getUser()
						.getFullName() == null) {
					log.getUser()
							.setFullName(csvRecord.get(LogCreator.USER_FULL_NAME));
				}
			} catch (Exception e) {
				LOGGER.error("Problema en linea de log: " + csvRecord + " usando el gestor: con los ids:" + ids, e);
			}
		}

		//considering Time column always first
		String time = csvRecord.get(0);

		ZonedDateTime zdt = ZonedDateTime.parse(time, actualDateTimeFormatter);
		log.setTime(zdt);

		if (headers.contains(LogCreator.ORIGIN)) {
			
		
			log.setOrigin(Origin.get(csvRecord.get(LogCreator.ORIGIN)));

		}

		if (headers.contains(LogCreator.IP_ADRESS)) {
			log.setIPAdress(csvRecord.get(LogCreator.IP_ADRESS));
		}

		return log;
	}

	/**
	 * Devuelve los componentes que no existen en el enum {@link model.Component} al
	 * parsear logs
	 * 
	 * @return los componentes que no existen en el enum {@link model.Component}
	 */
	public static Set<String> getNotAvaibleComponents() {
		return NOT_AVAIBLE_COMPONENTS;
	}

	/**
	 * Devuelve los eventos que no existen en el enum {@link model.Event} al parsear
	 * los logs
	 * 
	 * @return eventos que no existen en el enum {@link model.Event}
	 */
	public static Set<String> getNotAvaibleEvents() {
		return NOT_AVAIBLE_EVENTS;
	}

	/**
	 * Busca los integer de la Descripción de una linea de log
	 * 
	 * @param description de la columna decripción
	 * @return lista de integer encontrado en la descripción
	 */
	private static List<Integer> getIdsInDescription(String description) {
		Matcher m = INTEGER_PATTERN.matcher(description);
		List<Integer> list = new ArrayList<>();
		while (m.find()) {
			String integer = null;
			if (m.group("idQuote") != null) { // si el id esta entre comillas simples o dobles
				integer = m.group("idQuote");
			} else if (m.group("idNoQuote") != null) { // si el id no esta entre comillas
				integer = m.group("idNoQuote");
			}

			if (integer != null) {
				try {
					int value = Integer.parseInt(integer);
					list.add(value);
				}catch (Exception e){
					LOGGER.warn("No se ha podido convertir a integer el registro.", e);
				}
				
			}
		}
		return list;
	}

	private LogCreator() {
		throw new UnsupportedOperationException();
	}

}
