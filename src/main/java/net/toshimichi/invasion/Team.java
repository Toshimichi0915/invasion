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

import java.util.List;

/**
 * プレイヤーの所有するチームを表します.
 */
public interface Team {

    /**
     * チームを表す短いタグを返します.
     * @return タグ
     */
    String getTag();

    /**
     * チーム名を返します.
     * @return チーム名
     */
    String getName();

    /**
     * チームの所有者を返します.
     * @return チームの所有者
     */
    Player getOwner();

    /**
     * チームの所有者を変更します.
     * @param player 新たなチームの所有者
     */
    void setOwner(Player player);

    /**
     * 市民をチームに追加します.
     * @param player 新規市民
     */
    void addCitizen(Player player);

    /**
     * 市民をチームから削除します.
     * @param player 削除する市民
     */
    void removeCitizen(Player player);

    /**
     * チームに所属している市民を返します.
     * @return チームに所属している市民
     */
    List<Player> getCitizens();
}
