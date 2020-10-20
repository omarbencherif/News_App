package com.ecm2425.newsapp;

interface TaskCompleted {
    /**
     * This is a callback method so that the MainActivity can wait until the news is fetched.
     */
    void taskCompleted();
}
