package com.fave100.client.pages.users;

import java.util.List;

import com.fave100.client.pages.BasePresenter;
import com.fave100.client.requestfactory.AppUserProxy;
import com.fave100.client.requestfactory.ApplicationRequestFactory;
import com.fave100.client.requestfactory.SongProxy;
import com.fave100.client.widgets.favelist.NonpersonalFaveList;
import com.fave100.client.widgets.favelist.PersonalFaveList;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class UsersView extends ViewWithUiHandlers<UsersUiHandlers>
	implements UsersPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, UsersView> {
	}

	@UiField(provided = true) NonpersonalFaveList userFaveList;
	@UiField(provided = true) SuggestBox songSuggestBox;
	@UiField(provided = true) PersonalFaveList personalFaveList;
	@UiField HTMLPanel faveListContainer;
	@UiField InlineHyperlink editProfileButton;
	//@UiField Button followButton;
	@UiField Image avatar;
	@UiField SpanElement username;
	@UiField InlineHyperlink advancedSearchLink;
	@UiField HTMLPanel songSearchContainer;

	@Inject
	public UsersView(final Binder binder, final ApplicationRequestFactory requestFactory) {
		final MusicSuggestionOracle suggestions = new MusicSuggestionOracle();
		userFaveList = new NonpersonalFaveList(requestFactory);
		songSuggestBox = new SongSuggestBox(suggestions, requestFactory);
		personalFaveList = new PersonalFaveList(requestFactory);
		widget = binder.createAndBindUi(this);
		songSuggestBox.getElement().setAttribute("placeholder", "Add a song...");
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@UiField HTMLPanel topBar;
	@UiField HTMLPanel faveFeed;

	@Override
	public void setInSlot(final Object slot, final Widget content) {
		if(slot == BasePresenter.TOP_BAR_SLOT) {
			topBar.clear();
			if(content != null) {
				topBar.add(content);
			}
		}
		if(slot == UsersPresenter.FAVE_FEED_SLOT) {
			faveFeed.clear();
			if(content != null) {
				faveFeed.add(content);
			}
		}
		super.setInSlot(slot, content);
	}

	/*@UiHandler("followButton")
	void onFollowButtonClicked(final ClickEvent event) {
		getUiHandlers().follow();
	}*/

	@UiHandler("songSuggestBox")
	void onItemSelected(final SelectionEvent<Suggestion> event) {
		// Look up the selected song in the song map and add it to user fave list
		getUiHandlers().addSong(((SongSuggestBox) songSuggestBox).getFromSuggestionMap(event.getSelectedItem().getReplacementString()));
		songSuggestBox.setValue("");
	}

	/*@Override
	public void setFollowed() {
		followButton.setHTML("Following");
		followButton.setEnabled(false);
	}

	@Override
	public void setUnfollowed() {
		followButton.setHTML("Follow");
		followButton.setEnabled(true);
	}*/

	@Override
	public void setUserProfile(final AppUserProxy user) {
		avatar.setUrl(user.getAvatarImage());
		username.setInnerText(user.getUsername());
	}

	@Override
	public void setUserFaveList(final List<SongProxy> faveList) {
		userFaveList.setRowData(faveList);
	}

	@Override
	public void refreshPersonalFaveList() {
		personalFaveList.refreshList();
	}

	@Override
	public void showOwnPage() {
		personalFaveList.setVisible(true);
		userFaveList.setVisible(false);
		songSuggestBox.setVisible(true);
		advancedSearchLink.setVisible(true);
		faveFeed.setVisible(true);
		editProfileButton.setVisible(true);
		//followButton.setVisible(false);
		refreshPersonalFaveList();
	}

	@Override
	public void showOtherPage() {
		personalFaveList.setVisible(false);
		userFaveList.setVisible(true);
		songSuggestBox.setVisible(false);
		advancedSearchLink.setVisible(false);
		editProfileButton.setVisible(false);
		//followButton.setVisible(true);
		faveFeed.setVisible(false);
	}

}
