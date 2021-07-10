/*
 * Copyright (C) 2021 Toshimichi0915
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.toshimichi.invasion;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameTeam implements Team {

    private final List<Player> victims = new ArrayList<>();
    private final String tag;
    private Player owner;
    private String name;

    public GameTeam(Player owner, String tag) {
        this.owner = owner;
        this.tag = tag;
        newName();
    }

    /**
     * チーム名を更新します.
     */
    private void newName() {
        name = owner.getName() + "帝国";
    }

    @Override
    public String getTag() {
        return tag;
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
