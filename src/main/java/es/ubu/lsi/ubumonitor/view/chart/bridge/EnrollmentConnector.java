package es.ubu.lsi.ubumonitor.view.chart.bridge;

import java.util.Collection;

import es.ubu.lsi.ubumonitor.controllers.Controller;
import es.ubu.lsi.ubumonitor.controllers.MainController;
import es.ubu.lsi.ubumonitor.controllers.configuration.MainConfiguration;
import es.ubu.lsi.ubumonitor.model.Course;
import es.ubu.lsi.ubumonitor.view.chart.ChartType;
import es.ubu.lsi.ubumonitor.view.chart.Tabs;
import es.ubu.lsi.ubumonitor.view.chart.enrollment.CourseEnrollmentNetwork;
import es.ubu.lsi.ubumonitor.view.chart.enrollment.EnrollmentBar;
import es.ubu.lsi.ubumonitor.view.chart.enrollment.EnrollmentSankey;
import javafx.scene.web.WebView;

public class EnrollmentConnector extends JavaConnectorAbstract {

	public EnrollmentConnector(WebView webView, MainConfiguration mainConfiguration, MainController mainController,
			Course actualCourse) {
		super(webView, mainConfiguration, mainController, actualCourse);
		Collection<Course> allCourses = Controller.getInstance().getDataBase().getCourses().getMap().values();
		addChart(new EnrollmentBar(mainController, allCourses));
		addChart(new EnrollmentSankey(mainController, allCourses));
		addChart(new CourseEnrollmentNetwork(mainController, allCourses));
		currentChart = charts.get(ChartType.getDefault(Tabs.ENROLLMENT));
	}

}
