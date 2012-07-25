package com.fave100.client.pagefragments;

import com.fave100.client.requestfactory.AppUserProxy;
import com.fave100.client.requestfactory.AppUserRequest;
import com.fave100.client.requestfactory.ApplicationRequestFactory;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.InlineHyperlink;

/**
 * Top navigation bar that will be included on every page.
 * @author yissachar.radcliffe
 *
 */
public class TopBarPresenter extends PresenterWidget<TopBarPresenter.MyView> {

	public interface MyView extends View {
		SpanElement getLogInLogOutLink();
		SpanElement getGreeting();
		InlineHyperlink getMyFave100Link();
	}
	
	private AppUserProxy appUser;
	private ApplicationRequestFactory requestFactory;
	
	@Inject
	public TopBarPresenter(final EventBus eventBus, final MyView view,
							final ApplicationRequestFactory requestFactory) {
		super(eventBus, view);
		this.requestFactory = requestFactory;
	}

	@Override
	protected void onBind() {
		super.onBind();	
	}
	
	@Override
	protected void onReveal() {
		super.onReveal();
		
		// Whenever the page is refreshed, check to see if the user is logged in or not
		// and change the top bar links and elements appropriately.
		AppUserRequest appUserRequest = requestFactory.appUserRequest();
		Request<AppUserProxy> getLoggedInAppUserReq = appUserRequest.getLoggedInAppUser();
		getLoggedInAppUserReq.fire(new Receiver<AppUserProxy>() {
			@Override
			public void onSuccess(AppUserProxy appUser) {
				if(appUser != null) {
					setAppUser(appUser);
					getView().getGreeting().setInnerHTML("Welcome "+appUser.getName());
					getView().getMyFave100Link().setVisible(true);
				} else {
					setAppUser(null);
					getView().getMyFave100Link().setVisible(false);					
				}
				// Create the login/logout URL as appropriate
				AppUserRequest appUserRequest = requestFactory.appUserRequest();				
				Request<String> loginURLReq = appUserRequest.getLoginLogoutURL(Window.Location.getPath()+
						Window.Location.getQueryString()+Window.Location.getHash());
				loginURLReq.fire(new Receiver<String>() {
					@Override
					public void onSuccess(String response) {
						String loginLogoutString = (getAppUser() != null) ? "Log out" : "Log in";
						getView().getLogInLogOutLink().setInnerHTML("<a href="+response+">"+loginLogoutString+"</a>");
					}			
				});
			}
		});
	}
	
	public AppUserProxy getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUserProxy appUser) {
		this.appUser = appUser;
	}
}
