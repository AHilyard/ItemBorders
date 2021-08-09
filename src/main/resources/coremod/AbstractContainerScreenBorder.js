var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')
var Opcodes = Java.type('org.objectweb.asm.Opcodes')

function initializeCoreMod() {
	return {
		'renderSlot': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.gui.screens.inventory.AbstractContainerScreen',
				"methodName": "m_97799_",
				"methodDesc": "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/inventory/Slot;)V"
			},
			"transformer": function(methodNode) {

				ASMAPI.log("INFO", "[ItemBorders] AbstractContainerScreenBorder Transformer Called");

				var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
				
				var enableDepthTestName = ASMAPI.mapMethod('m_69482_');
				var enableDepthTest = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.STATIC, "com/mojang/blaze3d/systems/RenderSystem", enableDepthTestName, "()V");

				methodNode.instructions.insert(enableDepthTest, ASMAPI.buildMethodCall(
					"com/anthonyhilyard/itemborders/ItemBorders",
					"renderBorder",
					"(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/inventory/Slot;)V",
					 ASMAPI.MethodType.STATIC));

				methodNode.instructions.insert(enableDepthTest, new VarInsnNode(Opcodes.ALOAD, 2));
				methodNode.instructions.insert(enableDepthTest, new VarInsnNode(Opcodes.ALOAD, 1));
				
				ASMAPI.log("INFO", "[ItemBorders] AbstractContainerScreenBorder Transformer Complete");
				
				return methodNode;
			}
		}
	}
}