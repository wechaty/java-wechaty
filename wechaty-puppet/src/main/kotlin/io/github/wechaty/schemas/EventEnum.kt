package io.github.wechaty.io.github.wechaty.schemas

import io.github.wechaty.eventEmitter.Event

enum class EventEnum :Event{

    START,

    FRIENDSHIP,

    LOGIN,

    LOGOUT,

    MESSAGE,

    ROOM_INVITE,

    INVITE,

    ROOM_JOIN,

    JOIN,

    ROOM_LEAVE,

    LEAVE,

    ROOM_TOPIC,

    TOPIC,

    SCAN,

    DONG,

    ERROR,

    READY,

    RESET,

    HEART_BEAT,

    ON,
    OFF,

    WATCH_DOG;

}

