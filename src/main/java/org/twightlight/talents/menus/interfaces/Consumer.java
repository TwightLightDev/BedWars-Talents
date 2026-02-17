package org.twightlight.talents.menus.interfaces;

public interface Consumer<T, R> {
    T accept(R param);
}
