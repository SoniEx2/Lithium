package io.github.soniex2.lithium.api.energy;

/**
 * An energy buffer.
 *
 * @author soniex2
 */
public interface IEnergyAccessor extends IEnergyProvider, IEnergyReceiver {
	/**
	 * Returns the amount of energy in the buffer.
	 *
	 * @return The amount of energy in the buffer.
	 */
	int getEnergy();

	/**
	 * Returns the maximum amount of energy this buffer can have.
	 *
	 * @return The maximum amount of energy this buffer can have.
	 */
	int getMaxEnergy();

	/**
	 * Returns the amount of energy NOT in the buffer. Should be {@code getMaxEnergy() - getEnergy()}.
	 *
	 * @return The amount of energy NOT in the buffer.
	 */
	int getFreeSpace();
}
