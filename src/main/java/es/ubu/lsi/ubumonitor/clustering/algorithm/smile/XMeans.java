package es.ubu.lsi.ubumonitor.clustering.algorithm.smile;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ml.clustering.Clusterer;

import es.ubu.lsi.ubumonitor.clustering.algorithm.Algorithm;
import es.ubu.lsi.ubumonitor.clustering.data.ClusteringParameter;
import es.ubu.lsi.ubumonitor.clustering.data.UserData;
import smile.clustering.CentroidClustering;

/**
 * Algoritmo X-Means de Smile.
 * 
 * @author Xing Long Ji
 *
 */
public class XMeans extends Algorithm {

	private static final String NAME = "X-Means";
	private static final String LIBRARY = "Smile";

	/**
	 * Constructor del algoritmo X-Means.
	 */
	public XMeans() {
		super(NAME, LIBRARY);
		addParameter(ClusteringParameter.MAX_NUM_CLUSTER, 4);
		addParameter(ClusteringParameter.MAX_ITERATIONS, 50);
		addParameter(ClusteringParameter.TOLERANCE, 0.001);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Clusterer<UserData> getClusterer() {
		int kmax = getParameters().getValue(ClusteringParameter.MAX_NUM_CLUSTER);
		int max = getParameters().getValue(ClusteringParameter.MAX_ITERATIONS);
		double tol = getParameters().getValue(ClusteringParameter.TOLERANCE);

		checkParameter(ClusteringParameter.MAX_NUM_CLUSTER, kmax);
		checkParameter(ClusteringParameter.MAX_ITERATIONS, max);
		checkParameter(ClusteringParameter.TOLERANCE, tol);

		return new XMeansAdapter(kmax, max, tol);
	}

	private class XMeansAdapter extends SmileAdapter {

		private int kmax;
		private int maxIter;
		private double tol;

		private XMeansAdapter(int kmax, int maxIter, double tol) {
			this.kmax = kmax;
			this.maxIter = maxIter;
			this.tol = tol;
		}

		@Override
		protected CentroidClustering<double[], double[]> execute(double[][] data) {
			if (data.length < kmax)
				throw new NumberIsTooSmallException(data.length, kmax, true);
			return smile.clustering.XMeans.fit(data, kmax, maxIter, tol);
		}
	}
}
