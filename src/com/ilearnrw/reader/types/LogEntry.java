package com.ilearnrw.reader.types;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import com.google.gson.annotations.SerializedName;

/**
 * @author David Johansson
 * 
 *         DataLogger Log object.
 * 
 *         As specified in D6.1_ILearnRW_System_Architecture_final.
 * 
 * 
 * 
 */
public class LogEntry implements Serializable {

	public LogEntry(String username, String applicationId, Timestamp timestamp,
			String tag, String word, int problemCategory, int problemIndex,
			float duration, String level, String mode, String value, String supervisor) {

		this.username = username;
		this.applicationId = applicationId;
		this.tag = tag;
		this.word = word;
		this.problemCategory = problemCategory;
		this.problemIndex = problemIndex;
		this.duration = duration;
		this.level = level;
		this.mode = mode;
		this.value = value;
		this.timestamp = timestamp;
		this.supervisor = supervisor;
	}
	
	public LogEntry(String username, String applicationId, Timestamp timestamp,
			String tag, String value) {

		this.username = username;
		this.applicationId = applicationId;
		this.tag = tag;
		this.value = value;
		this.timestamp = timestamp;
	}
	
	/**
	 * We need the default constructor in order to serialize FROM json to this
	 * object.
	 */
	public LogEntry() {
	}

	private static final long serialVersionUID = -7085054143765991532L;

	/**
	 * The user-id for the user that the particular log concerns.
	 */
	@SerializedName("username")
	private String username;

	/**
	 * Tags for the LogEntry.
	 */
	@SerializedName("tag")
	private String tag;

	/**
	 * A value stored with the log.
	 * 
	 */
	@SerializedName("value")
	private String value;

	/**
	 * An application identifier.
	 */
	@SerializedName("applicationId")
	private String applicationId;

	@SerializedName("timestamp")
	private Timestamp timestamp;

	@SerializedName("word")
	private String word;

	@SerializedName("problem_category")
	private int problemCategory;

	@SerializedName("problem_index")
	private int problemIndex;

	@SerializedName("duration")
	private float duration;

	@SerializedName("level")
	private String level;

	@SerializedName("mode")
	private String mode;

	@SerializedName("supervisor")
	private String supervisor;
	

	public String getUsername() {
		return username;
	}

	public String getTag() {
		if (tag == null)
			return tag;
		
		return tag.toUpperCase(Locale.getDefault());
	}

	public String getValue() {
		return value;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public Timestamp getTimestamp() {
		if (timestamp != null)
			return timestamp;

		// we return the server's time if not set by application
		return new Timestamp(new Date().getTime());
	}

	public String getWord() {
		return word;
	}

	public int getProblemCategory() {
		return problemCategory;
	}

	public int getProblemIndex() {
		return problemIndex;
	}

	public float getDuration() {
		return duration;
	}

	public String getLevel() {
		return level;
	}

	public String getMode() {
		return mode;
	}

	public String getSupervisor() {
		return supervisor;
	}
	@Override
	public String toString() {
		return "LogEntry [username=" + username + ", tag=" + tag + ", value="
				+ value + ", applicationId=" + applicationId + ", timestamp="
				+ timestamp + ", word=" + word + ", problemCategory="
				+ problemCategory + ", problemIndex=" + problemIndex
				+ ", duration=" + duration + ", level=" + level + ", mode="
				+ mode + "]";
	}
}
