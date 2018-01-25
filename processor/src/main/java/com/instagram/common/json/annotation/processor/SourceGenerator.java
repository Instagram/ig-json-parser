package com.instagram.common.json.annotation.processor;

import javax.annotation.processing.Messager;

public interface SourceGenerator {
    String getInjectedFqcn();
    String getJavaCode(Messager messager);
}
