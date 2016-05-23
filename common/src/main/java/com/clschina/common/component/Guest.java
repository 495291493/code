package com.clschina.common.component;

import java.util.ArrayList;
import java.util.List;

public class Guest implements Login {

    @Override
    public String getId() {
        return "guest";
    }

    @Override
    public String getName() {
        return "Guest";
    }

    @Override
    public List<String> getPrivileges() {
        return new ArrayList<String>();
    }
}
