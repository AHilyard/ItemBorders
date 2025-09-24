package com.anthonyhilyard.iceberg.services;

import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IIcebergConfigSpecBuilder {
    IIcebergConfigSpecBuilder comment(String comment);
    IIcebergConfigSpecBuilder push(String path);
    IIcebergConfigSpecBuilder pop();
    
    <T> Supplier<T> define(String path, T defaultValue);
    <T> Supplier<T> define(String path, T defaultValue, Predicate<Object> validator);
    <T> Supplier<T> add(String path, T defaultValue);
    
    <K, V> Supplier<Map<K, V>> addSubconfig(String path, Map<K, V> defaultValue, 
                                           Function<Object, Boolean> keyValidator, 
                                           Function<Object, Boolean> valueValidator);
}