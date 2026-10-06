package au.lainey.biomefluence;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.ladysnake.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.chunk.ChunkComponentInitializer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;

public class Biomefluence implements ModInitializer, ChunkComponentInitializer {
    public static final String MOD_ID = "biomefluence";

    public static final ComponentKey<InfluenceComponent> INFLUENCE = ComponentRegistryV3.INSTANCE.getOrCreate(resourceLocation("influence"), InfluenceComponent.class);

    @Override
    public void onInitialize() {

    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(INFLUENCE, InfluenceComponent::new);
    }

    public static ResourceLocation resourceLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

}
