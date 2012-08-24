package com.fave100.client.pages.myfave100;

import static com.google.gwt.query.client.GQuery.$;

import java.util.ArrayList;
import java.util.List;

import com.fave100.client.requestfactory.ApplicationRequestFactory;
import com.fave100.client.requestfactory.FaveListRequest;
import com.fave100.client.requestfactory.SongProxy;
import com.fave100.client.widgets.FaveListBase;
import com.fave100.client.widgets.MouseClickCell;
import com.fave100.server.domain.FaveList;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;

public class PersonalFaveList extends FaveListBase {
	
	private Element draggedRow;
	private ApplicationRequestFactory requestFactory;
	private final List<SongProxy> clientFaveList = new ArrayList<SongProxy>();
	
	public PersonalFaveList(final ApplicationRequestFactory requestFactory) {
		super(requestFactory);
		
		this.requestFactory = requestFactory;
		
		_cells.add(0, new HasCell<SongProxy, String>() {
			MouseDownCell cell = new MouseDownCell(){
				@Override
				public void onBrowserEvent(final Context context, final Element parent, final String value,
					final NativeEvent event, final ValueUpdater<String> valueUpdater) {	
					if(value == null) return;		
					super.onBrowserEvent(context, parent, value, event, valueUpdater);					
					if(event.getType().equals("mousedown")) {	
						draggedRow = parent.getParentElement();
						final GQuery $row = $(draggedRow);
						addStyleName("unselectable");						
						
						// Add a hidden row to act as a placeholder while the real row is moved					
						$row.clone().css("visibility", "hidden").addClass("clonedHiddenRow").insertBefore($row);
						$row.addClass("draggedFaveListItem");
						
						setPos($row, event.getClientY()+Window.getScrollTop());
						
						$("body").mousemove(new Function() {
							@Override
							public boolean f(final Event event) {
								// Set the dragged row position to be equal to mouseY						
								final GQuery $draggedFaveListItem = $(".draggedFaveListItem");
								setPos($draggedFaveListItem, event.getClientY()+Window.getScrollTop());
								
								final int draggedTop = $draggedFaveListItem.offset().top;
								final int draggedBottom = draggedTop + $draggedFaveListItem.outerHeight(true);
								// Check if dragged row collides with row above or row below								
								final GQuery $clonedHiddenRow = $(".clonedHiddenRow");
								GQuery $previous = $clonedHiddenRow.prev();
								GQuery $next = $clonedHiddenRow.next();
								final int prevHeight = $previous.outerHeight(true);
								final int nextHeight = $next.outerHeight(true);
								// Make sure we are not checking against the dragged row itself
								if($previous.hasClass("draggedFaveListItem")) $previous = $previous.prev();
								if($next.hasClass("draggedFaveListItem")) $next = $next.next();
								final int previousBottom = $previous.offset().top+$previous.outerHeight(true);
								// Move the hidden row to the appropriate position
								if(draggedTop < previousBottom-prevHeight/2) {
									$(".clonedHiddenRow").insertBefore($previous);
								} else if(draggedBottom > $next.offset().top+nextHeight/2){//$next.outerHeight(true)) {
									$(".clonedHiddenRow").insertAfter($next);
								}
								return true;
							}			
						});
					}
				}
			};

            @Override
            public Cell<String> getCell() {
                return cell;
            }

			@Override
			public FieldUpdater<SongProxy, String> getFieldUpdater() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getValue(final SongProxy object) {
				return clientFaveList.indexOf(object)+1+".";
			}
		});	
		
		// Delete button
		_cells.add(4, new HasCell<SongProxy, String>() {
			private final MouseClickCell cell = new MouseClickCell(){
				@Override
				public void onBrowserEvent(final Context context, final Element parent, final String value,
					final NativeEvent event, final ValueUpdater<String> valueUpdater) {	
					if(value == null) return;		
					super.onBrowserEvent(context, parent, value, event, valueUpdater);
					if(event.getType().equals("click")) {
						// Delete the Fave Item
				    	final FaveListRequest faveListRequest = requestFactory.faveListRequest();
				    	final Request<Void> deleteReq = faveListRequest.removeFaveItemForCurrentUser(FaveList.DEFAULT_HASHTAG, context.getIndex());
				    	deleteReq.fire(new Receiver<Void>() {
				    		@Override
				    		public void onSuccess(final Void response) {
				    			refreshList();									
				    		}								
				    	});
					}
				}
			};

            @Override
            public MouseClickCell getCell() {
                return cell;
            }

			@Override
			public FieldUpdater<SongProxy, String> getFieldUpdater() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getValue(final SongProxy object) {
				return "X";
			}			
		});
		
		_cells.add(6, new HasCell<SongProxy, String>() {
			private final EditTextCell cell = new EditTextCell(){
				// TODO: CSS method won't work in all browser: get the following to work!
				/*@Override
				public void render(final Context context, final String value, final SafeHtmlBuilder sb) {
					// Show some default text if there isn't a whyline
					if(value == null || value.isEmpty()) {
						super.render(context, "Click here to enter a whyline", sb);						
					} else {
						// Otherwise, just render the value
						super.render(context, value, sb);
					}
				}*/
			};
			
			private final FieldUpdater<SongProxy, String> fieldUpdater = new FieldUpdater<SongProxy, String>() {
				@Override
				public void update(final int index, final SongProxy object, final String value) {
					if(value.isEmpty()) {
						// TODO: Need to handle null whylines in the list
						//cell.clearViewData(_cellList.getKeyProvider().getKey(object));
						//_cellList.redraw();						
					} else {
						final Request<Void> editWhyline = requestFactory.faveListRequest().editWhylineForCurrentUser(FaveList.DEFAULT_HASHTAG, clientFaveList.indexOf(object), value);
						editWhyline.fire();
					}
				}
			};

			@Override
			public Cell<String> getCell() {
				return cell;
			}

			@Override
			public FieldUpdater<SongProxy, String> getFieldUpdater() {
				// TODO Auto-generated method stub
				return fieldUpdater;
			}

			@Override
			public String getValue(final SongProxy object) {
				String whyline = object.getWhyline();
				if(whyline == null || whyline.isEmpty()) {
					whyline = "";
				}
				return whyline;
			}
		});
		
		// Mouse up handler for change position
		RootPanel.get().addDomHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(final MouseUpEvent event) {
				// TODO: Switch over to plain GWT?
				if(draggedRow == null) return;
				final GQuery $draggedItem = $(".draggedFaveListItem").first();
				// Get the index of the row being dragged
				final int currentIndex = $draggedItem.parent().children().not(".clonedHiddenRow").index(draggedRow);
				// Insert the dragged row back into the table at the correct position
				$draggedItem.first().insertAfter($(".clonedHiddenRow"));
				// Get the new index
				final int newIndex = $draggedItem.parent().children().not(".clonedHiddenRow").index(draggedRow);						
				// Rank on the server
				if(currentIndex != newIndex) {
					// Don't bother doing anything if the indices are the same
					final FaveListRequest faveListRequest = requestFactory.faveListRequest();
		    	  	final Request<Void> rankReq = faveListRequest.rerankFaveItemForCurrentUser(FaveList.DEFAULT_HASHTAG, currentIndex, newIndex);
		    	  	rankReq.fire(new Receiver<Void>() {
		    	  		@Override
		    	  		public void onSuccess(final Void response) {
		    	  			refreshList();
		    	  		}
					});
				}    	 
				//remove all drag associated items now that we are done with the drag
	    	  	$draggedItem.removeClass("draggedFaveListItem");
				removeStyleName("unselectable");
				$(".clonedHiddenRow").remove();
				$("body").unbind("mousemove");
				draggedRow = null;	
			}
			
		}, MouseUpEvent.getType());
		
		createCellList("personalFaves");		
	}
	
	private void setPos(final GQuery element, final int mouseY) {	
		//Window.alert(mouseY+" "+$(element).top()+" "+(mouseY-$(element).top()));
		final int clonedHeight = $(".clonedHiddenRow").outerHeight(true);
		final int elementHeight = element.outerHeight(true);
		final int newPos = mouseY-elementHeight/2;
		final int draggedTop = newPos;
		final int draggedBottom = draggedTop + elementHeight;
		final int faveListTop = $(".faveList").offset().top;
		final int faveListBottom = faveListTop+$(".faveList").outerHeight(true)-clonedHeight;
		
		// If dragged row goes out of top or bottom bounds, stop it
		if(draggedTop <  faveListTop) {
			// Element is above the favelist, make it at the favelist height
			element.css("top", faveListTop+"px");
		} else if(draggedBottom > faveListBottom) {
			// Element is below the favelist, set it at favelist bottom
			element.css("top", faveListBottom-elementHeight+"px");
		} else {
			// Element is neither above or below faveList, position it correctly
			element.css("top", newPos+"px");
		}
	}
	
	public void refreshList() {
		//TODO: To reduce number of RPC calls, perhaps don't refresh list every change
		// instead, make changes locally on client by adding elements to DOM
		
		// Get the data from the datastore
		final FaveListRequest faveListRequest = requestFactory.faveListRequest();
		final Request<List<SongProxy>> currentUserReq = faveListRequest.getFaveItemsForCurrentUser(FaveList.DEFAULT_HASHTAG);
		currentUserReq.fire(new Receiver<List<SongProxy>>() {
			@Override
			public void onSuccess(final List<SongProxy> faveItems) {				
				if(faveItems != null) {
					clientFaveList.clear();
					clientFaveList.addAll(faveItems);
					setRowData(clientFaveList);
				}			
			}
		});
	}

}
