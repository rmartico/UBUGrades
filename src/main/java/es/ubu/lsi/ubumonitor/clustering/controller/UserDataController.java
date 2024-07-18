package es.ubu.lsi.ubumonitor.clustering.controller;

import java.io.ByteArrayInputStream;
import java.util.Comparator;

import es.ubu.lsi.ubumonitor.clustering.data.Datum;
import es.ubu.lsi.ubumonitor.clustering.data.UserData;
import es.ubu.lsi.ubumonitor.AppInfo;
import es.ubu.lsi.ubumonitor.util.I18n;
import es.ubu.lsi.ubumonitor.model.EnrolledUser;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controllador de la ventana de los datos de un usuario.
 * 
 * @author Xing Long Ji
 *
 */
public class UserDataController {

	@FXML
	private Label labelUser;

	@FXML
	private ImageView imageView;

	@FXML
	private TableView<Datum> tableView;

	@FXML
	private TableColumn<Datum, String> columnType;

	@FXML
	private TableColumn<Datum, ImageView> columnIcon;

	@FXML
	private TableColumn<Datum, String> columnItem;

	@FXML
	private TableColumn<Datum, Number> columnValue;

	private TableView<UserData> table;

	/**
	 * Inicializa el controllador.
	 * 
	 * @param userData datos del usuario
	 * @param table    TableView
	 */
	public void init(UserData userData, TableView<UserData> table) {
		loadUser(userData);
		this.table = table;
	}

	private void loadUser(UserData userData) {
		EnrolledUser enrolledUser = userData.getEnrolledUser();
		labelUser.setText(enrolledUser.getFullName());
		imageView.setImage(new Image(new ByteArrayInputStream(enrolledUser.getImageBytes())));
		tableView.setItems(FXCollections.observableList(userData.getData()));
		columnType.setCellValueFactory(e -> new SimpleStringProperty(I18n.get(e.getValue().getType())));
		columnItem.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getItem()));
		columnValue.setCellValueFactory(e -> new SimpleObjectProperty<Number>(e.getValue().getValue()));
		columnValue.setComparator(Comparator.comparing(Number::doubleValue));

		columnIcon.setCellValueFactory(e -> {
			ImageView image;
			try {
				image = new ImageView(AppInfo.IMG_DIR + e.getValue().getIconFile() + ".png");
			} catch (Exception ex) {
				image = new ImageView("/img/manual.png");
			}
			return new SimpleObjectProperty<>(image);

		});
	}

	private void change(int index) {
		UserData user = table.getItems().get(index);
		table.getSelectionModel().clearAndSelect(index);
		table.scrollTo(index);
		loadUser(user);
	}

	/**
	 * Visualiza el siguiente usuario.
	 */
	public void loadNext() {
		int actual = table.getSelectionModel().getSelectedIndex();
		int next = (actual + 1) % table.getItems().size();
		change(next);
	}

	/**
	 * Visualiza el anterior usuario.
	 */
	public void loadPrevious() {
		int actual = table.getSelectionModel().getSelectedIndex();
		int next = actual == 0 ? table.getItems().size() - 1 : actual - 1;
		change(next);
	}

}
