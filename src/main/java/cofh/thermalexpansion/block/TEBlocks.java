package cofh.thermalexpansion.block;

import cofh.api.core.IInitializer;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.cell.BlockCell;
import cofh.thermalexpansion.block.device.BlockDevice;
import cofh.thermalexpansion.block.dynamo.BlockDynamo;
import cofh.thermalexpansion.block.machine.BlockMachine;
import cofh.thermalexpansion.block.simple.BlockFrame;

import java.util.ArrayList;

public class TEBlocks {

	private TEBlocks() {

	}

	public static void preInit() {

		blockMachine = new BlockMachine();
		blockCell = new BlockCell();
		blockFrame = new BlockFrame();
		//blockDynamo = new BlockDynamo();
		//blockDevice = new BlockDevice();

		initList.add(blockMachine);
		initList.add(blockCell);
		initList.add(blockFrame);
		//initList.add(blockDynamo);
		//initList.add(blockDevice);

		ThermalExpansion.proxy.addModelRegister(blockMachine);
		ThermalExpansion.proxy.addModelRegister(blockCell);
		ThermalExpansion.proxy.addModelRegister(blockFrame);

		for (int i = 0; i < initList.size(); i++) {
			initList.get(i).preInit();
		}
	}

	public static void initialize() {

		for (int i = 0; i < initList.size(); i++) {
			initList.get(i).initialize();
		}
	}

	public static void postInit() {

		for (int i = 0; i < initList.size(); i++) {
			initList.get(i).postInit();
		}
	}

	static ArrayList<IInitializer> initList = new ArrayList<IInitializer>();

	/* REFERENCES */
	public static BlockMachine blockMachine;
	public static BlockCell blockCell;
	public static BlockFrame blockFrame;
	public static BlockDynamo blockDynamo;
	public static BlockDevice blockDevice;

}
