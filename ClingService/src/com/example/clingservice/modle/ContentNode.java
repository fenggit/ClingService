package com.example.clingservice.modle;

import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

//store Container or Item
public class ContentNode {
		private Container mContainer;
		private Item mItem;
		private String mID;
		private String mFullPath;
		private boolean mIsItem;
		
		public ContentNode(String mID, Container mContainer) {
			this.mID = mID;
			this.mContainer = mContainer;
			this.mFullPath = null;
			this.mIsItem = false;
		}
		
		public ContentNode(String mID, Item mItem, String mFullPath) {
			this.mID = mID;
			this.mItem = mItem;
			this.mFullPath = mFullPath;
			this.mIsItem = true;
		}
		
		public String getId() {
			return mID;
		}
		
		public Container getContainer() {
			return mContainer;
		}
		
		public Item getItem() {
			return mItem;
		}
		
		public String getFullPath() {
			if (mIsItem && mFullPath != null) {
				return mFullPath;
			}
			return null;
		}
		
		public boolean isItem() {
			return mIsItem;
		}
}
