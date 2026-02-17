package org.twightlight.talents.runes.categories.defense;

import org.twightlight.talents.Talents;
import org.twightlight.talents.runes.Rune;

public abstract class DefenseRune extends Rune {

    public DefenseRune(int tier) {
        super(Talents.getInstance().getRunesManager().getCategories().get(1), tier);
    }
}
