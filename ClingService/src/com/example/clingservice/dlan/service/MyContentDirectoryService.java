package com.example.clingservice.dlan.service;

import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import com.example.clingservice.modle.ContentNode;
import com.example.clingservice.modle.ContentTree;
import com.example.clingservice.util.LogManager;

public class MyContentDirectoryService extends AbstractContentDirectoryService {
	private final String NULL_STRING = "";

	@Override
	public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter,
			long firstResult, long maxResults, SortCriterion[] orderby)
			throws ContentDirectoryException {
		LogManager.e( "ContentDirectoryService---browse()--objectID is " + objectID);
		try {
			DIDLContent didl = new DIDLContent();
			ContentNode contentNode = ContentTree.getNode(objectID);			

			if (contentNode == null)
				return new BrowseResult(NULL_STRING, 0, 0);

			if (contentNode.isItem()) {
				didl.addItem(contentNode.getItem());
				return new BrowseResult(new DIDLParser().generate(didl), 1, 1); 
			} else {
				if (browseFlag == BrowseFlag.METADATA) {
					didl.addContainer(contentNode.getContainer());					
					return new BrowseResult(new DIDLParser().generate(didl), 1, 1);
				} else {
					for (Container container : contentNode.getContainer().getContainers()) {
						didl.addContainer(container);						
					}
					for (Item item : contentNode.getContainer().getItems()) {
						didl.addItem(item);						
					}
					return new BrowseResult(new DIDLParser().generate(didl),
							contentNode.getContainer().getChildCount(),
							contentNode.getContainer().getChildCount());
				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new ContentDirectoryException(
					ContentDirectoryErrorCode.CANNOT_PROCESS, ex.toString());
		}
	}

}
