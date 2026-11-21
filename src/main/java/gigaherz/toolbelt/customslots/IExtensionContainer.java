package gigaherz.toolbelt.customslots;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityLivingBase;

import com.google.common.collect.ImmutableList;

public interface IExtensionContainer {

    @Nonnull
    EntityLivingBase getOwner();

    @Nonnull
    ImmutableList<IExtensionSlot> getSlots();

    void onContentsChanged(IExtensionSlot slot);
}
