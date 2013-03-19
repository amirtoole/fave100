package com.fave100.client.widgets.favelist;

import com.fave100.client.place.NameTokens;
import com.fave100.shared.requestfactory.ApplicationRequestFactory;
import com.fave100.shared.requestfactory.FaveItemProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;

public abstract class FaveListCellBase extends AbstractCell<FaveItemProxy>{

	protected ApplicationRequestFactory requestFactory;

	public FaveListCellBase(final ApplicationRequestFactory requestFactory) {
		super("click", "mousedown");
		this.requestFactory = requestFactory;
	}

	@Override
	public void render(final Context context, final FaveItemProxy object,
			final SafeHtmlBuilder sb) {
		if(object == null) return;

		sb.appendHtmlConstant(getRank(context));
		sb.appendHtmlConstant(getName(object));
		sb.appendHtmlConstant(getArtist(object));
		//sb.appendHtmlConstant(getReleaseDate(object));
		//sb.appendHtmlConstant(getImageThumb(object));
	}



	protected String getRank(final Context context) {
		final String rank = "<span class='faveListRank'>"+(context.getIndex()+1)+".</span>";
		return rank;
	}

	protected String getName(final FaveItemProxy object) {
		String songUrl = Window.Location.getPath();
		songUrl += Window.Location.getQueryString()+"#"+NameTokens.song+";id=";
		songUrl += URL.encodeQueryString(object.getSongID());
		songUrl = URL.encode(songUrl);

		String trackName = "<a href='"+songUrl.replace("'", "%27")+"'";
		trackName += "class='faveListTrackName'>"+object.getSong()+"</a>";
		return(trackName);
	}

	protected String getArtist(final FaveItemProxy object) {
		final String artistName = "<span class='faveListArtistName'>"+object.getArtist()+"</span>";
		return(artistName);
	}

	/*protected String getReleaseDate(final FaveItemProxy object) {
		String releaseDate = "<span class='faveListReleaseDate'>";
		final String dateVal = object.getReleaseDate();
		if(dateVal != null && !dateVal.isEmpty()) {
			releaseDate += object.getReleaseDate().substring(0, 4);
		}
	    releaseDate += "</span>";
	    return releaseDate;
	}*/

	/*protected String getImageThumb(final SongProxy object) {
		String imageThumb = "<img class='faveListImageThumb'";
		imageThumb += "src='"+object.getCoverArtUrl()+"'/>";
		return imageThumb;
	}*/
}

