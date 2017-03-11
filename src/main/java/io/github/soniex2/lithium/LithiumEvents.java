package io.github.soniex2.lithium;

import io.github.soniex2.lithium.api.CapabilityLithium;
import io.github.soniex2.lithium.api.RefreshLithiumCapabilitiesEvent;
import io.github.soniex2.lithium.api.action.IAction;
import io.github.soniex2.lithium.api.action.IExtractAction;
import io.github.soniex2.lithium.api.action.IInsertAction;
import io.github.soniex2.lithium.api.energy.IEnergyAccessor;
import io.github.soniex2.lithium.api.energy.IEnergyProvider;
import io.github.soniex2.lithium.api.energy.IEnergyReceiver;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author soniex2
 */
public class LithiumEvents {
	private final Queue<TileEntity> delayedTileEntityAdditions = new ConcurrentLinkedQueue<TileEntity>();
	private final Map<World, Queue<TileEntity>> delayedWorldTileEntities = Collections.synchronizedMap(new WeakHashMap<World, Queue<TileEntity>>());
	private final Map<World, List<TileEntity>> energyNet = Collections.synchronizedMap(new WeakHashMap<World, List<TileEntity>>());
	private final Queue<TileEntity> whatTheHell = new ConcurrentLinkedQueue<TileEntity>(); // TODO

	@SubscribeEvent
	public void onLithiumUpdate(final RefreshLithiumCapabilitiesEvent event) {
		delayedTileEntityAdditions.add(event.getTileEntity());
	}

	@SubscribeEvent
	public void onAttachCapabilities(final AttachCapabilitiesEvent<TileEntity> event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			// no enet on the client
			return;
		}
		TileEntity te = event.getObject();
		// this is TOO SOON so we delay it
		delayedTileEntityAdditions.add(te);
	}

	private void processDelayedTileEntities() {
		TileEntity te = delayedTileEntityAdditions.peek();
		while (te != null && te.isInvalid()) {
			delayedTileEntityAdditions.remove();
			te = delayedTileEntityAdditions.peek();
		}
		while (te != null && !te.isInvalid()) {
			delayedTileEntityAdditions.remove();
			if (!te.hasWorld()) {
				whatTheHell.add(te);
			} else {
				World w = te.getWorld();
				synchronized (delayedWorldTileEntities) { // sadly it has to be done this way.
					Queue<TileEntity> queue = delayedWorldTileEntities.get(w);
					if (queue == null) {
						delayedWorldTileEntities.put(w, queue = new ConcurrentLinkedQueue<TileEntity>());
					}
					queue.add(te);
				}
			}
			te = delayedTileEntityAdditions.peek();
		}
	}

	private void processDelayedWorld(World world) {
		// at this point, all TEs *should* be ready.
		Queue<TileEntity> queue = delayedWorldTileEntities.get(world);
		if (queue == null) {
			return;
		}
		List<TileEntity> temp = new ArrayList<TileEntity>();
		for (TileEntity te : queue) {
			if (te.isInvalid() || !te.hasWorld()) { // this CAN happen?
				continue;
			}
			temp.add(te);
		}
		synchronized (energyNet) {
			List<TileEntity> list = energyNet.get(world);
			if (list == null) {
				energyNet.put(world, list = new ArrayList<TileEntity>());
			}
			list.addAll(temp);
		}
	}

	@SubscribeEvent
	public void onWorldTick(final TickEvent.WorldTickEvent event) {
		if (event.world.isRemote) {
			return;
		}
		if (event.phase == TickEvent.Phase.END) {
			processDelayedWorld(event.world);
			processDelayedTileEntities();
			List<TileEntity> energyTiles = energyNet.get(event.world);
			if (energyTiles == null) {
				return;
			}
			Queue<EnergyTransaction> queue = new ConcurrentLinkedQueue<EnergyTransaction>();
			try {
				Iterator<TileEntity> iter = energyTiles.iterator();
				while (iter.hasNext()) {
					TileEntity te = iter.next();
					if (te.isInvalid() || !te.hasWorld()) {
						iter.remove();
						continue;
					}
					BlockPos pos = te.getPos();
					if (!(event.world.isBlockLoaded(pos) && event.world.getWorldBorder().contains(pos))) {
						iter.remove();
						continue;
					}
					boolean hasCap = false;
					for (EnumFacing side : EnumFacing.VALUES) {
						IEnergyProvider from = te.getCapability(CapabilityLithium.ENERGY_PROVIDER, side);
						if (from == null) {
							continue;
						}
						if (from instanceof IEnergyAccessor) {
							throw new IllegalStateException("Don't use IEnergyAccessors like that, " + te);
						}
						hasCap = true;
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
						// we could also wrap Forge Energy in an IEnergyReceiver if we really wanted to... but nah.
						IEnergyReceiver to = target.getCapability(CapabilityLithium.ENERGY_RECEIVER, side.getOpposite());
						if (to == null) {
							continue;
						}
						if (to instanceof IEnergyAccessor) {
							throw new IllegalStateException("Don't use IEnergyAccessors like that, " + te);
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
						queue.add(new EnergyTransaction(extracted, inserted, te, target));
					}
					if (!hasCap) {
						// don't process TEs without the cap.
						iter.remove();
					}
				}
			} finally {
				// We want to try and commit EVERY transaction, except in the case of energy duplication/deletion.
				// (aka "except when we can't guarantee the consistency of the internal state anymore".)
				// We also want to run this code even if the above code throws an exception.
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
	}

	private class EnergyTransaction implements IAction {
		private final IExtractAction extractAction;
		private final IInsertAction insertAction;
		private final TileEntity te;
		private final TileEntity target;

		public EnergyTransaction(IExtractAction extracted, IInsertAction inserted, TileEntity te, TileEntity target) {
			this.extractAction = extracted;
			this.insertAction = inserted;
			this.te = te;
			this.target = target;
		}

		@Override
		public boolean commit() {
			te.markDirty();
			target.markDirty();
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
			te.markDirty();
			target.markDirty();
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
