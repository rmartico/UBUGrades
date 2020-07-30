package es.ubu.lsi.ubumonitor.controllers.load;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import es.ubu.lsi.ubumonitor.model.DataBase;
import es.ubu.lsi.ubumonitor.model.DescriptionFormat;
import es.ubu.lsi.ubumonitor.model.DiscussionPost;
import es.ubu.lsi.ubumonitor.model.ForumDiscussion;
import es.ubu.lsi.ubumonitor.util.UtilMethods;
import es.ubu.lsi.ubumonitor.webservice.api.mod.forum.ModForumGetForumDiscussionPosts;
import es.ubu.lsi.ubumonitor.webservice.api.mod.forum.ModForumGetForumDiscussions;
import es.ubu.lsi.ubumonitor.webservice.webservices.WebService;

public class PopulateForum {

	private DataBase dataBase;
	private WebService webService;
	private static final ModForumGetForumDiscussions MOD_FORUM_GET_FORUM_DISCUSSIONS = new ModForumGetForumDiscussions(
			0);
	private static final ModForumGetForumDiscussionPosts MOD_FORUM_GET_FORUM_DISCUSSION_POSTS = new ModForumGetForumDiscussionPosts(
			0);

	public PopulateForum(DataBase dataBase, WebService webService) {
		this.dataBase = dataBase;
		this.webService = webService;
	}

	public List<ForumDiscussion> populateForumDiscussions(Collection<Integer> forumids) {
		List<ForumDiscussion> forumDiscussions = new ArrayList<>();
		for (int forumid : forumids) {

			forumDiscussions.addAll(populateForumDiscussion(forumid));

		}
		return forumDiscussions;
	}

	public List<ForumDiscussion> populateForumDiscussion(int forumid) {
		try {
			MOD_FORUM_GET_FORUM_DISCUSSIONS.setForumid(forumid);
			JSONObject jsonObject = UtilMethods.getJSONObjectResponse(webService, MOD_FORUM_GET_FORUM_DISCUSSIONS);
			return populateForumDiscussions(jsonObject);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	public List<ForumDiscussion> populateForumDiscussions(JSONObject jsonObject) {
		JSONArray jsonArray = jsonObject.getJSONArray(Constants.DISCUSSIONS);
		List<ForumDiscussion> forumDiscussions = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); ++i) {
			forumDiscussions.add(populateForumDiscussion(jsonArray.getJSONObject(i)));
		}
		return forumDiscussions;
	}

	public ForumDiscussion populateForumDiscussion(JSONObject jsonObject) {
		ForumDiscussion forumDiscussion = dataBase.getForumDiscussions()
				.getById(jsonObject.getInt(Constants.DISCUSSION));
		forumDiscussion.setName(jsonObject.optString(Constants.NAME));
		forumDiscussion.setTimemodified(Instant.ofEpochSecond(jsonObject.optLong(Constants.TIMEMODIFIED)));
		forumDiscussion.setUsermodified(dataBase.getUsers()
				.getById(jsonObject.optInt(Constants.USERMODIFIED)));
		forumDiscussion.setTimestart(Instant.ofEpochSecond(jsonObject.optLong(Constants.TIMESTART)));
		forumDiscussion.setTimeend(Instant.ofEpochSecond(jsonObject.optLong(Constants.TIMEEND)));
		forumDiscussion.setUser(dataBase.getUsers()
				.getById(jsonObject.optInt(Constants.USERID)));
		forumDiscussion.setCreated(Instant.ofEpochMilli(jsonObject.optLong(Constants.CREATED)));
		forumDiscussion.setModified(Instant.ofEpochMilli(jsonObject.optLong(Constants.MODIFIED)));
		forumDiscussion.setNumreplies(jsonObject.optInt(Constants.NUMREPLIES));
		forumDiscussion.setPinned(jsonObject.optBoolean(Constants.PINNED));
		forumDiscussion.setLocked(jsonObject.optBoolean(Constants.LOCKED));
		forumDiscussion.setStarred(jsonObject.optBoolean(Constants.STARRED));
		forumDiscussion.setCanreply(jsonObject.optBoolean(Constants.CANREPLY));
		forumDiscussion.setCanlock(jsonObject.optBoolean(Constants.CANLOCK));
		forumDiscussion.setCanfavourite(jsonObject.optBoolean(Constants.CANFAVOURITE));

		return forumDiscussion;
	}

	public List<DiscussionPost> populateDiscussionPosts(Collection<Integer> discussionids) {
		List<DiscussionPost> discussionPosts = new ArrayList<>();
		for (int discussionid : discussionids) {
			discussionPosts.addAll(populateDiscussionPosts(discussionid));
		}
		return discussionPosts;
	}

	public List<DiscussionPost> populateDiscussionPosts(int discussionid) {
		try {
			MOD_FORUM_GET_FORUM_DISCUSSION_POSTS.setDiscussionid(discussionid);
			JSONObject jsonObject = UtilMethods.getJSONObjectResponse(webService, MOD_FORUM_GET_FORUM_DISCUSSIONS);
			return populateDiscussionPosts(jsonObject);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	public List<DiscussionPost> populateDiscussionPosts(JSONObject jsonObject) {
		List<DiscussionPost> discussionPosts = new ArrayList<>();
		JSONArray jsonArray = jsonObject.getJSONArray(Constants.POSTS);
		for (int i = 0; i < jsonArray.length(); ++i) {
			discussionPosts.add(populateDiscussionPost(jsonArray.getJSONObject(i)));
		}
		 return discussionPosts;
	}

	public DiscussionPost populateDiscussionPost(JSONObject jsonObject) {
		DiscussionPost discussionPost = dataBase.getDiscussionPosts().getById(jsonObject.getInt(Constants.ID));
		discussionPost.setDiscussion(dataBase.getForumDiscussions().getById(jsonObject.optInt(Constants.DISCUSSION)));
		int parent = jsonObject.optInt(Constants.PARENT);
		if(parent!= 0) {
			discussionPost.setParent(dataBase.getDiscussionPosts().getById(parent));
		}
		discussionPost.setUser(dataBase.getUsers().getById(jsonObject.optInt(Constants.USERID)));
		discussionPost.setCreated(Instant.ofEpochSecond(jsonObject.optLong(Constants.CREATED)));
		discussionPost.setModified(Instant.ofEpochMilli(jsonObject.optLong(Constants.MODIFIED)));
		discussionPost.setMailed(jsonObject.optInt(Constants.MAILED) == 1);
		discussionPost.setSubject(jsonObject.optString(Constants.SUBJECT));
		discussionPost.setMessage(jsonObject.optString(Constants.MESSAGE));
		discussionPost.setMessageformat(DescriptionFormat.get(jsonObject.optInt(Constants.MESSAGEFORMAT)));
		discussionPost.setMessagetrust(jsonObject.optInt(Constants.MESSAGETRUST) == 1);
		discussionPost.setAttachment(jsonObject.optString(Constants.ATTACHMENT));
		discussionPost.setTotalscore(jsonObject.optInt(Constants.TOTALSCORE));
		discussionPost.setMailnow(jsonObject.optInt(Constants.MAILNOW) ==1);
		discussionPost.setCanreply(jsonObject.optBoolean(Constants.CANREPLY));
		discussionPost.setDeleted(jsonObject.optBoolean(Constants.DELETED));
		discussionPost.setIsprivatereply(jsonObject.optBoolean(Constants.ISPRIVATEREPLY));
		
		return discussionPost;
		
	}

}
