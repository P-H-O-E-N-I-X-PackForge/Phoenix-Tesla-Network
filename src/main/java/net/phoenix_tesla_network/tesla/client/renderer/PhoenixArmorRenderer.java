package net.phoenix_tesla_network.tesla.client.renderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixArmorItem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PhoenixArmorRenderer extends GeoArmorRenderer<PhoenixArmorItem> {

    /** Set while the wings are being drawn with their own texture. */
    private boolean wingsPass = false;

    public PhoenixArmorRenderer(PhoenixArmorItem phoenixItem) {
        super(new PhoenixArmorModel(phoenixItem));

        addRenderLayer(new GeoRenderLayer<PhoenixArmorItem>(this) {

            @Override
            public void render(PoseStack poseStack, PhoenixArmorItem animatable, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               float partialTick, int packedLight, int packedOverlay) {
                if (getCurrentSlot() == EquipmentSlot.CHEST) {
                    // the wings have their own texture, and the armor's texture has other things where the wings' UVs
                    // are, so each pass only draws its own part: this one only the wings
                    PhoenixArmorRenderer self = (PhoenixArmorRenderer) getRenderer();
                    self.wingsPass = true;
                    ResourceLocation wingsTex = ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network",
                            "textures/armor/phoenix_wings.png");

                    RenderType wingsRenderType = RenderType.armorCutoutNoCull(wingsTex);

                    try {
                        getRenderer().reRender(model, poseStack, bufferSource, animatable, wingsRenderType,
                                bufferSource.getBuffer(wingsRenderType), partialTick, packedLight, packedOverlay,
                                0xFFFFFFFF);
                    } finally {
                        self.wingsPass = false;
                    }
                }
            }
        });
    }

    @Override
    protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
        setAllVisible(false);
        // the belt is part of the leggings
        for (String waist : new String[] { "bipedWaist", "armorWaist" }) {
            boolean visible = currentSlot == EquipmentSlot.LEGS;
            getGeoModel().getBone(waist).ifPresent(b -> {
                b.setHidden(!visible);
                b.setChildrenHidden(!visible);
            });
        }
        switch (currentSlot) {
            case CHEST -> {
                if (wingsPass) {
                    // only the wings: the body and arms keep their bones but not their cubes
                    for (String part : new String[] { "armorBody", "armorRightArm", "armorLeftArm" }) {
                        getGeoModel().getBone(part).ifPresent(b -> {
                            b.setHidden(true);
                            b.setChildrenHidden(false);
                        });
                    }
                    for (String wing : new String[] { "wing_1", "wing_2" }) {
                        getGeoModel().getBone(wing).ifPresent(b -> {
                            b.setHidden(false);
                            b.setChildrenHidden(false);
                        });
                    }
                } else {
                    setBoneVisible(this.body, true);
                    setBoneVisible(this.rightArm, true);
                    setBoneVisible(this.leftArm, true);
                    // the wings are drawn by the layer above, with their own texture, not with the armor's
                    for (String wing : new String[] { "wing_1", "wing_2" }) {
                        getGeoModel().getBone(wing).ifPresent(b -> {
                            b.setHidden(true);
                            b.setChildrenHidden(true);
                        });
                    }
                }
            }
            case LEGS -> {
                setBoneVisible(this.rightLeg, true);
                setBoneVisible(this.leftLeg, true);
            }
            case FEET -> {
                setBoneVisible(this.rightBoot, true);
                setBoneVisible(this.leftBoot, true);
            }
            case HEAD -> setBoneVisible(this.head, true);
        }
    }
}
