package com.fave100.client.pagefragments;

import com.fave100.client.place.NameTokens;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class TopBarView extends ViewImpl implements TopBarPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, TopBarView> {
	}
	
	@UiField InlineHyperlink logInLogOutLink;
	@UiField SpanElement greeting;	
	@UiField Anchor myFave100Link;	
	@UiField InlineHyperlink registerLink;

	@Inject
	public TopBarView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setLoggedIn(final String username) {
		greeting.setInnerHTML(username);
		myFave100Link.setVisible(true);
		myFave100Link.setHref(Window.Location.getPath()+Window.Location.getQueryString()+"#"+NameTokens.getUsers()+";u="+username);
		registerLink.setVisible(false);
		logInLogOutLink.setText("Log out");
		logInLogOutLink.setTargetHistoryToken(NameTokens.logout);
		
	}

	@Override
	public void setLoggedOut() {
		greeting.setInnerHTML("");
		myFave100Link.setVisible(false);		
		registerLink.setVisible(true);
		logInLogOutLink.setText("Log in");
		logInLogOutLink.setTargetHistoryToken(NameTokens.login);
		
	}
}
