package io.github.soniex2.lithium.api.energy;

/**
 * @author soniex2
 */
public interface IEnergyAccessor extends IEnergyProvider, IEnergyReceiver {
	int getEnergy();

	int getMaxEnergy();

	int getFreeSpace();
}
