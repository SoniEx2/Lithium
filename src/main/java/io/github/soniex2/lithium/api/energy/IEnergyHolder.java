package io.github.soniex2.lithium.api.energy;

/**
 * An energy container.
 *
 * @author soniex2
 */
public interface IEnergyHolder {
	/**
	 * Returns the maximum amount of energy the given slot can store.
	 *
	 * @param slot The slot.
	 * @return The maximum amount of energy the given slot can store.
	 */
	public int getEnergyLimit(int slot);

	/**
	 * Returns the amount of energy currently in the slot.
	 *
	 * @param slot The slot.
	 * @return The amount of energy currently in the slot.
	 */
	public int getCurrentEnergy(int slot);

	/**
	 * Returns the amout of slots in this energy container.
	 *
	 * @return The amount of slots in this energy container.
	 */
	public int getSlotCount();
}
