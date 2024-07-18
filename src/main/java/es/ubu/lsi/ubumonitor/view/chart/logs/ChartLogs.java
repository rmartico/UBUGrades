package es.ubu.lsi.ubumonitor.view.chart.logs;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ubu.lsi.ubumonitor.controllers.MainController;
import es.ubu.lsi.ubumonitor.controllers.SelectionController;
import es.ubu.lsi.ubumonitor.controllers.tabs.VisualizationController;
import es.ubu.lsi.ubumonitor.model.Component;
import es.ubu.lsi.ubumonitor.model.ComponentEvent;
import es.ubu.lsi.ubumonitor.model.CourseModule;
import es.ubu.lsi.ubumonitor.model.Section;
import es.ubu.lsi.ubumonitor.model.datasets.DataSet;
import es.ubu.lsi.ubumonitor.model.log.FirstGroupBy;
import es.ubu.lsi.ubumonitor.model.log.GroupByAbstract;
import es.ubu.lsi.ubumonitor.util.JSObject;
import es.ubu.lsi.ubumonitor.util.LogAction;
import es.ubu.lsi.ubumonitor.view.chart.Chart;
import es.ubu.lsi.ubumonitor.view.chart.ChartType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;

public abstract class ChartLogs extends Chart {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChartLogs.class);
	protected DatePicker datePickerStart;
	protected DatePicker datePickerEnd;
	protected ChoiceBox<GroupByAbstract<?>> choiceBoxDate;
	protected TextField textFieldMax;
	protected TabPane tabPaneSelection;
	protected Tab tabComponent;
	protected Tab tabEvent;
	protected Tab tabSection;
	protected Tab tabCourseModule;
	protected ListView<Component> listViewComponent;
	protected ListView<ComponentEvent> listViewEvent;
	protected ListView<Section> listViewSection;
	protected ListView<CourseModule> listViewCourseModule;
	protected SelectionController selectionController;

	private String max;

	public ChartLogs(MainController mainController, ChartType chartType) {
		super(mainController, chartType);
		this.selectionController = mainController.getSelectionController();
		VisualizationController visualizationController = mainController.getWebViewTabsController()
				.getVisualizationController();
		this.datePickerStart = visualizationController.getDatePickerStart();
		this.datePickerEnd = visualizationController.getDatePickerEnd();
		this.choiceBoxDate = visualizationController.getChoiceBoxDate();
		this.textFieldMax = visualizationController.getTextFieldMax();
		this.tabPaneSelection = selectionController.getTabPaneUbuLogs();
		this.tabComponent = selectionController.getTabUbuLogsComponent();
		this.tabEvent = selectionController.getTabUbuLogsEvent();
		this.tabSection = selectionController.getTabUbuLogsSection();
		this.tabCourseModule = selectionController.getTabUbuLogsCourseModule();
		this.listViewComponent = selectionController.getListViewComponents();
		this.listViewEvent = selectionController.getListViewEvents();
		this.listViewSection = selectionController.getListViewSection();
		this.listViewCourseModule = selectionController.getListViewCourseModule();

	}

	@Override
	public String getMax() {
		return max;
	}

	@Override
	public void setMax(String max) {
		this.max = max;
	}

	@Override
	public void update() {
		String dataset = selectionController.typeLogsAction(new LogAction<String>() {

			@Override
			public <E extends Serializable, T extends Serializable> String action(List<E> logType, DataSet<E> dataSet,
					Function<GroupByAbstract<?>, FirstGroupBy<E, T>> function) {
				return createData(logType, dataSet);
			}
		});

		JSObject options = getOptions();
		LOGGER.info("Dataset {} en JS: {}", chartType, dataset);
		LOGGER.info("Opciones {} en JS: {}", chartType, options);
		try {
			webViewChartsEngine.executeScript(getJSFunction(dataset, options.toString()));
		} catch (Exception e) {
			LOGGER.info("Error al actualizar la grafica", e);
		}
	}

	@Override
	public void exportCSV(String path) throws IOException {
		String[] header = getCSVHeader();
		try (CSVPrinter printer = new CSVPrinter(getWritter(path), CSVFormat.DEFAULT.withHeader(header))) {
			selectionController.typeLogsAction(new LogAction<Void>() {

				@Override
				public <E extends Serializable, T extends Serializable> Void action(List<E> logType, DataSet<E> dataSet,
						Function<GroupByAbstract<?>, FirstGroupBy<E, T>> function) {
					try {
						exportCSV(printer, dataSet, logType);
						return null;
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
				}
			});
		}

	}

	public boolean hasId() {
		return tabSection.isSelected() || tabCourseModule.isSelected();
	}

	@Override
	public void exportCSVDesglosed(String path) throws IOException {
		String[] header = getCSVDesglosedHeader();
		try (CSVPrinter printer = new CSVPrinter(getWritter(path), CSVFormat.DEFAULT.withHeader(header))) {
			selectionController.typeLogsAction(new LogAction<Void>() {

				@Override
				public <E extends Serializable, T extends Serializable> Void action(List<E> logType, DataSet<E> dataSet,
						Function<GroupByAbstract<?>, FirstGroupBy<E, T>> function) {
					try {
						exportCSVDesglosed(printer, dataSet, logType);
						return null;
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
				}
			});
		}

	}

	@Override
	public boolean isCalculateMaxActivated() {
		return getConfigValue("calculateMax", false);
	}

	@Override
	public long getSuggestedMax(String maxString) {
		if (maxString == null || maxString.isEmpty()) {
			return 0;
		}
		return Long.valueOf(maxString);

	}

	protected abstract String getJSFunction(String dataset, String options);

	public abstract <E> String createData(List<E> typeLogs, DataSet<E> dataSet);

	protected abstract <E> void exportCSV(CSVPrinter printer, DataSet<E> dataSet, List<E> typeLogs) throws IOException;

	protected abstract String[] getCSVHeader();

	protected abstract <E> void exportCSVDesglosed(CSVPrinter printer, DataSet<E> dataSet, List<E> typeLogs)
			throws IOException;

	protected abstract String[] getCSVDesglosedHeader();

}
