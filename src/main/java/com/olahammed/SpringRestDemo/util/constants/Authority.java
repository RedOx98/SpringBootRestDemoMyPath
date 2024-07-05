package com.olahammed.SpringRestDemo.util.constants;

public enum Authority {
    READ,
    WRITE,
    UPDATE,
    USER, // can update, delete self object and read anything
    ADMIN // can update, read, delete object any object
}
