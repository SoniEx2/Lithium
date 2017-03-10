package io.github.soniex2.lithium.api.energy;

import io.github.soniex2.lithium.api.action.IExtractAction;

import javax.annotation.Nonnull;

/**
 * @author soniex2
 */
public interface IEnergyProvider {
	/**
	 * Extracts energy.
	 *
	 * @param maxExtract The maximum amount of energy to extract.
	 * @return The actual amount of energy extracted.
	 */
	@Nonnull
	public IExtractAction extract(int maxExtract);
}
