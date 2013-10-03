package com.fave100.server.domain.favelist;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fave100.server.MemcacheManager;
import com.fave100.server.domain.DatastoreObject;
import com.fave100.server.domain.Song;
import com.fave100.server.domain.Whyline;
import com.fave100.server.domain.appuser.AppUser;
import com.fave100.shared.Constants;
import com.fave100.shared.Validator;
import com.fave100.shared.exceptions.ValidationException;
import com.fave100.shared.exceptions.favelist.BadWhylineException;
import com.fave100.shared.exceptions.favelist.FaveListAlreadyExistsException;
import com.fave100.shared.exceptions.favelist.SongAlreadyInListException;
import com.fave100.shared.exceptions.favelist.SongLimitReachedException;
import com.fave100.shared.exceptions.favelist.TooManyFaveListsException;
import com.fave100.shared.exceptions.user.NotLoggedInException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

@Entity
public class FaveList extends DatastoreObject {

	@IgnoreSave public static final String SEPERATOR_TOKEN = ":";
	@IgnoreSave public static final int MAX_FAVES = 100;

	@Id private String id;
	private Ref<AppUser> user;
	@Index private String hashtag;
	private List<FaveItem> list = new ArrayList<FaveItem>();

	@SuppressWarnings("unused")
	private FaveList() {
	}

	public FaveList(final String username, final String hashtag) {
		this.id = username.toLowerCase() + FaveList.SEPERATOR_TOKEN + hashtag.toLowerCase();
		this.user = Ref.create(Key.create(AppUser.class, username.toLowerCase()));
		this.hashtag = hashtag;
	}

	public static FaveList findFaveList(final String id) {
		return ofy().load().type(FaveList.class).id(id).get();
	}

	public static FaveList findFaveList(final String username, final String hashtag) {
		return findFaveList(username.toLowerCase() + FaveList.SEPERATOR_TOKEN + hashtag.toLowerCase());
	}

	public static void addFaveListForCurrentUser(final String hashtagName) throws Exception {
		final String error = Validator.validateHashtag(hashtagName);
		if (error != null) {
			throw new ValidationException(error);
		}

		final AppUser currentUser = AppUser.getLoggedInAppUser();
		if (currentUser == null)
			throw new NotLoggedInException();

		// -1 because #fave100 is a default list not stored in the hashtags list
		if (currentUser.getHashtags().size() >= Constants.MAX_LISTS_PER_USER - 1)
			throw new TooManyFaveListsException("You can't have more than " + Constants.MAX_LISTS_PER_USER + " lists");

		final String username = currentUser.getUsername();

		if (findFaveList(username, hashtagName) != null)
			throw new FaveListAlreadyExistsException("You already have a list with that name");

		currentUser.getHashtags().add(hashtagName);
		final FaveList faveList = new FaveList(username, hashtagName);
		// Transaction to ensure no duplicate hashtags created
		ofy().transact(new VoidWork() {
			@Override
			public void vrun() {
				Hashtag hashtag = ofy().load().type(Hashtag.class).id(hashtagName).get();
				// Hashtag already exists, add it to user's lists
				if (hashtag != null) {
					ofy().save().entities(currentUser, faveList).now();
				}
				// Create a new hashtag
				else {
					hashtag = new Hashtag(hashtagName, username);
					ofy().save().entities(currentUser, faveList, hashtag).now();
				}
			}
		});
	}

	public static void addFaveItemForCurrentUser(final String hashtag, final String songID)
			throws Exception {

		final AppUser currentUser = AppUser.getLoggedInAppUser();
		if (currentUser == null)
			throw new NotLoggedInException();

		final FaveList faveList = findFaveList(currentUser.getUsername(), hashtag);
		if (faveList.getList().size() >= FaveList.MAX_FAVES)
			throw new SongLimitReachedException();

		// Get the song from Lucene lookup
		final Song song = Song.findSong(songID);
		if (song == null)
			return;

		final FaveItem newFaveItem = new FaveItem(song.getSong(), song.getArtist(), song.getId());

		// Check if it is a unique song for this user
		boolean unique = true;
		for (final FaveItem faveItem : faveList.getList()) {
			if (faveItem.getSongID().equals(newFaveItem.getSongID())) {
				unique = false;
			}
		}

		if (unique == false)
			throw new SongAlreadyInListException();

		// Create the new FaveItem
		faveList.getList().add(newFaveItem);
		ofy().save().entities(faveList).now();

		// Modify memcache ranking
		MemcacheManager.getInstance().modifyFaveItemScore(songID, hashtag, FaveList.calculateItemScore(faveList.getList().size()), newFaveItem);
	}

	public static void removeFaveItemForCurrentUser(final String hashtag, final String songID) throws NotLoggedInException {
		final AppUser currentUser = AppUser.getLoggedInAppUser();
		if (currentUser == null)
			throw new NotLoggedInException();

		final FaveList faveList = findFaveList(currentUser.getUsername(), hashtag);
		if (faveList == null)
			return;
		// Find the song to remove
		int position = 0;
		FaveItem faveItemToRemove = null;
		for (final FaveItem faveItem : faveList.getList()) {
			if (faveItem.getSongID().equals(songID)) {
				faveItemToRemove = faveItem;
				break;
			}
			position++;
		}

		if (faveItemToRemove == null)
			return;

		// We must also delete the whyline if it exists
		final Ref<Whyline> currentWhyline = faveItemToRemove.getWhylineRef();
		if (currentWhyline != null) {
			ofy().delete().key(currentWhyline.getKey()).now();
		}
		faveList.getList().remove(faveItemToRemove);
		ofy().save().entities(faveList).now();

		// Modify memcache ranking
		MemcacheManager.getInstance().modifyFaveItemScore(songID, hashtag, FaveList.calculateItemScore(position), faveItemToRemove);
	}

	public static void rerankFaveItemForCurrentUser(final String hashtag, final String songID, final int newIndex) throws NotLoggedInException {

		final AppUser currentUser = AppUser.getLoggedInAppUser();
		if (currentUser == null)
			throw new NotLoggedInException();
		final FaveList faveList = findFaveList(currentUser.getUsername(), hashtag);
		if (faveList == null)
			return;

		// Make sure new index is valid
		if (newIndex < 0 || newIndex >= faveList.getList().size())
			throw new IllegalArgumentException("Index out of range");

		// Find the song to change position
		int oldIndex = 0;
		FaveItem faveItemToRerank = null;
		for (final FaveItem faveItem : faveList.getList()) {
			if (faveItem.getSongID().equals(songID)) {
				faveItemToRerank = faveItem;
				break;
			}
			oldIndex++;
		}

		if (faveItemToRerank == null)
			return;

		faveList.getList().remove(faveItemToRerank);
		faveList.getList().add(newIndex, faveItemToRerank);
		ofy().save().entities(faveList).now();

		MemcacheManager.getInstance().modifyFaveItemScore(songID, hashtag, (oldIndex - newIndex) * Constants.SCORE_CALCULATOR, faveItemToRerank);
	}

	public static void editWhylineForCurrentUser(final String hashtag, final String songID, final String whyline)
			throws NotLoggedInException, BadWhylineException {
		Objects.requireNonNull(hashtag);
		Objects.requireNonNull(songID);
		Objects.requireNonNull(whyline);

		// First check that the whyline is valid
		final String whylineError = Validator.validateWhyline(whyline);
		if (whylineError != null)
			throw new BadWhylineException(whylineError);

		final AppUser currentUser = AppUser.getLoggedInAppUser();
		if (currentUser == null)
			throw new NotLoggedInException();
		final FaveList faveList = findFaveList(currentUser.getUsername(), hashtag);
		Objects.requireNonNull(faveList);

		// Find the song to edit whyline
		FaveItem faveItemToEdit = null;
		for (final FaveItem faveItem : faveList.getList()) {
			if (faveItem.getSongID().equals(songID)) {
				faveItemToEdit = faveItem;
				break;
			}
		}

		Objects.requireNonNull(faveItemToEdit);

		// Set the denormalized whyline for the FaveItem
		faveItemToEdit.setWhyline(whyline);

		// Set the external Whyline
		final Ref<Whyline> currentWhyline = faveItemToEdit.getWhylineRef();
		if (currentWhyline == null) {
			// Create a new Whyline entity
			final Whyline whylineEntity = new Whyline(whyline, faveItemToEdit.getSongID(), currentUser.getUsername());
			ofy().save().entity(whylineEntity).now();
			faveItemToEdit.setWhylineRef(Ref.create(whylineEntity));
		}
		else {
			// Just modify the existing Whyline entity
			final Whyline whylineEntity = (Whyline)ofy().load().value(currentWhyline).get();
			whylineEntity.setWhyline(whyline);
			ofy().save().entity(whylineEntity).now();
		}

		ofy().save().entity(faveList).now();
	}

	public static List<FaveItem> getFaveList(final String username, final String hashtag) {
		final FaveList faveList = findFaveList(username, hashtag);
		if (faveList == null)
			return null;
		return faveList.getList();
	}

	public static List<FaveItem> getMasterFaveList(final String hashtag) {
		List<FaveItem> master = MemcacheManager.getInstance().getMasterFaveList(hashtag);
		// Go to the datastore if memcache doesn't have it
		if (master == null) {
			master = ofy().load().type(Hashtag.class).id(hashtag).get().getList();
		}
		return master;
	}

	public static List<String> getHashtagAutocomplete(final String searchTerm) {
		final List<String> names = new ArrayList<String>();
		if (searchTerm.isEmpty())
			return names;

		// TODO: Need to sort by popularity
		final List<Hashtag> hashtags = ofy().load().type(Hashtag.class).filter("id >=", searchTerm).filter("id <", searchTerm + "\uFFFD").limit(5).list();
		for (final Hashtag hashtag : hashtags) {
			names.add(hashtag.getId());
		}
		return names;
	}

	public static Hashtag getHashtag(final String id) {
		return ofy().load().type(Hashtag.class).id(id).get();
	}

	public static double calculateItemScore(final int position) {
		return ((double)(-1 * position) / 11 + (111 / 11));
	}

	public static List<String> getTrendingFaveLists() {
		List<Hashtag> hashtags = ofy().load().type(Hashtag.class).order("zscore").limit(5).list();
		List<String> trending = new ArrayList<>();
		for (Hashtag hashtag : hashtags) {
			trending.add(hashtag.getName());
		}
		return trending;
	}

	/* Getters and Setters */

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public Ref<AppUser> getUser() {
		return user;
	}

	public void setUser(final Ref<AppUser> user) {
		this.user = user;
	}

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(final String hashtag) {
		this.hashtag = hashtag;
	}

	public List<FaveItem> getList() {
		return list;
	}

}
