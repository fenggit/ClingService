package com.example.clingservice.modle;

import java.util.HashMap;

import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Container;


//store all FxContentNode to be shared
public class ContentTree {
	public final static String ROOT_ID = "0";
	public final static String VIDEO_ID = "1";
	public final static String AUDIO_ID = "2";
	public final static String IMAGE_ID = "3";
	public final static String VIDEO_PREFIX = "video-item-";
	public final static String AUDIO_PREFIX = "audio-item-";
	public final static String IMAGE_PREFIX = "image-item-";
	
	private static final String ROOT_CONTAINER_ID = "-1";
	private static final String ROOT_CONTAINER_TITLE = "MediaServer root directory";
	private static final String ROOT_CONTAINER_CREATOR = "PHICOMM";
	private static HashMap<String, ContentNode> mContentMap = new HashMap<String, ContentNode>();

	private static ContentNode mRootNode = createRootNode();

	public ContentTree() {};

	protected static ContentNode createRootNode() {
		// create root container
		Container root = new Container();
		root.setId(ROOT_ID);
		root.setParentID(ROOT_CONTAINER_ID);
		root.setTitle(ROOT_CONTAINER_TITLE);
		root.setCreator(ROOT_CONTAINER_CREATOR);
		root.setRestricted(true);
		root.setSearchable(true);
		root.setWriteStatus(WriteStatus.NOT_WRITABLE);
		root.setChildCount(0);
		ContentNode mRootNode = new ContentNode(ROOT_ID, root);
		mContentMap.put(ROOT_ID, mRootNode);
		return mRootNode;
	}
	
	public static ContentNode getRootNode() {
		return mRootNode;
	}
	
	public static ContentNode getNode(String id) {
		if( mContentMap.containsKey(id)) {
			return mContentMap.get(id);
		}
		return null;
	}
	
	public static boolean hasNode(String id) {
		return mContentMap.containsKey(id);
	}
	
	public static void addNode(String ID, ContentNode Node) {
		mContentMap.put(ID, Node);
	}
	
	public static void removeNode(String id) {
		mContentMap.remove(id);
	}
}
