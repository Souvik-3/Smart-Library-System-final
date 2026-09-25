package com.smartlibrary.controller;

import com.smartlibrary.Main;

public abstract class BaseController {
    protected Main mainApp;
    public void setMain(Main mainApp) {
        this.mainApp = mainApp;
    }
}
