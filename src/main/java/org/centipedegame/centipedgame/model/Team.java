package org.centipedegame.centipedgame.model;

import org.bukkit.entity.Player;

public class Team {
    private final Player player1;
    private final Player player2;
    private final Player player3;
    private final String nameTeam;
    private final Player organizator;

    public Team(Player player1, Player player2, Player player3, String nameTeam, Player organizator) {
        this.player1 = player1;
        this.player2 = player2;
        this.player3 = player3;
        this.nameTeam = nameTeam;
        this.organizator = organizator;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Player getPlayer3() {
        return player3;
    }

    public String getNameTeam() {
        return nameTeam;
    }

    public Player getOrganizator() {
        return organizator;
    }
} 