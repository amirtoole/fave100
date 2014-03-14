package com.fave100.server.api;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fave100.server.domain.ApiPaths;
import com.fave100.server.domain.appuser.AppUser;
import com.fave100.server.domain.appuser.Following;
import com.fave100.server.domain.appuser.FollowingResult;
import com.fave100.server.domain.favelist.FaveItemCollection;
import com.fave100.server.domain.favelist.FaveList;
import com.fave100.server.domain.favelist.FaveListDao;
import com.fave100.server.exceptions.NotLoggedInException;
import com.fave100.shared.Constants;
import com.googlecode.objectify.Ref;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/" + ApiPaths.USERS_ROOT)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/" + ApiPaths.USERS_ROOT, description = "Operations on Users")
public class UsersApi {

	@GET
	@Path(ApiPaths.GET_USER)
	@ApiOperation(value = "Find a user by their username", response = AppUser.class)
	@ApiResponses(value = {@ApiResponse(code = 404, message = ApiExceptions.USER_NOT_FOUND)})
	public static AppUser getAppUser(@ApiParam(value = "The username", required = true) @PathParam("user") final String username) {
		AppUser appUser = ofy().load().type(AppUser.class).id(username.toLowerCase()).now();
		if (appUser == null)
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(ApiExceptions.USER_NOT_FOUND).build());

		return appUser;
	}

	@GET
	@Path(ApiPaths.GET_USERS_FOLLOWING)
	@ApiOperation(value = "Get following", response = FollowingResult.class)
	public static FollowingResult getFollowing(@Context HttpServletRequest request, @PathParam("user") final String username, @QueryParam("index") final int index) {

		// Only logged in users can see following		
		final AppUser currentUser = UserApi.getLoggedInUser(request);
		if (currentUser == null)
			throw new NotLoggedInException();

		final AppUser user = getAppUser(username);
		if (user == null)
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("User does not exist").build());

		if (user.isFollowingPrivate() && !user.getId().equals(currentUser.getId()))
			throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity("List is private").build());

		final Following following = ofy().load().type(Following.class).id(username.toLowerCase()).now();
		if (following != null && following.getFollowing() != null) {
			List<Ref<AppUser>> users = following.getFollowing();
			users = users.subList(index, Math.min(index + Constants.MORE_FOLLOWING_INC, following.getFollowing().size()));
			final boolean moreFollowing = (following.getFollowing().size() - index - users.size()) > 0;
			return new FollowingResult(new ArrayList<AppUser>(ofy().load().refs(users).values()), moreFollowing);
		}

		return new FollowingResult(new ArrayList<AppUser>(), false);
	}

	@GET
	@Path(ApiPaths.GET_USERS_FAVELIST)
	@ApiOperation(value = "Get a user's FaveList", response = FaveItemCollection.class)
	@ApiResponses(value = {@ApiResponse(code = 404, message = ApiExceptions.FAVELIST_NOT_FOUND)})
	public static FaveItemCollection getFaveList(
			@ApiParam(value = "The username", required = true) @PathParam("user") final String username,
			@ApiParam(value = "The list", required = true) @PathParam("list") final String list) {

		final FaveList faveList = FaveListDao.findFaveList(username, list);
		if (faveList == null)
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(ApiExceptions.FAVELIST_NOT_FOUND).build());

		return new FaveItemCollection(faveList.getList());
	}
}
