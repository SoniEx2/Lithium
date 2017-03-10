package io.github.soniex2.lithium;

import io.github.soniex2.lithium.api.CapabilityLithium;
import io.github.soniex2.lithium.api.action.IAction;
import io.github.soniex2.lithium.api.action.IExtractAction;
import io.github.soniex2.lithium.api.action.IInsertAction;
import io.github.soniex2.lithium.api.energy.IEnergyProvider;
import io.github.soniex2.lithium.api.energy.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ConcurrentModificationException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author soniex2
 */
public class LithiumEvents {
	@SubscribeEvent
	public void onWorldTick(final TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Queue<EnergyTransaction> queue = new ConcurrentLinkedQueue<EnergyTransaction>();
			for (TileEntity te : event.world.loadedTileEntityList) {
				if (te.isInvalid() || !te.hasWorld()) {
					continue;
				}
				BlockPos pos = te.getPos();
				if (!(event.world.isBlockLoaded(pos) && event.world.getWorldBorder().contains(pos))) {
					continue;
				}
				for (EnumFacing side : EnumFacing.VALUES) {
					IEnergyProvider from = te.getCapability(CapabilityLithium.ENERGY_PROVIDER, side);
					if (from == null) {
						continue;
					}
					IExtractAction maxExtract = from.extract(Integer.MAX_VALUE);
					maxExtract.revert();
					if (maxExtract.getEnergy() == 0) {
						// early exit for empty buffers
						continue;
					}
					BlockPos targetPos = te.getPos().offset(side);
					if (!(event.world.isBlockLoaded(pos) && event.world.getWorldBorder().contains(pos))) {
						continue;
					}
					TileEntity target = event.world.getTileEntity(targetPos);
					if (target == null) {
						continue;
					}
					// we could also wrap ForgeEnergy in an IEnergyReceiver if we really wanted to... but nah.
					IEnergyReceiver to = te.getCapability(CapabilityLithium.ENERGY_RECEIVER, side.getOpposite());
					if (to == null) {
						continue;
					}
					IInsertAction maxInsert = to.receive(maxExtract.getEnergy());
					maxInsert.revert();
					if (maxInsert.getEnergy() == 0) {
						continue;
					}
					IExtractAction extracted = from.extract(maxInsert.getEnergy());
					if (extracted.getEnergy() == 0) {
						extracted.revert();
						continue;
					}
					IInsertAction inserted = to.receive(extracted.getEnergy());
					assert extracted.getEnergy() == inserted.getEnergy();
					queue.add(new EnergyTransaction(extracted, inserted));
				}
			}
			// We want to try and commit EVERY transaction, except in the case of energy duplication/deletion.
			// (aka "except when we can't guarantee the consistency of the internal state anymore".)
			RuntimeException e = null;
			for (EnergyTransaction t : queue) {
				if (!t.commit()) {
					e = new ConcurrentModificationException("???");
				}
			}
			if (e != null) {
				// but we still want to throw.
				throw e;
			}
		}
	}

	private class EnergyTransaction implements IAction {
		private final IExtractAction extractAction;
		private final IInsertAction insertAction;

		public EnergyTransaction(IExtractAction extracted, IInsertAction inserted) {
			this.extractAction = extracted;
			this.insertAction = inserted;
		}

		@Override
		public boolean commit() {
			if (extractAction.commit()) {
				if (insertAction.commit()) {
					return true;
				}
				throw new ConcurrentModificationException("Energy duplication/deletion detected");
			} else {
				Lithium.logger.warn("Potential energy duplication/deletion detected!");
				insertAction.revert();
				return false;
			}
		}

		@Override
		public boolean revert() {
			// We could just throw UnsupportedOperationException, but w/e.
			if (insertAction.revert()) {
				if (extractAction.revert()) {
					return true;
				}
				throw new ConcurrentModificationException("Energy duplication/deletion detected");
			} else {
				Lithium.logger.warn("Potential energy duplication/deletion detected!");
				extractAction.commit();
				return false;
			}
		}

		@Override
		public int getEnergy() {
			// both actions should return the same energy.
			return extractAction.getEnergy();
		}
	}
}
