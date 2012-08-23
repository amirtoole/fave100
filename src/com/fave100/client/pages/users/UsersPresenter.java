package com.fave100.client.pages.users;

import java.util.List;

import com.fave100.client.pagefragments.SideNotification;
import com.fave100.client.pagefragments.TopBarPresenter;
import com.fave100.client.place.NameTokens;
import com.fave100.client.requestfactory.AppUserProxy;
import com.fave100.client.requestfactory.AppUserRequest;
import com.fave100.client.requestfactory.ApplicationRequestFactory;
import com.fave100.client.requestfactory.SongProxy;
import com.fave100.server.domain.FaveList;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.UiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

public class UsersPresenter extends
		Presenter<UsersPresenter.MyView, UsersPresenter.MyProxy> 
		implements UsersUiHandlers{
	
	

	public interface MyView extends View, HasUiHandlers<UsersUiHandlers> {		
		/*HTMLPanel getUserProfile();
		Image getAvatar();
		SpanElement getUsernameSpan();		
		Button getFollowButton();
		NonpersonalFaveList getUserFaveList();
		Anchor getFave100TabLink();
		Anchor getActivityTabLink();
		InlineHTML getActivityTab();*/
		
		void setFollowed();
		void setUnfollowed();
		void showFave100Tab();
		void showActivityTab(SafeHtml html);
		void setUserProfile(AppUserProxy user);
		void setUserFaveList(List<SongProxy> faveList);
	}
		
	@ContentSlot public static final Type<RevealContentHandler<?>> TOP_BAR_SLOT = new Type<RevealContentHandler<?>>();
	public static final String FAVE_100_TAB = "fave100";
	public static final String ACTIVITY_TAB = "activity";
	
	@Inject TopBarPresenter topBar;
	
	private String requestedUsername;
	private final ApplicationRequestFactory requestFactory;
	private final PlaceManager placeManager;

	@ProxyCodeSplit
	@NameToken(NameTokens.users)
	public interface MyProxy extends ProxyPlace<UsersPresenter> {
	}

	@Inject
	public UsersPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, final ApplicationRequestFactory requestFactory,
			final PlaceManager placeManager) {
		super(eventBus, view, proxy);
		this.requestFactory = requestFactory;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	@Override
	protected void revealInParent() {
		RevealRootContentEvent.fire(this, this);
	}
	
	@Override
	public boolean useManualReveal() {
		return true;
	}
	
	@Override
	public void prepareFromRequest(final PlaceRequest placeRequest) {
		super.prepareFromRequest(placeRequest);
		requestedUsername = placeRequest.getParameter("u", "");	
		if(requestedUsername.equals("")) {
			placeManager.revealDefaultPlace();
		} else {
			// Update follow button
			final Request<Boolean> checkFollowing = requestFactory.appUserRequest().checkFollowing(requestedUsername);
			checkFollowing.fire(new Receiver<Boolean>() {
				@Override
				public void onSuccess(final Boolean following) {
					if(following) {
						getView().setFollowed();
					} else {
						getView().setUnfollowed();
					}
				}
			});
			
			// Update user profile
		    final Request<AppUserProxy> userReq = requestFactory.appUserRequest().findAppUser(requestedUsername);			
		    userReq.fire(new Receiver<AppUserProxy>() {
		    	@Override
		    	public void onSuccess(final AppUserProxy user) {
		    		if(user != null) {	    				
		    			// Upate user profile
	    				getView().setUserProfile(user);
	    			} else {
	    				placeManager.revealDefaultPlace();
	    			}
		    		getProxy().manualReveal(UsersPresenter.this);
		    	}
		    });		
		    
		    // Update fave list
			final Request<List<SongProxy>> userFaveListReq = requestFactory.faveListRequest().getFaveList(requestedUsername, FaveList.DEFAULT_HASHTAG);
		    userFaveListReq.fire(new Receiver<List<SongProxy>>() {
		    	@Override
		    	public void onSuccess(final List<SongProxy> faveList) {
		    		if(faveList != null) {	 
	    	    		getView().setUserFaveList(faveList);
	    			} else {
	    				placeManager.revealDefaultPlace();
	    			}
		    		getProxy().manualReveal(UsersPresenter.this);
		    	}
		    });	
		    
		    final String tab = placeRequest.getParameter("tab", UsersPresenter.FAVE_100_TAB);
		    if(tab.equals(UsersPresenter.ACTIVITY_TAB)) {
		    	
				final Request<List<String>> getActivityReq = requestFactory.appUserRequest().getActivityForUser(requestedUsername);
				getActivityReq.fire(new Receiver<List<String>>() {
					@Override
					public void onSuccess(final List<String> activityList) {
						final SafeHtmlBuilder builder = new SafeHtmlBuilder();
						builder.appendHtmlConstant("<ul>");
						for(final String activity : activityList) {
							builder.appendHtmlConstant("<li>");
							builder.appendEscaped(activity);
							builder.appendHtmlConstant("</li>");
						}
						builder.appendHtmlConstant("</ul>");
						getView().showActivityTab(builder.toSafeHtml());
					}
				});
		    } else if(tab.equals(UsersPresenter.FAVE_100_TAB)) {
		    	getView().showFave100Tab();
		    }
		}
	}
	
	@Override
	protected void onReveal() {
	    super.onReveal();
	    setInSlot(TOP_BAR_SLOT, topBar);
	    // TODO: handle user visiting own page	    
	}

	@Override
	public void follow() {
		final AppUserRequest appUserRequest = requestFactory.appUserRequest();
		final Request<Void> followReq = appUserRequest.followUser(requestedUsername);
		followReq.fire(new Receiver<Void>() {
			@Override
			public void onSuccess(final Void response) {
				SideNotification.show("Following!");
				getView().setFollowed();
			}
			@Override
			public void onFailure(final ServerFailure failure) {
				SideNotification.show(failure.getMessage().replace("Server Error:", ""), true);
			}
		});
	}

	@Override
	public void goToFave100Tab() {
		placeManager.revealPlace(new PlaceRequest(NameTokens.users).with("u", requestedUsername).with("tab", UsersPresenter.FAVE_100_TAB));
	}

	@Override
	public void goToActivityTab() {
		placeManager.revealPlace(new PlaceRequest(NameTokens.users).with("u", requestedUsername).with("tab", UsersPresenter.ACTIVITY_TAB));
	}
}


interface UsersUiHandlers extends UiHandlers{
	void follow();
	void goToFave100Tab();
	void goToActivityTab();
}
