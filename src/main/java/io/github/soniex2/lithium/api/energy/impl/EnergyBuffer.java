package io.github.soniex2.lithium.api.energy.impl;

import com.google.common.base.Preconditions;
import io.github.soniex2.lithium.api.action.IExtractAction;
import io.github.soniex2.lithium.api.action.IInsertAction;
import io.github.soniex2.lithium.api.energy.IEnergyAccessor;
import io.github.soniex2.lithium.api.energy.IEnergyProvider;
import io.github.soniex2.lithium.api.energy.IEnergyReceiver;

/**
 * @author soniex2
 */
public class EnergyBuffer implements IEnergyAccessor, IEnergyProvider, IEnergyReceiver {
	/**
	 * The current energy in this buffer.
	 */
	protected int energy;
	/**
	 * The current energy, plus any pending IInsertActions.
	 */
	protected int energyPlusInsert;
	/**
	 * The current energy, minus any pending IExtractActions.
	 */
	protected int energyMinusExtract;
	/**
	 * The maximum amount of energy this buffer can hold.
	 */
	protected final int maxEnergy;

	public EnergyBuffer(int maxEnergy) {
		this.maxEnergy = maxEnergy;
	}

	@Override
	public int getEnergy() {
		return energy;
	}

	@Override
	public int getMaxEnergy() {
		return maxEnergy;
	}

	@Override
	public int getFreeSpace() {
		return getMaxEnergy() - getEnergy();
	}

	@Override
	public IInsertAction receive(int maxReceive) {
		return new InsertAction(maxReceive);
	}

	@Override
	public IExtractAction extract(int maxExtract) {
		return new ExtractAction(maxExtract);
	}

	private class InsertAction implements IInsertAction {
		private final int amount;
		private boolean done;

		private InsertAction(int maxReceive) {
			Preconditions.checkArgument(maxReceive >= 0, "maxReceive < 0");
			amount = Math.min(maxReceive, maxEnergy - energyPlusInsert);
			energyPlusInsert += amount;
			// TODO throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public boolean commit() {
			if (done) {
				return false;
			}
			done = true;
			energy += amount;
			energyMinusExtract += amount;
			return true;
		}

		@Override
		public boolean revert() {
			if (done) {
				return false;
			}
			done = true;
			energyPlusInsert -= amount;
			return true;
		}

		@Override
		public int getEnergy() {
			return amount;
		}
	}

	private class ExtractAction implements IExtractAction {
		private final int amount;
		private boolean done;

		private ExtractAction(int maxExtract) {
			Preconditions.checkArgument(maxExtract >= 0, "maxExtract < 0");
			amount = Math.min(maxExtract, energyMinusExtract);
			energyMinusExtract -= amount;
			// TODO throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public boolean commit() {
			if (done) {
				return false;
			}
			done = true;
			energy -= amount;
			energyPlusInsert -= amount;
			return true;
		}

		@Override
		public boolean revert() {
			if (done) {
				return false;
			}
			done = true;
			energyMinusExtract += amount;
			return true;
		}

		@Override
		public int getEnergy() {
			return amount;
		}
	}
}
