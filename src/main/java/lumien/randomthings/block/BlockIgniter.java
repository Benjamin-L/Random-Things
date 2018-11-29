package lumien.randomthings.block;

import java.util.Random;

import lumien.randomthings.RandomThings;
import lumien.randomthings.lib.GuiIds;
import lumien.randomthings.tileentity.TileEntityIgniter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockIgniter extends BlockContainerBase
{
	public static final PropertyDirection FACING = PropertyDirection.create("facing");

	protected BlockIgniter()
	{
		super("igniter", Material.ROCK);

		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setHardness(1.5F);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!worldIn.isRemote)
		{
			playerIn.openGui(RandomThings.instance, GuiIds.IGNITER, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public boolean isFireSource(World world, BlockPos pos, EnumFacing side)
	{
		EnumFacing facing = world.getBlockState(pos).getValue(FACING);

		return side == facing;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
	{
		super.onBlockAdded(worldIn, pos, state);

		this.setDefaultFacing(worldIn, pos, state);
	}

	private void setDefaultFacing(World worldIn, BlockPos pos, IBlockState state)
	{
		if (!worldIn.isRemote)
		{
			IBlockState iblockstate = worldIn.getBlockState(pos.north());
			IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
			IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
			IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
			EnumFacing enumfacing = state.getValue(FACING);

			if (enumfacing == EnumFacing.NORTH && iblockstate.isFullBlock() && !iblockstate1.isFullBlock())
			{
				enumfacing = EnumFacing.SOUTH;
			}
			else if (enumfacing == EnumFacing.SOUTH && iblockstate1.isFullBlock() && !iblockstate.isFullBlock())
			{
				enumfacing = EnumFacing.NORTH;
			}
			else if (enumfacing == EnumFacing.WEST && iblockstate2.isFullBlock() && !iblockstate3.isFullBlock())
			{
				enumfacing = EnumFacing.EAST;
			}
			else if (enumfacing == EnumFacing.EAST && iblockstate3.isFullBlock() && !iblockstate2.isFullBlock())
			{
				enumfacing = EnumFacing.WEST;
			}

			worldIn.setBlockState(pos, state.withProperty(FACING, enumfacing), 2);
		}
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		boolean powered = false;
		if (meta >= 6)
		{
			meta -= 6;
			powered = true;
		}

		return this.getDefaultState().withProperty(FACING, EnumFacing.values()[meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int i = state.getValue(FACING).getIndex();

		return i;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileEntityIgniter();
	}
}
