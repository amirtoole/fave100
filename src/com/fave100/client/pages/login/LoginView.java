package com.fave100.client.pages.login;

import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class LoginView extends ViewWithUiHandlers<LoginUiHandlers> 
	implements LoginPresenter.MyView {

	private final Widget widget;

	public interface Binder extends UiBinder<Widget, LoginView> {
	}

	@Inject
	public LoginView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@UiField HTMLPanel topBar;
	@UiField TextBox usernameInput;	
	@UiField PasswordTextBox passwordInput;
	@UiField SpanElement loginStatusMessage;
	@UiField Anchor signInWithGoogleButton;
	@UiField Anchor signInWithTwitterButton;
	@UiField Button loginButton;
	
	@Override
	public void setInSlot(final Object slot, final Widget content) {
		if(slot == LoginPresenter.TOP_BAR_SLOT) {
			topBar.clear();			
			if(content != null) {
				topBar.add(content);
			}
		}
		super.setInSlot(slot, content);
	}
	
	@UiHandler("loginButton")
	void onLogInButtonClick(final ClickEvent event) {
		getUiHandlers().login();
	}
	
	@Override
	public void setError(final String error) {
		loginStatusMessage.setInnerText(error);
	}

	@Override
	public void clearUsername() {
		usernameInput.setValue("");
	}

	@Override
	public void clearPassword() {
		passwordInput.setValue("");
		loginStatusMessage.setInnerText("");
	}

	@Override
	public String getUsername() {
		return usernameInput.getValue();
	}

	@Override
	public String getPassword() {
		return passwordInput.getValue();
	}

	@Override
	public void setGoogleLoginUrl(final String url) {
		signInWithGoogleButton.setHref(url);		
	}

	@Override
	public void setTwitterLoginUrl(final String url) {
		signInWithTwitterButton.setHref(url);		
	}
}
