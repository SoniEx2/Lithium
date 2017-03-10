package io.github.soniex2.lithium.api.energy.impl;

import io.github.soniex2.lithium.api.action.IAction;
import io.github.soniex2.lithium.api.action.IExtractAction;
import io.github.soniex2.lithium.api.action.IInsertAction;
import io.github.soniex2.lithium.api.energy.IEnergyAccessor;
import io.github.soniex2.lithium.api.energy.IEnergyHolder;
import io.github.soniex2.lithium.api.energy.IEnergyProvider;
import io.github.soniex2.lithium.api.energy.IEnergyReceiver;
import net.minecraft.util.ITickable;

/**
 * An IEnergyHolder that supports both IEnergyProvider and IEnergyReceiver. Must be manually {@code update()}d.
 *
 * @author soniex2
 */
public class BufferedEnergyHolder implements IEnergyHolder, IEnergyProvider, IEnergyReceiver, ITickable {
	protected final IEnergyAccessor[] slots;

	public BufferedEnergyHolder(int size, int maxExtract, int maxReceive) {
		slots = new IEnergyAccessor[] {
			new EnergyBuffer(size), new EnergyBuffer(maxExtract), new EnergyBuffer(maxReceive)
		};
	}

	@Override
	public IExtractAction extract(int maxExtract) {
		//slots[1] = extract buffer
		if (slots[1].getMaxEnergy() <= 0) {
			return slots[1].extract(0);
		}
		return slots[1].extract(maxExtract);
	}

	@Override
	public IInsertAction receive(int maxReceive) {
		//slots[2] = receive buffer
		if (slots[2].getMaxEnergy() <= 0) {
			return slots[2].receive(0);
		}
		return slots[2].receive(maxReceive);
	}

	@Override
	public void update() {
		// TODO cleanup & optimize?
		int i = 0;
		if (slots[0].getMaxEnergy() <= 0) {
			i = 1;
		}
		if (i == 0) {
			IExtractAction tomove = slots[0].extract(Integer.MAX_VALUE);
			IInsertAction moved = slots[1].receive(tomove.getEnergy());
			if (moved.getEnergy() != tomove.getEnergy()) {
				tomove.revert();
				tomove = slots[0].extract(moved.getEnergy());
				assert tomove.getEnergy() == moved.getEnergy();
			}
			tomove.commit();
			moved.commit();
		}
		{
			IExtractAction tomove = slots[2].extract(Integer.MAX_VALUE);
			IInsertAction moved = slots[i].receive(tomove.getEnergy());
			if (moved.getEnergy() != tomove.getEnergy()) {
				tomove.revert();
				tomove = slots[2].extract(moved.getEnergy());
				assert tomove.getEnergy() == moved.getEnergy();
			}
			tomove.commit();
			moved.commit();
		}
		if (i == 0) {
			IExtractAction tomove = slots[0].extract(Integer.MAX_VALUE);
			IInsertAction moved = slots[1].receive(tomove.getEnergy());
			if (moved.getEnergy() != tomove.getEnergy()) {
				tomove.revert();
				tomove = slots[0].extract(moved.getEnergy());
				assert tomove.getEnergy() == moved.getEnergy();
			}
			tomove.commit();
			moved.commit();
		}
	}

	@Override
	public int getEnergyLimit(int slot) {
		return slots[slot].getMaxEnergy();
	}

	@Override
	public int getCurrentEnergy(int slot) {
		return slots[slot].getEnergy();
	}

	@Override
	public int getSlotCount() {
		return slots.length;
	}
}
