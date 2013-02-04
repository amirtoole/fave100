package com.fave100.client.pages.song;

import com.fave100.client.Notification;
import com.fave100.client.pages.BasePresenter;
import com.fave100.client.pages.BaseView;
import com.fave100.client.place.NameTokens;
import com.fave100.client.requestfactory.ApplicationRequestFactory;
import com.fave100.client.requestfactory.FaveListRequest;
import com.fave100.client.requestfactory.SongProxy;
import com.fave100.server.domain.favelist.FaveList;
import com.fave100.shared.exceptions.favelist.SongAlreadyInListException;
import com.fave100.shared.exceptions.favelist.SongLimitReachedException;
import com.fave100.shared.exceptions.user.NotLoggedInException;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.UiHandlers;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

/**
 * A song page that will display details about the song
 *
 * @author yissachar.radcliffe
 *
 */
public class SongPresenter extends
		BasePresenter<SongPresenter.MyView, SongPresenter.MyProxy> implements
		SongUiHandlers {

	public interface MyView extends BaseView, HasUiHandlers<SongUiHandlers> {
		void setSongInfo(SongProxy song);

		void setYouTubeVideos(YouTubeSearchListJSON videos);
	}

	@ProxyCodeSplit
	@NameToken(NameTokens.song)
	public interface MyProxy extends ProxyPlace<SongPresenter> {
	}

	private final ApplicationRequestFactory	requestFactory;
	private final PlaceManager				placeManager;
	private SongProxy						songProxy;

	@Inject
	public SongPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy,
			final ApplicationRequestFactory requestFactory,
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

		// Use parameters to determine what to reveal on page
		final String song = URL.decode(placeRequest.getParameter("song", ""));
		final String artist = URL.decode(placeRequest
				.getParameter("artist", ""));
		if (song.isEmpty() || artist.isEmpty()) {
			// Malformed request, send the user away
			placeManager.revealDefaultPlace();
		} else {
			// Load the song from the datastore
			final Request<SongProxy> getSongReq = requestFactory.songRequest()
					.findSongByTitleAndArtist(song, artist);
			getSongReq.fire(new Receiver<SongProxy>() {
				@Override
				public void onSuccess(final SongProxy song) {
					songProxy = song;
					getView().setSongInfo(song);
					getProxy().manualReveal(SongPresenter.this);
				}
			});

			// Get any YouTube videos
			final Request<String> getYoutubeReq = requestFactory.songRequest()
					.getYouTubeResults(song, artist);
			getYoutubeReq.fire(new Receiver<String>() {
				@Override
				public void onSuccess(final String json) {
					final YouTubeSearchListJSON youTubeResults = JsonUtils
							.safeEval(json);
					getView().setYouTubeVideos(youTubeResults);
				}
			});
		}
	}

	@Override
	protected void onBind() {
		super.onBind();
	}

	@Override
	public void addSong() {

		// Make sure we actually have a song to work with
		if (songProxy == null)
			return;

		// Add the song as a FaveItem
		final FaveListRequest faveListRequest = requestFactory
				.faveListRequest();
		final Request<Void> addReq = faveListRequest.addFaveItemForCurrentUser(
				FaveList.DEFAULT_HASHTAG, songProxy.getId(),
				songProxy.getTitle(), songProxy.getArtist());

		addReq.fire(new Receiver<Void>() {
			@Override
			public void onSuccess(final Void response) {
				Notification.show("Added");
			}

			@Override
			public void onFailure(final ServerFailure failure) {
				// Alert the user if adding the song fails for any reason
				if (failure.getExceptionType().equals(
						NotLoggedInException.class.getName())) {
					placeManager
							.revealPlace(new PlaceRequest(NameTokens.login));
				} else if (failure.getExceptionType().equals(
						SongLimitReachedException.class.getName())) {
					Notification
							.show("You cannot have more than 100 songs in list");
				} else if (failure.getExceptionType().equals(
						SongAlreadyInListException.class.getName())) {
					Notification.show("The song is already in your list");
				}
			}
		});

	}
}

interface SongUiHandlers extends UiHandlers {
	void addSong();
}
