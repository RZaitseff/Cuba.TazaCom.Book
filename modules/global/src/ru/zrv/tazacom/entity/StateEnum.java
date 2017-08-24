package ru.zrv.tazacom.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum StateEnum implements EnumClass<String> {

    DONE("Done"),
    ACTIVE("Active"),
    POSTPONED("Postponed"),
    DELETED("Deleted"),
    RESUME("Resume");

    private String id;

    StateEnum() {
    }

    StateEnum(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static StateEnum fromId(String id) {
        for (StateEnum at : StateEnum.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}