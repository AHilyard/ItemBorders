package com.anthonyhilyard.itemborders.util;

public class ColorUtil
{
	public static Integer parseColor(String colorString)
	{
		if (colorString.startsWith("#"))
		{
			colorString = colorString.substring(1);
		}

		if (colorString.startsWith("0x"))
		{
			colorString = colorString.substring(2);
		}

		try
		{
			return Integer.parseUnsignedInt(colorString, 16);
		}
		catch (NumberFormatException e)
		{
			// Do nothing.
		}

		// If we didn't get a parsable color, return the color value of a standard Minecraft color.
		switch (colorString.toLowerCase())
		{
			case "black":
				return 0xFF000000;
			case "dark_blue":
				return 0xFF0000AA;
			case "dark_green":
				return 0xFF00AA00;
			case "dark_aqua":
				return 0xFF00AAAA;
			case "dark_red":
				return 0xFFAA0000;
			case "dark_purple":
				return 0xFFAA00AA;
			case "gold":
				return 0xFFFFAA00;
			case "gray":
				return 0xFFAAAAAA;
			case "dark_gray":
				return 0xFF555555;
			case "blue":
				return 0xFF5555FF;
			case "green":
				return 0xFF55FF55;
			case "aqua":
				return 0xFF55FFFF;
			case "red":
				return 0xFFFF5555;
			case "light_purple":
				return 0xFFFF55FF;
			case "yellow":
				return 0xFFFFFF55;
			case "white":
				return 0xFFFFFFFF;
			default:
				return null;
		}
	}
}
