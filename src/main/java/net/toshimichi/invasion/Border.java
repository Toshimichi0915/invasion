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

public class Border {
    private final int phase;
    private final int peace;
    private final double to;
    private final int time;

    public Border(int phase, int peace, double to, int time) {
        this.phase = phase;
        this.peace = peace;
        this.to = to;
        this.time = time;
    }

    public int getPhase() {
        return phase;
    }

    public int getPeace() {
        return peace;
    }

    public double getTo() {
        return to;
    }

    public int getTime() {
        return time;
    }
}
