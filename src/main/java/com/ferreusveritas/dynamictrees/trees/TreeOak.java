package com.ferreusveritas.dynamictrees.trees;

import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockFruit;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreatorApple;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenFruit;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenVine;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NodeFindEnds;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.registries.IForgeRegistry;

public class TreeOak extends DynamicTree {
	
	public class SpeciesOak extends Species {
		
		SpeciesOak(DynamicTree treeFamily) {
			super(treeFamily.getName(), treeFamily, ModBlocks.oakLeavesProperties);
			
			//Oak trees are about as average as you can get
			setBasicGrowingParameters(0.3f, 12.0f, upProbability, lowestBranchHeight, 0.8f);
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.50f);
			envFactor(Type.DRY, 0.50f);
			envFactor(Type.FOREST, 1.05f);
			
			if(ModConfigs.worldGen && !ModConfigs.enableAppleTrees) {//If we've disabled apple trees we still need some way to get apples.
				addDropCreator(new DropCreatorApple());
			}
			
			setupStandardSeedDropping();
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.FOREST, Biomes.FOREST_HILLS);
		}
	
		@Override
		public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
			if(super.rot(world, pos, neighborCount, radius, random)) {
				if(radius > 4 && TreeHelper.isRooty(world, pos.down()) && world.getLightFor(EnumSkyBlock.SKY, pos) < 4) {
					world.setBlockState(pos, random.nextInt(3) == 0 ? ModBlocks.blockStates.redMushroom : ModBlocks.blockStates.brownMushroom);//Change branch to a mushroom
					world.setBlockState(pos.down(), ModBlocks.blockStates.podzol);//Change rooty dirt to Podzol
				}
				return true;
			}
			
			return false;
		}
		
	}
	
	/**
	 * Swamp Oaks are just Oaks with slight growth differences that can generate in water
	 * and with vines hanging from their leaves.
	 */
	public class SpeciesSwampOak extends Species {
		
		FeatureGenVine vineGen;
		
		SpeciesSwampOak(DynamicTree treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getResourceDomain(), treeFamily.getName().getResourcePath() + "swamp"), treeFamily, ModBlocks.oakLeavesProperties);
			
			setBasicGrowingParameters(0.3f, 12.0f, upProbability, lowestBranchHeight, 0.8f);
			
			envFactor(Type.COLD, 0.50f);
			envFactor(Type.DRY, 0.50f);
			
			setupStandardSeedDropping();
						
			vineGen = new FeatureGenVine(this).setMaxLength(7).setVerSpread(30).setRayDistance(6);
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return isOneOfBiomes(biome, Biomes.SWAMPLAND);
		}
		
		@Override
		public boolean isAcceptableSoilForWorldgen(World world, BlockPos pos, IBlockState soilBlockState) {
			
			if(soilBlockState.getBlock() == Blocks.WATER) {
				Biome biome = world.getBiome(pos);
				if(CompatHelper.biomeHasType(biome, Type.SWAMP)) {
					BlockPos down = pos.down();
					if(isAcceptableSoil(world, down, world.getBlockState(down))) {
						return true;
					}
				}
			}
			
			return super.isAcceptableSoilForWorldgen(world, pos, soilBlockState);
		}
		
		//Swamp Oaks are just oaks in a swamp..  So they have the same seeds
		@Override
		public ItemStack getSeedStack(int qty) {
			return getCommonSpecies().getSeedStack(qty);
		}
		
		//Swamp Oaks are just oaks in a swamp..  So they have the same seeds
		@Override
		public Seed getSeed() {
			return getCommonSpecies().getSeed();
		}
		
		@Override
		public void postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, boolean worldGen) {
			super.postGeneration(world, rootPos, biome, radius, endPoints, worldGen);
			
			//Generate Vines
			vineGen.setQuantity(5).gen(world, rootPos.up(), endPoints);
		}
		
		@Override
		public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random) {
			if(super.rot(world, pos, neighborCount, radius, random)) {
				if(radius > 4 && TreeHelper.isRooty(world, pos.down()) && world.getLightFor(EnumSkyBlock.SKY, pos) < 4) {
					world.setBlockState(pos, random.nextInt(3) == 0 ? ModBlocks.blockStates.redMushroom : ModBlocks.blockStates.brownMushroom);//Change branch to a mushroom
					world.setBlockState(pos.down(), ModBlocks.blockStates.podzol);//Change rooty dirt to Podzol
				}
				return true;
			}
			
			return false;
		}
	}

	/**
	 * This species drops no seeds at all.  One must craft the seed from an apple.
	 */
	public class SpeciesAppleOak extends SpeciesRare {

		FeatureGenFruit appleGen;
		private static final String speciesName = "apple";
		
		public SpeciesAppleOak(DynamicTree treeFamily) {
			super(new ResourceLocation(treeFamily.getName().getResourceDomain(), speciesName), treeFamily, ModBlocks.oakLeavesProperties);
			
			//A bit stockier, smaller and slower than your basic oak
			setBasicGrowingParameters(0.4f, 10.0f, 1, 4, 0.7f);
			
			envFactor(Type.COLD, 0.75f);
			envFactor(Type.HOT, 0.75f);
			envFactor(Type.DRY, 0.25f);
			
			generateSeed();
						
			appleGen = new FeatureGenFruit(this, ModBlocks.blockFruit.getDefaultState()).setRayDistance(4);

			setDynamicSapling(ModBlocks.blockDynamicSaplingSpecies.getDefaultState());
		}
		
		@Override
		public boolean isBiomePerfect(Biome biome) {
			return biome == Biomes.PLAINS;
		}
		
		@Override
		public void postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, boolean worldGen) {
			super.postGeneration(world, rootPos, biome, radius, endPoints, worldGen);
			appleGen.setQuantity(10).setEnableHash(false).setFruit(ModBlocks.blockFruit.getDefaultState().withProperty(BlockFruit.AGE, 3)).gen(world, rootPos.up(), endPoints);
		}
		
		@Override
		public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, int soilLife, boolean rapid) {
			if(ModConfigs.enableAppleTrees) {
				IBlockState blockState = world.getBlockState(treePos);
				BlockBranch branch = TreeHelper.getBranch(blockState);
				
				if(branch != null && branch.getRadius(blockState, world, treePos) >= 8 && !rapid) {
					NodeFindEnds endFinder = new NodeFindEnds();
					TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
					appleGen.setQuantity(1).setEnableHash(true).setFruit(ModBlocks.blockFruit.getDefaultState().withProperty(BlockFruit.AGE, 0)).gen(world, rootPos.up(), endFinder.getEnds());
				}
			}
			return true;
		}
		
	}
	
	Species swampSpecies;
	Species appleSpecies;
	
	public TreeOak() {
		super(BlockPlanks.EnumType.OAK);
		ModBlocks.oakLeavesProperties.setTree(this);
		
		addConnectableVanillaLeaves(new IConnectable() {
			@Override
			public boolean isConnectable(IBlockState blockState) {
				return blockState.getBlock() instanceof BlockOldLeaf && (blockState.getValue(BlockOldLeaf.VARIANT) == BlockPlanks.EnumType.OAK);
			}
		});
	}
	
	@Override
	public void createSpecies() {
		setCommonSpecies(new SpeciesOak(this));
		swampSpecies = new SpeciesSwampOak(this);
		appleSpecies = new SpeciesAppleOak(this);
	}
	
	@Override
	public void registerSpecies(IForgeRegistry<Species> speciesRegistry) {
		super.registerSpecies(speciesRegistry);
		speciesRegistry.register(swampSpecies);
		speciesRegistry.register(appleSpecies);
	}
	
	@Override
	public List<Item> getRegisterableItems(List<Item> itemList) {
		itemList.add(appleSpecies.getSeed());//Since we generated the apple species internally we need to let the seed out to be registered.
		return super.getRegisterableItems(itemList);
	}
	
	/**
	 * This will cause the swamp variation of the oak to grow when the player plants
	 * a common oak acorn.
	 */
	@Override
	public Species getSpeciesForLocation(World world, BlockPos trunkPos) {
		if(CompatHelper.biomeHasType(world.getBiome(trunkPos), Type.SWAMP)) {
			return swampSpecies;
		}
		
		return super.getSpeciesForLocation(world, trunkPos);
	}
	
	@Override
	public int getRadiusForCellKit(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, BlockBranch branch) {
		int radius = branch.getRadius(blockState, blockAccess, pos);
		if(radius == 1) {
			if(blockAccess.getBlockState(pos.down()).getBlock() == branch) {
				return 128;
			}
		}
		return radius;
	}
	
}
