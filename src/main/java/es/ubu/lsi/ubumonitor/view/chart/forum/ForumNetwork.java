package es.ubu.lsi.ubumonitor.view.chart.forum;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;

import es.ubu.lsi.ubumonitor.controllers.MainController;
import es.ubu.lsi.ubumonitor.model.CourseModule;
import es.ubu.lsi.ubumonitor.model.DiscussionPost;
import es.ubu.lsi.ubumonitor.model.EnrolledUser;
import es.ubu.lsi.ubumonitor.util.I18n;
import es.ubu.lsi.ubumonitor.util.JSArray;
import es.ubu.lsi.ubumonitor.util.JSObject;
import es.ubu.lsi.ubumonitor.view.chart.ChartType;
import es.ubu.lsi.ubumonitor.view.chart.VisNetwork;
import javafx.scene.control.ListView;
import javafx.scene.web.WebView;

public class ForumNetwork extends VisNetwork {
	private static final Pattern INITIAL_LETTER_PATTERN = Pattern.compile("\\b\\w|,\\s");
	private ListView<CourseModule> listViewForum;

	public ForumNetwork(MainController mainController, WebView webView, ListView<CourseModule> listViewForum) {
		super(mainController, ChartType.FORUM_NETWORK, webView);
		this.listViewForum = listViewForum;
	}

	@Override
	public void exportCSV(String path) throws IOException {
		List<EnrolledUser> enrolledUsers = getSelectedEnrolledUser();
		List<DiscussionPost> discussionPosts = getSelectedDiscussionPosts();

		try (CSVPrinter printer = new CSVPrinter(getWritter(path),
				CSVFormat.DEFAULT.withHeader("fromId", "fromName", "toId", "toName", "countPostsReplies"))) {
			for (EnrolledUser from : enrolledUsers) {

				for (EnrolledUser to : enrolledUsers) {

					long countPosts = discussionPosts.stream()
							.filter(discussionPost -> from.equals(discussionPost.getUser())
									&& discussionPost.getParent() != null && to.equals(discussionPost.getParent()
											.getUser()))
							.count();
					if (countPosts > 0) {
						printer.printRecord(from.getId(), from.getFullName(), to.getId(), to.getFullName(), countPosts);
					}

				}

			}
		}

	}

	@Override
	public String getOptions(JSObject jsObject) {
		JSObject options = new JSObject();

		jsObject.put("physicsAfterDraw", getValue("physicsAfterDraw"));
		options.put("edges", getEdgesOptions());
		options.put("nodes", getNodesOptions());
		options.put("physics", getPhysicsOptions());
		options.put("interaction", getInteractionOptions());
		options.put("layout", getLayoutOptions());
		jsObject.put("options", options);

		return jsObject.toString();
	}

	private JSObject getEdgesOptions() {
		JSObject edges = new JSObject();
		edges.put("arrows", "{to:{enabled:true,scaleFactor:" + getValue("edges.arrows.to.scaleFactor") + "}}");
		edges.put("arrowStrikethrough", false);
		edges.put("dashes", getValue("edges.dashes"));
		JSObject scaling = new JSObject();
		scaling.put("max", getValue("edges.scaling.max"));
		scaling.put("min", getValue("edges.scaling.min"));
		edges.put("scaling", scaling);
		return edges;
	}

	private JSObject getNodesOptions() {
		JSObject nodes = new JSObject();
		if ((boolean) getValue("usePhoto")) {
			nodes.put("shape", "'circularImage'");
			nodes.put("brokenImage", "'../img/default_user.png'");
		} else {
			nodes.put("shape", "'circle'");
		}
		nodes.put("borderWidth", getValue("nodes.borderWidth"));
		JSObject scaling = new JSObject();
		scaling.put("max", getValue("nodes.scaling.max"));
		scaling.put("min", getValue("nodes.scaling.min"));
		nodes.put("scaling", scaling);
		return nodes;
	}

	private JSObject getPhysicsOptions() {
		JSObject physics = new JSObject();
		physics.put("enabled", true);
		Solver solver = getValue("physics.solver");
		physics.putWithQuote("solver", solver.getName());
		switch (solver) {
		case BARNES_HUT:
			JSObject barnesHut = new JSObject();
			barnesHut.put("theta", getValue("physics.barnesHut.theta"));
			barnesHut.put("gravitationalConstant", getValue("physics.barnesHut.gravitationalConstant"));
			barnesHut.put("centralGravity", getValue("physics.barnesHut.centralGravity"));
			barnesHut.put("springLength", getValue("physics.barnesHut.springLength"));
			barnesHut.put("springConstant", getValue("physics.barnesHut.springConstant"));
			barnesHut.put("damping", getValue("physics.barnesHut.damping"));
			barnesHut.put("avoidOverlap", getValue("physics.barnesHut.avoidOverlap"));
			physics.put("barnesHut", barnesHut);
			break;
		case FORCE_ATLAS_2_BASED:
			JSObject forceAtlas2Based = new JSObject();
			forceAtlas2Based.put("theta", getValue("physics.forceAtlas2Based.theta"));
			forceAtlas2Based.put("gravitationalConstant", getValue("physics.forceAtlas2Based.gravitationalConstant"));
			forceAtlas2Based.put("centralGravity", getValue("physics.forceAtlas2Based.centralGravity"));
			forceAtlas2Based.put("springLength", getValue("physics.forceAtlas2Based.springLength"));
			forceAtlas2Based.put("springConstant", getValue("physics.forceAtlas2Based.springConstant"));
			forceAtlas2Based.put("damping", getValue("physics.forceAtlas2Based.damping"));
			forceAtlas2Based.put("avoidOverlap", getValue("physics.forceAtlas2Based.avoidOverlap"));
			physics.put("forceAtlas2Based", forceAtlas2Based);
			break;
		case REPULSION:
			JSObject repulsion = new JSObject();
			repulsion.put("nodeDistance", getValue("physics.repulsion.nodeDistance"));
			repulsion.put("centralGravity", getValue("physics.repulsion.centralGravity"));
			repulsion.put("springLength", getValue("physics.repulsion.springLength"));
			repulsion.put("sprinConstant", getValue("physics.repulsion.springConstant"));
			repulsion.put("damping", getValue("physics.repulsion.damping"));
			physics.put("repulsion", repulsion);
			break;
		default:
			// default barneshut with default parameters
			break;

		}

		return physics;

	}

	private JSObject getInteractionOptions() {
		JSObject interaction = new JSObject();
		interaction.put("keyboard", getValue("interaction.keyboard"));
		interaction.put("multiselect", getValue("interaction.multiselect"));
		interaction.put("navigationButtons", getValue("interaction.navigationButtons"));
		interaction.put("tooltipDelay", getValue("interaction.tooltipDelay"));
		return interaction;
	}

	private JSObject getLayoutOptions() {
		JSObject layout = new JSObject();
		String randomSeed = getValue("layout.randomSeed");

		layout.put("randomSeed", StringUtils.isBlank(randomSeed) ? "undefined" : randomSeed);

		layout.put("clusterThreshold", getValue("layout.clusterThreshold"));
		return layout;
	}

	private <T> T getValue(String key) {
		return mainConfiguration.getValue(this.chartType, key);
	}

	@Override
	public void update() {
		List<EnrolledUser> users = getSelectedEnrolledUser();
		List<DiscussionPost> discussionPosts = getSelectedDiscussionPosts();
		JSObject data = new JSObject();

		JSArray nodes = new JSArray();

		JSArray edges = new JSArray();
		for (EnrolledUser from : users) {
			JSObject node = new JSObject();
			nodes.add(node);
			node.put("id", from.getId());
			node.putWithQuote("title", from.getFullName());
			node.put("color", rgb(from.getId() * 31));

			node.put("image", "'" + from.getImageBase64() + "'");

			long totalPosts = 0;
			for (EnrolledUser to : users) {
				long countPosts = discussionPosts.stream()
						.filter(discussionPost -> from.equals(discussionPost.getUser())
								&& to.equals(discussionPost.getParent()
										.getUser()))
						.count();

				if (countPosts > 0) {
					JSObject edge = new JSObject();
					edge.put("from", from.getId());
					edge.put("to", to.getId());
					edge.put("title", countPosts);
					edge.put("value", countPosts);
					edges.add(edge);
				}

				totalPosts += countPosts;

			}
			node.put("value", totalPosts);
			if (totalPosts > 0) {
				if ((boolean) getValue("useInitialNames")) {
					Matcher m = INITIAL_LETTER_PATTERN.matcher(from.getFullName());
					StringBuilder builder = new StringBuilder();
					while (m.find()) {
						builder.append(m.group());
					}
					node.put("label", "'" + builder + " (" + totalPosts + ")'");
				} else {
					node.put("label", "'" +totalPosts + "'");
				}

			}

		}

		data.put("nodes", nodes);
		data.put("edges", edges);
		webViewChartsEngine.executeScript("updateVisNetwork(" + data + "," + getOptions() + ")");
	}

	public List<DiscussionPost> getSelectedDiscussionPosts() {
		Set<CourseModule> selectedForums = new HashSet<>(listViewForum.getSelectionModel()
				.getSelectedItems());
		return actualCourse.getDiscussionPosts()
				.stream()
				.filter(discussionPost -> selectedForums.contains(discussionPost.getForum()))
				.collect(Collectors.toList());
	}

	public enum Solver {
		BARNES_HUT("barnesHut"), FORCE_ATLAS_2_BASED("forceAtlas2Based"), REPULSION("repulsion");

		private String name;

		private Solver(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return I18n.get(name());
		}
	}
}
