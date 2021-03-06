package model;

import java.io.Serializable;

/**
 * Clase Activity. Implementar en el futuro.
 * 
 * @author Claudia Mart�nez Herrero
 * @version 1.0
 *
 */
public class Activity implements Serializable {

	private static final long serialVersionUID = 1L;
	private String itemName;
	private String activityType;
	private float weight;
	private String minRange;
	private String maxRange;
	private float contributionCourseTotal;

	/**
	 * Constructor de una actividad con todos sus par�metros
	 * 
	 * @param itemName
	 *            nombre de la actividad
	 * @param type
	 *            tipo de actividad
	 * @param weight
	 *            peso
	 * @param minRange
	 *            rango m�nimo de nota
	 * @param maxRange
	 *            rango m�ximo de nota
	 */
	public Activity(String itemName, String type, float weight, String minRange, String maxRange) {
		this.itemName = itemName;
		this.activityType = type;
		this.weight = weight;
		this.minRange = minRange;
		this.maxRange = maxRange;
	}

	/**
	 * Devuelve el nombre de la actividad
	 * 
	 * @return nombre de la actividad
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * Modifica el nombre de la actividad
	 * 
	 * @param itemName
	 * 		nombre del item
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	/**
	 * Devuelve el tipo de la actividad
	 * 
	 * @return tipo de actividad
	 */
	public String getActivityType() {
		return activityType;
	}

	/**
	 * Modifica el tipo de la actividad
	 * 
	 * @param activityType
	 * 		tipo de actividad
	 */
	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	/**
	 * Devuelve el peso de la actividad
	 * 
	 * @return peso
	 */
	public float getWeight() {
		return weight;
	}

	/**
	 * Modifica el peso de la actividad
	 * 
	 * @param weight
	 * 		el peso
	 */
	public void setWeight(float weight) {
		this.weight = weight;
	}

	/**
	 * Devuelve el rango m�nimo
	 * 
	 * @return minRange
	 */
	public String getMinRange() {
		return minRange;
	}

	/**
	 * Modifica el rango m�nimo
	 * 
	 * @param minRange
	 * 		el rango minimo
	 */
	public void setMinRange(String minRange) {
		this.minRange = minRange;
	}

	/**
	 * Devuelve el rango m�ximo
	 * 
	 * @return maxRange
	 */
	public String getMaxRange() {
		return maxRange;
	}

	/**
	 * Modifica el rango m�ximo
	 * 
	 * @param maxRange
	 * 		el rango maximo
	 */
	public void setMaxRange(String maxRange) {
		this.maxRange = maxRange;
	}

	/**
	 * Devuelve la contribuci�n total de la actividad
	 * 
	 * @return contributionCourseTotal
	 */
	public float getContributionCourseTotal() {
		return contributionCourseTotal;
	}

	/**
	 * Modifica contribuci�n total de la actividad
	 * 
	 * @param contributionCourseTotal
	 * 		la contribuci�n de la actividad
	 */
	public void setContributionCourseTotal(float contributionCourseTotal) {
		this.contributionCourseTotal = contributionCourseTotal;
	}

}
