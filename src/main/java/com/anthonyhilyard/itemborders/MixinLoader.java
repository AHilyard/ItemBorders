package com.anthonyhilyard.itemborders;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@MCVersion(ForgeVersion.mcVersion)
public class MixinLoader implements IFMLLoadingPlugin
{
	public MixinLoader()
	{
		MixinBootstrap.init();
		Mixins.addConfiguration("itemborders.mixins.json");
	}

	@Override
	public String[] getASMTransformerClass() { return null; }

	@Override
	public String getModContainerClass() { return null; }

	@Override
	public String getSetupClass() { return null; }

	@Override
	public void injectData(Map<String, Object> data) { }

	@Override
	public String getAccessTransformerClass() { return null; }
}