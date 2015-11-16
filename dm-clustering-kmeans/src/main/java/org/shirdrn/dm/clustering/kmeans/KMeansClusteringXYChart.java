package org.shirdrn.dm.clustering.kmeans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.ShapeUtilities;
import org.shirdrn.dm.clustering.common.ClusterPoint2D;
import org.shirdrn.dm.clustering.common.utils.FileUtils;
import org.shirdrn.dm.clustering.tool.common.ClusteringXYChart;
import org.shirdrn.dm.clustering.tool.utils.ChartUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Paint XY chart for cluster points generated by k-means clustering algorithm. 
 *
 * @author yanjun
 */
public class KMeansClusteringXYChart extends JFrame implements ClusteringXYChart {

	private static final long serialVersionUID = 1L;
	private String chartTitle;
	private File clusterPointFile;
	private File centroidPointFile;
	private final Map<Integer, Set<ClusterPoint2D>> clusterPoints = Maps.newHashMap();
	private final Map<Integer, Set<ClusterPoint2D>> centroidPoints = Maps.newHashMap();
	private final List<Color> colorSpace = Lists.newArrayList();
	private int centroidPointClusterId;
	private XYSeries centroidSeries;
	
	public KMeansClusteringXYChart(String chartTitle) throws HeadlessException {
		super();
		this.chartTitle = chartTitle;
	}
	
	private XYSeriesCollection buildXYDataset() {
		FileUtils.read2DPointsFromFile(clusterPoints, "[\t,;\\s]+", clusterPointFile);
		FileUtils.read2DPointsFromFile(centroidPoints, "[\t,;\\s]+", centroidPointFile);
		return ChartUtils.createXYSeriesCollection(clusterPoints);
	}
	
	private Set<ClusterPoint2D> getCentroidPoints() {
		Set<ClusterPoint2D> set = Sets.newHashSet();
		for(Set<ClusterPoint2D> values : centroidPoints.values()) {
			set.addAll(values);
		}
		return set;
	}

	@Override
	public void drawXYChart() {
		// create XY dataset from points file
		final XYSeriesCollection xyDataset = buildXYDataset();
		centroidSeries = new XYSeries(centroidPointClusterId);
		xyDataset.addSeries(centroidSeries);
		
		// create chart & configure XY plot
		JFreeChart jfreechart = ChartFactory.createScatterPlot(null, "X", "Y", xyDataset, PlotOrientation.VERTICAL, true, true, false);
		TextTitle title = new TextTitle(chartTitle, new Font("Lucida Sans Unicode", Font.BOLD, 14), 
				Color.DARK_GRAY, RectangleEdge.TOP, HorizontalAlignment.CENTER, 
				VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS);
		jfreechart.setTitle(title);
		
		XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
		
		// render clustered series
		final XYItemRenderer renderer = xyPlot.getRenderer();
		Map<Integer, Color> colors = ChartUtils.generateXYColors(clusterPoints.keySet(), colorSpace);
		Iterator<Entry<Integer, Color>> iter = colors.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer, Color> entry = iter.next();
			renderer.setSeriesPaint(entry.getKey(), entry.getValue());
		}
		
		// set centroid point display styles
		renderer.setSeriesPaint(centroidPointClusterId, Color.BLACK);
		renderer.setSeriesShape(centroidPointClusterId, ShapeUtilities.createRegularCross(50, 15));
		
		xyPlot.setDomainCrosshairVisible(true);
		xyPlot.setRangeCrosshairVisible(true);
        
		NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
		domain.setVerticalTickLabels(true);
		
		final ChartPanel chartPanel = new ChartPanel(jfreechart);
		this.add(chartPanel, BorderLayout.CENTER);
        
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, false));
        
		ChartUtils.createToggledButtons(panel, centroidSeries, getCentroidPoints(), "Display Centroids", "Hide Centroids");
        this.add(panel, BorderLayout.SOUTH);
	}
	
	public void setCentroidPointClusterId(int centroidPointClusterId) {
		this.centroidPointClusterId = centroidPointClusterId;
	}

	public void setCentroidPointFile(File centroidPointFile) {
		this.centroidPointFile = centroidPointFile;
	}
	
	@Override
	public void setclusterPointFile(File clusterPointFile) {
		this.clusterPointFile = clusterPointFile;
	}
	
	public static void main(String args[]) {
		int k = 3;
		
		String chartTitle = "K-means [k=" + k + "]";
		File dir = FileUtils.getDataRootDir();
		File centroidPointFile = new File(dir, "kmeans_3_centroids.txt");
		File clusterPointFile = new File(dir, "kmeans_3_cluster_points.txt");
		
		final KMeansClusteringXYChart chart = new KMeansClusteringXYChart(chartTitle);
		chart.setclusterPointFile(clusterPointFile);
		chart.setCentroidPointFile(centroidPointFile);
		chart.setCentroidPointClusterId(9999);
        ChartUtils.generateXYChart(chart);
    }

}
