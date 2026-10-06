package au.lainey.biomefluence;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.ladysnake.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.chunk.ChunkComponentInitializer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;

import java.io.BufferedReader;

public class Biomefluence implements ModInitializer, ChunkComponentInitializer {
    public static final String MOD_ID = "biomefluence";

    public static final ComponentKey<InfluenceComponent> INFLUENCE = ComponentRegistryV3.INSTANCE.getOrCreate(resourceLocation("influence"), InfluenceComponent.class);

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return resourceLocation("influence");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                Event.SAPLING_GROW.reload();

                ThrowableRunnable.wrap(() -> {
                    for (Resource resource : resourceManager.listResources("influence", path -> path.toString().endsWith(".json")).values()) {
                        try (BufferedReader reader = resource.openAsReader()) {
                            DataResult<Influence> result = Influence.CODEC.parse(JsonOps.INSTANCE, new Gson().fromJson(reader, JsonElement.class));

                            if (result.isError()) {
                                ThrowableRunnable.LOGGER.error(result.error().orElseThrow().message());
                                continue;
                            }

                            Influence influence = result.getOrThrow();
                            influence.event().add(influence);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(INFLUENCE, InfluenceComponent::new);
    }

    public static ResourceLocation resourceLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
