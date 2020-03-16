package com.zing.zalo.tracking.pixel.abstracts;

import java.util.Map;

public interface IZPTracker {
    void track(String name, Map<String, Object> params);
}
