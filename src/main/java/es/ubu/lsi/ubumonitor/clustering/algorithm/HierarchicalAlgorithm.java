package es.ubu.lsi.ubumonitor.clustering.algorithm;

import java.util.List;
import java.util.stream.Collectors;

import es.ubu.lsi.ubumonitor.clustering.controller.collector.DataCollector;
import es.ubu.lsi.ubumonitor.clustering.data.LinkageMeasure;
import es.ubu.lsi.ubumonitor.clustering.data.UserData;
import es.ubu.lsi.ubumonitor.model.EnrolledUser;
import smile.clustering.HierarchicalClustering;
import smile.clustering.linkage.Linkage;
import smile.math.distance.Distance;

/**
 * Algoritmo del clustering jerárquico.
 * 
 * @author Xing Long Ji
 *
 */
public class HierarchicalAlgorithm {

	private LinkageMeasure linkageMeasure;
	private Distance<double[]> distance;
	private List<UserData> usersData;

	/**
	 * Ejecuta el algoritmo con los datos pasados por parámetro.
	 * 
	 * @param enrolledUsers  lista de usuarios
	 * @param dataCollectors lista de recolectores de datos
	 * @return HierarchicalClustering resultante
	 */
	public HierarchicalClustering execute(List<EnrolledUser> enrolledUsers, List<DataCollector> dataCollectors) {
		usersData = enrolledUsers.stream().map(UserData::new).collect(Collectors.toList());
		dataCollectors.forEach(collector -> collector.collect(usersData));

		if (usersData.size() < 2)
			throw new IllegalStateException("clustering.error.notUsers");

		if (usersData.get(0).getData().isEmpty())
			throw new IllegalStateException("clustering.error.notData");

		double[][] data = usersData.stream().map(UserData::getPoint).toArray(double[][]::new);
		Linkage linkage = linkageMeasure.of(data, distance);
		return HierarchicalClustering.fit(linkage);
	}

	/**
	 * Establece la medida de distancia entre clusters.
	 * 
	 * @param linkageMeasure a establecer
	 */
	public void setLinkageMeasure(LinkageMeasure linkageMeasure) {
		this.linkageMeasure = linkageMeasure;
	}

	/**
	 * Establece la medida de distancia entre elementos.
	 * 
	 * @param distance a establecer
	 */
	public void setDistance(Distance<double[]> distance) {
		this.distance = distance;
	}

	/**
	 * Devuelve la lista de usuarios con datos.
	 * 
	 * @return lista de UserData
	 */
	public List<UserData> getUsersData() {
		return usersData;
	}

}
