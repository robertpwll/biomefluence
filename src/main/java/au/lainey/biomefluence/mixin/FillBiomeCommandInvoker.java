package au.lainey.biomefluence.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.FillBiomeCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FillBiomeCommand.class)
public interface FillBiomeCommandInvoker {

    @Invoker("quantize")
    static BlockPos biomefluence$quantize(BlockPos blockPos) {
        throw new AssertionError();
    }
}
