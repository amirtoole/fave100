package com.fave100.client.pages.lists.widgets.listmanager.widgets.autocomplete;

import java.util.List;

import com.fave100.shared.Constants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class AddListAutocompleteView extends ViewWithUiHandlers<AddListAutocompleteUiHandlers> implements AddListAutocompletePresenter.MyView {
	public interface Binder extends UiBinder<HTMLPanel, AddListAutocompleteView> {
	}

	interface ListAutocompleteStyle extends CssResource {
		String selected();
	}

	@UiField TextBox searchBox;
	@UiField FlowPanel resultsPanel;
	@UiField Label noResultsMsg;
	@UiField ListAutocompleteStyle style;

	@Inject
	AddListAutocompleteView(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
		searchBox.getElement().setAttribute("placeholder", "Add a list...");
		searchBox.getElement().setAttribute("maxlength", Integer.toString(Constants.MAX_HASHTAG_LENGTH));
	}

	@UiHandler("searchBox")
	void onKeyUp(final KeyUpEvent event) {
		// If arrow key pressed
		if (KeyUpEvent.isArrow(event.getNativeKeyCode())) {
			// If down or up arrow, adjust list selection accordingly
			if (event.isDownArrow()) {
				getUiHandlers().setSelection(1, true);
			}
			else if (event.isUpArrow()) {
				getUiHandlers().setSelection(-1, true);
			}
		}
		else if (KeyCodes.KEY_ENTER == event.getNativeKeyCode()) {
			// Enter key pressed, add list
			getUiHandlers().listSelected();
		}
		else if (KeyCodes.KEY_ESCAPE == event.getNativeKeyCode()) {
			// Escape key, cancel search
			getUiHandlers().getAutocompleteResults("");
		}
		else {
			// Otherwise search for song
			getUiHandlers().getAutocompleteResults(searchBox.getText());
		}
	}

	@Override
	public void setSuggestions(final List<String> suggestions) {
		resultsPanel.clear();
		resultsPanel.setVisible(true);
		noResultsMsg.setVisible(false);
		if (suggestions == null || suggestions.size() == 0) {
			resultsPanel.setVisible(false);
			if (suggestions != null)
				noResultsMsg.setVisible(true);
			return;
		}

		for (final String suggestion : suggestions) {
			final Label listName = new Label(suggestion);
			listName.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(final MouseOverEvent event) {
					getUiHandlers().setSelection(resultsPanel.getWidgetIndex(listName), false);
				}
			});
			listName.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(final ClickEvent event) {
					getUiHandlers().listSelected();
				}
			});
			resultsPanel.add(listName);
		}
	}

	@Override
	public void setSelection(final int selection) {
		for (int i = 0; i < resultsPanel.getWidgetCount(); i++) {
			resultsPanel.getWidget(i).getElement().removeClassName(style.selected());
		}
		if (selection >= 0 && resultsPanel.getWidgetCount() > 0)
			resultsPanel.getWidget(selection).getElement().addClassName(style.selected());
	}

	@Override
	public void clearSearch() {
		searchBox.setText("");
		getUiHandlers().getAutocompleteResults("");
	}

	@Override
	public void setFocus(boolean focus) {
		searchBox.setFocus(focus);
	}
}
