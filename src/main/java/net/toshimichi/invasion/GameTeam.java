package net.toshimichi.invasion;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameTeam implements Team {

    private final List<Player> victims = new ArrayList<>();
    private Player owner;
    private String name;

    public GameTeam(Player owner) {
        this.owner = owner;
        newName();
    }

    /**
     * チーム名を更新します.
     */
    private void newName() {
        name = owner.getName() + "のチーム";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player player) {
        owner = player;
    }

    @Override
    public void addCitizen(Player player) {
        victims.add(player);
        newName();
    }

    @Override
    public void removeCitizen(Player player) {
        victims.remove(player);
    }

    @Override
    public List<Player> getCitizens() {
        return new ArrayList<>(victims);
    }
}
