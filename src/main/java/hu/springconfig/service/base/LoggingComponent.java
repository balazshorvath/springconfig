package hu.springconfig.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoggingComponent {
    protected final Logger log = LoggerFactory.getLogger(getClass());
}
