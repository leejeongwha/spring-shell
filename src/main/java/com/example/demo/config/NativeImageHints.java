package com.example.demo.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import static org.springframework.aot.hint.ExecutableMode.INVOKE;

/**
 * 오류 : Invalid logger interface org.hibernate.validator.internal.util.logging.Log
 * 참조 : https://github.com/oracle/graal/issues/5626
 */
@Configuration
@ImportRuntimeHints(NativeImageHints.class)
public class NativeImageHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        try {
            hints.reflection()
                .registerConstructor(org.hibernate.validator.internal.util.logging.Log_$logger.class.getConstructor(org.jboss.logging.Logger.class), INVOKE)
                .registerField(org.hibernate.validator.internal.util.logging.Messages_$bundle.class.getField("INSTANCE"));
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeHintsException(e);
        }
    }

    private static class RuntimeHintsException extends RuntimeException {
        RuntimeHintsException(Throwable cause) {
            super(cause);
        }
    }
}
