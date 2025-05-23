package org.centipedegame.centipedgame.model;

import org.bukkit.Location;
import org.bukkit.Material;

public class BlockInfo {
    private final Location location;
    private final Material material;

    public BlockInfo(Location location, Material material) {
        this.location = location;
        this.material = material;
    }

    public Location getLocation() {
        return location;
    }

    public Material getMaterial() {
        return material;
    }
} 