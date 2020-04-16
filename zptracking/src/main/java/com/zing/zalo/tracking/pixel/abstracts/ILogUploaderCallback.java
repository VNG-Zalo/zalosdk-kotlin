package com.zing.zalo.tracking.pixel.abstracts;

import com.zing.zalo.tracking.pixel.model.Event;

import java.util.List;

public interface ILogUploaderCallback {
    void onCompleted(List<Event> events, boolean result);
}
