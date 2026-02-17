package org.twightlight.talents.runes.categories.offense;

import org.bukkit.event.Event;
import org.twightlight.talents.Talents;
import org.twightlight.talents.dispatcher.CustomListener;
import org.twightlight.talents.runes.Rune;

public abstract class OffenseRune extends Rune {

    public OffenseRune(int tier) {
        super(Talents.getInstance().getRunesManager().getCategories().get(0), tier);
    }
}
