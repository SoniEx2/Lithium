package io.github.soniex2.lithium.api;

import io.github.soniex2.lithium.api.action.IExtractAction;
import io.github.soniex2.lithium.api.action.IInsertAction;
import io.github.soniex2.lithium.api.energy.IEnergyHolder;
import io.github.soniex2.lithium.api.energy.IEnergyProvider;
import io.github.soniex2.lithium.api.energy.IEnergyReceiver;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * @author soniex2
 */
public class CapabilityLithium {
	@CapabilityInject(IEnergyProvider.class)
	public static Capability<IEnergyProvider> ENERGY_PROVIDER = null;

	@CapabilityInject(IEnergyReceiver.class)
	public static Capability<IEnergyReceiver> ENERGY_RECEIVER = null;

	@CapabilityInject(IEnergyHolder.class)
	public static Capability<IEnergyHolder> ENERGY_HOLDER = null;

	public static void register() {
		CapabilityManager.INSTANCE.register(IEnergyProvider.class, new Capability.IStorage<IEnergyProvider>() {
			@Nullable
			@Override
			public NBTBase writeNBT(Capability<IEnergyProvider> capability, IEnergyProvider instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<IEnergyProvider> capability, IEnergyProvider instance, EnumFacing side, NBTBase nbt) {

			}
		}, new Callable<IEnergyProvider>() {
			@Override
			public IEnergyProvider call() throws Exception {
				return new IEnergyProvider() {
					@Override
					public IExtractAction extract(int maxExtract) {
						return new IExtractAction() {
							@Override
							public boolean commit() {
								return true;
							}

							@Override
							public boolean revert() {
								return true;
							}

							@Override
							public int getEnergy() {
								return 0;
							}
						};
					}
				};
			}
		});
		CapabilityManager.INSTANCE.register(IEnergyReceiver.class, new Capability.IStorage<IEnergyReceiver>() {
			@Nullable
			@Override
			public NBTBase writeNBT(Capability<IEnergyReceiver> capability, IEnergyReceiver instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<IEnergyReceiver> capability, IEnergyReceiver instance, EnumFacing side, NBTBase nbt) {

			}
		}, new Callable<IEnergyReceiver>() {
			@Override
			public IEnergyReceiver call() throws Exception {
				return new IEnergyReceiver() {
					@Override
					public IInsertAction receive(int maxReceive) {
						return new IInsertAction() {
							@Override
							public boolean commit() {
								return true;
							}

							@Override
							public boolean revert() {
								return true;
							}

							@Override
							public int getEnergy() {
								return 0;
							}
						};
					}
				};
			}
		});
		CapabilityManager.INSTANCE.register(IEnergyHolder.class, new Capability.IStorage<IEnergyHolder>() {
			@Nullable
			@Override
			public NBTBase writeNBT(Capability<IEnergyHolder> capability, IEnergyHolder instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<IEnergyHolder> capability, IEnergyHolder instance, EnumFacing side, NBTBase nbt) {

			}
		}, new Callable<IEnergyHolder>() {
			@Override
			public IEnergyHolder call() throws Exception {
				return new IEnergyHolder() {
					@Override
					public int getEnergyLimit(int slot) {
						return 0;
					}

					@Override
					public int getCurrentEnergy(int slot) {
						return 0;
					}

					@Override
					public int getSlotCount() {
						return 0;
					}
				};
			}
		});
	}
}
