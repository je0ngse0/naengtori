package com.naengtori.dto;
import java.util.List;
public record Recipe(String name, List<String> ingredients, int minutes, List<String> steps) {}
