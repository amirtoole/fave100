package com.fave100.client.pages.myfave100;

import java.util.HashMap;

import com.fave100.client.requestfactory.ApplicationRequestFactory;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

/**
 * SuggestBox that pulls its suggestions from iTunes Search API.
 * @author yissachar.radcliffe
 *
 */
public class SongSuggestBox extends SuggestBox{
	
	private MusicSuggestionOracle suggestions;
	private HashMap<String, SuggestionResult> itemSuggestionMap;
	private Timer suggestionsTimer;
		
	public SongSuggestBox(MusicSuggestionOracle suggestions, ApplicationRequestFactory requestFactory) {
		super(suggestions);
		this.suggestions = suggestions;
		this.setLimit(4);
		itemSuggestionMap = new HashMap<String, SuggestionResult>();	
		
		suggestionsTimer = new Timer() {
			public void run() {
				getAutocompleteList();
			}
		};
		
		addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {	
				//To restrict amount of queries, don't bother searching unless more than 200ms have passed
				//since the last keystroke.		
				suggestionsTimer.cancel();
				// don't search if it was just an arrow key being pressed
				if(!KeyCodeEvent.isArrow(event.getNativeKeyCode()) && event.getNativeKeyCode() != KeyCodes.KEY_ENTER) {
					suggestionsTimer.schedule(200);
				}
			}
		});
	}
	
	public interface ListResultFactory extends AutoBeanFactory {		
		AutoBean<ListResultOfSuggestion> result();		
	}

	private void getAutocompleteList() {	
		// Build a JSONP request to grab the info from iTunes
		String url = "http://itunes.apple.com/search?"+
						"term="+this.getValue()+
						"&media=music"+
						"&entity=song"+
						"&attribute=songTerm"+
						"&limit=5";
		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
		jsonp.requestObject(url, new AsyncCallback<JavaScriptObject>() {			
	       	public void onSuccess(JavaScriptObject jsObject) {	       		
	       		// Turn the resulting JavaScriptObject into an AutoBean
	       		JSONObject obj = new JSONObject(jsObject);
	       		ListResultFactory factory = GWT.create(ListResultFactory.class);
	       		AutoBean<ListResultOfSuggestion> autoBean = AutoBeanCodex.decode(factory, ListResultOfSuggestion.class, obj.toString());	       		
	       		ListResultOfSuggestion listResult = autoBean.as();
	       		
	       		// Clear the current suggestions)	       		
	       		suggestions.clear();
	       		itemSuggestionMap.clear();
	       		
	       		// Get the new suggestions from the iTunes API       		
	       		for (SuggestionResult entry : listResult.getResults()) {
	    	    	String suggestionEntry = entry.getTrackName()+"<br/><span class='artistName'>"+entry.getArtistName()+"</span>";	  
	    	    	itemSuggestionMap.put(suggestionEntry, entry);
	    	    }
	       		suggestions.addAll(itemSuggestionMap.keySet());
	    	    showSuggestionList();
	    	    
	       	}

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub				
			}
		});		
	}
	
	// Returns SuggestionResults mapped from the display string passed in
	public SuggestionResult getFromSuggestionMap(String key) {
		return itemSuggestionMap.get(key);
	}
}