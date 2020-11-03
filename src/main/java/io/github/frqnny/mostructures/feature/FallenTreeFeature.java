package io.github.frqnny.mostructures.feature;

import io.github.frqnny.mostructures.MoStructures;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PillarBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.Objects;
import java.util.Random;

public class FallenTreeFeature extends Feature<DefaultFeatureConfig> {
    public static final Identifier ID = MoStructures.id("fallen_tree");


    public FallenTreeFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    public static BlockState getWoodToPlace(Biome biome) {
        Biome.Category category = biome.getCategory();
        if (biome == BuiltinRegistries.BIOME.get(BiomeKeys.BIRCH_FOREST) || biome == BuiltinRegistries.BIOME.get(BiomeKeys.BIRCH_FOREST_HILLS) || biome == BuiltinRegistries.BIOME.get(BiomeKeys.TALL_BIRCH_FOREST)) {
            return Blocks.BIRCH_LOG.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.X);
        } else if (category == Biome.Category.FOREST || biome == BuiltinBiomes.PLAINS || category == Biome.Category.RIVER || category == Biome.Category.SWAMP) {
            return Blocks.OAK_LOG.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.X);
        } else if (category == Biome.Category.SAVANNA || category == Biome.Category.DESERT) {
            return Blocks.ACACIA_LOG.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.X);
        } else if (category == Biome.Category.TAIGA || category == Biome.Category.ICY) {
            return Blocks.SPRUCE_LOG.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.X);
        } else {
            return Blocks.AIR.getDefaultState();
        }
    }

    public static boolean canPlaceWood(BlockState block) {
        return block == Blocks.AIR.getDefaultState() || block == Blocks.GRASS.getDefaultState() || block == Blocks.WATER.getDefaultState() || block == Blocks.VINE.getDefaultState();
    }

    @Override
    public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos pos, DefaultFeatureConfig featureConfig) {
        Biome.Category category = world.getBiome(pos).getCategory();

        if (category == Biome.Category.OCEAN
                || category == Biome.Category.RIVER
                || category == Biome.Category.BEACH
                || category == Biome.Category.THEEND
                || category == Biome.Category.NETHER
                || !Objects.requireNonNull(world.toServerWorld().getRegistryManager().getOptional(Registry.BIOME_KEY).get().getId(world.getBiome(pos))).getNamespace().equals("minecraft")) {
            return false;
        }


        BlockPos newPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);

        if (world.getBlockState(newPos.down()) == Blocks.WATER.getDefaultState()) {
            newPos = newPos.down();
        }
        BlockState blockToPlace = getWoodToPlace(world.getBiome(newPos));
        if (blockToPlace.getBlock() == Blocks.AIR) {
            return false;
        }

        //Makes Desert Fallen Trees rarer, as in Forest that is taken care whether it is to generate in an open space or not
        if (category == Biome.Category.DESERT || category == Biome.Category.PLAINS || category == Biome.Category.SWAMP) {
            if (random.nextInt(10) != 1) {
                return false;
            }
        }
        int blocksitWillPlace = 0;
        BlockPos simulationPos = newPos;
        for (int i = 6; i > 0; i--) {
            if (canPlaceWood(world.getBlockState(simulationPos))) {
                blocksitWillPlace++;
                simulationPos = simulationPos.east();
            } else {
                break;
            }
        }
        boolean canPlace = blocksitWillPlace > 3;
        if (canPlace) {
            for (int i = 6; i > 0; i--) {
                if (canPlaceWood(world.getBlockState(newPos))) {
                    world.setBlockState(newPos, blockToPlace, 3);
                    newPos = newPos.east();
                } else {
                    break;
                }
            }
        }
        return canPlace;
    }
}