package com.justdavis.karl.rpstourney.service.api.game;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.DynamicUpdate;
import org.threeten.bp.Instant;

import com.justdavis.karl.misc.exceptions.BadCodeMonkeyException;
import com.justdavis.karl.rpstourney.service.api.game.GameSession.GameSessionPk;
import com.justdavis.karl.rpstourney.service.api.jaxb.InstantJaxbAdapter;

/**
 * <p>
 * Represents a round of a {@link GameSession}, tracking the moves made by the
 * players.
 * </p>
 * <p>
 * Please note that instances of this class are <strong>not</strong> immutable:
 * the {@link #setThrowForPlayer1(Throw)} and {@link #setThrowForPlayer2(Throw)}
 * methods will modify data. However, as both of those methods may only be
 * called once, instances are effectively immutable after they've both been
 * supplied with a value.
 * </p>
 */
@Entity
@IdClass(GameRound.GameRoundPk.class)
@Table(name = "`GameRounds`")
@DynamicUpdate(true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GameRound {
	/*
	 * FIXME This column can't be quoted unless/until
	 * https://hibernate.atlassian.net/browse/HHH-9427 is resolved.
	 */
	@Id
	@JoinColumn(name = "gameSessionId")
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REFRESH, CascadeType.DETACH })
	@XmlTransient
	private GameSession gameSession;

	@Id
	@Column(name = "`roundIndex`", nullable = false, updatable = false)
	@XmlElement
	private int roundIndex;

	@Column(name = "`throwForPlayer1`")
	@Enumerated(EnumType.STRING)
	@XmlElement
	private Throw throwForPlayer1;

	@Column(name = "`throwForPlayer1Timestamp`", nullable = true, updatable = true)
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.threetenbp.PersistentInstantAsTimestamp")
	@XmlElement
	@XmlJavaTypeAdapter(InstantJaxbAdapter.class)
	private Instant throwForPlayer1Timestamp;

	@Column(name = "`throwForPlayer2`")
	@Enumerated(EnumType.STRING)
	@XmlElement
	private Throw throwForPlayer2;

	@Column(name = "`throwForPlayer2Timestamp`", nullable = true, updatable = true)
	@org.hibernate.annotations.Type(type = "org.jadira.usertype.dateandtime.threetenbp.PersistentInstantAsTimestamp")
	@XmlElement
	@XmlJavaTypeAdapter(InstantJaxbAdapter.class)
	private Instant throwForPlayer2Timestamp;

	/**
	 * Constructs a new {@link GameRound} instance.
	 * 
	 * @param gameSession
	 *            the value to use for {@link #getGameSession()}
	 * @param roundIndex
	 *            the value to use for {@link #getRoundIndex()}
	 */
	public GameRound(GameSession gameSession, int roundIndex) {
		if (gameSession == null)
			throw new IllegalArgumentException();
		if (roundIndex < 0)
			throw new IllegalArgumentException();

		this.gameSession = gameSession;
		this.roundIndex = roundIndex;
		this.throwForPlayer1 = null;
		this.throwForPlayer1Timestamp = null;
		this.throwForPlayer2 = null;
		this.throwForPlayer2Timestamp = null;
	}

	/**
	 * <strong>Not intended for use:</strong> This constructor is only provided
	 * to comply with the JAXB and JPA specs.
	 */
	@Deprecated
	GameRound() {
	}

	/**
	 * @return the {@link GameSession} that this {@link GameRound} is a part of
	 */
	public GameSession getGameSession() {
		return gameSession;
	}

	/**
	 * @return the index of this {@link GameRound} in the {@link GameSession} 
	 *         it's part of
	 */
	public int getRoundIndex() {
		return roundIndex;
	}

	/**
	 * @return the {@link Throw} that was selected by the first player in this
	 *         {@link GameRound}, or <code>null</code> if that player has not
	 *         yet selected their move
	 */
	public Throw getThrowForPlayer1() {
		return throwForPlayer1;
	}

	/**
	 * @return the date-time that {@link #setThrowForPlayer1(Throw)} was called
	 *         (with a valid value) for this {@link GameRound}, or
	 *         <code>null</code> if it hasn't yet
	 */
	public Instant getThrowForPlayer1Timestamp() {
		return throwForPlayer1Timestamp;
	}

	/**
	 * Note: this method may only be called once, and should only be called by
	 * {@link GameSession}.
	 * 
	 * @param throwForPlayer1
	 *            the value to use for {@link #getThrowForPlayer1()}
	 * @throws GameConflictException
	 *             A {@link GameConflictException} will be thrown if the
	 *             {@link Player} has already submitted a {@link Throw} for this
	 *             {@link GameRound}.
	 * @see GameSession#submitThrow(int, Player, Throw)
	 */
	void setThrowForPlayer1(Throw throwForPlayer1) {
		if (throwForPlayer1 == null)
			throw new IllegalArgumentException();
		if (this.throwForPlayer1 != null)
			throw new GameConflictException(String.format(
					"Throw already set to '%s'; can't set to '%s'.",
					this.throwForPlayer1, throwForPlayer1));

		this.throwForPlayer1 = throwForPlayer1;
		this.throwForPlayer1Timestamp = Instant.now();
	}

	/**
	 * @return the {@link Throw} that was selected by the second player in this
	 *         {@link GameRound}, or <code>null</code> if that player has not
	 *         yet selected their move
	 */
	public Throw getThrowForPlayer2() {
		return throwForPlayer2;
	}

	/**
	 * @return the date-time that {@link #setThrowForPlayer2(Throw)} was called
	 *         (with a valid value) for this {@link GameRound}, or
	 *         <code>null</code> if it hasn't yet
	 */
	public Instant getThrowForPlayer2Timestamp() {
		return throwForPlayer2Timestamp;
	}

	/**
	 * Note: this method may only be called once, and should only be called by
	 * {@link GameSession}.
	 * 
	 * @param throwForPlayer2
	 *            the value to use for {@link #getThrowForPlayer2()}
	 * @throws GameConflictException
	 *             A {@link GameConflictException} will be thrown if the
	 *             {@link Player} has already submitted a {@link Throw} for this
	 *             {@link GameRound}.
	 * @see GameSession#submitThrow(int, Player, Throw)
	 */
	void setThrowForPlayer2(Throw throwForPlayer2) {
		if (throwForPlayer2 == null)
			throw new IllegalArgumentException();
		if (this.throwForPlayer2 != null)
			throw new GameConflictException(String.format(
					"Throw already set to '%s'; can't set to '%s'.",
					this.throwForPlayer2, throwForPlayer2));

		this.throwForPlayer2 = throwForPlayer2;
		this.throwForPlayer2Timestamp = Instant.now();
	}

	/**
	 * @return the {@link Result} of this {@link GameRound}, or
	 *         <code>null</code> if the {@link GameRound} has not yet completed.
	 */
	public Result getResult() {
		if (throwForPlayer1 == null || throwForPlayer2 == null)
			return null;

		/*
		 * Eventually, I may want to abstract out this logic to allow for custom
		 * Throw types. For right now, though, this works.
		 */
		if (throwForPlayer1.equals(throwForPlayer2))
			return Result.TIED;
		if (throwForPlayer1 == Throw.ROCK && throwForPlayer2 == Throw.PAPER)
			return Result.PLAYER_2_WON;
		if (throwForPlayer1 == Throw.ROCK && throwForPlayer2 == Throw.SCISSORS)
			return Result.PLAYER_1_WON;
		if (throwForPlayer1 == Throw.PAPER && throwForPlayer2 == Throw.ROCK)
			return Result.PLAYER_1_WON;
		if (throwForPlayer1 == Throw.PAPER && throwForPlayer2 == Throw.SCISSORS)
			return Result.PLAYER_2_WON;
		if (throwForPlayer1 == Throw.SCISSORS && throwForPlayer2 == Throw.ROCK)
			return Result.PLAYER_2_WON;
		if (throwForPlayer1 == Throw.SCISSORS && throwForPlayer2 == Throw.PAPER)
			return Result.PLAYER_1_WON;

		// Must have a missing case in the logic above.
		throw new BadCodeMonkeyException();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GameRound [roundIndex=");
		builder.append(roundIndex);
		builder.append(", throwForPlayer1=");
		builder.append(throwForPlayer1);
		builder.append(", throwForPlayer2=");
		builder.append(throwForPlayer2);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Enumerates the possible results for a completed {@link GameRound}.
	 */
	public static enum Result {
		PLAYER_1_WON,

		PLAYER_2_WON,

		TIED;
	}

	/**
	 * The JPA {@link IdClass} for {@link GameRound}.
	 */
	public static final class GameRoundPk implements Serializable {
		private static final long serialVersionUID = 9072061865707404807L;

		private GameSessionPk gameSession;
		private int roundIndex;

		/**
		 * This public, no-arg/default constructor is required by JPA.
		 */
		public GameRoundPk() {
		}

		/**
		 * @return this {@link IdClass} field corresponds to
		 *         {@link GameRound#getGameSession()}, which is mapped as a
		 *         foreign key to {@link GameSession#getId()}
		 */
		public GameSessionPk getGameSession() {
			return gameSession;
		}

		/**
		 * @param gameSession
		 *            the value to use for {@link #getGameSession()}
		 */
		public void setGameSession(GameSessionPk gameSession) {
			this.gameSession = gameSession;
		}

		/**
		 * @return this {@link IdClass} field corresponds to
		 *         {@link GameRound#getRoundIndex()}
		 */
		public int getRoundIndex() {
			return roundIndex;
		}

		/**
		 * @param roundIndex
		 *            the value to use for {@link #getRoundIndex()}
		 */
		public void setRoundIndex(int roundIndex) {
			this.roundIndex = roundIndex;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			/*
			 * This method was generated via Eclipse's 'Source > Generate
			 * hashCode() and equals()...' function.
			 */

			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((gameSession == null) ? 0 : gameSession.hashCode());
			result = prime * result + roundIndex;
			return result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			/*
			 * This method was generated via Eclipse's 'Source > Generate
			 * hashCode() and equals()...' function.
			 */

			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GameRoundPk other = (GameRoundPk) obj;
			if (gameSession == null) {
				if (other.gameSession != null)
					return false;
			} else if (!gameSession.equals(other.gameSession))
				return false;
			if (roundIndex != other.roundIndex)
				return false;
			return true;
		}
	}
}
