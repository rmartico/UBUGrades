package es.ubu.lsi.ubumonitor.view.chart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.controlsfx.control.CheckComboBox;

import es.ubu.lsi.ubumonitor.controllers.Controller;
import es.ubu.lsi.ubumonitor.controllers.MainController;
import es.ubu.lsi.ubumonitor.controllers.SelectionUserController;
import es.ubu.lsi.ubumonitor.controllers.configuration.MainConfiguration;
import es.ubu.lsi.ubumonitor.model.Course;
import es.ubu.lsi.ubumonitor.model.EnrolledUser;
import es.ubu.lsi.ubumonitor.model.GradeItem;
import es.ubu.lsi.ubumonitor.model.Group;
import es.ubu.lsi.ubumonitor.model.Role;
import es.ubu.lsi.ubumonitor.util.Charsets;
import es.ubu.lsi.ubumonitor.util.I18n;
import es.ubu.lsi.ubumonitor.util.JSArray;
import es.ubu.lsi.ubumonitor.util.JSObject;
import es.ubu.lsi.ubumonitor.util.UtilMethods;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public abstract class Chart implements ExportableChart {

	protected static final DescriptiveStatistics EMPTY_DESCRIPTIVE_STATISTICS = new DescriptiveStatistics();

	protected WebEngine webViewChartsEngine;
	private CheckComboBox<Group> checkComboBoxGroup;
	protected ChartType chartType;
	protected boolean useLegend;
	protected boolean useGeneralButton;
	protected boolean useGroupButton;
	protected boolean useGroupBy;

	protected boolean useRangeDate;
	protected boolean useLogs;

	protected boolean useNegativeValues;
	protected boolean useOptions;
	protected Controller controller = Controller.getInstance();
	protected MainController mainController;
	private MainConfiguration mainConfiguration;
	protected SelectionUserController selectionUserController;
	protected Course actualCourse;
	protected static final double OPACITY = 0.2;
	protected WebView webView;

	public Chart(MainController mainController, ChartType chartType) {

		this.selectionUserController = mainController.getSelectionUserController();
		this.checkComboBoxGroup = selectionUserController.getCheckComboBoxGroup();
		this.mainController = mainController;
		this.chartType = chartType;

	}

	public boolean isUseLegend() {
		return useLegend;
	}

	public boolean isUseGeneralButton() {
		return useGeneralButton;
	}

	public boolean isUseGroupButton() {
		return useGroupButton;
	}

	public ChartType getChartType() {
		return chartType;
	}

	public void setChartType(ChartType chartType) {
		this.chartType = chartType;
	}

	public List<EnrolledUser> getSelectedEnrolledUser() {
		return selectionUserController.getSelectedUsers();
	}

	public List<EnrolledUser> getFilteredUsers() {
		return selectionUserController.getFilteredUsers();
	}

	public double adjustTo10(double value) {
		return Double.isNaN(value) ? value : Math.round(value * 10) / 100.0;
	}

	public int onClick(int index) {

		if (getSelectedEnrolledUser().size() < index) {
			return -1;
		}

		EnrolledUser selectedUser = getSelectedEnrolledUser().get(index);
		return getFilteredUsers().indexOf(selectedUser);
	}

	public static <T> String rgba(T hash, double alpha) {
		return "colorRGBA('" + UtilMethods.escapeJavaScriptText(hash.toString()) + "'," + alpha + ")";
	}

	public static <T> String rgb(T hash) {
		return "colorRGB('" + UtilMethods.escapeJavaScriptText(hash.toString()) + "')";
	}

	public static <T> String hex(T hash) {
		return "colorHEX('" + UtilMethods.escapeJavaScriptText(hash.toString()) + "')";
	}

	public String colorToRGB(Color color) {

		return colorToRGB(color, color.getOpacity());
	}
	
	public String awtColorToRGB(java.awt.Color color) {
		return awtColorToRGB(color, color.getAlpha()/255.0);
	}

	public String awtColorToRGB(java.awt.Color color, double opacity) {
		return String.format("'rgba(%s,%s,%s,%s)'", color.getRed(), color.getGreen(),  color.getBlue(), opacity);
	}

	
	public String colorToRGB(Color color, double opacity) {

		return String.format("'rgba(%s,%s,%s,%s)'", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255), opacity);
	}

	public List<GradeItem> getSelectedGradeItems(TreeView<GradeItem> treeViewGradeItem) {
		return UtilMethods.getSelectedGradeItems(treeViewGradeItem);
	}

	public List<Group> getSelectedGroups() {
		List<Group> groups = new ArrayList<>(checkComboBoxGroup.getCheckModel()
				.getCheckedItems());
		groups.removeIf(g -> g == null || g.getGroupId() < 0);
		return groups;
	}

	public List<Role> getSelectedRoles() {
		List<Role> roles = new ArrayList<>(selectionUserController.getCheckComboBoxRole()
				.getCheckModel()
				.getCheckedItems());
		roles.removeIf(r -> r == null || r.getRoleId() < 0);
		return roles;
	}
	
	/**
	 * Filter user with one or more specified roles
	 * @param user users to filter
	 * @param roles roler to filter
	 * @return filtered users
	 */
	public List<EnrolledUser> getUserWithRole(Collection<EnrolledUser> user, Collection<Role> roles) {
		Set<EnrolledUser> usersInRoles =getUserWithRole(roles);
		return user.stream()
				.filter(usersInRoles::contains)
				.collect(Collectors.toList());

	}

	/**
	 * Return all users with the collection role
	 * @param roles
	 * @return users with one or more roles
	 */
	public Set<EnrolledUser> getUserWithRole(Collection<Role> roles) {
		return  roles.stream()
				.map(Role::getEnrolledUsers)
				.flatMap(Set::stream)
				.distinct()
				.collect(Collectors.toSet());
	}

	public abstract void fillOptions(JSObject jsObject);

	public JSObject getOptions() {
		JSObject jsObject = getDefaultOptions();
		fillOptions(jsObject);
		return jsObject;

	}

	public abstract void update();

	public abstract void clear();

	public void exportImage(File file) throws IOException {
		UtilMethods.snapshotNode(file, webView);
		UtilMethods.showExportedFile(file);
	}

	public JSObject getDefaultOptions() {

		JSObject jsObject = new JSObject();
		jsObject.put("chartBackgroundColor",
				colorToRGB(mainConfiguration.getValue(MainConfiguration.GENERAL, "chartBackgroundColor")));
		jsObject.put("useLegend", useLegend);
		jsObject.put("useGroup", useGroupButton);
		jsObject.put("useGeneral", useGeneralButton);
		jsObject.putWithQuote("tab", chartType.getTab());
		jsObject.putWithQuote("button", getChartType().name());
		jsObject.put("legendActive", mainConfiguration.getValue(MainConfiguration.GENERAL, "legendActive"));
		jsObject.put("generalActive", mainConfiguration.getValue(MainConfiguration.GENERAL, "generalActive"));
		jsObject.put("groupActive", mainConfiguration.getValue(MainConfiguration.GENERAL, "groupActive"));
		JSArray jsArray = new JSArray();
		ObservableList<ChartType> listCharts = mainConfiguration.getValue(MainConfiguration.GENERAL, "listCharts");

		jsArray.addAllWithQuote(listCharts);
		jsArray.addAllWithQuote(ChartType.getDefaultValues());
		jsObject.put("listCharts", jsArray);

		return jsObject;

	}

	public String calculateMax() {
		return null;
	}

	public boolean isUseNegativeValues() {
		return useNegativeValues;
	}

	public boolean isCalculateMaxActivated() {
		return Controller.getInstance()
				.getMainConfiguration()
				.getValue(getChartType(), "calculateMax", false);
	}

	public long getSuggestedMax(String maxString) {
		if (maxString == null || maxString.isEmpty()) {
			return 0;
		}
		return Long.valueOf(maxString);

	}

	public void setUseNegativeValues(boolean useNegativeValues) {
		this.useNegativeValues = useNegativeValues;
	}

	public String getYAxisTitle() {
		return I18n.get(getChartType() + ".yAxisTitle");

	}

	public String getXAxisTitle() {
		return I18n.get(getChartType() + ".xAxisTitle");

	}

	public String getMax() {
		return null;
	}

	public void setMax(String max) {
		// do nothing

	}

	public boolean isUseGroupBy() {
		return useGroupBy;
	}

	public boolean isUseRangeDate() {
		return useRangeDate;
	}

	@Override
	public Writer getWritter(String path) throws IOException {
		Charsets charset = mainConfiguration.getValue(MainConfiguration.GENERAL, "charset");
		return new OutputStreamWriter(new FileOutputStream(path), charset.get());
	}

	public void setWebViewChartsEngine(WebEngine webViewChartsEngine) {
		this.webViewChartsEngine = webViewChartsEngine;
	}

	public WebEngine getWebViewChartsEngine() {
		return webViewChartsEngine;
	}

	public CheckComboBox<Group> getSlcGroup() {
		return checkComboBoxGroup;
	}

	public Controller getController() {
		return controller;
	}

	public MainController getMainController() {
		return mainController;
	}

	public SelectionUserController getSelectionUserController() {
		return selectionUserController;
	}

	public static double getOpacity() {
		return OPACITY;
	}

	public boolean isUseOptions() {
		return useOptions;
	}

	public boolean isUseLogs() {
		return useLogs;
	}

	public MainConfiguration getMainConfiguration() {
		return mainConfiguration;
	}

	public void setMainConfiguration(MainConfiguration mainConfiguration) {
		this.mainConfiguration = mainConfiguration;
	}

	public Course getActualCourse() {
		return actualCourse;
	}

	public void setActualCourse(Course actualCourse) {
		this.actualCourse = actualCourse;
	}

	public <T> T getConfigValue(String name) {
		return mainConfiguration.getValue(this.chartType, name);
	}

	public <T> T getConfigValue(String name, T defaultValue) {
		return mainConfiguration.getValue(this.chartType, name, defaultValue);
	}

	public <T> T getGeneralConfigValue(String name) {
		return mainConfiguration.getValue(MainConfiguration.GENERAL, name);
	}

	public boolean getGeneralButtonlActive() {
		return getGeneralConfigValue("generalActive");
	}

	public boolean getGroupButtonActive() {
		return getGeneralConfigValue("groupActive");
	}

	/**
	 * @return the webView
	 */
	public WebView getWebView() {
		return webView;
	}

	/**
	 * @param webView the webView to set
	 */
	public void setWebView(WebView webView) {
		this.webView = webView;
	}

}
