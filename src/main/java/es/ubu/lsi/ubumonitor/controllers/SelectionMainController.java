package es.ubu.lsi.ubumonitor.controllers;

import javafx.fxml.FXML;

public class SelectionMainController {
	
	@FXML
	private SelectionController selectionController;
	
	@FXML
	private SelectionForumController selectionForumController;

	public void init(MainController mainController) {
		selectionController.init(mainController);
		selectionForumController.init(mainController);
	}
	
	public SelectionController getSelectionController() {
		return selectionController;
	}
	
	public SelectionForumController getSelectionForumController() {
		return selectionForumController;
	}
	
}
