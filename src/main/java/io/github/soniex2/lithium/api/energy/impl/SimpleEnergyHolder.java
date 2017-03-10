package io.github.soniex2.lithium.api.energy.impl;

import com.google.common.base.Preconditions;
import io.github.soniex2.lithium.api.action.IExtractAction;
import io.github.soniex2.lithium.api.action.IInsertAction;
import io.github.soniex2.lithium.api.energy.IEnergyHolder;
import io.github.soniex2.lithium.api.energy.IEnergyProvider;
import io.github.soniex2.lithium.api.energy.IEnergyReceiver;

/**
 * @author soniex2
 */
public class SimpleEnergyHolder implements IEnergyHolder, IEnergyProvider, IEnergyReceiver {
	protected EnergyBuffer energy;
	protected int extractRate;
	protected int insertRate;

	public SimpleEnergyHolder(int size, int extractRate, int insertRate) {
		this.energy = new EnergyBuffer(size);
		this.extractRate = extractRate;
		this.insertRate = insertRate;
	}

	@Override
	public IExtractAction extract(int maxExtract) {
		if (this.extractRate <= 0) {
			return energy.extract(0);
		}
		int amount = Math.min(maxExtract, extractRate);
		return energy.extract(amount);
	}

	@Override
	public IInsertAction receive(int maxReceive) {
		if (this.insertRate <= 0) {
			return energy.receive(0);
		}
		int amount = Math.min(maxReceive, insertRate);
		return energy.receive(amount);
	}

	@Override
	public int getEnergyLimit(int slot) {
		Preconditions.checkElementIndex(slot, 1);
		return energy.getMaxEnergy();
	}

	@Override
	public int getCurrentEnergy(int slot) {
		Preconditions.checkElementIndex(slot, 1);
		return energy.getEnergy();
	}

	@Override
	public int getSlotCount() {
		return 1;
	}
}
