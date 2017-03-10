package io.github.soniex2.lithium.api.energy;

import io.github.soniex2.lithium.api.action.IInsertAction;

import javax.annotation.Nonnull;

/**
 * @author soniex2
 */
public interface IEnergyReceiver {
	/**
	 * Inserts energy.
	 *
	 * @param maxReceive The maximum amount of energy to insert.
	 * @return The actual amount of energy inserted.
	 */
	@Nonnull
	public IInsertAction receive(int maxReceive);
}
