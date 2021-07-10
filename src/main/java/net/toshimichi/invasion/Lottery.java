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

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * 出現比を用いた抽選を行います.
 *
 * @param <T> 抽選の対象
 */
public class Lottery<T> implements ConfigurationSerializable {
    private static final Random rand = new SecureRandom();
    private int size;
    private final LinkedHashMap<Integer, T> map = new LinkedHashMap<>();

    /**
     * 抽選の対象を追加します.
     *
     * @param ratio 出現比
     * @param t     対象
     */
    public void add(int ratio, T t) {
        size += ratio;
        map.put(size, t);
    }

    /**
     * 抽選を行います.
     *
     * @return 抽選結果
     */
    public T draw() {
        return draw(rand);
    }

    /**
     * 乱数のアルゴリズムを指定して抽選を行います.
     *
     * @param random 乱数のアルゴリズム
     * @return 抽選結果
     */
    public T draw(Random random) {
        int i = random.nextInt(size);
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            if (i < entry.getKey()) return entry.getValue();
        }
        throw new RuntimeException();
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();
        int sum = 0;
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            result.put(Integer.toString(entry.getKey() - sum), entry.getValue());
            sum += entry.getKey();
        }
        return result;
    }

    public static <R> Lottery<R> deserialize(Map<Integer, R> map) {
        Lottery<R> lottery = new Lottery<>();
        map.forEach(lottery::add);
        return lottery;
    }
}
