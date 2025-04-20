package org.twightlight.talents.talents.interfaces;

import org.twightlight.talents.talents.enums.TalentsCategory;

import java.util.List;

public interface Talent<T> {
    T handle(int level, List<Object> params);
    TalentsCategory getCategory();
    List<Integer> getCostList();
}
