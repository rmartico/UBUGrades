package controllers.UBULogs.logCreator.logsTypes;

import java.util.List;

import model.Log;
/**
 * 
 * The user with id '' has created the discussion with id '' in the forum with course module id ''.
 * The user with id '' has viewed the discussion with id '' in the forum with course module id ''.
 * 
 * 
 * @author Yi Peng Ji
 *
 */
public class UserDiscussionCmid extends ReferencesLog{

	/**
	 * static Singleton instance.
	 */
	private static UserDiscussionCmid instance;

	/**
	 * Private constructor for singleton.
	 */
	private UserDiscussionCmid() {
	}

	/**
	 * Return a singleton instance of UserDiscussionCmid.
	 */
	public static UserDiscussionCmid getInstance() {
		if (instance == null) {
			instance = new UserDiscussionCmid();
		}
		return instance;
	}
	
	@Override
	public void setLogReferencesAttributes(Log log, List<Integer> ids) {
		// TODO Auto-generated method stub
		
	}

}
