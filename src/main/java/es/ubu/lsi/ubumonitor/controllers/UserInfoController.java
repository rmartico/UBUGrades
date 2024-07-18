package es.ubu.lsi.ubumonitor.controllers;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Collectors;

import es.ubu.lsi.ubumonitor.model.Course;
import es.ubu.lsi.ubumonitor.model.EnrolledUser;
import es.ubu.lsi.ubumonitor.model.Group;
import es.ubu.lsi.ubumonitor.model.LastActivityFactory;
import es.ubu.lsi.ubumonitor.model.Role;
import es.ubu.lsi.ubumonitor.util.UtilMethods;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class UserInfoController {
	
	private MainController mainController;
	private EnrolledUser actualEnrolledUser;

	@FXML
	private ImageView imageView;
	@FXML
	private Label labelUser;
	@FXML
	private Hyperlink hyperlinkEmail;
	@FXML
	private Label labelFirstAccess;
	@FXML
	private Label labelLastAccess;
	@FXML
	private Label labelLastCourseAccess;
	@FXML
	private Label labelRoles;
	@FXML
	private Label labelGroups;
	@FXML
	private Label labelNcourses;

	@FXML
	private TableColumn<Course, String> coursesColumn;

	@FXML
	private TableView<Course> tableView;

	public void init(MainController mainController, EnrolledUser enrolledUser) {
		this.mainController = mainController;
		setUser(enrolledUser);
	}

	public void previousUser() {
		ObservableList<EnrolledUser> list = mainController.getSelectionUserController()
				.getListParticipants()
				.getItems();
		int index = list.indexOf(actualEnrolledUser);
		EnrolledUser previousUser = index == 0 ? list.get(list.size() - 1) : list.get(--index);
		setUser(previousUser);
	}

	public void nextUser() {
		ObservableList<EnrolledUser> list = mainController.getSelectionUserController()
				.getListParticipants()
				.getItems();

		int index = list.indexOf(actualEnrolledUser);

		EnrolledUser previousUser = list.get(++index % list.size());

		setUser(previousUser);
	}
	public void firstUser() {
		setUser(mainController.getSelectionUserController()
				.getListParticipants().getItems().get(0));
	}
	
	public void lastUser() {
		setUser(mainController.getSelectionUserController()
				.getListParticipants().getItems().get(mainController.getSelectionUserController()
						.getListParticipants().getItems().size()-1));
	}

	public void setUser(EnrolledUser enrolledUser) {
		actualEnrolledUser = enrolledUser;
		try {
			imageView.setImage(new Image(new ByteArrayInputStream(enrolledUser.getImageBytes())));
		} catch (Exception e) {
			imageView.setImage(SelectionUserController.DEFAULT_IMAGE);
		}
		labelUser.setText(enrolledUser.toString());
		if(enrolledUser.getEmail()!=null) {
			hyperlinkEmail.setText(enrolledUser.getEmail());
			hyperlinkEmail.setOnAction(e -> UtilMethods.mailTo(enrolledUser.getEmail()));
		}

		Instant reference = Controller.getInstance()
				.getUpdatedCourseData()
				.toInstant();
		labelFirstAccess.setText(UtilMethods.getDifferenceTime(enrolledUser.getFirstaccess(), reference));
		labelLastAccess.setText(UtilMethods.getDifferenceTime(enrolledUser.getLastaccess(), reference));
		Circle circleLastAccees = new Circle(10);
		circleLastAccees.setFill(LastActivityFactory.DEFAULT.getColorActivity(enrolledUser.getLastaccess(), reference));
		labelLastAccess.setGraphic(circleLastAccees);

		labelLastCourseAccess.setText(UtilMethods.getDifferenceTime(enrolledUser.getLastcourseaccess(), reference));
		Circle circle = new Circle(10);
		circle.setFill(LastActivityFactory.DEFAULT.getColorActivity(enrolledUser.getLastcourseaccess(), reference));
		labelLastCourseAccess.setGraphic(circle);
		labelRoles.setText(Controller.getInstance()
				.getActualCourse()
				.getRoles()
				.stream()
				.filter(r -> r.contains(enrolledUser))
				.map(Role::getRoleName)
				.collect(Collectors.joining(", ")));
		labelGroups.setText(Controller.getInstance()
				.getActualCourse()
				.getGroups()
				.stream()
				.filter(r -> r.contains(enrolledUser))
				.map(Group::getGroupName)
				.collect(Collectors.joining(", ")));

		ObservableList<Course> courses = Controller.getInstance()
				.getDataBase()
				.getCourses()
				.getMap()
				.values()
				.stream()
				.filter(c -> c.contains(enrolledUser))
				.collect(Collectors.toCollection(FXCollections::observableArrayList));
		courses.sort(Comparator.comparing(Course::getId, Comparator.reverseOrder()));

		coursesColumn.setCellValueFactory(v -> new SimpleStringProperty(v.getValue()
				.getFullName()));
		coursesColumn.setComparator(String::compareToIgnoreCase);
		coursesColumn.setCellFactory(tc -> {
		    TableCell<Course, String> cell = new TableCell<>();
		    Text text = new Text();
		    cell.setGraphic(text);
		    cell.setPrefHeight(Region.USE_COMPUTED_SIZE);
		    text.wrappingWidthProperty().bind(coursesColumn.widthProperty());
		    text.textProperty().bind(cell.itemProperty());
		    return cell ;
		});
		tableView.setItems(courses);
		
		labelNcourses.setText(String.valueOf(courses.size()));

	}

	
}
