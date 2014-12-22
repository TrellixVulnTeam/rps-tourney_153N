package com.justdavis.karl.rpstourney.service.api.game;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.justdavis.karl.rpstourney.service.api.auth.Account;

/**
 * Models a player in a {@link Game}. This class allows other code to abstract
 * away the difference between human and AI players. While it's not enforced by
 * database constraints, whatever logic is used to create {@link Player}
 * instances should ensure that no more than one {@link Player} instance exists
 * for a given human or AI player; {@link Player} instances should be shared
 * between {@link Game}s.
 */
@Entity
@Table(name = "`Players`")
@XmlRootElement
public class Player {
	/*
	 * FIXME Would rather use GenerationType.IDENTITY, but can't, due to
	 * https://hibernate.atlassian.net/browse/HHH-9430.
	 */
	/*
	 * FIXME Would rather sequence name was mixed-case, but it can't be, due to
	 * https://hibernate.atlassian.net/browse/HHH-9431.
	 */
	@Id
	@Column(name = "`id`", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "Players_id_seq")
	@SequenceGenerator(name = "Players_id_seq", sequenceName = "players_id_seq")
	@XmlElement
	private long id;

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE,
			CascadeType.REFRESH, CascadeType.DETACH })
	@JoinColumn(name = "`humanAccountId`", nullable = true, unique = true)
	@XmlElement
	private Account humanAccount;

	/*
	 * TODO This class also needs to have an optional field to identify who an
	 * AI player might be (namely, which AI algorithm is represented). Logic
	 * such as an isHuman() method would likely also be helpful.
	 */

	/**
	 * Constructs a new {@link Player} instance to represent a human player.
	 * 
	 * @param humanAccount
	 *            the value to use for {@link #getHumanAccount()}
	 */
	public Player(Account humanAccount) {
		this.id = -1;
		this.humanAccount = humanAccount;
	}

	/**
	 * <strong>Not intended for use:</strong> This constructor is only provided
	 * to comply with the JAXB and JPA specs.
	 */
	@Deprecated
	Player() {
		this.id = -1;
		this.humanAccount = null;
	}

	/**
	 * @return <code>true</code> if this {@link Player} has been assigned an ID
	 *         (which it should if it's been persisted), <code>false</code> if
	 *         it has not
	 */
	public boolean hasId() {
		return id >= 0;
	}

	/**
	 * <p>
	 * Returns the unique integer that identifies and represents this
	 * {@link Player} instance.
	 * </p>
	 * <p>
	 * This value will be assigned by JPA when the {@link Entity} is persisted.
	 * Until then, this value should not be accessed.
	 * </p>
	 * 
	 * @return the unique integer that identifies and represents this
	 *         {@link Player} instance
	 */
	public long getId() {
		if (!hasId())
			throw new IllegalStateException("Field value not yet available.");

		return id;
	}

	/**
	 * @return a user-displayable name for the {@link Player}
	 */
	public String getName() {
		return humanAccount.getName();
	}

	/**
	 * @return the {@link Account} for the human player, or <code>null</code> if
	 *         this {@link Player} instance represents an AI player
	 */
	public Account getHumanAccount() {
		return humanAccount;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		/*
		 * Uses the id field (which has a UNIQUE constraint in the DB) when
		 * present, otherwise falls back to the superclass' implementation
		 * (mostly for the benefit of unit tests).
		 */

		if (hasId()) {
			/*
			 * Generated by Eclipse's
			 * "Source > Generate hashCode() and equals()..." function.
			 */
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		} else {
			return super.hashCode();
		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		/*
		 * Uses the id field (which has a UNIQUE constraint in the DB) when
		 * present, otherwise falls back to instance equality (mostly for the
		 * benefit of unit tests).
		 */

		if (hasId()) {
			/*
			 * Generated by Eclipse's
			 * "Source > Generate hashCode() and equals()..." function.
			 */
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Player other = (Player) obj;
			if (id != other.id)
				return false;
			return true;
		} else {
			return this == obj;
		}
	}
}
