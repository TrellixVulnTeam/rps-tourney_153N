package com.justdavis.karl.rpstourney.service.app.game;

import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.justdavis.karl.misc.exceptions.BadCodeMonkeyException;
import com.justdavis.karl.rpstourney.service.api.auth.Account;
import com.justdavis.karl.rpstourney.service.api.auth.SecurityRole;
import com.justdavis.karl.rpstourney.service.api.game.GameSession;
import com.justdavis.karl.rpstourney.service.api.game.IGameSessionResource;
import com.justdavis.karl.rpstourney.service.api.game.Player;
import com.justdavis.karl.rpstourney.service.api.game.Throw;
import com.justdavis.karl.rpstourney.service.app.auth.AccountSecurityContext;
import com.justdavis.karl.rpstourney.service.app.auth.AuthenticationFilter;

/**
 * The web service implementation of {@link IGameSessionResource}, which is the
 * primary service for gameplay interactions.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GameSessionResourceImpl implements IGameSessionResource {
	private AccountSecurityContext securityContext;
	private IPlayersDao playersDao;
	private IGameSessionsDao gamesDao;

	/**
	 * This public, default/no-arg constructor is required by Spring (for
	 * request-scoped beans).
	 */
	public GameSessionResourceImpl() {
	}

	/**
	 * @param securityContext
	 *            the {@link AccountSecurityContext} for the request that the
	 *            {@link GameSessionResourceImpl} was instantiated to handle
	 */
	@Context
	public void setAccountSecurityContext(AccountSecurityContext securityContext) {
		if (securityContext == null)
			throw new IllegalArgumentException();

		this.securityContext = securityContext;
	}

	/**
	 * @param playersDao
	 *            the injected {@link IPlayersDao} to use
	 */
	@Inject
	public void setPlayersDao(IPlayersDao playersDao) {
		if (playersDao == null)
			throw new IllegalArgumentException();

		this.playersDao = playersDao;
	}

	/**
	 * @param gamesDao
	 *            the injected {@link IGameSessionsDao} to use
	 */
	@Inject
	public void setGamesDao(IGameSessionsDao gamesDao) {
		if (gamesDao == null)
			throw new IllegalArgumentException();

		this.gamesDao = gamesDao;
	}

	/**
	 * @see com.justdavis.karl.rpstourney.service.api.game.IGameSessionResource#createGame()
	 */
	@RolesAllowed({ SecurityRole.ID_USERS })
	@Transactional
	@Override
	public GameSession createGame() {
		// Determine the current user/player.
		Account userAccount = getUserAccount();
		Player userPlayer = playersDao
				.findOrCreatePlayerForAccount(userAccount);

		// Create the new game.
		GameSession game = new GameSession(userPlayer);
		gamesDao.save(game);

		return game;
	}

	/**
	 * @see com.justdavis.karl.rpstourney.service.api.game.IGameSessionResource#getGamesForPlayer()
	 */
	@Override
	public List<GameSession> getGamesForPlayer() {
		// Return an empty Set for unauthenticated users.
		if (securityContext.getUserPrincipal() == null) {
			return Collections.emptyList();
		}

		// Determine the current user/player.
		Account userAccount = getUserAccount();
		Player userPlayer = playersDao
				.findOrCreatePlayerForAccount(userAccount);

		// Get the games for that Player.
		List<GameSession> games = gamesDao.getGameSessionsForPlayer(userPlayer);
		return games;
	}

	/**
	 * @see com.justdavis.karl.rpstourney.service.api.game.IGameSessionResource#getGame(java.lang.String)
	 */
	@Override
	public GameSession getGame(String gameSessionId) {
		// Look up the specified game.
		GameSession game = gamesDao.findById(gameSessionId);
		if (game == null)
			throw new NotFoundException("Game not found: " + gameSessionId);

		return game;
	}

	/**
	 * @see com.justdavis.karl.rpstourney.service.api.game.IGameSessionResource#setMaxRounds(java.lang.String,
	 *      int, int)
	 */
	@RolesAllowed({ SecurityRole.ID_USERS })
	@Transactional
	@Override
	public GameSession setMaxRounds(String gameSessionId,
			int oldMaxRoundsValue, int newMaxRoundsValue) {
		GameSession game = getGame(gameSessionId);

		/*
		 * Check to make sure that the requesting user is one of the two
		 * players.
		 */
		Account userAccount = getUserAccount();
		Player userPlayer = playersDao
				.findOrCreatePlayerForAccount(userAccount);
		if (!userPlayer.equals(game.getPlayer1())
				&& !userPlayer.equals(game.getPlayer2()))
			throw new IllegalArgumentException();

		try {
			game = gamesDao.setMaxRounds(gameSessionId, oldMaxRoundsValue,
					newMaxRoundsValue);
		} catch (IllegalArgumentException e) {
			// Invalid rounds value.
			throw new WebApplicationException(e, Status.BAD_REQUEST);
		}

		return game;
	}

	/**
	 * @see com.justdavis.karl.rpstourney.service.api.game.IGameSessionResource#joinGame(java.lang.String)
	 */
	@RolesAllowed({ SecurityRole.ID_USERS })
	@Transactional
	@Override
	public GameSession joinGame(String gameSessionId) {
		GameSession game = getGame(gameSessionId);

		// Determine the current user/player.
		Account userAccount = getUserAccount();
		Player userPlayer = playersDao
				.findOrCreatePlayerForAccount(userAccount);

		try {
			game.setPlayer2(userPlayer);
		} catch (IllegalArgumentException e) {
			// Trying to set the same user as both players.
			throw new WebApplicationException(e, Status.BAD_REQUEST);
		}

		gamesDao.save(game);
		return game;
	}

	/**
	 * @see com.justdavis.karl.rpstourney.service.api.game.IGameSessionResource#prepareRound(java.lang.String)
	 */
	@Transactional
	@Override
	public GameSession prepareRound(String gameSessionId) {
		/*
		 * Note: This method is intentionally not marked with @RolesAllowed, as
		 * it doesn't really matter who calls it.
		 */

		GameSession game = getGame(gameSessionId);

		// Prepare the round, if needed.
		if (!game.isRoundPrepared()) {
			game.prepareRound();
		}

		gamesDao.save(game);
		return game;
	}

	/**
	 * @see com.justdavis.karl.rpstourney.service.api.game.IGameSessionResource#submitThrow(java.lang.String,
	 *      int, com.justdavis.karl.rpstourney.service.api.game.Throw)
	 */
	@RolesAllowed({ SecurityRole.ID_USERS })
	@Transactional
	@Override
	public GameSession submitThrow(String gameSessionId, int roundIndex,
			Throw throwToPlay) {
		GameSession game = getGame(gameSessionId);

		// Determine the current user/player.
		Account userAccount = getUserAccount();
		Player userPlayer = playersDao
				.findOrCreatePlayerForAccount(userAccount);

		try {
			game.submitThrow(roundIndex, userPlayer, throwToPlay);
		} catch (IllegalArgumentException e) {
			// Trying to set the same user as both players.
			throw new WebApplicationException(e, Status.BAD_REQUEST);
		}

		gamesDao.save(game);
		return game;
	}

	/**
	 * This method should only be used on web service requests annotated with
	 * <code>@RolesAllowed({ SecurityRole.ID_USERS })</code>, as it assumes that
	 * the request currently being processed is authenticated.
	 * 
	 * @return the requestor's Account from {@link #securityContext}, which will
	 *         have been set by the {@link AuthenticationFilter}
	 */
	private Account getUserAccount() {
		Account userAccount = securityContext.getUserPrincipal();
		if (userAccount == null)
			throw new BadCodeMonkeyException("RolesAllowed not working.");

		return userAccount;
	}
}
