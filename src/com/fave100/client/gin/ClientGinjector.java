package com.fave100.client.gin;

import com.google.gwt.inject.client.GinModules;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import com.fave100.client.gin.ClientModule;
import com.fave100.client.pages.home.HomePresenter;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.google.gwt.inject.client.AsyncProvider;
import com.fave100.client.pages.about.AboutPresenter;
import com.fave100.client.pages.myfave100.MyFave100Presenter;
import com.fave100.client.requestfactory.ApplicationRequestFactory;

@GinModules({ DispatchAsyncModule.class, ClientModule.class })
public interface ClientGinjector extends Ginjector {

	EventBus getEventBus();

	PlaceManager getPlaceManager();
	
	ApplicationRequestFactory getApplicationRequestFactory();

	AsyncProvider<HomePresenter> getHomePresenter();

	AsyncProvider<AboutPresenter> getAboutPresenter();

	AsyncProvider<MyFave100Presenter> getMyFave100Presenter();
}
