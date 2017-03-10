package io.github.soniex2.lithium.api.energy;

/**
 * @author soniex2
 */
public interface IEnergyHolder {
	public int getEnergyLimit(int slot);

	public int getCurrentEnergy(int slot);

	// what do you mean I can have internal slots and energy monitors can monitor them separately?
	public int getSlotCount();
}
