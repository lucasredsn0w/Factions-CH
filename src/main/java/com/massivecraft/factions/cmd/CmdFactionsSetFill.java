package com.massivecraft.factions.cmd;

import java.util.LinkedHashSet;
import java.util.Set;

import com.massivecraft.factions.Perm;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MConf;
import com.massivecraft.massivecore.cmd.req.ReqHasPerm;
import com.massivecraft.massivecore.cmd.req.ReqIsPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.util.MUtil;


public class CmdFactionsSetFill extends CmdFactionsSetXSimple
{
	// -------------------------------------------- //
	// CONSTRUCT
	// -------------------------------------------- //
	
	public CmdFactionsSetFill()
	{
		// Aliases
		this.addAliases("f", "fill");

		// Requirements
		this.addRequirements(ReqIsPlayer.get());
		this.addRequirements(ReqHasPerm.get(Perm.SET_FILL.node));
	}

	// -------------------------------------------- //
	// OVERRIDE
	// -------------------------------------------- //
	
	@Override
	public Set<PS> getChunks()
	{
		// Common Startup
		final PS chunk = PS.valueOf(me).getChunk(true);
		final Set<PS> chunks = new LinkedHashSet<PS>();
		
		// What faction (aka color) resides there?
		// NOTE: Wilderness/None is valid. 
		final Faction color = BoardColl.get().getFactionAt(chunk);
		
		// We start where we are!
		chunks.add(chunk);
		
		// Flood!
		int max = MConf.get().setFillMax;
		floodSearch(chunks, color, max);
		
		// Limit Reached?
		if (chunks.size() >= max)
		{
			msg("<b>Fill limit of <h>%d <b>reached.", max);
			return null;
		}
		
		// OK!
		return chunks;
	}
	
	// -------------------------------------------- //
	// FLOOD FILL
	// -------------------------------------------- //
	
	public static void floodSearch(Set<PS> set, Faction color, int max)
	{
		// Clean
		if (set == null) throw new NullPointerException("set");
		if (color == null) throw new NullPointerException("color");
		
		// Expand
		Set<PS> expansion = new LinkedHashSet<PS>();
		for (PS chunk : set)
		{
			Set<PS> neighbours = MUtil.set(
				chunk.withChunkX(chunk.getChunkX() + 1),
				chunk.withChunkX(chunk.getChunkX() - 1),
				chunk.withChunkZ(chunk.getChunkZ() + 1),
				chunk.withChunkZ(chunk.getChunkZ() - 1)
			);
			
			for (PS neighbour : neighbours)
			{
				if (set.contains(neighbour)) continue;
				Faction faction = BoardColl.get().getFactionAt(neighbour);
				if (faction == null) continue;
				if (faction != color) continue;
				expansion.add(neighbour);
			}
		}
		set.addAll(expansion);
		
		// No Expansion?
		if (expansion.isEmpty()) return;
		
		// Reached Max?
		if (set.size() >= max) return;
		
		// Recurse
		floodSearch(set, color, max);
	}
	
}
