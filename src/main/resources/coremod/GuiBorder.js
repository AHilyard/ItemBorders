var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')
var Opcodes = Java.type('org.objectweb.asm.Opcodes')

function initializeCoreMod() {
	return {
		'renderSlot': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.gui.Gui',
				"methodName": "m_168677_",
				"methodDesc": "(IIFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V"
			},
			"transformer": function(methodNode) {

				ASMAPI.log("INFO", "[ItemBorders] GuiBorder Transformer Called");

				var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

				var renderAndDecorateItemName = ASMAPI.mapMethod('m_174229_');
				var renderAndDecorateItem = ASMAPI.findFirstMethodCall(methodNode, ASMAPI.MethodType.VIRTUAL,
					"net/minecraft/client/renderer/entity/ItemRenderer",
					renderAndDecorateItemName,
					"(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V");
				
				var inst = methodNode.instructions.get(methodNode.instructions.indexOf(renderAndDecorateItem) - 5);

				methodNode.instructions.insertBefore(inst, new VarInsnNode(Opcodes.ALOAD, 7));
				methodNode.instructions.insertBefore(inst, new VarInsnNode(Opcodes.ALOAD, 5));
				methodNode.instructions.insertBefore(inst, new VarInsnNode(Opcodes.ILOAD, 1));
				methodNode.instructions.insertBefore(inst, new VarInsnNode(Opcodes.ILOAD, 2));
				methodNode.instructions.insertBefore(inst, ASMAPI.buildMethodCall(
					"com/anthonyhilyard/itemborders/ItemBorders",
					"renderBorder",
					"(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V",
					 ASMAPI.MethodType.STATIC));

				ASMAPI.log("INFO", "[ItemBorders] GuiBorder Transformer Complete");
				
				return methodNode;
			}
		}
	}
}