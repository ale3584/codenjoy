package com.codenjoy.dojo.moebius.model;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2016 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.services.LengthToXY;
import com.codenjoy.dojo.services.Point;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LevelImpl implements Level {
    private final LengthToXY xy;

    private String map;

    public LevelImpl(String map) {
        this.map = map;
        xy = new LengthToXY(getSize());
    }

    @Override
    public int getSize() {
        return (int) Math.sqrt(map.length());
    }

    @Override
    public List<Line> getLines() {
        List<Line> result = new LinkedList<Line>();

        for (int index = 0; index < map.length(); index++) {
            for (Elements el : Arrays.asList(Elements.LEFT_UP,
                    Elements.UP_RIGHT,
                    Elements.RIGHT_DOWN,
                    Elements.DOWN_LEFT,
                    Elements.LEFT_RIGHT,
                    Elements.UP_DOWN,
                    Elements.CROSS)) {
                if (map.charAt(index) == el.ch()) {
                    Point pt = xy.getXY(index);
                    result.add(new Line(pt, el));
                }
            }
        }

        return result;
    }
}
