package com.fave100.server.guice;

import com.fave100.server.domain.AppUser;
import com.fave100.server.domain.FaveItem;
import com.fave100.server.domain.GoogleID;
import com.fave100.server.domain.Song;
import com.googlecode.objectify.ObjectifyService;
import com.gwtplatform.dispatch.server.guice.HandlerModule;

public class ServerModule extends HandlerModule {

	static{
		ObjectifyService.register(FaveItem.class);
		ObjectifyService.register(AppUser.class);
		ObjectifyService.register(Song.class);
		ObjectifyService.register(GoogleID.class);
	}
	
	@Override
	protected void configureHandlers() {
	}
}
