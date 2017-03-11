package io.github.soniex2.lithium.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;

/**
 * This event should be manually posted when you dynamically change TileEntity capabilities.
 *
 * @author soniex2
 */
public class RefreshLithiumCapabilitiesEvent extends BlockEvent {
	private final TileEntity te;

	/**
	 * Construct a new RefreshLithiumCapabilitiesEvent for the given block.
	 *
	 * @param world The world.
	 * @param pos The position.
	 * @param state The block state.
	 * @param te The TileEntity.
	 */
	public RefreshLithiumCapabilitiesEvent(World world, BlockPos pos, IBlockState state, TileEntity te) {
		super(world, pos, state);
		this.te = te;
	}

	/**
	 * Returs the TileEntity that needs to be refreshed.
	 *
	 * @return The TileEntity that needs to be refreshed.
	 */
	public TileEntity getTileEntity() {
		return te;
	}
}
