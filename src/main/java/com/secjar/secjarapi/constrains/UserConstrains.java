package com.secjar.secjarapi.constrains;

public interface UserConstrains {
    long MIN_FILE_DELETION_DELAY = 1_000_000L;
    long MAX_FILE_DELETION_DELAY = 2_592_000_000L;

    long MIN_SESSION_TIME = 900_000L;
    long MAX_SESSION_TIME = 3_600_000L;

}
