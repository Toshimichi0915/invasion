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
