package carminite.mixin;

import carminite.events.neoforge.ViewportEvent;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private @Nullable Entity entity;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private float eyeHeight;

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    private float xRot;

    @Shadow
    private float yRot;

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    @Final
    private static Vector3fc FORWARDS;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    @Final
    private static Vector3fc UP;

    @Shadow
    @Final
    private static Vector3fc LEFT;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f left;

    @Shadow
    private int matrixPropertiesDirty;

    @WrapMethod(
        method = "setRotation(FF)V"
    )
    private void carminite$redirectSetRotation(
        float yRot,
        float xRot,
        Operation<Void> original
    ) {
        carminite$setRotation(yRot, xRot, 0.0F);
    }

    @WrapOperation(
        method = "alignWithEntity(F)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setRotation(FF)V",
            ordinal = 1
        )
    )
    private void carminite$computeCameraAngles(
        Camera instance,
        float yRot,
        float xRot,
        Operation<Void> original,
        @Local(argsOnly = true, name = "partialTicks") float partialTicks
    ) {
        var cameraSetup = new ViewportEvent.ComputeCameraAngles(
            instance,
            partialTicks,
            this.entity.getViewYRot(partialTicks),
            this.entity.getViewXRot(partialTicks),
            0
        ).post();

        this.carminite$setRotation(cameraSetup.getYaw(), cameraSetup.getPitch(), cameraSetup.getRoll());
        this.setPosition(
            Mth.lerp(partialTicks, this.entity.xo, this.entity.getX()),
            Mth.lerp(partialTicks, this.entity.yo, this.entity.getY()) + Mth.lerp(partialTicks, this.eyeHeightOld, this.eyeHeight),
            Mth.lerp(partialTicks, this.entity.zo, this.entity.getZ())
        );
    }

    @Unique
    private void carminite$setRotation(float yRot, float xRot, float roll) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation.rotationYXZ((float) Math.PI - yRot * (float) (Math.PI / 180.0), -xRot * (float) (Math.PI / 180.0), -roll * (float) (Math.PI / 180.0));
        FORWARDS.rotate(this.rotation, this.forwards);
        UP.rotate(this.rotation, this.up);
        LEFT.rotate(this.rotation, this.left);
        this.matrixPropertiesDirty |= 3;
    }
}
