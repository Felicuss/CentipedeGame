package org.centipedegame.centipedgame.utils;

import org.bukkit.entity.Player;
import org.centipedegame.centipedgame.model.Team;

public class MessageUtils {

    public static void sendTeamTitle(Team team, String title, String subtitle) {
        Player player1 = team.getPlayer1();
        Player player2 = team.getPlayer2();
        Player player3 = team.getPlayer3();
        Player organizator = team.getOrganizator();
        
        player1.sendTitle(title, subtitle, 10, 40, 10);
        player2.sendTitle(title, subtitle, 10, 40, 10);
        player3.sendTitle(title, subtitle, 10, 40, 10);
        organizator.sendTitle(title, subtitle, 10, 40, 10);
    }

    public static void sendTeamActionBar(Team team, String message) {
        Player player1 = team.getPlayer1();
        Player player2 = team.getPlayer2();
        Player player3 = team.getPlayer3();
        Player organizator = team.getOrganizator();
        
        player1.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(message));
        player2.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(message));
        player3.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(message));
        organizator.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(message));
    }

    public static void sendTeamMessage(Team team, String message) {
        Player player1 = team.getPlayer1();
        Player player2 = team.getPlayer2();
        Player player3 = team.getPlayer3();
        Player organizator = team.getOrganizator();
        
        player1.sendMessage(message);
        player2.sendMessage(message);
        player3.sendMessage(message);
        organizator.sendMessage(message);
    }
} 