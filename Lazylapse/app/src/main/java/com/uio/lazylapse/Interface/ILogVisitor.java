package com.uio.lazylapse.Interface;

/**
 * interface used to update the log display in {@link Controller}
 */
public interface ILogVisitor {
    void visit(String log);
}
