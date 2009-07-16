package org.linkedprocess.os.errors;

/**
 * Author: josh
 * Date: Jun 30, 2009
 * Time: 3:44:43 PM
 */
public class UnsupportedScriptEngineException extends SchedulerException {
    public UnsupportedScriptEngineException(final String name) {
        super("script engine '" + name + "' is not supported");
    }
}
