package model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.UBUGrades;
import webservice.CourseWS;

/**
 * Clase GradeReportLine (GRL). Representa a una l�nea en el calificador de un
 * alumno. Cada l�nea se compone de su id, nombre, nivel, nota, porcentaje,
 * peso, rango y tipo principalmente.
 * 
 * @author Claudia Mart�nez Herrero
 * @version 1.0
 *
 */
public class GradeReportLine {
	
	static final Logger logger = LoggerFactory.getLogger(GradeReportLine.class);
	
	private int id;
	private String name;
	private int level;
	private String grade;
	private float percentage;
	private float weight;
	private String rangeMin;
	private String rangeMax;
	private boolean type; // False = Category, True = Item
	private String typeName;
	private ArrayList<GradeReportLine> children;
	private Activity activity;

	/**
	 * Contructor GRL con 4 par�metros. Le a�adimos solo los necesarios a la hora de generar el grafico de lineas.
	 * @param id
	 * 			id de la categor�a
	 * @param name
	 * 			nombre de la categoria
	 * @param grade
	 * 			la nota del elemento
	 * @param rangeMin
	 * 			rango minimo de la calificaci�n
	 * @param rangeMax
	 * 			rango maximo de la calificai�n
	 */
	public GradeReportLine(int id, String name, String grade, String rangeMin, String rangeMax) {
		this.id = id;
		this.name = name;
		this.grade = grade;
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
	}
	
	/**
	 * Constructor GRL con 4 par�metros. Se le a�aden a posteriori los elementos
	 * de su suma de calificaciones
	 * 
	 * @param id
	 *            id de la categor�a
	 * @param name
	 *            nombre de la categor�a
	 * @param level
	 *            nivel de profundidad
	 * @param type
	 *            categor�a o item
	 */
	public GradeReportLine(int id, String name, int level, boolean type) {
		this.id = id;
		this.name = name;
		this.level = level;
		this.type = type;
		this.children = new ArrayList<>();
	}

	/**
	 * Constructor de GRL con 7 par�metros.
	 * 
	 * @param id
	 *            id de la categor�a
	 * @param name
	 *            nombre de la categor�a
	 * @param level
	 *            nivel de profundidad
	 * @param type
	 *            categor�a o item
	 * @param weight
	 *            peso
	 * @param rangeMin
	 *            rango m�nimo de calificaci�n
	 * @param rangeMax
	 *            rango m�ximo de calificaci�n
	 */
	public GradeReportLine(int id, String name, int level, boolean type, float weight, String rangeMin, String rangeMax,
			String grade, float percentage, String nameType) {
		this.id = id;
		this.name = name;
		this.level = level;
		this.weight = weight;
		this.type = type;
		this.rangeMax = rangeMax;
		this.rangeMin = rangeMin;
		this.grade = grade;
		this.percentage = percentage;
		this.typeName = nameType;
		this.children = new ArrayList<>();
	}

	/**
	 * Devuelve el id del GradeReportLine
	 * 
	 * @return id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Modifica el id del GradeReportLine
	 * 
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Devuelve el nombre del GradeReportLine
	 * 
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Modifica el nombre del GradeReportLine
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Devuelve el nivel del GradeReportLine en el �rbol del calificador
	 * 
	 * @return level
	 */
	public int getLevel() {
		return this.level;
	}

	/**
	 * Modifica el nivel del GradeReportLine en el �rbol del calificador
	 * 
	 * @param level
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * Devuelve la nota del GradeReportLine
	 * 
	 * @return grade
	 */
	public String getGrade() {
		return grade;
	}
	
	/**
	 * Devuelve la nota del GradeReportLine ajustada al rango de 0 a 10
	 * 
	 * @return
	 * 		La nota ajustada.
	 */
	public String getGradeAdjustedTo10() {
		Float gradeAdjusted = CourseWS.getFloat(grade);
		if (!Float.isNaN(gradeAdjusted)) {
			Float newRangeMax = Float.parseFloat(rangeMax);
			Float newRangeMin = Float.parseFloat(rangeMin);
			
			newRangeMax -= newRangeMin;
			gradeAdjusted -= newRangeMin;
			
			gradeAdjusted = (float) ((gradeAdjusted*10.0) / newRangeMax); 	
		}
		return Float.toString(gradeAdjusted);
	}
	
	/**
	 * Devuelve la nota de una escala ajustada al rango de 0 a 10
	 * 
	 * @return
	 * 		La nota ajustada.
	 */
	public String getGradeWithScale() {
		Float gradeAdjusted;
		int scaleId = ((Assignment) activity).getScaleId();
		if (scaleId != 0 && !grade.equals("NaN")) {
			Scale scale = UBUGrades.session.getActualCourse().getScale(scaleId);
			gradeAdjusted = (float) scale.getElements().indexOf(this.grade);
			Float newRangeMax = (float) scale.getElements().size()-1;
			gradeAdjusted = (float) ((gradeAdjusted*10.0) / newRangeMax); 
		} else {
			return getGradeAdjustedTo10();
		}
		return Float.toString(gradeAdjusted);
	}
	
	/**
	 * Modifica la nota del GradeReportLine
	 * 
	 * @param grade
	 */
	public void setGrade(String grade) {
		this.grade = grade;
	}

	/**
	 * Devuelve el peso del GradeReportLine
	 * 
	 * @return weight
	 */
	public float getWeight() {
		return this.weight;
	}

	/**
	 * Modifica el peso del GradeReportLine
	 * 
	 * @param weight
	 */
	public void setWeight(float weight) {
		this.weight = weight;
	}

	/**
	 * Devuelve el rango m�ximo de nota
	 * 
	 * @return rangeMax
	 */
	// RMS changed
	public String getRangeMax() {
		return this.rangeMax;
	}

	/**
	 * Modifica el rango m�ximo de nota
	 * 
	 * @param rangeMax
	 */
	public void setRangeMax(String rangeMax) {
		this.rangeMax = rangeMax;
	}

	/**
	 * Devuelve el rango m�nimo de nota
	 * 
	 * @return rangeMin
	 */
	public String getRangeMin() {
		return this.rangeMin;
	}

	/**
	 * Modifica el rango m�nimo de nota
	 * 
	 * @param rangeMin
	 */
	public void setRangeMin(String rangeMin) {
		this.rangeMin = rangeMin;
	}

	/**
	 * Devuelve el porcentaje del GradeReportLine
	 * 
	 * @return percentage
	 */
	public float getPercentage() {
		return percentage;
	}

	/**
	 * Modifica el porcentaje del GradeReportLine
	 * 
	 * @param percentage
	 */
	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}

	/**
	 * Devuelve el tipo (boolean) de GradeReportLine
	 * 
	 * @return type
	 */
	public boolean getType() {
		return this.type;
	}

	/**
	 * Modifica el tipo (boolean) del GradeReportLine
	 * 
	 * @param type
	 */
	public void setType(boolean type) {
		this.type = type;
	}

	/**
	 * Devuelve el tipo de GradeReportLine
	 * 
	 * @return nameType
	 */
	public String getNameType() {
		return this.typeName;
	}

	/**
	 * Modifica el tipo de GradeReportLine
	 * 
	 * @param nameType
	 */
	public void setNameType(String nameType) {
		this.typeName = nameType;
	}

	/**
	 * Devuelve la actividad asociada al GradeReportLine
	 * 
	 * @return activity
	 */
	public Activity getActivity() {
		return this.activity;
	}

	/**
	 * Crea una actividad a partir del GradeReportLine
	 */
	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Devuelve los hijos que tiene el GradeReportLine
	 * 
	 * @return children
	 */
	public List<GradeReportLine> getChildren() {
		return this.children;
	}

	/**
	 * A�ade un hijo al GradeReportLine
	 * 
	 * @param kid
	 */
	public void addChild(GradeReportLine child) {
		this.children.add(child);
	}
	
	/**
	 * Convierte el GradeReportLine a un String con su nombre.
	 */
	public String toString() {
		return this.getName();
	}
}
